package com.sleepsounds.storytelling.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepsounds.storytelling.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing first-open language selection and onboarding completion (No Ads, No Billing).
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
) : ViewModel() {

    val appLanguage: StateFlow<String> = preferences.appLanguage
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferencesRepository.LANGUAGE_EN)

    val hasSelectedLanguageFirstOpen: StateFlow<Boolean> =
        preferences.hasSelectedLanguageFirstOpen
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val appStartDestination: StateFlow<AppStartDestination> = combine(
        preferences.hasCompletedOnboarding,
        preferences.hasSelectedLanguageFirstOpen,
    ) { quizDone, languagePicked ->
        if (quizDone && languagePicked) {
            AppStartDestination.Home
        } else {
            AppStartDestination.Onboarding
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AppStartDestination.Loading)

    val hasCompletedOnboarding: StateFlow<Boolean> =
        preferences.hasCompletedOnboarding
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setAppLanguage(languageCode: String) {
        viewModelScope.launch {
            preferences.setAppLanguage(languageCode)
        }
    }

    fun confirmFirstOpenLanguage(languageCode: String) {
        viewModelScope.launch {
            preferences.markLanguageSelectedFirstOpen(languageCode)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferences.setOnboardingCompleted()
        }
    }
}
enum class AppStartDestination {
    Loading,
    Onboarding,
    Home,
}
