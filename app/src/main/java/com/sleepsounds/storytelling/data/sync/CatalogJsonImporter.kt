package com.sleepsounds.storytelling.data.sync

import androidx.sqlite.db.SupportSQLiteDatabase
import com.sleepsounds.storytelling.data.local.DeepSleepDatabase
import com.sleepsounds.storytelling.data.local.entity.PodcastChapterEntity
import com.sleepsounds.storytelling.data.local.entity.PodcastEntity
import com.sleepsounds.storytelling.data.local.entity.SleepPresetItemEntity
import com.sleepsounds.storytelling.data.local.entity.SoundEntity
import com.sleepsounds.storytelling.data.local.entity.SyncStateEntity
import com.sleepsounds.storytelling.data.remote.ContentCatalog
import com.sleepsounds.storytelling.data.remote.ContentCategory
import com.sleepsounds.storytelling.data.remote.ContentPodcast
import com.sleepsounds.storytelling.data.remote.ContentScene
import com.sleepsounds.storytelling.data.remote.ContentSound
import com.sleepsounds.storytelling.util.R2Log
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Transactional SQLite importer that merges Firebase Realtime Database or JSON catalog payloads
 * into Room while preserving user favorites and custom scene layer overrides.
 */
@Singleton
class CatalogJsonImporter @Inject constructor(
    private val database: DeepSleepDatabase,
    private val catalogRefreshBus: CatalogRefreshBus,
) {
    suspend fun importAll(
        mediaCategoriesJson: String,
        podcastCategoriesJson: String,
        soundsJson: String,
        mixesJson: String,
        podcastsJson: String,
        manifest: ContentManifest,
    ) {
        database.runInTransaction {
            val sqlDb = database.openHelper.writableDatabase
            val snapshotCustomizations = captureUserCustomizations(sqlDb)
            val snapshotPresetLayers = captureUserPresetLayers(sqlDb)

            writeMediaCategoriesFromJson(mediaCategoriesJson)
            writePodcastCategoriesFromJson(podcastCategoriesJson)
            writeSoundsFromJson(soundsJson)
            writeScenesFromJson(mixesJson)
            writePodcastsFromJson(podcastsJson)
            reapplyUserCustomizations(sqlDb, snapshotCustomizations, snapshotPresetLayers)
        }
        database.syncStateDao().upsert(
            SyncStateEntity(
                manifestVersion = manifest.version,
                lastSyncAt = System.currentTimeMillis(),
                soundsHash = manifest.hashes["sounds"].orEmpty(),
                mixesHash = manifest.hashes["mixes"].orEmpty(),
                podcastsHash = manifest.hashes["podcasts"].orEmpty(),
            ),
        )
        catalogRefreshBus.notifyCatalogUpdated()
        R2Log.i("Catalog JSON imported (manifest v${manifest.version})")
    }

    suspend fun importFromRtdb(catalog: ContentCatalog) {
        val activeSounds = catalog.enabledSounds
        val activeScenes = catalog.enabledScenes
        val activePodcasts = catalog.enabledPodcasts

        database.runInTransaction {
            val sqlDb = database.openHelper.writableDatabase
            val snapshotCustomizations = captureUserCustomizations(sqlDb)
            val snapshotPresetLayers = captureUserPresetLayers(sqlDb)

            writeMediaCategoryList(catalog.mediaCategories.filter { it.enabled })
            writePodcastCategoryList(catalog.podcastCategories.filter { it.enabled })
            writeSoundList(activeSounds)
            writeSceneList(activeScenes)
            writePodcastList(activePodcasts)
            reapplyUserCustomizations(sqlDb, snapshotCustomizations, snapshotPresetLayers)
        }
        database.syncStateDao().upsert(
            SyncStateEntity(
                manifestVersion = 0,
                lastSyncAt = System.currentTimeMillis(),
                soundsHash = catalog.baseUrl,
                mixesHash = "${activeScenes.size}",
                podcastsHash = "${activePodcasts.size}",
            ),
        )
        catalogRefreshBus.notifyCatalogUpdated()
        R2Log.i(
            "RTDB catalog imported: sounds=${activeSounds.size}, scenes=${activeScenes.size}, " +
                "podcasts=${activePodcasts.size}, base=${catalog.baseUrl}",
        )
    }

    private fun writeMediaCategoryList(categories: List<ContentCategory>) {
        val sqlDb = database.openHelper.writableDatabase
        sqlDb.execSQL("DELETE FROM media_category")
        categories.forEachIndexed { idx, cat ->
            sqlDb.execSQL(
                "INSERT OR REPLACE INTO media_category (id, label, emoji, sort_index) VALUES (?, ?, ?, ?)",
                arrayOf<Any?>(cat.id, cat.label, cat.emoji, idx),
            )
        }
    }

    private fun writePodcastCategoryList(categories: List<ContentCategory>) {
        val sqlDb = database.openHelper.writableDatabase
        sqlDb.execSQL("DELETE FROM podcast_category")
        categories.forEachIndexed { idx, cat ->
            sqlDb.execSQL(
                "INSERT OR REPLACE INTO podcast_category (id, label, emoji, sort_index) VALUES (?, ?, ?, ?)",
                arrayOf<Any?>(cat.id, cat.label.ifBlank { cat.id }, cat.emoji, idx),
            )
        }
    }

    private fun writeSoundList(sounds: List<ContentSound>) {
        val sqlDb = database.openHelper.writableDatabase
        val favoriteSoundIds = queryFavoriteIds(sqlDb, "sound")
        sqlDb.execSQL("DELETE FROM sleep_preset_item")
        sqlDb.execSQL("DELETE FROM user_preset_item")
        sqlDb.execSQL("DELETE FROM sound")

        sounds.forEachIndexed { idx, item ->
            val row = SoundEntity(
                id = item.id,
                name = item.title,
                volume = item.defaultVolume,
                categories = item.categories.encodeJsonArray(),
                isFavorite = if (item.id in favoriteSoundIds) 1 else 0,
                symbolPath = item.symbol,
                audioPath = item.file,
                unlockDay = item.unlockDay,
                isPremium = if (item.isPremium) 1 else 0,
            )
            sqlDb.execSQL(
                "INSERT OR REPLACE INTO sound " +
                    "(id, name, volume, categories, is_favorite, symbol_path, audio_path, unlock_day, is_premium, sort_index) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any?>(
                    row.id,
                    row.name,
                    row.volume,
                    row.categories,
                    row.isFavorite,
                    row.symbolPath,
                    row.audioPath,
                    row.unlockDay,
                    row.isPremium,
                    idx,
                ),
            )
        }
    }

    private fun writeSceneList(scenes: List<ContentScene>) {
        val sqlDb = database.openHelper.writableDatabase
        val favoriteSceneIds = queryFavoriteIds(sqlDb, "sleep_preset")
        sqlDb.execSQL("DELETE FROM sleep_preset_item")
        sqlDb.execSQL("DELETE FROM sleep_preset")

        scenes.forEachIndexed { idx, scene ->
            sqlDb.execSQL(
                "INSERT OR REPLACE INTO sleep_preset " +
                    "(id, name, categories, is_favorite, is_premium, image_path, sort_index) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any?>(
                    scene.id,
                    scene.title,
                    scene.categories.encodeJsonArray(),
                    if (scene.id in favoriteSceneIds) 1 else 0,
                    if (scene.isPremium) 1 else 0,
                    scene.cover,
                    idx,
                ),
            )
            scene.layers.forEachIndexed { layerIdx, layer ->
                if (!hasSoundRow(sqlDb, layer.soundId)) {
                    R2Log.w("Skipping scene layer ${scene.id} -> ${layer.soundId} (sound not found)")
                    return@forEachIndexed
                }
                sqlDb.execSQL(
                    "INSERT OR REPLACE INTO sleep_preset_item " +
                        "(preset_id, sound_id, volume, sort_index) VALUES (?, ?, ?, ?)",
                    arrayOf<Any?>(scene.id, layer.soundId, layer.volume, layerIdx),
                )
            }
        }
    }

    private fun writePodcastList(podcasts: List<ContentPodcast>) {
        val sqlDb = database.openHelper.writableDatabase
        val favoritePodcastIds = queryFavoriteIds(sqlDb, "podcast")
        sqlDb.execSQL("DELETE FROM podcast_chapter")
        sqlDb.execSQL("DELETE FROM podcast")

        podcasts.forEachIndexed { idx, pod ->
            val row = PodcastEntity(
                id = pod.id,
                title = pod.title,
                author = pod.author,
                narrator = pod.narrator,
                summary = pod.summary,
                categories = pod.categories.encodeJsonArray(),
                imagePath = pod.cover,
                isPremium = if (pod.isPremium) 1 else 0,
                freeChapters = pod.freeChapters,
                backgroundSoundId = pod.backgroundSoundId,
                isFavorite = if (pod.id in favoritePodcastIds) 1 else 0,
                language = pod.language.ifBlank { "en" },
                durationSeconds = 0,
            )
            sqlDb.execSQL(
                "INSERT OR REPLACE INTO podcast " +
                    "(id, title, author, narrator, summary, categories, image_path, " +
                    "is_premium, free_chapters, background_sound_id, is_favorite, language, " +
                    "duration_seconds, sort_index) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any?>(
                    row.id,
                    row.title,
                    row.author,
                    row.narrator,
                    row.summary,
                    row.categories,
                    row.imagePath,
                    row.isPremium,
                    row.freeChapters,
                    row.backgroundSoundId,
                    row.isFavorite,
                    row.language,
                    0,
                    idx,
                ),
            )
            for (chapter in pod.chapters) {
                sqlDb.execSQL(
                    "INSERT OR REPLACE INTO podcast_chapter " +
                        "(id, podcast_id, title, chapter_index, duration_seconds, audio_path) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                    arrayOf<Any?>(
                        chapter.id,
                        pod.id,
                        chapter.title,
                        chapter.index,
                        0,
                        chapter.file,
                    ),
                )
            }
        }
    }

    private fun List<String>.encodeJsonArray(): String {
        val arr = JSONArray()
        for (entry in this) {
            if (entry.isNotBlank()) arr.put(entry)
        }
        return arr.toString()
    }

    private fun writeMediaCategoriesFromJson(jsonText: String) {
        val arr = JSONObject(jsonText).getJSONArray("items")
        val sqlDb = database.openHelper.writableDatabase
        sqlDb.execSQL("DELETE FROM media_category")
        for (i in 0 until arr.length()) {
            val node = arr.getJSONObject(i)
            sqlDb.execSQL(
                "INSERT OR REPLACE INTO media_category (id, label, emoji, sort_index) VALUES (?, ?, ?, ?)",
                arrayOf<Any?>(
                    node.getString("id"),
                    node.getString("label"),
                    node.optString("emoji").takeIf { it.isNotBlank() },
                    i,
                ),
            )
        }
    }

    private fun writePodcastCategoriesFromJson(jsonText: String) {
        val arr = JSONObject(jsonText).getJSONArray("items")
        val sqlDb = database.openHelper.writableDatabase
        sqlDb.execSQL("DELETE FROM podcast_category")
        for (i in 0 until arr.length()) {
            val node = arr.getJSONObject(i)
            val id = node.getString("id")
            sqlDb.execSQL(
                "INSERT OR REPLACE INTO podcast_category (id, label, emoji, sort_index) VALUES (?, ?, ?, ?)",
                arrayOf<Any?>(
                    id,
                    node.optString("label", id),
                    node.optString("emoji").takeIf { it.isNotBlank() },
                    i,
                ),
            )
        }
    }

    private fun writeSoundsFromJson(jsonText: String) {
        val arr = JSONObject(jsonText).getJSONArray("items")
        val sqlDb = database.openHelper.writableDatabase
        val favoriteSoundIds = queryFavoriteIds(sqlDb, "sound")
        sqlDb.execSQL("DELETE FROM sleep_preset_item")
        sqlDb.execSQL("DELETE FROM user_preset_item")
        sqlDb.execSQL("DELETE FROM sound")

        for (i in 0 until arr.length()) {
            val node = arr.getJSONObject(i)
            val id = node.getString("id")
            val row = SoundEntity(
                id = id,
                name = node.getString("title"),
                volume = node.optDouble("defaultVolume", 0.75).toFloat(),
                categories = node.getJSONArray("categories").toString(),
                isFavorite = if (id in favoriteSoundIds) 1 else 0,
                symbolPath = node.getString("symbol"),
                audioPath = node.getString("file"),
                unlockDay = node.optInt("unlockDay", 0),
                isPremium = if (node.optBoolean("isPremium", false)) 1 else 0,
            )
            sqlDb.execSQL(
                "INSERT OR REPLACE INTO sound " +
                    "(id, name, volume, categories, is_favorite, symbol_path, audio_path, unlock_day, is_premium, sort_index) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any?>(
                    row.id,
                    row.name,
                    row.volume,
                    row.categories,
                    row.isFavorite,
                    row.symbolPath,
                    row.audioPath,
                    row.unlockDay,
                    row.isPremium,
                    i,
                ),
            )
        }
    }

    private fun writeScenesFromJson(jsonText: String) {
        val arr = JSONObject(jsonText).getJSONArray("items")
        val sqlDb = database.openHelper.writableDatabase
        val favoriteSceneIds = queryFavoriteIds(sqlDb, "sleep_preset")
        sqlDb.execSQL("DELETE FROM sleep_preset_item")
        sqlDb.execSQL("DELETE FROM sleep_preset")

        for (i in 0 until arr.length()) {
            val node = arr.getJSONObject(i)
            val sceneId = node.getString("id")
            sqlDb.execSQL(
                "INSERT OR REPLACE INTO sleep_preset " +
                    "(id, name, categories, is_favorite, is_premium, image_path, sort_index) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any?>(
                    sceneId,
                    node.getString("title"),
                    node.getJSONArray("categories").toString(),
                    if (sceneId in favoriteSceneIds) 1 else 0,
                    if (node.optBoolean("isPremium", false)) 1 else 0,
                    node.optString("cover", ""),
                    i,
                ),
            )
            val layersArr = node.optJSONArray("sounds") ?: JSONArray()
            for (j in 0 until layersArr.length()) {
                val layerNode = layersArr.getJSONObject(j)
                val soundId = layerNode.getString("soundId")
                if (!hasSoundRow(sqlDb, soundId)) {
                    R2Log.w("Skipping JSON scene layer $sceneId -> $soundId (sound missing)")
                    continue
                }
                val itemRow = SleepPresetItemEntity(
                    presetId = sceneId,
                    soundId = soundId,
                    volume = layerNode.optDouble("volume", 0.75).toFloat(),
                )
                sqlDb.execSQL(
                    "INSERT OR REPLACE INTO sleep_preset_item " +
                        "(preset_id, sound_id, volume, sort_index) VALUES (?, ?, ?, ?)",
                    arrayOf<Any?>(itemRow.presetId, itemRow.soundId, itemRow.volume, j),
                )
            }
        }
    }

    private fun writePodcastsFromJson(jsonText: String) {
        val arr = JSONObject(jsonText).getJSONArray("items")
        val sqlDb = database.openHelper.writableDatabase
        val favoritePodcastIds = queryFavoriteIds(sqlDb, "podcast")
        sqlDb.execSQL("DELETE FROM podcast_chapter")
        sqlDb.execSQL("DELETE FROM podcast")

        for (i in 0 until arr.length()) {
            val node = arr.getJSONObject(i)
            val podId = node.getString("id")
            val chaptersArr = node.optJSONArray("chapters") ?: JSONArray()
            val singleAudioFile = node.optString("file", "").trimStart('/')
            val row = PodcastEntity(
                id = podId,
                title = node.getString("title"),
                author = node.optString("author").takeIf { it.isNotBlank() },
                narrator = node.optString("narrator").takeIf { it.isNotBlank() },
                summary = node.optString("summary").takeIf { it.isNotBlank() },
                categories = node.getJSONArray("categories").toString(),
                imagePath = node.optString("cover", ""),
                isPremium = if (node.optBoolean("isPremium", false)) 1 else 0,
                freeChapters = node.optInt("freeChapters", 0),
                backgroundSoundId = node.optString("backgroundSoundId").takeIf { it.isNotBlank() },
                isFavorite = if (podId in favoritePodcastIds) 1 else 0,
                language = node.optString("language", "en"),
                durationSeconds = 0,
            )
            sqlDb.execSQL(
                "INSERT OR REPLACE INTO podcast " +
                    "(id, title, author, narrator, summary, categories, image_path, " +
                    "is_premium, free_chapters, background_sound_id, is_favorite, language, " +
                    "duration_seconds, sort_index) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any?>(
                    row.id,
                    row.title,
                    row.author,
                    row.narrator,
                    row.summary,
                    row.categories,
                    row.imagePath,
                    row.isPremium,
                    row.freeChapters,
                    row.backgroundSoundId,
                    row.isFavorite,
                    row.language,
                    0,
                    i,
                ),
            )
            if (chaptersArr.length() == 0 && singleAudioFile.isNotEmpty()) {
                sqlDb.execSQL(
                    "INSERT OR REPLACE INTO podcast_chapter " +
                        "(id, podcast_id, title, chapter_index, duration_seconds, audio_path) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                    arrayOf<Any?>(
                        "${podId}_audio",
                        podId,
                        node.getString("title"),
                        0,
                        0,
                        singleAudioFile,
                    ),
                )
            }
            for (j in 0 until chaptersArr.length()) {
                val chNode = chaptersArr.getJSONObject(j)
                val chRow = PodcastChapterEntity(
                    id = chNode.getString("id"),
                    podcastId = podId,
                    title = chNode.getString("title"),
                    chapterIndex = chNode.optInt("index", j),
                    durationSeconds = 0,
                    audioPath = chNode.getString("file"),
                )
                sqlDb.execSQL(
                    "INSERT OR REPLACE INTO podcast_chapter " +
                        "(id, podcast_id, title, chapter_index, duration_seconds, audio_path) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                    arrayOf<Any?>(
                        chRow.id,
                        chRow.podcastId,
                        chRow.title,
                        chRow.chapterIndex,
                        0,
                        chRow.audioPath,
                    ),
                )
            }
        }
    }

    private fun queryFavoriteIds(sqlDb: SupportSQLiteDatabase, tableName: String): Set<String> {
        val result = mutableSetOf<String>()
        sqlDb.query("SELECT id FROM $tableName WHERE is_favorite = 1").use { cursor ->
            while (cursor.moveToNext()) {
                result.add(cursor.getString(0))
            }
        }
        return result
    }

    private data class CustomizationSnapshot(
        val presetId: String,
        val isModified: Int,
        val updatedAt: Long,
        val catalogVersion: Int,
    )

    private data class PresetLayerSnapshot(
        val presetId: String,
        val soundId: String,
        val volume: Float,
    )

    private fun captureUserCustomizations(sqlDb: SupportSQLiteDatabase): List<CustomizationSnapshot> {
        val snapshots = mutableListOf<CustomizationSnapshot>()
        sqlDb.query(
            "SELECT preset_id, is_modified, updated_at, catalog_version FROM user_preset_customization",
        ).use { cursor ->
            while (cursor.moveToNext()) {
                snapshots.add(
                    CustomizationSnapshot(
                        presetId = cursor.getString(0),
                        isModified = cursor.getInt(1),
                        updatedAt = cursor.getLong(2),
                        catalogVersion = cursor.getInt(3),
                    ),
                )
            }
        }
        return snapshots
    }

    private fun captureUserPresetLayers(sqlDb: SupportSQLiteDatabase): List<PresetLayerSnapshot> {
        val snapshots = mutableListOf<PresetLayerSnapshot>()
        sqlDb.query("SELECT preset_id, sound_id, volume FROM user_preset_item").use { cursor ->
            while (cursor.moveToNext()) {
                snapshots.add(
                    PresetLayerSnapshot(
                        presetId = cursor.getString(0),
                        soundId = cursor.getString(1),
                        volume = cursor.getFloat(2),
                    ),
                )
            }
        }
        return snapshots
    }

    private fun reapplyUserCustomizations(
        sqlDb: SupportSQLiteDatabase,
        customizations: List<CustomizationSnapshot>,
        layers: List<PresetLayerSnapshot>,
    ) {
        if (customizations.isEmpty() && layers.isEmpty()) return

        val validSceneIds = queryAllTableIds(sqlDb, "sleep_preset")
        val validSoundIds = queryAllTableIds(sqlDb, "sound")

        for (entry in customizations) {
            if (entry.presetId !in validSceneIds) continue
            sqlDb.execSQL(
                "INSERT OR REPLACE INTO user_preset_customization " +
                    "(preset_id, is_modified, updated_at, catalog_version) VALUES (?, ?, ?, ?)",
                arrayOf<Any?>(entry.presetId, entry.isModified, entry.updatedAt, entry.catalogVersion),
            )
        }

        for (layer in layers) {
            if (layer.presetId !in validSceneIds || layer.soundId !in validSoundIds) continue
            sqlDb.execSQL(
                "INSERT OR REPLACE INTO user_preset_item " +
                    "(preset_id, sound_id, volume) VALUES (?, ?, ?)",
                arrayOf<Any?>(layer.presetId, layer.soundId, layer.volume),
            )
        }
    }

    private fun queryAllTableIds(sqlDb: SupportSQLiteDatabase, tableName: String): Set<String> {
        val ids = mutableSetOf<String>()
        sqlDb.query("SELECT id FROM $tableName").use { cursor ->
            while (cursor.moveToNext()) {
                ids.add(cursor.getString(0))
            }
        }
        return ids
    }

    private fun hasSoundRow(sqlDb: SupportSQLiteDatabase, soundId: String): Boolean {
        sqlDb.query("SELECT 1 FROM sound WHERE id = ? LIMIT 1", arrayOf(soundId)).use { cursor ->
            return cursor.moveToFirst()
        }
    }
}
