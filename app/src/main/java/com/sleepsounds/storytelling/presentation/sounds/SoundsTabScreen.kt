package com.sleepsounds.storytelling.presentation.sounds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.data.remote.ContentCategory
import com.sleepsounds.storytelling.domain.model.HistoryItemType
import com.sleepsounds.storytelling.domain.model.MixSoundState
import com.sleepsounds.storytelling.domain.model.PlaybackHistoryItem
import com.sleepsounds.storytelling.domain.model.Podcast
import com.sleepsounds.storytelling.domain.model.SleepPreset
import com.sleepsounds.storytelling.domain.model.UserMix
import com.sleepsounds.storytelling.presentation.components.AssetImage
import com.sleepsounds.storytelling.presentation.components.ScreenBackground
import com.sleepsounds.storytelling.presentation.components.SleepTimerBottomSheet
import com.sleepsounds.storytelling.presentation.components.SoundSymbolIcon
import com.sleepsounds.storytelling.presentation.home.HomeViewModel
import com.sleepsounds.storytelling.presentation.library.LibraryUiState
import com.sleepsounds.storytelling.presentation.library.LibraryViewModel
import com.sleepsounds.storytelling.presentation.mixer.MixerViewModel
import com.sleepsounds.storytelling.presentation.podcard.PodcardGridCard
import com.sleepsounds.storytelling.presentation.podcard.PodcardViewModel
import com.sleepsounds.storytelling.service.PlaybackMode
import com.sleepsounds.storytelling.service.SleepTimerManager
import com.sleepsounds.storytelling.ui.theme.Dimens
import com.sleepsounds.storytelling.ui.theme.MidnightBlue
import com.sleepsounds.storytelling.ads.AdaptiveBannerAdBar
import com.sleepsounds.storytelling.ads.RewardedUnlockBottomSheet
import com.sleepsounds.storytelling.ui.theme.PastelPurple
import com.sleepsounds.storytelling.ui.theme.PremiumGold
import com.sleepsounds.storytelling.ui.theme.DeepSleepBlue
import com.sleepsounds.storytelling.ui.theme.DeepSleepNewBadgeRed
import com.sleepsounds.storytelling.ui.theme.DeepSleepAccentGold
import com.sleepsounds.storytelling.ui.theme.SurfaceBlue
import com.sleepsounds.storytelling.ui.theme.SurfaceBlueLight
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.TextSecondary
import com.sleepsounds.storytelling.ui.theme.rememberResponsiveLayoutInfo
import com.sleepsounds.storytelling.ui.theme.tr

