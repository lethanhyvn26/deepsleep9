package com.sleepsounds.storytelling.data.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sleepsounds.storytelling.di.ApplicationScope
import com.sleepsounds.storytelling.util.R2Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Subscribes to the live `/store` node in Firebase Realtime Database and resolves product image URLs.
 */
@Singleton
class ShopCatalogRepository @Inject constructor(
    database: FirebaseDatabase,
    private val remoteConfigRepository: RemoteConfigRepository,
    @ApplicationScope appScope: CoroutineScope,
) {
    private val storeNodeRef = database.getReference(PATH_STORE)
    private val rawStoreState = MutableStateFlow(ShopCatalog())
    private val shuffleSeed = MutableStateFlow(System.nanoTime())

    val shopCatalog: StateFlow<ShopCatalog> = combine(
        rawStoreState,
        remoteConfigRepository.r2BaseUrl,
        remoteConfigRepository.workerBaseUrl,
        remoteConfigRepository.useWorkerRoutes,
        shuffleSeed,
    ) { catalog, _, _, workerEnabled, seedValue ->
        val withResolvedImages = ShopImageUrls.resolveCatalog(
            catalog = catalog,
            rcBase = remoteConfigRepository.getContentBaseUrl(),
            useWorkerRoutes = workerEnabled,
        )
        withResolvedImages.randomizedForSession(Random(seedValue))
    }.stateIn(appScope, SharingStarted.Eagerly, ShopCatalog())

    @Volatile
    private var listening = false

    private val storeValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            rawStoreState.value = ShopCatalogMapper.fromSnapshot(snapshot)
        }

        override fun onCancelled(error: DatabaseError) {
            R2Log.e("Firebase /store listener cancelled", error.toException())
        }
    }

    fun reshuffleForSession() {
        shuffleSeed.value = System.nanoTime()
    }

    fun start() {
        if (listening) return
        listening = true
        runCatching { storeNodeRef.keepSynced(true) }
        storeNodeRef.addValueEventListener(storeValueListener)
    }

    companion object {
        const val PATH_STORE: String = "store"
        const val PATH_SHOP: String = "shop"
        const val DATABASE_URL: String = "https://musicsleep-d3b67-default-rtdb.firebaseio.com/"
    }
}
