package com.sleepsounds.storytelling.presentation.mixer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.ads.AdaptiveBannerAdBar
import com.sleepsounds.storytelling.ads.RewardedUnlockBottomSheet
import com.sleepsounds.storytelling.domain.model.MixSoundState
import com.sleepsounds.storytelling.presentation.components.ScreenBackground
import com.sleepsounds.storytelling.presentation.components.SoundSymbolIcon
import com.sleepsounds.storytelling.presentation.sounds.SoundGridButton
import com.sleepsounds.storytelling.ui.theme.ContentSafeArea
import com.sleepsounds.storytelling.ui.theme.DeepSleepBlue
import com.sleepsounds.storytelling.ui.theme.Dimens
import com.sleepsounds.storytelling.ui.theme.MidnightBlue
import com.sleepsounds.storytelling.ui.theme.SurfaceBlue
import com.sleepsounds.storytelling.ui.theme.SurfaceBlueLight
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.TextSecondary
import com.sleepsounds.storytelling.ui.theme.rememberResponsiveLayoutInfo
import com.sleepsounds.storytelling.ui.theme.responsiveContentWidth
import com.sleepsounds.storytelling.ui.theme.statusAndCutoutPadding

@Composable
private fun MixerBottomActionToolbar(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    onReset: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MidnightBlue.copy(alpha = 0.96f))
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .responsiveContentWidth(540.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onCancel,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SurfaceBlueLight),
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.action_cancel),
                tint = TextSecondary,
            )
        }

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(DeepSleepBlue)
                .clickable(onClick = onConfirm),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.action_done),
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }

        IconButton(
            onClick = onReset,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SurfaceBlueLight),
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = stringResource(R.string.mixer_reset),
                tint = TextSecondary,
            )
        }
    }
}
@Composable
private fun ActiveMixerLayerTile(
    sound: MixSoundState,
    onVolumeChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .width(106.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceBlue.copy(alpha = 0.9f))
            .border(1.dp, DeepSleepBlue.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(DeepSleepBlue.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center,
        ) {
            SoundSymbolIcon(
                soundId = sound.soundId,
                symbolPath = sound.symbolPath,
                contentDescription = sound.name,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = sound.name,
            color = TextPrimary,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Slider(
            value = sound.volume,
            onValueChange = onVolumeChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = DeepSleepBlue,
                inactiveTrackColor = SurfaceBlueLight,
            ),
            modifier = Modifier.height(28.dp),
        )
    }
}
@Composable
fun MixerScreen(
    onBack: () -> Unit,
    onOpenPaywall: () -> Unit = {},
    viewModel: MixerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val layoutInfo = rememberResponsiveLayoutInfo()
    val gridColumns = layoutInfo.soundGridColumns
    val activeSounds = remember(state.sounds) { state.sounds.filter { it.isActive } }
    var pendingUnlockSound by remember { mutableStateOf<MixSoundState?>(null) }

    ScreenBackground(contentSafeArea = ContentSafeArea.None) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                MixerBottomActionToolbar(
                    onCancel = { viewModel.cancelEdit(onBack) },
                    onConfirm = { viewModel.confirmEdit(onBack) },
                    onReset = { viewModel.resetEdit() },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusAndCutoutPadding()
                    .padding(padding),
            ) {
                Text(
                    text = stringResource(R.string.mixer_current_selection),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(
                        horizontal = layoutInfo.screenHorizontalPadding,
                        vertical = 10.dp,
                    ),
                )

                if (activeSounds.isEmpty()) {
                    Text(
                        text = stringResource(R.string.mixer_empty_selection),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(
                            horizontal = layoutInfo.screenHorizontalPadding,
                            vertical = 6.dp,
                        ),
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = layoutInfo.screenHorizontalPadding),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(activeSounds, key = { it.soundId }) { sound ->
                            ActiveMixerLayerTile(
                                sound = sound,
                                onVolumeChange = { viewModel.setVolume(sound.soundId, it) },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.section_all_sounds),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(
                        horizontal = layoutInfo.screenHorizontalPadding,
                        vertical = 6.dp,
                    ),
                )

                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    val horizontalPadding = layoutInfo.screenHorizontalPadding
                    val gridSpacing = Dimens.gridSpacing
                    val availableWidth = maxWidth - horizontalPadding * 2
                    val cellWidth = (availableWidth - gridSpacing * (gridColumns - 1)) / gridColumns
                    val circleSize = min(cellWidth * 0.78f, 64.dp).coerceAtLeast(52.dp)
                    val firstTwoRowsCount = gridColumns * 2
                    val firstTwoRowsSounds = remember(state.sounds, firstTwoRowsCount) {
                        state.sounds.take(firstTwoRowsCount)
                    }
                    val remainingSounds = remember(state.sounds, firstTwoRowsCount) {
                        state.sounds.drop(firstTwoRowsCount)
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumns),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = horizontalPadding,
                            end = horizontalPadding,
                            top = 10.dp,
                            bottom = 24.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        items(
                            items = firstTwoRowsSounds,
                            key = { it.soundId },
                        ) { sound ->
                            SoundGridButton(
                                sound = sound,
                                circleSize = circleSize,
                                onClick = {
                                    if (sound.isPremium && !sound.isActive) {
                                        pendingUnlockSound = sound
                                    } else {
                                        viewModel.adMobManager.onUserAction {
                                            viewModel.toggleSound(sound)
                                        }
                                    }
                                },
                            )
                        }

                        item(
                            key = "mixer_banner_row_2_3",
                            span = { GridItemSpan(maxLineSpan) },
                        ) {
                            AdaptiveBannerAdBar(
                                adMobManager = viewModel.adMobManager,
                                slotKey = "mixer_screen_banner",
                            )
                        }

                        items(
                            items = remainingSounds,
                            key = { it.soundId },
                        ) { sound ->
                            SoundGridButton(
                                sound = sound,
                                circleSize = circleSize,
                                onClick = {
                                    if (sound.isPremium && !sound.isActive) {
                                        pendingUnlockSound = sound
                                    } else {
                                        viewModel.adMobManager.onUserAction {
                                            viewModel.toggleSound(sound)
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    val soundToUnlock = pendingUnlockSound
    if (soundToUnlock != null) {
        RewardedUnlockBottomSheet(
            itemId = soundToUnlock.soundId,
            itemTitle = soundToUnlock.name,
            adMobManager = viewModel.adMobManager,
            onUnlocked = {
                val unlockedSound = soundToUnlock.copy(isPremium = false)
                pendingUnlockSound = null
                viewModel.toggleSound(unlockedSound)
            },
            onOpenPaywall = onOpenPaywall,
            onDismiss = { pendingUnlockSound = null },
        )
    }
}
