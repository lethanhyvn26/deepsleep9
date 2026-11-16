package com.sleepsounds.storytelling.data.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sleepsounds.storytelling.data.local.DeepSleepDatabase
import com.sleepsounds.storytelling.data.sync.CatalogJsonImporter
import com.sleepsounds.storytelling.di.ApplicationScope
import com.sleepsounds.storytelling.util.R2Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Observes the `/content` catalog tree in Firebase Realtime Database and synchronizes changes into Room.
 */
@Singleton
class ContentCatalogRepository @Inject constructor(
    database: FirebaseDatabase,
    private val roomDatabase: DeepSleepDatabase,
    private val catalogJsonImporter: CatalogJsonImporter,
    @ApplicationScope private val appScope: CoroutineScope,
) {
    private val contentNodeRef = database.getReference(PATH_CONTENT)

    private val _baseUrl = MutableStateFlow(RemoteConfigRepository.DEFAULT_R2_BASE_URL)
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    private val _catalog = MutableStateFlow(ContentCatalog())
    val catalog: StateFlow<ContentCatalog> = _catalog.asStateFlow()

    @Volatile
    var minAppVersion: Int = 0
        private set

    @Volatile
    private var attached = false

    private val initialImportSignal = CompletableDeferred<Boolean>()
    private val roomWriteMutex = Mutex()
    private var debounceImportJob: Job? = null

    @Volatile
    private var lastPersistedCatalog: ContentCatalog? = null

    private val contentValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (!snapshot.exists()) {
                R2Log.w("Realtime Database node /$PATH_CONTENT is empty")
                signalFirstImport(false)
                return
            }
            val parsedCatalog = ContentCatalogMapper.fromSnapshot(snapshot)
            if (parsedCatalog.baseUrl.isNotBlank()) {
                _baseUrl.value = parsedCatalog.baseUrl
            }
            minAppVersion = parsedCatalog.minAppVersion
            _catalog.value = parsedCatalog

            val waitMs = if (initialImportSignal.isCompleted) UPDATE_DEBOUNCE_MS else 0L
            debounceImportJob?.cancel()
            debounceImportJob = appScope.launch {
                if (waitMs > 0L) delay(waitMs)
                writeCatalogToRoom(parsedCatalog)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            R2Log.e("Realtime Database /content listener cancelled", error.toException())
            signalFirstImport(lastPersistedCatalog != null)
        }
    }

    fun start() {
        if (attached) return
        attached = true
        appScope.launch {
            runCatching {
                val persistedBase = roomDatabase.syncStateDao().get()?.soundsHash?.trim().orEmpty()
                if (persistedBase.startsWith("http://") || persistedBase.startsWith("https://")) {
                    _baseUrl.value = if (persistedBase.endsWith("/")) persistedBase else "$persistedBase/"
                }
            }
        }
        runCatching { contentNodeRef.keepSynced(true) }
        contentNodeRef.addValueEventListener(contentValueListener)
    }

    suspend fun awaitFirstImport(timeoutMs: Long = FIRST_IMPORT_TIMEOUT_MS): Boolean {
        start()
        return withTimeoutOrNull(timeoutMs) { initialImportSignal.await() } ?: false
    }

    private suspend fun writeCatalogToRoom(catalog: ContentCatalog) {
        roomWriteMutex.withLock {
            if (catalog == lastPersistedCatalog) {
                signalFirstImport(true)
                return
            }
            runCatching {
                catalogJsonImporter.importFromRtdb(catalog)
                lastPersistedCatalog = catalog
                signalFirstImport(true)
            }.onFailure { err ->
                R2Log.e("Failed importing RTDB catalog into Room: ${err.message}", err)
            }
        }
    }

    private fun signalFirstImport(succeeded: Boolean) {
        if (!initialImportSignal.isCompleted) {
            initialImportSignal.complete(succeeded)
        }
    }

    companion object {
        const val PATH_CONTENT: String = "content"
        const val FIRST_IMPORT_TIMEOUT_MS: Long = 20_000L
        private const val UPDATE_DEBOUNCE_MS: Long = 300L
    }
}
