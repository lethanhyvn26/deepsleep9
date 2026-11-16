package com.sleepsounds.storytelling.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity linking a [SleepPresetEntity] to its constituent [SoundEntity] layers (`sleep_preset_item`).
 */
@Entity(
    tableName = "sleep_preset_item",
    primaryKeys = ["preset_id", "sound_id"],
    foreignKeys = [
        ForeignKey(
            entity = SleepPresetEntity::class,
            parentColumns = ["id"],
            childColumns = ["preset_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SoundEntity::class,
            parentColumns = ["id"],
            childColumns = ["sound_id"],
            onDelete = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [
        Index(value = ["preset_id"]),
        Index(value = ["sound_id"]),
    ],
)
data class SleepPresetItemEntity(
    @ColumnInfo(name = "preset_id")
    val presetId: String,
    @ColumnInfo(name = "sound_id")
    val soundId: String,
    val volume: Float,
    @ColumnInfo(name = "sort_index", defaultValue = "0")
    val sortIndex: Int = 0,
)
/**
 * Room entity for a curated multi-layer sleep scene (`sleep_preset`).
 */
@Entity(tableName = "sleep_preset")
data class SleepPresetEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val categories: String,
    @ColumnInfo(name = "is_favorite", defaultValue = "0")
    val isFavorite: Int,
    @ColumnInfo(name = "is_premium", defaultValue = "0")
    val isPremium: Int,
    @ColumnInfo(name = "image_path", defaultValue = "''")
    val imagePath: String = "",
    @ColumnInfo(name = "sort_index", defaultValue = "0")
    val sortIndex: Int = 0,
)
