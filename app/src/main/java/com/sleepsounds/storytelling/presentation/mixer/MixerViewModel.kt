package com.sleepsounds.storytelling.presentation.mixer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepsounds.storytelling.ads.AdMobManager
import com.sleepsounds.storytelling.data.remote.ContentCategory
import com.sleepsounds.storytelling.data.repository.ContentRepository
import com.sleepsounds.storytelling.domain.model.AudioTrack
import com.sleepsounds.storytelling.domain.model.MixSoundState
import com.sleepsounds.storytelling.presentation.navigation.AppRoute
import com.sleepsounds.storytelling.service.AudioPlayerManager
import com.sleepsounds.storytelling.service.PlaybackDeepLinks
import com.sleepsounds.storytelling.service.PlaybackMode
import com.sleepsounds.storytelling.util.CategoryIcons
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing sound layer toggles, volume sliders, category filters, and preset customization.
 */
@HiltViewModel
class MixerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contentRepository: ContentRepository,
    private val audioPlayerManager: AudioPlayerManager,
    val adMobManager: AdMobManager,
) : ViewModel() {

    private val targetPresetOrMixId: String? =
        savedStateHandle.get<String>(AppRoute.Mixer.PRESET_ID_ARG)?.takeIf { it.isNotBlank() }

    private val activeEditMode = MutableStateFlow<MixerEditMode>(MixerEditMode.None)
    private val pendingVolumeOverrides = MutableStateFlow<Map<String, MixSoundState>>(emptyMap())
    private val categoryFilter = MutableStateFlow<String?>(null)
    private var sessionBackup: MixerSessionBackup? = null

    val uiState: StateFlow<MixerUiState> = combine(
        contentRepository.observeSounds(),
        contentRepository.observeMediaCategories(),
        pendingVolumeOverrides,
        categoryFilter,
        audioPlayerManager.playbackState,
    ) { allSounds, catalogCategories, volumeMap, activeCat, playback ->
        val presentCategoryIds = allSounds.flatMap { it.categories }.distinct()
        val visibleCategories = if (catalogCategories.isNotEmpty()) {
            catalogCategories.filter { cat ->
                presentCategoryIds.any { it.equals(cat.id, ignoreCase = true) }
            }.ifEmpty { catalogCategories }
        } else {
            presentCategoryIds.map { rawId ->
                ContentCategory(
                    id = rawId,
                    label = rawId.replace('_', ' ').lowercase().replaceFirstChar { it.uppercaseChar() },
                )
            }
        }

        val matchingTracks = filterTracksByCategory(allSounds, activeCat)
        val soundStates = matchingTracks.map { track ->
            val overrideEntry = volumeMap[track.id]
            val playingLayer = playback.activeSounds.firstOrNull { it.soundId == track.id }
            MixSoundState(
                soundId = track.id,
                name = track.name,
                isActive = playingLayer != null,
                volume = playingLayer?.volume ?: overrideEntry?.volume ?: track.defaultVolume,
                isPremium = track.isPremium,
                categories = track.categories,
                symbolPath = track.imagePath,
                audioPath = track.audioPath,
            )
        }

        MixerUiState(
            sounds = soundStates,
            categories = visibleCategories,
            activeCount = soundStates.count { it.isActive },
            selectedCategory = activeCat,
            editTargetId = targetPresetOrMixId,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MixerUiState())

    val playbackState = audioPlayerManager.playbackState

    val isEditMode: Boolean
        get() = activeEditMode.value !is MixerEditMode.None

    val isPresetEditMode: Boolean
        get() = isEditMode

    init {
        viewModelScope.launch {
            sessionBackup = snapshotCurrentPlayback()
            activeEditMode.value = determineEditMode(targetPresetOrMixId)
        }
    }

    fun selectCategory(category: String?) {
        categoryFilter.value = category
    }

    fun toggleSound(sound: MixSoundState, navigateToPlayer: Boolean = false) {
        viewModelScope.launch {
            if (sound.isActive) {
                audioPlayerManager.removeMixerSound(sound.soundId)
            } else {
                audioPlayerManager.toggleMixerSound(
                    soundId = sound.soundId,
                    name = sound.name,
                    audioFilePath = sound.audioPath,
                    volume = sound.volume,
                    isPremiumTrack = false,
                    userIsPremium = true,
                    navigateToPlayer = navigateToPlayer,
                )
            }
        }
    }

    fun adjustVolume(soundId: String, delta: Float) {
        val target = uiState.value.sounds.firstOrNull { it.soundId == soundId } ?: return
        setVolume(soundId, (target.volume + delta).coerceIn(0f, 1f))
    }

    fun setVolume(soundId: String, volume: Float) {
        val target = uiState.value.sounds.firstOrNull { it.soundId == soundId } ?: return
        val clamped = volume.coerceIn(0f, 1f)
        pendingVolumeOverrides.update { prev ->
            prev + (soundId to target.copy(volume = clamped))
        }
        audioPlayerManager.setMixerVolume(soundId, clamped)
    }

    fun pauseAll() = audioPlayerManager.pauseAll()

    fun resumeAll() = audioPlayerManager.resumeAll()

    fun saveMix(name: String, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            val activeLayers = uiState.value.sounds.filter { it.isActive }
            if (activeLayers.isNotEmpty()) {
                contentRepository.saveUserMix(name, activeLayers)
                onSaved()
            }
        }
    }

    fun confirmEdit(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val mode = ensureEditModeResolved()
            val activeLayers = uiState.value.sounds.filter { it.isActive }
            if (activeLayers.isEmpty()) {
                when (mode) {
                    is MixerEditMode.SleepPreset,
                    is MixerEditMode.UserMix -> replayTargetMode(mode)
                    MixerEditMode.None -> Unit
                }
                onDone()
                return@launch
            }
            when (mode) {
                is MixerEditMode.SleepPreset -> {
                    contentRepository.saveUserPresetCustomization(mode.presetId, activeLayers)
                    startPresetPlayback(mode.presetId)
                }
                is MixerEditMode.UserMix -> {
                    contentRepository.updateUserMix(mode.mixId, activeLayers)
                    startUserMixPlayback(mode.mixId)
                }
                MixerEditMode.None -> Unit
            }
            onDone()
        }
    }

    fun resetEdit(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            when (val mode = ensureEditModeResolved()) {
                is MixerEditMode.SleepPreset -> startFactoryPresetPlayback(mode.presetId)
                is MixerEditMode.UserMix -> startUserMixPlayback(mode.mixId)
                MixerEditMode.None -> {
                    val activeSoundIds = uiState.value.sounds.filter { it.isActive }.map { it.soundId }
                    activeSoundIds.forEach(audioPlayerManager::removeMixerSound)
                }
            }
            pendingVolumeOverrides.value = emptyMap()
            onDone()
        }
    }

    fun cancelEdit(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val backup = sessionBackup
            if (backup != null) {
                restorePlaybackFromBackup(backup)
            } else {
                replayTargetMode(ensureEditModeResolved())
            }
            onDone()
        }
    }

    fun confirmPresetEdit(onDone: () -> Unit = {}) = confirmEdit(onDone)
    fun resetPresetEdit(onDone: () -> Unit = {}) = resetEdit(onDone)
    fun cancelPresetEdit(onDone: () -> Unit = {}) = cancelEdit(onDone)

    fun stopAll() = audioPlayerManager.stopAll()

    private suspend fun ensureEditModeResolved(): MixerEditMode {
        val current = activeEditMode.value
        if (current !is MixerEditMode.None || targetPresetOrMixId.isNullOrBlank()) return current
        return determineEditMode(targetPresetOrMixId).also { activeEditMode.value = it }
    }

    private suspend fun determineEditMode(targetId: String?): MixerEditMode {
        if (targetId.isNullOrBlank()) return MixerEditMode.None
        if (contentRepository.getSleepPresetById(targetId) != null) {
            return MixerEditMode.SleepPreset(targetId)
        }
        if (contentRepository.getUserMixById(targetId) != null) {
            return MixerEditMode.UserMix(targetId)
        }
        return MixerEditMode.None
    }

    private suspend fun replayTargetMode(mode: MixerEditMode = activeEditMode.value) {
        when (mode) {
            is MixerEditMode.SleepPreset -> startPresetPlayback(mode.presetId)
            is MixerEditMode.UserMix -> startUserMixPlayback(mode.mixId)
            MixerEditMode.None -> Unit
        }
    }

    private suspend fun snapshotCurrentPlayback(): MixerSessionBackup? {
        val currentPlayback = audioPlayerManager.playbackState.value
        if (currentPlayback.mode != PlaybackMode.MIXER) return null
        val playbackItems = currentPlayback.activeSounds.map { layer ->
            val track = contentRepository.getSoundById(layer.soundId)
            AudioPlayerManager.PresetPlaybackItem(
                soundId = layer.soundId,
                name = layer.name,
                audioPath = track?.audioPath,
                volume = layer.volume,
                isPremium = false,
            )
        }
        return MixerSessionBackup(
            presetId = currentPlayback.currentTrackId,
            title = currentPlayback.title,
            items = playbackItems,
            iconCategory = currentPlayback.iconCategory,
            contentRoute = currentPlayback.contentRoute,
        )
    }

    private fun restorePlaybackFromBackup(backup: MixerSessionBackup) {
        val defaultRoute = backup.presetId
            ?.takeIf { it.isNotBlank() }
            ?.let(PlaybackDeepLinks::sceneRoute)
            ?: AppRoute.NowPlaying.route

        audioPlayerManager.playSleepPreset(
            presetId = backup.presetId.orEmpty(),
            presetTitle = backup.title,
            items = backup.items,
            userIsPremium = true,
            iconCategory = backup.iconCategory ?: CategoryIcons.CUSTOM_KEY,
            contentRoute = backup.contentRoute?.takeIf { it.isNotBlank() } ?: defaultRoute,
        )
    }

    private suspend fun startFactoryPresetPlayback(presetId: String) {
        val factoryScene = contentRepository.getSleepPresetFactoryById(presetId) ?: return
        audioPlayerManager.playSleepPreset(
            presetId = factoryScene.id,
            presetTitle = factoryScene.name,
            items = factoryScene.items.map { item ->
                AudioPlayerManager.PresetPlaybackItem(
                    soundId = item.soundId,
                    name = item.name,
                    audioPath = item.audioPath,
                    volume = item.volume,
                    isPremium = false,
                )
            },
            userIsPremium = true,
            iconCategory = CategoryIcons.resolveForMix(factoryScene.categories),
        )
    }

    private suspend fun startPresetPlayback(presetId: String) {
        val scene = contentRepository.getSleepPresetById(presetId) ?: return
        audioPlayerManager.playSleepPreset(
            presetId = scene.id,
            presetTitle = scene.name,
            items = scene.items.map { item ->
                AudioPlayerManager.PresetPlaybackItem(
                    soundId = item.soundId,
                    name = item.name,
                    audioPath = item.audioPath,
                    volume = item.volume,
                    isPremium = false,
                )
            },
            userIsPremium = true,
            iconCategory = CategoryIcons.resolveForMix(scene.categories),
        )
    }

    private suspend fun startUserMixPlayback(mixId: String) {
        val userMix = contentRepository.getUserMixById(mixId) ?: return
        audioPlayerManager.playSleepPreset(
            presetId = userMix.id,
            presetTitle = userMix.name,
            items = userMix.items.map { item ->
                AudioPlayerManager.PresetPlaybackItem(
                    soundId = item.soundId,
                    name = item.name,
                    audioPath = item.audioPath,
                    volume = item.volume,
                    isPremium = false,
                )
            },
            userIsPremium = true,
            iconCategory = CategoryIcons.CUSTOM_KEY,
            contentRoute = AppRoute.NowPlaying.route,
        )
    }

    private fun filterTracksByCategory(
        tracks: List<AudioTrack>,
        categoryKey: String?,
    ): List<AudioTrack> {
        if (categoryKey.isNullOrBlank()) return tracks
        return tracks.filter { track ->
            track.categories.any { it.equals(categoryKey, ignoreCase = true) }
        }
    }
}
private data class MixerSessionBackup(
    val presetId: String?,
    val title: String,
    val items: List<AudioPlayerManager.PresetPlaybackItem>,
    val iconCategory: String? = null,
    val contentRoute: String? = null,
)
sealed class MixerEditMode {
    data object None : MixerEditMode()
    data class SleepPreset(val presetId: String) : MixerEditMode()
    data class UserMix(val mixId: String) : MixerEditMode()
}
data class MixerUiState(
    val sounds: List<MixSoundState> = emptyList(),
    val categories: List<ContentCategory> = emptyList(),
    val activeCount: Int = 0,
    val selectedCategory: String? = null,
    val editTargetId: String? = null,
)
