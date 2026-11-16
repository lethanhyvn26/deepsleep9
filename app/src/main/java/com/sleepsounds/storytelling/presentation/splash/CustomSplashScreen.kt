package com.sleepsounds.storytelling.presentation.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.ads.AdConfigManager
import com.sleepsounds.storytelling.ads.AdMobManager
import com.sleepsounds.storytelling.ads.AdaptiveBannerAdBar
import com.sleepsounds.storytelling.ads.findActivity
import com.sleepsounds.storytelling.ads.rememberAdMobManager
import com.sleepsounds.storytelling.ui.theme.DeepSleepBlue
import com.sleepsounds.storytelling.ui.theme.SurfaceBlue
import com.sleepsounds.storytelling.ui.theme.TextPrimary
import com.sleepsounds.storytelling.ui.theme.TextSecondary
import com.sleepsounds.storytelling.ui.theme.rememberResponsiveLayoutInfo
import com.sleepsounds.storytelling.ui.theme.tr
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

private val NightSkySplashGradient = Brush.verticalGradient(
    0f to Color(0xFF070C1F),
    0.45f to Color(0xFF0D1533),
    0.78f to Color(0xFF090F26),
    1f to Color(0xFF050814),
)

private fun buildStarField(starCount: Int): List<StarPoint> {
    val rng = kotlin.random.Random(seed = 108)
    return List(starCount) {
        StarPoint(
            xRatio = rng.nextFloat(),
            yRatio = rng.nextFloat() * 0.88f,
            radiusPx = 1.0f + rng.nextFloat() * 2.0f,
            baseOpacity = 0.16f + rng.nextFloat() * 0.36f,
            phaseOffset = rng.nextFloat(),
        )
    }
}

private data class StarPoint(
    val xRatio: Float,
    val yRatio: Float,
    val radiusPx: Float,
    val baseOpacity: Float,
    val phaseOffset: Float,
)

@Composable
private fun CrescentMoonArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    DeepSleepBlue.copy(alpha = 0.38f),
                    Color(0xFF1E3A8A).copy(alpha = 0.15f),
                    Color.Transparent,
                ),
                center = Offset(w * 0.52f, h * 0.44f),
                radius = w * 0.42f,
            ),
            center = Offset(w * 0.52f, h * 0.44f),
            radius = w * 0.42f,
        )

        val sparkles = listOf(
            Offset(w * 0.22f, h * 0.22f) to w * 0.030f,
            Offset(w * 0.78f, h * 0.25f) to w * 0.026f,
            Offset(w * 0.28f, h * 0.46f) to w * 0.022f,
            Offset(w * 0.82f, h * 0.52f) to w * 0.020f,
        )
        sparkles.forEach { (pt, rad) ->
            drawCircle(color = Color(0xFFE2E8F0), radius = rad, center = pt)
        }

        val moonOrigin = Offset(w * 0.53f, h * 0.43f)
        val moonRadius = w * 0.24f
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFFE082), Color(0xFFFFD54F), Color(0xFFFFB300)),
                start = Offset(moonOrigin.x - moonRadius, moonOrigin.y - moonRadius),
                end = Offset(moonOrigin.x + moonRadius, moonOrigin.y + moonRadius),
            ),
            radius = moonRadius,
            center = moonOrigin,
        )
        drawCircle(
            color = Color(0xFF121E40),
            radius = moonRadius * 0.84f,
            center = Offset(moonOrigin.x - moonRadius * 0.34f, moonOrigin.y - moonRadius * 0.24f),
        )

        val backCloud = Color(0xFF1E3266)
        val frontCloud = Color(0xFF132046)
        drawCircle(color = backCloud, radius = w * 0.22f, center = Offset(w * 0.22f, h * 0.78f))
        drawCircle(color = backCloud, radius = w * 0.24f, center = Offset(w * 0.50f, h * 0.76f))
        drawCircle(color = backCloud, radius = w * 0.22f, center = Offset(w * 0.78f, h * 0.78f))

        drawCircle(color = frontCloud, radius = w * 0.24f, center = Offset(w * 0.16f, h * 0.88f))
        drawCircle(color = frontCloud, radius = w * 0.26f, center = Offset(w * 0.42f, h * 0.86f))
        drawCircle(color = frontCloud, radius = w * 0.25f, center = Offset(w * 0.68f, h * 0.87f))
        drawCircle(color = frontCloud, radius = w * 0.22f, center = Offset(w * 0.88f, h * 0.89f))
    }
}

