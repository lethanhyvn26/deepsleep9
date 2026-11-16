package com.sleepsounds.storytelling.ads

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleepsounds.storytelling.data.repository.UserPreferencesRepository
import com.sleepsounds.storytelling.ui.theme.DeepSleepBlue
import com.sleepsounds.storytelling.ui.theme.MidnightBlue
import com.sleepsounds.storytelling.ui.theme.PastelPurple
import com.sleepsounds.storytelling.ui.theme.PremiumGold
import com.sleepsounds.storytelling.ui.theme.SurfaceBlue
import com.sleepsounds.storytelling.ui.theme.SurfaceBlueLight
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.TextSecondary
import com.sleepsounds.storytelling.ui.theme.tr
import kotlinx.coroutines.delay

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight

/**
 * Formats remaining milliseconds into `HH:MM:SS` or `MM:SS` for the 3-Hour Ad-Free Pass countdown.
 */
fun formatRemainingDuration(remainingMs: Long): String {
    val totalSeconds = (remainingMs / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

/**
 * Card showing the user's progress toward watching 20 Rewarded Ads to unlock
 * 3 Hours of 100% Ad-Free & Full VIP Access, or a live countdown when active.
 *
 * - When [isInsidePaywall] is `false` (on Home and Profile screens): tapping anywhere on this
 *   card immediately opens the VIP Paywall & Rewarded Ad explanation screen ([onOpenPaywall]).
 * - When [isInsidePaywall] is `true` (inside [PaywallScreen]): shows the detailed step-by-step
 *   explanation of how Rewarded Ads work alongside the direct "Watch Ad (X/20)" button.
 */
@Composable
fun RewardedAdFreePassCard(
    adMobManager: AdMobManager,
    onOpenPaywall: () -> Unit,
    modifier: Modifier = Modifier,
    isInsidePaywall: Boolean = false,
) {
    val isPaidVip by adMobManager.isPaidVip.collectAsStateWithLifecycle()
    val watchCount by adMobManager.rewardedAdWatchCount.collectAsStateWithLifecycle()
    val adFreeUntilMs by adMobManager.rewardedAdFreeUntilMs.collectAsStateWithLifecycle()
    val isLoadingAd by adMobManager.isLoadingRewarded.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current ?: remember(context) { context.findActivity() }

    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(isInsidePaywall, watchCount, isPaidVip) {
        if (isInsidePaywall && !isPaidVip) {
            adMobManager.ensureRewardedAdLoaded()
        }
    }
    LaunchedEffect(adFreeUntilMs) {
        while (adFreeUntilMs > System.currentTimeMillis()) {
            nowMs = System.currentTimeMillis()
            delay(1_000L)
        }
        nowMs = System.currentTimeMillis()
    }

    if (isPaidVip) return

    val remainingMs = (adFreeUntilMs - nowMs).coerceAtLeast(0L)
    val isPassActive = remainingMs > 0L
    val targetCount = UserPreferencesRepository.REWARDED_VIDEOS_FOR_AD_FREE_PASS
    val progressFraction = if (isPassActive && watchCount == 0) {
        1f
    } else {
        (watchCount.toFloat() / targetCount.toFloat()).coerceIn(0f, 1f)
    }

    val toastPassUnlocked = tr(
        "Unlocked 3 Hours of Ad-Free & Full VIP Access!",
        "Desbloqueado 3 Horas Sem Anúncios e Acesso VIP Total!",
        "Đã mở khóa 3 Giờ Không Quảng Cáo & Toàn Bộ VIP!",
    )
    val toastStepEarned = tr(
        "Reward earned! Progress: %d/%d toward 3h Ad-Free & VIP",
        "Recompensa ganha! Progresso: %d/%d para 3h Sem Anúncios e VIP",
        "Đã nhận thưởng! Tiến độ: %d/%d để mở khóa 3h Không Quảng Cáo & VIP",
    )
    val toastAdNotReady = tr(
        "Ad is loading, please try again in a few seconds.",
        "O anúncio está carregando, tente novamente em alguns segundos.",
        "Quảng cáo đang tải, vui lòng thử lại sau vài giây.",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E295B),
                        Color(0xFF141B3D),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                color = if (isPassActive) Color(0xFF34D399).copy(alpha = 0.6f) else PremiumGold.copy(alpha = 0.45f),
                shape = RoundedCornerShape(20.dp),
            )
            .then(
                if (!isInsidePaywall) {
                    Modifier.clickable(onClick = onOpenPaywall)
                } else {
                    Modifier
                },
            )
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPassActive) {
                            Color(0xFF34D399).copy(alpha = 0.2f)
                        } else {
                            PremiumGold.copy(alpha = 0.2f)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isPassActive) Icons.Filled.CheckCircle else Icons.Filled.WorkspacePremium,
                    contentDescription = null,
                    tint = if (isPassActive) Color(0xFF34D399) else PremiumGold,
                    modifier = Modifier.size(24.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isPassActive) {
                        tr(
                            "3-Hour Ad-Free & VIP Active",
                            "3 Horas Sem Anúncios e VIP Ativo",
                            "Đang kích hoạt 3h Không Quảng Cáo & VIP",
                        )
                    } else {
                        tr(
                            "Free 3-Hour Ad-Free & VIP Pass",
                            "Passe Grátis de 3h Sem Anúncios e VIP",
                            "Mở Khóa 3 Giờ Không Quảng Cáo & VIP Miễn Phí",
                        )
                    },
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isPassActive) {
                        "${tr("Remaining time", "Tempo restante", "Thời gian còn lại")}: ${formatRemainingDuration(remainingMs)}"
                    } else {
                        "${tr("Watch 20 rewarded ads to remove all ads & unlock VIP for 3 hours", "Assista a 20 anúncios para remover anúncios e liberar VIP por 3 horas", "Xem 20 quảng cáo thưởng để xóa toàn bộ quảng cáo & mở khóa VIP trong 3 giờ")} ($watchCount/$targetCount)"
                    },
                    color = if (isPassActive) Color(0xFF34D399) else TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }

            if (!isInsidePaywall) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = PremiumGold,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (isPassActive) Color(0xFF34D399) else PremiumGold,
            trackColor = SurfaceBlueLight,
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (!isInsidePaywall) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PremiumGold.copy(alpha = 0.16f))
                    .border(1.dp, PremiumGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.WorkspacePremium,
                    contentDescription = null,
                    tint = PremiumGold,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${tr(
                        "Tap to View VIP Plans & Rewarded Ad Guide",
                        "Toque para Ver Planos VIP e Guia de Anúncios",
                        "Chạm để xem Bảng VIP & Giải thích Quảng Cáo Thưởng",
                    )} ($watchCount/$targetCount)",
                    color = PremiumGold,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = PremiumGold,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            val rules = listOf(
                tr(
                    "✓ Watch 20 rewarded ads: Unlock 3 hours of 100% Ad-Free (no Banner or Full Ads) & Full VIP access for free.",
                    "✓ Assista a 20 anúncios premiados: Desbloqueie 3 horas 100% Sem Anúncios e Acesso VIP Total grátis.",
                    "✓ Xem đủ 20 quảng cáo tặng thưởng: Mở khóa miễn phí 3 giờ Không Quảng Cáo (tắt toàn bộ Banner & Full Ads) + Toàn Bộ VIP.",
                ),
                tr(
                    "✓ Watch 1 ad on any locked item: Immediately unlock that VIP sound or story for 12 hours + earn +1 toward the 20-ad pass.",
                    "✓ Assista a 1 anúncio em qualquer item bloqueado: Libere esse som ou história VIP por 12 horas + ganhe +1 no passe de 20 anúncios.",
                    "✓ Xem 1 quảng cáo tại nội dung khóa: Mở khóa ngay âm thanh/truyện VIP đó trong 12 giờ + được cộng +1 vào mốc 20 quảng cáo.",
                ),
                tr(
                    "✓ Unlimited views & auto-saved progress: Accumulate anytime at your own pace and extend +3 hours endlessly.",
                    "✓ Visualizações ilimitadas e progresso salvo: Acumule no seu ritmo e estenda +3 horas sem limites.",
                    "✓ Không giới hạn lượt xem & tự động lưu tiến độ: Tích lũy bất kỳ lúc nào, xem tiếp để cộng dồn thêm +3 giờ liên tục.",
                ),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceBlue.copy(alpha = 0.72f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rules.forEach { ruleText ->
                    Text(
                        text = ruleText,
                        color = TextPrimary.copy(alpha = 0.92f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 17.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (!isLoadingAd) {
                        adMobManager.showRewardedAd(
                            activity = activity,
                            unlockItemId = null,
                            onRewardEarned = { activatedPass, updatedCount ->
                                val msg = if (activatedPass) {
                                    toastPassUnlocked
                                } else {
                                    toastStepEarned.format(updatedCount, targetCount)
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            onAdUnavailable = {
                                Toast.makeText(context, toastAdNotReady, Toast.LENGTH_SHORT).show()
                            },
                        )
                    }
                },
                enabled = !isLoadingAd,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepSleepBlue,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isLoadingAd) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(
                        imageVector = Icons.Filled.PlayCircleFilled,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = if (isPassActive) {
                        "${tr("Extend +3h", "Estender +3h", "Cộng thêm +3h")} ($watchCount/$targetCount)"
                    } else {
                        "${tr("Watch Ad", "Ver Anúncio", "Xem Quảng Cáo")} ($watchCount/$targetCount)"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * Bottom sheet shown when the user taps a locked VIP sound, scene, or bedtime story.
 * Gives the user 2 clear choices:
 * 1. Watch 1 short Rewarded Ad -> unlocks this item immediately for 12 hours AND adds +1 toward
 *    the 20-Ad 3-Hour Ad-Free & Full VIP Pass (`X/20`).
 * 2. Upgrade to VIP -> opens the Google Play Billing Paywall screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardedUnlockBottomSheet(
    itemId: String,
    itemTitle: String,
    adMobManager: AdMobManager,
    onUnlocked: () -> Unit,
    onOpenPaywall: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val watchCount by adMobManager.rewardedAdWatchCount.collectAsStateWithLifecycle()
    val isLoadingAd by adMobManager.isLoadingRewarded.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current ?: remember(context) { context.findActivity() }
    val targetCount = UserPreferencesRepository.REWARDED_VIDEOS_FOR_AD_FREE_PASS

    LaunchedEffect(itemId, watchCount) {
        adMobManager.ensureRewardedAdLoaded()
    }

    val toastPassUnlocked = tr(
        "Unlocked 3 Hours of Ad-Free & Full VIP Access!",
        "Desbloqueado 3 Horas Sem Anúncios e Acesso VIP Total!",
        "Đã mở khóa 3 Giờ Không Quảng Cáo & Toàn Bộ VIP!",
    )
    val toastItemUnlocked = tr(
        "Unlocked \"%s\" for 12h! (%d/%d toward 3h Ad-Free)",
        "\"%s\" desbloqueado por 12h! (%d/%d para 3h Sem Anúncios)",
        "Đã mở khóa \"%s\" trong 12h! (%d/%d tiến tới 3h Không Quảng Cáo)",
    )
    val toastAdNotReady = tr(
        "Ad is loading, please try again in a few seconds.",
        "O anúncio está carregando, tente novamente em alguns segundos.",
        "Quảng cáo đang tải, vui lòng thử lại sau vài giây.",
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MidnightBlue,
        contentColor = TextPrimary,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(PremiumGold.copy(alpha = 0.18f))
                    .border(1.dp, PremiumGold.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.LockOpen,
                    contentDescription = null,
                    tint = PremiumGold,
                    modifier = Modifier.size(30.dp),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = itemTitle,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = tr(
                    "Watch 1 short video to unlock this item for 12 hours, or collect 20 rewarded ads to unlock 3 hours of 100% Ad-Free & Full VIP access.",
                    "Assista a 1 vídeo curto para desbloquear este item por 12 horas, ou acumule 20 anúncios premiados para ganhar 3 horas Sem Anúncios e VIP Total.",
                    "Xem 1 quảng cáo ngắn để mở khóa mục này trong 12 giờ, hoặc tích lũy đủ 20 quảng cáo thưởng để mở khóa 3 giờ Không Quảng Cáo & Toàn Bộ VIP.",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceBlue.copy(alpha = 0.9f))
                    .padding(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = tr(
                            "3-Hour Ad-Free & VIP Progress",
                            "Progresso para 3h Sem Anúncios e VIP",
                            "Tiến độ mở khóa 3h Không Quảng Cáo & VIP",
                        ),
                        color = PastelPurple,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "$watchCount / $targetCount",
                        color = PremiumGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (watchCount.toFloat() / targetCount.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PremiumGold,
                    trackColor = SurfaceBlueLight,
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (!isLoadingAd) {
                        adMobManager.showRewardedAd(
                            activity = activity,
                            unlockItemId = itemId,
                            onRewardEarned = { activatedPass, updatedCount ->
                                val msg = if (activatedPass) {
                                    toastPassUnlocked
                                } else {
                                    toastItemUnlocked.format(itemTitle, updatedCount, targetCount)
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                onUnlocked()
                            },
                            onAdUnavailable = {
                                Toast.makeText(context, toastAdNotReady, Toast.LENGTH_SHORT).show()
                            },
                        )
                    }
                },
                enabled = !isLoadingAd,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepSleepBlue,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (isLoadingAd) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                } else {
                    Icon(
                        imageVector = Icons.Filled.PlayCircleFilled,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = tr(
                        "Watch 1 Ad to Unlock Now",
                        "Assistir 1 Anúncio para Desbloquear",
                        "Xem 1 Quảng Cáo Để Mở Khóa Ngay",
                    ),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    onDismiss()
                    onOpenPaywall()
                },
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PremiumGold.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.WorkspacePremium,
                    contentDescription = null,
                    tint = PremiumGold,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tr(
                        "Upgrade to Somni VIP (No Ads Forever)",
                        "Assinar Somni VIP (Sem Anúncios)",
                        "Nâng cấp gói Somni VIP (Xóa Quảng Cáo Vĩnh Viễn)",
                    ),
                    color = PremiumGold,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}
