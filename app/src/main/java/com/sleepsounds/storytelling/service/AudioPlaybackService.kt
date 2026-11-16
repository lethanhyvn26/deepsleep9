package com.sleepsounds.storytelling.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.LocaleList
import androidx.annotation.OptIn
import androidx.core.app.ServiceCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.sleepsounds.storytelling.MainActivity
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.data.repository.UserPreferencesRepository
import com.sleepsounds.storytelling.ui.theme.translateTextForLanguage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground Media3 [MediaSessionService] hosting multi-track sleep audio and localized lockscreen controls.
 */
@OptIn(UnstableApi::class)
@AndroidEntryPoint
class AudioPlaybackService : MediaSessionService(), AudioServiceCallback {

    @Inject
    lateinit var audioPlayerManager: AudioPlayerManager

    @Inject
    lateinit var sleepTimerManager: SleepTimerManager

    @Inject
    lateinit var playbackNotificationBuilder: PlaybackNotificationBuilder

    @Inject
    lateinit var notificationArtworkLoader: NotificationArtworkLoader

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private var activeMediaSession: MediaSession? = null
    private var anchorExoPlayer: ExoPlayer? = null
    private var runningInForeground = false
    private var cachedCoverBitmap: Bitmap? = null
    private var currentSessionRoute: String? = null
    private var idleShutdownJob: Job? = null
    private val serviceCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Volatile
    private var activeLanguageCode: String = UserPreferencesRepository.LANGUAGE_EN

    private fun localizedServiceContext(): Context = runCatching {
        val locale = UserPreferencesRepository.localeForLanguage(activeLanguageCode)
        val config = Configuration(resources.configuration).apply {
            setLocales(LocaleList(locale))
            setLayoutDirection(locale)
        }
        createConfigurationContext(config)
    }.getOrDefault(this)

    override fun onCreate() {
        super.onCreate()
        ensurePlaybackNotificationChannel()
        cachedCoverBitmap = notificationArtworkLoader.getSharedHdBitmapSync()
        setForegroundServiceTimeoutMs(PAUSED_FOREGROUND_TIMEOUT_MS)

        val basePlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                false,
            )
            .setHandleAudioBecomingNoisy(false)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
        anchorExoPlayer = basePlayer

        val sessionBridgePlayer = object : ForwardingPlayer(basePlayer) {
            override fun play() {
                if (audioPlayerManager.hasActivePlayback()) {
                    audioPlayerManager.resumeAll()
                } else {
                    super.play()
                }
            }

            override fun pause() {
                if (audioPlayerManager.hasActivePlayback()) {
                    audioPlayerManager.pauseAll()
                } else {
                    super.pause()
                }
            }

            override fun stop() {
                haltPlaybackAndService()
            }

            override fun isPlaying(): Boolean {
                val snap = audioPlayerManager.playbackState.value
                return snap.mode != PlaybackMode.IDLE && snap.isPlaying
            }

            override fun getPlayWhenReady(): Boolean {
                val snap = audioPlayerManager.playbackState.value
                return snap.mode != PlaybackMode.IDLE && snap.isPlaying
            }

            override fun getPlaybackState(): Int =
                if (audioPlayerManager.hasActivePlayback()) STATE_READY else STATE_IDLE

            override fun getAvailableCommands(): Player.Commands =
                Player.Commands.Builder()
                    .add(COMMAND_PLAY_PAUSE)
                    .add(COMMAND_STOP)
                    .add(COMMAND_GET_METADATA)
                    .build()

            override fun getMediaMetadata(): MediaMetadata =
                composeMediaMetadata(audioPlayerManager.playbackState.value)
        }
        alignAnchorPlayerState()

        val startRoute = PlaybackDeepLinks.routeFor(audioPlayerManager.playbackState.value)
        currentSessionRoute = startRoute
        activeMediaSession = MediaSession.Builder(this, sessionBridgePlayer)
            .setSessionActivity(buildDeepLinkPendingIntent(startRoute))
            .build()
            .also(::addSession)

