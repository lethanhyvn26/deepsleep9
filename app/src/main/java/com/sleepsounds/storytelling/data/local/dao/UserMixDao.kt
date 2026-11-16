package com.sleepsounds.storytelling.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sleepsounds.storytelling.data.local.entity.UserMixEntity
import com.sleepsounds.storytelling.data.local.entity.UserMixItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for custom sound mixes saved by the user.
 */
@Dao
interface UserMixDao {

    @Query("SELECT * FROM user_mix ORDER BY createdAt DESC")
    suspend fun getAll(): List<UserMixEntity>

    @Query("SELECT * FROM user_mix ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<UserMixEntity>>

    @Query("SELECT * FROM user_mix WHERE id = :mixId LIMIT 1")
    suspend fun getById(mixId: String): UserMixEntity?

    @Query("SELECT * FROM user_mix_item WHERE mixId = :mixId")
    suspend fun getItemsForMix(mixId: String): List<UserMixItemEntity>

    @Query("SELECT * FROM user_mix_item")
    suspend fun getAllItems(): List<UserMixItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMix(mix: UserMixEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<UserMixItemEntity>)

    @Query("DELETE FROM user_mix WHERE id = :mixId")
    suspend fun deleteMix(mixId: String)

    @Query("DELETE FROM user_mix_item WHERE mixId = :mixId")
    suspend fun deleteItemsForMix(mixId: String)

    @Transaction
    suspend fun saveMix(mix: UserMixEntity, items: List<UserMixItemEntity>) {
        insertMix(mix)
        deleteItemsForMix(mix.id)
        insertItems(items)
    }
}
data class UserMixWithItems(
    val mix: UserMixEntity,
    val items: List<UserMixItemEntity>,
)
