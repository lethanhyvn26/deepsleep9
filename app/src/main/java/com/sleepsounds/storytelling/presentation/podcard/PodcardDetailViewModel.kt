package com.sleepsounds.storytelling.presentation.podcard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepsounds.storytelling.ads.AdMobManager
import com.sleepsounds.storytelling.data.repository.ContentRepository
import com.sleepsounds.storytelling.data.repository.UserPreferencesRepository
import com.sleepsounds.storytelling.data.sync.DetailContentLoader
import com.sleepsounds.storytelling.domain.model.Podcast
import com.sleepsounds.storytelling.domain.model.PodcastChapter
import com.sleepsounds.storytelling.presentation.navigation.AppRoute
import com.sleepsounds.storytelling.service.AudioPlayerManager
import com.sleepsounds.storytelling.service.PlaybackMode
import com.sleepsounds.storytelling.service.PlaybackState
import com.sleepsounds.storytelling.util.YouTubeUrls
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PodcardDetailUiState(
    val podcast: Podcast? = null,
    val selectedChapter: PodcastChapter? = null,
    val suggestions: List<Podcast> = emptyList(),
    val isLoading: Boolean = true,
    val playback: PlaybackState = PlaybackState(),
    val isPlayingThisPodcast: Boolean = false,
    val youtubeVideoId: String? = null,
    val youtubeError: Boolean = false,
)

/**
 * ViewModel coordinating bedtime story playback, inline YouTube sessions, and 0ms instant switching to related stories.
 */