        refreshSessionAndForegroundNotification()
        audioPlayerManager.attachHost(this)

        serviceCoroutineScope.launch {
            userPreferencesRepository.appLanguage.collect { lang ->
                activeLanguageCode = lang
                ensurePlaybackNotificationChannel()
                if (audioPlayerManager.hasActivePlayback() || runningInForeground) {
                    refreshSessionAndForegroundNotification()
                }
            }
        }

        serviceCoroutineScope.launch {
            sleepTimerManager.state.collect {
                refreshSessionAndForegroundNotification()
            }
        }

        serviceCoroutineScope.launch {
            audioPlayerManager.playbackState.collect { snap ->
                if (snap.mode != PlaybackMode.IDLE) {
                    refreshSessionAndForegroundNotification()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        idleShutdownJob?.cancel()
        refreshSessionAndForegroundNotification()
        audioPlayerManager.attachHost(this)

        when (intent?.action) {
            ACTION_TOGGLE_PLAYBACK -> {
                if (audioPlayerManager.playbackState.value.isPlaying) {
                    audioPlayerManager.pauseAll()
                } else {
                    audioPlayerManager.resumeAll()
                }
                refreshSessionAndForegroundNotification()
                return START_STICKY
            }
            ACTION_STOP -> {
                haltPlaybackAndService()
                return START_NOT_STICKY
            }
        }

        if (intent == null && !audioPlayerManager.hasActivePlayback()) {
            runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
            runningInForeground = false
            stopSelf()
            return START_NOT_STICKY
        }

        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (audioPlayerManager.playbackState.value.mode != PlaybackMode.IDLE) {
            idleShutdownJob?.cancel()
            refreshSessionAndForegroundNotification()
            return
        }
        idleShutdownJob?.cancel()
        haltPlaybackAndService()
        audioPlayerManager.detachHost(this)
        stopSelf()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        activeMediaSession

    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        postForegroundNotification()
    }

    override fun onPlaybackStarted() {
        idleShutdownJob?.cancel()
        refreshSessionAndForegroundNotification()
    }

    override fun onPlaybackStopped() {
        idleShutdownJob?.cancel()
        cachedCoverBitmap = null
        currentSessionRoute = null
        notificationArtworkLoader.clearCache()
        idleShutdownJob = serviceCoroutineScope.launch {
            delay(IDLE_STOP_DELAY_MS)
            if (!audioPlayerManager.hasActivePlayback()) {
                runningInForeground = false
                runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
                audioPlayerManager.detachHost(this@AudioPlaybackService)
                stopSelf()
            }
        }
    }

    override fun onPlaybackStateChanged(state: PlaybackState) {
        refreshSessionAndForegroundNotification()
    }

    override fun onDestroy() {
        idleShutdownJob?.cancel()
        serviceCoroutineScope.cancel()
        audioPlayerManager.detachHost(this)
        activeMediaSession?.run {
            player.release()
            release()
        }
        activeMediaSession = null
        anchorExoPlayer = null
        cachedCoverBitmap = null
        notificationArtworkLoader.clearCache()
        runningInForeground = false
        super.onDestroy()
    }

    private fun refreshSessionAndForegroundNotification() {
        alignAnchorPlayerState()
        refreshSessionPendingIntentIfChanged()
        postForegroundNotification()
        ensureArtworkLoaded()
    }

    private fun alignAnchorPlayerState() {
        val snap = audioPlayerManager.playbackState.value
        val player = anchorExoPlayer ?: return
        val activePlaying = snap.mode != PlaybackMode.IDLE && snap.isPlaying
        if (player.playWhenReady != activePlaying) {
            player.playWhenReady = activePlaying
        }
    }

    private fun refreshSessionPendingIntentIfChanged() {
        val session = activeMediaSession ?: return
        val targetRoute = PlaybackDeepLinks.routeFor(audioPlayerManager.playbackState.value)
        if (targetRoute == currentSessionRoute) return
        currentSessionRoute = targetRoute
        session.setSessionActivity(buildDeepLinkPendingIntent(targetRoute))
    }

    private fun ensureArtworkLoaded() {
        val snap = audioPlayerManager.playbackState.value
        if (snap.mode == PlaybackMode.IDLE) return

        serviceCoroutineScope.launch {
            val loaded = notificationArtworkLoader.loadForPlayback()
            if (loaded != cachedCoverBitmap) {
                cachedCoverBitmap = loaded
                postForegroundNotification()
            }
        }
    }

    private fun postForegroundNotification() {
        val session = activeMediaSession ?: return
        val snap = audioPlayerManager.playbackState.value
        val localCtx = localizedServiceContext()
        if (snap.mode == PlaybackMode.IDLE) {
            runCatching {
                val idleNote = playbackNotificationBuilder.buildIdlePlaceholder(localCtx, session)
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    idleNote,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
                )
                runningInForeground = true
            }
            return
        }

        runCatching {
            val activeNote = playbackNotificationBuilder.build(
                context = localCtx,
                session = session,
                playback = snap,
                timer = sleepTimerManager.state.value,
                artwork = cachedCoverBitmap,
                contentRoute = PlaybackDeepLinks.routeFor(snap),
            )
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                activeNote,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
            runningInForeground = true
        }
    }

    private fun haltPlaybackAndService() {
        sleepTimerManager.cancelTimer()
        audioPlayerManager.stopAll()
    }

    private fun buildDeepLinkPendingIntent(route: String): PendingIntent {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_ROUTE, route)
            putExtra(MainActivity.EXTRA_FROM_NOTIFICATION, true)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            route.hashCode(),
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun composeMediaMetadata(snap: PlaybackState): MediaMetadata {
        val localCtx = localizedServiceContext()
        val builder = MediaMetadata.Builder()
            .setTitle(snap.title.ifBlank { localCtx.getString(R.string.app_name) })
            .setArtist(resolveArtistSubtitle(localCtx, snap))
        notificationArtworkLoader.getSharedHdBytesSync()?.let { coverBytes ->
            builder.setArtworkData(coverBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
        }
        if (snap.mode == PlaybackMode.MIXER) {
            val layerNames = snap.activeSounds.joinToString(" · ") { it.name }.ifBlank { null }
            if (layerNames != null) {
                builder.setAlbumTitle(layerNames)
            }
        }
        return builder.build()
    }

    private fun resolveArtistSubtitle(localCtx: Context, snap: PlaybackState): String {
        val timerSnap = sleepTimerManager.state.value
        if (timerSnap.isActive && timerSnap.remainingSeconds > 0) {
            return localCtx.getString(
                R.string.notification_timer_active,
                sleepTimerManager.formatRemaining(timerSnap.remainingSeconds),
            )
        }
        return when (snap.mode) {
            PlaybackMode.MIXER -> {
                val layerTotal = snap.mixCount.coerceAtLeast(snap.activeSounds.size)
                localCtx.getString(R.string.notification_mix_count, layerTotal)
            }
            else -> snap.subtitle.takeIf { it.isNotBlank() }?.let { translateTextForLanguage(it, activeLanguageCode) } ?: localCtx.getString(R.string.notification_artist)
        }
    }

    private fun ensurePlaybackNotificationChannel() {
        runCatching {
            val localCtx = localizedServiceContext()
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                localCtx.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = localCtx.getString(R.string.notification_channel_desc)
                setShowBadge(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val ACTION_STOP = "com.sleepsounds.storytelling.action.STOP_PLAYBACK"
        const val ACTION_TOGGLE_PLAYBACK = "com.sleepsounds.storytelling.action.TOGGLE_PLAYBACK"
        const val NOTIFICATION_CHANNEL_ID = "deepsleep_playback"
        private const val NOTIFICATION_ID = 1001
        private const val IDLE_STOP_DELAY_MS = 1_500L
        private const val PAUSED_FOREGROUND_TIMEOUT_MS = 12L * 60L * 60L * 1000L
    }
}
