package com.sleepsounds.storytelling.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * Stores a customized sound layer volume for a built-in sleep scene (`user_preset_item`).
 */
@Entity(
    tableName = "user_preset_item",
    primaryKeys = ["preset_id", "sound_id"],
    foreignKeys = [
        ForeignKey(
            entity = UserPresetCustomizationEntity::class,
            parentColumns = ["preset_id"],
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
)
data class UserPresetItemEntity(
    @ColumnInfo(name = "preset_id")
    val presetId: String,
    @ColumnInfo(name = "sound_id")
    val soundId: String,
    @ColumnInfo(name = "volume")
    val volume: Float,
)
