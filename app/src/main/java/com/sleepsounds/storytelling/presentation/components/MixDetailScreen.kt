package com.sleepsounds.storytelling.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.ads.AdaptiveBannerAdBar
import com.sleepsounds.storytelling.presentation.scene.MixSoundDisplay
import com.sleepsounds.storytelling.service.SleepTimerManager
import com.sleepsounds.storytelling.ui.theme.GlassOverlay
import com.sleepsounds.storytelling.ui.theme.MidnightBlue
import com.sleepsounds.storytelling.ui.theme.PastelPurple
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.TextSecondary
import com.sleepsounds.storytelling.ui.theme.rememberResponsiveLayoutInfo
import com.sleepsounds.storytelling.ui.theme.responsiveContentWidth
import com.sleepsounds.storytelling.ui.theme.statusAndCutoutPadding
import com.sleepsounds.storytelling.ui.theme.tr

private val SceneOverlayScrim = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0E1733).copy(alpha = 0.88f),
        Color(0xFF0B1228).copy(alpha = 0.96f),
        Color(0xFF090E20),
    ),
)
private val LayerSliderActiveBlue = Color(0xFF2196F3)
private val LayerIconBadgeBlue = Color(0xFF64B5F6)
private val TransportCircleNavy = Color(0xFF15203E)
private val FavoriteHeartCoral = Color(0xFFFF5252)
fun SleepTimerManager.formatTimerLabel(remainingSeconds: Int): String =
    formatRemaining(remainingSeconds)
