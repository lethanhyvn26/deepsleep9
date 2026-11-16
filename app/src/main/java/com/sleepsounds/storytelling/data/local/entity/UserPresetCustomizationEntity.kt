package com.sleepsounds.storytelling.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Flags whether a built-in sleep scene (`preset_id`) has custom user layer overrides.
 */
@Entity(
    tableName = "user_preset_customization",
    foreignKeys = [
        ForeignKey(
            entity = SleepPresetEntity::class,
            parentColumns = ["id"],
            childColumns = ["preset_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class UserPresetCustomizationEntity(
    @PrimaryKey
    @ColumnInfo(name = "preset_id")
    val presetId: String,
    @ColumnInfo(name = "is_modified", defaultValue = "1")
    val isModified: Int = 1,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "catalog_version", defaultValue = "0")
    val catalogVersion: Int = 0,
)
