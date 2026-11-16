package com.sleepsounds.storytelling.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.data.repository.ContentRepository
import com.sleepsounds.storytelling.domain.model.MixSoundState
import com.sleepsounds.storytelling.service.AudioPlayerManager
import com.sleepsounds.storytelling.service.PlaybackMode
import com.sleepsounds.storytelling.service.PlaybackState
import com.sleepsounds.storytelling.service.SleepTimerManager
import com.sleepsounds.storytelling.ui.theme.GlassOverlay
import com.sleepsounds.storytelling.ui.theme.SurfaceBlueLight
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.TextSecondary
import com.sleepsounds.storytelling.ui.theme.responsiveContentWidth
import com.sleepsounds.storytelling.ui.theme.tr
import com.sleepsounds.storytelling.util.CategoryIcons
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private val MiniPlayerContainerColor = Color(0xFF1A2442)
private val FavoriteHeartAccent = Color(0xFFFF5252)
/**
 * Floating mini-player bar docked above the bottom navigation bar when audio is active.
 */
@Composable
fun NowPlayingBar(
    audioPlayerManager: AudioPlayerManager,
    sleepTimerManager: SleepTimerManager,
    modifier: Modifier = Modifier,
    onExpand: (() -> Unit)? = null,
    barViewModel: NowPlayingBarViewModel = hiltViewModel(),
) {
    val playback by audioPlayerManager.playbackState.collectAsStateWithLifecycle()
    val timer by sleepTimerManager.state.collectAsStateWithLifecycle()
    val isFavorite by barViewModel.isCurrentFavorite.collectAsStateWithLifecycle()
    val assetResolver = rememberMediaAssetResolver()

    if (playback.mode == PlaybackMode.IDLE) return

    val displayTitle = when (playback.mode) {
        PlaybackMode.MIXER -> playback.title.ifBlank {
            stringResource(R.string.now_playing_current_mix)
        }
        else -> playback.title
    }

    val categoryKey = when (playback.mode) {
        PlaybackMode.MIXER -> playback.iconCategory ?: CategoryIcons.CUSTOM_KEY
        PlaybackMode.PODCARD -> "MEDITATE"
        else -> playback.iconCategory
    }
    val thumbUri = categoryKey?.let(assetResolver::iconUriForCategory)
        ?: assetResolver.iconUriForCategory(CategoryIcons.CUSTOM_KEY)

    val secondaryLine = when {
        timer.isActive -> {
            val countdown = sleepTimerManager.formatRemaining(timer.remainingSeconds)
            "$countdown · ${tr(playback.subtitle.ifBlank { "Sleep Aid" })}"
        }
        playback.mode == PlaybackMode.PODCARD && playback.durationMs > 0L ->
            "${(playback.durationMs / 60_000L).coerceAtLeast(1)} ${tr("MIN", "MIN", "PHÚT")} · ${tr("Meditation", "Meditação", "Thiền")}"
        else -> tr(playback.subtitle.ifBlank { "Sleep Aid" })
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .responsiveContentWidth(680.dp)
                .heightIn(min = 60.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MiniPlayerContainerColor)
                .border(1.dp, GlassOverlay, RoundedCornerShape(18.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SurfaceBlueLight)
                    .clickable {
                        if (playback.isPlaying) {
                            audioPlayerManager.pauseAll()
                        } else {
                            audioPlayerManager.resumeAll()
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (thumbUri != null) {
                    AssetImage(
                        assetUri = thumbUri,
                        contentDescription = displayTitle,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (playback.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onExpand?.invoke() },
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = TextPrimary,
                )
                Text(
                    text = secondaryLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(
                onClick = { barViewModel.toggleFavorite(playback) },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) FavoriteHeartAccent else TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }

            IconButton(
                onClick = {
                    sleepTimerManager.cancelTimer()
                    audioPlayerManager.stopAll()
                },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.now_playing_stop),
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
/**
 * ViewModel tracking favorite state for the currently playing item in the floating mini-player bar.
 */
@HiltViewModel
class NowPlayingBarViewModel @Inject constructor(
    private val audioPlayerManager: AudioPlayerManager,
    private val contentRepository: ContentRepository,
) : ViewModel() {

    val isCurrentFavorite: StateFlow<Boolean> = combine(
        audioPlayerManager.playbackState,
        contentRepository.observeSleepPresets(),
        contentRepository.observePodcasts(),
        contentRepository.observeSounds(),
        contentRepository.observeUserMixes(),
    ) { playback, scenes, podcasts, sounds, userMixes ->
        val activeId = playback.currentTrackId
        when (playback.mode) {
            PlaybackMode.PODCARD -> podcasts.any { it.id == activeId && it.isFavorite }
            PlaybackMode.MIXER -> {
                if (activeId != null) {
                    scenes.any { it.id == activeId && it.isFavorite }
                } else {
                    val activeSet = playback.activeSounds.map { it.soundId }.toSet()
                    activeSet.isNotEmpty() && userMixes.any { saved ->
                        saved.items.map { it.soundId }.toSet() == activeSet
                    }
                }
            }
            PlaybackMode.SINGLE ->
                sounds.any { it.id == activeId && it.isFavorite } ||
                    scenes.any { it.id == activeId && it.isFavorite }
            PlaybackMode.IDLE -> false
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun toggleFavorite(playback: PlaybackState) {
        viewModelScope.launch {
            val activeId = playback.currentTrackId
            when (playback.mode) {
                PlaybackMode.PODCARD -> if (activeId != null) {
                    contentRepository.togglePodcastFavorite(activeId)
                }
                PlaybackMode.MIXER -> {
                    if (activeId != null) {
                        contentRepository.toggleSceneFavorite(activeId)
                    } else if (playback.activeSounds.isNotEmpty()) {
                        val layers = playback.activeSounds.map { active ->
                            val soundTrack = contentRepository.getSoundById(active.soundId)
                            MixSoundState(
                                soundId = active.soundId,
                                name = active.name,
                                isActive = true,
                                volume = active.volume,
                                isPremium = soundTrack?.isPremium ?: false,
                                categories = soundTrack?.categories ?: emptyList(),
                                symbolPath = soundTrack?.imagePath,
                                audioPath = soundTrack?.audioPath,
                            )
                        }
                        val mixName = playback.title.ifBlank {
                            playback.activeSounds.joinToString(" + ") { it.name }
                        }
                        contentRepository.saveUserMix(mixName, layers)
                    }
                }
                PlaybackMode.SINGLE -> if (activeId != null) {
                    contentRepository.toggleSoundFavorite(activeId)
                }
                PlaybackMode.IDLE -> Unit
            }
        }
    }
}
