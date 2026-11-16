package com.sleepsounds.storytelling.presentation.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.presentation.components.MixDetailActions
import com.sleepsounds.storytelling.presentation.components.MixDetailScreen
import com.sleepsounds.storytelling.presentation.components.SaveMixDialog
import com.sleepsounds.storytelling.presentation.components.SleepTimerBottomSheet
import com.sleepsounds.storytelling.presentation.components.formatTimerLabel
import com.sleepsounds.storytelling.presentation.components.rememberAppBackgroundUri
import com.sleepsounds.storytelling.service.SleepTimerManager
import com.sleepsounds.storytelling.ui.theme.ImmersiveSystemBars

/**
 * Detail and playback screen for a multi-layer sleep scene (`SleepPreset`).
 */
@Composable
fun SceneDetailScreen(
    onBack: () -> Unit,
    onOpenMixer: (presetId: String) -> Unit,
    sleepTimerManager: SleepTimerManager,
    viewModel: SceneDetailViewModel = hiltViewModel(),
) {
    ImmersiveSystemBars()

    val sceneState by viewModel.uiState.collectAsStateWithLifecycle()
    val timerSnap by viewModel.timerState.collectAsStateWithLifecycle()
    val fallbackWallpaperUri = rememberAppBackgroundUri()

    var timerSheetOpen by remember { mutableStateOf(false) }
    var saveMixDialogOpen by remember { mutableStateOf(false) }
    var savedBannerShown by remember { mutableStateOf(false) }

    if (timerSheetOpen) {
        SleepTimerBottomSheet(
            sleepTimerManager = sleepTimerManager,
            onDismiss = { timerSheetOpen = false },
        )
    }

    if (saveMixDialogOpen) {
        SaveMixDialog(
            defaultName = sceneState.displayTitle.ifBlank { "Mix0" },
            onDismiss = { saveMixDialogOpen = false },
            onSave = { customName ->
                viewModel.saveSceneAsUserMix(customName) {
                    saveMixDialogOpen = false
                    savedBannerShown = true
                }
            },
        )
    }

    val resolvedBackdropUri = sceneState.backgroundImageUri?.takeIf { it.isNotBlank() }
        ?: fallbackWallpaperUri

    val formattedTimer = if (timerSnap.isActive) {
        sleepTimerManager.formatTimerLabel(timerSnap.remainingSeconds)
    } else {
        stringResource(R.string.tool_timer)
    }

    MixDetailScreen(
        title = sceneState.displayTitle,
        backgroundUri = resolvedBackdropUri,
        mixSounds = sceneState.mixSounds,
        isPlaying = sceneState.playback.isPlaying,
        timerLabel = formattedTimer,
        timerRemainingSeconds = if (timerSnap.isActive) timerSnap.remainingSeconds else null,
        timerTotalSeconds = if (timerSnap.isActive) timerSnap.totalSeconds else null,
        isFavorite = sceneState.isFavorite,
        actions = MixDetailActions(
            onBack = onBack,
            onTimerClick = { timerSheetOpen = true },
            onEditMix = { onOpenMixer(sceneState.presetId) },
            onPlayPause = viewModel::pauseResume,
            onToggleFavorite = viewModel::toggleFavorite,
            onSaveMix = viewModel::toggleFavorite,
            onVolumeChange = viewModel::setLayerVolume,
        ),
        showSaveMix = true,
        showEdit = true,
        mixSavedMessage = savedBannerShown,
    )
}
