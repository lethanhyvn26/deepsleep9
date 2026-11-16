package com.sleepsounds.storytelling.di

import javax.inject.Qualifier

/**
 * Qualifier for the process-wide background [kotlinx.coroutines.CoroutineScope].
 */
@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