@Composable
private fun DeepSleepSplashOverlay() {
    val context = LocalContext.current
    val adMobManager = rememberAdMobManager()
    val responsive = rememberResponsiveLayoutInfo()
    val compactHeight = responsive.isCompactHeight
    val tabletMode = responsive.isTabletOrFoldable

    val infiniteAnim = rememberInfiniteTransition(label = "splash_ambient")
    val pulseScale by infiniteAnim.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "emblem_pulse",
    )
    val starCycle by infiniteAnim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "star_cycle",
    )

    val mainActivity = remember(context) {
        context.findActivity() as? com.sleepsounds.storytelling.MainActivity
    }
    val coordinatorProgressFlow = remember(mainActivity) {
        mainActivity?.startupSplashCoordinator?.splashProgress
    }
    val coordinatorProgress by (coordinatorProgressFlow?.collectAsStateWithLifecycle()
        ?: remember { androidx.compose.runtime.mutableFloatStateOf(0.08f) })
    val animatedBarProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = coordinatorProgress.coerceIn(0.06f, 1.0f),
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "splash_bar_progress",
    )

    val emblemReveal = remember { Animatable(0f) }
    val titleReveal = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            emblemReveal.animateTo(1f, tween(durationMillis = 340, easing = FastOutSlowInEasing))
        }
        launch {
            delay(70L)
            titleReveal.animateTo(1f, tween(durationMillis = 300, easing = FastOutSlowInEasing))
        }
    }

    val starField = remember { buildStarField(starCount = 30) }
    val badgeDimension = when {
        compactHeight -> 124.dp
        tabletMode -> 172.dp
        else -> 148.dp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightSkySplashGradient)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach { it.consume() }
                    }
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        DeepSleepBlue.copy(alpha = 0.22f),
                        SurfaceBlue.copy(alpha = 0.10f),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.80f, size.height * 0.40f),
                    radius = size.width * 0.84f,
                ),
                center = Offset(size.width * 0.80f, size.height * 0.40f),
                radius = size.width * 0.84f,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A).copy(alpha = 0.24f),
                        Color(0xFF0F172A).copy(alpha = 0.10f),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.20f, size.height * 0.76f),
                    radius = size.width * 0.76f,
                ),
                center = Offset(size.width * 0.20f, size.height * 0.76f),
                radius = size.width * 0.76f,
            )

            starField.forEach { pt ->
                val wave = 0.5f + 0.5f * sin(2.0 * PI * (starCycle + pt.phaseOffset)).toFloat()
                val alpha = pt.baseOpacity * (0.25f + 0.55f * wave)
                drawCircle(
                    color = Color(0xFFCBD5E1).copy(alpha = alpha),
                    radius = pt.radiusPx,
                    center = Offset(pt.xRatio * size.width, pt.yRatio * size.height),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = if (compactHeight) 74.dp else 136.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(badgeDimension)
                    .graphicsLayer {
                        val currentScale = (0.90f + 0.10f * emblemReveal.value) * pulseScale
                        scaleX = currentScale
                        scaleY = currentScale
                        alpha = emblemReveal.value
                    }
                    .clip(RoundedCornerShape(34.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0B132B),
                                Color(0xFF152248),
                                Color(0xFF0E1733),
                            ),
                        ),
                    )
                    .border(
                        width = 1.5.dp,
                        color = DeepSleepBlue.copy(alpha = 0.42f),
                        shape = RoundedCornerShape(34.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                CrescentMoonArtwork(modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.height(if (compactHeight) 16.dp else 22.dp))

            Text(
                text = stringResource(R.string.app_name),
                color = TextPrimary,
                fontSize = if (compactHeight) 23.sp else if (tabletMode) 30.sp else 27.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = titleReveal.value
                    translationY = (1f - titleReveal.value) * 16f
                },
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = tr(
                    "Drift into deep, restful sleep",
                    "Mergulhe em um sono profundo e tranquilo",
                    "Chìm vào giấc ngủ sâu và thư thái",
                ),
                color = TextSecondary.copy(alpha = 0.88f),
                fontSize = if (tabletMode) 14.5.sp else 13.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = titleReveal.value * 0.9f },
            )
        }

        // Bottom Splash Progress + `banner_splash` (Modeled 100% after `activity_splash.xml` in the reference APK)
        val progressPercent = (animatedBarProgress * 100f).toInt().coerceIn(8, 100)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LinearProgressIndicator(
                progress = { animatedBarProgress },
                color = DeepSleepBlue,
                trackColor = Color.White.copy(alpha = 0.16f),
                modifier = Modifier
                    .width(236.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .graphicsLayer { alpha = titleReveal.value },
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${
                    tr(
                        "Loading ads & soundscapes...",
                        "Carregando sons...",
                        "Đang tải dữ liệu...",
                    )
                } ($progressPercent%)",
                color = TextSecondary.copy(alpha = 0.88f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.graphicsLayer { alpha = titleReveal.value },
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Dedicated Bottom Splash Banner (`banner_splash`) with Shimmer Skeleton
            AdaptiveBannerAdBar(
                adMobManager = adMobManager,
                slotKey = AdConfigManager.PLACEMENT_BANNER_SPLASH,
                allowDuringSplash = true,
                fullBleedSticky = true,
            )
        }
    }
}

