package com.sleepsounds.storytelling.presentation.tracker

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.ads.AdaptiveBannerAdBar
import com.sleepsounds.storytelling.ads.rememberAdMobManager
import com.sleepsounds.storytelling.domain.model.SleepNoteFactor
import com.sleepsounds.storytelling.domain.model.SmartAlarm
import com.sleepsounds.storytelling.domain.model.WakeUpMood
import com.sleepsounds.storytelling.presentation.components.ScreenBackground
import com.sleepsounds.storytelling.ui.theme.MidnightBlue
import com.sleepsounds.storytelling.ui.theme.PastelPurple
import com.sleepsounds.storytelling.ui.theme.DeepSleepBlue
import com.sleepsounds.storytelling.ui.theme.SurfaceBlue
import com.sleepsounds.storytelling.ui.theme.SurfaceBlueLight
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.TextSecondary
import com.sleepsounds.storytelling.ui.theme.rememberResponsiveLayoutInfo
import com.sleepsounds.storytelling.ui.theme.responsiveContentWidth
import com.sleepsounds.storytelling.ui.theme.tr
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreSleepFactorsSelectionSheet(
    selectedNotes: Set<String>,
    selectedMood: WakeUpMood,
    onToggleNote: (String) -> Unit,
    onSelectMood: (WakeUpMood) -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = tr("Pre-Sleep Notes & Mood", "Notas de Sono e Humor", "Ghi Chú Trước Khi Ngủ & Tâm Trạng"),
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = tr("Tag activities before bed to see how they affect your deep sleep.", "Marque atividades antes de dormir para ver como afetam seu sono profundo.", "Đánh dấu hoạt động trước khi ngủ để xem ảnh hưởng tới giấc ngủ sâu của bạn."),
            color = TextSecondary,
            fontSize = 13.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SleepNoteFactor.entries.forEach { factor ->
                val isSelected = factor.id in selectedNotes
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isSelected) DeepSleepBlue else SurfaceBlueLight)
                        .clickable { onToggleNote(factor.id) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = factor.emoji, fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tr(factor.label),
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(6.dp))
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

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = tr("Expected Wake-up Mood", "Humor ao Acordar", "Tâm trạng khi thức dậy"),
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            WakeUpMood.entries.forEach { mood ->
                val isSelected = selectedMood == mood
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) DeepSleepBlue.copy(alpha = 0.28f) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) DeepSleepBlue else Color.Transparent,
                            shape = RoundedCornerShape(14.dp),
                        )
                        .clickable { onSelectMood(mood) }
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = mood.emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tr(mood.label),
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DeepSleepBlue),
        ) {
            Text(stringResource(R.string.action_done), color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
@Composable
private fun PhoneBedsidePlacementSheet(
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = tr("Phone Placement & Battery Guide", "Guia de Posição e Bateria", "Hướng Dẫn Đặt Máy & Pin"),
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceBlue)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.PhoneAndroid,
                contentDescription = null,
                tint = DeepSleepBlue,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = tr("Place your phone on your nightstand or near your pillow with the microphone facing toward your bed (30–50 cm away).", "Coloque o celular na mesa de cabeceira ou perto do travesseiro com o microfone voltado para a cama (30–50 cm).", "Đặt điện thoại trên bàn đầu giường hoặc cạnh gối, hướng micro về phía giường (cách 30–50 cm)."),
                color = TextSecondary,
                fontSize = 13.5.sp,
                lineHeight = 19.sp,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceBlue)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.BatteryChargingFull,
                contentDescription = null,
                tint = Color(0xFF22C55E),
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = tr("Keep your device connected to a charger overnight or ensure at least 40% battery before starting the sleep tracker.", "Mantenha o aparelho conectado ao carregador durante a noite ou tenha pelo menos 40% de bateria.", "Cắm sạc qua đêm hoặc đảm bảo pin trên 40% trước khi bắt đầu theo dõi giấc ngủ."),
                color = TextSecondary,
                fontSize = 13.5.sp,
                lineHeight = 19.sp,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DeepSleepBlue),
        ) {
            Text(tr("Got It", "Entendi", "Đã hiểu"), color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
@Composable
private fun SmartAlarmConfigurationSheet(
    alarm: SmartAlarm,
    onSave: (hour: Int, minute: Int, smartWindowMinutes: Int, vibrate: Boolean) -> Unit,
    onClose: () -> Unit,
) {
    var hour by remember(alarm) { mutableIntStateOf(alarm.hour) }
    var minute by remember(alarm) { mutableIntStateOf(alarm.minute) }
    var smartWindow by remember(alarm) { mutableIntStateOf(alarm.smartWindowMinutes) }
    var vibrate by remember(alarm) { mutableStateOf(alarm.vibrate) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = tr("Smart Wake-up Alarm", "Alarme Inteligente", "Báo Thức Thông Minh"),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "%02d:%02d".format(hour, minute),
            color = DeepSleepBlue,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "${tr("Hour", "Hora", "Giờ")} ($hour h)",
            color = TextSecondary,
            fontSize = 13.sp,
        )
        Slider(
            value = hour.toFloat(),
            onValueChange = { hour = it.toInt().coerceIn(0, 23) },
            valueRange = 0f..23f,
            steps = 22,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = DeepSleepBlue,
            ),
        )

        Text(
            text = "${tr("Minute", "Minuto", "Phút")} ($minute min)",
            color = TextSecondary,
            fontSize = 13.sp,
        )
        Slider(
            value = minute.toFloat(),
            onValueChange = { minute = ((it.toInt() / 5) * 5).coerceIn(0, 55) },
            valueRange = 0f..55f,
            steps = 10,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = DeepSleepBlue,
            ),
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = tr("Smart Wake Window (wakes you in light sleep)", "Janela Inteligente (desperta no sono leve)", "Khung giờ đánh thức thông minh (pha ngủ nông)"),
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(0, 15, 20, 30).forEach { win ->
                val selected = smartWindow == win
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) DeepSleepBlue else SurfaceBlueLight)
                        .clickable { smartWindow = win }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = if (win == 0) tr("Exact", "Exato", "Đúng giờ") else "${win}m",
                        color = if (selected) Color.White else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = tr("Gentle Vibration", "Vibração Suave", "Rung dịu nhẹ"),
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = vibrate,
                onCheckedChange = { vibrate = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = DeepSleepBlue,
                ),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onSave(hour, minute, smartWindow, vibrate) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DeepSleepBlue),
        ) {
            Text(
                text = tr("Save Alarm", "Salvar Alarme", "Lưu báo thức"),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
@Composable
private fun TrackerFeatureShortcutCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceBlue.copy(alpha = 0.85f))
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(SurfaceBlueLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PastelPurple,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            color = TextSecondary,
            fontSize = 11.sp,
            maxLines = 1,
        )
    }
}
@Composable
private fun TrackerSmartAlarmSummaryCard(
    alarm: SmartAlarm,
    onToggleEnabled: (Boolean) -> Unit,
    onEditAlarm: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceBlue.copy(alpha = 0.9f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .clickable(onClick = onEditAlarm)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DeepSleepBlue.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.AccessAlarm,
                contentDescription = null,
                tint = DeepSleepBlue,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = alarm.formattedTime,
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = if (alarm.smartWindowMinutes > 0) {
                            "Smart ${alarm.smartWindowMinutes}m"
                        } else {
                            "Exact"
                        },
                        color = PastelPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tr("Wake-up Alarm · Gentle phase wake", "Alarme Inteligente · Despertar suave", "Báo thức thông minh · Đánh thức dịu nhẹ"),
                color = TextSecondary,
                fontSize = 12.5.sp,
            )
        }

        Switch(
            checked = alarm.enabled,
            onCheckedChange = onToggleEnabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = DeepSleepBlue,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceBlueLight,
            ),
        )
    }
}
@Composable
private fun TrackerNightClockDial(
    timeText: String,
    subtitle: String,
    isTracking: Boolean,
    amplitudes: List<Float>,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tracker_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )

    Box(
        modifier = Modifier.size(250.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension * 0.42f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        DeepSleepBlue.copy(alpha = if (isTracking) 0.36f else 0.22f),
                        PastelPurple.copy(alpha = 0.08f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = baseRadius * 1.25f * pulseScale,
                ),
                radius = baseRadius * 1.25f * pulseScale,
                center = center,
            )

            drawCircle(
                color = Color(0xFF151C39),
                radius = baseRadius,
                center = center,
            )

            drawCircle(
                color = DeepSleepBlue.copy(alpha = 0.65f),
                radius = baseRadius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx()),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 20.dp),
        ) {
            Text(
                text = timeText,
                color = TextPrimary,
                fontSize = if (isTracking) 34.sp else 44.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )

            if (isTracking) {
                Spacer(modifier = Modifier.height(16.dp))
                Canvas(
                    modifier = Modifier
                        .width(150.dp)
                        .height(36.dp),
                ) {
                    val count = amplitudes.size.coerceAtLeast(1)
                    val barW = size.width / (count * 1.8f)
                    val gap = (size.width - barW * count) / (count - 1).coerceAtLeast(1)
                    amplitudes.forEachIndexed { idx, amp ->
                        val barH = (size.height * amp).coerceAtLeast(4.dp.toPx())
                        val x = idx * (barW + gap)
                        val y = (size.height - barH) / 2f
                        drawRoundRect(
                            color = DeepSleepBlue,
                            topLeft = Offset(x, y),
                            size = Size(barW, barH),
                            cornerRadius = CornerRadius(barW / 2f, barW / 2f),
                        )
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTrackerScreen(
    onOpenAids: () -> Unit,
    onSessionSavedNavigateToJournal: () -> Unit,
    viewModel: SleepTrackerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var currentTimeText by remember { mutableStateOf("") }
    var currentDateText by remember { mutableStateOf("") }
    var showAlarmSheet by remember { mutableStateOf(false) }
    var showPlacementSheet by remember { mutableStateOf(false) }
    var showSleepNotesSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFmt = SimpleDateFormat("EEEE, MMM d", Locale.ENGLISH)
        while (true) {
            val now = Date()
            currentTimeText = timeFmt.format(now)
            currentDateText = dateFmt.format(now)
            delay(10_000L)
        }
    }

    val layout = rememberResponsiveLayoutInfo()
    val adMobManager = rememberAdMobManager()
    ScreenBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier.responsiveContentWidth(760.dp),
                contentPadding = PaddingValues(
                    start = layout.screenHorizontalPadding,
                    end = layout.screenHorizontalPadding,
                    top = 12.dp,
                    bottom = 24.dp,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item(key = "tracker_header") {
                    Text(
                        text = tr("Sleep Tracker", "Monitor de Sono", "Theo dõi giấc ngủ"),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentDateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(22.dp))
                }

                item(key = "tracker_center_dial") {
                    TrackerNightClockDial(
                        timeText = if (state.isTracking) state.formattedElapsed else currentTimeText,
                        subtitle = if (state.isTracking) {
                            tr("Tracking sleep phases & ambient sound…", "Monitorando fases do sono e som ambiente…", "Đang theo dõi chu kỳ ngủ & âm thanh…")
                        } else {
                            tr("Ready to analyze your sleep cycles", "Pronto para analisar seus ciclos de sono", "Sẵn sàng phân tích chu kỳ giấc ngủ của bạn")
                        },
                        isTracking = state.isTracking,
                        amplitudes = state.waveformAmplitudes,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item(key = "tracker_inline_banner") {
                    AdaptiveBannerAdBar(slotKey = "tracker_tab_banner")
                }

                item(key = "tracker_alarm_card") {
                    TrackerSmartAlarmSummaryCard(
                        alarm = state.alarm,
                        onToggleEnabled = { enabled ->
                            adMobManager?.onUserAction()
                            viewModel.toggleAlarmEnabled(enabled)
                        },
                        onEditAlarm = {
                            adMobManager?.onUserAction { showAlarmSheet = true }
                                ?: run { showAlarmSheet = true }
                        },
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                item(key = "tracker_helper_row") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TrackerFeatureShortcutCard(
                            icon = Icons.Filled.PhoneAndroid,
                            title = stringResource(R.string.tracker_placement),
                            subtitle = tr("Nightstand guide", "Guia de cabeceira", "Hướng dẫn đặt máy"),
                            onClick = {
                                adMobManager?.onUserAction { showPlacementSheet = true }
                                    ?: run { showPlacementSheet = true }
                            },
                            modifier = Modifier.weight(1f),
                        )
                        TrackerFeatureShortcutCard(
                            icon = Icons.Filled.EditNote,
                            title = stringResource(R.string.tracker_sleep_note),
                            subtitle = "${state.selectedNotes.size} ${tr("selected", "selecionados", "đã chọn")}",
                            onClick = {
                                adMobManager?.onUserAction { showSleepNotesSheet = true }
                                    ?: run { showSleepNotesSheet = true }
                            },
                            modifier = Modifier.weight(1f),
                        )
                        TrackerFeatureShortcutCard(
                            icon = Icons.Filled.GraphicEq,
                            title = tr("Sleep Aid", "Sons", "Âm thanh"),
                            subtitle = stringResource(R.string.aids_white_noise),
                            onClick = onOpenAids,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                item(key = "tracker_cta") {
                    if (!state.isTracking) {
                        Button(
                            onClick = {
                                val manager = adMobManager
                                if (manager != null) {
                                    manager.onUserAction { viewModel.startTracking() }
                                } else {
                                    viewModel.startTracking()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),
                            shape = RoundedCornerShape(29.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepSleepBlue,
                                contentColor = Color.White,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bedtime,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.tracker_start),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.stopTrackingAndSave(
                                    onSaved = onSessionSavedNavigateToJournal,
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),
                            shape = RoundedCornerShape(29.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF4D6D),
                                contentColor = Color.White,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = tr("Wake Up & View Sleep Report", "Acordar & Ver Relatório de Sono", "Thức Dậy & Xem Báo Cáo Giấc Ngủ"),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAlarmSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAlarmSheet = false },
            sheetState = sheetState,
            containerColor = MidnightBlue,
            dragHandle = null,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        ) {
            SmartAlarmConfigurationSheet(
                alarm = state.alarm,
                onSave = { hour, minute, windowMin, vibrate ->
                    viewModel.updateAlarmTime(hour, minute, windowMin, vibrate)
                    showAlarmSheet = false
                },
                onClose = { showAlarmSheet = false },
            )
        }
    }

    if (showPlacementSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPlacementSheet = false },
            sheetState = sheetState,
            containerColor = MidnightBlue,
            dragHandle = null,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        ) {
            PhoneBedsidePlacementSheet(onDismiss = { showPlacementSheet = false })
        }
    }

    if (showSleepNotesSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSleepNotesSheet = false },
            sheetState = sheetState,
            containerColor = MidnightBlue,
            dragHandle = null,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        ) {
            PreSleepFactorsSelectionSheet(
                selectedNotes = state.selectedNotes,
                selectedMood = state.selectedMood,
                onToggleNote = { viewModel.toggleSleepNote(it) },
                onSelectMood = { viewModel.selectWakeUpMood(it) },
                onDone = { showSleepNotesSheet = false },
            )
        }
    }
}
