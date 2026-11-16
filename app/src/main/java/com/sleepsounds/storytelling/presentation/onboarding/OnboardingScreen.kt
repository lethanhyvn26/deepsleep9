package com.sleepsounds.storytelling.presentation.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.runtime.key
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.ads.AdConfigManager
import com.sleepsounds.storytelling.ads.AdaptiveBannerAdBar
import com.sleepsounds.storytelling.ads.findActivity
import com.sleepsounds.storytelling.ads.preloadBannerChannel
import com.sleepsounds.storytelling.ads.rememberAdMobManager
import com.sleepsounds.storytelling.data.repository.UserPreferencesRepository
import com.sleepsounds.storytelling.ui.theme.ImmersiveSystemBars
import com.sleepsounds.storytelling.ui.theme.MidnightBlue
import com.sleepsounds.storytelling.ui.theme.systemSafePadding
import com.sleepsounds.storytelling.ui.theme.PastelPurple
import com.sleepsounds.storytelling.ui.theme.PremiumGold
import com.sleepsounds.storytelling.ui.theme.SUPPORTED_APP_LANGUAGES
import com.sleepsounds.storytelling.ui.theme.DeepSleepBlue
import com.sleepsounds.storytelling.ui.theme.StageAwakeOrange
import com.sleepsounds.storytelling.ui.theme.StageFellAsleepTeal
import com.sleepsounds.storytelling.ui.theme.StageLightPurple
import com.sleepsounds.storytelling.ui.theme.SurfaceBlue
import com.sleepsounds.storytelling.ui.theme.SurfaceBlueLight
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.TextSecondary
import com.sleepsounds.storytelling.ui.theme.responsiveContentWidth
import com.sleepsounds.storytelling.ui.theme.tr
import kotlinx.coroutines.delay
import kotlin.math.sin

