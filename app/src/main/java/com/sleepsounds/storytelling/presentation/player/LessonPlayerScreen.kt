package com.sleepsounds.storytelling.presentation.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.ads.AdaptiveBannerAdBar
import com.sleepsounds.storytelling.domain.model.Podcast
import com.sleepsounds.storytelling.service.PlaybackState
import com.sleepsounds.storytelling.ui.theme.MidnightBlue
import com.sleepsounds.storytelling.ui.theme.PastelPurple
import com.sleepsounds.storytelling.ui.theme.DeepSleepBlue
import com.sleepsounds.storytelling.ui.theme.SurfaceBlue
import com.sleepsounds.storytelling.ui.theme.SurfaceBlueLight
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.TextSecondary
import com.sleepsounds.storytelling.ui.theme.rememberResponsiveLayoutInfo
import com.sleepsounds.storytelling.ui.theme.responsiveContentWidth
import com.sleepsounds.storytelling.ui.theme.statusAndCutoutPadding
import com.sleepsounds.storytelling.ui.theme.tr

private val StoryPlayerBackdrop = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0E1630),
        Color(0xFF0A1024),
        Color(0xFF070B19),
    ),
)
private val SeekBarBlue = Color(0xFF1E88E5)
private val FavoriteHeartRed = Color(0xFFFF5252)
private val storyAmbientBackgroundOptions = listOf(
    null to "Off (Voice Only)",
    "light_rain" to "🌧️ Light Rain",
    "calm_sea" to "🌊 Calm Sea",
    "fire" to "🔥 Campfire",
    "rain_on_roof" to "🏠 Rain on Roof",
    "white_noise" to "✨ Soft White Noise",
)
internal fun formatPlaybackTime(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSec = (ms / 1000L).toInt()
    val hrs = totalSec / 3600
    val mins = (totalSec % 3600) / 60
    val secs = totalSec % 60
    return if (hrs > 0) {
        "%d:%02d:%02d".format(hrs, mins, secs)
    } else {
        "%d:%02d".format(mins, secs)
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AmbientAndStoryQueueSheet(
    activePodcastId: String?,
    selectedAmbientSoundId: String?,
    ambientVolume: Float,
    storyQueue: List<Podcast>,
    onChooseAmbientSound: (String?) -> Unit,
    onAmbientVolumeChanged: (Float) -> Unit,
    onChoosePodcast: (Podcast) -> Unit,
    onCloseSheet: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(onClick = onCloseSheet),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .responsiveContentWidth(600.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MidnightBlue)
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.08f),
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                )
                .clickable(enabled = false) {}
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = DeepSleepBlue,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr(
                            "Background Music & Stories",
                            "Música de Fundo & Histórias",
                            "Nhạc Nền & Truyện Ngủ",
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                IconButton(onClick = onCloseSheet) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tr(
                    "Ambient Background Sound",
                    "Som Ambiente de Fundo",
                    "Âm thanh nền dịu nhẹ",
                ),
                color = PastelPurple,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                storyAmbientBackgroundOptions.forEach { (soundKey, displayLabel) ->
                    val isSelected = selectedAmbientSoundId == soundKey
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) DeepSleepBlue else SurfaceBlueLight)
                            .clickable { onChooseAmbientSound(soundKey) }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                    ) {
                        Text(
                            text = tr(displayLabel),
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
            }

            if (!selectedAmbientSoundId.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = tr("Background Volume", "Volume do Fundo", "Âm lượng nhạc nền"),
                        color = TextSecondary,
                        fontSize = 12.5.sp,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${(ambientVolume * 100).toInt()}%",
                        color = TextPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Slider(
                    value = ambientVolume.coerceIn(0.05f, 1f),
                    onValueChange = onAmbientVolumeChanged,
                    colors = SliderDefaults.colors(
                        thumbColor = DeepSleepBlue,
                        activeTrackColor = DeepSleepBlue,
                        inactiveTrackColor = SurfaceBlueLight,
                    ),
                )
            }

            if (storyQueue.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${tr("Switch Bedtime Story", "Trocar História", "Đổi truyện ngủ")} (${storyQueue.size})",
                    color = PastelPurple,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    storyQueue.forEach { storyItem ->
                        val isPlayingNow = storyItem.id == activePodcastId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isPlayingNow) {
                                        DeepSleepBlue.copy(alpha = 0.22f)
                                    } else {
                                        SurfaceBlue.copy(alpha = 0.85f)
                                    },
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isPlayingNow) {
                                        DeepSleepBlue
                                    } else {
                                        Color.White.copy(alpha = 0.06f)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                )
                                .clickable { onChoosePodcast(storyItem) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = if (isPlayingNow) {
                                    Icons.Filled.GraphicEq
                                } else {
                                    Icons.Filled.PlayArrow
                                },
                                contentDescription = null,
                                tint = if (isPlayingNow) DeepSleepBlue else TextSecondary,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = storyItem.title,
                                color = if (isPlayingNow) Color.White else TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = if (isPlayingNow) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun LessonTransportControls(
    isPlaying: Boolean,
    timerLabel: String,
    hasAmbientSound: Boolean,
    onOpenTimer: () -> Unit,
    onSkipPrevious: () -> Unit,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    onOpenQueueSheet: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onOpenTimer)
                .padding(horizontal = 6.dp, vertical = 4.dp),
        ) {
            Text(
                text = timerLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = timerLabel,
                tint = TextPrimary,
                modifier = Modifier.size(24.dp),
            )
        }

        IconButton(onClick = onSkipPrevious) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = stringResource(R.string.podcard_previous_lesson),
                tint = TextPrimary,
                modifier = Modifier.size(32.dp),
            )
        }

        IconButton(
            onClick = onTogglePlay,
            modifier = Modifier.size(60.dp),
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = stringResource(
                    if (isPlaying) R.string.now_playing_pause else R.string.now_playing_play,
                ),
                tint = Color.White,
                modifier = Modifier.size(42.dp),
            )
        }

        IconButton(onClick = onSkipNext) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = stringResource(R.string.podcard_next_lesson),
                tint = TextPrimary,
                modifier = Modifier.size(32.dp),
            )
        }

        Box(contentAlignment = Alignment.TopEnd) {
            IconButton(onClick = onOpenQueueSheet) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = "Background Music & Story Queue",
                    tint = if (hasAmbientSound) DeepSleepBlue else TextPrimary,
                    modifier = Modifier.size(28.dp),
                )
            }
            if (hasAmbientSound) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, end = 6.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(DeepSleepBlue),
                )
            }
        }
    }
}
@Composable
private fun LessonPlayerHeaderBar(
    headerTitle: String,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.action_back),
                tint = TextPrimary,
                modifier = Modifier.size(30.dp),
            )
        }

        Text(
            text = headerTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
        )

        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = if (isFavorite) FavoriteHeartRed else TextPrimary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
