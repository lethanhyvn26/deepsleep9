package com.sleepsounds.storytelling.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.sleepsounds.storytelling.data.remote.ShopCatalogRepository
import com.sleepsounds.storytelling.util.R2Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides the configured [FirebaseDatabase] instance connected to the Southeast Asia RTDB endpoint.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseDatabaseModule {

    @Provides
    @Singleton
    fun provideFirebaseDatabase(@ApplicationContext context: Context): FirebaseDatabase {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        return FirebaseDatabase.getInstance(ShopCatalogRepository.DATABASE_URL).also { db ->
            runCatching { db.setPersistenceEnabled(true) }
                .onFailure { R2Log.w("FirebaseDatabase persistence already configured: ${it.message}") }
        }
    }
}
