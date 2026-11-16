package com.sleepsounds.storytelling.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a bedtime story or sleep podcard (`podcast`).
 */
@Entity(tableName = "podcast")
data class PodcastEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val author: String? = null,
    val narrator: String? = null,
    val summary: String? = null,
    val categories: String,
    @ColumnInfo(name = "image_path")
    val imagePath: String,
    @ColumnInfo(name = "is_premium", defaultValue = "0")
    val isPremium: Int = 0,
    @ColumnInfo(name = "free_chapters", defaultValue = "0")
    val freeChapters: Int = 0,
    @ColumnInfo(name = "background_sound_id")
    val backgroundSoundId: String? = null,
    @ColumnInfo(name = "is_favorite", defaultValue = "0")
    val isFavorite: Int = 0,
    @ColumnInfo(name = "language", defaultValue = "'en'")
    val language: String = "en",
    @ColumnInfo(name = "duration_seconds", defaultValue = "0")
    val durationSeconds: Int = 0,
    @ColumnInfo(name = "sort_index", defaultValue = "0")
    val sortIndex: Int = 0,
)

/**
 * Room entity representing an audio chapter belonging to a [PodcastEntity] (`podcast_chapter`).
 */
@Entity(
    tableName = "podcast_chapter",
    foreignKeys = [
        ForeignKey(
            entity = PodcastEntity::class,
            parentColumns = ["id"],
            childColumns = ["podcast_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(
            value = ["podcast_id", "chapter_index"],
            name = "idx_podcast_chapter_podcast",
        ),
    ],
)
data class PodcastChapterEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "podcast_id")
    val podcastId: String,
    val title: String,
    @ColumnInfo(name = "chapter_index")
    val chapterIndex: Int,
    @ColumnInfo(name = "duration_seconds", defaultValue = "0")
    val durationSeconds: Int = 0,
    @ColumnInfo(name = "audio_path")
    val audioPath: String,
)
