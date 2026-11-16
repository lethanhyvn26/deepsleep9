package com.sleepsounds.storytelling.presentation.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.data.local.DailyQuoteItem
import com.sleepsounds.storytelling.data.local.DreamSymbolItem
import com.sleepsounds.storytelling.data.local.localized
import com.sleepsounds.storytelling.domain.model.MixSoundState
import com.sleepsounds.storytelling.domain.model.Podcast
import com.sleepsounds.storytelling.domain.model.SleepPreset
import com.sleepsounds.storytelling.presentation.components.AssetImage
import com.sleepsounds.storytelling.presentation.components.ResetScrollOnTabOrCategoryChange
import com.sleepsounds.storytelling.presentation.components.ScreenBackground
import com.sleepsounds.storytelling.presentation.components.SoundSymbolIcon
import com.sleepsounds.storytelling.ads.AdaptiveBannerAdBar
import com.sleepsounds.storytelling.ads.RewardedAdFreePassCard
import com.sleepsounds.storytelling.ads.RewardedUnlockBottomSheet
import com.sleepsounds.storytelling.ui.theme.MidnightBlue
import com.sleepsounds.storytelling.ui.theme.PastelPurple
import com.sleepsounds.storytelling.ui.theme.PremiumGold
import com.sleepsounds.storytelling.ui.theme.DeepSleepBlue
import com.sleepsounds.storytelling.ui.theme.DeepSleepNewBadgeRed
import com.sleepsounds.storytelling.ui.theme.DeepSleepAccentGold
import com.sleepsounds.storytelling.ui.theme.SurfaceBlue
import com.sleepsounds.storytelling.ui.theme.SurfaceBlueLight
import com.sleepsounds.storytelling.ui.theme.statusAndCutoutPadding
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.TextSecondary
import com.sleepsounds.storytelling.ui.theme.rememberResponsiveLayoutInfo
import com.sleepsounds.storytelling.ui.theme.responsiveContentWidth
import com.sleepsounds.storytelling.ui.theme.tr

private val FavoriteHeartAccentRed = Color(0xFFFF4D6D)

@Composable
private fun HomeDailyWisdomQuoteCard(
    quote: DailyQuoteItem,
    modifier: Modifier = Modifier,
) {
    val localizedQuote = quote.localized()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF192042),
                        Color(0xFF10152E),
                    ),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
            .padding(22.dp),
    ) {
        Column {
            Icon(
                imageVector = Icons.Filled.FormatQuote,
                contentDescription = null,
                tint = PastelPurple.copy(alpha = 0.65f),
                modifier = Modifier.size(30.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"${localizedQuote.quote}\"",
                color = TextPrimary,
                fontSize = 15.5.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                lineHeight = 23.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "— ${localizedQuote.author}",
                color = PastelPurple,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun HomeDreamSymbolPreviewItem(
    symbol: DreamSymbolItem,
    onClick: () -> Unit,
) {
    val localizedSymbol = symbol.localized()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceBlue.copy(alpha = 0.86f))
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF312E81), Color(0xFF4C1D95)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = symbol.emoji,
                fontSize = 22.sp,
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = localizedSymbol.title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = localizedSymbol.subtitle,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary,
        )
    }
}

