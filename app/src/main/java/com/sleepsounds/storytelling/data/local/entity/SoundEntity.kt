package com.sleepsounds.storytelling.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an individual ambient loop sound (`sound`).
 */
@Entity(tableName = "sound")
data class SoundEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val volume: Float,
    @ColumnInfo(name = "categories", defaultValue = "'[]'")
    val categories: String,
    @ColumnInfo(name = "is_favorite", defaultValue = "0")
    val isFavorite: Int,
    @ColumnInfo(name = "symbol_path")
    val symbolPath: String,
    @ColumnInfo(name = "audio_path")
    val audioPath: String,
    @ColumnInfo(name = "unlock_day", defaultValue = "0")
    val unlockDay: Int = 0,
    @ColumnInfo(name = "is_premium", defaultValue = "0")
    val isPremium: Int,
    @ColumnInfo(name = "sort_index", defaultValue = "0")
    val sortIndex: Int = 0,
)