private val sleepAssessmentQuestions = listOf(
    SleepAssessmentQuestion(
        titleEn = "How much sleep do you usually get at night?",
        titlePtBr = "Quantas horas você costuma dormir por noite?",
        titleVi = "Bạn thường ngủ bao nhiêu tiếng mỗi đêm?",
        options = listOf(
            SleepAssessmentOption("🕕", "6 hours or less", "6 horas ou menos", "6 tiếng hoặc ít hơn"),
            SleepAssessmentOption("🕖", "6–8 hours", "6–8 horas", "6–8 tiếng"),
            SleepAssessmentOption("🕗", "8–10 hours", "8–10 horas", "8–10 tiếng"),
            SleepAssessmentOption("🕙", "More than 10 hours", "Mais de 10 horas", "Hơn 10 tiếng"),
        ),
    ),
    SleepAssessmentQuestion(
        titleEn = "How satisfied are you with your sleep?",
        titlePtBr = "Qual é o seu nível de satisfação com o seu sono?",
        titleVi = "Bạn hài lòng với giấc ngủ của mình ở mức nào?",
        options = listOf(
            SleepAssessmentOption("😊", "Very satisfied", "Muito satisfeito", "Rất hài lòng"),
            SleepAssessmentOption("🙂", "Neutral", "Neutro", "Bình thường"),
            SleepAssessmentOption("😐", "Unsatisfied", "Insatisfeito", "Chưa hài lòng"),
            SleepAssessmentOption("😫", "Very Unsatisfied", "Muito insatisfeito", "Rất mệt mỏi / Mất ngủ"),
        ),
    ),
    SleepAssessmentQuestion(
        titleEn = "How long after getting in bed do you usually fall asleep?",
        titlePtBr = "Quanto tempo você leva para adormecer ao deitar?",
        titleVi = "Bạn thường mất bao lâu để chìm vào giấc ngủ?",
        options = listOf(
            SleepAssessmentOption("🔔", "Immediately", "Imediatamente", "Ngủ ngay lập tức"),
            SleepAssessmentOption("🕒", "10–15 minutes", "10–15 minutos", "10–15 phút"),
            SleepAssessmentOption("🕓", "20–40 minutes", "20–40 minutos", "20–40 phút"),
            SleepAssessmentOption("😔", "Hard to fall asleep", "Demoro muito para dormir", "Rất khó ngủ"),
        ),
    ),
    SleepAssessmentQuestion(
        titleEn = "Do you snore or talk in your sleep at night?",
        titlePtBr = "Você ronca ou fala dormindo à noite?",
        titleVi = "Bạn có ngáy hoặc nói mớ khi ngủ vào ban đêm không?",
        options = listOf(
            SleepAssessmentOption("🤔", "I don't know", "Não sei", "Tôi không rõ"),
            SleepAssessmentOption("😌", "No, I don't snore", "Não ronco", "Không, tôi không ngáy"),
            SleepAssessmentOption("😴", "Yes, sometimes", "Sim, às vezes", "Có, thỉnh thoảng"),
            SleepAssessmentOption("🔊", "Often / told by partner", "Com frequência", "Thường xuyên"),
        ),
    ),
    SleepAssessmentQuestion(
        titleEn = "Does lack of sleep affect your daily life?",
        titlePtBr = "A falta de sono afeta o seu dia a dia?",
        titleVi = "Thiếu ngủ có ảnh hưởng đến sinh hoạt hàng ngày của bạn không?",
        options = listOf(
            SleepAssessmentOption("✋", "Not at all", "De jeito nenhum", "Không ảnh hưởng"),
            SleepAssessmentOption("👆", "A little", "Um pouco", "Một chút"),
            SleepAssessmentOption("😥", "Somewhat", "Moderadamente", "Khá nhiều"),
            SleepAssessmentOption("👎", "Very much", "Muito", "Ảnh hưởng rất nhiều"),
        ),
    ),
)
@Composable
private fun OnboardingLanguagePickerPage(
    selectedLanguageCode: String,
    onSelectLanguage: (String) -> Unit,
    onConfirmLanguage: () -> Unit,
) {
    val displayLanguages = remember {
        SUPPORTED_APP_LANGUAGES.map { lang ->
            Triple(lang.code, lang.flagEmoji, lang.nativeName to lang.englishSubtitle)
        }
    }

    var currentSelection by remember(selectedLanguageCode) {
        mutableStateOf(UserPreferencesRepository.normalizeLanguage(selectedLanguageCode))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .responsiveContentWidth(600.dp)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.language_select_title),
                        color = TextPrimary,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = stringResource(R.string.language_select_subtitle),
                        color = TextSecondary,
                        fontSize = 12.5.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(DeepSleepBlue, Color(0xFF3B66FF)),
                            ),
                        )
                        .clickable(onClick = onConfirmLanguage)
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.language_continue),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                displayLanguages.forEach { (code, flag, names) ->
                    val isSelected = currentSelection == code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) {
                                    DeepSleepBlue.copy(alpha = 0.22f)
                                } else {
                                    SurfaceBlue.copy(alpha = 0.85f)
                                },
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) DeepSleepBlue else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(16.dp),
                            )
                            .clickable {
                                currentSelection = code
                                onSelectLanguage(code)
                            }
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = flag, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = names.first,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            )
                            Text(
                                text = names.second,
                                color = TextSecondary,
                                fontSize = 12.sp,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) DeepSleepBlue else Color.Transparent)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) DeepSleepBlue else TextSecondary.copy(alpha = 0.5f),
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun OnboardingCompanionAvatar(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(DeepSleepBlue, PastelPurple),
                ),
            )
            .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.SmartToy,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp),
        )
    }
}
@Composable
private fun OnboardingHighlightStatBadge(
    headline: String,
    subline: String,
    rotationDeg: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .rotate(rotationDeg)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFF5D996), Color(0xFFE5BE6E)),
                ),
            )
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = headline,
            color = Color(0xFF3A2A0D),
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = subline,
            color = Color(0xFF5A4319),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