@Composable
private fun HomeBedtimeStoryCard(
    podcast: Podcast,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(152.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.28f)
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

            if (podcast.isPremium) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(PremiumGold)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "VIP",
                        color = MidnightBlue,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(onClick = onToggleFavorite),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (podcast.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (podcast.isFavorite) FavoriteHeartAccentRed else Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = podcast.title,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(R.string.aids_story),
            color = TextSecondary,
            fontSize = 11.5.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun HomeAmbientSoundQuickButton(
    sound: MixSoundState,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(74.dp)
            .clickable(
                interactionSource = remember(sound.soundId) { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(62.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(
                        if (sound.isActive) Color(0xFF3E7BFA) else Color(0xFF1B243E),
                    )
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
                    tint = if (sound.isActive) Color.White else TextPrimary.copy(alpha = 0.88f),
                    modifier = Modifier.size(26.dp),
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
            color = if (sound.isActive) TextPrimary else TextSecondary,
            fontSize = 11.5.sp,
            fontWeight = if (sound.isActive) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HomeSleepScenePosterCard(
    scene: SleepPreset,
    badgeLabel: String?,
    badgeColor: Color = DeepSleepNewBadgeRed,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(152.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.28f)
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.18f),
                                Color.Black.copy(alpha = 0.50f),
                            ),
                        ),
                    ),
            )

            val effectiveBadgeLabel = if (scene.isPremium) "VIP" else badgeLabel
            val effectiveBadgeColor = if (scene.isPremium) PremiumGold else badgeColor
            if (!effectiveBadgeLabel.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(effectiveBadgeColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = effectiveBadgeLabel,
                        color = if (scene.isPremium) MidnightBlue else Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(onClick = onToggleFavorite),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (scene.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (scene.isFavorite) FavoriteHeartAccentRed else Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
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
            fontSize = 11.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HomeFeedSectionHeader(
    title: String,
    subtitle: String? = null,
    onSeeAll: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
            }
        }
        if (onSeeAll != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onSeeAll)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = tr("See All", "Ver tudo", "Xem tất cả"),
                    color = PastelPurple,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "See All",
                    tint = PastelPurple,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun HomeShortcutActionCard(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember(label) { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accentColor.copy(alpha = 0.16f))
                .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp,
        )
    }
}

@Composable
private fun HomeAtmosphericHeroBanner(
    greeting: String,
    onOpenSettings: () -> Unit,
    onOpenSleepAid: () -> Unit,
    onOpenSoundscape: () -> Unit,
    onOpenDreamAnalysis: () -> Unit,
    onOpenBreathing: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0E152E),
                        Color(0xFF0A1024),
                        MidnightBlue,
                    ),
                ),
            ),
    ) {
        Canvas(
            modifier = Modifier.matchParentSize(),
        ) {
            val w = size.width
            val h = size.height

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A).copy(alpha = 0.28f),
                        Color.Transparent,
                    ),
                    center = Offset(w * 0.25f, h * 0.28f),
                    radius = w * 0.55f,
                ),
                center = Offset(w * 0.25f, h * 0.28f),
                radius = w * 0.55f,
            )

            val starPositions = listOf(
                Offset(w * 0.14f, h * 0.18f),
                Offset(w * 0.34f, h * 0.12f),
                Offset(w * 0.52f, h * 0.24f),
                Offset(w * 0.68f, h * 0.15f),
                Offset(w * 0.84f, h * 0.22f),
                Offset(w * 0.26f, h * 0.34f),
                Offset(w * 0.74f, h * 0.32f),
            )
            starPositions.forEachIndexed { i, pos ->
                drawCircle(
                    color = Color(0xFFCBD5E1).copy(alpha = if (i % 2 == 0) 0.45f else 0.25f),
                    radius = if (i % 3 == 0) 3.2f else 2.2f,
                    center = pos,
                )
            }

            val mountainPath = Path().apply {
                moveTo(0f, h * 0.72f)
                lineTo(w * 0.22f, h * 0.46f)
                lineTo(w * 0.45f, h * 0.64f)
                lineTo(w * 0.72f, h * 0.40f)
                lineTo(w, h * 0.62f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = mountainPath,
                color = Color(0xFF0A0F24).copy(alpha = 0.85f),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusAndCutoutPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = tr("Drift into calm, uninterrupted deep sleep", "Mergulhe em um sono calmo e profundo", "Chìm vào giấc ngủ sâu, êm dịu không gián đoạn"),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOpenSettings,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.cd_settings),
                        tint = TextPrimary,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(SurfaceBlue.copy(alpha = 0.90f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(22.dp),
                    )
                    .padding(vertical = 14.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                HomeShortcutActionCard(
                    icon = Icons.Filled.GraphicEq,
                    label = tr("Sleep Aid", "Sons", "Âm thanh"),
                    accentColor = Color(0xFF60A5FA),
                    onClick = onOpenSleepAid,
                )
                HomeShortcutActionCard(
                    icon = Icons.Filled.Bedtime,
                    label = tr("Soundscape", "Ambiente", "Không gian"),
                    accentColor = PremiumGold,
                    onClick = onOpenSoundscape,
                )
                HomeShortcutActionCard(
                    icon = Icons.Filled.AutoAwesome,
                    label = tr("Dream\nAnalysis", "Análise de\nSonhos", "Giải mã\nGiấc mơ"),
                    accentColor = Color(0xFFC084FC),
                    onClick = onOpenDreamAnalysis,
                )
                HomeShortcutActionCard(
                    icon = Icons.Filled.Air,
                    label = tr("Breathing", "Respiração", "Nhịp thở"),
                    accentColor = Color(0xFF34D399),
                    onClick = onOpenBreathing,
                )
            }
        }
    }
}

