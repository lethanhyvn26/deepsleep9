package com.sleepsounds.storytelling.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepsounds.storytelling.R
import com.sleepsounds.storytelling.ads.AdMobManager
import com.sleepsounds.storytelling.data.local.DailyQuoteItem
import com.sleepsounds.storytelling.data.local.DreamInterpretationCatalog
import com.sleepsounds.storytelling.data.local.DreamSymbolItem
import com.sleepsounds.storytelling.data.remote.ContentCategory
import com.sleepsounds.storytelling.data.repository.ContentRepository
import com.sleepsounds.storytelling.data.repository.UserPreferencesRepository
import com.sleepsounds.storytelling.domain.model.AudioTrack
import com.sleepsounds.storytelling.domain.model.MixSoundState
import com.sleepsounds.storytelling.domain.model.Podcast
import com.sleepsounds.storytelling.domain.model.SleepPreset
import com.sleepsounds.storytelling.domain.model.UserMix
import com.sleepsounds.storytelling.service.AudioPlayerManager
import com.sleepsounds.storytelling.service.PlaybackDeepLinks
import com.sleepsounds.storytelling.service.PlaybackNavigationManager
import com.sleepsounds.storytelling.service.PlaybackState
import com.sleepsounds.storytelling.util.CategoryIcons
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

private val curatedQuickSoundOrder = listOf(
    "light_rain",
    "soft_fan",
    "fire",
    "water_wave",
    "rain_on_leaves",
    "bamboo_flute",
    "clock",
    "crickets",
    "rain_on_tent",
    "seagull",
    "rain_on_roof",
    "cold_wind",
    "white_noise",
    "brown_noise",
    "pink_noise",
    "heavy_rain",
    "thunder",
    "rain_on_windows",
    "soft_guitar",
    "air_conditioner",
    "waterfall",
    "perfect_rain",
    "soft_piano",
    "rain_forest",
)
private val featuredSceneIds = setOf(
    "sc_perfect_rain",
    "sc_ocean_heartbeat",
    "mu_healing_sleep",
    "sc_campfire",
    "mu_soft_piano_night",
    "sc_rainy_day_comfort",
    "mu_brain_massage",
    "sc_calm_lake",
)
private fun formatCategoryDisplayName(rawId: String): String =
    rawId.replace('_', ' ').lowercase().replaceFirstChar { it.uppercaseChar() }
