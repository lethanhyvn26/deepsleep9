package com.sleepsounds.storytelling.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.sleepsounds.storytelling.domain.model.SleepQuizProfile
import com.sleepsounds.storytelling.util.AppBackgrounds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val isPremiumKey = booleanPreferencesKey("is_premium")
    private val dbVersionKey = intPreferencesKey("db_version")
    private val userNameKey = stringPreferencesKey("user_name")
    private val streakKey = intPreferencesKey("streak_days")
    private val mindfulMinutesKey = longPreferencesKey("mindful_minutes")
    private val midnightRescuePresetIdKey = stringPreferencesKey("midnight_rescue_preset_id")
    private val midnightRescueSoundIdKey = stringPreferencesKey("midnight_rescue_sound_id")
    private val lastMoodKey = stringPreferencesKey("last_mood")
    private val pendingDbPathKey = stringPreferencesKey("pending_db_path")
    private val hasCompletedOnboardingKey = booleanPreferencesKey("has_completed_onboarding_v31_clean")
    private val hasSelectedLanguageFirstOpenKey = booleanPreferencesKey("has_selected_language_first_open_v5")
    private val userFavoriteIdsKey = stringSetPreferencesKey("user_favorite_ids")
    private val contentPathCacheKey = stringPreferencesKey("content_path_cache")
    private val appBackgroundIdKey = stringPreferencesKey("app_background_id")
    private val appLanguageKey = stringPreferencesKey("app_language_v3_en_default")
    private val appLaunchCountKey = intPreferencesKey("app_launch_count")
    private val hasRatedAppKey = booleanPreferencesKey("has_rated_app")
    private val lastRatePromptLaunchKey = intPreferencesKey("last_rate_prompt_launch")

    private val quizSleepHoursKey = stringPreferencesKey("quiz_sleep_hours")
    private val quizSnoreHabitKey = stringPreferencesKey("quiz_snore_habit")
    private val quizConditionsKey = stringSetPreferencesKey("quiz_conditions")
    private val quizReportScoreKey = intPreferencesKey("quiz_report_score")

    private val trackerPlacementOnKey = booleanPreferencesKey("tracker_placement_on")
    private val trackerBatteryWarningOnKey = booleanPreferencesKey("tracker_battery_warning_on")
    private val sleepNoteEnabledKey = booleanPreferencesKey("sleep_note_enabled")
    private val activeSleepNotesKey = stringSetPreferencesKey("active_sleep_notes")
    private val wakeUpMoodEnabledKey = booleanPreferencesKey("wake_up_mood_enabled")
    private val sleepReminderEnabledKey = booleanPreferencesKey("sleep_reminder_enabled")
    private val sleepReminderTimeKey = stringPreferencesKey("sleep_reminder_time")

    private val luckyDrawClaimedKey = booleanPreferencesKey("lucky_draw_claimed")
    private val specialOfferExpiresAtMsKey = longPreferencesKey("special_offer_expires_at_ms")

    private val rewardedUnlockExpirationsKey = stringPreferencesKey("rewarded_unlock_expirations_v1")
    private val rewardedAdWatchCountKey = intPreferencesKey("rewarded_ad_watch_count_v2")
    private val rewardedAdFreeUntilMsKey = longPreferencesKey("rewarded_ad_free_until_ms_v2")

    private val expirationTickerFlow: Flow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(15_000L)
        }
    }

    val isPremium: Flow<Boolean> = combine(
        dataStore.data,
        expirationTickerFlow,
    ) { prefs, nowMs ->
        val paidVip = prefs[isPremiumKey] ?: false
        val adFreeUntil = prefs[rewardedAdFreeUntilMsKey] ?: 0L
        paidVip || adFreeUntil > nowMs
    }.distinctUntilChanged()

    val isPaidSubscriber: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[isPremiumKey] ?: false
    }

    val rewardedAdWatchCount: Flow<Int> = dataStore.data.map { prefs ->
        (prefs[rewardedAdWatchCountKey] ?: 0).coerceIn(0, REWARDED_VIDEOS_FOR_AD_FREE_PASS)
    }

    val rewardedAdFreeUntilMs: Flow<Long> = dataStore.data.map { prefs ->
        prefs[rewardedAdFreeUntilMsKey] ?: 0L
    }

    val activeRewardedUnlockedIds: Flow<Set<String>> = combine(
        dataStore.data,
        expirationTickerFlow,
    ) { prefs, nowMs ->
        parseRewardedUnlockMap(prefs[rewardedUnlockExpirationsKey])
            .filterValues { expiresAt -> expiresAt > nowMs }
            .keys
    }.distinctUntilChanged()

    val dbVersion: Flow<Int> = dataStore.data.map { it[dbVersionKey] ?: 0 }
    val pendingDbPath: Flow<String?> = dataStore.data.map { it[pendingDbPathKey] }
    val hasCompletedOnboarding: Flow<Boolean> =
        dataStore.data.map { it[hasCompletedOnboardingKey] ?: false }
    val hasSelectedLanguageFirstOpen: Flow<Boolean> =
        dataStore.data.map { it[hasSelectedLanguageFirstOpenKey] ?: false }
    val userName: Flow<String> = dataStore.data.map { it[userNameKey] ?: "Friend" }
    val streakDays: Flow<Int> = dataStore.data.map { it[streakKey] ?: 0 }
    val mindfulMinutes: Flow<Long> = dataStore.data.map { it[mindfulMinutesKey] ?: 0L }
    val midnightRescuePresetId: Flow<String> = dataStore.data.map { prefs ->
        prefs[midnightRescuePresetIdKey]
            ?: legacyMidnightPresetId(prefs[midnightRescueSoundIdKey])
            ?: DEFAULT_MIDNIGHT_PRESET_ID
    }
    val midnightRescueSoundId: Flow<String> = midnightRescuePresetId
    val lastMood: Flow<String?> = dataStore.data.map { it[lastMoodKey] }
    val appBackgroundId: Flow<String> = dataStore.data.map { prefs ->
        AppBackgrounds.normalizeId(prefs[appBackgroundIdKey])
    }
    val appLanguage: Flow<String> = dataStore.data.map { prefs ->
        normalizeLanguage(prefs[appLanguageKey])
    }

    val sleepQuizProfile: Flow<SleepQuizProfile> = dataStore.data.map { prefs ->
        SleepQuizProfile(
            averageSleepHours = prefs[quizSleepHoursKey] ?: "6–8 hours",
            snoreHabit = prefs[quizSnoreHabitKey] ?: "Sometimes",
            sleepConditions = prefs[quizConditionsKey] ?: emptySet(),
            calculatedReportScore = prefs[quizReportScoreKey] ?: 94,
        )
    }

    val trackerPlacementOn: Flow<Boolean> = dataStore.data.map { it[trackerPlacementOnKey] ?: true }
    val trackerBatteryWarningOn: Flow<Boolean> =
        dataStore.data.map { it[trackerBatteryWarningOnKey] ?: true }
    val sleepNoteEnabled: Flow<Boolean> = dataStore.data.map { it[sleepNoteEnabledKey] ?: false }
    val activeSleepNotes: Flow<Set<String>> =
        dataStore.data.map { it[activeSleepNotesKey] ?: emptySet() }
    val wakeUpMoodEnabled: Flow<Boolean> = dataStore.data.map { it[wakeUpMoodEnabledKey] ?: true }
    val sleepReminderEnabled: Flow<Boolean> =
        dataStore.data.map { it[sleepReminderEnabledKey] ?: false }
    val sleepReminderTime: Flow<String> =
        dataStore.data.map { it[sleepReminderTimeKey] ?: "22:30" }

    val luckyDrawClaimed: Flow<Boolean> = dataStore.data.map { it[luckyDrawClaimedKey] ?: false }
    val specialOfferExpiresAtMs: Flow<Long> =
        dataStore.data.map { it[specialOfferExpiresAtMsKey] ?: 0L }

    val shouldShowRatePrompt: Flow<Boolean> = dataStore.data.map { prefs ->
        val hasRated = prefs[hasRatedAppKey] ?: false
        val launches = prefs[appLaunchCountKey] ?: 0
        val lastPromptLaunch = prefs[lastRatePromptLaunchKey] ?: 0
        !hasRated && launches >= 2 && (lastPromptLaunch == 0 || launches - lastPromptLaunch >= 3)
    }

    suspend fun recordAppLaunch() {
        dataStore.edit { prefs ->
            val current = prefs[appLaunchCountKey] ?: 0
            prefs[appLaunchCountKey] = current + 1
        }
    }

    suspend fun markAppRated() {
        dataStore.edit { prefs ->
            prefs[hasRatedAppKey] = true
        }
    }

    suspend fun deferRatePrompt() {
        dataStore.edit { prefs ->
            val launches = prefs[appLaunchCountKey] ?: 1
            prefs[lastRatePromptLaunchKey] = launches
        }
    }

    suspend fun setPremium(value: Boolean) {
        dataStore.edit { it[isPremiumKey] = value }
    }

    /**
     * Records 1 completed Rewarded Ad view.
     * - Optionally unlocks [unlockItemId] for 12 hours (`REWARDED_UNLOCK_DURATION_MS`).
     * - Increments the 20-ad milestone counter (`0/20` -> `20/20`).
     * - When reaching 20 ads (`REWARDED_VIDEOS_FOR_AD_FREE_PASS`), activates a 3-Hour Ad-Free & Full VIP Pass
     *   (`REWARDED_AD_FREE_PASS_DURATION_MS = 3 hours`) and resets the counter to 0.
     * @return `true` if this 20th ad triggered the 3-Hour Ad-Free & VIP Pass.
     */
    suspend fun recordRewardedAdEarned(
        unlockItemId: String? = null,
        itemUnlockDurationMs: Long = REWARDED_UNLOCK_DURATION_MS,
    ): Boolean {
        var activatedThreeHourPass = false
        val now = System.currentTimeMillis()
        dataStore.edit { prefs ->
            if (!unlockItemId.isNullOrBlank()) {
                val activeUnlocks = parseRewardedUnlockMap(prefs[rewardedUnlockExpirationsKey])
                    .filterValues { expiresAt -> expiresAt > now }
                    .toMutableMap()
                activeUnlocks[unlockItemId] = now + itemUnlockDurationMs
                prefs[rewardedUnlockExpirationsKey] = serializeRewardedUnlockMap(activeUnlocks)
            }

            val currentCount = (prefs[rewardedAdWatchCountKey] ?: 0).coerceAtLeast(0)
            val nextCount = currentCount + 1
            if (nextCount >= REWARDED_VIDEOS_FOR_AD_FREE_PASS) {
                prefs[rewardedAdWatchCountKey] = 0
                val existingAdFreeUntil = prefs[rewardedAdFreeUntilMsKey] ?: 0L
                val baseTime = maxOf(existingAdFreeUntil, now)
                prefs[rewardedAdFreeUntilMsKey] = baseTime + REWARDED_AD_FREE_PASS_DURATION_MS
                activatedThreeHourPass = true
            } else {
                prefs[rewardedAdWatchCountKey] = nextCount
            }
        }
        return activatedThreeHourPass
    }

    suspend fun unlockItemWithRewardedAd(
        itemId: String,
        durationMs: Long = REWARDED_UNLOCK_DURATION_MS,
    ): Boolean = recordRewardedAdEarned(unlockItemId = itemId, itemUnlockDurationMs = durationMs)

    suspend fun isItemUnlockedByReward(itemId: String): Boolean {
        if (itemId.isBlank()) return false
        val prefs = dataStore.data.first()
        val now = System.currentTimeMillis()
        if ((prefs[isPremiumKey] ?: false) || (prefs[rewardedAdFreeUntilMsKey] ?: 0L) > now) {
            return true
        }
        val map = parseRewardedUnlockMap(prefs[rewardedUnlockExpirationsKey])
        return (map[itemId] ?: 0L) > now
    }

    private fun parseRewardedUnlockMap(raw: String?): Map<String, Long> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(";")
            .mapNotNull { token ->
                val idx = token.indexOf('=')
                if (idx <= 0) return@mapNotNull null
                val key = token.substring(0, idx).trim()
                val expiry = token.substring(idx + 1).trim().toLongOrNull() ?: return@mapNotNull null
                if (key.isEmpty()) null else key to expiry
            }
            .toMap()
    }

    private fun serializeRewardedUnlockMap(map: Map<String, Long>): String =
        map.entries.joinToString(";") { "${it.key}=${it.value}" }

    suspend fun setDbVersion(version: Int) {
        dataStore.edit { it[dbVersionKey] = version }
    }

    suspend fun setPendingDbPath(path: String) {
        dataStore.edit { it[pendingDbPathKey] = path }
    }

    suspend fun clearPendingDbPath() {
        dataStore.edit { it.remove(pendingDbPathKey) }
    }

    suspend fun setOnboardingCompleted() {
        dataStore.edit { it[hasCompletedOnboardingKey] = true }
    }

    suspend fun saveSleepQuizProfile(profile: SleepQuizProfile) {
        dataStore.edit { prefs ->
            prefs[quizSleepHoursKey] = profile.averageSleepHours
            prefs[quizSnoreHabitKey] = profile.snoreHabit
            prefs[quizConditionsKey] = profile.sleepConditions
            prefs[quizReportScoreKey] = profile.calculatedReportScore
        }
    }

    suspend fun setTrackerPlacementOn(enabled: Boolean) {
        dataStore.edit { it[trackerPlacementOnKey] = enabled }
    }

    suspend fun setTrackerBatteryWarningOn(enabled: Boolean) {
        dataStore.edit { it[trackerBatteryWarningOnKey] = enabled }
    }

    suspend fun setSleepNoteEnabled(enabled: Boolean) {
        dataStore.edit { it[sleepNoteEnabledKey] = enabled }
    }

    suspend fun toggleSleepNoteFactor(factorId: String) {
        dataStore.edit { prefs ->
            val current = prefs[activeSleepNotesKey] ?: emptySet()
            prefs[activeSleepNotesKey] = if (factorId in current) {
                current - factorId
            } else {
                current + factorId
            }
        }
    }

    suspend fun clearActiveSleepNotes() {
        dataStore.edit { it.remove(activeSleepNotesKey) }
    }

    suspend fun setWakeUpMoodEnabled(enabled: Boolean) {
        dataStore.edit { it[wakeUpMoodEnabledKey] = enabled }
    }

    suspend fun setSleepReminder(enabled: Boolean, timeHHmm: String = "22:30") {
        dataStore.edit { prefs ->
            prefs[sleepReminderEnabledKey] = enabled
            prefs[sleepReminderTimeKey] = timeHHmm
        }
    }

    suspend fun markLuckyDrawClaimed(offerDurationMs: Long = 3_600_000L) {
        dataStore.edit { prefs ->
            prefs[luckyDrawClaimedKey] = true
            prefs[specialOfferExpiresAtMsKey] = System.currentTimeMillis() + offerDurationMs
        }
    }

    suspend fun setUserName(name: String) {
        dataStore.edit { it[userNameKey] = name }
    }

    suspend fun setMidnightRescuePresetId(presetId: String) {
        dataStore.edit {
            it[midnightRescuePresetIdKey] = presetId
            it.remove(midnightRescueSoundIdKey)
        }
    }

    suspend fun setMidnightRescueSoundId(soundId: String) {
        setMidnightRescuePresetId(legacyMidnightPresetId(soundId) ?: DEFAULT_MIDNIGHT_PRESET_ID)
    }

    suspend fun setLastMood(mood: String) {
        dataStore.edit { it[lastMoodKey] = mood }
    }

    suspend fun setAppBackgroundId(id: String) {
        dataStore.edit { it[appBackgroundIdKey] = AppBackgrounds.normalizeId(id) }
    }

    suspend fun setAppLanguage(language: String) {
        dataStore.edit { it[appLanguageKey] = normalizeLanguage(language) }
    }

    suspend fun markLanguageSelectedFirstOpen(language: String) {
        dataStore.edit {
            it[appLanguageKey] = normalizeLanguage(language)
            it[hasSelectedLanguageFirstOpenKey] = true
        }
    }

    suspend fun incrementMindfulMinutes(minutes: Long) {
        dataStore.edit { prefs ->
            val current = prefs[mindfulMinutesKey] ?: 0L
            prefs[mindfulMinutesKey] = current + minutes
        }
    }

    suspend fun getUserFavoriteIds(): Set<String> =
        dataStore.data.first()[userFavoriteIdsKey] ?: emptySet()

    suspend fun addUserFavorite(id: String) {
        dataStore.edit { prefs ->
            val current = prefs[userFavoriteIdsKey] ?: emptySet()
            prefs[userFavoriteIdsKey] = current + id
        }
    }

    suspend fun removeUserFavorite(id: String) {
        dataStore.edit { prefs ->
            val current = prefs[userFavoriteIdsKey] ?: emptySet()
            prefs[userFavoriteIdsKey] = current - id
        }
    }

    suspend fun updateContentPathCache(
        trackId: String,
        audioPath: String? = null,
        imagePath: String? = null,
    ) {
        if (audioPath == null && imagePath == null) return
        dataStore.edit { prefs ->
            val cache = parseContentPathCache(prefs[contentPathCacheKey]).toMutableMap()
            val existing = cache[trackId].orEmpty().split("|", limit = 2)
            val audio = audioPath ?: existing.getOrElse(0) { "" }
            val image = imagePath ?: existing.getOrElse(1) { "" }
            cache[trackId] = "$audio|$image"
            prefs[contentPathCacheKey] = serializeContentPathCache(cache)
        }
    }

    suspend fun getCachedContentPaths(trackId: String): Pair<String?, String?> {
        val cache = parseContentPathCache(dataStore.data.first()[contentPathCacheKey])
        val entry = cache[trackId] ?: return null to null
        val parts = entry.split("|", limit = 2)
        return parts.getOrNull(0)?.takeIf { it.isNotBlank() } to
            parts.getOrNull(1)?.takeIf { it.isNotBlank() }
    }

    private fun parseContentPathCache(raw: String?): Map<String, String> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(";")
            .mapNotNull { entry ->
                val idx = entry.indexOf('=')
                if (idx <= 0) return@mapNotNull null
                entry.substring(0, idx) to entry.substring(idx + 1)
            }
            .toMap()
    }

    private fun serializeContentPathCache(cache: Map<String, String>): String =
        cache.entries.joinToString(";") { "${it.key}=${it.value}" }

    companion object {
        const val DEFAULT_MIDNIGHT_PRESET_ID = "sc_under_the_blanket"
        const val REWARDED_UNLOCK_DURATION_MS = 12L * 60L * 60L * 1000L
        const val REWARDED_VIDEOS_FOR_AD_FREE_PASS = 20
        const val REWARDED_AD_FREE_PASS_DURATION_MS = 3L * 60L * 60L * 1000L

        const val LANGUAGE_EN = "en"
        const val LANGUAGE_PT_BR = "pt-BR"
        const val LANGUAGE_VI = "vi"
        const val LANGUAGE_ES = "es"
        const val LANGUAGE_FR = "fr"
        const val LANGUAGE_DE = "de"
        const val LANGUAGE_JA = "ja"
        const val LANGUAGE_KO = "ko"
        const val LANGUAGE_IT = "it"
        const val LANGUAGE_RU = "ru"
        const val LANGUAGE_ID = "id"
        const val LANGUAGE_TR = "tr"
        const val LANGUAGE_HI = "hi"

        fun normalizeLanguage(raw: String?): String {
            if (raw.isNullOrBlank()) return LANGUAGE_EN
            val cleaned = raw.trim().lowercase(Locale.ROOT)
            return when {
                cleaned.startsWith("pt") -> LANGUAGE_PT_BR
                cleaned.startsWith("vi") -> LANGUAGE_VI
                cleaned.startsWith("es") -> LANGUAGE_ES
                cleaned.startsWith("fr") -> LANGUAGE_FR
                cleaned.startsWith("de") -> LANGUAGE_DE
                cleaned.startsWith("ja") -> LANGUAGE_JA
                cleaned.startsWith("ko") -> LANGUAGE_KO
                cleaned.startsWith("it") -> LANGUAGE_IT
                cleaned.startsWith("ru") -> LANGUAGE_RU
                cleaned.startsWith("id") || cleaned.startsWith("in") -> LANGUAGE_ID
                cleaned.startsWith("tr") -> LANGUAGE_TR
                cleaned.startsWith("hi") -> LANGUAGE_HI
                else -> LANGUAGE_EN
            }
        }

        fun localeForLanguage(raw: String?): Locale = when (normalizeLanguage(raw)) {
            LANGUAGE_PT_BR -> Locale.forLanguageTag("pt-BR")
            LANGUAGE_VI -> Locale.forLanguageTag("vi-VN")
            LANGUAGE_ES -> Locale.forLanguageTag("es-ES")
            LANGUAGE_FR -> Locale.forLanguageTag("fr-FR")
            LANGUAGE_DE -> Locale.forLanguageTag("de-DE")
            LANGUAGE_JA -> Locale.forLanguageTag("ja-JP")
            LANGUAGE_KO -> Locale.forLanguageTag("ko-KR")
            LANGUAGE_IT -> Locale.forLanguageTag("it-IT")
            LANGUAGE_RU -> Locale.forLanguageTag("ru-RU")
            LANGUAGE_ID -> Locale.forLanguageTag("id-ID")
            LANGUAGE_TR -> Locale.forLanguageTag("tr-TR")
            LANGUAGE_HI -> Locale.forLanguageTag("hi-IN")
            else -> Locale.forLanguageTag("en-US")
        }

        fun legacyMidnightPresetId(legacySoundOrPresetId: String?): String? {
            if (legacySoundOrPresetId.isNullOrBlank()) return null
            if (legacySoundOrPresetId.startsWith("sc_") ||
                legacySoundOrPresetId.startsWith("mu_") ||
                legacySoundOrPresetId.startsWith("os_")
            ) {
                return legacySoundOrPresetId
            }
            return when (legacySoundOrPresetId) {
                "light_rain", "heavy_rain", "rain_on_roof" -> "sc_rainy_day_comfort"
                "white_noise", "soft_fan", "432hz" -> DEFAULT_MIDNIGHT_PRESET_ID
                "fire" -> "sc_cozy_cabin_fireplace"
                "water_wave", "calm_sea", "beach" -> "sc_ocean_heartbeat"
                else -> null
            }
        }
    }
}
