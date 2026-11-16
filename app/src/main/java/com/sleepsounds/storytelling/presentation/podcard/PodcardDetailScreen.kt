package com.sleepsounds.storytelling.presentation.podcard

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.ads.AdaptiveBannerAdBar
import com.sleepsounds.storytelling.ads.RewardedUnlockBottomSheet
import com.sleepsounds.storytelling.domain.model.Podcast
import com.sleepsounds.storytelling.ui.theme.Dimens
import com.sleepsounds.storytelling.ui.theme.GlassOverlay
import com.sleepsounds.storytelling.ui.theme.ImmersiveSystemBars
import com.sleepsounds.storytelling.ui.theme.MidnightBlue
import com.sleepsounds.storytelling.ui.theme.PastelPurple
import com.sleepsounds.storytelling.ui.theme.PremiumGold
import com.sleepsounds.storytelling.ui.theme.SurfaceBlue
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.TextSecondary
import com.sleepsounds.storytelling.ui.theme.rememberResponsiveLayoutInfo
import com.sleepsounds.storytelling.ui.theme.responsiveContentWidth
import com.sleepsounds.storytelling.ui.theme.statusAndCutoutPadding
import com.sleepsounds.storytelling.ui.theme.systemSafePadding
import com.sleepsounds.storytelling.util.YouTubeUrls

