package com.sleepsounds.storytelling.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepsounds.storytelling.ads.AdMobManager
import com.sleepsounds.storytelling.data.media.MediaFileRepository
import com.sleepsounds.storytelling.data.repository.SleepProfileStats
import com.sleepsounds.storytelling.data.repository.SleepTrackerRepository
import com.sleepsounds.storytelling.data.repository.UserPreferencesRepository
import com.sleepsounds.storytelling.domain.model.SmartAlarm
import com.sleepsounds.storytelling.util.AppBackgrounds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userName: String = "Friend",
    val streakDays: Int = 0,
    val mindfulMinutes: Long = 0,
    val downloadedCount: Int = 0,
    val selectedAppBackgroundId: String = AppBackgrounds.DEFAULT_ID,
    val sleepStats: SleepProfileStats = SleepProfileStats(),
    val primaryAlarm: SmartAlarm = SmartAlarm(),
    val isPremium: Boolean = false,
    val isPaidSubscriber: Boolean = false,
)

/**
 * ViewModel managing user sleep calibration stats, smart alarm toggle, VIP status, wallpaper theme, and app language.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val localMediaStore: MediaFileRepository,
    private val trackerRepository: SleepTrackerRepository,
    val adMobManager: AdMobManager,
) : ViewModel() {

    val appLanguage: StateFlow<String> = preferences.appLanguage
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferencesRepository.LANGUAGE_EN)

    fun setAppLanguage(languageCode: String) {
        viewModelScope.launch {
            preferences.setAppLanguage(languageCode)
        }
    }

    val uiState: StateFlow<ProfileUiState> = combine(
        preferences.userName,
        preferences.streakDays,
        preferences.mindfulMinutes,
        preferences.appBackgroundId,
        combine(
            trackerRepository.observeProfileStats(),
            trackerRepository.observePrimaryAlarm(),
            preferences.isPremium,
            preferences.isPaidSubscriber,
        ) { stats, alarm, premium, paid ->
            ProfileEntitlementAndStats(stats, alarm, premium, paid)
        },
    ) { name, streak, mins, bgId, extra ->
        ProfileUiState(
            userName = name,
            streakDays = streak,
            mindfulMinutes = mins,
            downloadedCount = localMediaStore.downloadedSoundCount(),
            selectedAppBackgroundId = AppBackgrounds.resolvedId(bgId),
            sleepStats = extra.stats,
            primaryAlarm = extra.alarm,
            isPremium = extra.isPremium,
            isPaidSubscriber = extra.isPaidSubscriber,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun toggleAlarmEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val existing = trackerRepository.getPrimaryAlarm()
            trackerRepository.savePrimaryAlarm(existing.copy(enabled = enabled))
        }
    }

    fun setAppBackground(id: String) {
        viewModelScope.launch {
            preferences.setAppBackgroundId(AppBackgrounds.normalizeId(id))
        }
    }
}

private data class ProfileEntitlementAndStats(
    val stats: SleepProfileStats,
    val alarm: SmartAlarm,
    val isPremium: Boolean,
    val isPaidSubscriber: Boolean,
)