@Composable
private fun HomeFeaturedSceneCarousel(
    onOpenFeaturedAids: () -> Unit,
    onOpenStories: () -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        item(key = "banner_featured_soundscapes") {
            Row(
                modifier = Modifier
                    .width(312.dp)
                    .heightIn(min = 114.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF1F244D), Color(0xFF312E81)),
                        ),
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .clickable(onClick = onOpenFeaturedAids)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DeepSleepBlue)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = tr("Featured Soundscape", "Ambiente em Destaque", "Không Gian Nổi Bật"),
                            color = Color.White,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tr("Drift off with soothing rain, ocean & forest mixes", "Relaxe com mixes suaves de chuva, oceano e floresta", "Chìm vào giấc ngủ với tiếng mưa, sóng biển & rừng đêm"),
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 19.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Filled.GraphicEq,
                    contentDescription = null,
                    tint = PremiumGold,
                    modifier = Modifier.size(34.dp),
                )
            }
        }

        item(key = "banner_featured_reader") {
            Row(
                modifier = Modifier
                    .width(312.dp)
                    .heightIn(min = 114.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF132A3E), Color(0xFF1E3A5F)),
                        ),
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .clickable(onClick = onOpenStories)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PremiumGold)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = tr("Featured Reader", "Histórias em Destaque", "Truyện Ngủ Chọn Lọc"),
                            color = MidnightBlue,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tr("Calm bedtime stories & guided sleep meditations", "Histórias calmas para dormir e meditações guiadas", "Truyện kể êm dịu & thiền dẫn giấc ngủ sâu"),
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 19.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = PremiumGold,
                    modifier = Modifier.size(34.dp),
                )
            }
        }
    }
}

private data class HomePendingUnlock(
    val itemId: String,
    val itemTitle: String,
    val onUnlocked: () -> Unit,
)

