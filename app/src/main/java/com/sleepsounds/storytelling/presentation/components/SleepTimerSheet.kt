package com.sleepsounds.storytelling.presentation.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.service.SleepTimerManager
import com.sleepsounds.storytelling.ui.theme.GlassOverlay
import com.sleepsounds.storytelling.ui.theme.DeepSleepBlue
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.TextSecondary
import com.sleepsounds.storytelling.ui.theme.responsiveContentWidth
import kotlin.math.abs
import kotlin.math.roundToInt

private val TimerSheetBackground = Color(0xFF101936)
private val PresetPillUnselectedBg = Color(0xFF192548)
private val CancelTimerCoral = Color(0xFFFF8A80)
private const val TIMER_MIN_MINUTES = 5
private const val TIMER_MAX_MINUTES = 180
private const val TIMER_STEP_MINUTES = 5
@Composable
private fun HorizontalMinuteDial(
    selectedMinutes: Int,
    onMinutesSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val tickStepPx = with(density) { 18.dp.toPx() }
    val labelFontPx = with(density) { 12.sp.toPx() }
    val majorTickPx = with(density) { 28.dp.toPx() }
    val minorTickPx = with(density) { 15.dp.toPx() }
    val labelBaselinePx = with(density) { 44.dp.toPx() }

    var dragDeltaPx by remember(selectedMinutes) { mutableFloatStateOf(0f) }

    val normalTickPaint = remember(labelFontPx) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(190, 158, 172, 204)
            textSize = labelFontPx
            textAlign = Paint.Align.CENTER
        }
    }

    val activeTickPaint = remember(labelFontPx) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = labelFontPx
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
    }

    Canvas(
        modifier = modifier.pointerInput(tickStepPx) {
            detectHorizontalDragGestures(
                onDragStart = { dragDeltaPx = 0f },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    dragDeltaPx -= dragAmount
                    val stepDelta = (dragDeltaPx / tickStepPx).roundToInt()
                    if (stepDelta != 0) {
                        dragDeltaPx -= stepDelta * tickStepPx
                        val nextMinutes = (selectedMinutes + stepDelta * TIMER_STEP_MINUTES)
                            .coerceIn(TIMER_MIN_MINUTES, TIMER_MAX_MINUTES)
                        onMinutesSelected(nextMinutes)
                    }
                },
            )
        },
    ) {
        val midX = size.width / 2f
        val maxStepIndex = (TIMER_MAX_MINUTES - TIMER_MIN_MINUTES) / TIMER_STEP_MINUTES
        val currentStepIndex = (selectedMinutes - TIMER_MIN_MINUTES) / TIMER_STEP_MINUTES

        for (idx in 0..maxStepIndex) {
            val minuteMark = TIMER_MIN_MINUTES + idx * TIMER_STEP_MINUTES
            val relativeSteps = idx - currentStepIndex
            val xPos = midX + relativeSteps * tickStepPx
            if (xPos < -tickStepPx || xPos > size.width + tickStepPx) continue

            val isQuarterHour = minuteMark % 15 == 0
            val isCenterMark = minuteMark == selectedMinutes
            val lineLen = if (isQuarterHour || isCenterMark) majorTickPx else minorTickPx
            val yStart = (majorTickPx - lineLen) / 2f
            val yEnd = yStart + lineLen

            val edgeRatio = (abs(xPos - midX) / (size.width * 0.5f)).coerceIn(0f, 1f)
            val fadeAlpha = (1f - edgeRatio * 0.72f).coerceIn(0.18f, 1f)

            drawLine(
                color = if (isCenterMark) {
                    Color.White
                } else {
                    Color.White.copy(alpha = fadeAlpha * if (isQuarterHour) 0.85f else 0.45f)
                },
                start = Offset(xPos, yStart),
                end = Offset(xPos, yEnd),
                strokeWidth = when {
                    isCenterMark -> 4f
                    isQuarterHour -> 3f
                    else -> 2f
                },
            )

            if (isQuarterHour) {
                val paint = if (isCenterMark) activeTickPaint else normalTickPaint
                paint.alpha = (fadeAlpha * 255).toInt().coerceIn(50, 255)
                drawContext.canvas.nativeCanvas.drawText(
                    minuteMark.toString(),
                    xPos,
                    labelBaselinePx,
                    paint,
                )
            }
        }
    }
}
/**
 * Interactive bottom sheet for configuring and starting the sleep countdown timer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerBottomSheet(
    sleepTimerManager: SleepTimerManager,
    onDismiss: () -> Unit,
) {
    val modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val timerState by sleepTimerManager.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    val initialSelection = remember(timerState.isActive, timerState.lastSelectedMinutes) {
        if (timerState.isActive && timerState.remainingSeconds > 0) {
            ((timerState.remainingSeconds + 150) / 300 * 5).coerceIn(TIMER_MIN_MINUTES, TIMER_MAX_MINUTES)
        } else {
            timerState.lastSelectedMinutes.coerceIn(TIMER_MIN_MINUTES, TIMER_MAX_MINUTES)
        }
    }

    var chosenMinutes by remember { mutableIntStateOf(initialSelection) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalSheetState,
        containerColor = TimerSheetBackground,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .responsiveContentWidth(560.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.action_close),
                        tint = TextSecondary,
                        modifier = Modifier.size(28.dp),
                    )
                }

                Text(
                    text = stringResource(R.string.mixer_set_timer),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )

                if (timerState.isActive) {
                    Text(
                        text = stringResource(R.string.timer_cancel),
                        style = MaterialTheme.typography.labelLarge,
                        color = CancelTimerCoral,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(CancelTimerCoral.copy(alpha = 0.14f))
                            .clickable {
                                sleepTimerManager.cancelTimer()
                                onDismiss()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                } else {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.action_close),
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.timer_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                        ),
                    ) {
                        append(chosenMinutes.toString())
                    }
                    withStyle(
                        SpanStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary.copy(alpha = 0.85f),
                        ),
                    ) {
                        append("min")
                    }
                },
            )

            Spacer(modifier = Modifier.height(4.dp))

            Canvas(modifier = Modifier.size(width = 14.dp, height = 9.dp)) {
                val pointerTriangle = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width / 2f, size.height)
                    close()
                }
                drawPath(path = pointerTriangle, color = Color.White)
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalMinuteDial(
                selectedMinutes = chosenMinutes,
                onMinutesSelected = { updated ->
                    if (updated != chosenMinutes) {
                        chosenMinutes = updated
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                sleepTimerManager.presetMinutes.forEach { presetMin ->
                    val isCurrentChoice = chosenMinutes == presetMin
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (isCurrentChoice) DeepSleepBlue else PresetPillUnselectedBg)
                            .border(
                                width = 1.dp,
                                color = if (isCurrentChoice) DeepSleepBlue else GlassOverlay,
                                shape = CircleShape,
                            )
                            .clickable {
                                if (chosenMinutes != presetMin) {
                                    chosenMinutes = presetMin
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${presetMin}'",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isCurrentChoice) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrentChoice) Color.White else TextSecondary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(27.dp))
                    .background(DeepSleepBlue)
                    .clickable {
                        sleepTimerManager.startTimer(chosenMinutes)
                        onDismiss()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.timer_custom_start),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
