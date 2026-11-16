package com.sleepsounds.storytelling.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleepsounds.storytelling.di.UserPreferencesEntryPoint
import com.sleepsounds.storytelling.util.AppBackgrounds
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.combine

/**
 * Observes the currently active wallpaper asset URI based on user preferences and VIP status.
 */
@Composable
fun rememberAppBackgroundUri(): String {
    val appCtx = LocalContext.current.applicationContext
    val prefsRepo = remember(appCtx) {
        EntryPointAccessors.fromApplication(appCtx, UserPreferencesEntryPoint::class.java)
            .userPreferencesRepository()
    }
    val resolvedUriFlow = remember(prefsRepo) {
        combine(
            prefsRepo.appBackgroundId,
            prefsRepo.isPremium,
        ) { selectedBgId, vipUnlocked ->
            AppBackgrounds.uriForUser(selectedBgId, vipUnlocked)
        }
    }
    val activeUri by resolvedUriFlow.collectAsStateWithLifecycle(
        initialValue = AppBackgrounds.uriFor(AppBackgrounds.DEFAULT_ID),
    )
    return activeUri
}
