package com.sleepsounds.storytelling.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Singleton Room entity (`id = 1`) storing catalog synchronization metadata (`sync_state`).
 */
@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey
    val id: Int = SINGLETON_ID,
    val manifestVersion: Int = 0,
    val lastSyncAt: Long = 0L,
    val soundsHash: String = "",
    val mixesHash: String = "",
    val podcastsHash: String = "",
) {
    companion object {
        const val SINGLETON_ID: Int = 1
    }
}
