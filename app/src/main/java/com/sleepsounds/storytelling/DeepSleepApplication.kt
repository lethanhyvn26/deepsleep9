package com.sleepsounds.storytelling

import android.app.Application
import android.content.ComponentCallbacks2
import coil3.SingletonImageLoader
import com.sleepsounds.storytelling.ads.AdMobManager
import com.sleepsounds.storytelling.data.billing.BillingRepository
import com.sleepsounds.storytelling.di.CatalogSyncScheduler
import com.sleepsounds.storytelling.di.CoilImageLoaderFactory
import com.sleepsounds.storytelling.di.StartupCoordinator
import com.sleepsounds.storytelling.util.R2Log
import com.sleepsounds.storytelling.util.StartupTrace
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point for Deep Sleep v3.1 with Google AdMob and Play Billing v9.
 */
@HiltAndroidApp
class DeepSleepApplication : Application(), SingletonImageLoader.Factory {

    @Inject
    lateinit var bootstrapCoordinator: StartupCoordinator

    @Inject
    lateinit var backgroundSyncScheduler: CatalogSyncScheduler

    @Inject
    lateinit var adMobManager: AdMobManager

    @Inject
    lateinit var billingRepository: BillingRepository

    override fun newImageLoader(context: coil3.PlatformContext): coil3.ImageLoader =
        CoilImageLoaderFactory.newImageLoader(context)

    override fun onCreate() {
        StartupTrace.mark("DeepSleepApplication.onCreate:enter")
        super.onCreate()
        bootstrapCoordinator.start()
        backgroundSyncScheduler.scheduleBackgroundSync()
        adMobManager.initialize()
        billingRepository.startConnection()
        StartupTrace.mark("DeepSleepApplication.onCreate:exit")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN ||
            level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND
        ) {
            runCatching {
                SingletonImageLoader.get(this).memoryCache?.clear()
            }
            R2Log.d("Cleared memory image cache at trim level=$level")
        }
    }
}
