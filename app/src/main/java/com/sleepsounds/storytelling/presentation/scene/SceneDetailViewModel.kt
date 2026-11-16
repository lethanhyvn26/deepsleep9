package com.sleepsounds.storytelling.presentation.scene

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepsounds.storytelling.data.repository.ContentRepository
import com.sleepsounds.storytelling.domain.model.MixSoundState
import com.sleepsounds.storytelling.domain.model.SleepPreset
import com.sleepsounds.storytelling.presentation.navigation.AppRoute
import com.sleepsounds.storytelling.service.ActiveSoundPlayback
import com.sleepsounds.storytelling.service.AudioPlayerManager
import com.sleepsounds.storytelling.service.PlaybackMode
import com.sleepsounds.storytelling.service.PlaybackState
import com.sleepsounds.storytelling.service.SleepTimerManager
import com.sleepsounds.storytelling.service.SleepTimerState
import com.sleepsounds.storytelling.util.CategoryIcons
import com.sleepsounds.storytelling.util.MediaAssetResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel coordinating playback, layer volume sliders, and favorites for a sleep scene.
 */
@HiltViewModel
class SceneDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val audioPlayerManager: AudioPlayerManager,
    private val contentRepository: ContentRepository,
    private val mediaAssetResolver: MediaAssetResolver,
    sleepTimerManager: SleepTimerManager,
) : ViewModel() {

    private val targetSceneId: String =
        savedStateHandle.get<String>(AppRoute.SceneDetail.SCENE_ID_ARG).orEmpty()

    val timerState: StateFlow<SleepTimerState> = sleepTimerManager.state

    private val headerVisualState = MutableStateFlow<Pair<String?, String>>(null to "")
    private val layerDisplayState = MutableStateFlow<List<MixSoundDisplay>>(emptyList())
    private val sceneFavoriteState = MutableStateFlow(false)

    val uiState: StateFlow<SceneDetailUiState> = combine(
        audioPlayerManager.playbackState,
        headerVisualState,
        layerDisplayState,
        sceneFavoriteState,
    ) { playback, (coverUri, sceneTitle), layers, liked ->
        SceneDetailUiState(
            presetId = targetSceneId,
            playback = playback,
            backgroundImageUri = coverUri,
            displayTitle = sceneTitle.ifBlank { playback.title },
            mixSounds = layers,
            isFavorite = liked,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SceneDetailUiState(),
    )

    init {
        viewModelScope.launch {
            startOrResumeScenePlayback()
        }
        viewModelScope.launch {
            audioPlayerManager.playbackState.collect { playback ->
                updateHeaderVisuals(playback)
                updateLayerDisplays(playback)
            }
        }
        viewModelScope.launch {
            contentRepository.observeSleepPresets().collect { presetList ->
                val matching = presetList.firstOrNull { it.id == targetSceneId }
                if (matching != null) {
                    sceneFavoriteState.value = matching.isFavorite
                }
                updateLayerDisplays(audioPlayerManager.playbackState.value)
            }
        }
    }

    private suspend fun startOrResumeScenePlayback() {
        val scene = contentRepository.getSleepPresetById(targetSceneId) ?: return
        sceneFavoriteState.value = scene.isFavorite

        val playback = audioPlayerManager.playbackState.value
        val isSameSceneActive = playback.mode == PlaybackMode.MIXER &&
            playback.currentTrackId == targetSceneId &&
            playback.activeSounds.isNotEmpty()

        if (!isSameSceneActive) {
            launchScenePlayback(scene)
        } else if (!playback.isPlaying) {
            audioPlayerManager.resumeAll()
        }
        updateHeaderVisuals(audioPlayerManager.playbackState.value)
    }

    private fun launchScenePlayback(scene: SleepPreset) {
        audioPlayerManager.playSleepPreset(
            presetId = scene.id,
            presetTitle = scene.name,
            items = scene.items.map { layer ->
                AudioPlayerManager.PresetPlaybackItem(
                    soundId = layer.soundId,
                    name = layer.name,
                    audioPath = layer.audioPath,
                    volume = layer.volume,
                    isPremium = false,
                )
            },
            userIsPremium = true,
            iconCategory = CategoryIcons.resolveForMix(scene.categories),
        )
    }

    private suspend fun updateHeaderVisuals(playback: PlaybackState) {
        val scene = contentRepository.getSleepPresetById(targetSceneId)
        if (scene != null) {
            sceneFavoriteState.value = scene.isFavorite
        }
        val coverUri = scene?.imagePath?.takeIf { it.isNotBlank() }
            ?.let(mediaAssetResolver::resolveCoverUri)
            ?: scene?.displaySymbolPath?.let { symbolPath ->
                mediaAssetResolver.resolveSymbolUri(symbolPath)?.let { uri ->
                    if (uri.startsWith("http://") || uri.startsWith("https://")) {
                        uri
                    } else {
                        mediaAssetResolver.toCoilUri(uri)
                    }
                }
            }
        headerVisualState.value = coverUri to (scene?.name ?: playback.title)
    }

    private suspend fun updateLayerDisplays(playback: PlaybackState) {
        val scene = contentRepository.getSleepPresetById(targetSceneId)
        val iconBySoundId = scene?.items?.associate { it.soundId to it.symbolPath }.orEmpty()

        val activeLayers = when {
            playback.mode == PlaybackMode.MIXER && playback.currentTrackId == targetSceneId ->
                playback.activeSounds
            scene != null ->
                scene.items.map { item ->
                    ActiveSoundPlayback(
                        soundId = item.soundId,
                        name = item.name,
                        volume = item.volume,
                    )
                }
            else -> emptyList()
        }

        layerDisplayState.value = activeLayers.map { layer ->
            val resolvedIcon = iconBySoundId[layer.soundId]
                ?: mediaAssetResolver.resolveSoundSymbolUri(layer.soundId, null)
            MixSoundDisplay(
                soundId = layer.soundId,
                name = layer.name,
                volume = layer.volume,
                iconUri = resolvedIcon,
            )
        }
    }

    fun setLayerVolume(soundId: String, volume: Float) {
        audioPlayerManager.setMixerVolume(soundId, volume.coerceIn(0f, 1f))
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            sceneFavoriteState.value = contentRepository.toggleSceneFavorite(targetSceneId)
        }
    }

    fun saveSceneAsUserMix(name: String, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            val displayedLayers = uiState.value.mixSounds
            if (displayedLayers.isEmpty()) return@launch
            val mixStates = displayedLayers.map { displayed ->
                val soundTrack = contentRepository.getSoundById(displayed.soundId)
                MixSoundState(
                    soundId = displayed.soundId,
                    name = displayed.name,
                    isActive = true,
                    volume = displayed.volume,
                    isPremium = false,
                    categories = soundTrack?.categories ?: emptyList(),
                    symbolPath = displayed.iconUri,
                    audioPath = soundTrack?.audioPath,
                )
            }
            contentRepository.saveUserMix(name, mixStates)
            if (!sceneFavoriteState.value) {
                sceneFavoriteState.value = contentRepository.toggleSceneFavorite(targetSceneId)
            }
            onSaved()
        }
    }

    fun pauseResume() {
        val currentPlayback = uiState.value.playback
        if (currentPlayback.mode == PlaybackMode.IDLE || currentPlayback.currentTrackId != targetSceneId) {
            viewModelScope.launch {
                startOrResumeScenePlayback()
            }
        } else if (currentPlayback.isPlaying) {
            audioPlayerManager.pauseAll()
        } else {
            audioPlayerManager.resumeAll()
        }
    }
}
data class SceneDetailUiState(
    val presetId: String = "",
    val playback: PlaybackState = PlaybackState(),
    val backgroundImageUri: String? = null,
    val displayTitle: String = "",
    val mixSounds: List<MixSoundDisplay> = emptyList(),
    val isFavorite: Boolean = false,
)
data class MixSoundDisplay(
    val soundId: String,
    val name: String,
    val volume: Float,
    val iconUri: String?,
)
