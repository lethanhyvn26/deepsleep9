package com.sleepsounds.storytelling.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import com.sleepsounds.storytelling.MainActivity
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.ui.theme.translateTextForLanguage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Constructs Media3 transport notifications for active soundscapes, stories, and sleep timers.
 */
@UnstableApi
@Singleton
class PlaybackNotificationBuilder @Inject constructor() {

    fun build(
        context: Context,
        session: MediaSession,
        playback: PlaybackState,
        timer: SleepTimerState,
        artwork: Bitmap?,
        contentRoute: String = PlaybackDeepLinks.routeFor(playback),
    ): Notification {
        val resolvedTitle = resolveNotificationTitle(context, playback)
        val statusText = resolveStatusLine(context, playback)
        val detailText = resolveSecondaryLine(context, playback, timer)

        val launchIntent = createRoutePendingIntent(context, contentRoute)
        val playPausePendingIntent = createServiceActionIntent(
            context = context,
            action = AudioPlaybackService.ACTION_TOGGLE_PLAYBACK,
            requestCode = REQUEST_CODE_TOGGLE,
        )
        val stopPendingIntent = createServiceActionIntent(
            context = context,
            action = AudioPlaybackService.ACTION_STOP,
            requestCode = REQUEST_CODE_STOP,
        )

        val playing = playback.isPlaying
        val transportIconRes = if (playing) R.drawable.ic_notif_pause else R.drawable.ic_notif_play
        val transportLabel = context.getString(
            if (playing) R.string.now_playing_pause else R.string.now_playing_play,
        )

        val notificationBuilder = NotificationCompat.Builder(
            context,
            AudioPlaybackService.NOTIFICATION_CHANNEL_ID,
        )
            .setSmallIcon(R.drawable.ic_stat_deepsleep)
            .setContentTitle(resolvedTitle)
            .setContentText(statusText)
            .setContentIntent(launchIntent)
            .setDeleteIntent(stopPendingIntent)
            .setLargeIcon(artwork ?: NotificationArtworkLoader.decodeSharedHdAsset(context))
            .setColor(ACCENT_NOTIFICATION_COLOR)
            .setColorized(true)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(transportIconRes, transportLabel, playPausePendingIntent)
            .addAction(
                R.drawable.ic_notif_close,
                context.getString(R.string.notification_close),
                stopPendingIntent,
            )
            .setStyle(
                MediaStyleNotificationHelper.MediaStyle(session)
                    .setShowActionsInCompactView(0, 1),
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationBuilder.setForegroundServiceBehavior(
                NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE,
            )
        }

        if (!detailText.isNullOrBlank()) {
            notificationBuilder.setSubText(detailText)
        }

        if (timer.isActive && timer.totalSeconds > 0) {
            notificationBuilder.setProgress(
                timer.totalSeconds,
                timer.remainingSeconds.coerceAtLeast(0),
                false,
            )
            notificationBuilder.setContentText(resolveTimerSummaryLine(context, playback, timer))
            notificationBuilder.setSubText(statusText)
        } else {
            notificationBuilder.setProgress(0, 0, false)
        }

        return notificationBuilder.build()
    }

    fun buildIdlePlaceholder(
        context: Context,
        session: MediaSession,
    ): Notification {
        val builder = NotificationCompat.Builder(
            context,
            AudioPlaybackService.NOTIFICATION_CHANNEL_ID,
        )
            .setSmallIcon(R.drawable.ic_stat_deepsleep)
            .setLargeIcon(NotificationArtworkLoader.decodeSharedHdAsset(context))
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.notification_status_playing))
            .setContentIntent(createDefaultLaunchIntent(context))
            .setColor(ACCENT_NOTIFICATION_COLOR)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setShowWhen(false)
            .setStyle(MediaStyleNotificationHelper.MediaStyle(session))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        }
        return builder.build()
    }

    private fun resolveNotificationTitle(context: Context, playback: PlaybackState): String {
        val appTitle = context.getString(R.string.app_name)
        return when (playback.mode) {
            PlaybackMode.MIXER -> {
                val mixTitle = playback.title.ifBlank {
                    playback.activeSounds.joinToString(" + ") { it.name }
                }
                mixTitle.ifBlank { context.getString(R.string.now_playing_current_mix) }
            }
            else -> playback.title.ifBlank { appTitle }
        }
    }

    private fun resolveStatusLine(context: Context, playback: PlaybackState): String {
        val stateLabel = if (playback.isPlaying) {
            context.getString(R.string.notification_status_playing)
        } else {
            context.getString(R.string.notification_status_paused)
        }

        return when (playback.mode) {
            PlaybackMode.MIXER -> {
                val activeLayerCount = playback.mixCount.coerceAtLeast(playback.activeSounds.size)
                context.getString(R.string.notification_mix_status, stateLabel, activeLayerCount)
            }
            PlaybackMode.PODCARD -> context.getString(
                R.string.notification_podcard_status,
                stateLabel,
            )
            PlaybackMode.SINGLE -> stateLabel
            PlaybackMode.IDLE -> context.getString(R.string.notification_artist)
        }
    }

    private fun resolveTimerSummaryLine(
        context: Context,
        playback: PlaybackState,
        timer: SleepTimerState,
    ): String {
        val clockText = formatCountdown(timer.remainingSeconds)
        val timerLabel = if (timer.isFading) {
            context.getString(R.string.notification_timer_fading, clockText)
        } else {
            context.getString(R.string.notification_timer_active, clockText)
        }
        return if (playback.mode == PlaybackMode.MIXER) {
            val activeCount = playback.mixCount.coerceAtLeast(playback.activeSounds.size)
            val layersLabel = context.getString(R.string.notification_mix_count, activeCount)
            "$timerLabel · $layersLabel"
        } else {
            timerLabel
        }
    }

    private fun resolveSecondaryLine(
        context: Context,
        playback: PlaybackState,
        timer: SleepTimerState,
    ): String? {
        if (timer.isActive && timer.remainingSeconds > 0) return null
        return when (playback.mode) {
            PlaybackMode.MIXER -> playback.activeSounds.joinToString(" · ") { layer ->
                context.getString(
                    R.string.notification_mix_layer,
                    layer.name,
                    (layer.volume * 100).toInt(),
                )
            }.ifBlank { null }
            else -> playback.subtitle.takeIf { it.isNotBlank() }?.let { sub ->
                val langTag = context.resources.configuration.locales.get(0)?.toLanguageTag().orEmpty()
                translateTextForLanguage(sub, langTag)
            }
        }
    }

    private fun formatCountdown(totalSec: Int): String {
        val minPart = totalSec / 60
        val secPart = totalSec % 60
        return "%d:%02d".format(minPart, secPart)
    }

    private fun createRoutePendingIntent(context: Context, route: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_ROUTE, route)
            putExtra(MainActivity.EXTRA_FROM_NOTIFICATION, true)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            route.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun createDefaultLaunchIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_FROM_NOTIFICATION, true)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun createServiceActionIntent(
        context: Context,
        action: String,
        requestCode: Int,
    ): PendingIntent = PendingIntent.getForegroundService(
        context,
        requestCode,
        Intent(context, AudioPlaybackService::class.java).setAction(action),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    companion object {
        private const val ACCENT_NOTIFICATION_COLOR = 0xFFB39DDB.toInt()
        private const val REQUEST_CODE_TOGGLE = 10
        private const val REQUEST_CODE_STOP = 11
    }
}
