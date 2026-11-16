package com.sleepsounds.storytelling.service

import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.sleepsounds.storytelling.data.audio.AudioSourceType
import com.sleepsounds.storytelling.data.audio.AudioUriResolver
import com.sleepsounds.storytelling.data.audio.ResolvedAudio
import com.sleepsounds.storytelling.data.local.dao.PlaybackHistoryDao
import com.sleepsounds.storytelling.data.local.dao.SoundDao
import com.sleepsounds.storytelling.data.local.entity.PlaybackHistoryEntity
import com.sleepsounds.storytelling.data.media.MediaFileRepository
import com.sleepsounds.storytelling.domain.model.HistoryItemType
import com.sleepsounds.storytelling.presentation.navigation.AppRoute
import com.sleepsounds.storytelling.util.R2Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Active operating mode of the Media3 playback engine.
 */
enum class PlaybackMode {
    IDLE,
    SINGLE,
    MIXER,
    PODCARD,
}

/**
 * Represents a single active loop sound layer inside the multi-track mixer.
 */
data class ActiveSoundPlayback(
    val soundId: String,
    val name: String,
    val volume: Float,
)

/**
 * Immutable snapshot of the audio engine's playback state observed by UI and notifications.
 */
data class PlaybackState(
    val mode: PlaybackMode = PlaybackMode.IDLE,
    val isPlaying: Boolean = false,
    val title: String = "",
    val subtitle: String = "",
    val currentTrackId: String? = null,
    val iconCategory: String? = null,
    val activeSounds: List<ActiveSoundPlayback> = emptyList(),
    val isRemoteSource: Boolean = false,
    val mixCount: Int = 0,
    val chapterId: String? = null,
    val chapterIndex: Int = 0,
    val chapterTitle: String = "",
    val chapterCount: Int = 0,
    val backgroundSoundId: String? = null,
    val backgroundVolume: Float = 0.35f,
    val isRepeatEnabled: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val contentRoute: String? = null,
)

/**
 * Core Media3 / ExoPlayer engine managing single-track loops, multi-layer soundscapes (up to 5 simultaneous tracks),
 * and bedtime story voice + ambient background playback.
 */
