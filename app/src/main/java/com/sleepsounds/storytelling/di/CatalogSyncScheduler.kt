package com.sleepsounds.storytelling.di

import com.sleepsounds.storytelling.data.remote.RemoteConfigRepository
import com.sleepsounds.storytelling.data.sync.ContentPrefetchService
import com.sleepsounds.storytelling.data.sync.SyncEngine
import com.sleepsounds.storytelling.util.R2Log
import com.sleepsounds.storytelling.util.StartupTrace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinates serialized catalog synchronization and media thumbnail prefetching
 * after initial startup and on foreground resume.
 */
@Singleton
class CatalogSyncScheduler @Inject constructor(
    private val bootstrapCoordinator: StartupCoordinator,
    private val remoteConfigGateway: RemoteConfigRepository,
    private val catalogSyncEngine: SyncEngine,
    private val mediaPrefetcher: ContentPrefetchService,
    @ApplicationScope private val appScope: CoroutineScope,
) {
    private val backgroundSyncArmed = AtomicBoolean(false)
    private val syncGate = Mutex()

    fun scheduleBackgroundSync() {
        if (!backgroundSyncArmed.compareAndSet(false, true)) return
        appScope.launch {
            bootstrapCoordinator.awaitInitialSyncComplete()
            executeCatalogSync(refreshRemoteConfig = false)
        }
    }

    fun syncOnResume() {
        appScope.launch {
            executeCatalogSync(refreshRemoteConfig = true)
        }
    }

    private suspend fun executeCatalogSync(refreshRemoteConfig: Boolean) {
        syncGate.withLock {
            StartupTrace.span("CatalogSyncScheduler.execute") {
                if (refreshRemoteConfig) {
                    runCatching { remoteConfigGateway.fetchRemoteConfig() }
                        .onFailure { err -> R2Log.e("Remote Config refresh failed on resume", err) }
                }
                runCatching {
                    catalogSyncEngine.syncIfNeeded(force = refreshRemoteConfig)
                }.onFailure { err ->
                    R2Log.e("Catalog background sync failed", err)
                }
                mediaPrefetcher.prefetchListMetadata()
            }
        }
    }
}
