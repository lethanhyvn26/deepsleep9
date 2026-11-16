package com.sleepsounds.storytelling.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity storing recently played sounds, scenes, custom mixes, and podcards (`playback_history`).
 */
@Entity(
    tableName = "playback_history",
    indices = [
        Index(value = ["played_at"]),
        Index(value = ["item_type"]),
    ],
)
data class PlaybackHistoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "item_key")
    val itemKey: String,
    @ColumnInfo(name = "item_id")
    val itemId: String,
    @ColumnInfo(name = "item_type")
    val itemType: String,
    val title: String,
    val subtitle: String = "",
    @ColumnInfo(name = "image_path")
    val imagePath: String? = null,
    @ColumnInfo(name = "duration_seconds", defaultValue = "0")
    val durationSeconds: Int = 0,
    @ColumnInfo(name = "is_premium", defaultValue = "0")
    val isPremium: Int = 0,
    @ColumnInfo(name = "mix_layers_json")
    val mixLayersJson: String? = null,
    @ColumnInfo(name = "played_at")
    val playedAt: Long,
)