@OptIn(UnstableApi::class)
@Singleton
class AudioPlayerManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val audioUriResolver: AudioUriResolver,
    private val mediaFileRepository: MediaFileRepository,
    private val soundDao: SoundDao,
    private val playbackHistoryDao: PlaybackHistoryDao,
    private val cacheDataSourceFactory: CacheDataSource.Factory,
    private val defaultDataSourceFactory: DefaultDataSource.Factory,
    private val playbackNavigationManager: PlaybackNavigationManager,
) {
    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val activeMixPlayers = mutableMapOf<String, ExoPlayer>()
    private val activeSoundTitles = mutableMapOf<String, String>()
    private val layerTargetVolumes = mutableMapOf<String, Float>()
    private val activeFadeJobs = mutableMapOf<String, Job>()

    private val mediaAudioAttributes = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .build()

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var primarySinglePlayer: ExoPlayer? = null
    private var storyVoicePlayer: ExoPlayer? = null
    private var storyAmbientPlayer: ExoPlayer? = null

    private var singleTrackBaseGain = 1f
    private var storyVoiceBaseGain = 1f
    private var storyAmbientBaseGain = 0.35f
    private var masterFadeScale = 1f

    private var serviceHostCallback: AudioServiceCallback? = null
    private var autoOpenPlayerScreen = false
    private var progressPollingJob: Job? = null

    @Volatile
    private var serviceHostBound = false
    private var hostBoundSignal: CompletableDeferred<Unit>? = null

    data class PresetPlaybackItem(
        val soundId: String,
        val name: String,
        val audioPath: String?,
        val volume: Float,
        val isPremium: Boolean,
    )

    fun attachHost(callback: AudioServiceCallback) {
        serviceHostCallback = callback
        serviceHostBound = true
        hostBoundSignal?.complete(Unit)
        hostBoundSignal = null
        if (hasActivePlayback()) {
            callback.onPlaybackStarted()
        }
    }

    fun detachHost(caller: AudioServiceCallback? = null) {
        if (caller != null && serviceHostCallback != null && serviceHostCallback !== caller) {
            return
        }
        serviceHostBound = false
        serviceHostCallback = null
        hostBoundSignal = null
        tearDownAllPlayers(notifyHostStopped = false)
    }

    fun setAutoNavigateEnabled(enabled: Boolean) {
        autoOpenPlayerScreen = enabled
    }

    fun isHostAttached(): Boolean = serviceHostBound

    fun playSingleTrack(
        trackId: String,
        audioFilePath: String? = null,
        title: String = trackId,
        isPremiumTrack: Boolean = false,
        userIsPremium: Boolean = false,
        navigateToPlayer: Boolean = false,
    ) {
        engineScope.launch {
            val resolvedAudio = audioUriResolver.resolveForPlayback(
                trackId = trackId,
                audioFilePath = audioFilePath,
                isPremiumTrack = isPremiumTrack,
                userIsPremium = userIsPremium,
            )
            startResolvedSingleTrack(resolvedAudio, title, isPremiumTrack, navigateToPlayer)
        }
    }

    fun playPodcard(
        podcardId: String,
        title: String,
        voiceAudioPath: String?,
        backgroundSoundId: String?,
        voiceTrackId: String = podcardId,
        chapterIndex: Int = 0,
        chapterTitle: String = "",
        chapterCount: Int = 0,
        durationMsHint: Long = 0L,
        isPremiumTrack: Boolean = false,
        userIsPremium: Boolean = false,
        navigateToPlayer: Boolean = false,
        contentRoute: String = PlaybackDeepLinks.podcardRoute(podcardId),
    ) {
        engineScope.launch {
            if (!ensureHostBound()) return@launch
            releaseMixerPlayers()
            releaseSinglePlayer()
            releaseStoryPlayers()

            val resolvedVoice = voiceAudioPath?.let { path ->
                audioUriResolver.resolveForPlayback(
                    trackId = voiceTrackId,
                    audioFilePath = path,
                    isPremiumTrack = isPremiumTrack,
                    userIsPremium = userIsPremium,
                )
            }

            val voiceExo = storyVoicePlayer ?: buildExoPlayer(requestAudioFocus = true)
                .also { storyVoicePlayer = it }

            if (resolvedVoice != null) {
                registerFallbackRecovery(voiceExo, resolvedVoice)
                registerDurationObserver(voiceExo)
                val voiceMediaSource = selectSourceFactory(resolvedVoice.sourceType).createMediaSource(
                    MediaItem.Builder()
                        .setUri(resolvedVoice.uri)
                        .setMediaId(voiceTrackId)
                        .build(),
                )
                voiceExo.setMediaSource(voiceMediaSource)
                voiceExo.prepare()
                storyVoiceBaseGain = 1f
                voiceExo.volume = storyVoiceBaseGain * masterFadeScale
                voiceExo.playWhenReady = true
                voiceExo.repeatMode = Player.REPEAT_MODE_ONE
            }

            if (!backgroundSoundId.isNullOrBlank()) {
                val ambientDbPath = soundDao.getById(backgroundSoundId)?.audioPath
                    ?.takeIf { it.isNotBlank() }
                    ?: "sounds/$backgroundSoundId/audio.ogg"
                val resolvedAmbient = audioUriResolver.resolveForPlayback(
                    trackId = backgroundSoundId,
                    audioFilePath = ambientDbPath,
                    isPremiumTrack = false,
                    userIsPremium = userIsPremium,
                )
                val ambientExo = storyAmbientPlayer ?: buildExoPlayer(requestAudioFocus = false)
                    .also { storyAmbientPlayer = it }
                registerFallbackRecovery(ambientExo, resolvedAmbient)
                val ambientSource = selectSourceFactory(resolvedAmbient.sourceType).createMediaSource(
                    MediaItem.fromUri(resolvedAmbient.uri),
                )
                ambientExo.setMediaSource(ambientSource)
                ambientExo.prepare()
                storyAmbientBaseGain = 0.35f
                ambientExo.volume = storyAmbientBaseGain * masterFadeScale
                ambientExo.playWhenReady = true
                ambientExo.repeatMode = Player.REPEAT_MODE_ONE
            }

            val resolvedSubtitle = chapterTitle.ifBlank { "Podcast" }
            publishPlaybackState(
                PlaybackState(
                    mode = PlaybackMode.PODCARD,
                    isPlaying = true,
                    title = title,
                    subtitle = resolvedSubtitle,
                    currentTrackId = podcardId,
                    isRemoteSource = resolvedVoice?.sourceType == AudioSourceType.REMOTE_CACHED,
                    mixCount = if (backgroundSoundId.isNullOrBlank()) 1 else 2,
                    chapterId = voiceTrackId,
                    chapterIndex = chapterIndex,
                    chapterTitle = chapterTitle,
                    chapterCount = chapterCount,
                    backgroundSoundId = backgroundSoundId,
                    backgroundVolume = storyAmbientBaseGain,
                    isRepeatEnabled = true,
                    positionMs = 0L,
                    durationMs = durationMsHint,
                    contentRoute = contentRoute,
                ),
            )
            saveHistoryRecordAsync(
                itemId = podcardId,
                itemType = HistoryItemType.PODCAST,
                title = title,
                subtitle = resolvedSubtitle,
                imagePath = "podcasts/$podcardId/cover.webp",
                isPremium = isPremiumTrack,
            )
            startProgressPolling()
            serviceHostCallback?.onPlaybackStarted()
            if (navigateToPlayer && autoOpenPlayerScreen) {
                playbackNavigationManager.requestNowPlaying()
            }
        }
    }

    fun playSleepPreset(
        presetId: String,
        presetTitle: String,
        items: List<PresetPlaybackItem>,
        userIsPremium: Boolean,
        navigateToPlayer: Boolean = false,
        iconCategory: String? = null,
        contentRoute: String = PlaybackDeepLinks.sceneRoute(presetId),
    ) {
        engineScope.launch {
            if (!ensureHostBound()) return@launch
            stopProgressPolling()
            releaseSinglePlayer()
            releaseStoryPlayers()
            releaseMixerPlayers()

            for (layer in items) {
                if (activeMixPlayers.size >= MAX_MIXER_TRACKS) break
                if (layer.isPremium && !userIsPremium) continue
                val resolvedLayer = audioUriResolver.resolveForPlayback(
                    trackId = layer.soundId,
                    audioFilePath = layer.audioPath,
                    isPremiumTrack = layer.isPremium,
                    userIsPremium = userIsPremium,
                )
                prepareAndFadeInLayer(
                    soundId = layer.soundId,
                    name = layer.name,
                    resolved = resolvedLayer,
                    volume = layer.volume,
                )
            }

            val playingLayers = activeMixPlayers.map { (id, _) ->
                ActiveSoundPlayback(
                    soundId = id,
                    name = activeSoundTitles[id] ?: id,
                    volume = layerTargetVolumes[id] ?: 0.7f,
                )
            }
            publishPlaybackState(
                PlaybackState(
                    mode = PlaybackMode.MIXER,
                    isPlaying = playingLayers.isNotEmpty(),
                    title = presetTitle,
                    subtitle = "${playingLayers.size} sounds",
                    currentTrackId = presetId,
                    iconCategory = iconCategory,
                    activeSounds = playingLayers,
                    mixCount = playingLayers.size,
                    isRepeatEnabled = true,
                    contentRoute = contentRoute,
                ),
            )
            if (playingLayers.isNotEmpty()) {
                saveHistoryRecordAsync(
                    itemId = presetId,
                    itemType = HistoryItemType.SCENE,
                    title = presetTitle,
                    subtitle = playingLayers.joinToString(", ") { it.name },
                    imagePath = "scenes/$presetId.webp",
                    isPremium = items.any { it.isPremium },
                )
            }
            serviceHostCallback?.onPlaybackStarted()
            if (navigateToPlayer && autoOpenPlayerScreen) {
                playbackNavigationManager.requestNowPlaying()
            }
        }
    }

    fun toggleMixerSound(
        soundId: String,
        name: String,
        audioFilePath: String? = null,
        volume: Float,
        isPremiumTrack: Boolean = false,
        userIsPremium: Boolean = false,
        navigateToPlayer: Boolean = false,
    ) {
        if (activeMixPlayers.containsKey(soundId)) {
            removeMixerSound(soundId)
            return
        }
        engineScope.launch {
            if (!ensureHostBound()) return@launch
            val resolvedAudio = audioUriResolver.resolveForPlayback(
                trackId = soundId,
                audioFilePath = audioFilePath,
                isPremiumTrack = isPremiumTrack,
                userIsPremium = userIsPremium,
            )
            insertMixerLayer(
                soundId = soundId,
                name = name,
                resolved = resolvedAudio,
                volume = volume,
                isPremiumTrack = isPremiumTrack,
                navigateToPlayer = navigateToPlayer,
            )
        }
    }

    private fun startResolvedSingleTrack(
        resolved: ResolvedAudio,
        title: String,
        isPremiumTrack: Boolean,
        navigateToPlayer: Boolean,
    ) {
        engineScope.launch {
            if (!ensureHostBound()) return@launch
            releaseMixerPlayers()
            releaseStoryPlayers()

            val exo = primarySinglePlayer ?: buildExoPlayer(requestAudioFocus = true)
                .also { primarySinglePlayer = it }
            registerFallbackRecovery(exo, resolved)
            registerDurationObserver(exo)

            val mediaItem = MediaItem.Builder()
                .setUri(resolved.uri)
                .setMediaId(resolved.trackId)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setArtist("Deep Sleep")
                        .build(),
                )
                .build()

            exo.setMediaSource(selectSourceFactory(resolved.sourceType).createMediaSource(mediaItem))
            exo.prepare()
            singleTrackBaseGain = 1f
            exo.volume = singleTrackBaseGain * masterFadeScale
            exo.playWhenReady = true
            exo.repeatMode = Player.REPEAT_MODE_ONE

            val sourceLabel = when (resolved.sourceType) {
                AudioSourceType.LOCAL_FILE -> "Offline"
                AudioSourceType.REMOTE_CACHED -> "Streaming · caching"
            }

            publishPlaybackState(
                PlaybackState(
                    mode = PlaybackMode.SINGLE,
                    isPlaying = true,
                    title = title,
                    subtitle = sourceLabel,
                    currentTrackId = resolved.trackId,
                    isRemoteSource = resolved.sourceType == AudioSourceType.REMOTE_CACHED,
                    mixCount = 1,
                    isRepeatEnabled = true,
                    contentRoute = AppRoute.NowPlaying.route,
                ),
            )
            saveHistoryRecordAsync(
                itemId = resolved.trackId,
                itemType = HistoryItemType.SOUND,
                title = title,
                subtitle = "White Noise",
                imagePath = "sounds/${resolved.trackId}/icon.webp",
                isPremium = isPremiumTrack,
            )
            startProgressPolling()
            serviceHostCallback?.onPlaybackStarted()
            if (navigateToPlayer && autoOpenPlayerScreen) {
                playbackNavigationManager.requestNowPlaying()
            }
        }
    }

    private fun insertMixerLayer(
        soundId: String,
        name: String,
        resolved: ResolvedAudio,
        volume: Float,
        isPremiumTrack: Boolean = false,
        navigateToPlayer: Boolean,
    ) {
        releaseSinglePlayer()
        releaseStoryPlayers()

        while (activeMixPlayers.size >= MAX_MIXER_TRACKS) {
            val evictedSoundId = activeMixPlayers.keys.firstOrNull() ?: break
            activeFadeJobs.remove(evictedSoundId)?.cancel()
            val evictedPlayer = activeMixPlayers.remove(evictedSoundId)
            activeSoundTitles.remove(evictedSoundId)
            layerTargetVolumes.remove(evictedSoundId)
            if (evictedPlayer != null) {
                fadeOutAndDisposePlayer(evictedPlayer)
            }
        }

        prepareAndFadeInLayer(soundId, name, resolved, volume)
        syncMixerPlaybackState(isPlaying = true, navigateToPlayer = navigateToPlayer)
        saveHistoryRecordAsync(
            itemId = soundId,
            itemType = HistoryItemType.SOUND,
            title = name,
            subtitle = "White Noise",
            imagePath = "sounds/$soundId/icon.webp",
            isPremium = isPremiumTrack,
        )
        serviceHostCallback?.onPlaybackStarted()
    }

    private fun prepareAndFadeInLayer(
        soundId: String,
        name: String,
        resolved: ResolvedAudio,
        volume: Float,
    ) {
        val requestFocus = activeMixPlayers.isEmpty()
        val layerPlayer = buildExoPlayer(requestAudioFocus = requestFocus)
        registerFallbackRecovery(layerPlayer, resolved)

        val mediaSource = selectSourceFactory(resolved.sourceType)
            .createMediaSource(MediaItem.fromUri(resolved.uri))
        layerPlayer.setMediaSource(mediaSource)
        layerTargetVolumes[soundId] = volume
        layerPlayer.volume = 0f
        layerPlayer.prepare()
        layerPlayer.playWhenReady = true
        layerPlayer.repeatMode = Player.REPEAT_MODE_ONE

        activeMixPlayers[soundId] = layerPlayer
        activeSoundTitles[soundId] = name
        rampLayerVolumeUp(soundId, targetGain = volume * masterFadeScale)
    }

    private fun registerDurationObserver(player: ExoPlayer) {
        player.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    refreshDurationFromPlayer(player)
                }

                override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                    refreshDurationFromPlayer(player)
                }
            },
        )
    }

    private fun refreshDurationFromPlayer(player: ExoPlayer) {
        val mediaDuration = player.duration
        if (mediaDuration <= 0L || mediaDuration == C.TIME_UNSET) return
        val snapshot = _playbackState.value
        if (snapshot.mode != PlaybackMode.PODCARD && snapshot.mode != PlaybackMode.SINGLE) return
        if (snapshot.durationMs != mediaDuration) {
            publishPlaybackState(
                snapshot.copy(
                    positionMs = player.currentPosition.coerceAtLeast(0L),
                    durationMs = mediaDuration,
                ),
            )
        }
    }

    private fun registerFallbackRecovery(
        player: ExoPlayer,
        resolved: ResolvedAudio,
    ) {
        var attemptedFallback = false
        player.addListener(
            object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    R2Log.e("ExoPlayer error on ${resolved.trackId}: ${error.errorCodeName}", error)
                    val canFallbackToRemote = !attemptedFallback &&
                        resolved.sourceType == AudioSourceType.LOCAL_FILE &&
                        !resolved.remoteFallbackUri.isNullOrBlank()
                    if (canFallbackToRemote) {
                        attemptedFallback = true
                        mediaFileRepository.deleteLocal(resolved.relativePath)
                        val remoteSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(resolved.remoteFallbackUri))
                        player.setMediaSource(remoteSource)
                        player.prepare()
                        player.playWhenReady = true
                    }
                }
            },
        )
    }

    private fun rampLayerVolumeUp(soundId: String, targetGain: Float) {
        activeFadeJobs.remove(soundId)?.cancel()
        activeFadeJobs[soundId] = engineScope.launch {
            for (step in 1..FADE_STEP_COUNT) {
                val player = activeMixPlayers[soundId] ?: break
                player.volume = targetGain * (step.toFloat() / FADE_STEP_COUNT)
                delay(FADE_IN_DURATION_MS / FADE_STEP_COUNT)
            }
            activeMixPlayers[soundId]?.volume = targetGain
            activeFadeJobs.remove(soundId)
        }
    }

    private fun fadeOutAndDisposePlayer(player: ExoPlayer) {
        val initialVolume = player.volume
        engineScope.launch {
            for (step in 1..FADE_STEP_COUNT) {
                player.volume = initialVolume * (1f - step.toFloat() / FADE_STEP_COUNT)
                delay(FADE_OUT_DURATION_MS / FADE_STEP_COUNT)
            }
            runCatching { player.release() }
        }
    }

    fun setMixerVolume(soundId: String, volume: Float) {
        activeFadeJobs.remove(soundId)?.cancel()
        layerTargetVolumes[soundId] = volume
        activeMixPlayers[soundId]?.volume = volume * masterFadeScale
        val currentlyPlaying = activeMixPlayers.values.any { it.isPlaying || it.playWhenReady }
        syncMixerPlaybackState(isPlaying = currentlyPlaying)
    }

    fun applyVolumeScale(scale: Float) {
        masterFadeScale = scale.coerceIn(0f, 1f)
        primarySinglePlayer?.volume = singleTrackBaseGain * masterFadeScale
        storyVoicePlayer?.volume = storyVoiceBaseGain * masterFadeScale
        storyAmbientPlayer?.volume = storyAmbientBaseGain * masterFadeScale
        activeMixPlayers.forEach { (id, player) ->
            player.volume = (layerTargetVolumes[id] ?: 1f) * masterFadeScale
        }
    }

    fun resetVolumeScale() {
        masterFadeScale = 1f
        applyVolumeScale(1f)
    }

    fun removeMixerSound(soundId: String) {
        activeFadeJobs.remove(soundId)?.cancel()
        val removedPlayer = activeMixPlayers.remove(soundId)
        activeSoundTitles.remove(soundId)
        layerTargetVolumes.remove(soundId)
        if (removedPlayer != null) {
            fadeOutAndDisposePlayer(removedPlayer)
        }
        if (activeMixPlayers.isEmpty()) {
            stopAll()
        } else {
            // Ensure the remaining primary mix player holds audio focus
            activeMixPlayers.values.firstOrNull()?.let { primaryLayer ->
                runCatching {
                    primaryLayer.setAudioAttributes(mediaAudioAttributes, true)
                    primaryLayer.setHandleAudioBecomingNoisy(true)
                }
            }
            val anyPlaying = activeMixPlayers.values.any { it.isPlaying || it.playWhenReady }
            syncMixerPlaybackState(isPlaying = anyPlaying)
        }
    }

    private var pausedByFullScreenAd = false

    /**
     * Pauses all active audio layers before a full-screen AdMob ad (App Open, Interstitial, Rewarded)
     * is displayed so app audio never bleeds underneath video ads.
     */
    fun pauseForFullScreenAd() {
        if (_playbackState.value.mode == PlaybackMode.IDLE) return
        val currentlyPlaying = _playbackState.value.isPlaying ||
            primarySinglePlayer?.playWhenReady == true ||
            storyVoicePlayer?.playWhenReady == true ||
            activeMixPlayers.values.any { it.playWhenReady }
        if (currentlyPlaying) {
            pausedByFullScreenAd = true
            pauseInternal()
        }
    }

    /**
     * Resumes playback after a full-screen AdMob ad is dismissed, if playback was active prior to the ad.
     */
    fun resumeAfterFullScreenAd() {
        if (pausedByFullScreenAd) {
            pausedByFullScreenAd = false
            resumeAll()
        }
    }

    fun stopAll() {
        pausedByFullScreenAd = false
        tearDownAllPlayers(notifyHostStopped = true)
    }

    fun pauseAll() {
        pausedByFullScreenAd = false
        pauseInternal()
    }

    private fun pauseInternal() {
        if (_playbackState.value.mode == PlaybackMode.IDLE) return
        ensureForegroundServiceStarted()
        primarySinglePlayer?.pause()
        storyVoicePlayer?.pause()
        storyAmbientPlayer?.pause()
        activeMixPlayers.values.forEach { it.pause() }
        publishPlaybackState(_playbackState.value.copy(isPlaying = false))
        serviceHostCallback?.onPlaybackStarted()
    }

    fun resumeAll() {
        pausedByFullScreenAd = false
        if (_playbackState.value.mode == PlaybackMode.IDLE) return
        engineScope.launch {
            if (!ensureHostBound()) return@launch
            when (_playbackState.value.mode) {
                PlaybackMode.SINGLE -> primarySinglePlayer?.play()
                PlaybackMode.MIXER -> activeMixPlayers.values.forEach { it.play() }
                PlaybackMode.PODCARD -> {
                    storyVoicePlayer?.play()
                    storyAmbientPlayer?.play()
                }
                PlaybackMode.IDLE -> return@launch
            }
            publishPlaybackState(_playbackState.value.copy(isPlaying = true))
            serviceHostCallback?.onPlaybackStarted()
        }
    }

    fun seekTo(positionMs: Long) {
        val current = _playbackState.value
        val activePlayer = when (current.mode) {
            PlaybackMode.PODCARD -> storyVoicePlayer
            PlaybackMode.SINGLE -> primarySinglePlayer
            PlaybackMode.MIXER,
            PlaybackMode.IDLE -> null
        } ?: return

        val playerDuration = activePlayer.duration
        val effectiveDuration = when {
            playerDuration > 0L && playerDuration != C.TIME_UNSET -> playerDuration
            current.durationMs > 0L -> current.durationMs
            else -> Long.MAX_VALUE
        }
        val clampedPosition = positionMs.coerceIn(0L, effectiveDuration)
        activePlayer.seekTo(clampedPosition)
        publishPlaybackState(current.copy(positionMs = clampedPosition))
    }

    fun seekBy(deltaMs: Long) {
        val current = _playbackState.value
        val activePlayer = when (current.mode) {
            PlaybackMode.PODCARD -> storyVoicePlayer
            PlaybackMode.SINGLE -> primarySinglePlayer
            PlaybackMode.MIXER,
            PlaybackMode.IDLE -> null
        } ?: return
        seekTo(activePlayer.currentPosition.coerceAtLeast(0L) + deltaMs)
    }

    fun setPodcardBackgroundSound(
        backgroundSoundId: String?,
        userIsPremium: Boolean = true,
    ) {
        val current = _playbackState.value
        if (current.mode != PlaybackMode.PODCARD) return
        engineScope.launch {
            if (!ensureHostBound()) return@launch
            if (backgroundSoundId.isNullOrBlank()) {
                storyAmbientPlayer?.run {
                    stop()
                    clearMediaItems()
                    release()
                }
                storyAmbientPlayer = null
                publishPlaybackState(
                    _playbackState.value.copy(
                        backgroundSoundId = null,
                        mixCount = 1,
                    ),
                )
                return@launch
            }

            val ambientDbPath = soundDao.getById(backgroundSoundId)?.audioPath
                ?.takeIf { it.isNotBlank() }
                ?: "sounds/$backgroundSoundId/audio.ogg"
            val resolvedAmbient = audioUriResolver.resolveForPlayback(
                trackId = backgroundSoundId,
                audioFilePath = ambientDbPath,
                isPremiumTrack = false,
                userIsPremium = userIsPremium,
            )
            val ambientExo = storyAmbientPlayer ?: buildExoPlayer(requestAudioFocus = false)
                .also { storyAmbientPlayer = it }
            registerFallbackRecovery(ambientExo, resolvedAmbient)
            val ambientSource = selectSourceFactory(resolvedAmbient.sourceType).createMediaSource(
                MediaItem.fromUri(resolvedAmbient.uri),
            )
            ambientExo.setMediaSource(ambientSource)
            ambientExo.prepare()
            ambientExo.volume = storyAmbientBaseGain * masterFadeScale
            ambientExo.playWhenReady = _playbackState.value.isPlaying
            ambientExo.repeatMode = Player.REPEAT_MODE_ONE

            publishPlaybackState(
                _playbackState.value.copy(
                    backgroundSoundId = backgroundSoundId,
                    backgroundVolume = storyAmbientBaseGain,
                    mixCount = 2,
                ),
            )
        }
    }

    fun setPodcardBackgroundVolume(volume: Float) {
        val clamped = volume.coerceIn(0.05f, 1f)
        storyAmbientBaseGain = clamped
        storyAmbientPlayer?.volume = clamped * masterFadeScale
        val current = _playbackState.value
        if (current.mode == PlaybackMode.PODCARD) {
            publishPlaybackState(current.copy(backgroundVolume = clamped))
        }
    }

    fun hasActivePlayback(): Boolean = _playbackState.value.mode != PlaybackMode.IDLE

    fun isTrackActive(trackId: String): Boolean {
        val current = _playbackState.value
        return when (current.mode) {
            PlaybackMode.SINGLE,
            PlaybackMode.PODCARD -> current.currentTrackId == trackId
            PlaybackMode.MIXER -> current.currentTrackId == trackId ||
                current.activeSounds.any { it.soundId == trackId }
            PlaybackMode.IDLE -> false
        }
    }

    private fun saveHistoryRecordAsync(
        itemId: String,
        itemType: HistoryItemType,
        title: String,
        subtitle: String,
        imagePath: String?,
        isPremium: Boolean,
    ) {
        engineScope.launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    playbackHistoryDao.upsert(
                        PlaybackHistoryEntity(
                            itemKey = "${itemType.name}:$itemId",
                            itemId = itemId,
                            itemType = itemType.name,
                            title = title,
                            subtitle = subtitle,
                            imagePath = imagePath,
                            durationSeconds = 0,
                            isPremium = if (isPremium) 1 else 0,
                            mixLayersJson = null,
                            playedAt = System.currentTimeMillis(),
                        ),
                    )
                    playbackHistoryDao.trimToLimit(100)
                }
            }
        }
    }

    private fun selectSourceFactory(sourceType: AudioSourceType): ProgressiveMediaSource.Factory =
        when (sourceType) {
            AudioSourceType.REMOTE_CACHED -> ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            AudioSourceType.LOCAL_FILE -> ProgressiveMediaSource.Factory(defaultDataSourceFactory)
        }

    private fun releaseSinglePlayer() {
        primarySinglePlayer?.run {
            stop()
            clearMediaItems()
            release()
        }
        primarySinglePlayer = null
    }

    private fun releaseStoryPlayers() {
        storyVoicePlayer?.run {
            stop()
            clearMediaItems()
            release()
        }
        storyVoicePlayer = null
        storyAmbientPlayer?.run {
            stop()
            clearMediaItems()
            release()
        }
        storyAmbientPlayer = null
    }

    private fun releaseMixerPlayers() {
        activeFadeJobs.values.forEach { it.cancel() }
        activeFadeJobs.clear()
        activeMixPlayers.values.forEach { it.release() }
        activeMixPlayers.clear()
        activeSoundTitles.clear()
        layerTargetVolumes.clear()
    }

    private fun syncMixerPlaybackState(isPlaying: Boolean, navigateToPlayer: Boolean = false) {
        stopProgressPolling()
        val prev = _playbackState.value
        val activeLayers = activeMixPlayers.map { (id, player) ->
            ActiveSoundPlayback(
                soundId = id,
                name = activeSoundTitles[id] ?: id,
                volume = layerTargetVolumes[id] ?: player.volume,
            )
        }
        val preserveSceneContext = prev.mode == PlaybackMode.MIXER && prev.currentTrackId != null
        val resolvedTitle = if (preserveSceneContext) {
            prev.title
        } else {
            activeLayers.joinToString(" + ") { it.name }.ifBlank { "Sound Mix" }
        }
        val targetRoute = when {
            preserveSceneContext && !prev.contentRoute.isNullOrBlank() -> prev.contentRoute
            preserveSceneContext -> PlaybackDeepLinks.sceneRoute(prev.currentTrackId.orEmpty())
            else -> AppRoute.NowPlaying.route
        }

        publishPlaybackState(
            PlaybackState(
                mode = PlaybackMode.MIXER,
                isPlaying = isPlaying,
                title = resolvedTitle,
                subtitle = "${activeLayers.size} sounds",
                currentTrackId = if (preserveSceneContext) prev.currentTrackId else null,
                iconCategory = if (preserveSceneContext) prev.iconCategory else null,
                activeSounds = activeLayers,
                mixCount = activeLayers.size,
                isRepeatEnabled = true,
                contentRoute = targetRoute,
            ),
        )
        if (navigateToPlayer && autoOpenPlayerScreen) {
            playbackNavigationManager.requestNowPlaying()
        }
    }

    private fun tearDownAllPlayers(notifyHostStopped: Boolean) {
        stopProgressPolling()
        releaseSinglePlayer()
        releaseMixerPlayers()
        releaseStoryPlayers()
        resetVolumeScale()
        publishPlaybackState(PlaybackState())
        if (notifyHostStopped) {
            serviceHostCallback?.onPlaybackStopped()
        }
    }

    private fun startProgressPolling() {
        progressPollingJob?.cancel()
        progressPollingJob = engineScope.launch {
            while (true) {
                val current = _playbackState.value
                val activePlayer = when (current.mode) {
                    PlaybackMode.PODCARD -> storyVoicePlayer
                    PlaybackMode.SINGLE -> primarySinglePlayer
                    PlaybackMode.MIXER,
                    PlaybackMode.IDLE -> null
                }
                if (activePlayer != null) {
                    val posMs = activePlayer.currentPosition.coerceAtLeast(0L)
                    val rawDur = activePlayer.duration
                    val durMs = when {
                        rawDur > 0L && rawDur != C.TIME_UNSET -> rawDur
                        current.durationMs > 0L -> current.durationMs
                        else -> 0L
                    }
                    if (posMs != current.positionMs || durMs != current.durationMs) {
                        publishPlaybackState(
                            current.copy(
                                positionMs = posMs,
                                durationMs = durMs,
                            ),
                        )
                    }
                }
                delay(PROGRESS_POLL_INTERVAL_MS)
            }
        }
    }

    private fun stopProgressPolling() {
        progressPollingJob?.cancel()
        progressPollingJob = null
    }

    private fun publishPlaybackState(nextState: PlaybackState) {
        _playbackState.value = nextState
        if (nextState.mode != PlaybackMode.IDLE) {
            serviceHostCallback?.onPlaybackStateChanged(nextState)
        }
    }

    private fun buildExoPlayer(requestAudioFocus: Boolean): ExoPlayer {
        if (!serviceHostBound) {
            ensureForegroundServiceStarted()
        }
        return ExoPlayer.Builder(appContext)
            .setAudioAttributes(mediaAudioAttributes, requestAudioFocus)
            .setHandleAudioBecomingNoisy(requestAudioFocus)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
    }

    private suspend fun ensureHostBound(): Boolean {
        ensureForegroundServiceStarted()
        if (serviceHostBound) return true
        val gate = hostBoundSignal ?: CompletableDeferred<Unit>().also { hostBoundSignal = it }
        if (serviceHostBound) {
            gate.complete(Unit)
            return true
        }
        val boundInTime = withTimeoutOrNull(SERVICE_BIND_TIMEOUT_MS) { gate.await() }
        return boundInTime != null && serviceHostBound
    }

    private fun ensureForegroundServiceStarted() {
        if (!serviceHostBound) {
            runCatching {
                val serviceIntent = Intent(appContext, AudioPlaybackService::class.java)
                appContext.startForegroundService(serviceIntent)
            }
        }
        serviceHostCallback?.onPlaybackStarted()
    }

    companion object {
        const val MAX_MIXER_TRACKS = 5
        private const val FADE_STEP_COUNT = 16
        private const val FADE_IN_DURATION_MS = 700L
        private const val FADE_OUT_DURATION_MS = 450L
        private const val PROGRESS_POLL_INTERVAL_MS = 500L
        private const val SERVICE_BIND_TIMEOUT_MS = 5_000L
    }
}

/**
 * Callback interface linking [AudioPlayerManager] with [AudioPlaybackService].
 */
interface AudioServiceCallback {
    fun onPlaybackStarted()
    fun onPlaybackStopped()
    fun onPlaybackStateChanged(state: PlaybackState)
}
