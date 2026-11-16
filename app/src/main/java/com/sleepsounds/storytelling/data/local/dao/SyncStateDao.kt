package com.sleepsounds.storytelling.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sleepsounds.storytelling.data.local.entity.SyncStateEntity

/**
 * Data access object tracking the local catalog manifest version and sync timestamp.
 */
@Dao
interface SyncStateDao {

    @Query("SELECT * FROM sync_state WHERE id = 1 LIMIT 1")
    suspend fun get(): SyncStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(syncRecord: SyncStateEntity)
}