@HiltViewModel
class PodcardDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentRepository: ContentRepository,
    private val detailContentLoader: DetailContentLoader,
    private val audioPlayerManager: AudioPlayerManager,
    val adMobManager: AdMobManager,
) : ViewModel() {

    private val activePodcardId = MutableStateFlow(
        savedStateHandle.get<String>(AppRoute.PodcardDetail.PODCARD_ID_ARG).orEmpty(),
    )

    private val localDetailState = MutableStateFlow(PodcardDetailUiState(isLoading = true))

    val uiState: StateFlow<PodcardDetailUiState> = combine(
        activePodcardId,
        localDetailState,
        contentRepository.observePodcastsByLanguage(UserPreferencesRepository.LANGUAGE_EN),
        audioPlayerManager.playbackState,
    ) { currentId, detail, catalogList, currentPlayback ->
        val updatedPodcast = catalogList.firstOrNull { it.id == currentId }
            ?: detail.podcast?.takeIf { it.id == currentId }
        val isCurrentPodPlaying = currentPlayback.mode == PlaybackMode.PODCARD &&
            currentPlayback.currentTrackId == currentId

        val activeChapter = if (isCurrentPodPlaying && currentPlayback.chapterId != null) {
            updatedPodcast?.chapters?.firstOrNull { it.id == currentPlayback.chapterId }
                ?: detail.selectedChapter
        } else {
            updatedPodcast?.chapters?.firstOrNull { it.id == detail.selectedChapter?.id }
                ?: updatedPodcast?.chapters?.firstOrNull()
                ?: detail.selectedChapter
        }

        detail.copy(
            podcast = updatedPodcast,
            playback = currentPlayback,
            selectedChapter = activeChapter,
            isPlayingThisPodcast = isCurrentPodPlaying,
            suggestions = rankSuggestions(updatedPodcast, catalogList),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PodcardDetailUiState())

    init {
        viewModelScope.launch {
            savedStateHandle.getStateFlow(AppRoute.PodcardDetail.PODCARD_ID_ARG, "")
                .collectLatest { navId ->
                    if (navId.isNotBlank() && navId != activePodcardId.value) {
                        activePodcardId.value = navId
                        loadPodcastById(navId, autoStartPlayback = false)
                    }
                }
        }
        viewModelScope.launch {
            val initialId = activePodcardId.value
            if (initialId.isNotBlank()) {
                loadPodcastById(initialId, autoStartPlayback = false)
            }
        }
    }

    private suspend fun loadPodcastById(podcardId: String, autoStartPlayback: Boolean) {
        val inMemoryPodcast = uiState.value.suggestions.firstOrNull { it.id == podcardId }
        val loadedPodcast = inMemoryPodcast ?: contentRepository.getPodcastById(podcardId)
        val firstChapter = loadedPodcast?.chapters?.firstOrNull()
        val chapterPath = firstChapter?.audioPath
        val isVideo = YouTubeUrls.isYouTubeUrl(chapterPath)
        val canAutoStart = autoStartPlayback && loadedPodcast?.isPremium != true
        val nextYoutubeId = if (canAutoStart && isVideo) {
            YouTubeUrls.videoIdOrNull(chapterPath)
        } else {
            null
        }
        if (nextYoutubeId != null) {
            audioPlayerManager.stopAll()
        }

        localDetailState.value = PodcardDetailUiState(
            podcast = loadedPodcast,
            selectedChapter = firstChapter,
            isLoading = false,
            youtubeVideoId = nextYoutubeId,
            youtubeError = canAutoStart && isVideo && nextYoutubeId == null,
        )

        if (loadedPodcast != null) {
            viewModelScope.launch(Dispatchers.IO) {
                detailContentLoader.prepareForDetail(
                    trackId = loadedPodcast.id,
                    audioPath = firstChapter?.audioPath,
                    imagePath = loadedPodcast.imagePath,
                )
            }
        }

        if (canAutoStart && loadedPodcast != null && !isVideo) {
            startChapterAudioPlayback(loadedPodcast, firstChapter)
        }
    }

    fun openRelatedPodcast(podcardId: String, autoPlay: Boolean = true) {
        val cleanId = podcardId.trim()
        if (cleanId.isEmpty()) return
        savedStateHandle[AppRoute.PodcardDetail.PODCARD_ID_ARG] = cleanId
        activePodcardId.value = cleanId
        viewModelScope.launch {
            loadPodcastById(cleanId, autoStartPlayback = autoPlay)
        }
    }

    fun togglePlayback() {
        val currentPod = uiState.value.podcast ?: localDetailState.value.podcast ?: return
        if (localDetailState.value.youtubeVideoId != null) return

        viewModelScope.launch {
            val chapter = uiState.value.selectedChapter
                ?: localDetailState.value.selectedChapter
                ?: currentPod.chapters.firstOrNull()
            val chapterAudioPath = chapter?.audioPath

            if (YouTubeUrls.isYouTubeUrl(chapterAudioPath)) {
                val parsedVideoId = YouTubeUrls.videoIdOrNull(chapterAudioPath)
                if (parsedVideoId == null) {
                    localDetailState.update { it.copy(youtubeVideoId = null, youtubeError = true) }
                    return@launch
                }
                audioPlayerManager.stopAll()
                localDetailState.update { it.copy(youtubeVideoId = parsedVideoId, youtubeError = false) }
                return@launch
            }

            if (uiState.value.isPlayingThisPodcast) {
                if (uiState.value.playback.isPlaying) {
                    audioPlayerManager.pauseAll()
                } else {
                    audioPlayerManager.resumeAll()
                }
                return@launch
            }
            startChapterAudioPlayback(currentPod, chapter)
        }
    }

    fun onYouTubeEnded() {
        localDetailState.update { it.copy(youtubeVideoId = null, youtubeError = false) }
    }

    fun onYouTubeFailed() {
        localDetailState.update { it.copy(youtubeVideoId = null, youtubeError = true) }
    }

    private suspend fun startChapterAudioPlayback(
        podcast: Podcast,
        chapter: PodcastChapter?,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            detailContentLoader.prepareForDetail(
                trackId = chapter?.id ?: podcast.id,
                audioPath = chapter?.audioPath,
                imagePath = podcast.imagePath,
                downloadAudio = false,
            )
        }

        audioPlayerManager.playPodcard(
            podcardId = podcast.id,
            title = podcast.title,
            voiceAudioPath = chapter?.audioPath,
            voiceTrackId = chapter?.id ?: podcast.id,
            chapterIndex = chapter?.chapterIndex ?: 0,
            chapterTitle = chapter?.title?.ifBlank { podcast.title } ?: podcast.title,
            chapterCount = podcast.chapters.size.coerceAtLeast(1),
            durationMsHint = 0L,
            backgroundSoundId = null,
            isPremiumTrack = false,
            userIsPremium = true,
            navigateToPlayer = true,
        )
    }
}

internal fun rankSuggestions(
    current: Podcast?,
    catalog: List<Podcast>,
): List<Podcast> {
    if (current == null) return emptyList()
    val matchYouTubeFlag = isYouTubePodcast(current)
    val matchingItems = catalog
        .asSequence()
        .filter { isYouTubePodcast(it) == matchYouTubeFlag }
        .distinctBy { it.id }
        .toList()
    if (matchingItems.size <= 1) return emptyList()
    val pos = matchingItems.indexOfFirst { it.id == current.id }
    if (pos < 0) return matchingItems
    return matchingItems.subList(pos + 1, matchingItems.size) + matchingItems.subList(0, pos)
}

internal fun isYouTubePodcast(podcast: Podcast): Boolean =
    YouTubeUrls.isYouTubeUrl(
        podcast.chapters.minByOrNull { it.chapterIndex }?.audioPath,
    )
