package com.sleepsounds.storytelling.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sleepsounds.storytelling.data.local.entity.SmartAlarmEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for the primary smart wake-up alarm configuration (`id = 1`).
 */
@Dao
interface SmartAlarmDao {

    @Query("SELECT * FROM smart_alarm WHERE id = 1 LIMIT 1")
    fun observePrimaryAlarm(): Flow<SmartAlarmEntity?>

    @Query("SELECT * FROM smart_alarm WHERE id = 1 LIMIT 1")
    suspend fun getPrimaryAlarm(): SmartAlarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(alarmEntity: SmartAlarmEntity)
}
