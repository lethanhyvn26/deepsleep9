package com.sleepsounds.storytelling.presentation.journal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.sleepsounds.storytelling.ads.AdaptiveBannerAdBar
import com.sleepsounds.storytelling.data.repository.SleepTrackerRepository
import com.sleepsounds.storytelling.domain.model.SleepNoteFactor
import com.sleepsounds.storytelling.domain.model.SleepSession
import com.sleepsounds.storytelling.domain.model.SleepStageSample
import com.sleepsounds.storytelling.domain.model.SleepStageType
import com.sleepsounds.storytelling.presentation.components.ScreenBackground
import com.sleepsounds.storytelling.ui.theme.PastelPurple
import com.sleepsounds.storytelling.ui.theme.DeepSleepBlue
import com.sleepsounds.storytelling.ui.theme.StageAwakeOrange
import com.sleepsounds.storytelling.ui.theme.StageDeepBlue
import com.sleepsounds.storytelling.ui.theme.StageFellAsleepTeal
import com.sleepsounds.storytelling.ui.theme.StageLightPurple
import com.sleepsounds.storytelling.ui.theme.SurfaceBlue
import com.sleepsounds.storytelling.ui.theme.SurfaceBlueLight
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.TextSecondary
import com.sleepsounds.storytelling.ui.theme.rememberResponsiveLayoutInfo
import com.sleepsounds.storytelling.ui.theme.responsiveContentWidth
import com.sleepsounds.storytelling.ui.theme.tr
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private fun formatMinutesAsHoursMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}min" else "${m}min"
}
@Composable
private fun JournalFactorsAndNoiseCard(
    session: SleepSession,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceBlue.copy(alpha = 0.9f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.GraphicEq,
                contentDescription = null,
                tint = DeepSleepBlue,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = tr("Ambient Noise & Sleep Factors", "Ruído Ambiente & Fatores de Sono", "Tiếng Ồn Đêm & Yếu Tố Giấc Ngủ"),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (session.snoreCount == 0) {
                tr("Quiet environment · 0 loud snore or talk disruptions detected", "Ambiente silencioso · 0 interrupções de ronco detectadas", "Không gian yên tĩnh · Không phát hiện tiếng ngáy lớn")
            } else {
                "${session.snoreCount} sound events (${session.snoreMinutes} min) detected overnight"
            },
            color = TextSecondary,
            fontSize = 13.sp,
        )

        if (session.sleepNotes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                session.sleepNotes.forEach { noteId ->
                    val factor = SleepNoteFactor.entries.find { it.id == noteId }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceBlueLight)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = if (factor != null) "${factor.emoji} ${tr(factor.label)}" else noteId,
                            color = PastelPurple,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun SleepStageSummaryTile(
    dotColor: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceBlueLight.copy(alpha = 0.7f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 11.5.sp,
            )
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
@Composable
private fun SleepStageHypnogramCanvas(
    stages: List<SleepStageSample>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val yAwake = h * 0.16f
        val yLight = h * 0.52f
        val yDeep = h * 0.86f

        listOf(yAwake, yLight, yDeep).forEach { y ->
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.dp.toPx(),
            )
        }

        if (stages.size < 2) return@Canvas
        val maxMinute = stages.last().minuteOffset.coerceAtLeast(1)

        fun stageY(stage: SleepStageType): Float = when (stage) {
            SleepStageType.AWAKE -> yAwake
            SleepStageType.LIGHT -> yLight
            SleepStageType.DEEP -> yDeep
        }

        val linePath = Path()
        val fillPath = Path()

        stages.forEachIndexed { index, sample ->
            val x = (sample.minuteOffset.toFloat() / maxMinute) * w
            val y = stageY(sample.stage)
            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                val prev = stages[index - 1]
                val prevX = (prev.minuteOffset.toFloat() / maxMinute) * w
                val prevY = stageY(prev.stage)
                val midX = (prevX + x) / 2f
                linePath.cubicTo(midX, prevY, midX, y, x, y)
                fillPath.cubicTo(midX, prevY, midX, y, x, y)
            }
        }

        fillPath.lineTo(w, h)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    StageLightPurple.copy(alpha = 0.34f),
                    StageDeepBlue.copy(alpha = 0.12f),
                    Color.Transparent,
                ),
            ),
        )

        drawPath(
            path = linePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    StageAwakeOrange,
                    StageLightPurple,
                    StageDeepBlue,
                ),
            ),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}
