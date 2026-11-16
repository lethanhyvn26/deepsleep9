package com.sleepsounds.storytelling.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.sleepsounds.storytelling.data.local.entity.SleepPresetEntity
import com.sleepsounds.storytelling.data.local.entity.SleepPresetItemEntity

/**
 * Data access object for multi-layer sleep scenes (`sleep_preset`) and their default layers.
 */
@Dao
interface SleepPresetDao {

    @Query("SELECT * FROM sleep_preset ORDER BY sort_index ASC, id ASC")
    suspend fun getAll(): List<SleepPresetEntity>

    @Query("SELECT * FROM sleep_preset WHERE is_favorite = 1 ORDER BY sort_index ASC, id ASC")
    suspend fun getFavorites(): List<SleepPresetEntity>

    @Query("SELECT * FROM sleep_preset WHERE id = :presetId LIMIT 1")
    suspend fun getById(presetId: String): SleepPresetEntity?

    @Query("SELECT * FROM sleep_preset_item WHERE preset_id = :targetPresetId ORDER BY sort_index ASC, sound_id ASC")
    suspend fun getItemsForPreset(targetPresetId: String): List<SleepPresetItemEntity>

    @Query("SELECT * FROM sleep_preset_item ORDER BY preset_id ASC, sort_index ASC, sound_id ASC")
    suspend fun getAllItems(): List<SleepPresetItemEntity>

    @Update
    suspend fun update(presetEntity: SleepPresetEntity)
}
