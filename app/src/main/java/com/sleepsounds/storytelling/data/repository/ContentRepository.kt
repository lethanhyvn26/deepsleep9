package com.sleepsounds.storytelling.data.repository

import com.sleepsounds.storytelling.data.local.dao.CategoryDao
import com.sleepsounds.storytelling.data.local.dao.PlaybackHistoryDao
import com.sleepsounds.storytelling.data.local.dao.PodcastDao
import com.sleepsounds.storytelling.data.local.dao.SleepPresetDao
import com.sleepsounds.storytelling.data.local.dao.SoundDao
import com.sleepsounds.storytelling.data.local.dao.SyncStateDao
import com.sleepsounds.storytelling.data.local.dao.UserMixDao
import com.sleepsounds.storytelling.data.local.dao.UserPresetDao
import com.sleepsounds.storytelling.data.local.entity.PlaybackHistoryEntity
import com.sleepsounds.storytelling.data.local.entity.PodcastEntity
import com.sleepsounds.storytelling.data.local.entity.SleepPresetItemEntity
import com.sleepsounds.storytelling.data.local.entity.UserMixEntity
import com.sleepsounds.storytelling.data.local.entity.UserMixItemEntity
import com.sleepsounds.storytelling.data.local.entity.UserPresetCustomizationEntity
import com.sleepsounds.storytelling.data.local.entity.UserPresetItemEntity
import com.sleepsounds.storytelling.data.remote.ContentCategory
import com.sleepsounds.storytelling.data.sync.CatalogRefreshBus
import com.sleepsounds.storytelling.domain.model.AudioTrack
import com.sleepsounds.storytelling.domain.model.ContentType
import com.sleepsounds.storytelling.domain.model.HistoryItemType
import com.sleepsounds.storytelling.domain.model.MixSoundState
import com.sleepsounds.storytelling.domain.model.PlaybackHistoryItem
import com.sleepsounds.storytelling.domain.model.Podcast
import com.sleepsounds.storytelling.domain.model.SleepPreset
import com.sleepsounds.storytelling.domain.model.UserMix
import com.sleepsounds.storytelling.domain.model.toAudioTrack
import com.sleepsounds.storytelling.domain.model.toCategoryLabel
import com.sleepsounds.storytelling.domain.model.toDomain
import com.sleepsounds.storytelling.domain.model.toMixState
import com.sleepsounds.storytelling.domain.model.toPodcast
import com.sleepsounds.storytelling.domain.model.toPodcastChapter
import com.sleepsounds.storytelling.domain.model.toSleepPreset
import com.sleepsounds.storytelling.util.MediaAssetResolver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central repository exposing reactive streams and CRUD operations for sounds, scenes,
 * bedtime stories, custom user mixes, favorites, and playback history.
 * Automatically reflects VIP subscription status, the 3-Hour Ad-Free & Full VIP Pass,
 * and individual 12-hour Rewarded Ad item unlocks.
 */