@Composable
private fun JournalHypnogramTimelineCard(
    session: SleepSession,
    startLabel: String,
    endLabel: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceBlue.copy(alpha = 0.9f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
            .padding(20.dp),
    ) {
        Text(
            text = tr("Sleep Stages", "Fases do Sono", "Chu kỳ giấc ngủ"),
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(14.dp))

        SleepStageHypnogramCanvas(
            stages = session.stages,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
        )

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = startLabel, color = TextSecondary, fontSize = 11.sp)
            Text(text = tr("Awake · Light · Deep", "Acordado · Leve · Profundo", "Thức · Ngủ nông · Ngủ sâu"), color = TextSecondary, fontSize = 11.sp)
            Text(text = endLabel, color = TextSecondary, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SleepStageSummaryTile(
                dotColor = StageAwakeOrange,
                label = tr("Awake", "Acordado", "Thức giấc"),
                value = "${formatMinutesAsHoursMinutes(session.awakeMinutes)} (${session.awakePercent}%)",
                modifier = Modifier.weight(1f),
            )
            SleepStageSummaryTile(
                dotColor = StageLightPurple,
                label = tr("Light Sleep", "Sono Leve", "Ngủ nông"),
                value = "${formatMinutesAsHoursMinutes(session.lightSleepMinutes)} (${session.lightSleepPercent}%)",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SleepStageSummaryTile(
                dotColor = StageDeepBlue,
                label = tr("Deep Sleep", "Sono Profundo", "Ngủ sâu"),
                value = "${formatMinutesAsHoursMinutes(session.deepSleepMinutes)} (${session.deepSleepPercent}%)",
                modifier = Modifier.weight(1f),
            )
            SleepStageSummaryTile(
                dotColor = StageFellAsleepTeal,
                label = tr("Fell Asleep", "Adormeceu em", "Đi vào giấc ngủ"),
                value = "${session.fellAsleepMinutes}min",
                modifier = Modifier.weight(1f),
            )
        }
    }
}
@Composable
private fun JournalQualityScoreCard(
    session: SleepSession,
    bedtimeText: String,
) {
    val hrs = session.durationMinutes / 60
    val mins = session.durationMinutes % 60
    val qualityLabel = when {
        session.sleepScore >= 85 -> tr("Excellent", "Excelente", "Tuyệt vời")
        session.sleepScore >= 75 -> tr("Good", "Bom", "Tốt")
        session.sleepScore >= 60 -> tr("Fair", "Regular", "Khá")
        else -> tr("Needs Rest", "Precisa Descansar", "Cần nghỉ ngơi")
    }
    val scoreColor = when {
        session.sleepScore >= 85 -> Color(0xFF22C55E)
        session.sleepScore >= 70 -> DeepSleepBlue
        else -> StageAwakeOrange
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceBlue.copy(alpha = 0.9f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(112.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 10.dp.toPx()
                val arcSize = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(stroke / 2f, stroke / 2f)

                drawArc(
                    color = Color.White.copy(alpha = 0.1f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )

                val sweep = 270f * (session.sleepScore.coerceIn(0, 100) / 100f)
                drawArc(
                    color = scoreColor,
                    startAngle = 135f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = session.sleepScore.toString(),
                    color = TextPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = qualityLabel,
                    color = scoreColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tr("Time in Bed", "Tempo na Cama", "Thời gian trên giường"),
                color = TextSecondary,
                fontSize = 12.sp,
            )
            Text(
                text = "${hrs}h ${mins}m",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Bedtime,
                    contentDescription = null,
                    tint = PastelPurple,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = bedtimeText,
                    color = PastelPurple,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${tr("Wake-up mood", "Humor ao acordar", "Tâm trạng")}: ${session.wakeUpMood.emoji} ${tr(session.wakeUpMood.label)}",
                color = TextSecondary,
                fontSize = 12.5.sp,
            )
        }
    }
}
@Composable
fun SleepJournalScreen(
    onNavigateToTracker: () -> Unit,
    viewModel: SleepJournalViewModel = hiltViewModel(),
) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    var selectedIndex by remember(sessions.size) { mutableIntStateOf(0) }
    val activeSession = sessions.getOrNull(selectedIndex) ?: sessions.first()

    val dateFormatter = remember { SimpleDateFormat("MMM d", Locale.ENGLISH) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val layout = rememberResponsiveLayoutInfo()
    ScreenBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 12.dp,
                    bottom = 24.dp,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(key = "journal_header") {
                    Column(
                        modifier = Modifier
                            .responsiveContentWidth(760.dp)
                            .padding(horizontal = layout.screenHorizontalPadding),
                    ) {
                        Text(
                            text = tr("Sleep Journal", "Diário de Sono", "Nhật ký giấc ngủ"),
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            itemsIndexed(sessions) { index, session ->
                                val isSelected = index == selectedIndex
                                val label = if (session.isDemoData) {
                                    tr("Sample Report", "Relatório Exemplo", "Báo cáo mẫu")
                                } else {
                                    dateFormatter.format(Date(session.endedAt))
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (isSelected) DeepSleepBlue else SurfaceBlueLight)
                                        .clickable { selectedIndex = index }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                }

                if (activeSession.isDemoData) {
                    item(key = "demo_banner") {
                        Row(
                            modifier = Modifier
                                .responsiveContentWidth(760.dp)
                                .padding(horizontal = layout.screenHorizontalPadding)
                                .clip(RoundedCornerShape(16.dp))
                                .background(DeepSleepBlue.copy(alpha = 0.22f))
                                .border(1.dp, DeepSleepBlue.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = DeepSleepBlue,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tr("This is demo report data", "Estes são dados de demonstração", "Đây là dữ liệu báo cáo mẫu"),
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = tr("Start the Sleep Tracker tonight to generate your personal sleep stages & score.", "Inicie o Monitor de Sono hoje à noite para gerar suas fases e pontuação de sono.", "Hãy bật Theo dõi giấc ngủ tối nay để tạo biểu đồ và điểm số giấc ngủ của riêng bạn."),
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onNavigateToTracker,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepSleepBlue),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text(tr("Track", "Monitorar", "Theo dõi"), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item(key = "journal_inline_banner") {
                    AdaptiveBannerAdBar(slotKey = "journal_tab_banner")
                }

                item(key = "score_gauge_card") {
                    Box(
                        modifier = Modifier
                            .responsiveContentWidth(760.dp)
                            .padding(horizontal = layout.screenHorizontalPadding),
                    ) {
                        val startStr = timeFormatter.format(Date(activeSession.startedAt))
                        val endStr = timeFormatter.format(Date(activeSession.endedAt))
                        JournalQualityScoreCard(
                            session = activeSession,
                            bedtimeText = "$startStr - $endStr",
                        )
                    }
                }

                item(key = "hypnogram_card") {
                    Box(
                        modifier = Modifier
                            .responsiveContentWidth(760.dp)
                            .padding(horizontal = layout.screenHorizontalPadding),
                    ) {
                        val startStr = timeFormatter.format(Date(activeSession.startedAt))
                        val endStr = timeFormatter.format(Date(activeSession.endedAt))
                        JournalHypnogramTimelineCard(
                            session = activeSession,
                            startLabel = startStr,
                            endLabel = endStr,
                        )
                    }
                }

                item(key = "snore_and_notes_card") {
                    Box(
                        modifier = Modifier
                            .responsiveContentWidth(760.dp)
                            .padding(horizontal = layout.screenHorizontalPadding),
                    ) {
                        JournalFactorsAndNoiseCard(session = activeSession)
                    }
                }
            }
        }
    }
}
@HiltViewModel
class SleepJournalViewModel @Inject constructor(
    sleepTrackerRepository: SleepTrackerRepository,
) : ViewModel() {
    val sessions: StateFlow<List<SleepSession>> = sleepTrackerRepository
        .observeSessionsWithDemoFallback()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            listOf(SleepTrackerRepository.buildSampleSleepSession()),
        )
}
