package com.sleepsounds.storytelling.data.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Broadcasts a monotonically increasing revision counter whenever catalog tables are updated via raw SQL.
 */
@Singleton
class CatalogRefreshBus @Inject constructor() {
    private val _revision = MutableStateFlow(0)
    val revision: StateFlow<Int> = _revision.asStateFlow()

    fun notifyCatalogUpdated() {
        _revision.update { current -> current + 1 }
    }
}
