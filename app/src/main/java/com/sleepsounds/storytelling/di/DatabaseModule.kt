package com.sleepsounds.storytelling.di

import android.content.Context
import com.sleepsounds.storytelling.data.local.DeepSleepDatabase
import com.sleepsounds.storytelling.data.local.dao.CategoryDao
import com.sleepsounds.storytelling.data.local.dao.PlaybackHistoryDao
import com.sleepsounds.storytelling.data.local.dao.PodcastDao
import com.sleepsounds.storytelling.data.local.dao.SleepPresetDao
import com.sleepsounds.storytelling.data.local.dao.SleepSessionDao
import com.sleepsounds.storytelling.data.local.dao.SmartAlarmDao
import com.sleepsounds.storytelling.data.local.dao.SoundDao
import com.sleepsounds.storytelling.data.local.dao.SyncStateDao
import com.sleepsounds.storytelling.data.local.dao.UserMixDao
import com.sleepsounds.storytelling.data.local.dao.UserPresetDao
import com.sleepsounds.storytelling.util.StartupTrace
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing the singleton [DeepSleepDatabase] instance and all Room DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDeepSleepDatabase(
        @ApplicationContext appContext: Context,
    ): DeepSleepDatabase = StartupTrace.span("DeepSleepDatabase.open") {
        DatabaseOpener.openFast(appContext)
    }

    @Provides
    fun bindSoundDao(db: DeepSleepDatabase): SoundDao = db.soundDao()

    @Provides
    fun bindSleepPresetDao(db: DeepSleepDatabase): SleepPresetDao = db.sleepPresetDao()

    @Provides
    fun bindPodcastDao(db: DeepSleepDatabase): PodcastDao = db.podcastDao()

    @Provides
    fun bindCategoryDao(db: DeepSleepDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun bindUserMixDao(db: DeepSleepDatabase): UserMixDao = db.userMixDao()

    @Provides
    fun bindUserPresetDao(db: DeepSleepDatabase): UserPresetDao = db.userPresetDao()

    @Provides
    fun bindSyncStateDao(db: DeepSleepDatabase): SyncStateDao = db.syncStateDao()

    @Provides
    fun bindPlaybackHistoryDao(db: DeepSleepDatabase): PlaybackHistoryDao = db.playbackHistoryDao()

    @Provides
    fun bindSleepSessionDao(db: DeepSleepDatabase): SleepSessionDao = db.sleepSessionDao()

    @Provides
    fun bindSmartAlarmDao(db: DeepSleepDatabase): SmartAlarmDao = db.smartAlarmDao()
}
