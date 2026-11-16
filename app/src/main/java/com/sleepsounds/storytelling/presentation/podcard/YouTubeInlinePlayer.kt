package com.sleepsounds.storytelling.presentation.podcard

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Lifecycle-aware embedded YouTube player view for video-backed podcards.
 * Reuses the initialized WebView instance when switching between related video IDs for instant playback.
 */
@Composable
fun YouTubeInlinePlayer(
    videoId: String,
    onEnded: () -> Unit,
    onError: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 0.dp,
    onDurationKnown: (durationSeconds: Float) -> Unit = {},
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnEnded by rememberUpdatedState(onEnded)
    val latestOnError by rememberUpdatedState(onError)
    val latestOnDuration by rememberUpdatedState(onDurationKnown)
    val isActive = remember { AtomicBoolean(true) }
    val requestedVideoIdRef = remember { AtomicReference(videoId) }
    val loadedVideoIdRef = remember { AtomicReference<String?>(null) }
    val activePlayerRef = remember { AtomicReference<YouTubePlayer?>(null) }
    val radiusPixels = with(LocalDensity.current) { cornerRadius.toPx() }

    LaunchedEffect(videoId) {
        requestedVideoIdRef.set(videoId)
        val player = activePlayerRef.get()
        if (isActive.get() && player != null && videoId.isNotBlank() && loadedVideoIdRef.get() != videoId) {
            loadedVideoIdRef.set(videoId)
            player.loadOrCueVideo(lifecycleOwner.lifecycle, videoId, 0f)
        }
    }

    AndroidView(
        modifier = modifier.clip(RoundedCornerShape(cornerRadius)),
        factory = { ctx ->
            YouTubePlayerView(ctx).apply {
                if (radiusPixels > 0f) {
                    outlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(view: View, outline: Outline) {
                            outline.setRoundRect(0, 0, view.width, view.height, radiusPixels)
                        }
                    }
                    clipToOutline = true
                }
                lifecycleOwner.lifecycle.addObserver(this)
                addYouTubePlayerListener(
                    object : AbstractYouTubePlayerListener() {
                        override fun onVideoDuration(
                            youTubePlayer: YouTubePlayer,
                            duration: Float,
                        ) {
                            if (isActive.get() && duration > 0f) {
                                latestOnDuration(duration)
                            }
                        }

                        override fun onStateChange(
                            youTubePlayer: YouTubePlayer,
                            state: PlayerConstants.PlayerState,
                        ) {
                            if (state == PlayerConstants.PlayerState.ENDED && isActive.get()) {
                                latestOnEnded()
                            }
                        }

                        override fun onError(
                            youTubePlayer: YouTubePlayer,
                            error: PlayerConstants.PlayerError,
                        ) {
                            if (!isActive.get()) return
                            if (error == PlayerConstants.PlayerError.HTML_5_PLAYER) return
                            latestOnError()
                        }
                    },
                )
                getYouTubePlayerWhenReady(
                    object : YouTubePlayerCallback {
                        override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                            activePlayerRef.set(youTubePlayer)
                            val targetId = requestedVideoIdRef.get()
                            if (isActive.get() && targetId.isNotBlank()) {
                                loadedVideoIdRef.set(targetId)
                                youTubePlayer.loadOrCueVideo(lifecycleOwner.lifecycle, targetId, 0f)
                            }
                        }
                    },
                )
            }
        },
        update = { _ ->
            requestedVideoIdRef.set(videoId)
            val player = activePlayerRef.get()
            if (isActive.get() && player != null && videoId.isNotBlank() && loadedVideoIdRef.get() != videoId) {
                loadedVideoIdRef.set(videoId)
                player.loadOrCueVideo(lifecycleOwner.lifecycle, videoId, 0f)
            }
        },
        onRelease = { playerView ->
            isActive.set(false)
            activePlayerRef.set(null)
            loadedVideoIdRef.set(null)
            val hostLifecycle = lifecycleOwner.lifecycle
            hostLifecycle.removeObserver(playerView)
            if (hostLifecycle.currentState != Lifecycle.State.DESTROYED) {
                playerView.release()
            }
        },
    )
}
