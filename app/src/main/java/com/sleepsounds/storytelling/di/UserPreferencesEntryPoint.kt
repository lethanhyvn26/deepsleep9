package com.sleepsounds.storytelling.di

import com.sleepsounds.storytelling.data.repository.UserPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint allowing Compose UI components to read user preferences.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface UserPreferencesEntryPoint {
    fun userPreferencesRepository(): UserPreferencesRepository
}
