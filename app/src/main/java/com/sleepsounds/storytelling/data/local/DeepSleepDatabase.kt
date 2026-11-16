package com.sleepsounds.storytelling.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
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
import com.sleepsounds.storytelling.data.local.entity.MediaCategoryEntity
import com.sleepsounds.storytelling.data.local.entity.PlaybackHistoryEntity
import com.sleepsounds.storytelling.data.local.entity.PodcastCategoryEntity
import com.sleepsounds.storytelling.data.local.entity.PodcastChapterEntity
import com.sleepsounds.storytelling.data.local.entity.PodcastEntity
import com.sleepsounds.storytelling.data.local.entity.SleepPresetEntity
import com.sleepsounds.storytelling.data.local.entity.SleepPresetItemEntity
import com.sleepsounds.storytelling.data.local.entity.SleepSessionEntity
import com.sleepsounds.storytelling.data.local.entity.SmartAlarmEntity
import com.sleepsounds.storytelling.data.local.entity.SoundEntity
import com.sleepsounds.storytelling.data.local.entity.SyncStateEntity
import com.sleepsounds.storytelling.data.local.entity.UserMixEntity
import com.sleepsounds.storytelling.data.local.entity.UserMixItemEntity
import com.sleepsounds.storytelling.data.local.entity.UserPresetCustomizationEntity
import com.sleepsounds.storytelling.data.local.entity.UserPresetItemEntity

/**
 * Primary Room SQLite database storing offline catalog items, user mixes,
 * playback history, sleep tracking sessions, and smart alarm configuration.
 * Retains schema version 6 and database filename `somni.db` for seamless Play Store upgrade compatibility.
 */
@Database(
    entities = [
        MediaCategoryEntity::class,
        PodcastCategoryEntity::class,
        SoundEntity::class,
        SleepPresetEntity::class,
        SleepPresetItemEntity::class,
        PodcastEntity::class,
        PodcastChapterEntity::class,
        UserMixEntity::class,
        UserMixItemEntity::class,
        UserPresetCustomizationEntity::class,
        UserPresetItemEntity::class,
        SyncStateEntity::class,
        PlaybackHistoryEntity::class,
        SleepSessionEntity::class,
        SmartAlarmEntity::class,
    ],
    version = DeepSleepDatabase.SCHEMA_VERSION,
    exportSchema = false,
)
abstract class DeepSleepDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun soundDao(): SoundDao
    abstract fun sleepPresetDao(): SleepPresetDao
    abstract fun podcastDao(): PodcastDao
    abstract fun userMixDao(): UserMixDao
    abstract fun userPresetDao(): UserPresetDao
    abstract fun syncStateDao(): SyncStateDao
    abstract fun playbackHistoryDao(): PlaybackHistoryDao
    abstract fun sleepSessionDao(): SleepSessionDao
    abstract fun smartAlarmDao(): SmartAlarmDao

    companion object {
        const val SCHEMA_VERSION: Int = 6
        const val DB_NAME: String = "somni.db"
    }
}
