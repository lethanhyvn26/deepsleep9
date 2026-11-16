package com.sleepsounds.storytelling.presentation.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleepsounds.storytelling.BuildConfig
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.ads.AdaptiveBannerAdBar
import com.sleepsounds.storytelling.ads.RewardedAdFreePassCard
import com.sleepsounds.storytelling.data.repository.SleepProfileStats
import com.sleepsounds.storytelling.presentation.components.ScreenBackground
import com.sleepsounds.storytelling.ui.theme.DeepSleepBlue
import com.sleepsounds.storytelling.ui.theme.GlassSurface
import com.sleepsounds.storytelling.ui.theme.MidnightBlue
import com.sleepsounds.storytelling.ui.theme.PastelPurple
import com.sleepsounds.storytelling.ui.theme.PremiumGold
import com.sleepsounds.storytelling.ui.theme.SUPPORTED_APP_LANGUAGES
import com.sleepsounds.storytelling.ui.theme.SurfaceBlue
import com.sleepsounds.storytelling.ui.theme.SurfaceBlueLight
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.TextSecondary
import com.sleepsounds.storytelling.ui.theme.rememberResponsiveLayoutInfo
import com.sleepsounds.storytelling.ui.theme.responsiveContentWidth
import com.sleepsounds.storytelling.ui.theme.tr

@Composable
private fun SettingsItemRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    val layoutInfo = rememberResponsiveLayoutInfo()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = layoutInfo.screenHorizontalPadding, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GlassSurface),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
        trailing?.invoke()
    }
}

@Composable
private fun SettingsSectionHeading(text: String) {
    val layoutInfo = rememberResponsiveLayoutInfo()
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = TextSecondary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(
            start = layoutInfo.screenHorizontalPadding,
            end = layoutInfo.screenHorizontalPadding,
            top = 20.dp,
            bottom = 4.dp,
        ),
    )
}

@Composable
private fun SleepStatColumn(
    value: String,
    label: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.5.sp,
        )
    }
}

@Composable
private fun SleepMetricsSummaryCard(
    stats: SleepProfileStats,
    onOpenJournal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceBlue.copy(alpha = 0.92f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
            .clickable(onClick = onOpenJournal)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(DeepSleepBlue, PastelPurple))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.GraphicEq,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tr("My Sleep Profile", "Meu Perfil de Sono", "Hồ sơ giấc ngủ của tôi"),
                    color = TextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tr(
                        "Ready to analyze your sleep cycles",
                        "Pronto para analisar seus ciclos de sono",
                        "Sẵn sàng phân tích chu kỳ giấc ngủ của bạn",
                    ),
                    color = TextSecondary,
                    fontSize = 12.5.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceBlueLight.copy(alpha = 0.72f))
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SleepStatColumn(
                value = stats.averageQualityScore?.let { "$it%" }
                    ?: tr("Calibrating", "Calibrando", "Đang đo"),
                label = tr("Avg Quality", "Qualidade Média", "Chất lượng TB"),
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(Color.White.copy(alpha = 0.1f)),
            )
            SleepStatColumn(
                value = "${stats.nightsCount} ${tr("Nights", "Noites", "Đêm")}",
                label = tr("Tracked", "Monitoradas", "Đã theo dõi"),
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(Color.White.copy(alpha = 0.1f)),
            )
            SleepStatColumn(
                value = stats.formattedAverageDuration,
                label = tr("Avg Duration", "Duração Média", "Thời lượng TB"),
            )
        }
    }
}

