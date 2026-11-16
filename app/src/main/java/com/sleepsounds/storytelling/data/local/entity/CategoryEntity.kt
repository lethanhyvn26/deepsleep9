package com.sleepsounds.storytelling.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a soundscape or ambient sound category (`media_category`).
 */
@Entity(tableName = "media_category")
data class MediaCategoryEntity(
    @PrimaryKey
    val id: String,
    val label: String,
    val emoji: String? = null,
    @ColumnInfo(name = "sort_index", defaultValue = "0")
    val sortIndex: Int = 0,
)

/**
 * Room entity representing a bedtime story / podcard category (`podcast_category`).
 */
@Entity(tableName = "podcast_category")
data class PodcastCategoryEntity(
    @PrimaryKey
    val id: String,
    val label: String,
    val emoji: String? = null,
    @ColumnInfo(name = "sort_index", defaultValue = "0")
    val sortIndex: Int = 0,
)