@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class ContentRepository @Inject constructor(
    private val soundDao: SoundDao,
    private val sleepPresetDao: SleepPresetDao,
    private val podcastDao: PodcastDao,
    private val categoryDao: CategoryDao,
    private val userMixDao: UserMixDao,
    private val userPresetDao: UserPresetDao,
    private val syncStateDao: SyncStateDao,
    private val playbackHistoryDao: PlaybackHistoryDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val mediaAssetResolver: MediaAssetResolver,
    private val catalogRefreshBus: CatalogRefreshBus,
) {
    private data class EntitlementSnapshot(
        val revision: Int,
        val hasFullVip: Boolean,
        val unlockedIds: Set<String>,
    ) {
        fun isLocked(rawIsPremium: Boolean, itemId: String): Boolean =
            rawIsPremium && !hasFullVip && itemId !in unlockedIds
    }

    private val entitlementFlow: Flow<EntitlementSnapshot> = combine(
        catalogRefreshBus.revision,
        userPreferencesRepository.isPremium,
        userPreferencesRepository.activeRewardedUnlockedIds,
    ) { rev, hasFullVip, unlocked ->
        EntitlementSnapshot(
            revision = rev,
            hasFullVip = hasFullVip,
            unlockedIds = unlocked,
        )
    }

    private fun <T> observeOnCatalogRevision(
        block: suspend (EntitlementSnapshot) -> T,
    ): Flow<T> = entitlementFlow.flatMapLatest { snapshot ->
        flow { emit(block(snapshot)) }
    }

    private suspend fun currentEntitlementSnapshot(): EntitlementSnapshot = EntitlementSnapshot(
        revision = catalogRefreshBus.revision.value,
        hasFullVip = userPreferencesRepository.isPremium.first(),
        unlockedIds = userPreferencesRepository.activeRewardedUnlockedIds.first(),
    )

    fun observeSounds(): Flow<List<AudioTrack>> =
        observeOnCatalogRevision { entitlement ->
            soundDao.getAll().map { row ->
                val track = row.toAudioTrack().attachResolvedArtwork()
                track.copy(isPremium = entitlement.isLocked(track.isPremium, track.id))
            }
        }

    fun observeSleepPresets(): Flow<List<SleepPreset>> =
        observeOnCatalogRevision { entitlement ->
            fetchAllSleepPresets(entitlement)
        }

    fun observeSleepPresetTracks(): Flow<List<AudioTrack>> =
        observeOnCatalogRevision { entitlement ->
            fetchAllSleepPresets(entitlement).map { scene ->
                scene.toAudioTrack().attachResolvedArtwork()
            }
        }

    fun observePodcasts(): Flow<List<Podcast>> =
        observeOnCatalogRevision { entitlement ->
            podcastDao.getAll().hydratePodcastChapters(entitlement)
        }

    fun observePodcastsByLanguage(language: String): Flow<List<Podcast>> =
        observeOnCatalogRevision { entitlement ->
            podcastDao.getByLanguage(language).hydratePodcastChapters(entitlement)
        }

    fun observePodcastsByCategory(category: String): Flow<List<Podcast>> =
        observeOnCatalogRevision { entitlement ->
            podcastDao.getByCategory(category).hydratePodcastChapters(entitlement)
        }

    fun observeFavoritePodcasts(): Flow<List<Podcast>> =
        observeOnCatalogRevision { entitlement ->
            podcastDao.getFavorites().hydratePodcastChapters(entitlement)
        }

    fun observeFavoriteSleepPresets(): Flow<List<SleepPreset>> =
        observeOnCatalogRevision { entitlement ->
            fetchAllSleepPresets(entitlement).filter { it.isFavorite }
        }

    suspend fun getPodcastById(id: String): Podcast? {
        val row = podcastDao.getById(id) ?: return null
        val chapters = podcastDao.getChaptersForPodcast(id).map { it.toPodcastChapter() }
        val entitlement = currentEntitlementSnapshot()
        val pod = row.toPodcast(chapters).attachPodcastCover()
        return pod.copy(isPremium = entitlement.isLocked(pod.isPremium, pod.id))
    }

    fun observeFavoriteSounds(): Flow<List<AudioTrack>> =
        observeOnCatalogRevision { entitlement ->
            soundDao.getFavorites().map { row ->
                val track = row.toAudioTrack().attachResolvedArtwork()
                track.copy(isPremium = entitlement.isLocked(track.isPremium, track.id))
            }
        }

    suspend fun toggleSoundFavorite(soundId: String): Boolean {
        val current = soundDao.getById(soundId) ?: return false
        val nextFlag = if (current.isFavorite == 1) 0 else 1
        soundDao.update(current.copy(isFavorite = nextFlag))
        syncFavoritePreference(soundId, nextFlag == 1)
        catalogRefreshBus.notifyCatalogUpdated()
        return nextFlag == 1
    }

    suspend fun toggleSceneFavorite(presetId: String): Boolean {
        val current = sleepPresetDao.getById(presetId) ?: return false
        val nextFlag = if (current.isFavorite == 1) 0 else 1
        sleepPresetDao.update(current.copy(isFavorite = nextFlag))
        syncFavoritePreference(presetId, nextFlag == 1)
        catalogRefreshBus.notifyCatalogUpdated()
        return nextFlag == 1
    }

    suspend fun togglePodcastFavorite(podcastId: String): Boolean {
        val current = podcastDao.getById(podcastId) ?: return false
        val nextFlag = if (current.isFavorite == 1) 0 else 1
        podcastDao.update(current.copy(isFavorite = nextFlag))
        syncFavoritePreference(podcastId, nextFlag == 1)
        catalogRefreshBus.notifyCatalogUpdated()
        return nextFlag == 1
    }

    private suspend fun syncFavoritePreference(itemId: String, isNowFavorite: Boolean) {
        if (isNowFavorite) {
            userPreferencesRepository.addUserFavorite(itemId)
        } else {
            userPreferencesRepository.removeUserFavorite(itemId)
        }
    }

    fun observePlaybackHistory(limit: Int = 50): Flow<List<PlaybackHistoryItem>> =
        playbackHistoryDao.observeRecent(limit).map { entities ->
            entities.map { entity ->
                val item = entity.toDomain()
                item.copy(imagePath = item.imagePath?.let(mediaAssetResolver::toCoilUri))
            }
        }

    suspend fun recordPlaybackHistory(
        itemId: String,
        itemType: HistoryItemType,
        title: String,
        subtitle: String = "",
        imagePath: String? = null,
        durationSeconds: Int = 0,
        isPremium: Boolean = false,
        mixLayersJson: String? = null,
    ) {
        if (itemId.isBlank() || title.isBlank()) return
        val compositeKey = "${itemType.name}:$itemId"
        playbackHistoryDao.upsert(
            PlaybackHistoryEntity(
                itemKey = compositeKey,
                itemId = itemId,
                itemType = itemType.name,
                title = title,
                subtitle = subtitle,
                imagePath = imagePath,
                durationSeconds = durationSeconds,
                isPremium = if (isPremium) 1 else 0,
                mixLayersJson = mixLayersJson,
                playedAt = System.currentTimeMillis(),
            ),
        )
        playbackHistoryDao.trimToLimit(100)
    }

    suspend fun clearPlaybackHistory() {
        playbackHistoryDao.clearAll()
    }

    suspend fun getSoundById(id: String): AudioTrack? {
        val raw = soundDao.getById(id)?.toAudioTrack()?.attachResolvedArtwork() ?: return null
        val entitlement = currentEntitlementSnapshot()
        return raw.copy(isPremium = entitlement.isLocked(raw.isPremium, raw.id))
    }

    suspend fun getSleepPresetById(id: String): SleepPreset? {
        val entitlement = currentEntitlementSnapshot()
        return fetchPresetById(id, entitlement)?.attachSceneImages()
    }

    suspend fun getSleepPresetFactoryById(id: String): SleepPreset? {
        val row = sleepPresetDao.getById(id) ?: return null
        val soundMap = soundDao.getAll().associateBy { it.id }
        val defaultItems = sleepPresetDao.getItemsForPreset(id)
        val entitlement = currentEntitlementSnapshot()
        val preset = row.toSleepPreset(defaultItems, soundMap)
        return preset.copy(
            isPremium = entitlement.isLocked(preset.isPremium, preset.id),
            items = preset.items.map { item ->
                item.copy(isPremium = entitlement.isLocked(item.isPremium, item.soundId))
            },
            isUserModified = false,
        ).attachSceneImages()
    }

    suspend fun getSleepPresetTrackById(id: String): AudioTrack? =
        getSleepPresetById(id)?.toAudioTrack()?.attachResolvedArtwork()

    fun observeSceneCategories(): Flow<List<ContentCategory>> = observeMediaCategories()

    fun observeMediaCategories(): Flow<List<ContentCategory>> =
        observeOnCatalogRevision {
            categoryDao.getMediaCategories().map { row ->
                ContentCategory(
                    id = row.id,
                    label = row.toCategoryLabel(),
                    emoji = row.emoji,
                )
            }
        }

    fun observePodcastCategories(): Flow<List<ContentCategory>> =
        observeOnCatalogRevision {
            categoryDao.getPodcastCategories().map { row ->
                ContentCategory(
                    id = row.id,
                    label = row.toCategoryLabel(),
                    emoji = row.emoji,
                )
            }
        }

    fun observeMixerSounds(): Flow<List<MixSoundState>> =
        observeOnCatalogRevision { entitlement ->
            soundDao.getAll().map { row ->
                val state = row.toMixState()
                state.copy(isPremium = entitlement.isLocked(state.isPremium, state.soundId))
            }
        }

    fun observeUserMixes(): Flow<List<UserMix>> = observeOnCatalogRevision { entitlement ->
        val savedMixes = userMixDao.getAll()
        if (savedMixes.isEmpty()) return@observeOnCatalogRevision emptyList()
        val soundLookup = soundDao.getAll().associateBy { it.id }
        val groupedLayers = userMixDao.getAllItems().groupBy { it.mixId }
        savedMixes.map { header ->
            val layers = groupedLayers[header.id].orEmpty()
            UserMix(
                id = header.id,
                name = header.name,
                createdAt = header.createdAt,
                items = layers.mapNotNull { layer ->
                    val mixState = soundLookup[layer.soundId]?.toMixState(
                        isActive = true,
                        volume = layer.volumeLevel,
                    ) ?: return@mapNotNull null
                    mixState.copy(isPremium = entitlement.isLocked(mixState.isPremium, mixState.soundId))
                },
            )
        }
    }

    suspend fun saveUserMix(name: String, activeSounds: List<MixSoundState>) {
        val newMixId = UUID.randomUUID().toString()
        val header = UserMixEntity(
            id = newMixId,
            name = name,
            createdAt = System.currentTimeMillis(),
        )
        val layerRows = activeSounds.map { state ->
            UserMixItemEntity(
                mixId = newMixId,
                soundId = state.soundId,
                volumeLevel = state.volume,
            )
        }
        userMixDao.saveMix(header, layerRows)
        catalogRefreshBus.notifyCatalogUpdated()
    }

    suspend fun getUserMixById(mixId: String): UserMix? {
        val header = userMixDao.getById(mixId) ?: return null
        val soundLookup = soundDao.getAll().associateBy { it.id }
        val layers = userMixDao.getItemsForMix(mixId)
        val entitlement = currentEntitlementSnapshot()
        return UserMix(
            id = header.id,
            name = header.name,
            createdAt = header.createdAt,
            items = layers.mapNotNull { layer ->
                val mixState = soundLookup[layer.soundId]?.toMixState(
                    isActive = true,
                    volume = layer.volumeLevel,
                ) ?: return@mapNotNull null
                mixState.copy(isPremium = entitlement.isLocked(mixState.isPremium, mixState.soundId))
            },
        )
    }

    suspend fun updateUserMix(mixId: String, activeSounds: List<MixSoundState>) {
        val header = userMixDao.getById(mixId) ?: return
        val layerRows = activeSounds.map { state ->
            UserMixItemEntity(
                mixId = mixId,
                soundId = state.soundId,
                volumeLevel = state.volume,
            )
        }
        userMixDao.saveMix(header, layerRows)
        catalogRefreshBus.notifyCatalogUpdated()
    }

    suspend fun deleteUserMix(mixId: String) {
        userMixDao.deleteItemsForMix(mixId)
        userMixDao.deleteMix(mixId)
        catalogRefreshBus.notifyCatalogUpdated()
    }

    suspend fun saveUserPresetCustomization(
        presetId: String,
        activeSounds: List<MixSoundState>,
    ) {
        val manifestVer = syncStateDao.get()?.manifestVersion ?: 0
        userPresetDao.saveCustomization(
            customization = UserPresetCustomizationEntity(
                presetId = presetId,
                isModified = 1,
                updatedAt = System.currentTimeMillis(),
                catalogVersion = manifestVer,
            ),
            items = activeSounds.map { state ->
                UserPresetItemEntity(
                    presetId = presetId,
                    soundId = state.soundId,
                    volume = state.volume,
                )
            },
        )
        catalogRefreshBus.notifyCatalogUpdated()
    }

    suspend fun resetUserPresetCustomization(presetId: String) {
        userPresetDao.resetCustomization(presetId)
        catalogRefreshBus.notifyCatalogUpdated()
    }

    suspend fun isPresetUserModified(presetId: String): Boolean =
        userPresetDao.getCustomization(presetId)?.isModified == 1

    suspend fun getCoverImagePaths(): List<String> =
        fetchAllSleepPresets(currentEntitlementSnapshot())
            .mapNotNull { scene -> scene.imagePath?.takeIf { it.isNotBlank() } }
            .distinct()

    private suspend fun resolveEffectivePresetItems(
        presetId: String,
    ): Pair<List<SleepPresetItemEntity>, Boolean> {
        val overrideHeader = userPresetDao.getCustomization(presetId)
        if (overrideHeader?.isModified == 1) {
            val customRows = userPresetDao.getItemsForPreset(presetId)
            if (customRows.isNotEmpty()) {
                val converted = customRows.map { row ->
                    SleepPresetItemEntity(
                        presetId = row.presetId,
                        soundId = row.soundId,
                        volume = row.volume,
                    )
                }
                return converted to true
            }
        }
        return sleepPresetDao.getItemsForPreset(presetId) to false
    }

    private suspend fun fetchAllSleepPresets(entitlement: EntitlementSnapshot): List<SleepPreset> {
        val sceneRows = sleepPresetDao.getAll()
        if (sceneRows.isEmpty()) return emptyList()
        val soundLookup = soundDao.getAll().associateBy { it.id }
        val defaultItemsByScene = sleepPresetDao.getAllItems().groupBy { it.presetId }
        val customizedIds = userPresetDao.getModifiedPresetIds().toSet()
        val customItemsByScene = if (customizedIds.isNotEmpty()) {
            userPresetDao.getAllItems().groupBy { it.presetId }
        } else {
            emptyMap()
        }

        return sceneRows.map { row ->
            val customList = if (row.id in customizedIds) {
                customItemsByScene[row.id].orEmpty()
            } else {
                emptyList()
            }
            val (effectiveLayers, customized) = if (customList.isNotEmpty()) {
                customList.map { item ->
                    SleepPresetItemEntity(
                        presetId = item.presetId,
                        soundId = item.soundId,
                        volume = item.volume,
                    )
                } to true
            } else {
                defaultItemsByScene[row.id].orEmpty() to false
            }
            val preset = row.toSleepPreset(effectiveLayers, soundLookup)
            preset.copy(
                isPremium = entitlement.isLocked(preset.isPremium, preset.id),
                items = preset.items.map { item ->
                    item.copy(isPremium = entitlement.isLocked(item.isPremium, item.soundId))
                },
                isUserModified = customized,
            ).attachSceneImages()
        }
    }

    private suspend fun fetchPresetById(id: String, entitlement: EntitlementSnapshot): SleepPreset? {
        val row = sleepPresetDao.getById(id) ?: return null
        val soundLookup = soundDao.getAll().associateBy { it.id }
        val (effectiveLayers, customized) = resolveEffectivePresetItems(id)
        val preset = row.toSleepPreset(effectiveLayers, soundLookup)
        return preset.copy(
            isPremium = entitlement.isLocked(preset.isPremium, preset.id),
            items = preset.items.map { item ->
                item.copy(isPremium = entitlement.isLocked(item.isPremium, item.soundId))
            },
            isUserModified = customized,
        )
    }

    private fun SleepPreset.attachSceneImages(): SleepPreset {
        val mappedLayers = items.map { layer ->
            layer.copy(symbolPath = resolveSymbolToCoilUri(layer.symbolPath))
        }
        val coverUri = mediaAssetResolver.resolveImageUri(
            trackId = id,
            contentType = ContentType.SLEEP_PRESET,
            dbPath = imagePath,
            categories = categories,
        )?.let { uri ->
            if (uri.startsWith("http://") || uri.startsWith("https://") || uri.startsWith("file://")) {
                uri
            } else {
                mediaAssetResolver.toCoilUri(uri)
            }
        }
        val previewSymbol = resolveSymbolToCoilUri(displaySymbolPath)
            ?: mappedLayers.randomOrNull()?.symbolPath
        return copy(
            items = mappedLayers,
            imagePath = coverUri,
            displaySymbolPath = previewSymbol,
        )
    }

    private fun resolveSymbolToCoilUri(rawPath: String?): String? {
        if (rawPath.isNullOrBlank()) return null
        if (rawPath.startsWith("http://") || rawPath.startsWith("https://") || rawPath.startsWith("file://")) {
            return rawPath
        }
        val candidate = mediaAssetResolver.resolveSymbolUri(rawPath) ?: rawPath
        return mediaAssetResolver.toCoilUri(candidate)
    }

    private fun AudioTrack.attachResolvedArtwork(): AudioTrack {
        val resolvedUri = when (type) {
            ContentType.SOUND ->
                mediaAssetResolver.resolveSoundSymbolUri(id, imagePath)
            ContentType.SLEEP_PRESET,
            ContentType.PODCAST ->
                mediaAssetResolver.resolveImageUri(
                    trackId = id,
                    contentType = type,
                    dbPath = imagePath,
                    categories = categories,
                )?.let { uri ->
                    if (uri.startsWith("http") || uri.startsWith("file://")) {
                        uri
                    } else {
                        mediaAssetResolver.toCoilUri(uri)
                    }
                }
        }
        return copy(imagePath = resolvedUri)
    }

    private suspend fun List<PodcastEntity>.hydratePodcastChapters(
        entitlement: EntitlementSnapshot,
    ): List<Podcast> {
        if (isEmpty()) return emptyList()
        val chaptersGrouped = podcastDao.getAllChapters()
            .groupBy { it.podcastId }
            .mapValues { (_, list) ->
                list.sortedBy { it.chapterIndex }.map { it.toPodcastChapter() }
            }
        return map { row ->
            val pod = row.toPodcast(chaptersGrouped[row.id].orEmpty()).attachPodcastCover()
            pod.copy(isPremium = entitlement.isLocked(pod.isPremium, pod.id))
        }
    }

    private fun Podcast.attachPodcastCover(): Podcast {
        val resolvedCover = mediaAssetResolver.resolveImageUri(
            trackId = id,
            contentType = ContentType.PODCAST,
            dbPath = imagePath,
            categories = categories,
        )
        return copy(imagePath = resolvedCover)
    }
}