private val DeepSleepHeartRed = Color(0xFFFF4D6D)
private val curatedAmbientSoundOrder = listOf(
    "light_rain",
    "soft_fan",
    "fire",
    "water_wave",
    "rain_on_leaves",
    "bamboo_flute",
    "clock",
    "crickets",
    "rain_on_tent",
    "seagull",
    "rain_on_roof",
    "cold_wind",
    "white_noise",
    "brown_noise",
    "pink_noise",
    "heavy_rain",
    "thunder",
    "rain_on_windows",
    "soft_guitar",
    "air_conditioner",
    "waterfall",
    "perfect_rain",
    "soft_piano",
    "rain_forest",
    "morning_rain",
    "calm_sea",
    "campfire_glow",
    "river",
    "creek",
    "under_water",
    "cave",
    "cave_drops",
    "leaves",
    "water_drop",
    "soft_water",
    "heaven_water",
    "night_lake",
    "bird_forest",
    "birds_singing",
    "cat_purring",
    "frogs",
    "owl",
    "whale",
    "wolves",
    "loons",
    "city_rush",
    "street_life",
    "train_journey",
    "airplane",
    "spaceship",
    "wind_chime",
    "kalimba",
    "crystal_bowls",
    "meditation_bowl",
    "zen_bowls",
    "bell",
    "evening_guitar",
    "warm_guitar",
    "midnight_piano",
    "green_noise",
    "432hz",
    "528hz",
    "binaural_alpha",
    "binaural_delta",
    "binaural_theta",
)
private val ambientSoundFilterChips = listOf(
    "all" to "All",
    "mixes" to "Mixes",
    "rain" to "Rain",
    "asmr" to "ASMR",
    "nature" to "Nature",
    "animal" to "Animal",
    "city" to "City",
    "fan" to "Fan",
    "melody" to "Melody",
    "baby" to "Baby",
    "noise" to "Noise",
    "special" to "Special",
)
@Composable
fun SoundGridButton(
    sound: MixSoundState,
    circleSize: Dp,
    onClick: () -> Unit,
) {
    val activeFill = Color(0xFF3E7BFA)
    val inactiveFill = Color(0xFF1B243E)
    val iconSize = (circleSize * 0.44f).coerceIn(22.dp, 32.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember(sound.soundId) { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        Box(
            modifier = Modifier.size(circleSize),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(if (sound.isActive) activeFill else inactiveFill)
                    .border(
                        width = if (sound.isActive) 2.dp else 1.dp,
                        color = if (sound.isActive) {
                            Color.White.copy(alpha = 0.85f)
                        } else {
                            Color.White.copy(alpha = 0.08f)
                        },
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                SoundSymbolIcon(
                    soundId = sound.soundId,
                    symbolPath = sound.symbolPath,
                    contentDescription = sound.name,
                    tint = if (sound.isActive) {
                        Color.White
                    } else {
                        TextPrimary.copy(alpha = 0.86f)
                    },
                    modifier = Modifier.size(iconSize),
                )
            }
            if (sound.isPremium && !sound.isActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(PremiumGold),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "VIP",
                        tint = MidnightBlue,
                        modifier = Modifier.size(11.dp),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = sound.name,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
            color = if (sound.isActive) TextPrimary else TextSecondary,
            fontWeight = if (sound.isActive) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 14.sp,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
@Composable
private fun SoundsCategoryTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) TextPrimary else TextSecondary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .then(if (selected) Modifier.fillMaxWidth() else Modifier)
                .background(
                    if (selected) TextPrimary else Color.Transparent,
                    RoundedCornerShape(1.dp),
                ),
        )
    }
}
@Composable
fun SoundsCategoryTabRow(
    categories: List<ContentCategory>,
    selectedKey: String?,
    onCategorySelected: (String?) -> Unit,
    horizontalPadding: Dp = Dimens.screenPadding,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(22.dp),
        contentPadding = PaddingValues(horizontal = horizontalPadding),
    ) {
        item {
            SoundsCategoryTab(
                label = stringResource(R.string.filter_all),
                selected = selectedKey == null,
                onClick = { onCategorySelected(null) },
            )
        }
        items(categories, key = { it.id }) { category ->
            SoundsCategoryTab(
                label = tr(category.label),
                selected = selectedKey == category.id,
                onClick = { onCategorySelected(category.id) },
            )
        }
    }
}
@Composable
private fun FloatingActiveMixDock(
    activeSounds: List<MixSoundState>,
    mixTitle: String,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    onOpenMixDetail: () -> Unit,
    onStopAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeSubtitle = remember(activeSounds, mixTitle) {
        activeSounds.joinToString(", ") { it.name }.ifBlank { mixTitle }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1A2340).copy(alpha = 0.98f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .clickable(onClick = onOpenMixDetail)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF28355E))
                .clickable(onClick = onTogglePlayPause),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tr("Mix", "Mix", "Bản phối"),
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                text = activeSubtitle,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Box(contentAlignment = Alignment.TopEnd) {
            IconButton(onClick = onOpenMixDetail) {
                Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = "Mix Settings",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
            if (activeSounds.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp, end = 4.dp)
                        .size(17.dp)
                        .clip(CircleShape)
                        .background(DeepSleepHeartRed),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = activeSounds.size.toString(),
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }

        IconButton(onClick = onStopAll) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close Mix",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
@Composable
private fun SelectableCategoryPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) DeepSleepBlue else SurfaceBlueLight.copy(alpha = 0.78f),
            )
            .border(
                width = 1.dp,
                color = if (selected) DeepSleepBlue else Color.White.copy(alpha = 0.07f),
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
        )
    }
}
@Composable
private fun SquareSceneArtworkCard(
    scene: SleepPreset,
    badgeText: String? = null,
    badgeColor: Color = DeepSleepBlue,
    onToggleFavorite: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceBlueLight),
        ) {
            scene.imagePath?.let { path ->
                AssetImage(
                    assetUri = path,
                    contentDescription = scene.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            val effectiveBadgeText = if (scene.isPremium) "VIP" else badgeText
            val effectiveBadgeColor = if (scene.isPremium) PremiumGold else badgeColor
            if (!effectiveBadgeText.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(effectiveBadgeColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = effectiveBadgeText,
                        color = if (scene.isPremium) MidnightBlue else Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }

            if (onToggleFavorite != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable(onClick = onToggleFavorite),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (scene.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (scene.isFavorite) DeepSleepHeartRed else Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = scene.name,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = scene.items.joinToString(" · ") { it.name }.ifBlank { "Soundscape" },
            color = TextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
@Composable
private fun CatalogEmptyPlaceholder(
    title: String,
    subtitle: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            DeepSleepBlue.copy(alpha = 0.28f),
                            SurfaceBlue.copy(alpha = 0.1f),
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.GraphicEq,
                contentDescription = null,
                tint = PastelPurple,
                modifier = Modifier.size(44.dp),
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            color = TextSecondary,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )
    }
}
@Composable
private fun PlaybackHistoryEntryRow(
    item: PlaybackHistoryItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceBlue.copy(alpha = 0.82f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(DeepSleepBlue.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.History,
                contentDescription = null,
                tint = DeepSleepBlue,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tr(item.subtitle.ifBlank { item.itemType.name }),
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(SurfaceBlueLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Replay",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
@Composable
private fun FavoritedStoryItemRow(
    podcast: Podcast,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceBlue.copy(alpha = 0.88f))
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceBlueLight),
        ) {
            podcast.imagePath?.let { path ->
                AssetImage(
                    assetUri = path,
                    contentDescription = podcast.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = podcast.title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tr("Sleep Story", "História de Dormir", "Truyện kể ru ngủ"),
                color = PastelPurple,
                fontSize = 12.sp,
            )
        }
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Remove Favorite",
                tint = DeepSleepHeartRed,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
@Composable
private fun FavoritedSceneItemRow(
    scene: SleepPreset,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceBlue.copy(alpha = 0.88f))
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceBlueLight),
        ) {
            scene.imagePath?.let { path ->
                AssetImage(
                    assetUri = path,
                    contentDescription = scene.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = scene.name,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = scene.items.joinToString(" · ") { it.name }.ifBlank { "Soundscape" },
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Remove Favorite",
                tint = DeepSleepHeartRed,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
@Composable
private fun MiniSoundLayerBadgeDot(
    soundId: String,
    activeColor: Color,
) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(
                if (soundId.isNotBlank()) activeColor else Color.White.copy(alpha = 0.07f),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (soundId.isNotBlank()) {
            SoundSymbolIcon(
                soundId = soundId,
                symbolPath = null,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}
@Composable
private fun QuadLayerIconGridBadge(
    soundIds: List<String>,
) {
    val slots = remember(soundIds) {
        (soundIds + listOf("", "", "", "")).take(4)
    }
    val dotColors = remember {
        listOf(
            Color(0xFF4F8BFF),
            Color(0xFFFF5E62),
            Color(0xFFF59E0B),
            Color(0xFF10B981),
        )
    }

    Column(
        modifier = Modifier
            .size(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF161F3A))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MiniSoundLayerBadgeDot(soundId = slots[0], activeColor = dotColors[0])
            MiniSoundLayerBadgeDot(soundId = slots[1], activeColor = dotColors[1])
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MiniSoundLayerBadgeDot(soundId = slots[2], activeColor = dotColors[2])
            MiniSoundLayerBadgeDot(soundId = slots[3], activeColor = dotColors[3])
        }
    }
}
@Composable
private fun SavedCustomMixCardRow(
    mix: UserMix,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceBlue.copy(alpha = 0.9f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .clickable(onClick = onPlay)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuadLayerIconGridBadge(
            soundIds = mix.items.map { it.soundId },
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mix.name,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = mix.items.joinToString(", ") { it.name }.ifBlank { "${mix.items.size} sounds" },
                color = TextSecondary,
                fontSize = 12.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.DeleteOutline,
                contentDescription = "Delete mix",
                tint = TextSecondary,
            )
        }

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(DeepSleepBlue),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
@Composable
private fun NaturalSoundscapesPage(
    scenes: List<SleepPreset>,
    onOpenScene: (SleepPreset) -> Unit,
    onToggleSceneFavorite: (String) -> Unit,
) {
    val gridState = remember { LazyGridState(0, 0) }
    val firstTwoRows = remember(scenes) { scenes.take(2) }
    val remainingRows = remember(scenes) { scenes.drop(2) }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 8.dp,
            bottom = 32.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(firstTwoRows, key = { "sc_sub_${it.id}" }) { scene ->
            SquareSceneArtworkCard(
                scene = scene,
                onToggleFavorite = { onToggleSceneFavorite(scene.id) },
                onClick = { onOpenScene(scene) },
            )
        }
        item(
            key = "soundscapes_banner_row_2_3",
            span = { GridItemSpan(maxLineSpan) },
        ) {
            AdaptiveBannerAdBar(slotKey = "sounds_soundscapes_banner")
        }
        items(remainingRows, key = { "sc_sub_${it.id}" }) { scene ->
            SquareSceneArtworkCard(
                scene = scene,
                onToggleFavorite = { onToggleSceneFavorite(scene.id) },
                onClick = { onOpenScene(scene) },
            )
        }
    }
}
@Composable
private fun GuidedMeditationScenesPage(
    scenes: List<SleepPreset>,
    onOpenScene: (SleepPreset) -> Unit,
    onToggleSceneFavorite: (String) -> Unit,
) {
    var selectedSubChip by rememberSaveable { mutableIntStateOf(0) }
    val displayScenes = remember(scenes, selectedSubChip) {
        if (selectedSubChip == 0) scenes else scenes.reversed()
    }
    val firstTwoRows = remember(displayScenes) { displayScenes.take(2) }
    val remainingRows = remember(displayScenes) { displayScenes.drop(2) }
    val gridState = remember(selectedSubChip) { LazyGridState(0, 0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SelectableCategoryPill(
                label = tr("Sleep Meditation", "Meditação para Dormir", "Thiền ngủ"),
                selected = selectedSubChip == 0,
                onClick = { selectedSubChip = 0 },
            )
            SelectableCategoryPill(
                label = tr("Guided Meditation", "Meditação Guiada", "Thiền dẫn"),
                selected = selectedSubChip == 1,
                onClick = { selectedSubChip = 1 },
            )
        }

        key(selectedSubChip) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 8.dp,
                    bottom = 32.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(firstTwoRows, key = { "med_sub_${selectedSubChip}_${it.id}" }) { scene ->
                    SquareSceneArtworkCard(
                        scene = scene,
                        onToggleFavorite = { onToggleSceneFavorite(scene.id) },
                        onClick = { onOpenScene(scene) },
                    )
                }
                item(
                    key = "meditation_banner_row_2_3_$selectedSubChip",
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    AdaptiveBannerAdBar(slotKey = "sounds_meditation_banner")
                }
                items(remainingRows, key = { "med_sub_${selectedSubChip}_${it.id}" }) { scene ->
                    SquareSceneArtworkCard(
                        scene = scene,
                        onToggleFavorite = { onToggleSceneFavorite(scene.id) },
                        onClick = { onOpenScene(scene) },
                    )
                }
            }
        }
    }
}
@Composable
private fun BedtimeStoriesCatalogPage(
    podcasts: List<Podcast>,
    categories: List<ContentCategory>,
    selectedCategory: String?,
    onSelectCategory: (String?) -> Unit,
    onOpenPodcast: (Podcast) -> Unit,
) {
    val catKey = selectedCategory.orEmpty()
    val gridState = remember(catKey) { LazyGridState(0, 0) }
    val firstTwoRows = remember(podcasts) { podcasts.take(2) }
    val remainingRows = remember(podcasts) { podcasts.drop(2) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (categories.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item(key = "story_all") {
                    SelectableCategoryPill(
                        label = stringResource(R.string.filter_all),
                        selected = selectedCategory == null,
                        onClick = { onSelectCategory(null) },
                    )
                }
                items(categories, key = { it.id }) { cat ->
                    SelectableCategoryPill(
                        label = tr(cat.label),
                        selected = selectedCategory == cat.id,
                        onClick = { onSelectCategory(cat.id) },
                    )
                }
            }
        }

        if (podcasts.isEmpty()) {
            AdaptiveBannerAdBar(
                slotKey = "sounds_stories_banner",
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            CatalogEmptyPlaceholder(
                title = tr("No Stories Found", "Nenhuma História Encontrada", "Không tìm thấy truyện"),
                subtitle = tr("Try selecting another category filter above.", "Tente selecionar outro filtro de categoria acima.", "Hãy thử chọn một danh mục khác ở phía trên."),
            )
        } else {
            key(catKey) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 32.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(firstTwoRows, key = { "${catKey}_story_card_${it.id}" }) { podcast ->
                        PodcardGridCard(
                            podcast = podcast,
                            onClick = { onOpenPodcast(podcast) },
                        )
                    }
                    item(
                        key = "${catKey}_stories_banner_row_2_3",
                        span = { GridItemSpan(maxLineSpan) },
                    ) {
                        AdaptiveBannerAdBar(slotKey = "sounds_stories_banner")
                    }
                    items(remainingRows, key = { "${catKey}_story_card_${it.id}" }) { podcast ->
                        PodcardGridCard(
                            podcast = podcast,
                            onClick = { onOpenPodcast(podcast) },
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun HealingMusicScenesPage(
    scenes: List<SleepPreset>,
    onOpenScene: (SleepPreset) -> Unit,
    onToggleSceneFavorite: (String) -> Unit,
) {
    val musicChips = remember {
        listOf(
            "all" to "All",
            "healing" to "Healing Music",
            "nature" to "Nature Melodies",
            "lullaby" to "Lullaby",
            "binaural" to "Binaural Beats",
            "meditation" to "Meditation Music",
        )
    }
    var selectedMusicChip by rememberSaveable { mutableStateOf("all") }

    val filteredScenes = remember(scenes, selectedMusicChip) {
        when (selectedMusicChip) {
            "all" -> scenes
            "healing" -> scenes.filter {
                it.name.contains("Heal", ignoreCase = true) ||
                    it.name.contains("Peace", ignoreCase = true) ||
                    it.categories.any { c -> c.contains("relax", ignoreCase = true) }
            }.ifEmpty { scenes }
            "nature" -> scenes.filter {
                it.categories.any { c -> c.contains("nature", ignoreCase = true) || c.contains("water", ignoreCase = true) }
            }.ifEmpty { scenes }
            "lullaby" -> scenes.filter {
                it.name.contains("Piano", ignoreCase = true) ||
                    it.name.contains("Lullaby", ignoreCase = true) ||
                    it.name.contains("Star", ignoreCase = true) ||
                    it.categories.any { c -> c.contains("instrument", ignoreCase = true) }
            }.ifEmpty { scenes }
            "binaural" -> scenes.filter {
                it.name.contains("Wave", ignoreCase = true) ||
                    it.name.contains("Hum", ignoreCase = true) ||
                    it.categories.any { c -> c.contains("focus", ignoreCase = true) }
            }.ifEmpty { scenes }
            else -> scenes.filter {
                it.categories.any { c -> c.contains("meditat", ignoreCase = true) }
            }.ifEmpty { scenes }
        }
    }
    val firstTwoRows = remember(filteredScenes) { filteredScenes.take(2) }
    val remainingRows = remember(filteredScenes) { filteredScenes.drop(2) }

    val gridState = remember(selectedMusicChip) { LazyGridState(0, 0) }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(musicChips, key = { it.first }) { (chipId, label) ->
                SelectableCategoryPill(
                    label = tr(label),
                    selected = selectedMusicChip == chipId,
                    onClick = { selectedMusicChip = chipId },
                )
            }
        }

        key(selectedMusicChip) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 8.dp,
                    bottom = 32.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(firstTwoRows, key = { "${selectedMusicChip}_music_scene_${it.id}" }) { scene ->
                    SquareSceneArtworkCard(
                        scene = scene,
                        onToggleFavorite = { onToggleSceneFavorite(scene.id) },
                        onClick = { onOpenScene(scene) },
                    )
                }
                item(
                    key = "${selectedMusicChip}_music_banner_row_2_3",
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    AdaptiveBannerAdBar(slotKey = "sounds_music_banner")
                }
                items(remainingRows, key = { "${selectedMusicChip}_music_scene_${it.id}" }) { scene ->
                    SquareSceneArtworkCard(
                        scene = scene,
                        onToggleFavorite = { onToggleSceneFavorite(scene.id) },
                        onClick = { onOpenScene(scene) },
                    )
                }
            }
        }
    }
}
@Composable
private fun CuratedFeaturedScenesPage(
    featuredScenes: List<SleepPreset>,
    recentlyUpdatedScenes: List<SleepPreset>,
    healingMusicScenes: List<SleepPreset>,
    onOpenScene: (SleepPreset) -> Unit,
    onToggleSceneFavorite: (String) -> Unit,
) {
    val allFeatured = remember(featuredScenes, recentlyUpdatedScenes, healingMusicScenes) {
        (featuredScenes + recentlyUpdatedScenes + healingMusicScenes).distinctBy { it.id }
    }
    val firstTwoRows = remember(allFeatured) { allFeatured.take(2) }
    val remainingRows = remember(allFeatured) { allFeatured.drop(2) }
    val gridState = remember { LazyGridState(0, 0) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 8.dp,
            bottom = 32.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        itemsIndexed(firstTwoRows, key = { _, it -> "feat_grid_${it.id}" }) { index, scene ->
            SquareSceneArtworkCard(
                scene = scene,
                badgeText = if (index < 4) "NEW" else "FEATURED",
                badgeColor = if (index < 4) DeepSleepNewBadgeRed else DeepSleepBlue,
                onToggleFavorite = { onToggleSceneFavorite(scene.id) },
                onClick = { onOpenScene(scene) },
            )
        }
        item(
            key = "featured_banner_row_2_3",
            span = { GridItemSpan(maxLineSpan) },
        ) {
            AdaptiveBannerAdBar(slotKey = "sounds_featured_banner")
        }
        itemsIndexed(remainingRows, key = { _, it -> "feat_grid_${it.id}" }) { _, scene ->
            SquareSceneArtworkCard(
                scene = scene,
                badgeText = "FEATURED",
                badgeColor = DeepSleepBlue,
                onToggleFavorite = { onToggleSceneFavorite(scene.id) },
                onClick = { onOpenScene(scene) },
            )
        }
    }
}
@Composable
private fun AmbientWhiteNoisePage(
    sounds: List<MixSoundState>,
    presets: List<SleepPreset>,
    tabResetGeneration: Int,
    onToggleSound: (MixSoundState) -> Unit,
    onSelectPresetMix: (SleepPreset) -> Unit,
    onTogglePresetFavorite: (String) -> Unit,
    onFilterSelected: () -> Unit = {},
) {
    var selectedChipId by rememberSaveable { mutableStateOf("all") }
    val gridState = remember(selectedChipId, tabResetGeneration) { LazyGridState(0, 0) }
    val mixesListState = remember(selectedChipId, tabResetGeneration) { LazyListState(0, 0) }

    val layoutInfo = rememberResponsiveLayoutInfo()
    val gridColumns = layoutInfo.soundGridColumns

    val displayedSounds = remember(sounds, selectedChipId) {
        val ordered = sounds.sortedWith(
            compareBy { s ->
                val idx = curatedAmbientSoundOrder.indexOf(s.soundId)
                if (idx >= 0) idx else 1000
            },
        )
        if (selectedChipId == "all" || selectedChipId == "mixes") {
            ordered
        } else {
            ordered.filter { matchesAmbientSoundFilter(it, selectedChipId) }
        }
    }

    LaunchedEffect(displayedSounds.isNotEmpty(), selectedChipId, tabResetGeneration) {
        gridState.requestScrollToItem(0, 0)
        mixesListState.requestScrollToItem(0, 0)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(ambientSoundFilterChips, key = { it.first }) { (chipId, label) ->
                SelectableCategoryPill(
                    label = tr(label),
                    selected = selectedChipId == chipId,
                    onClick = {
                        if (selectedChipId != chipId) {
                            selectedChipId = chipId
                            onFilterSelected()
                        }
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        key(selectedChipId) {
            if (selectedChipId == "mixes") {
                val firstTwoPresets = remember(presets) { presets.take(1) }
                val remainingPresets = remember(presets) { presets.drop(1) }
                LazyColumn(
                    state = mixesListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 32.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(firstTwoPresets, key = { "preset_mix_${selectedChipId}_${it.id}" }) { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(SurfaceBlue.copy(alpha = 0.88f))
                                .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
                                .clickable { onSelectPresetMix(preset) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            QuadLayerIconGridBadge(
                                soundIds = preset.items.map { it.soundId },
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = preset.name,
                                        color = TextPrimary,
                                        fontSize = 15.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = preset.items.joinToString(", ") { it.name },
                                    color = TextSecondary,
                                    fontSize = 12.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            IconButton(
                                onClick = { onTogglePresetFavorite(preset.id) },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    imageVector = if (preset.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (preset.isFavorite) DeepSleepHeartRed else TextSecondary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }

                    item(key = "preset_mix_banner_row_2_3") {
                        AdaptiveBannerAdBar(slotKey = "sounds_mixes_banner")
                    }

                    items(remainingPresets, key = { "preset_mix_${selectedChipId}_${it.id}" }) { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(SurfaceBlue.copy(alpha = 0.88f))
                                .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
                                .clickable { onSelectPresetMix(preset) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            QuadLayerIconGridBadge(
                                soundIds = preset.items.map { it.soundId },
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = preset.name,
                                        color = TextPrimary,
                                        fontSize = 15.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = preset.items.joinToString(", ") { it.name },
                                    color = TextSecondary,
                                    fontSize = 12.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            IconButton(
                                onClick = { onTogglePresetFavorite(preset.id) },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    imageVector = if (preset.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (preset.isFavorite) DeepSleepHeartRed else TextSecondary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            } else {
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
                    val firstTwoRowsSounds = remember(displayedSounds, firstTwoRowsCount) {
                        displayedSounds.take(firstTwoRowsCount)
                    }
                    val remainingSounds = remember(displayedSounds, firstTwoRowsCount) {
                        displayedSounds.drop(firstTwoRowsCount)
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumns),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = horizontalPadding,
                            end = horizontalPadding,
                            top = 12.dp,
                            bottom = 32.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        items(
                            items = firstTwoRowsSounds,
                            key = { "${selectedChipId}_${it.soundId}" },
                            contentType = { "sound_tile" },
                        ) { sound ->
                            SoundGridButton(
                                sound = sound,
                                circleSize = circleSize,
                                onClick = { onToggleSound(sound) },
                            )
                        }

                        item(
                            key = "${selectedChipId}_wn_banner_row_2_3",
                            span = { GridItemSpan(maxLineSpan) },
                        ) {
                            AdaptiveBannerAdBar(slotKey = "sounds_whitenoise_banner")
                        }

                        items(
                            items = remainingSounds,
                            key = { "${selectedChipId}_${it.soundId}" },
                            contentType = { "sound_tile" },
                        ) { sound ->
                            SoundGridButton(
                                sound = sound,
                                circleSize = circleSize,
                                onClick = { onToggleSound(sound) },
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun SavedAidsAndHistoryPage(
    libraryState: LibraryUiState,
    onPlayUserMix: (UserMix) -> Unit,
    onDeleteUserMix: (String) -> Unit,
    onOpenScene: (SleepPreset) -> Unit,
    onToggleSceneFavorite: (String) -> Unit,
    onOpenPodcast: (String) -> Unit,
    onTogglePodcastFavorite: (String) -> Unit,
    onReplayHistory: (PlaybackHistoryItem) -> Unit,
    onClearHistory: () -> Unit,
) {
    var selectedSection by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SelectableCategoryPill(
                label = stringResource(R.string.aids_favorites),
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
            )
            SelectableCategoryPill(
                label = stringResource(R.string.aids_history),
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
            )
            Spacer(modifier = Modifier.weight(1f))
            if (selectedSection == 1 && libraryState.playbackHistory.isNotEmpty()) {
                Text(
                    text = tr("Clear", "Limpar", "Xóa"),
                    color = PastelPurple,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onClearHistory)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        if (selectedSection == 0) {
            val hasAnyFavorites = libraryState.userMixes.isNotEmpty() ||
                libraryState.favoriteScenes.isNotEmpty() ||
                libraryState.favoritePodcasts.isNotEmpty() ||
                libraryState.favoriteSounds.isNotEmpty()

            if (!hasAnyFavorites) {
                AdaptiveBannerAdBar(
                    slotKey = "sounds_tab_banner",
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                CatalogEmptyPlaceholder(
                    title = tr("No Favorites Yet", "Nenhum Favorito Ainda", "Chưa có mục yêu thích"),
                    subtitle = tr("Save custom mixes or tap the heart icon on any soundscape or story to find them here.", "Salve mixes ou toque no coração em qualquer som ou história para encontrá-los aqui.", "Lưu bản phối hoặc chạm vào biểu tượng trái tim trên âm thanh/truyện để xem tại đây."),
                )
            } else {
                val favListState = remember(selectedSection) { LazyListState(0, 0) }
                LazyColumn(
                    state = favListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 10.dp,
                        bottom = 32.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "fav_banner_row_2_3") {
                        AdaptiveBannerAdBar(slotKey = "sounds_tab_banner")
                    }

                    items(libraryState.userMixes, key = { "mix_${it.id}" }) { mix ->
                        SavedCustomMixCardRow(
                            mix = mix,
                            onPlay = { onPlayUserMix(mix) },
                            onDelete = { onDeleteUserMix(mix.id) },
                        )
                    }

                    items(libraryState.favoriteScenes, key = { "fav_scene_${it.id}" }) { scene ->
                        FavoritedSceneItemRow(
                            scene = scene,
                            onToggleFavorite = { onToggleSceneFavorite(scene.id) },
                            onClick = { onOpenScene(scene) },
                        )
                    }

                    items(libraryState.favoritePodcasts, key = { "fav_pod_${it.id}" }) { podcast ->
                        FavoritedStoryItemRow(
                            podcast = podcast,
                            onToggleFavorite = { onTogglePodcastFavorite(podcast.id) },
                            onClick = { onOpenPodcast(podcast.id) },
                        )
                    }
                }
            }
        } else {
            if (libraryState.playbackHistory.isEmpty()) {
                AdaptiveBannerAdBar(
                    slotKey = "sounds_tab_banner",
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                CatalogEmptyPlaceholder(
                    title = tr("No Listening History", "Sem Histórico de Reprodução", "Chưa có lịch sử nghe"),
                    subtitle = tr("Sounds, mixes, and bedtime stories you play will automatically appear here.", "Os sons, mixes e histórias que você ouvir aparecerão automaticamente aqui.", "Các âm thanh, bản phối và truyện ngủ bạn phát sẽ tự động hiển thị tại đây."),
                )
            } else {
                val histListState = remember(selectedSection) { LazyListState(0, 0) }
                val firstTwoHistory = remember(libraryState.playbackHistory) {
                    libraryState.playbackHistory.take(1)
                }
                val remainingHistory = remember(libraryState.playbackHistory) {
                    libraryState.playbackHistory.drop(1)
                }
                LazyColumn(
                    state = histListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 10.dp,
                        bottom = 32.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(firstTwoHistory, key = { "hist_${it.itemKey}" }) { item ->
                        PlaybackHistoryEntryRow(
                            item = item,
                            onClick = { onReplayHistory(item) },
                        )
                    }
                    item(key = "hist_banner_row_2_3") {
                        AdaptiveBannerAdBar(slotKey = "sounds_tab_banner")
                    }
                    items(remainingHistory, key = { "hist_${it.itemKey}" }) { item ->
                        PlaybackHistoryEntryRow(
                            item = item,
                            onClick = { onReplayHistory(item) },
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun SleepAidsTopCategoryBar(
    selectedSubtab: Int,
    onSelectSubtab: (Int) -> Unit,
    timerActive: Boolean,
    timerFormatted: String,
    onOpenTimer: () -> Unit,
) {
    val tabs = listOf(
        stringResource(R.string.aids_my_aids),
        stringResource(R.string.aids_white_noise),
        stringResource(R.string.aids_featured),
        stringResource(R.string.aids_music),
        stringResource(R.string.aids_story),
        stringResource(R.string.aids_meditation),
        stringResource(R.string.aids_soundscape),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 14.dp, top = 10.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            itemsIndexed(tabs) { index, label ->
                val isSelected = selectedSubtab == index
                Column(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectSubtab(index) },
                        )
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = if (isSelected) 18.sp else 15.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Box(
                        modifier = Modifier
                            .width(if (isSelected) 24.dp else 0.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isSelected) DeepSleepBlue else Color.Transparent),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (timerActive) DeepSleepBlue.copy(alpha = 0.25f) else SurfaceBlueLight.copy(alpha = 0.8f),
                )
                .border(
                    width = 1.dp,
                    color = if (timerActive) DeepSleepBlue else Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(16.dp),
                )
                .clickable(onClick = onOpenTimer)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Timer,
                contentDescription = "Sleep Timer",
                tint = if (timerActive) DeepSleepBlue else TextPrimary,
                modifier = Modifier.size(16.dp),
            )
            if (timerActive && timerFormatted.isNotBlank()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = timerFormatted,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
private data class PendingRewardUnlock(
    val itemId: String,
    val itemTitle: String,
    val onUnlocked: () -> Unit,
)

@Composable
fun SoundsTabScreen(
    sleepTimerManager: SleepTimerManager,
    tabResetGeneration: Int = 0,
    initialSubtab: Int = 1,
    onOpenMixDetail: () -> Unit,
    onNavigateToSceneDetail: (String) -> Unit = {},
    onNavigateToPodcardDetail: (String) -> Unit = {},
    onOpenPaywall: () -> Unit = {},
    mixerViewModel: MixerViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    podcardViewModel: PodcardViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel(),
) {
    val mixerState by mixerViewModel.uiState.collectAsStateWithLifecycle()
    val playbackState by mixerViewModel.playbackState.collectAsStateWithLifecycle()
    val timerState by sleepTimerManager.state.collectAsStateWithLifecycle()
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val podcardState by podcardViewModel.uiState.collectAsStateWithLifecycle()
    val libraryState by libraryViewModel.uiState.collectAsStateWithLifecycle()

    var selectedSubtab by rememberSaveable { mutableIntStateOf(initialSubtab.coerceIn(0, 6)) }
    var showTimerSheet by remember { mutableStateOf(false) }
    var pendingUnlock by remember { mutableStateOf<PendingRewardUnlock?>(null) }

    fun handleSoundClick(sound: MixSoundState) {
        if (sound.isPremium && !sound.isActive) {
            pendingUnlock = PendingRewardUnlock(
                itemId = sound.soundId,
                itemTitle = sound.name,
                onUnlocked = { mixerViewModel.toggleSound(sound.copy(isPremium = false)) },
            )
        } else {
            mixerViewModel.adMobManager.onUserAction {
                mixerViewModel.toggleSound(sound)
            }
        }
    }

    fun handleSceneClick(scene: SleepPreset) {
        if (scene.isPremium) {
            pendingUnlock = PendingRewardUnlock(
                itemId = scene.id,
                itemTitle = scene.name,
                onUnlocked = { onNavigateToSceneDetail(scene.id) },
            )
        } else {
            onNavigateToSceneDetail(scene.id)
        }
    }

    fun handlePodcastClick(podcast: Podcast) {
        if (podcast.isPremium) {
            pendingUnlock = PendingRewardUnlock(
                itemId = podcast.id,
                itemTitle = podcast.title,
                onUnlocked = { onNavigateToPodcardDetail(podcast.id) },
            )
        } else {
            onNavigateToPodcardDetail(podcast.id)
        }
    }

    LaunchedEffect(initialSubtab) {
        selectedSubtab = initialSubtab.coerceIn(0, 6)
    }

    val activeSounds = remember(mixerState.sounds) { mixerState.sounds.filter { it.isActive } }
    val showFloatingMixBar = activeSounds.isNotEmpty() || playbackState.mode == PlaybackMode.MIXER
    val formattedTimer = if (timerState.isActive) {
        sleepTimerManager.formatRemaining(timerState.remainingSeconds)
    } else {
        ""
    }

    ScreenBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SleepAidsTopCategoryBar(
                    selectedSubtab = selectedSubtab,
                    onSelectSubtab = { newTab ->
                        if (newTab != selectedSubtab) {
                            selectedSubtab = newTab
                            mixerViewModel.adMobManager.onUserAction()
                        }
                    },
                    timerActive = timerState.isActive,
                    timerFormatted = formattedTimer,
                    onOpenTimer = { showTimerSheet = true },
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    key(selectedSubtab) {
                        when (selectedSubtab) {
                            0 -> SavedAidsAndHistoryPage(
                                libraryState = libraryState,
                                onPlayUserMix = { mix ->
                                    mixerViewModel.adMobManager.onUserAction {
                                        homeViewModel.playUserMix(mix)
                                    }
                                },
                                onDeleteUserMix = { mixId -> libraryViewModel.deleteMix(mixId) },
                                onOpenScene = { scene -> onNavigateToSceneDetail(scene.id) },
                                onToggleSceneFavorite = { sceneId -> homeViewModel.toggleSceneFavorite(sceneId) },
                                onOpenPodcast = onNavigateToPodcardDetail,
                                onTogglePodcastFavorite = { podId -> homeViewModel.togglePodcastFavorite(podId) },
                                onReplayHistory = { item ->
                                    when (item.itemType) {
                                        HistoryItemType.SCENE -> {
                                            val scene = homeState.scenes.find { it.id == item.itemId }
                                            if (scene != null) {
                                                onNavigateToSceneDetail(scene.id)
                                            }
                                        }
                                        HistoryItemType.PODCAST, HistoryItemType.YOUTUBE -> {
                                            onNavigateToPodcardDetail(item.itemId)
                                        }
                                        HistoryItemType.MIX -> {
                                            val mix = libraryState.userMixes.find { it.id == item.itemId }
                                            if (mix != null) {
                                                mixerViewModel.adMobManager.onUserAction {
                                                    homeViewModel.playUserMix(mix)
                                                }
                                            }
                                        }
                                        HistoryItemType.SOUND -> {
                                            val sound = mixerState.sounds.find { it.soundId == item.itemId }
                                            if (sound != null) {
                                                mixerViewModel.adMobManager.onUserAction {
                                                    mixerViewModel.toggleSound(sound)
                                                }
                                            }
                                        }
                                    }
                                },
                                onClearHistory = { libraryViewModel.clearHistory() },
                            )

                            1 -> AmbientWhiteNoisePage(
                                sounds = mixerState.sounds,
                                presets = homeState.scenes,
                                tabResetGeneration = tabResetGeneration,
                                onToggleSound = ::handleSoundClick,
                                onSelectPresetMix = ::handleSceneClick,
                                onTogglePresetFavorite = { presetId ->
                                    homeViewModel.toggleSceneFavorite(presetId)
                                },
                                onFilterSelected = {
                                    mixerViewModel.adMobManager.onUserAction()
                                },
                            )

                            2 -> CuratedFeaturedScenesPage(
                                featuredScenes = homeState.featuredScenes,
                                recentlyUpdatedScenes = homeState.recentlyUpdatedScenes,
                                healingMusicScenes = homeState.healingMusicScenes,
                                onOpenScene = ::handleSceneClick,
                                onToggleSceneFavorite = { sceneId ->
                                    homeViewModel.toggleSceneFavorite(sceneId)
                                },
                            )

                            3 -> HealingMusicScenesPage(
                                scenes = homeState.healingMusicScenes.ifEmpty { homeState.scenes },
                                onOpenScene = ::handleSceneClick,
                                onToggleSceneFavorite = { sceneId ->
                                    homeViewModel.toggleSceneFavorite(sceneId)
                                },
                            )

                            4 -> BedtimeStoriesCatalogPage(
                                podcasts = podcardState.podcasts,
                                categories = podcardState.categories,
                                selectedCategory = podcardState.selectedCategory,
                                onSelectCategory = { catId ->
                                    mixerViewModel.adMobManager.onUserAction()
                                    podcardViewModel.selectCategory(catId)
                                },
                                onOpenPodcast = ::handlePodcastClick,
                            )

                            5 -> GuidedMeditationScenesPage(
                                scenes = homeState.meditationScenes.ifEmpty { homeState.scenes },
                                onOpenScene = ::handleSceneClick,
                                onToggleSceneFavorite = { sceneId ->
                                    homeViewModel.toggleSceneFavorite(sceneId)
                                },
                            )

                            else -> NaturalSoundscapesPage(
                                scenes = homeState.soundscapeScenes.ifEmpty { homeState.scenes },
                                onOpenScene = ::handleSceneClick,
                                onToggleSceneFavorite = { sceneId ->
                                    homeViewModel.toggleSceneFavorite(sceneId)
                                },
                            )
                        }
                    }
                }
            }

            if (showFloatingMixBar) {
                FloatingActiveMixDock(
                    activeSounds = activeSounds,
                    mixTitle = playbackState.title.ifBlank { "Mix" },
                    isPlaying = playbackState.isPlaying,
                    onTogglePlayPause = {
                        if (playbackState.isPlaying) {
                            mixerViewModel.pauseAll()
                        } else {
                            mixerViewModel.resumeAll()
                        }
                    },
                    onOpenMixDetail = onOpenMixDetail,
                    onStopAll = { mixerViewModel.stopAll() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
    }

    if (showTimerSheet) {
        SleepTimerBottomSheet(
            sleepTimerManager = sleepTimerManager,
            onDismiss = { showTimerSheet = false },
        )
    }

    val unlockTarget = pendingUnlock
    if (unlockTarget != null) {
        RewardedUnlockBottomSheet(
            itemId = unlockTarget.itemId,
            itemTitle = unlockTarget.itemTitle,
            adMobManager = mixerViewModel.adMobManager,
            onUnlocked = {
                val callback = unlockTarget.onUnlocked
                pendingUnlock = null
                callback()
            },
            onOpenPaywall = onOpenPaywall,
            onDismiss = { pendingUnlock = null },
        )
    }
}
private fun matchesAmbientSoundFilter(sound: MixSoundState, chipId: String): Boolean {
    val id = sound.soundId.lowercase()
    val cats = sound.categories.map { it.lowercase() }
    return when (chipId) {
        "all" -> true
        "rain" -> id.contains("rain") || id.contains("thunder") || "rain" in cats
        "asmr" -> id in setOf(
            "fire", "campfire_glow", "leaves", "water_drop", "cave_drops",
            "cat_purring", "clock", "rain_on_tent", "rain_on_leaves", "soft_water",
        )
        "nature" -> id in setOf(
            "fire", "campfire_glow", "water_wave", "calm_sea", "cold_wind", "wind_storm",
            "river", "creek", "waterfall", "under_water", "cave", "leaves", "water_drop",
            "night_forest", "night_lake", "grassland", "night", "night_breeze", "snow",
        ) || ("nature" in cats && "rain" !in cats && "animals" !in cats)
        "animal" -> "animals" in cats || id in setOf(
            "bird_forest", "birds_singing", "cat_purring", "crickets",
            "frogs", "loons", "owl", "seagull", "whale", "wolves",
        )
        "city" -> "city" in cats || id in setOf(
            "clock", "city_rush", "street_life", "train_journey",
            "airplane", "spaceship", "air_conditioner",
        )
        "fan" -> id in setOf(
            "soft_fan", "air_conditioner", "cold_wind", "wind_storm",
            "night_breeze", "white_noise", "brown_noise",
        )
        "melody" -> "instrument" in cats || id in setOf(
            "bamboo_flute", "soft_piano", "midnight_piano", "soft_guitar",
            "evening_guitar", "warm_guitar", "kalimba", "wind_chime",
            "crystal_bowls", "meditation_bowl", "zen_bowls", "bell",
        )
        "baby" -> id in setOf(
            "soft_piano", "kalimba", "music_box", "white_noise", "pink_noise",
            "soft_fan", "water_wave", "calm_sea", "peace_and_happy", "peaceful_day",
        )
        "noise" -> id in setOf(
            "white_noise", "pink_noise", "brown_noise", "green_noise",
            "432hz", "528hz", "binaural_alpha", "binaural_delta", "binaural_theta",
        ) || "white_noise" in cats
        "special" -> id in setOf(
            "432hz", "528hz", "binaural_alpha", "binaural_delta", "binaural_theta",
            "om_chanting", "temple_ambience", "crystal_bowls", "meditation_bowl", "zen_bowls",
        ) || "meditate" in cats
        else -> true
    }
}
