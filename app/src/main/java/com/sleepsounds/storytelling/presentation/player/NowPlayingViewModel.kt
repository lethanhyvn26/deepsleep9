package com.sleepsounds.storytelling.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepsounds.storytelling.data.repository.ContentRepository
import com.sleepsounds.storytelling.data.repository.UserPreferencesRepository
import com.sleepsounds.storytelling.data.sync.DetailContentLoader
import com.sleepsounds.storytelling.domain.model.AudioTrack
import com.sleepsounds.storytelling.domain.model.MixSoundState
import com.sleepsounds.storytelling.domain.model.Podcast
import com.sleepsounds.storytelling.service.AudioPlayerManager
import com.sleepsounds.storytelling.service.PlaybackMode
import com.sleepsounds.storytelling.service.PlaybackState
import com.sleepsounds.storytelling.service.SleepTimerManager
import com.sleepsounds.storytelling.service.SleepTimerState
import com.sleepsounds.storytelling.util.MediaAssetResolver
import com.sleepsounds.storytelling.util.YouTubeUrls
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel backing the Now Playing screen for soundscapes, custom mixes, and bedtime stories.
 */
@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val audioPlayerManager: AudioPlayerManager,
    private val contentRepository: ContentRepository,
    private val mediaAssetResolver: MediaAssetResolver,
    private val detailContentLoader: DetailContentLoader,
    sleepTimerManager: SleepTimerManager,
) : ViewModel() {

    val timerState: StateFlow<SleepTimerState> = sleepTimerManager.state

    private val visualMetadata =
        MutableStateFlow<Triple<String?, AudioTrack?, String>>(Triple(null, null, ""))
    private val activeSoundTracksMap = MutableStateFlow<Map<String, AudioTrack>>(emptyMap())
    private val favoriteFlag = MutableStateFlow(false)
    private val streamablePodcasts = MutableStateFlow<List<Podcast>>(emptyList())

    val uiState: StateFlow<NowPlayingUiState> = combine(
        audioPlayerManager.playbackState,
        visualMetadata,
        activeSoundTracksMap,
        favoriteFlag,
        contentRepository.observeUserMixes(),
    ) { playback, (coverUri, mainTrack, resolvedTitle), trackLookup, liked, userMixList ->
        NowPlayingUiState(
            playback = playback,
            backgroundImageUri = coverUri,
            primaryTrack = mainTrack,
            soundTracks = trackLookup,
            displayTitle = resolvedTitle.ifBlank { playback.title },
            isFavorite = liked,
            savedMixCount = userMixList.size,
            audioPodcasts = streamablePodcasts.value,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        NowPlayingUiState(playback = audioPlayerManager.playbackState.value),
    )

    init {
        viewModelScope.launch {
            contentRepository.observePodcastsByLanguage(UserPreferencesRepository.LANGUAGE_EN)
                .collect { catalogPodcasts ->
                    streamablePodcasts.value = catalogPodcasts.filter { pod ->
                        val firstAudio = pod.chapters.minByOrNull { it.chapterIndex }?.audioPath
                        !YouTubeUrls.isYouTubeUrl(firstAudio)
                    }
                }
        }

        viewModelScope.launch {
            audioPlayerManager.playbackState.collect { playback ->
                when (playback.mode) {
                    PlaybackMode.IDLE -> Unit
                    PlaybackMode.MIXER -> updateMixerVisuals(playback)
                    PlaybackMode.PODCARD -> {
                        val pod = playback.currentTrackId?.let { contentRepository.getPodcastById(it) }
                        val coverUri = pod?.imagePath?.let(mediaAssetResolver::toCoilUri)
                        visualMetadata.value = Triple(coverUri, null, pod?.title ?: playback.title)
                        activeSoundTracksMap.value = emptyMap()
                        favoriteFlag.value = pod?.isFavorite ?: false
                    }
                    PlaybackMode.SINGLE -> {
                        val activeId = playback.currentTrackId ?: return@collect
                        val resolvedTrack = contentRepository.getSleepPresetTrackById(activeId)
                            ?: contentRepository.getSoundById(activeId)
                        val coverUri = toCoilCoverUri(resolvedTrack)
                        visualMetadata.value = Triple(
                            coverUri,
                            resolvedTrack,
                            resolvedTrack?.name ?: playback.title,
                        )
                        activeSoundTracksMap.value = emptyMap()
                        favoriteFlag.value = resolvedTrack?.isFavorite ?: false
                    }
                }
            }
        }
    }

    private suspend fun updateMixerVisuals(playback: PlaybackState) {
        val currentPresetId = playback.currentTrackId
        val presetTrack = currentPresetId?.let { contentRepository.getSleepPresetTrackById(it) }
        if (presetTrack != null) {
            visualMetadata.value = Triple(
                toCoilCoverUri(presetTrack),
                presetTrack,
                playback.title,
            )
            activeSoundTracksMap.value = emptyMap()
            favoriteFlag.value = presetTrack.isFavorite
            return
        }

        val resolvedTracks = playback.activeSounds.mapNotNull { layer ->
            contentRepository.getSoundById(layer.soundId)?.let { layer.soundId to it }
        }.toMap()
        activeSoundTracksMap.value = resolvedTracks

        val candidateCovers = contentRepository.getCoverImagePaths()
        val activeIds = playback.activeSounds.map { it.soundId }
        val backgroundUri = mediaAssetResolver.resolveMixBackgroundUri(activeIds, candidateCovers)
            ?: resolvedTracks.values.firstOrNull()?.let(::toCoilCoverUri)

        visualMetadata.value = Triple(
            backgroundUri,
            resolvedTracks.values.firstOrNull(),
            playback.title,
        )
    }

    private fun toCoilCoverUri(track: AudioTrack?): String? =
        track?.imagePath?.let(mediaAssetResolver::toCoilUri)

    fun pauseResume() {
        if (uiState.value.playback.isPlaying) {
            audioPlayerManager.pauseAll()
        } else {
            audioPlayerManager.resumeAll()
        }
    }

    fun seekTo(positionMs: Long) {
        audioPlayerManager.seekTo(positionMs)
    }

    fun seekBy(deltaMs: Long) {
        audioPlayerManager.seekBy(deltaMs)
    }

    fun setPodcardBackgroundSound(soundId: String?) {
        viewModelScope.launch {
            audioPlayerManager.setPodcardBackgroundSound(soundId, true)
        }
    }

    fun setPodcardBackgroundVolume(volume: Float) {
        audioPlayerManager.setPodcardBackgroundVolume(volume)
    }

    fun selectPodcastFromQueue(podcast: Podcast) {
        viewModelScope.launch {
            val firstChapter = podcast.chapters.firstOrNull()
            val ambientBgId = uiState.value.playback.backgroundSoundId
            detailContentLoader.prepareForDetail(
                trackId = firstChapter?.id ?: podcast.id,
                audioPath = firstChapter?.audioPath,
                imagePath = podcast.imagePath,
                downloadAudio = false,
            )
            audioPlayerManager.playPodcard(
                podcardId = podcast.id,
                title = podcast.title,
                voiceAudioPath = firstChapter?.audioPath,
                voiceTrackId = firstChapter?.id ?: podcast.id,
                chapterIndex = firstChapter?.chapterIndex ?: 0,
                chapterTitle = firstChapter?.title?.ifBlank { podcast.title } ?: podcast.title,
                chapterCount = podcast.chapters.size.coerceAtLeast(1),
                durationMsHint = 0L,
                backgroundSoundId = ambientBgId,
                isPremiumTrack = false,
                userIsPremium = true,
                navigateToPlayer = false,
            )
        }
    }

    fun toggleCurrentFavorite() {
        val currentPlayback = uiState.value.playback
        val trackId = currentPlayback.currentTrackId ?: return
        viewModelScope.launch {
            val updatedFav = when (currentPlayback.mode) {
                PlaybackMode.PODCARD -> contentRepository.togglePodcastFavorite(trackId)
                PlaybackMode.MIXER -> contentRepository.toggleSceneFavorite(trackId)
                PlaybackMode.SINGLE -> contentRepository.toggleSoundFavorite(trackId)
                PlaybackMode.IDLE -> false
            }
            favoriteFlag.value = updatedFav
        }
    }

    fun playAdjacentChapter(delta: Int) {
        val currentPlayback = uiState.value.playback
        if (currentPlayback.mode != PlaybackMode.PODCARD) return
        val activePodcastId = currentPlayback.currentTrackId ?: return

        viewModelScope.launch {
            val currentPod = contentRepository.getPodcastById(activePodcastId)
            if (currentPod != null && currentPod.chapters.size > 1) {
                val targetChapterIndex = currentPlayback.chapterIndex + delta
                if (targetChapterIndex in currentPod.chapters.indices) {
                    val nextChapter = currentPod.chapters[targetChapterIndex]
                    detailContentLoader.prepareForDetail(
                        trackId = nextChapter.id,
                        audioPath = nextChapter.audioPath,
                        imagePath = currentPod.imagePath,
                        downloadAudio = false,
                    )
                    audioPlayerManager.playPodcard(
                        podcardId = currentPod.id,
                        title = currentPod.title,
                        voiceAudioPath = nextChapter.audioPath,
                        voiceTrackId = nextChapter.id,
                        chapterIndex = nextChapter.chapterIndex,
                        chapterTitle = nextChapter.title,
                        chapterCount = currentPod.chapters.size,
                        durationMsHint = 0L,
                        backgroundSoundId = currentPlayback.backgroundSoundId,
                        isPremiumTrack = false,
                        userIsPremium = true,
                    )
                    return@launch
                }
            }

            val queue = contentRepository
                .observePodcastsByLanguage(UserPreferencesRepository.LANGUAGE_EN)
                .first()
                .filter { pod ->
                    val firstAudio = pod.chapters.minByOrNull { it.chapterIndex }?.audioPath
                    !YouTubeUrls.isYouTubeUrl(firstAudio)
                }
            streamablePodcasts.value = queue
            if (queue.isEmpty()) return@launch

            val currentPos = queue.indexOfFirst { it.id == activePodcastId }.coerceAtLeast(0)
            val nextPod = queue[(currentPos + delta).mod(queue.size)]
            val firstChapter = nextPod.chapters.firstOrNull()

            detailContentLoader.prepareForDetail(
                trackId = firstChapter?.id ?: nextPod.id,
                audioPath = firstChapter?.audioPath,
                imagePath = nextPod.imagePath,
                downloadAudio = false,
            )
            audioPlayerManager.playPodcard(
                podcardId = nextPod.id,
                title = nextPod.title,
                voiceAudioPath = firstChapter?.audioPath,
                voiceTrackId = firstChapter?.id ?: nextPod.id,
                chapterIndex = firstChapter?.chapterIndex ?: 0,
                chapterTitle = firstChapter?.title?.ifBlank { nextPod.title } ?: nextPod.title,
                chapterCount = nextPod.chapters.size.coerceAtLeast(1),
                durationMsHint = 0L,
                backgroundSoundId = currentPlayback.backgroundSoundId,
                isPremiumTrack = false,
                userIsPremium = true,
                navigateToPlayer = false,
            )
        }
    }

    fun stop() {
        audioPlayerManager.stopAll()
    }

    fun setMixerVolume(soundId: String, volume: Float) {
        audioPlayerManager.setMixerVolume(soundId, volume)
    }

    fun removeMixerSound(soundId: String) {
        audioPlayerManager.removeMixerSound(soundId)
    }

    fun saveCurrentMix(name: String, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            val currentPlayback = uiState.value.playback
            if (currentPlayback.mode != PlaybackMode.MIXER || currentPlayback.activeSounds.isEmpty()) {
                return@launch
            }
            val layers = currentPlayback.activeSounds.map { active ->
                val soundTrack = contentRepository.getSoundById(active.soundId)
                MixSoundState(
                    soundId = active.soundId,
                    name = active.name,
                    isActive = true,
                    volume = active.volume,
                    isPremium = false,
                    categories = soundTrack?.categories ?: emptyList(),
                    symbolPath = soundTrack?.imagePath,
                    audioPath = soundTrack?.audioPath,
                )
            }
            contentRepository.saveUserMix(name, layers)
            onSaved()
        }
    }
}
data class NowPlayingUiState(
    val playback: PlaybackState = PlaybackState(),
    val backgroundImageUri: String? = null,
    val primaryTrack: AudioTrack? = null,
    val soundTracks: Map<String, AudioTrack> = emptyMap(),
    val displayTitle: String = "",
    val isFavorite: Boolean = false,
    val savedMixCount: Int = 0,
    val audioPodcasts: List<Podcast> = emptyList(),
)
