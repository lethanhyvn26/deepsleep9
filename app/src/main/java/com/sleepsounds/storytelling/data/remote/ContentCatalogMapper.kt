package com.sleepsounds.storytelling.data.remote

import com.google.firebase.database.DataSnapshot

private fun Any?.asBooleanOrDefault(default: Boolean): Boolean = when (this) {
    is Boolean -> this
    is Number -> toInt() != 0
    is String -> when (trim().lowercase()) {
        "true", "1", "yes" -> true
        "false", "0", "no" -> false
        else -> default
    }
    else -> default
}
private fun Any?.asFloatOrDefault(default: Float): Float = when (this) {
    is Number -> toFloat()
    is String -> trim().toFloatOrNull() ?: default
    else -> default
}
private fun Any?.asIntOrDefault(default: Int): Int = when (this) {
    is Number -> toInt()
    is String -> trim().toIntOrNull() ?: default
    else -> default
}
private fun Any?.asTrimmedString(): String = when (this) {
    null -> ""
    is String -> trim()
    else -> toString().trim()
}
@Suppress("UNCHECKED_CAST")
private fun Any?.toNestedKeyedMaps(): Map<String, Map<String, Any?>> {
    val outer = this as? Map<*, *> ?: return emptyMap()
    return buildMap {
        for ((k, v) in outer) {
            val cleanKey = k?.toString()?.trim().orEmpty()
            if (cleanKey.isEmpty()) continue
            val innerMap = when (v) {
                is Map<*, *> -> v.entries.associate { (ik, iv) -> ik.toString() to iv }
                else -> emptyMap()
            }
            put(cleanKey, innerMap)
        }
    }
}
private fun Map<String, Any?>.readField(vararg candidateKeys: String): Any? {
    for (candidate in candidateKeys) {
        if (containsKey(candidate)) return this[candidate]
    }
    return null
}
private fun DataSnapshot.extractNestedValue(): Any? {
    if (!hasChildren()) return value
    return buildMap {
        for (child in children) {
            val childKey = child.key ?: continue
            put(childKey, child.extractNestedValue())
        }
    }
}
private fun DataSnapshot.extractFieldMap(): Map<String, Any?> = buildMap {
    if (!hasChildren()) return@buildMap
    for (child in children) {
        val childKey = child.key ?: continue
        put(childKey, child.extractNestedValue())
    }
}
private fun DataSnapshot.extractKeyedMaps(): Map<String, Map<String, Any?>> = buildMap {
    for (child in children) {
        val childKey = child.key?.trim().orEmpty()
        if (childKey.isNotEmpty()) {
            put(childKey, child.extractFieldMap())
        }
    }
}
private fun splitCsvTags(raw: String): List<String> =
    raw.split(',')
        .map { it.trim().trim('"') }
        .filter { it.isNotBlank() }
private fun extractTagsFromMap(map: Map<*, *>): List<String> {
    val pairs = map.entries.mapNotNull { (k, v) ->
        val keyStr = k?.toString()?.trim().orEmpty()
        if (keyStr.isEmpty()) null else keyStr to v
    }
    val indexedKeys = pairs.all { it.first.toIntOrNull() != null }
    return if (indexedKeys) {
        pairs.sortedBy { it.first.toInt() }
            .mapNotNull { it.second?.toString()?.trim() }
            .filter { it.isNotBlank() }
    } else {
        pairs.mapNotNull { (keyStr, flagVal) ->
            if (flagVal.asBooleanOrDefault(default = true)) keyStr else null
        }
    }
}
internal fun parseCategoryIds(value: Any?): List<String> = when (value) {
    null -> emptyList()
    is String -> splitCsvTags(value)
    is Collection<*> -> value.mapNotNull { it?.toString()?.trim() }.filter { it.isNotBlank() }
    is Map<*, *> -> extractTagsFromMap(value)
    else -> splitCsvTags(value.toString())
}.distinct()
internal fun normalizeBaseUrl(raw: String): String {
    val clean = raw.trim()
    if (clean.isEmpty()) return ""
    return if (clean.endsWith("/")) clean else "$clean/"
}
/**
 * Parses `/content` snapshots from Firebase Realtime Database into [ContentCatalog] DTOs.
 */
object ContentCatalogMapper {

    fun fromSnapshot(snapshot: DataSnapshot): ContentCatalog {
        if (!snapshot.exists()) return ContentCatalog()
        return map(
            baseUrl = snapshot.child(KEY_BASE_URL).value.asTrimmedString(),
            minAppVersion = snapshot.child(KEY_MIN_APP_VERSION).value.asIntOrDefault(0),
            mediaCategoryChildren = snapshot.child(KEY_MEDIA_CATEGORIES).extractKeyedMaps(),
            podcastCategoryChildren = snapshot.child(KEY_PODCAST_CATEGORIES).extractKeyedMaps(),
            soundChildren = snapshot.child(KEY_SOUNDS).extractKeyedMaps(),
            sceneChildren = snapshot.child(KEY_SCENES).extractKeyedMaps(),
            podcastChildren = snapshot.child(KEY_PODCASTS).extractKeyedMaps(),
        )
    }

