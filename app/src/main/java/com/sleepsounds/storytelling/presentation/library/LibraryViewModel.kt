package com.sleepsounds.storytelling.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepsounds.storytelling.data.repository.ContentRepository
import com.sleepsounds.storytelling.domain.model.AudioTrack
import com.sleepsounds.storytelling.domain.model.PlaybackHistoryItem
import com.sleepsounds.storytelling.domain.model.Podcast
import com.sleepsounds.storytelling.domain.model.SleepPreset
import com.sleepsounds.storytelling.domain.model.UserMix
import com.sleepsounds.storytelling.presentation.navigation.AppRoute
import com.sleepsounds.storytelling.service.AudioPlayerManager
import com.sleepsounds.storytelling.util.CategoryIcons
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel backing the "My Aids" subtab (user saved mixes, favorited items, and listening history).
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val catalogRepository: ContentRepository,
    private val playbackManager: AudioPlayerManager,
) : ViewModel() {

    val uiState: StateFlow<LibraryUiState> = combine(
        catalogRepository.observeUserMixes(),
        catalogRepository.observeFavoriteSleepPresets(),
        catalogRepository.observeFavoritePodcasts(),
        catalogRepository.observeFavoriteSounds(),
        catalogRepository.observePlaybackHistory(),
    ) { customMixes, scenes, podcasts, sounds, history ->
        LibraryUiState(
            savedMixes = customMixes,
            favoriteScenes = scenes,
            favoritePodcasts = podcasts,
            favoriteSounds = sounds,
            playbackHistory = history,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun playMix(mix: UserMix) {
        val presetLayers = mix.items.map { layer ->
            AudioPlayerManager.PresetPlaybackItem(
                soundId = layer.soundId,
                name = layer.name,
                audioPath = layer.audioPath,
                volume = layer.volume,
                isPremium = false,
            )
        }
        playbackManager.playSleepPreset(
            presetId = mix.id,
            presetTitle = mix.name,
            items = presetLayers,
            userIsPremium = true,
            iconCategory = CategoryIcons.CUSTOM_KEY,
            contentRoute = AppRoute.NowPlaying.route,
        )
    }

    fun deleteMix(mixId: String) {
        viewModelScope.launch {
            catalogRepository.deleteUserMix(mixId)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            catalogRepository.clearPlaybackHistory()
        }
    }
}
data class LibraryUiState(
    val savedMixes: List<UserMix> = emptyList(),
    val favoriteScenes: List<SleepPreset> = emptyList(),
    val favoritePodcasts: List<Podcast> = emptyList(),
    val favoriteSounds: List<AudioTrack> = emptyList(),
    val playbackHistory: List<PlaybackHistoryItem> = emptyList(),
) {
    val userMixes: List<UserMix> get() = savedMixes
}