@Composable
fun CustomSplashScreen(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(120)),
        exit = fadeOut(tween(180)),
    ) {
        DeepSleepSplashOverlay()
    }
}

/**
 * Modeled 100% after `WelcomeBackActivity` (`activity_welcome_back.xml`) in the reference APK:
 * Shown when the user returns to the app from background, presenting the app emblem,
 * `"Welcome back"` greeting, a `"Continue"` button, and triggering `inter_welcome_back` / `AppOpenAd`.
 */
@Composable
fun WelcomeBackOverlay(
    visible: Boolean,
    adMobManager: AdMobManager,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(160)),
        exit = fadeOut(tween(180)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NightSkySplashGradient)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent().changes.forEach { it.consume() }
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF0B132B),
                                    Color(0xFF152248),
                                    Color(0xFF0E1733),
                                ),
                            ),
                        )
                        .border(
                            width = 1.5.dp,
                            color = DeepSleepBlue.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(28.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CrescentMoonArtwork(modifier = Modifier.fillMaxSize())
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = tr("Welcome back!", "Bem-vindo de volta!", "Chào mừng bạn quay lại!"),
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = tr(
                        "Resuming your peaceful sleep space...",
                        "Retomando seu espaço de sono...",
                        "Đang khôi phục không gian thư giãn của bạn...",
                    ),
                    color = TextSecondary,
                    fontSize = 13.5.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(26.dp))

                CircularProgressIndicator(
                    color = DeepSleepBlue,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(32.dp),
                )

                Spacer(modifier = Modifier.height(28.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(DeepSleepBlue, Color(0xFF3B66FF)),
                            ),
                        )
                        .clickable {
                            adMobManager.showWelcomeBackAd(activity) {
                                adMobManager.dismissWelcomeBackOverlay()
                            }
                        }
                        .padding(horizontal = 36.dp, vertical = 13.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tr("Continue", "Continuar", "Tiếp tục"),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
