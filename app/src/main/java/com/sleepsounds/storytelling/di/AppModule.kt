package com.sleepsounds.storytelling.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

private const val USER_PREFS_DATASTORE_NAME = "deepsleep_prefs"

private val Context.userPreferencesStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFS_DATASTORE_NAME,
)

/**
 * Application-scoped Hilt bindings for DataStore and background coroutine execution.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext appContext: Context,
    ): DataStore<Preferences> = appContext.userPreferencesStore

    @Provides
    @Singleton
    @ApplicationScope
    fun provideGlobalCoroutineScope(): CoroutineScope {
        val supervisor = SupervisorJob()
        return CoroutineScope(supervisor + Dispatchers.IO)
    }
}