    fun map(
        baseUrl: String,
        mediaCategoryChildren: Map<String, Map<String, Any?>>,
        podcastCategoryChildren: Map<String, Map<String, Any?>>,
        soundChildren: Map<String, Map<String, Any?>>,
        sceneChildren: Map<String, Map<String, Any?>>,
        podcastChildren: Map<String, Map<String, Any?>>,
        minAppVersion: Int = 0,
    ): ContentCatalog {
        val parsedMediaCategories = mediaCategoryChildren.entries.mapNotNull { (id, map) ->
            mapCategory(id, map)
        }
        val parsedPodcastCategories = podcastCategoryChildren.entries.mapNotNull { (id, map) ->
            mapCategory(id, map)
        }
        val parsedSounds = soundChildren.entries.mapNotNull { (id, map) ->
            mapSound(id, map)
        }
        val parsedScenes = sceneChildren.entries.mapNotNull { (id, map) ->
            mapScene(id, map)
        }
        val parsedPodcasts = podcastChildren.entries.mapNotNull { (id, map) ->
            mapPodcast(id, map)
        }
        return ContentCatalog(
            baseUrl = normalizeBaseUrl(baseUrl),
            minAppVersion = minAppVersion,
            mediaCategories = parsedMediaCategories,
            podcastCategories = parsedPodcastCategories,
            sounds = parsedSounds,
            scenes = parsedScenes,
            podcasts = parsedPodcasts,
        )
    }

    private fun mapCategory(
        rawId: String,
        fields: Map<String, Any?>,
    ): ContentCategory? {
        val cleanId = rawId.trim()
        if (cleanId.isEmpty()) return null
        return ContentCategory(
            id = cleanId,
            label = fields.readField(KEY_LABEL).asTrimmedString().ifBlank { cleanId },
            emoji = fields.readField(KEY_EMOJI).asTrimmedString().takeIf { it.isNotBlank() },
            enabled = fields.readField(KEY_ENABLED).asBooleanOrDefault(default = true),
        )
    }

    private fun mapSound(
        rawId: String,
        fields: Map<String, Any?>,
    ): ContentSound? {
        val cleanId = rawId.trim()
        val displayTitle = fields.readField(KEY_TITLE).asTrimmedString()
        val audioFile = fields.readField(KEY_FILE).asTrimmedString().trimStart('/')
        if (cleanId.isEmpty() || displayTitle.isEmpty() || audioFile.isEmpty()) return null
        return ContentSound(
            id = cleanId,
            title = displayTitle,
            file = audioFile,
            symbol = fields.readField(KEY_SYMBOL).asTrimmedString().trimStart('/'),
            categories = parseCategoryIds(fields.readField(KEY_CATEGORIES)),
            defaultVolume = fields.readField(KEY_DEFAULT_VOLUME, KEY_DEFAULT_VOLUME_CAMEL).asFloatOrDefault(0.75f),
            isPremium = fields.readField(KEY_IS_PREMIUM, KEY_IS_PREMIUM_CAMEL).asBooleanOrDefault(default = false),
            unlockDay = fields.readField(KEY_UNLOCK_DAY, KEY_UNLOCK_DAY_CAMEL).asIntOrDefault(0),
            enabled = fields.readField(KEY_ENABLED).asBooleanOrDefault(default = true),
        )
    }

    private fun mapScene(
        rawId: String,
        fields: Map<String, Any?>,
    ): ContentScene? {
        val cleanId = rawId.trim()
        val displayTitle = fields.readField(KEY_TITLE).asTrimmedString()
        if (cleanId.isEmpty() || displayTitle.isEmpty()) return null
        val sceneLayers = fields.readField(KEY_LAYERS).toNestedKeyedMaps().entries.mapNotNull { (soundKey, layerMap) ->
            val sid = soundKey.trim()
            if (sid.isEmpty()) return@mapNotNull null
            ContentSceneLayer(
                soundId = sid,
                volume = layerMap.readField(KEY_VOLUME).asFloatOrDefault(0.75f),
            )
        }
        return ContentScene(
            id = cleanId,
            title = displayTitle,
            cover = fields.readField(KEY_COVER).asTrimmedString().trimStart('/'),
            categories = parseCategoryIds(fields.readField(KEY_CATEGORIES)),
            isPremium = fields.readField(KEY_IS_PREMIUM, KEY_IS_PREMIUM_CAMEL).asBooleanOrDefault(default = false),
            enabled = fields.readField(KEY_ENABLED).asBooleanOrDefault(default = true),
            layers = sceneLayers,
        )
    }