@Composable
fun MixSoundRow(
    sound: MixSoundDisplay,
    modifier: Modifier = Modifier,
) {
    MixSoundRowCompact(sound = sound, modifier = modifier)
}
@Composable
fun MixSoundRowCompact(
    sound: MixSoundDisplay,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GlassOverlay.copy(alpha = 0.32f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (sound.iconUri != null) {
            AssetImage(
                assetUri = sound.iconUri,
                contentDescription = sound.name,
                modifier = Modifier.size(36.dp),
                contentScale = ContentScale.Fit,
            )
        } else {
            Text(text = "🎵", style = MaterialTheme.typography.bodyLarge)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = sound.name,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            LinearProgressIndicator(
                progress = { sound.volume.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = PastelPurple,
                trackColor = TextPrimary.copy(alpha = 0.12f),
            )
        }
        Text(
            text = "${(sound.volume * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
        )
    }
}
@Composable
private fun MixBottomTransportBar(
    isPlaying: Boolean,
    defaultTimerText: String,
    remainingSeconds: Int?,
    totalSeconds: Int?,
    isSavedOrLiked: Boolean,
    canSaveMix: Boolean,
    canEditMix: Boolean,
    onOpenTimer: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSecondaryAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timerRunning = remainingSeconds != null && remainingSeconds > 0
    val formattedTimer = if (timerRunning) {
        val mins = (remainingSeconds ?: 0) / 60
        val secs = remainingSeconds % 60
        "%02d:%02d".format(mins, secs)
    } else {
        defaultTimerText
    }
    val ringFraction = if (timerRunning && totalSeconds != null && totalSeconds > 0) {
        (remainingSeconds ?: 0).toFloat() / totalSeconds.toFloat()
    } else {
        0f
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(TransportCircleNavy)
                    .border(1.5.dp, LayerSliderActiveBlue.copy(alpha = 0.45f), CircleShape)
                    .clickable(onClick = onOpenTimer),
                contentAlignment = Alignment.Center,
            ) {
                if (ringFraction > 0f) {
                    Canvas(modifier = Modifier.size(56.dp)) {
                        drawArc(
                            color = LayerSliderActiveBlue,
                            startAngle = -90f,
                            sweepAngle = 360f * ringFraction,
                            useCenter = false,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = formattedTimer,
                    tint = LayerSliderActiveBlue,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = formattedTimer,
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(TransportCircleNavy)
                    .border(1.5.dp, GlassOverlay, CircleShape)
                    .clickable(onClick = onTogglePlayPause),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = stringResource(
                        if (isPlaying) R.string.now_playing_pause else R.string.now_playing_play,
                    ),
                    tint = Color.White,
                    modifier = Modifier.size(32.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (canSaveMix || canEditMix) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(TransportCircleNavy)
                        .border(
                            1.5.dp,
                            if (canSaveMix && isSavedOrLiked) {
                                FavoriteHeartCoral.copy(alpha = 0.65f)
                            } else {
                                GlassOverlay
                            },
                            CircleShape,
                        )
                        .clickable(onClick = onSecondaryAction),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = when {
                            canSaveMix && isSavedOrLiked -> Icons.Default.Favorite
                            canSaveMix -> Icons.Default.FavoriteBorder
                            else -> Icons.Default.Edit
                        },
                        contentDescription = stringResource(R.string.mixer_save_mix),
                        tint = if (canSaveMix && isSavedOrLiked) FavoriteHeartCoral else Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Text(
                    text = when {
                        canSaveMix && isSavedOrLiked -> tr("Saved", "Salvo", "Đã lưu")
                        canSaveMix -> tr("Mix Save", "Salvar Mix", "Lưu phối âm")
                        else -> stringResource(R.string.mixer_edit)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (canSaveMix && isSavedOrLiked) FavoriteHeartCoral else TextPrimary,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
@Composable
private fun MixLayerVolumeRow(
    layer: MixSoundDisplay,
    onChangeVolume: (Float) -> Unit,
    onDeleteLayer: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(LayerIconBadgeBlue.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center,
        ) {
            if (layer.iconUri != null) {
                AssetImage(
                    assetUri = layer.iconUri,
                    contentDescription = layer.name,
                    modifier = Modifier.size(26.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MidnightBlue,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = layer.name,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Slider(
                value = layer.volume.coerceIn(0f, 1f),
                onValueChange = onChangeVolume,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = LayerSliderActiveBlue,
                    inactiveTrackColor = Color.White.copy(alpha = 0.22f),
                ),
            )
        }

        if (onDeleteLayer != null) {
            IconButton(
                onClick = onDeleteLayer,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = stringResource(R.string.mixer_delete_mix),
                    tint = TextSecondary,
                    modifier = Modifier.size(22.dp),
                )
            }
        } else {
            Text(
                text = "${(layer.volume * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
        }
    }
}
@Composable
private fun MixDetailTopHeader(
    title: String,
    isFavorite: Boolean,
    onNavigateBack: () -> Unit,
    onFavoritePress: (() -> Unit)?,
    onEditPress: (() -> Unit)?,
    onResetPress: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.action_back),
                tint = TextPrimary,
                modifier = Modifier.size(28.dp),
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (onFavoritePress != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onFavoritePress,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) FavoriteHeartCoral else TextPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            if (onEditPress != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onEditPress,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.mixer_edit),
                        tint = TextPrimary,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
            if (onResetPress != null) {
                Text(
                    text = stringResource(R.string.mixer_reset),
                    style = MaterialTheme.typography.titleSmall,
                    color = LayerSliderActiveBlue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onResetPress)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
    }
}
/**
 * Full-screen mixer and sleep scene player displaying active sound layers, volume sliders, and timer controls.
 */
@Composable
fun MixDetailScreen(
    title: String,
    backgroundUri: String,
    mixSounds: List<MixSoundDisplay>,
    isPlaying: Boolean,
    timerLabel: String?,
    timerRemainingSeconds: Int?,
    timerTotalSeconds: Int?,
    actions: MixDetailActions,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    showSaveMix: Boolean = true,
    showEdit: Boolean = true,
    mixSavedMessage: Boolean = false,
    belowHeroContent: (@Composable () -> Unit)? = null,
) {
    val layoutMetrics = rememberResponsiveLayoutInfo()
    val isHeartHighlighted = isFavorite || mixSavedMessage

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightBlue),
    ) {
        if (backgroundUri.isNotBlank()) {
            AssetImage(
                assetUri = backgroundUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SceneOverlayScrim),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusAndCutoutPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val favoriteCallback = actions.onToggleFavorite ?: actions.onSaveMix
            MixDetailTopHeader(
                title = title,
                isFavorite = isHeartHighlighted,
                onNavigateBack = actions.onBack,
                onFavoritePress = favoriteCallback,
                onEditPress = if (showEdit) actions.onEditMix else null,
                onResetPress = actions.onClearAll,
            )

            if (mixSavedMessage || isFavorite) {
                Text(
                    text = stringResource(R.string.mixer_saved_to_library),
                    style = MaterialTheme.typography.bodySmall,
                    color = LayerIconBadgeBlue,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                )
            }

            if (belowHeroContent != null) {
                AdaptiveBannerAdBar(slotKey = "mix_detail_banner")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .responsiveContentWidth(680.dp)
                        .padding(horizontal = layoutMetrics.screenHorizontalPadding),
                ) {
                    belowHeroContent()
                }
            } else if (mixSounds.isEmpty()) {
                AdaptiveBannerAdBar(slotKey = "mix_detail_banner")
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.mixer_empty_selection),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .responsiveContentWidth(680.dp),
                    contentPadding = PaddingValues(
                        horizontal = layoutMetrics.screenHorizontalPadding,
                        vertical = 12.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    val firstTwoRows = mixSounds.take(2)
                    val remainingRows = mixSounds.drop(2)

                    items(firstTwoRows, key = { it.soundId }) { layer ->
                        MixLayerVolumeRow(
                            layer = layer,
                            onChangeVolume = { updatedVol ->
                                actions.onVolumeChange?.invoke(layer.soundId, updatedVol)
                            },
                            onDeleteLayer = if (actions.onRemoveSound != null) {
                                { actions.onRemoveSound.invoke(layer.soundId) }
                            } else {
                                null
                            },
                        )
                    }

                    item(key = "mix_detail_banner_row_2_3") {
                        AdaptiveBannerAdBar(slotKey = "mix_detail_banner")
                    }

                    items(remainingRows, key = { it.soundId }) { layer ->
                        MixLayerVolumeRow(
                            layer = layer,
                            onChangeVolume = { updatedVol ->
                                actions.onVolumeChange?.invoke(layer.soundId, updatedVol)
                            },
                            onDeleteLayer = if (actions.onRemoveSound != null) {
                                { actions.onRemoveSound.invoke(layer.soundId) }
                            } else {
                                null
                            },
                        )
                    }
                }
            }

            MixBottomTransportBar(
                isPlaying = isPlaying,
                defaultTimerText = timerLabel ?: stringResource(R.string.tool_timer),
                remainingSeconds = timerRemainingSeconds,
                totalSeconds = timerTotalSeconds,
                isSavedOrLiked = isHeartHighlighted,
                canSaveMix = showSaveMix && (actions.onSaveMix != null || actions.onToggleFavorite != null),
                canEditMix = showEdit,
                onOpenTimer = actions.onTimerClick,
                onTogglePlayPause = actions.onPlayPause,
                onSecondaryAction = {
                    when {
                        actions.onSaveMix != null -> actions.onSaveMix.invoke()
                        actions.onToggleFavorite != null -> actions.onToggleFavorite.invoke()
                        else -> actions.onEditMix()
                    }
                },
                modifier = Modifier
                    .responsiveContentWidth(600.dp)
                    .padding(
                        horizontal = layoutMetrics.screenHorizontalPadding,
                        vertical = if (layoutMetrics.isCompactHeight) 14.dp else 20.dp,
                    ),
            )
        }
    }
}
/**
 * Callback bundle for the soundscape / mix detail transport and layer sliders.
 */
data class MixDetailActions(
    val onBack: () -> Unit,
    val onTimerClick: () -> Unit,
    val onEditMix: () -> Unit,
    val onPlayPause: () -> Unit,
    val onSaveMix: (() -> Unit)? = null,
    val onToggleFavorite: (() -> Unit)? = null,
    val onClearAll: (() -> Unit)? = null,
    val onVolumeChange: ((soundId: String, volume: Float) -> Unit)? = null,
    val onRemoveSound: ((soundId: String) -> Unit)? = null,
)
