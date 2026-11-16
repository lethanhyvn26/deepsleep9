package com.sleepsounds.storytelling.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sleepsounds.storytelling.data.local.entity.SleepSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for recorded overnight sleep tracking sessions and hypnogram data.
 */
@Dao
interface SleepSessionDao {

    @Query("SELECT * FROM sleep_session ORDER BY started_at DESC")
    fun observeAll(): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_session ORDER BY started_at DESC LIMIT :maxRows")
    suspend fun getRecent(maxRows: Int = 30): List<SleepSessionEntity>

    @Query("SELECT * FROM sleep_session WHERE id = :sessionId LIMIT 1")
    suspend fun getById(sessionId: String): SleepSessionEntity?

    @Query("SELECT * FROM sleep_session WHERE started_at BETWEEN :fromEpochMs AND :toEpochMs ORDER BY started_at DESC")
    suspend fun getBetween(fromEpochMs: Long, toEpochMs: Long): List<SleepSessionEntity>

    @Query("SELECT COUNT(*) FROM sleep_session")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: SleepSessionEntity)

    @Query("DELETE FROM sleep_session WHERE id = :sessionId")
    suspend fun deleteById(sessionId: String)
}
