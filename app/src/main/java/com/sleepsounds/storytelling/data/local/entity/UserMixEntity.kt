package com.sleepsounds.storytelling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Sound layer row belonging to a [UserMixEntity] (`user_mix_item`).
 */
@Entity(
    tableName = "user_mix_item",
    primaryKeys = ["mixId", "soundId"],
)
data class UserMixItemEntity(
    val mixId: String,
    val soundId: String,
    val volumeLevel: Float,
)
/**
 * Header entity for a user-saved custom sound mix (`user_mix`).
 */
@Entity(tableName = "user_mix")
data class UserMixEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val createdAt: Long,
)
