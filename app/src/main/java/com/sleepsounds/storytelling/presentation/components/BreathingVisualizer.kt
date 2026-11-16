package com.sleepsounds.storytelling.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sleepsounds.storytelling.ui.theme.PastelPurple
import com.sleepsounds.storytelling.ui.theme.SurfaceBlueLight
import kotlinx.coroutines.launch

@Composable
private fun ConcentricBreathOrbs(
    modifier: Modifier,
    diameter: Dp,
    scaleFactor: Float,
    opacity: Float,
) {
    Box(
        modifier = modifier.size(diameter),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(diameter)
                .scale(scaleFactor)
                .alpha(opacity * 0.4f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(PastelPurple.copy(alpha = 0.5f), SurfaceBlueLight),
                    ),
                    shape = CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .size(diameter * 0.65f)
                .scale(scaleFactor * 0.9f)
                .alpha(opacity)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(PastelPurple, PastelPurple.copy(alpha = 0.3f)),
                    ),
                    shape = CircleShape,
                ),
        )
    }
}
/**
 * Animated concentric circle visualizer for guided 4-7-8 breathing sessions.
 */
@Composable
fun BreathingVisualizer(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    phase: BreathingPhase? = null,
) {
    if (phase == null) {
        val ambientLoop = rememberInfiniteTransition(label = "ambient_breath")
        val loopScale by ambientLoop.animateFloat(
            initialValue = 0.75f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "loop_scale",
        )
        val loopOpacity by ambientLoop.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.75f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "loop_opacity",
        )
        ConcentricBreathOrbs(
            modifier = modifier,
            diameter = size,
            scaleFactor = loopScale,
            opacity = loopOpacity,
        )
        return
    }

    val orbScale = remember { Animatable(0.75f) }
    val orbOpacity = remember { Animatable(0.35f) }

    LaunchedEffect(phase) {
        when (phase) {
            BreathingPhase.INHALE -> {
                launch {
                    orbScale.animateTo(1.15f, tween(durationMillis = 4000, easing = FastOutSlowInEasing))
                }
                launch {
                    orbOpacity.animateTo(0.78f, tween(durationMillis = 4000, easing = FastOutSlowInEasing))
                }
            }
            BreathingPhase.HOLD -> {
                orbScale.snapTo(1.15f)
                orbOpacity.snapTo(0.78f)
            }
            BreathingPhase.EXHALE -> {
                launch {
                    orbScale.animateTo(0.75f, tween(durationMillis = 8000, easing = FastOutSlowInEasing))
                }
                launch {
                    orbOpacity.animateTo(0.35f, tween(durationMillis = 8000, easing = FastOutSlowInEasing))
                }
            }
        }
    }

    ConcentricBreathOrbs(
        modifier = modifier,
        diameter = size,
        scaleFactor = orbScale.value,
        opacity = orbOpacity.value,
    )
}
enum class BreathingPhase {
    INHALE,
    HOLD,
    EXHALE,
}