    private fun mapPodcast(
        rawId: String,
        fields: Map<String, Any?>,
    ): ContentPodcast? {
        val cleanId = rawId.trim()
        val displayTitle = fields.readField(KEY_TITLE).asTrimmedString()
        if (cleanId.isEmpty() || displayTitle.isEmpty()) return null

        val fallbackFile = fields.readField(KEY_FILE).asTrimmedString().trimStart('/')
        val explicitChapters = fields.readField(KEY_CHAPTERS).toNestedKeyedMaps().entries
            .mapIndexedNotNull { idx, (chapterKey, chapterFields) ->
                val chId = chapterFields.readField(KEY_ID).asTrimmedString().ifBlank { chapterKey.trim() }
                val chFile = chapterFields.readField(KEY_FILE).asTrimmedString().trimStart('/')
                val chTitle = chapterFields.readField(KEY_TITLE).asTrimmedString().ifBlank { chapterKey.trim() }
                if (chId.isEmpty() || chFile.isEmpty()) return@mapIndexedNotNull null
                ContentChapter(
                    id = chId,
                    title = chTitle,
                    index = chapterFields.readField(KEY_INDEX).asIntOrDefault(idx),
                    durationSeconds = 0,
                    file = chFile,
                )
            }
            .sortedWith(compareBy({ it.index }, { it.id }))

        val resolvedChapters = explicitChapters.ifEmpty {
            if (fallbackFile.isEmpty()) {
                emptyList()
            } else {
                listOf(
                    ContentChapter(
                        id = "${cleanId}_audio",
                        title = displayTitle,
                        index = 0,
                        durationSeconds = 0,
                        file = fallbackFile,
                    ),
                )
            }
        }
        if (resolvedChapters.isEmpty()) return null

        return ContentPodcast(
            id = cleanId,
            title = displayTitle,
            author = fields.readField(KEY_AUTHOR).asTrimmedString().takeIf { it.isNotBlank() },
            narrator = fields.readField(KEY_NARRATOR).asTrimmedString().takeIf { it.isNotBlank() },
            summary = fields.readField(KEY_SUMMARY).asTrimmedString().takeIf { it.isNotBlank() },
            cover = fields.readField(KEY_COVER).asTrimmedString().trimStart('/'),
            file = fallbackFile.ifBlank { resolvedChapters.first().file },
            durationSeconds = 0,
            categories = parseCategoryIds(fields.readField(KEY_CATEGORIES)),
            isPremium = fields.readField(KEY_IS_PREMIUM, KEY_IS_PREMIUM_CAMEL).asBooleanOrDefault(default = false),
            freeChapters = fields.readField(KEY_FREE_CHAPTERS, KEY_FREE_CHAPTERS_CAMEL).asIntOrDefault(0),
            language = fields.readField(KEY_LANGUAGE).asTrimmedString().ifBlank { "en" },
            enabled = fields.readField(KEY_ENABLED).asBooleanOrDefault(default = true),
            backgroundSoundId = fields.readField(KEY_BACKGROUND_SOUND_ID, KEY_BACKGROUND_SOUND_ID_CAMEL)
                .asTrimmedString()
                .takeIf { it.isNotBlank() },
            chapters = resolvedChapters,
        )
    }

    const val KEY_BASE_URL = "base_url"
    const val KEY_MIN_APP_VERSION = "min_app_version"
    const val KEY_MEDIA_CATEGORIES = "media_categories"
    const val KEY_PODCAST_CATEGORIES = "podcast_categories"
    const val KEY_SOUNDS = "sounds"
    const val KEY_SCENES = "scenes"
    const val KEY_PODCASTS = "podcasts"
    const val KEY_ENABLED = "enabled"
    const val KEY_LABEL = "label"
    const val KEY_EMOJI = "emoji"
    const val KEY_TITLE = "title"
    const val KEY_FILE = "file"
    const val KEY_SYMBOL = "symbol"
    const val KEY_CATEGORIES = "categories"
    const val KEY_DEFAULT_VOLUME = "default_volume"
    const val KEY_DEFAULT_VOLUME_CAMEL = "defaultVolume"
    const val KEY_IS_PREMIUM = "is_premium"
    const val KEY_IS_PREMIUM_CAMEL = "isPremium"
    const val KEY_UNLOCK_DAY = "unlock_day"
    const val KEY_UNLOCK_DAY_CAMEL = "unlockDay"
    const val KEY_COVER = "cover"
    const val KEY_LAYERS = "layers"
    const val KEY_VOLUME = "volume"
    const val KEY_CHAPTERS = "chapters"
    const val KEY_ID = "id"
    const val KEY_INDEX = "index"
    const val KEY_DURATION_SECONDS = "duration_seconds"
    const val KEY_DURATION_SECONDS_CAMEL = "durationSeconds"
    const val KEY_AUTHOR = "author"
    const val KEY_NARRATOR = "narrator"
    const val KEY_SUMMARY = "summary"
    const val KEY_FREE_CHAPTERS = "free_chapters"
    const val KEY_FREE_CHAPTERS_CAMEL = "freeChapters"
    const val KEY_LANGUAGE = "language"
    const val KEY_BACKGROUND_SOUND_ID = "background_sound_id"
    const val KEY_BACKGROUND_SOUND_ID_CAMEL = "backgroundSoundId"
}
