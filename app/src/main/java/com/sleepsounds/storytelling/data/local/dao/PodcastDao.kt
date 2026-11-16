package com.sleepsounds.storytelling.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.sleepsounds.storytelling.data.local.entity.PodcastChapterEntity
import com.sleepsounds.storytelling.data.local.entity.PodcastEntity

/**
 * Data access object for bedtime stories, podcasts, and their ordered chapters.
 */
@Dao
interface PodcastDao {

    @Query("SELECT * FROM podcast ORDER BY sort_index ASC, id ASC")
    suspend fun getAll(): List<PodcastEntity>

    @Query("SELECT * FROM podcast WHERE language = :languageCode ORDER BY sort_index ASC, id ASC")
    suspend fun getByLanguage(languageCode: String): List<PodcastEntity>

    @Query("SELECT * FROM podcast WHERE categories LIKE '%' || :categoryKey || '%' ORDER BY sort_index ASC, id ASC")
    suspend fun getByCategory(categoryKey: String): List<PodcastEntity>

    @Query("SELECT * FROM podcast WHERE is_favorite = 1 ORDER BY sort_index ASC, id ASC")
    suspend fun getFavorites(): List<PodcastEntity>

    @Query("SELECT * FROM podcast WHERE id = :podcastId LIMIT 1")
    suspend fun getById(podcastId: String): PodcastEntity?

    @Query("SELECT * FROM podcast_chapter WHERE podcast_id = :seriesId ORDER BY chapter_index ASC")
    suspend fun getChaptersForPodcast(seriesId: String): List<PodcastChapterEntity>

    @Query("SELECT * FROM podcast_chapter ORDER BY podcast_id ASC, chapter_index ASC")
    suspend fun getAllChapters(): List<PodcastChapterEntity>

    @Update
    suspend fun update(entity: PodcastEntity)
}
