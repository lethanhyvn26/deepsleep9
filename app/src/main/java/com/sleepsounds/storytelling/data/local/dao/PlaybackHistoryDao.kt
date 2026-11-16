package com.sleepsounds.storytelling.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sleepsounds.storytelling.data.local.entity.PlaybackHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for the user's recent playback history (`playback_history`).
 */
@Dao
interface PlaybackHistoryDao {

    @Query("SELECT * FROM playback_history ORDER BY played_at DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT * FROM playback_history ORDER BY played_at DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 50): List<PlaybackHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: PlaybackHistoryEntity)

    @Query("DELETE FROM playback_history WHERE item_key = :itemKey")
    suspend fun deleteByKey(itemKey: String)

    @Query("DELETE FROM playback_history")
    suspend fun clearAll()

    @Query("DELETE FROM playback_history WHERE item_key NOT IN (SELECT item_key FROM playback_history ORDER BY played_at DESC LIMIT :keepCount)")
    suspend fun trimToLimit(keepCount: Int = 100)
}
