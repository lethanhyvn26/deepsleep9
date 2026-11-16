package com.sleepsounds.storytelling.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.sleepsounds.storytelling.data.local.entity.MediaCategoryEntity
import com.sleepsounds.storytelling.data.local.entity.PodcastCategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for media and podcast category tables ordered by `sort_index`.
 */
@Dao
interface CategoryDao {

    @Query("SELECT * FROM media_category ORDER BY sort_index ASC, id ASC")
    suspend fun getMediaCategories(): List<MediaCategoryEntity>

    @Query("SELECT * FROM podcast_category ORDER BY sort_index ASC, id ASC")
    suspend fun getPodcastCategories(): List<PodcastCategoryEntity>

    @Query("SELECT * FROM media_category ORDER BY sort_index ASC, id ASC")
    fun observeMediaCategories(): Flow<List<MediaCategoryEntity>>

    @Query("SELECT * FROM podcast_category ORDER BY sort_index ASC, id ASC")
    fun observePodcastCategories(): Flow<List<PodcastCategoryEntity>>
}
