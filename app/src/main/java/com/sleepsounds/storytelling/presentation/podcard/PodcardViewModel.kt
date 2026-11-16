package com.sleepsounds.storytelling.presentation.podcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepsounds.storytelling.data.remote.ContentCategory
import com.sleepsounds.storytelling.data.repository.ContentRepository
import com.sleepsounds.storytelling.data.repository.UserPreferencesRepository
import com.sleepsounds.storytelling.domain.model.Podcast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private fun formatPodcastCategoryLabel(rawId: String): String =
    rawId.replace('_', ' ').lowercase().replaceFirstChar { it.uppercaseChar() }
internal fun resolvePodcastCategories(
    catalogCategories: List<ContentCategory>,
    availableCategoryIds: List<String>,
): List<ContentCategory> {
    if (availableCategoryIds.isEmpty()) return emptyList()
    if (catalogCategories.isEmpty()) {
        return availableCategoryIds.map { rawId ->
            ContentCategory(id = rawId, label = formatPodcastCategoryLabel(rawId))
        }
    }
    val matched = catalogCategories.filter { cat ->
        availableCategoryIds.any { it.equals(cat.id, ignoreCase = true) }
    }
    if (matched.isNotEmpty()) return matched
    return availableCategoryIds.map { rawId ->
        ContentCategory(id = rawId, label = formatPodcastCategoryLabel(rawId))
    }
}
/**
 * ViewModel managing category filtering for the bedtime stories / Podcard catalog tab.
 */
@HiltViewModel
class PodcardViewModel @Inject constructor(
    contentRepository: ContentRepository,
) : ViewModel() {

    private val activeCategoryFilter = MutableStateFlow<String?>(null)

    private val englishPodcastsFlow =
        contentRepository.observePodcastsByLanguage(UserPreferencesRepository.LANGUAGE_EN)

    val uiState: StateFlow<PodcardUiState> = combine(
        englishPodcastsFlow,
        contentRepository.observePodcastCategories(),
        activeCategoryFilter,
    ) { allPodcasts, catalogCategories, chosenCategory ->
        val presentCategoryIds = allPodcasts.flatMap { it.categories }.distinct()
        val resolvedCategories = resolvePodcastCategories(catalogCategories, presentCategoryIds)
        val validFilter = chosenCategory?.takeIf { filterKey ->
            presentCategoryIds.any { it.equals(filterKey, ignoreCase = true) }
        }
        val visiblePodcasts = if (validFilter.isNullOrBlank()) {
            allPodcasts
        } else {
            allPodcasts.filter { item ->
                item.categories.any { it.equals(validFilter, ignoreCase = true) }
            }
        }
        PodcardUiState(
            podcasts = visiblePodcasts,
            selectedCategory = validFilter,
            categories = resolvedCategories,
            language = UserPreferencesRepository.LANGUAGE_EN,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PodcardUiState())

    fun selectCategory(category: String?) {
        activeCategoryFilter.value = category
    }
}
data class PodcardUiState(
    val podcasts: List<Podcast> = emptyList(),
    val selectedCategory: String? = null,
    val categories: List<ContentCategory> = emptyList(),
    val language: String = UserPreferencesRepository.LANGUAGE_EN,
)
