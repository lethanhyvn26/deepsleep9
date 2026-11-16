package com.sleepsounds.storytelling.presentation.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.sleepsounds.storytelling.presentation.components.rememberMediaAssetResolver
import com.sleepsounds.storytelling.presentation.scene.MixSoundDisplay
import com.sleepsounds.storytelling.service.PlaybackMode
import com.sleepsounds.storytelling.service.SleepTimerManager
import com.sleepsounds.storytelling.ui.theme.ImmersiveSystemBars
import com.sleepsounds.storytelling.ui.theme.tr

/**
 * Full-screen player switching between [LessonPlayerScreen] (for bedtime stories/podcasts)
 * and [MixDetailScreen] (for multi-layer soundscapes and custom mixes).
 */
@Composable
fun NowPlayingScreen(
    onBack: () -> Unit,
    onOpenMixer: (editTargetId: String?) -> Unit,
    sleepTimerManager: SleepTimerManager,
    onStartTracker: (() -> Unit)? = null,
    viewModel: NowPlayingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timerSnap by viewModel.timerState.collectAsStateWithLifecycle()
    val mediaResolver = rememberMediaAssetResolver()
    val fallbackWallpaperUri = rememberAppBackgroundUri()

    var timerModalOpen by remember { mutableStateOf(false) }
    var saveMixModalOpen by remember { mutableStateOf(false) }
    var showSavedBanner by remember { mutableStateOf(false) }

    ImmersiveSystemBars()

    LaunchedEffect(uiState.playback.mode) {
        if (uiState.playback.mode == PlaybackMode.IDLE) {
            onBack()
        }
    }

    if (uiState.playback.mode == PlaybackMode.IDLE) return

    if (timerModalOpen) {
        SleepTimerBottomSheet(
            sleepTimerManager = sleepTimerManager,
            onDismiss = { timerModalOpen = false },
        )
    }

    if (saveMixModalOpen) {
        SaveMixDialog(
            defaultName = "Mix${uiState.savedMixCount}",
            onDismiss = { saveMixModalOpen = false },
            onSave = { mixTitle ->
                viewModel.saveCurrentMix(mixTitle) {
                    saveMixModalOpen = false
                    showSavedBanner = true
                }
            },
        )
    }

    val backdropUri = uiState.backgroundImageUri?.takeIf { it.isNotBlank() }
        ?: fallbackWallpaperUri
    val activeTimerCaption = if (timerSnap.isActive) {
        sleepTimerManager.formatTimerLabel(timerSnap.remainingSeconds)
    } else {
        stringResource(R.string.tool_timer)
    }

    if (uiState.playback.mode == PlaybackMode.PODCARD) {
        LessonPlayerScreen(
            seriesTitle = uiState.displayTitle,
            playback = uiState.playback,
            timerLabel = activeTimerCaption,
            categoryLabel = tr(uiState.playback.subtitle.ifBlank { "Meditation" }),
            isFavorite = uiState.isFavorite,
            audioPodcasts = uiState.audioPodcasts,
            onToggleFavorite = viewModel::toggleCurrentFavorite,
            onBack = onBack,
            onTimerClick = { timerModalOpen = true },
            onPlayPause = viewModel::pauseResume,
            onSeek = viewModel::seekTo,
            onPreviousLesson = { viewModel.playAdjacentChapter(-1) },
            onNextLesson = { viewModel.playAdjacentChapter(1) },
            onSelectBackgroundSound = viewModel::setPodcardBackgroundSound,
            onChangeBackgroundVolume = viewModel::setPodcardBackgroundVolume,
            onSelectPodcastFromQueue = viewModel::selectPodcastFromQueue,
            onStartTracker = onStartTracker,
        )
        return
    }

    val displayLayers = when (uiState.playback.mode) {
        PlaybackMode.MIXER -> uiState.playback.activeSounds.map { activeLayer ->
            val catalogTrack = uiState.soundTracks[activeLayer.soundId]
            MixSoundDisplay(
                soundId = activeLayer.soundId,
                name = activeLayer.name,
                volume = activeLayer.volume,
                iconUri = catalogTrack?.imagePath
                    ?: mediaResolver.resolveSoundSymbolUri(activeLayer.soundId, null),
            )
        }
        PlaybackMode.SINGLE -> uiState.primaryTrack?.let { singleTrack ->
            listOf(
                MixSoundDisplay(
                    soundId = singleTrack.id,
                    name = singleTrack.name,
                    volume = singleTrack.defaultVolume,
                    iconUri = singleTrack.imagePath
                        ?: mediaResolver.resolveSoundSymbolUri(singleTrack.id, null),
                ),
            )
        } ?: emptyList()
        else -> emptyList()
    }

    val isAdHocCustomMix = uiState.playback.mode == PlaybackMode.MIXER &&
        uiState.playback.currentTrackId == null
    val targetPresetId = uiState.playback.currentTrackId

    MixDetailScreen(
        title = uiState.displayTitle,
        backgroundUri = backdropUri,
        mixSounds = displayLayers,
        isPlaying = uiState.playback.isPlaying,
        timerLabel = activeTimerCaption,
        timerRemainingSeconds = if (timerSnap.isActive) timerSnap.remainingSeconds else null,
        timerTotalSeconds = if (timerSnap.isActive) timerSnap.totalSeconds else null,
        isFavorite = uiState.isFavorite,
        actions = MixDetailActions(
            onBack = onBack,
            onTimerClick = { timerModalOpen = true },
            onEditMix = { onOpenMixer(targetPresetId) },
            onPlayPause = viewModel::pauseResume,
            onToggleFavorite = if (targetPresetId != null) {
                viewModel::toggleCurrentFavorite
            } else {
                { saveMixModalOpen = true }
            },
            onSaveMix = {
                if (targetPresetId != null && !isAdHocCustomMix) {
                    viewModel.toggleCurrentFavorite()
                } else {
                    saveMixModalOpen = true
                }
            },
            onClearAll = {
                sleepTimerManager.cancelTimer()
                viewModel.stop()
            },
            onVolumeChange = viewModel::setMixerVolume,
            onRemoveSound = if (uiState.playback.mode == PlaybackMode.MIXER) {
                viewModel::removeMixerSound
            } else {
                null
            },
        ),
        showSaveMix = uiState.playback.mode == PlaybackMode.MIXER,
        showEdit = !isAdHocCustomMix,
        mixSavedMessage = showSavedBanner,
    )
}