@Composable
private fun OnboardingForwardActionCircle(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(62.dp)
            .clip(CircleShape)
            .background(DeepSleepBlue)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Next",
            tint = Color.White,
            modifier = Modifier.size(28.dp),
        )
    }
}
@Composable
private fun OnboardingSoundscapeFeaturePage(
    onNext: () -> Unit,
) {
    val sampleSounds = remember {
        listOf(
            "🌧️" to "Rain",
            "🌀" to "Soft Fan",
            "🔥" to "Campfire",
            "🌊" to "Ocean Waves",
            "🍃" to "Leaves",
            "⛺" to "Rain on Tent",
            "🦗" to "Crickets",
            "🎹" to "Soft Piano",
            "🧘" to "Meditation",
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .responsiveContentWidth(560.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = tr("Best Sound Machine", "Melhor Máquina de Sons", "Kho Âm Thanh Ngủ Tuyệt Đỉnh"),
                color = PremiumGold,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tr("Customized white noise\nhelps you fall asleep faster", "Ruído branco personalizado\najuda você a dormir mais rápido", "Tiếng ồn trắng tùy chỉnh\ngiúp bạn dễ ngủ nhanh hơn"),
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 31.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF101730))
                    .border(2.dp, DeepSleepBlue.copy(alpha = 0.45f), RoundedCornerShape(28.dp))
                    .padding(horizontal = 16.dp, vertical = 20.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    listOf("All", "Rain", "ASMR", "Nature", "Melody").forEachIndexed { idx, tab ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (idx == 0) DeepSleepBlue else SurfaceBlueLight)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = tr(tab),
                                color = if (idx == 0) Color.White else TextSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                sampleSounds.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        row.forEach { (emoji, name) ->
                            val active = name == "Rain" || name == "Campfire" || name == "Soft Piano"
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(if (active) DeepSleepBlue else SurfaceBlueLight)
                                        .border(
                                            width = 1.dp,
                                            color = if (active) Color.White.copy(alpha = 0.7f) else Color.Transparent,
                                            shape = CircleShape,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(text = emoji, fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = tr(name),
                                    color = if (active) TextPrimary else TextSecondary,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(18.dp))

            OnboardingForwardActionCircle(onClick = onNext)
        }
    }
}
@Composable
private fun OnboardingSleepCycleFeaturePage(
    onNext: () -> Unit,
) {
    val sleepQualityLabel = tr("Sleep Quality", "Qualidade do Sono", "Chất lượng ngủ")
    val cards = listOf(
        Triple(tr("Apnea Risk", "Risco de Apneia", "Nhịp thở đêm"), tr("Track breathing & nighttime disturbances", "Monitore a respiração e interrupções", "Theo dõi nhịp thở & gián đoạn ban đêm"), StageFellAsleepTeal),
        Triple(tr("Sleep Stages", "Fases do Sono", "Chu kỳ ngủ"), tr("Deep 32% · Light 54% · REM 14%", "Profundo 32% · Leve 54% · REM 14%", "Ngủ sâu 32% · Ngủ nông 54% · REM 14%"), DeepSleepBlue),
        Triple(tr("Snoring", "Ronco", "Tiếng ngáy"), tr("Max 76 dB · Duration & audio clips", "Máx 76 dB · Duração e gravações", "Tối đa 76 dB · Thời lượng & bản ghi"), StageAwakeOrange),
        Triple(tr("Trends", "Tendências", "Xu hướng"), tr("Weekly & monthly sleep quality charts", "Gráficos semanais e mensais", "Biểu đồ chất lượng ngủ tuần & tháng"), StageLightPurple),
        Triple(tr("Smart Alarm", "Alarme Inteligente", "Báo thức thông minh"), tr("Wake up gently in light sleep phase", "Acorde suavemente na fase leve", "Đánh thức nhẹ nhàng trong pha ngủ nông"), DeepSleepBlue),
        Triple(sleepQualityLabel, tr("Score 87 · Optimal recovery analysis", "Nota 87 · Análise de recuperação", "Điểm 87 · Phân tích phục hồi tối ưu"), Color(0xFFEC4899)),
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .responsiveContentWidth(560.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = tr("Powerful Sleep Tracker", "Monitor de Sono Avançado", "Theo Dõi Giấc Ngủ Chuyên Sâu"),
                color = PremiumGold,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tr("Understand your sleep\nbetter than ever", "Entenda o seu sono\nmelhor do que nunca", "Thấu hiểu giấc ngủ của bạn\nhơn bao giờ hết"),
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 31.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            cards.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max)
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    pair.forEach { (title, desc, accent) ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF141B34))
                                .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                                .padding(14.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(accent.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (title == sleepQualityLabel) {
                                    Text(
                                        text = "87",
                                        color = accent,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                    )
                                } else {
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp)
                                            .height(18.dp),
                                    ) {
                                        val path = Path().apply {
                                            moveTo(0f, size.height * 0.7f)
                                            cubicTo(
                                                size.width * 0.3f,
                                                size.height * 0.1f,
                                                size.width * 0.65f,
                                                size.height * 0.9f,
                                                size.width,
                                                size.height * 0.25f,
                                            )
                                        }
                                        drawPath(
                                            path = path,
                                            color = accent,
                                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = title,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = desc,
                                color = TextSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp,
                                minLines = 2,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(18.dp))

            OnboardingForwardActionCircle(onClick = onNext)
        }
    }
}
@Composable
private fun OnboardingAudioDetectionFeaturePage(
    onNext: () -> Unit,
) {
    val events = listOf(
        Triple("😴", tr("You Snored", "Você Roncou", "Bạn đã ngáy"), "11:26 pm"),
        Triple("💬", tr("You Talked", "Você Falou", "Bạn đã nói mớ"), "00:23 am"),
        Triple("🌬️", tr("Deep Breathing", "Respiração Profunda", "Thở sâu êm dịu"), "02:14 am"),
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .responsiveContentWidth(560.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = tr("Accurate Sleep Recorder", "Gravador de Sono Preciso", "Ghi Âm Giấc Ngủ Chính Xác"),
                color = PremiumGold,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tr("Find out what you did in\nyour sleep", "Descubra o que você fez\nenquanto dormia", "Khám phá những gì diễn ra\ntrong lúc bạn ngủ"),
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 31.sp,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                events.forEachIndexed { idx, (emoji, title, time) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF151C36))
                            .border(
                                width = if (idx == 0) 1.5.dp else 1.dp,
                                color = if (idx == 0) DeepSleepBlue.copy(alpha = 0.65f)
                                else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(20.dp),
                            )
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(DeepSleepBlue.copy(alpha = 0.35f), SurfaceBlueLight),
                                    ),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = emoji, fontSize = 26.sp)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = time,
                                color = TextSecondary,
                                fontSize = 12.sp,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(22.dp),
                            ) {
                                val bars = 28
                                val barW = 3.dp.toPx()
                                val gap = (size.width - bars * barW) / (bars - 1).coerceAtLeast(1)
                                for (i in 0 until bars) {
                                    val ratio = (0.25f + 0.7f * kotlin.math.abs(sin((i + idx * 4) * 0.45f)))
                                        .coerceIn(0.2f, 1f)
                                    val bh = size.height * ratio
                                    drawRoundRect(
                                        color = if (idx == 0) DeepSleepBlue else PastelPurple.copy(alpha = 0.7f),
                                        topLeft = Offset(i * (barW + gap), (size.height - bh) / 2f),
                                        size = Size(barW, bh),
                                        cornerRadius = CornerRadius(barW / 2f, barW / 2f),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(20.dp))

            OnboardingForwardActionCircle(onClick = onNext)
        }
    }
}
@Composable
private fun OnboardingReportGenerationPage(
    onFinishedLoading: () -> Unit,
) {
    var targetPercent by remember { mutableIntStateOf(0) }
    val animatedPercent by animateIntAsState(
        targetValue = targetPercent,
        animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
        label = "report_loading_percent",
    )

    LaunchedEffect(Unit) {
        delay(120L)
        targetPercent = 100
        delay(1950L)
        onFinishedLoading()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .responsiveContentWidth(560.dp)
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OnboardingCompanionAvatar()

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = tr("Creating your sleep report", "Criando seu relatório de sono", "Đang tạo báo cáo giấc ngủ của bạn"),
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(34.dp))

            Box(
                modifier = Modifier.size(136.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 8.dp.toPx()
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    val topLeft = Offset(stroke / 2f, stroke / 2f)

                    drawArc(
                        color = Color.White.copy(alpha = 0.10f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )

                    drawArc(
                        color = DeepSleepBlue,
                        startAngle = -90f,
                        sweepAngle = 360f * (animatedPercent / 100f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                }

                Text(
                    text = "$animatedPercent%",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            if (animatedPercent < 50) {
                Text(
                    text = tr("10+ Million Users", "10+ Milhões de Usuários", "Hơn 10 Triệu Người Dùng"),
                    color = Color(0xFF7B9CFF),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tr("have chosen our sleep soundscapes & tracker", "escolheram nossos sons de sono e monitor", "đã tin chọn âm thanh & bộ theo dõi giấc ngủ của chúng tôi"),
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            } else {
                Text(
                    text = tr("#1 Sleep App", "App de Sono Nº 1", "Ứng Dụng Ngủ Ngon #1"),
                    color = Color(0xFF7B9CFF),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tr("by revenue & sleep satisfaction worldwide", "em satisfação de sono no mundo todo", "về mức độ hài lòng giấc ngủ trên toàn cầu"),
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
            ) {
                OnboardingHighlightStatBadge(
                    headline = tr("50 million", "50 milhões", "50 triệu"),
                    subline = tr("downloads", "downloads", "lượt tải"),
                    rotationDeg = 6f,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 16.dp),
                )
                OnboardingHighlightStatBadge(
                    headline = tr("2 billion", "2 bilhões", "2 tỷ"),
                    subline = tr("nights analyzed", "noites analisadas", "đêm phân tích"),
                    rotationDeg = -10f,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp, top = 16.dp),
                )
                OnboardingHighlightStatBadge(
                    headline = "★★★★★",
                    subline = tr("High-Rated Sleep App", "App Mais Bem Avaliado", "Ứng Dụng Đánh Giá Cao"),
                    rotationDeg = -6f,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 10.dp, bottom = 6.dp),
                )
            }
        }
    }
}
@Composable
private fun OnboardingSleepAssessmentPage(
    question: SleepAssessmentQuestion,
    stepIndex: Int,
    totalSteps: Int,
    onBack: () -> Unit,
    onSelectOption: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .responsiveContentWidth(560.dp)
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                if (stepIndex > 1) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = TextSecondary,
                        )
                    }
                }

                OnboardingCompanionAvatar(
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            val localizedTitle = tr(question.titleEn, question.titlePtBr, question.titleVi)

            Text(
                text = localizedTitle,
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 29.sp,
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                question.options.forEach { opt ->
                    val emoji = opt.emoji
                    val label = tr(opt.en, opt.ptBr, opt.vi)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF151C36))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .clickable(onClick = onSelectOption)
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = label,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Box(
                modifier = Modifier
                    .width(68.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.14f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (stepIndex.toFloat() / totalSteps).coerceIn(0.2f, 1f))
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(DeepSleepBlue),
                )
            }
        }
    }
}
@Composable
private fun OnboardingWelcomeIntroPage(
    onSkipOrNext: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onSkipOrNext)
            .padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(144.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PremiumGold.copy(alpha = 0.24f),
                                Color.Transparent,
                            ),
                        ),
                    )
                    .border(2.dp, PremiumGold.copy(alpha = 0.65f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(5) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = PremiumGold,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = tr("10 Million\nuser's choices", "Escolha de\n10 Milhões", "Lựa chọn của\n10 Triệu người"),
                        color = PremiumGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF151C36))
                    .border(1.dp, PremiumGold.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = tr("1 Billion+", "1 Bilhão+", "Hơn 1 Tỷ"),
                        color = PremiumGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = tr("nights tracked worldwide", "noites monitoradas no mundo", "đêm giấc ngủ được phân tích"),
                        color = TextSecondary,
                        fontSize = 12.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = tr("It's time to\nupgrade your sleep!", "É hora de melhorar\no seu sono!", "Đã đến lúc nâng tầm\ngiấc ngủ của bạn!"),
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tr("Helped 9,200,000+ users sleep better every night", "Ajudou 9.200.000+ usuários a dormir melhor todas as noites", "Đã giúp hơn 9.200.000 người ngủ ngon hơn mỗi đêm"),
                color = TextSecondary,
                fontSize = 13.5.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onFirstContentReady: () -> Unit = {},
    onboardingViewModel: OnboardingViewModel = hiltViewModel(),
) {
    ImmersiveSystemBars()

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val adMobManager = rememberAdMobManager()
    val currentLanguage by onboardingViewModel.appLanguage.collectAsStateWithLifecycle()
    var currentStep by rememberSaveable { mutableIntStateOf(-1) }
    var hasClickedLanguageItem by rememberSaveable { mutableStateOf(false) }
    var isProcessingAdTransition by remember { mutableStateOf(false) }

    val rewardEarnedToastTemplate = tr(
        "Reward earned! Progress: %d/%d toward 3h Ad-Free & VIP",
        "Recompensa ganha! Progresso: %d/%d para 3h Sem Anúncios e VIP",
        "Đã nhận thưởng! Tiến độ: %d/%d mở khóa 3 giờ Không Quảng Cáo & VIP",
    )

    val mainActivity = activity as? com.sleepsounds.storytelling.MainActivity
    val splashVisibleFlow = remember(mainActivity) {
        mainActivity?.startupSplashCoordinator?.splashVisible
    }
    val coldStartDoneFlow = remember(mainActivity) {
        mainActivity?.startupSplashCoordinator?.coldStartSequenceCompleted
    }
    val isSplashCovering by (splashVisibleFlow?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(false) })
    val isColdStartDone by (coldStartDoneFlow?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(true) })
    val isShowingFullScreenAd by (adMobManager?.isShowingFullScreenAdState?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(false) })

    val activeOnboardingBannerChannel = when {
        currentStep == -1 && !hasClickedLanguageItem -> AdConfigManager.PLACEMENT_BANNER_LANGUAGE_1
        currentStep == -1 && hasClickedLanguageItem -> AdConfigManager.PLACEMENT_BANNER_LANGUAGE_1_CLICK
        currentStep in 0..4 -> AdConfigManager.PLACEMENT_BANNER_ONBOARDING_1
        else -> AdConfigManager.PLACEMENT_BANNER_ONBOARDING_2
    }

    LaunchedEffect(Unit) {
        onFirstContentReady()
    }

    BackHandler(enabled = currentStep > -1 && currentStep != 9 && !isProcessingAdTransition) {
        currentStep = (currentStep - 1).coerceAtLeast(-1)
    }

    val sdkReady by (adMobManager?.sdkReady?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(false) })

    // Look-Ahead Preload Strategy (modeled on reference APK):
    // Warm the next step's banner only after Splash has unlocked next-screen preloading
    // so `banner_splash` and the currently visible mounted banner get 100% network priority.
    LaunchedEffect(currentStep, hasClickedLanguageItem, sdkReady, isSplashCovering, isColdStartDone, isShowingFullScreenAd) {
        if (!sdkReady || isSplashCovering) return@LaunchedEffect
        when {
            currentStep == -1 -> {
                adMobManager?.preloadInterstitialAd()
                if (!hasClickedLanguageItem) {
                    preloadBannerChannel(
                        context = context,
                        adMobManager = adMobManager,
                        slotKey = AdConfigManager.PLACEMENT_BANNER_LANGUAGE_1_CLICK,
                    )
                } else {
                    preloadBannerChannel(
                        context = context,
                        adMobManager = adMobManager,
                        slotKey = AdConfigManager.PLACEMENT_BANNER_ONBOARDING_1,
                    )
                }
            }
            currentStep == 0 -> {
                if (!isColdStartDone || isShowingFullScreenAd) return@LaunchedEffect
                adMobManager?.ensureRewardedAdLoaded()
                preloadBannerChannel(
                    context = context,
                    adMobManager = adMobManager,
                    slotKey = AdConfigManager.PLACEMENT_BANNER_ONBOARDING_1,
                )
                preloadBannerChannel(
                    context = context,
                    adMobManager = adMobManager,
                    slotKey = AdConfigManager.PLACEMENT_BANNER_ONBOARDING_2,
                )
                delay(2_000L)
                if (currentStep == 0) {
                    currentStep = 1
                }
            }
            currentStep in 1..4 -> {
                adMobManager?.ensureRewardedAdLoaded()
                preloadBannerChannel(
                    context = context,
                    adMobManager = adMobManager,
                    slotKey = AdConfigManager.PLACEMENT_BANNER_ONBOARDING_2,
                )
            }
            currentStep >= 5 -> {
                adMobManager?.preloadInterstitialAd()
                preloadBannerChannel(
                    context = context,
                    adMobManager = adMobManager,
                    slotKey = AdConfigManager.PLACEMENT_BANNER_COLLAPSIBLE_HOME,
                )
                preloadBannerChannel(
                    context = context,
                    adMobManager = adMobManager,
                    slotKey = AdConfigManager.PLACEMENT_BANNER_HOME,
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF060914),
                        Color(0xFF0B1021),
                        Color(0xFF060914),
                    ),
                ),
            )
            .systemSafePadding(),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn(tween(240)) togetherWith fadeOut(tween(180))
                },
                label = "deep_sleep_onboarding_step",
            ) { step ->
                when (step) {
                    -1 -> OnboardingLanguagePickerPage(
                        selectedLanguageCode = currentLanguage,
                        onSelectLanguage = { code ->
                            hasClickedLanguageItem = true
                            onboardingViewModel.setAppLanguage(code)
                            preloadBannerChannel(
                                context = context,
                                adMobManager = adMobManager,
                                slotKey = AdConfigManager.PLACEMENT_BANNER_ONBOARDING_1,
                            )
                        },
                        onConfirmLanguage = {
                            if (!isProcessingAdTransition) {
                                isProcessingAdTransition = true
                                onboardingViewModel.confirmFirstOpenLanguage(currentLanguage)
                                val manager = adMobManager
                                if (manager != null) {
                                    manager.showDedicatedInterstitial(
                                        activity = activity,
                                        maxWaitMs = 2_500L,
                                    ) {
                                        isProcessingAdTransition = false
                                        currentStep = 0
                                    }
                                } else {
                                    isProcessingAdTransition = false
                                    currentStep = 0
                                }
                            }
                        },
                    )

                    0 -> OnboardingWelcomeIntroPage(
                        onSkipOrNext = { currentStep = 1 },
                    )

                    in 1..sleepAssessmentQuestions.size -> {
                        val questionIdx = step - 1
                        OnboardingSleepAssessmentPage(
                            question = sleepAssessmentQuestions[questionIdx],
                            stepIndex = step,
                            totalSteps = sleepAssessmentQuestions.size,
                            onBack = {
                                if (step > 1 && !isProcessingAdTransition) {
                                    currentStep = step - 1
                                }
                            },
                            onSelectOption = {
                                if (step == 4) {
                                    if (!isProcessingAdTransition) {
                                        isProcessingAdTransition = true
                                        val manager = adMobManager
                                        if (manager != null) {
                                            manager.showOnboardingRewardedAd(
                                                activity = activity,
                                                maxWaitMs = 3_000L,
                                            ) { rewardEarned, newCount ->
                                                if (rewardEarned) {
                                                    Toast.makeText(
                                                        context,
                                                        rewardEarnedToastTemplate.format(
                                                            newCount,
                                                            UserPreferencesRepository.REWARDED_VIDEOS_FOR_AD_FREE_PASS,
                                                        ),
                                                        Toast.LENGTH_SHORT,
                                                    ).show()
                                                }
                                                isProcessingAdTransition = false
                                                currentStep = step + 1
                                            }
                                        } else {
                                            isProcessingAdTransition = false
                                            currentStep = step + 1
                                        }
                                    }
                                } else {
                                    currentStep = step + 1
                                }
                            },
                        )
                    }

                    6 -> OnboardingAudioDetectionFeaturePage(
                        onNext = { currentStep = 7 },
                    )

                    7 -> OnboardingSleepCycleFeaturePage(
                        onNext = { currentStep = 8 },
                    )

                    8 -> OnboardingSoundscapeFeaturePage(
                        onNext = { currentStep = 9 },
                    )

                    else -> OnboardingReportGenerationPage(
                        onFinishedLoading = {
                            if (!isProcessingAdTransition) {
                                isProcessingAdTransition = true
                                val manager = adMobManager
                                if (manager != null) {
                                    manager.showDedicatedInterstitial(
                                        activity = activity,
                                        maxWaitMs = 2_500L,
                                    ) {
                                        onComplete()
                                    }
                                } else {
                                    onComplete()
                                }
                            }
                        },
                    )
                }
            }
        }

        // Click-Swap 2-Banner on Language Screen + Preloaded Multi-Step Banner on Onboarding
        key(activeOnboardingBannerChannel) {
            AdaptiveBannerAdBar(
                modifier = Modifier.fillMaxWidth(),
                adMobManager = adMobManager,
                slotKey = activeOnboardingBannerChannel,
                allowDuringOnboarding = true,
                fullBleedSticky = true,
            )
        }
    }
}
private data class SleepAssessmentQuestion(
    val titleEn: String,
    val titlePtBr: String,
    val titleVi: String,
    val options: List<SleepAssessmentOption>,
)
private data class SleepAssessmentOption(
    val emoji: String,
    val en: String,
    val ptBr: String,
    val vi: String,
)