internal fun resolveSceneCategories(
    catalogCategories: List<ContentCategory>,
    availableCategoryIds: List<String>,
): List<ContentCategory> {
    if (availableCategoryIds.isEmpty()) return emptyList()
    if (catalogCategories.isEmpty()) {
        return availableCategoryIds.map { rawId ->
            ContentCategory(id = rawId, label = formatCategoryDisplayName(rawId))
        }
    }
    val matching = catalogCategories.filter { cat ->
        availableCategoryIds.any { it.equals(cat.id, ignoreCase = true) }
    }
    if (matching.isNotEmpty()) return matching
    return availableCategoryIds.map { rawId ->
        ContentCategory(id = rawId, label = formatCategoryDisplayName(rawId))
    }
}
/**
 * ViewModel aggregating featured sleep scenes, quick ambient sounds, bedtime stories, and dream symbols.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val audioPlayerManager: AudioPlayerManager,
    private val playbackNavigationManager: PlaybackNavigationManager,
    val adMobManager: AdMobManager,
) : ViewModel() {

    private val categoryFilter = MutableStateFlow<String?>(null)

    val playbackState: StateFlow<PlaybackState> = audioPlayerManager.playbackState

    private val primaryCatalogFlow = combine(
        contentRepository.observeSleepPresets(),
        contentRepository.observeUserMixes(),
        contentRepository.observeMediaCategories(),
    ) { presetList, mixList, categoryList ->
        Triple(presetList, mixList, categoryList)
    }

    private val mediaAndPlaybackFlow = combine(
        contentRepository.observeSounds(),
        contentRepository.observePodcastsByLanguage(UserPreferencesRepository.LANGUAGE_EN),
        audioPlayerManager.playbackState,
    ) { soundTracks, storyPodcasts, currentPlayback ->
        Triple(soundTracks, storyPodcasts, currentPlayback)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        primaryCatalogFlow,
        mediaAndPlaybackFlow,
        categoryFilter,
    ) { (scenesList, mixesList, catsList), (soundTracks, storyPodcasts, currentPlayback), activeCategory ->
        val presentCategoryIds = scenesList.flatMap { it.categories }.distinct()
        val resolvedCategories = resolveSceneCategories(catsList, presentCategoryIds)
        val validCategoryFilter = activeCategory?.takeIf { selected ->
            presentCategoryIds.any { it.equals(selected, ignoreCase = true) }
        }
        val filteredScenes = filterScenesByCategory(scenesList, validCategoryFilter)

        val featuredList = scenesList.filter { it.id in featuredSceneIds }
            .ifEmpty { scenesList.take(8) }

        val meditationList = scenesList.filter { preset ->
            preset.categories.any {
                it.contains("meditat", ignoreCase = true) ||
                    it.contains("focus", ignoreCase = true)
            }
        }.ifEmpty { scenesList.drop(2).take(8).ifEmpty { scenesList.take(8) } }

        val soundscapeList = scenesList.filter { preset ->
            preset.id.startsWith("sc_") ||
                preset.id.startsWith("os_") ||
                preset.categories.any {
                    it.contains("rain", ignoreCase = true) ||
                        it.contains("nature", ignoreCase = true) ||
                        it.contains("water", ignoreCase = true)
                }
        }.ifEmpty { scenesList.take(10) }

        val healingMusicList = scenesList.filter { preset ->
            preset.id.startsWith("mu_") ||
                preset.categories.any {
                    it.contains("instrument", ignoreCase = true) ||
                        it.contains("relax", ignoreCase = true)
                }
        }.ifEmpty { scenesList.take(8) }

        val prioritizedSounds = soundTracks.sortedWith(
            compareBy { track ->
                val rank = curatedQuickSoundOrder.indexOf(track.id)
                if (rank >= 0) rank else 1000
            },
        )

        val quickSoundGrid = prioritizedSounds.take(16).map { track: AudioTrack ->
            val playingLayer = currentPlayback.activeSounds.firstOrNull { it.soundId == track.id }
            MixSoundState(
                soundId = track.id,
                name = track.name,
                isActive = playingLayer != null,
                volume = playingLayer?.volume ?: track.defaultVolume,
                isPremium = track.isPremium,
                categories = track.categories,
                symbolPath = track.imagePath,
                audioPath = track.audioPath,
            )
        }

        HomeUiState(
            isLoading = false,
            scenes = filteredScenes,
            featuredScenes = featuredList,
            recentlyUpdatedScenes = scenesList.take(8),
            meditationScenes = meditationList,
            soundscapeScenes = soundscapeList,
            healingMusicScenes = healingMusicList,
            quickSounds = quickSoundGrid,
            sleepStories = storyPodcasts.take(8),
            dreamPreviews = DreamInterpretationCatalog.SYMBOLS.take(3),
            dailyQuote = DreamInterpretationCatalog.quoteOfTheDay(),
            favoriteScenes = scenesList.filter { it.isFavorite },
            userMixes = mixesList,
            selectedCategory = validCategoryFilter,
            greetingResId = currentGreetingResourceId(),
            categories = resolvedCategories,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun selectCategory(category: String?) {
        categoryFilter.value = category
    }

    fun playPreset(preset: SleepPreset, navigateToPlayer: Boolean = true) {
        viewModelScope.launch {
            val layers = preset.items.map { item ->
                AudioPlayerManager.PresetPlaybackItem(
                    soundId = item.soundId,
                    name = item.name,
                    audioPath = item.audioPath,
                    volume = item.volume,
                    isPremium = false,
                )
            }
            audioPlayerManager.playSleepPreset(
                presetId = preset.id,
                presetTitle = preset.name,
                items = layers,
                userIsPremium = true,
                navigateToPlayer = navigateToPlayer,
                iconCategory = CategoryIcons.resolveForMix(preset.categories),
                contentRoute = PlaybackDeepLinks.sceneRoute(preset.id),
            )
        }
    }

    fun playUserMix(mix: UserMix) {
        viewModelScope.launch {
            val layers = mix.items.map { item ->
                AudioPlayerManager.PresetPlaybackItem(
                    soundId = item.soundId,
                    name = item.name,
                    audioPath = item.audioPath,
                    volume = item.volume,
                    isPremium = false,
                )
            }
            audioPlayerManager.playSleepPreset(
                presetId = mix.id,
                presetTitle = mix.name,
                items = layers,
                userIsPremium = true,
                navigateToPlayer = false,
                iconCategory = CategoryIcons.CUSTOM_KEY,
                contentRoute = PlaybackDeepLinks.mixerRoute(mix.id),
            )
            playbackNavigationManager.requestNowPlaying()
        }
    }

    fun toggleQuickSound(sound: MixSoundState) {
        viewModelScope.launch {
            if (sound.isActive) {
                audioPlayerManager.removeMixerSound(sound.soundId)
            } else {
                audioPlayerManager.toggleMixerSound(
                    soundId = sound.soundId,
                    name = sound.name,
                    audioFilePath = sound.audioPath,
                    volume = sound.volume,
                    isPremiumTrack = false,
                    userIsPremium = true,
                    navigateToPlayer = false,
                )
            }
        }
    }

    fun playDreamRecommendation(symbol: DreamSymbolItem) {
        viewModelScope.launch {
            val recommendedScene = symbol.recommendedSceneId
            if (!recommendedScene.isNullOrBlank()) {
                val scene = contentRepository.getSleepPresetById(recommendedScene)
                if (scene != null) {
                    playPreset(scene, navigateToPlayer = false)
                    return@launch
                }
            }
            val recommendedSound = symbol.recommendedSoundId
            if (!recommendedSound.isNullOrBlank()) {
                val soundTrack = contentRepository.getSoundById(recommendedSound)
                if (soundTrack != null) {
                    audioPlayerManager.toggleMixerSound(
                        soundId = soundTrack.id,
                        name = soundTrack.name,
                        audioFilePath = soundTrack.audioPath,
                        volume = soundTrack.defaultVolume,
                        isPremiumTrack = false,
                        userIsPremium = true,
                        navigateToPlayer = false,
                    )
                    return@launch
                }
            }
            uiState.value.scenes.firstOrNull()?.let { fallbackScene ->
                playPreset(fallbackScene, navigateToPlayer = false)
            }
        }
    }

    fun toggleSceneFavorite(sceneId: String) {
        viewModelScope.launch {
            contentRepository.toggleSceneFavorite(sceneId)
        }
    }

    fun togglePodcastFavorite(podcastId: String) {
        viewModelScope.launch {
            contentRepository.togglePodcastFavorite(podcastId)
        }
    }

    private fun filterScenesByCategory(
        scenes: List<SleepPreset>,
        categoryKey: String?,
    ): List<SleepPreset> {
        if (categoryKey.isNullOrBlank()) return scenes
        return scenes.filter { preset ->
            preset.categories.any { it.equals(categoryKey, ignoreCase = true) }
        }
    }

    private fun currentGreetingResourceId(): Int {
        val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hourOfDay) {
            in 5..11 -> R.string.greeting_morning
            in 12..16 -> R.string.greeting_afternoon
            in 17..20 -> R.string.greeting_evening
            else -> R.string.greeting_night
        }
    }
}
data class HomeUiState(
    val isLoading: Boolean = true,
    val scenes: List<SleepPreset> = emptyList(),
    val featuredScenes: List<SleepPreset> = emptyList(),
    val recentlyUpdatedScenes: List<SleepPreset> = emptyList(),
    val meditationScenes: List<SleepPreset> = emptyList(),
    val soundscapeScenes: List<SleepPreset> = emptyList(),
    val healingMusicScenes: List<SleepPreset> = emptyList(),
    val quickSounds: List<MixSoundState> = emptyList(),
    val sleepStories: List<Podcast> = emptyList(),
    val dreamPreviews: List<DreamSymbolItem> = DreamInterpretationCatalog.SYMBOLS.take(3),
    val dailyQuote: DailyQuoteItem = DreamInterpretationCatalog.quoteOfTheDay(),
    val favoriteScenes: List<SleepPreset> = emptyList(),
    val userMixes: List<UserMix> = emptyList(),
    val selectedCategory: String? = null,
    val greetingResId: Int = R.string.greeting_evening,
    val categories: List<ContentCategory> = emptyList(),
)