@Composable
fun ProfileScreen(
    onNavigateToBreathing: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToTracker: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    onNavigateToDreamDictionary: () -> Unit = {},
    onNavigateToProducts: () -> Unit = {},
    onNavigateToPaywall: () -> Unit = {},
    onReplayOnboarding: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    var showLanguagePickerSheet by remember { mutableStateOf(false) }
    val activeLanguageItem = remember(currentLanguage) {
        SUPPORTED_APP_LANGUAGES.firstOrNull { it.code == currentLanguage }
            ?: SUPPORTED_APP_LANGUAGES.first()
    }
    val context = LocalContext.current
    val layoutInfo = rememberResponsiveLayoutInfo()
    val shareText = stringResource(R.string.settings_share_text)

    fun openPlayStoreListing() {
        val pkg = context.packageName
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$pkg".toUri()))
        } catch (_: ActivityNotFoundException) {
            runCatching {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        "https://play.google.com/store/apps/details?id=$pkg".toUri(),
                    ),
                )
            }
        }
    }

    ScreenBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier.responsiveContentWidth(720.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item(key = "sleep_profile_summary") {
                    SleepMetricsSummaryCard(
                        stats = state.sleepStats,
                        onOpenJournal = onNavigateToJournal,
                        modifier = Modifier.padding(
                            horizontal = layoutInfo.screenHorizontalPadding,
                            vertical = 12.dp,
                        ),
                    )
                }

                item(key = "profile_inline_banner") {
                    AdaptiveBannerAdBar(
                        adMobManager = viewModel.adMobManager,
                        slotKey = "profile_tab_banner",
                    )
                }

                item(key = "rewarded_or_vip_card") {
                    RewardedAdFreePassCard(
                        adMobManager = viewModel.adMobManager,
                        onOpenPaywall = onNavigateToPaywall,
                        modifier = Modifier.padding(
                            horizontal = layoutInfo.screenHorizontalPadding,
                            vertical = 6.dp,
                        ),
                    )
                }

                item(key = "row_vip_membership") {
                    SettingsItemRow(
                        icon = { Icon(Icons.Default.WorkspacePremium, null, tint = PremiumGold) },
                        title = if (state.isPaidSubscriber) {
                            tr("Somni VIP Active", "Somni VIP Ativo", "Đã kích hoạt Somni VIP")
                        } else {
                            tr("Somni VIP Membership", "Assinatura Somni VIP", "Gói Thành Viên Somni VIP")
                        },
                        subtitle = if (state.isPaidSubscriber) {
                            tr("All sounds, stories & ad-free mode unlocked", "Todos os sons, histórias e modo sem anúncios ativos", "Đã mở khóa toàn bộ âm thanh, truyện & xóa quảng cáo")
                        } else {
                            tr("Unlock all sounds, stories & remove ads forever", "Desbloqueie tudo e remova anúncios para sempre", "Mở khóa toàn bộ âm thanh, truyện & xóa quảng cáo vĩnh viễn")
                        },
                        onClick = onNavigateToPaywall,
                        trailing = {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = PremiumGold,
                            )
                        },
                    )
                }

                item(key = "section_alarm") {
                    SettingsSectionHeading(
                        tr(
                            "Smart Alarm & Sleep Tracking",
                            "Alarme Inteligente & Monitoramento",
                            "Báo thức thông minh & Theo dõi ngủ",
                        ),
                    )
                }
                item(key = "row_alarm") {
                    SettingsItemRow(
                        icon = { Icon(Icons.Default.AccessAlarm, null, tint = DeepSleepBlue) },
                        title = "${stringResource(R.string.tracker_wake_up_alarm)} · ${state.primaryAlarm.formattedTime}",
                        subtitle = if (state.primaryAlarm.smartWindowMinutes > 0) {
                            "${tr("Smart wake window", "Janela inteligente", "Đánh thức thông minh")}: ${state.primaryAlarm.smartWindowMinutes} min"
                        } else {
                            tr("Exact wake-up alarm", "Alarme no horário exato", "Báo thức đúng giờ")
                        },
                        onClick = onNavigateToTracker,
                        trailing = {
                            Switch(
                                checked = state.primaryAlarm.enabled,
                                onCheckedChange = viewModel::toggleAlarmEnabled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = DeepSleepBlue,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = SurfaceBlueLight,
                                ),
                            )
                        },
                    )
                }
                item(key = "row_journal") {
                    SettingsItemRow(
                        icon = { Icon(Icons.Default.Assessment, null, tint = PastelPurple) },
                        title = tr("Sleep Journal & Reports", "Diário e Relatórios de Sono", "Nhật ký & Báo cáo giấc ngủ"),
                        subtitle = tr("View hypnogram stages, sleep score & snore logs", "Veja fases do sono, pontuação e registros de ronco", "Xem biểu đồ giấc ngủ, điểm số & bản ghi tiếng ngáy"),
                        onClick = onNavigateToJournal,
                        trailing = {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = TextSecondary,
                            )
                        },
                    )
                }

                item(key = "section_tools") {
                    SettingsSectionHeading(stringResource(R.string.settings_section_tools))
                }
                item(key = "row_dream") {
                    SettingsItemRow(
                        icon = { Icon(Icons.Default.AutoAwesome, null, tint = PastelPurple) },
                        title = stringResource(R.string.dream_analysis_title),
                        subtitle = stringResource(R.string.dream_analysis_subtitle),
                        onClick = onNavigateToDreamDictionary,
                        trailing = {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = TextSecondary,
                            )
                        },
                    )
                }
                item(key = "row_breathing") {
                    SettingsItemRow(
                        icon = { Icon(Icons.Default.Air, null, tint = PastelPurple) },
                        title = stringResource(R.string.settings_breathing_title),
                        subtitle = stringResource(R.string.settings_breathing_desc),
                        onClick = onNavigateToBreathing,
                        trailing = {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = TextSecondary,
                            )
                        },
                    )
                }
                item(key = "row_quiz") {
                    SettingsItemRow(
                        icon = { Icon(Icons.Default.Bedtime, null, tint = PastelPurple) },
                        title = tr("Sleep Quiz & Personalized Report", "Avaliação de Sono e Relatório", "Khảo sát & Báo cáo giấc ngủ"),
                        subtitle = tr("Personalized sleep habits & nightly routine check", "Avaliação de hábitos de sono e rotina noturna", "Đánh giá thói quen & lịch trình giấc ngủ cá nhân"),
                        onClick = onReplayOnboarding,
                        trailing = {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = TextSecondary,
                            )
                        },
                    )
                }
                item(key = "row_shop") {
                    SettingsItemRow(
                        icon = { Icon(Icons.Default.ShoppingBag, null, tint = PremiumGold) },
                        title = stringResource(R.string.products_title),
                        subtitle = tr("Curated pillows, sleep masks & relaxation essentials", "Travesseiros, máscaras de dormir e itens de relaxamento", "Gối ngủ, bịt mắt & phụ kiện chăm sóc giấc ngủ chọn lọc"),
                        onClick = onNavigateToProducts,
                        trailing = {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = TextSecondary,
                            )
                        },
                    )
                }

                item(key = "section_language") {
                    SettingsSectionHeading(stringResource(R.string.settings_language_title))
                }
                item(key = "row_language_selector") {
                    SettingsItemRow(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = PastelPurple,
                            )
                        },
                        title = stringResource(R.string.settings_language_title),
                        subtitle = tr(
                            "Tap to choose from 13 supported languages",
                            "Toque para escolher entre 13 idiomas suportados",
                            "Nhấn để chọn trong danh sách 13 ngôn ngữ hỗ trợ",
                        ),
                        onClick = { showLanguagePickerSheet = true },
                        trailing = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceBlueLight.copy(alpha = 0.85f))
                                        .border(
                                            width = 1.dp,
                                            color = PastelPurple.copy(alpha = 0.45f),
                                            shape = RoundedCornerShape(12.dp),
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(text = activeLanguageItem.flagEmoji, fontSize = 15.sp)
                                    Text(
                                        text = activeLanguageItem.nativeName,
                                        color = TextPrimary,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                )
                            }
                        },
                    )
                }

                item(key = "section_library") {
                    SettingsSectionHeading(stringResource(R.string.settings_section_account))
                }
                item(key = "row_downloads") {
                    SettingsItemRow(
                        icon = { Icon(Icons.Default.CloudDone, null, tint = PastelPurple) },
                        title = stringResource(R.string.profile_downloads),
                        subtitle = stringResource(
                            R.string.settings_downloads_desc,
                            state.downloadedCount,
                        ),
                        onClick = {},
                    )
                }

                item(key = "section_support") {
                    SettingsSectionHeading(stringResource(R.string.settings_section_support))
                }
                item(key = "row_rate") {
                    SettingsItemRow(
                        icon = { Icon(Icons.Default.StarRate, null, tint = PremiumGold) },
                        title = stringResource(R.string.settings_rate),
                        subtitle = stringResource(R.string.settings_rate_desc),
                        onClick = ::openPlayStoreListing,
                    )
                }
                item(key = "row_share") {
                    SettingsItemRow(
                        icon = { Icon(Icons.Default.Share, null, tint = PastelPurple) },
                        title = stringResource(R.string.settings_share),
                        subtitle = stringResource(R.string.settings_share_desc),
                        onClick = {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            runCatching {
                                context.startActivity(Intent.createChooser(send, null))
                            }
                        },
                    )
                }
                item(key = "row_terms") {
                    SettingsItemRow(
                        icon = { Icon(Icons.Default.Description, null, tint = TextSecondary) },
                        title = stringResource(R.string.settings_terms),
                        subtitle = "",
                        onClick = onNavigateToTerms,
                    )
                }
                item(key = "row_version") {
                    SettingsItemRow(
                        icon = { Icon(Icons.Default.Info, null, tint = TextPrimary) },
                        title = stringResource(R.string.app_name),
                        subtitle = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                        onClick = {},
                    )
                }
            }
        }
    }

    if (showLanguagePickerSheet) {
        LanguagePickerBottomSheet(
            currentLanguageCode = currentLanguage,
            onSelectLanguage = { selectedCode ->
                viewModel.setAppLanguage(selectedCode)
                showLanguagePickerSheet = false
            },
            onDismiss = { showLanguagePickerSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagePickerBottomSheet(
    currentLanguageCode: String,
    onSelectLanguage: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DeepSleepBlue.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = PastelPurple,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.settings_language_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Text(
                            text = tr(
                                "Select display language",
                                "Selecione o idioma do aplicativo",
                                "Chọn ngôn ngữ hiển thị",
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SurfaceBlueLight.copy(alpha = 0.8f)),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 12.dp),
            ) {
                items(SUPPORTED_APP_LANGUAGES, key = { it.code }) { lang ->
                    val isSelected = currentLanguageCode == lang.code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF23326A), Color(0xFF1A2550)),
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(SurfaceBlue.copy(alpha = 0.88f), SurfaceBlue.copy(alpha = 0.88f)),
                                    )
                                },
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) PastelPurple else Color.White.copy(alpha = 0.07f),
                                shape = RoundedCornerShape(16.dp),
                            )
                            .clickable { onSelectLanguage(lang.code) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SurfaceBlueLight.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = lang.flagEmoji, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = lang.nativeName,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            )
                            Text(
                                text = lang.englishSubtitle,
                                color = TextSecondary,
                                fontSize = 12.sp,
                            )
                        }
                        Icon(
                            imageVector = if (isSelected) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.Default.RadioButtonUnchecked
                            },
                            contentDescription = null,
                            tint = if (isSelected) PremiumGold else TextSecondary.copy(alpha = 0.45f),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }
}

