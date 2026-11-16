package com.sleepsounds.storytelling.presentation.meditate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.ads.AdaptiveBannerAdBar
import com.sleepsounds.storytelling.presentation.components.BreathingPhase
import com.sleepsounds.storytelling.presentation.components.BreathingVisualizer
import com.sleepsounds.storytelling.ui.theme.ImmersiveSystemBars
import com.sleepsounds.storytelling.ui.theme.MidnightBlue
import com.sleepsounds.storytelling.ui.theme.PastelPurple
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.rememberResponsiveLayoutInfo
import com.sleepsounds.storytelling.ui.theme.systemSafePadding
import kotlinx.coroutines.delay

/**
 * Guided 4-7-8 paced breathing exercise screen.
 */
@Composable
fun BreathingScreen(
    onBack: () -> Unit,
) {
    ImmersiveSystemBars()

    val layoutMetrics = rememberResponsiveLayoutInfo()
    val circleDiameter = when {
        layoutMetrics.isCompactHeight -> 150.dp
        layoutMetrics.isTabletOrFoldable -> 260.dp
        else -> 220.dp
    }

    var currentPhase by remember { mutableStateOf(BreathingPhase.INHALE) }
    var countdownSeconds by remember { mutableIntStateOf(4) }

    LaunchedEffect(Unit) {
        val cycleStages = listOf(
            BreathingPhase.INHALE to 4,
            BreathingPhase.HOLD to 7,
            BreathingPhase.EXHALE to 8,
        )
        while (true) {
            for ((stage, durationSec) in cycleStages) {
                currentPhase = stage
                for (sec in durationSec downTo 1) {
                    countdownSeconds = sec
                    delay(1_000L)
                }
            }
        }
    }

    val instructionLabel = when (currentPhase) {
        BreathingPhase.INHALE -> stringResource(R.string.breathing_inhale)
        BreathingPhase.HOLD -> stringResource(R.string.breathing_hold)
        BreathingPhase.EXHALE -> stringResource(R.string.breathing_exhale)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlue),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemSafePadding()
                .verticalScroll(rememberScrollState())
                .padding(if (layoutMetrics.isCompactHeight) 16.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AdaptiveBannerAdBar(slotKey = "breathing_screen_banner")
            Spacer(modifier = Modifier.height(16.dp))
            BreathingVisualizer(size = circleDiameter, phase = currentPhase)
            Spacer(modifier = Modifier.height(if (layoutMetrics.isCompactHeight) 16.dp else 32.dp))
            Text(
                text = instructionLabel,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${countdownSeconds}s",
                style = MaterialTheme.typography.titleLarge,
                color = PastelPurple,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.breathing_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(onClick = onBack) {
                Text(stringResource(R.string.action_done))
            }
        }
    }
}
