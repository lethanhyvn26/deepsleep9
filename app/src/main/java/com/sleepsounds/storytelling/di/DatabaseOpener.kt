package com.sleepsounds.storytelling.di

import android.app.ActivityManager
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sleepsounds.storytelling.data.local.DeepSleepDatabase
import com.sleepsounds.storytelling.util.R2Log

/**
 * Builds and migrates the Room SQLite instance (`somni.db` v6) with RAM-appropriate journaling.
 */
internal object DatabaseOpener {

    fun openFast(context: Context): DeepSleepDatabase =
        createRoomInstance(context.applicationContext)

    private fun createRoomInstance(appContext: Context): DeepSleepDatabase {
        val activityMgr = appContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val selectedJournalMode = if (activityMgr?.isLowRamDevice == true) {
            RoomDatabase.JournalMode.TRUNCATE
        } else {
            RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING
        }

        return Room.databaseBuilder(
            appContext,
            DeepSleepDatabase::class.java,
            DeepSleepDatabase.DB_NAME,
        )
            .setJournalMode(selectedJournalMode)
            .addMigrations(MIGRATION_V4_TO_V5, MIGRATION_V5_TO_V6)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .addCallback(
                object : RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        val byteSize = appContext.getDatabasePath(DeepSleepDatabase.DB_NAME).length()
                        R2Log.i("DeepSleepDatabase opened ($byteSize bytes)")
                    }
                },
            )
            .build()
    }

    private val MIGRATION_V4_TO_V5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            val catalogTables = listOf(
                "media_category",
                "podcast_category",
                "sound",
                "sleep_preset",
                "podcast",
                "sleep_preset_item",
            )
            for (tableName in catalogTables) {
                db.execSQL("ALTER TABLE $tableName ADD COLUMN sort_index INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

    private val MIGRATION_V5_TO_V6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `playback_history` (" +
                    "`item_key` TEXT NOT NULL, " +
                    "`item_id` TEXT NOT NULL, " +
                    "`item_type` TEXT NOT NULL, " +
                    "`title` TEXT NOT NULL, " +
                    "`subtitle` TEXT NOT NULL, " +
                    "`image_path` TEXT, " +
                    "`duration_seconds` INTEGER NOT NULL DEFAULT 0, " +
                    "`is_premium` INTEGER NOT NULL DEFAULT 0, " +
                    "`mix_layers_json` TEXT, " +
                    "`played_at` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`item_key`))"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_playback_history_played_at` ON `playback_history` (`played_at`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_playback_history_item_type` ON `playback_history` (`item_type`)"
            )

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `sleep_session` (" +
                    "`id` TEXT NOT NULL, " +
                    "`started_at` INTEGER NOT NULL, " +
                    "`ended_at` INTEGER NOT NULL, " +
                    "`duration_minutes` INTEGER NOT NULL, " +
                    "`sleep_score` INTEGER NOT NULL, " +
                    "`awake_minutes` INTEGER NOT NULL, " +
                    "`light_sleep_minutes` INTEGER NOT NULL, " +
                    "`deep_sleep_minutes` INTEGER NOT NULL, " +
                    "`fell_asleep_minutes` INTEGER NOT NULL DEFAULT 12, " +
                    "`snore_minutes` INTEGER NOT NULL DEFAULT 0, " +
                    "`snore_count` INTEGER NOT NULL DEFAULT 0, " +
                    "`sleep_notes_csv` TEXT NOT NULL DEFAULT '', " +
                    "`wake_up_mood` TEXT NOT NULL DEFAULT 'GOOD', " +
                    "`stages_json` TEXT NOT NULL DEFAULT '[]', " +
                    "PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_sleep_session_started_at` ON `sleep_session` (`started_at`)"
            )

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `smart_alarm` (" +
                    "`id` INTEGER NOT NULL, " +
                    "`hour` INTEGER NOT NULL, " +
                    "`minute` INTEGER NOT NULL, " +
                    "`enabled` INTEGER NOT NULL DEFAULT 1, " +
                    "`smart_window_minutes` INTEGER NOT NULL DEFAULT 20, " +
                    "`days_of_week_mask` INTEGER NOT NULL DEFAULT 127, " +
                    "`ringtone_sound_id` TEXT NOT NULL DEFAULT 'morning_relaxation', " +
                    "`vibrate` INTEGER NOT NULL DEFAULT 1, " +
                    "`snooze_minutes` INTEGER NOT NULL DEFAULT 10, " +
                    "`label` TEXT NOT NULL DEFAULT 'Wake-up Alarm', " +
                    "PRIMARY KEY(`id`))"
            )
        }
    }
}
