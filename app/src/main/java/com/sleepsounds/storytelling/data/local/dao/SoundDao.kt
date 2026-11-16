package com.sleepsounds.storytelling.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.sleepsounds.storytelling.data.local.entity.SoundEntity

/**
 * Data access object for individual ambient sound tracks (`sound`).
 */
@Dao
interface SoundDao {

    @Query("SELECT * FROM sound ORDER BY sort_index ASC, id ASC")
    suspend fun getAll(): List<SoundEntity>

    @Query("SELECT * FROM sound WHERE id = :soundId LIMIT 1")
    suspend fun getById(soundId: String): SoundEntity?

    @Query("SELECT * FROM sound WHERE is_favorite = 1 ORDER BY sort_index ASC, id ASC")
    suspend fun getFavorites(): List<SoundEntity>

    @Update
    suspend fun update(trackEntity: SoundEntity)
}
