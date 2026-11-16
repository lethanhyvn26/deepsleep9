package com.sleepsounds.storytelling.data.sync

/**
 * Descriptor for a remote catalog manifest payload (`manifest.json`).
 */
data class ContentManifest(
    val version: Int,
    val minAppVersion: Int,
    val updatedAt: String,
    val resources: Map<String, String>,
    val hashes: Map<String, String>,
)
/**
 * Outcome of a local or remote catalog synchronization pass.
 */
sealed class CatalogSyncResult {
    data object Idle : CatalogSyncResult()
    data object UpToDate : CatalogSyncResult()
    data class Updated(val version: Int) : CatalogSyncResult()
    data class Failed(val message: String) : CatalogSyncResult()
    data class ForceUpdateRequired(val minAppVersion: Int) : CatalogSyncResult()
}
