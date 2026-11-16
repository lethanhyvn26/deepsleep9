package com.sleepsounds.storytelling.data.sync

import com.sleepsounds.storytelling.BuildConfig
import com.sleepsounds.storytelling.data.local.DeepSleepDatabase
import com.sleepsounds.storytelling.data.local.entity.SoundEntity
import com.sleepsounds.storytelling.data.media.MediaFileRepository
import com.sleepsounds.storytelling.data.remote.ContentCatalogRepository
import com.sleepsounds.storytelling.di.ApplicationScope
import com.sleepsounds.storytelling.util.R2Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

data class SyncProgress(
    val phase: String = "",
    val completed: Int = 0,
    val total: Int = 0,
)

/**
 * Coordinates Firebase Realtime Database catalog import and background Cloudflare R2 audio prefetching.
 */
@Singleton
class SyncEngine @Inject constructor(
    private val contentCatalogRepository: ContentCatalogRepository,
    private val mediaFileRepository: MediaFileRepository,
    private val database: DeepSleepDatabase,
    @ApplicationScope private val appScope: CoroutineScope,
) {
    private val _syncResult = MutableStateFlow<CatalogSyncResult>(CatalogSyncResult.Idle)
    val syncResult: StateFlow<CatalogSyncResult> = _syncResult.asStateFlow()

    private val _progress = MutableStateFlow(SyncProgress())
    val progress: StateFlow<SyncProgress> = _progress.asStateFlow()

    suspend fun performInitialSync() {
        _syncResult.value = CatalogSyncResult.Idle
        _progress.value = SyncProgress(phase = "catalog")

        val rtdbImported = contentCatalogRepository.awaitFirstImport()
        if (!rtdbImported) {
            val cachedRoomSounds = database.soundDao().getAll().isNotEmpty()
            if (cachedRoomSounds) {
                _syncResult.value = CatalogSyncResult.UpToDate
            } else {
                _syncResult.value = CatalogSyncResult.Failed("Realtime Database content unavailable")
                _progress.value = SyncProgress(phase = "done")
                return
            }
        } else {
            val requiredVersion = contentCatalogRepository.minAppVersion
            if (requiredVersion > BuildConfig.VERSION_CODE) {
                _syncResult.value = CatalogSyncResult.ForceUpdateRequired(requiredVersion)
                _progress.value = SyncProgress(phase = "done")
                return
            }
            _syncResult.value = CatalogSyncResult.Updated(1)
        }

        scheduleAllSoundDownloads()
        _progress.value = SyncProgress(phase = "done")
    }

    suspend fun syncIfNeeded(force: Boolean = false) {
        val current = _syncResult.value
        if (force || current is CatalogSyncResult.Idle || current is CatalogSyncResult.Failed) {
            performInitialSync()
        }
    }

    fun scheduleAllSoundDownloads() {
        appScope.launch {
            val allSounds = database.soundDao().getAll()
            if (allSounds.isEmpty()) return@launch
            val finishedCounter = AtomicInteger(0)
            _progress.value = SyncProgress(phase = "sounds", completed = 0, total = allSounds.size)
            val concurrencyGate = Semaphore(MAX_CONCURRENT_SOUND_DOWNLOADS)
            coroutineScope {
                allSounds.map { entity ->
                    async {
                        concurrencyGate.withPermit {
                            ensureSingleSoundCached(entity)
                            val done = finishedCounter.incrementAndGet()
                            _progress.value = SyncProgress(
                                phase = "sounds",
                                completed = done,
                                total = allSounds.size,
                            )
                        }
                    }
                }.forEach { it.await() }
            }
        }
    }

    private suspend fun ensureSingleSoundCached(sound: SoundEntity) {
        mediaFileRepository.ensureSoundDownloaded(sound.id, sound.audioPath, etag = null)
            .onFailure { R2Log.w("Failed to cache sound ${sound.id}: ${it.message}") }
    }

    companion object {
        private const val MAX_CONCURRENT_SOUND_DOWNLOADS = 3
    }
}