@Composable
fun HomeScreen(
    onFirstContentReady: () -> Unit = {},
    tabResetGeneration: Int = 0,
    onNavigateToSettings: () -> Unit,
    onNavigateToSceneDetail: (String) -> Unit,
    onNavigateToAids: (initialSubtab: Int) -> Unit = {},
    onNavigateToPodcardDetail: (String) -> Unit = {},
    onNavigateToDreamDictionary: (initialDreamId: String?) -> Unit = {},
    onNavigateToBreathing: () -> Unit = {},
    onNavigateToPaywall: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var pendingUnlock by remember { mutableStateOf<HomePendingUnlock?>(null) }

    fun handleSceneTap(scene: SleepPreset) {
        if (scene.isPremium) {
            pendingUnlock = HomePendingUnlock(
                itemId = scene.id,
                itemTitle = scene.name,
                onUnlocked = { onNavigateToSceneDetail(scene.id) },
            )
        } else {
            onNavigateToSceneDetail(scene.id)
        }
    }

    fun handleQuickSoundTap(sound: MixSoundState) {
        if (sound.isPremium && !sound.isActive) {
            pendingUnlock = HomePendingUnlock(
                itemId = sound.soundId,
                itemTitle = sound.name,
                onUnlocked = { viewModel.toggleQuickSound(sound.copy(isPremium = false)) },
            )
        } else {
            viewModel.adMobManager.onUserAction {
                viewModel.toggleQuickSound(sound)
            }
        }
    }

    fun handleStoryTap(podcast: Podcast) {
        if (podcast.isPremium) {
            pendingUnlock = HomePendingUnlock(
                itemId = podcast.id,
                itemTitle = podcast.title,
                onUnlocked = { onNavigateToPodcardDetail(podcast.id) },
            )
        } else {
            onNavigateToPodcardDetail(podcast.id)
        }
    }

    ResetScrollOnTabOrCategoryChange(
        tabResetGeneration = tabResetGeneration,
        categoryKey = state.selectedCategory,
    ) {
        listState.requestScrollToItem(0)
    }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            onFirstContentReady()
        }
    }

    ScreenBackground {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = DeepSleepBlue)
            }
        } else {
            val layout = rememberResponsiveLayoutInfo()
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.responsiveContentWidth(if (layout.isExpandedWidth) 960.dp else 760.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                ) {
                    item(key = "deep_sleep_hero_header") {
                        HomeAtmosphericHeroBanner(
                            greeting = stringResource(state.greetingResId),
                            onOpenSettings = onNavigateToSettings,
                            onOpenSleepAid = { onNavigateToAids(1) },
                            onOpenSoundscape = { onNavigateToAids(2) },
                            onOpenDreamAnalysis = { onNavigateToDreamDictionary(null) },
                            onOpenBreathing = onNavigateToBreathing,
                        )
                    }

                    item(key = "home_inline_banner_1") {
                        AdaptiveBannerAdBar(
                            adMobManager = viewModel.adMobManager,
                            slotKey = "home_banner_1",
                        )
                    }

                    item(key = "deep_sleep_featured_banners") {
                        HomeFeaturedSceneCarousel(
                            onOpenFeaturedAids = { onNavigateToAids(2) },
                            onOpenStories = { onNavigateToAids(4) },
                            horizontalPadding = layout.screenHorizontalPadding,
                        )
                    }

                    item(key = "home_rewarded_ad_free_pass") {
                        RewardedAdFreePassCard(
                            adMobManager = viewModel.adMobManager,
                            onOpenPaywall = onNavigateToPaywall,
                            modifier = Modifier.padding(horizontal = layout.screenHorizontalPadding),
                        )
                    }

                    if (state.featuredScenes.isNotEmpty()) {
                        item(key = "section_featured_scenes") {
                            Column {
                                HomeFeedSectionHeader(
                                    title = tr("⭐ Featured", "⭐ Destaques", "⭐ Nổi bật"),
                                    subtitle = tr("Handpicked soundscapes & deep sleep mixes", "Paisagens sonoras e mixes selecionados para dormir", "Không gian âm thanh chọn lọc cho giấc ngủ sâu"),
                                    onSeeAll = { onNavigateToAids(2) },
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = layout.screenHorizontalPadding),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    items(
                                        items = state.featuredScenes,
                                        key = { "feat_${it.id}" },
                                    ) { scene ->
                                        HomeSleepScenePosterCard(
                                            scene = scene,
                                            badgeLabel = "FEATURED",
                                            badgeColor = DeepSleepBlue,
                                            onToggleFavorite = {
                                                viewModel.toggleSceneFavorite(scene.id)
                                            },
                                            onClick = {
                                                handleSceneTap(scene)
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        item(key = "home_inline_banner_2") {
                            AdaptiveBannerAdBar(
                                adMobManager = viewModel.adMobManager,
                                slotKey = "home_banner_2",
                            )
                        }
                    }

                    if (state.recentlyUpdatedScenes.isNotEmpty()) {
                        item(key = "section_recently_update") {
                            Column {
                                HomeFeedSectionHeader(
                                    title = tr("💖 Recently Update", "💖 Atualizados Recentemente", "💖 Mới cập nhật"),
                                    onSeeAll = { onNavigateToAids(2) },
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = layout.screenHorizontalPadding),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    items(
                                        items = state.recentlyUpdatedScenes,
                                        key = { "recent_${it.id}" },
                                    ) { scene ->
                                        HomeSleepScenePosterCard(
                                            scene = scene,
                                                                                        badgeLabel = "NEW",
                                            badgeColor = DeepSleepNewBadgeRed,
                                            onToggleFavorite = {
                                                viewModel.toggleSceneFavorite(scene.id)
                                            },
                                            onClick = {
                                                handleSceneTap(scene)
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (state.quickSounds.isNotEmpty()) {
                        item(key = "section_white_noise") {
                            val pairs = remember(state.quickSounds) { state.quickSounds.chunked(2) }
                            Column {
                                HomeFeedSectionHeader(
                                    title = tr("🌧️ White Noise", "🌧️ Ruído Branco", "🌧️ Tiếng ồn trắng"),
                                    onSeeAll = { onNavigateToAids(1) },
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = layout.screenHorizontalPadding),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    items(
                                        items = pairs,
                                        key = { pair -> "wn_pair_${pair.first().soundId}" },
                                    ) { pair ->
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            pair.forEach { sound ->
                                                HomeAmbientSoundQuickButton(
                                                    sound = sound,
                                                                                                        onClick = {
                                                        handleQuickSoundTap(sound)
                                                    },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (state.meditationScenes.isNotEmpty()) {
                        item(key = "section_sleep_meditation") {
                            Column {
                                HomeFeedSectionHeader(
                                    title = tr("🧘 Sleep Meditation", "🧘 Meditação para Dormir", "🧘 Thiền ngủ"),
                                    onSeeAll = { onNavigateToAids(5) },
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = layout.screenHorizontalPadding),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    items(
                                        items = state.meditationScenes,
                                        key = { "med_${it.id}" },
                                    ) { scene ->
                                        HomeSleepScenePosterCard(
                                            scene = scene,
                                                                                        badgeLabel = null,
                                            onToggleFavorite = {
                                                viewModel.toggleSceneFavorite(scene.id)
                                            },
                                            onClick = {
                                                handleSceneTap(scene)
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (state.soundscapeScenes.isNotEmpty()) {
                        item(key = "section_soundscape") {
                            Column {
                                HomeFeedSectionHeader(
                                    title = tr("🌲 Soundscape", "🌲 Paisagem Sonora", "🌲 Không gian tự nhiên"),
                                    subtitle = tr("Relax with the sounds of the great outdoors", "Relaxe com os sons da natureza ao ar livre", "Thư giãn với âm thanh thiên nhiên"),
                                    onSeeAll = { onNavigateToAids(6) },
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = layout.screenHorizontalPadding),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    items(
                                        items = state.soundscapeScenes,
                                        key = { "sc_${it.id}" },
                                    ) { scene ->
                                        HomeSleepScenePosterCard(
                                            scene = scene,
                                                                                        badgeLabel = null,
                                            onToggleFavorite = {
                                                viewModel.toggleSceneFavorite(scene.id)
                                            },
                                            onClick = {
                                                handleSceneTap(scene)
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (state.healingMusicScenes.isNotEmpty()) {
                        item(key = "section_healing_music") {
                            Column {
                                HomeFeedSectionHeader(
                                    title = tr("🎵 Healing Music", "🎵 Música Curativa", "🎵 Nhạc chữa lành"),
                                    subtitle = tr("Deep Healing Music for The Body", "Música de cura profunda para o corpo e a mente", "Nhạc thư giãn phục hồi năng lượng"),
                                    onSeeAll = { onNavigateToAids(3) },
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = layout.screenHorizontalPadding),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    items(
                                        items = state.healingMusicScenes,
                                        key = { "heal_${it.id}" },
                                    ) { scene ->
                                        HomeSleepScenePosterCard(
                                            scene = scene,
                                                                                        badgeLabel = null,
                                            onToggleFavorite = {
                                                viewModel.toggleSceneFavorite(scene.id)
                                            },
                                            onClick = {
                                                handleSceneTap(scene)
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (state.sleepStories.isNotEmpty()) {
                        item(key = "section_sleep_story") {
                            Column {
                                HomeFeedSectionHeader(
                                    title = tr("📖 Sleep Story", "📖 Histórias para Dormir", "📖 Truyện kể ru ngủ"),
                                    subtitle = tr("Say goodbye to sleepless nights with sleep stories", "Diga adeus às noites em claro com histórias relaxantes", "Tạm biệt mất ngủ với những câu chuyện êm dịu"),
                                    onSeeAll = { onNavigateToAids(4) },
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = layout.screenHorizontalPadding),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    items(
                                        items = state.sleepStories,
                                        key = { "story_${it.id}" },
                                    ) { podcast ->
                                        HomeBedtimeStoryCard(
                                            podcast = podcast,
                                                                                        onToggleFavorite = {
                                                viewModel.togglePodcastFavorite(podcast.id)
                                            },
                                            onClick = {
                                                handleStoryTap(podcast)
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item(key = "section_dream_analysis") {
                        Column {
                            HomeFeedSectionHeader(
                                title = stringResource(R.string.dream_analysis_title),
                                subtitle = stringResource(R.string.dream_analysis_subtitle),
                                onSeeAll = { onNavigateToDreamDictionary(null) },
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(
                                modifier = Modifier.padding(horizontal = layout.screenHorizontalPadding),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                state.dreamPreviews.forEach { symbol ->
                                    HomeDreamSymbolPreviewItem(
                                        symbol = symbol,
                                        onClick = { onNavigateToDreamDictionary(symbol.id) },
                                    )
                                }
                            }
                        }
                    }

                    item(key = "section_daily_quote") {
                        Column {
                            HomeFeedSectionHeader(
                                title = stringResource(R.string.daily_quote_title),
                                onSeeAll = null,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            HomeDailyWisdomQuoteCard(
                                quote = state.dailyQuote,
                                modifier = Modifier.padding(horizontal = layout.screenHorizontalPadding),
                            )
                        }
                    }
                }
            }
        }
    }

    val unlockTarget = pendingUnlock
    if (unlockTarget != null) {
        RewardedUnlockBottomSheet(
            itemId = unlockTarget.itemId,
            itemTitle = unlockTarget.itemTitle,
            adMobManager = viewModel.adMobManager,
            onUnlocked = {
                val action = unlockTarget.onUnlocked
                pendingUnlock = null
                action()
            },
            onOpenPaywall = onNavigateToPaywall,
            onDismiss = { pendingUnlock = null },
        )
    }
}
