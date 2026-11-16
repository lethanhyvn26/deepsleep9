package com.sleepsounds.storytelling.di

import com.sleepsounds.storytelling.data.local.DeepSleepDatabase
import com.sleepsounds.storytelling.data.remote.ContentCatalogRepository
import com.sleepsounds.storytelling.data.remote.RemoteConfigRepository
import com.sleepsounds.storytelling.data.remote.ShopCatalogRepository
import com.sleepsounds.storytelling.data.repository.UserPreferencesRepository
import com.sleepsounds.storytelling.data.sync.CatalogSyncResult
import com.sleepsounds.storytelling.data.sync.SyncEngine
import com.sleepsounds.storytelling.util.R2Log
import com.sleepsounds.storytelling.util.StartupTrace
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinates cold-start initialization: Firebase RTDB listeners, Remote Config, and Room catalog seeding.
 */
@Singleton
class StartupCoordinator @Inject constructor(
    private val remoteConfigRepository: RemoteConfigRepository,
    private val shopCatalogRepository: ShopCatalogRepository,
    private val contentCatalogRepository: ContentCatalogRepository,
    private val syncEngine: SyncEngine,
    private val database: DeepSleepDatabase,
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationScope private val appScope: CoroutineScope,
) {
    private val syncGate = CompletableDeferred<Unit>()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _forceUpdateRequired = MutableStateFlow(false)
    val forceUpdateRequired: StateFlow<Boolean> = _forceUpdateRequired.asStateFlow()

    @Volatile
    private var initialized = false

    fun start() {
        if (initialized) return
        initialized = true

        shopCatalogRepository.start()
        contentCatalogRepository.start()

        appScope.launch {
            runCatching {
                StartupTrace.span("StartupCoordinator.prepare") {
                    withContext(Dispatchers.IO) {
                        remoteConfigRepository.applyLocalDefaults()
                        runCatching { remoteConfigRepository.fetchRemoteConfig() }
                            .onFailure { R2Log.w("RemoteConfig refresh skipped, using local defaults") }

                        userPreferencesRepository.hasCompletedOnboarding.first()
                        syncEngine.performInitialSync()

                        when (syncEngine.syncResult.value) {
                            is CatalogSyncResult.ForceUpdateRequired -> {
                                _forceUpdateRequired.value = true
                            }
                            is CatalogSyncResult.Failed -> {
                                R2Log.w("Initial catalog sync reported failure")
                            }
                            else -> verifyRoomWarmup()
                        }
                    }
                }
            }.onFailure { err ->
                R2Log.e("StartupCoordinator preparation error", err)
            }
            _isReady.value = true
            syncGate.complete(Unit)
        }
    }

    private fun verifyRoomWarmup() {
        database.openHelper.writableDatabase
            .query("SELECT COUNT(*) FROM media_category")
            .use { cursor ->
                if (cursor.moveToFirst()) {
                    R2Log.i("Catalog database ready (${cursor.getInt(0)} media categories)")
                }
            }
    }

    suspend fun awaitInitialSyncComplete() {
        syncGate.await()
    }
}
