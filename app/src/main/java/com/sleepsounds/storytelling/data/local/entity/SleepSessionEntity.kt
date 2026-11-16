package com.sleepsounds.storytelling.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity storing a recorded overnight sleep tracking session (`sleep_session`).
 */
@Entity(
    tableName = "sleep_session",
    indices = [
        Index(value = ["started_at"]),
    ],
)
data class SleepSessionEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "started_at")
    val startedAt: Long,
    @ColumnInfo(name = "ended_at")
    val endedAt: Long,
    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,
    @ColumnInfo(name = "sleep_score")
    val sleepScore: Int,
    @ColumnInfo(name = "awake_minutes")
    val awakeMinutes: Int,
    @ColumnInfo(name = "light_sleep_minutes")
    val lightSleepMinutes: Int,
    @ColumnInfo(name = "deep_sleep_minutes")
    val deepSleepMinutes: Int,
    @ColumnInfo(name = "fell_asleep_minutes", defaultValue = "12")
    val fellAsleepMinutes: Int = 12,
    @ColumnInfo(name = "snore_minutes", defaultValue = "0")
    val snoreMinutes: Int = 0,
    @ColumnInfo(name = "snore_count", defaultValue = "0")
    val snoreCount: Int = 0,
    @ColumnInfo(name = "sleep_notes_csv", defaultValue = "''")
    val sleepNotesCsv: String = "",
    @ColumnInfo(name = "wake_up_mood", defaultValue = "'GOOD'")
    val wakeUpMood: String = "GOOD",
    @ColumnInfo(name = "stages_json", defaultValue = "'[]'")
    val stagesJson: String = "[]",
)
