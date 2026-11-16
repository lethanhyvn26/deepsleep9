package com.sleepsounds.storytelling.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity persisting user configuration for the smart wake-up alarm (`smart_alarm`).
 */
@Entity(tableName = "smart_alarm")
data class SmartAlarmEntity(
    @PrimaryKey
    val id: Int = 1,
    val hour: Int = 8,
    val minute: Int = 30,
    @ColumnInfo(name = "enabled", defaultValue = "1")
    val enabled: Int = 1,
    @ColumnInfo(name = "smart_window_minutes", defaultValue = "20")
    val smartWindowMinutes: Int = 20,
    @ColumnInfo(name = "days_of_week_mask", defaultValue = "127")
    val daysOfWeekMask: Int = 127,
    @ColumnInfo(name = "ringtone_sound_id", defaultValue = "'morning_relaxation'")
    val ringtoneSoundId: String = "morning_relaxation",
    @ColumnInfo(name = "vibrate", defaultValue = "1")
    val vibrate: Int = 1,
    @ColumnInfo(name = "snooze_minutes", defaultValue = "10")
    val snoozeMinutes: Int = 10,
    @ColumnInfo(name = "label", defaultValue = "'Wake-up Alarm'")
    val label: String = "Wake-up Alarm",
)