@Composable
fun PodcardDetailScreen(
    onBack: () -> Unit,
    onOpenRelated: (String) -> Unit = {},
    onOpenPaywall: () -> Unit = {},
    viewModel: PodcardDetailViewModel = hiltViewModel(),
) {
    ImmersiveSystemBars()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val layoutMetrics = rememberResponsiveLayoutInfo()
    val horizontalInset = layoutMetrics.screenHorizontalPadding
    val appContext = LocalContext.current
    val currentPodcast = uiState.podcast
    val contentScrollState = rememberScrollState()
    var pendingUnlockPodcast by remember { mutableStateOf<Podcast?>(null) }

    if (uiState.isLoading || currentPodcast == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightBlue)
                .systemSafePadding(),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = stringResource(R.string.podcard_loading), color = TextSecondary)
        }
        return
    }

    LaunchedEffect(currentPodcast.id) {
        contentScrollState.animateScrollTo(0)
    }

    val primaryChapterPath = uiState.selectedChapter?.audioPath
        ?: currentPodcast.chapters.firstOrNull()?.audioPath
    val isVideoBacked = YouTubeUrls.isYouTubeUrl(primaryChapterPath)
    val playingVideoId = uiState.youtubeVideoId
    val videoMaxHeight = if (layoutMetrics.isCompactHeight) {
        (layoutMetrics.screenHeightDp * 0.45f).dp.coerceAtLeast(160.dp)
    } else {
        420.dp
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlue),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusAndCutoutPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(GlassOverlay.copy(alpha = 0.35f)),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = TextPrimary,
                )
            }
            IconButton(
                onClick = {
                    val shareText = "${currentPodcast.title} — Deep Sleep: " +
                        "https://play.google.com/store/apps/details?id=${appContext.packageName}"
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    runCatching {
                        appContext.startActivity(Intent.createChooser(shareIntent, null))
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(GlassOverlay.copy(alpha = 0.35f)),
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.settings_share),
                    tint = TextPrimary,
                )
            }
        }

        if (playingVideoId != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalInset, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                YouTubeInlinePlayer(
                    videoId = playingVideoId,
                    onEnded = viewModel::onYouTubeEnded,
                    onError = viewModel::onYouTubeFailed,
                    cornerRadius = 18.dp,
                    modifier = Modifier
                        .widthIn(max = 760.dp)
                        .heightIn(max = videoMaxHeight)
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(SurfaceBlue, RoundedCornerShape(18.dp)),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(contentScrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .responsiveContentWidth(760.dp)
                    .padding(horizontal = horizontalInset)
                    .padding(top = if (playingVideoId == null) 20.dp else 12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .background(PastelPurple.copy(alpha = 0.14f), RoundedCornerShape(Dimens.chipRadius))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        imageVector = if (isVideoBacked) Icons.Default.SmartDisplay else Icons.Default.Headset,
                        contentDescription = null,
                        tint = PastelPurple,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = stringResource(
                            if (isVideoBacked) R.string.podcard_type_video else R.string.podcard_type_podcast,
                        ).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = PastelPurple,
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = currentPodcast.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = if (layoutMetrics.isUltraCompactWidth) 24.sp else 28.sp,
                        lineHeight = if (layoutMetrics.isUltraCompactWidth) 30.sp else 34.sp,
                    ),
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )

                currentPodcast.subtitle?.takeIf { it.isNotBlank() }?.let { authorLine ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = authorLine,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (uiState.youtubeError) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceBlue, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(R.string.podcard_youtube_error),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                }

                if (playingVideoId == null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    val actionText = when {
                        isVideoBacked -> stringResource(R.string.podcard_watch)
                        uiState.isPlayingThisPodcast && uiState.playback.isPlaying ->
                            stringResource(R.string.now_playing_pause)
                        uiState.isPlayingThisPodcast -> stringResource(R.string.podcard_continue)
                        else -> stringResource(R.string.podcard_listen)
                    }
                    val actionIcon = when {
                        currentPodcast.isPremium -> Icons.Default.Lock
                        uiState.isPlayingThisPodcast && uiState.playback.isPlaying && !isVideoBacked ->
                            Icons.Default.Pause
                        else -> Icons.Default.PlayArrow
                    }

                    Row(
                        modifier = Modifier
                            .responsiveContentWidth(480.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(if (currentPodcast.isPremium) PremiumGold else PastelPurple)
                            .clickable {
                                if (currentPodcast.isPremium) {
                                    pendingUnlockPodcast = currentPodcast
                                } else {
                                    viewModel.adMobManager.onUserAction {
                                        viewModel.togglePlayback()
                                    }
                                }
                            }
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    ) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = null,
                            tint = MidnightBlue,
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = actionText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MidnightBlue,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            AdaptiveBannerAdBar(
                adMobManager = viewModel.adMobManager,
                slotKey = "podcard_detail_banner",
            )

            Column(
                modifier = Modifier
                    .responsiveContentWidth(760.dp)
                    .padding(horizontal = horizontalInset),
            ) {
                currentPodcast.description?.takeIf { it.isNotBlank() }?.let { summaryText ->
                    Spacer(modifier = Modifier.height(24.dp))
                    var isExpanded by rememberSaveable(summaryText) { mutableStateOf(false) }
                    var hasOverflow by rememberSaveable(summaryText) { mutableStateOf(false) }

                    Column(modifier = Modifier.animateContentSize()) {
                        Text(
                            text = summaryText,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = TextSecondary,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 4,
                            overflow = TextOverflow.Ellipsis,
                            onTextLayout = { layoutResult ->
                                if (!isExpanded) hasOverflow = layoutResult.hasVisualOverflow
                            },
                        )
                        if (hasOverflow || isExpanded) {
                            Text(
                                text = stringResource(
                                    if (isExpanded) R.string.podcard_read_less else R.string.podcard_read_more,
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = PastelPurple,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { isExpanded = !isExpanded }
                                    .padding(vertical = 6.dp),
                            )
                        }
                    }
                }
            }

            if (uiState.suggestions.isNotEmpty()) {
                val cardWidth = if (layoutMetrics.isTabletOrFoldable) 184.dp else 160.dp
                Spacer(modifier = Modifier.height(32.dp))
                Box(
                    modifier = Modifier
                        .responsiveContentWidth(760.dp)
                        .padding(horizontal = horizontalInset)
                        .padding(bottom = 12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.podcard_more_like_this),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.gridSpacing),
                    contentPadding = PaddingValues(horizontal = horizontalInset),
                    modifier = Modifier.responsiveContentWidth(760.dp),
                ) {
                    items(
                        items = uiState.suggestions,
                        key = { it.id },
                        contentType = { "related_podcard_item" },
                    ) { related ->
                        PodcardGridCard(
                            podcast = related,
                            onClick = {
                                if (related.isPremium) {
                                    pendingUnlockPodcast = related
                                } else {
                                    viewModel.adMobManager.onUserAction {
                                        viewModel.openRelatedPodcast(related.id, autoPlay = true)
                                    }
                                }
                            },
                            modifier = Modifier.width(cardWidth),
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier
                    .navigationBarsPadding()
                    .height(24.dp),
            )
        }
    }

    val targetToUnlock = pendingUnlockPodcast
    if (targetToUnlock != null) {
        RewardedUnlockBottomSheet(
            itemId = targetToUnlock.id,
            itemTitle = targetToUnlock.title,
            adMobManager = viewModel.adMobManager,
            onUnlocked = {
                val unlockedId = targetToUnlock.id
                pendingUnlockPodcast = null
                if (unlockedId == currentPodcast.id) {
                    viewModel.togglePlayback()
                } else {
                    viewModel.openRelatedPodcast(unlockedId, autoPlay = true)
                    onOpenRelated(unlockedId)
                }
            },
            onOpenPaywall = onOpenPaywall,
            onDismiss = { pendingUnlockPodcast = null },
        )
    }
}