/**
 * Full-screen audio player for bedtime stories, guided meditations, and single-track lessons.
 */
@Composable
fun LessonPlayerScreen(
    seriesTitle: String,
    playback: PlaybackState,
    timerLabel: String,
    onBack: () -> Unit,
    onTimerClick: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (positionMs: Long) -> Unit,
    onPreviousLesson: () -> Unit,
    onNextLesson: () -> Unit,
    modifier: Modifier = Modifier,
    categoryLabel: String = "Meditation",
    isFavorite: Boolean = false,
    audioPodcasts: List<Podcast> = emptyList(),
    onToggleFavorite: () -> Unit = {},
    onSelectBackgroundSound: (String?) -> Unit = {},
    onChangeBackgroundVolume: (Float) -> Unit = {},
    onSelectPodcastFromQueue: (Podcast) -> Unit = {},
    onStartTracker: (() -> Unit)? = null,
) {
    val layoutMetrics = rememberResponsiveLayoutInfo()
    val compactHeight = layoutMetrics.isCompactHeight
    var queueSheetVisible by remember { mutableStateOf(false) }

    BackHandler(enabled = queueSheetVisible) {
        queueSheetVisible = false
    }

    var isScrubbing by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    val playbackFraction = if (playback.durationMs > 0L) {
        (playback.positionMs.toFloat() / playback.durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val sliderFraction = if (isScrubbing) dragFraction else playbackFraction
    val shownPositionMs = if (isScrubbing && playback.durationMs > 0L) {
        (dragFraction * playback.durationMs).toLong()
    } else {
        playback.positionMs
    }

    val totalMinutes = ((playback.durationMs / 60_000L).toInt()).coerceAtLeast(1)
    val durationAndCategoryLine = if (playback.durationMs > 0L) {
        "$totalMinutes MIN · $categoryLabel"
    } else {
        categoryLabel
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StoryPlayerBackdrop),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusAndCutoutPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LessonPlayerHeaderBar(
                headerTitle = categoryLabel,
                isFavorite = isFavorite,
                onDismiss = onBack,
                onFavoriteClick = onToggleFavorite,
            )

            AdaptiveBannerAdBar(slotKey = "lesson_player_banner")

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .responsiveContentWidth(600.dp)
                    .padding(horizontal = if (layoutMetrics.isUltraCompactWidth) 20.dp else 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Spacer(modifier = Modifier.height(if (compactHeight) 8.dp else 16.dp))

                Box(
                    modifier = Modifier
                        .size(if (compactHeight) 64.dp else 76.dp)
                        .clip(CircleShape)
                        .background(SurfaceBlueLight),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.GraphicEq,
                        contentDescription = null,
                        tint = DeepSleepBlue,
                        modifier = Modifier.size(if (compactHeight) 32.dp else 38.dp),
                    )
                }

                Spacer(modifier = Modifier.height(if (compactHeight) 16.dp else 26.dp))

                Text(
                    text = seriesTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = if (compactHeight) 20.sp else 23.sp,
                    ),
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = durationAndCategoryLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(SeekBarBlue)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "${formatPlaybackTime(shownPositionMs)}/${formatPlaybackTime(playback.durationMs)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }

                    Slider(
                        value = sliderFraction,
                        onValueChange = { updated ->
                            isScrubbing = true
                            dragFraction = updated
                        },
                        onValueChangeFinished = {
                            if (playback.durationMs > 0L) {
                                onSeek((dragFraction * playback.durationMs).toLong())
                            }
                            isScrubbing = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = SeekBarBlue,
                            activeTrackColor = SeekBarBlue,
                            inactiveTrackColor = Color.White.copy(alpha = 0.16f),
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(if (compactHeight) 8.dp else 14.dp))

                LessonTransportControls(
                    isPlaying = playback.isPlaying,
                    timerLabel = timerLabel,
                    hasAmbientSound = !playback.backgroundSoundId.isNullOrBlank(),
                    onOpenTimer = onTimerClick,
                    onSkipPrevious = onPreviousLesson,
                    onTogglePlay = onPlayPause,
                    onSkipNext = onNextLesson,
                    onOpenQueueSheet = { queueSheetVisible = true },
                )

                Spacer(modifier = Modifier.height(if (compactHeight) 16.dp else 26.dp))

                if (onStartTracker != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(SeekBarBlue)
                            .clickable(onClick = onStartTracker),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.tracker_start),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        AnimatedVisibility(
            visible = queueSheetVisible,
            enter = fadeIn() + slideInVertically { it / 3 },
            exit = fadeOut() + slideOutVertically { it / 3 },
        ) {
            AmbientAndStoryQueueSheet(
                activePodcastId = playback.currentTrackId,
                selectedAmbientSoundId = playback.backgroundSoundId,
                ambientVolume = playback.backgroundVolume,
                storyQueue = audioPodcasts,
                onChooseAmbientSound = onSelectBackgroundSound,
                onAmbientVolumeChanged = onChangeBackgroundVolume,
                onChoosePodcast = { chosen ->
                    onSelectPodcastFromQueue(chosen)
                    queueSheetVisible = false
                },
                onCloseSheet = { queueSheetVisible = false },
            )
        }
    }
}
