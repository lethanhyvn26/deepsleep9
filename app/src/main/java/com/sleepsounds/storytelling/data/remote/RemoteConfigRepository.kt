package com.sleepsounds.storytelling.data.remote

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.sleepsounds.storytelling.BuildConfig
import com.sleepsounds.storytelling.util.R2Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Firebase Remote Config parameters controlling Cloudflare R2 and Worker CDN endpoints.
 */
@Singleton
class RemoteConfigRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    private val firebaseRemoteConfig: FirebaseRemoteConfig by lazy {
        if (FirebaseApp.getApps(appContext).isEmpty()) {
            FirebaseApp.initializeApp(appContext)
        }
        FirebaseRemoteConfig.getInstance()
    }

    private val _r2BaseUrl = MutableStateFlow(DEFAULT_R2_BASE_URL)
    private val _workerBaseUrl = MutableStateFlow(DEFAULT_WORKER_BASE_URL)
    private val _useWorkerRoutes = MutableStateFlow(false)

    val r2BaseUrl: StateFlow<String> = _r2BaseUrl.asStateFlow()
    val workerBaseUrl: StateFlow<String> = _workerBaseUrl.asStateFlow()
    val useWorkerRoutes: StateFlow<Boolean> = _useWorkerRoutes.asStateFlow()

    suspend fun applyLocalDefaults() = withContext(Dispatchers.IO) {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0L else 3_600L
        }
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        val defaultMap = mapOf<String, Any>(
            KEY_R2_BASE_URL to DEFAULT_R2_BASE_URL,
            KEY_WORKER_BASE_URL to DEFAULT_WORKER_BASE_URL,
            KEY_USE_WORKER_ROUTES to false,
        )
        firebaseRemoteConfig.setDefaultsAsync(defaultMap).await()
        syncFlowsFromRemoteConfig()
    }

    suspend fun fetchRemoteConfig(): Boolean = withContext(Dispatchers.IO) {
        val updated = runCatching {
            withTimeout(REMOTE_FETCH_TIMEOUT_MS) {
                firebaseRemoteConfig.fetchAndActivate().await()
            }
        }.onFailure { err ->
            R2Log.e("Firebase Remote Config fetch failed", err)
        }.getOrDefault(false)

        syncFlowsFromRemoteConfig()
        updated
    }

    fun getContentBaseUrl(): String {
        val candidate = if (useWorkerRoutes()) {
            val workerCandidate = _workerBaseUrl.value
            if (workerCandidate.isBlank() || workerCandidate.contains("YOUR_SUBDOMAIN")) {
                _r2BaseUrl.value
            } else {
                workerCandidate
            }
        } else {
            _r2BaseUrl.value
        }
        return ensureTrailingSlash(candidate.ifBlank { DEFAULT_R2_BASE_URL })
    }

    fun getR2BaseUrl(): String =
        ensureTrailingSlash(_r2BaseUrl.value.ifBlank { DEFAULT_R2_BASE_URL })

    fun getWorkerBaseUrl(): String =
        ensureTrailingSlash(_workerBaseUrl.value.ifBlank { DEFAULT_WORKER_BASE_URL })

    fun useWorkerRoutes(): Boolean = _useWorkerRoutes.value

    private fun syncFlowsFromRemoteConfig() {
        _r2BaseUrl.value = firebaseRemoteConfig.getString(KEY_R2_BASE_URL)
            .ifBlank { DEFAULT_R2_BASE_URL }
        _workerBaseUrl.value = firebaseRemoteConfig.getString(KEY_WORKER_BASE_URL)
            .ifBlank { DEFAULT_WORKER_BASE_URL }
        _useWorkerRoutes.value = firebaseRemoteConfig.getBoolean(KEY_USE_WORKER_ROUTES)
    }

    private fun ensureTrailingSlash(rawUrl: String): String =
        "${rawUrl.trimEnd('/')}/"

    companion object {
        const val KEY_R2_BASE_URL = "r2_base_url"
        const val KEY_WORKER_BASE_URL = "worker_base_url"
        const val KEY_USE_WORKER_ROUTES = "use_worker_routes"

        const val DEFAULT_R2_BASE_URL =
            "https://pub-902b89e71204483092cf27a11d18d989.r2.dev/"
        const val DEFAULT_WORKER_BASE_URL =
            "https://deepsleep-media.YOUR_SUBDOMAIN.workers.dev/"

        private const val REMOTE_FETCH_TIMEOUT_MS = 8_000L
    }
}
