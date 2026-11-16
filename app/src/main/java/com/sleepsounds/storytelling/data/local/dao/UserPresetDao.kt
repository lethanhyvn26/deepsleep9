package com.sleepsounds.storytelling.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sleepsounds.storytelling.data.local.entity.UserPresetCustomizationEntity
import com.sleepsounds.storytelling.data.local.entity.UserPresetItemEntity

/**
 * Data access object storing user-customized layer overrides for built-in sleep scenes.
 */
@Dao
interface UserPresetDao {

    @Query("SELECT * FROM user_preset_customization WHERE preset_id = :presetId LIMIT 1")
    suspend fun getCustomization(presetId: String): UserPresetCustomizationEntity?

    @Query("SELECT * FROM user_preset_item WHERE preset_id = :presetId")
    suspend fun getItemsForPreset(presetId: String): List<UserPresetItemEntity>

    @Query("SELECT * FROM user_preset_item")
    suspend fun getAllItems(): List<UserPresetItemEntity>

    @Query("SELECT preset_id FROM user_preset_customization WHERE is_modified = 1")
    suspend fun getModifiedPresetIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomization(entity: UserPresetCustomizationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<UserPresetItemEntity>)

    @Query("DELETE FROM user_preset_item WHERE preset_id = :presetId")
    suspend fun deleteItemsForPreset(presetId: String)

    @Query("DELETE FROM user_preset_customization WHERE preset_id = :presetId")
    suspend fun deleteCustomization(presetId: String)

    @Transaction
    suspend fun saveCustomization(
        customization: UserPresetCustomizationEntity,
        items: List<UserPresetItemEntity>,
    ) {
        insertCustomization(customization)
        deleteItemsForPreset(customization.presetId)
        insertItems(items)
    }

    @Transaction
    suspend fun resetCustomization(presetId: String) {
        deleteItemsForPreset(presetId)
        deleteCustomization(presetId)
    }
}
