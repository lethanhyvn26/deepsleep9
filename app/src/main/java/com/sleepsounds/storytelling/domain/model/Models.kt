package com.sleepsounds.storytelling.domain.model

import androidx.compose.runtime.Immutable

enum class TimeOfDay {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT,
}
enum class Mood(val emoji: String, val label: String) {
    CALM("😌", "Calm"),
    TIRED("😴", "Tired"),
    STRESSED("😰", "Stressed"),
}
@Immutable
data class SleepQuizProfile(
    val averageSleepHours: String = "6–8 hours",
    val snoreHabit: String = "Sometimes",
    val sleepConditions: Set<String> = emptySet(),
    val calculatedReportScore: Int = 94,
)
enum class SleepNoteFactor(val id: String, val label: String, val emoji: String) {
    COFFEE("coffee", "Caffeine", "☕"),
    WORKOUT("workout", "Workout", "🏋️"),
    LATE_MEAL("late_meal", "Late Meal", "🍽️"),
    SCREEN_TIME("screen_time", "Screen Time", "📱"),
    STRESS("stress", "Stressful Day", "⚡"),
    READING("reading", "Reading", "📖"),
    MEDITATION("meditation", "Meditation", "🧘"),
    SHOWER("warm_shower", "Warm Shower", "🚿"),
}
enum class WakeUpMood(val emoji: String, val label: String) {
    GREAT("🤩", "Refreshed"),
    GOOD("😊", "Good"),
    OKAY("😐", "Okay"),
    TIRED("😴", "Tired"),
    EXHAUSTED("😫", "Exhausted"),
}
@Immutable
data class SmartAlarm(
    val id: Int = 1,
    val hour: Int = 8,
    val minute: Int = 30,
    val enabled: Boolean = true,
    val smartWindowMinutes: Int = 20,
    val daysOfWeekMask: Int = 127,
    val ringtoneSoundId: String = "morning_relaxation",
    val vibrate: Boolean = true,
    val snoozeMinutes: Int = 10,
    val label: String = "Wake-up Alarm",
) {
    val formattedTime: String
        get() = "%02d:%02d".format(hour, minute)
}
@Immutable
data class SleepSession(
    val id: String,
    val startedAt: Long,
    val endedAt: Long,
    val durationMinutes: Int,
    val sleepScore: Int,
    val awakeMinutes: Int,
    val lightSleepMinutes: Int,
    val deepSleepMinutes: Int,
    val fellAsleepMinutes: Int = 12,
    val snoreMinutes: Int = 0,
    val snoreCount: Int = 0,
    val sleepNotes: List<String> = emptyList(),
    val wakeUpMood: WakeUpMood = WakeUpMood.GOOD,
    val stages: List<SleepStageSample> = emptyList(),
    val isDemoData: Boolean = false,
) {
    val awakePercent: Int
        get() = if (durationMinutes > 0) {
            ((awakeMinutes * 100f) / durationMinutes).toInt().coerceIn(0, 100)
        } else {
            5
        }

    val lightSleepPercent: Int
        get() = if (durationMinutes > 0) {
            ((lightSleepMinutes * 100f) / durationMinutes).toInt().coerceIn(0, 100)
        } else {
            52
        }

    val deepSleepPercent: Int
        get() = (100 - awakePercent - lightSleepPercent).coerceIn(0, 100)
}
@Immutable
data class SleepStageSample(
    val minuteOffset: Int,
    val stage: SleepStageType,
)
enum class SleepStageType {
    AWAKE,
    LIGHT,
    DEEP,
}
@Immutable
data class PlaybackHistoryItem(
    val itemKey: String,
    val itemId: String,
    val itemType: HistoryItemType,
    val title: String,
    val subtitle: String,
    val imagePath: String? = null,
    val durationSeconds: Int = 0,
    val isPremium: Boolean = false,
    val playedAt: Long = 0L,
) {
    val id: String
        get() = itemKey
}
enum class HistoryItemType {
    SOUND,
    SCENE,
    MIX,
    PODCAST,
    YOUTUBE,
}
@Immutable
data class UserMix(
    val id: String,
    val name: String,
    val items: List<MixSoundState>,
    val createdAt: Long,
)
@Immutable
data class MixSoundState(
    val soundId: String,
    val name: String,
    val isActive: Boolean,
    val volume: Float,
    val isPremium: Boolean,
    val categories: List<String> = emptyList(),
    val symbolPath: String? = null,
    val audioPath: String? = null,
)
@Immutable
data class AudioTrack(
    val id: String,
    val name: String,
    val type: ContentType,
    val categories: List<String>,
    val isPremium: Boolean,
    val isFavorite: Boolean,
    val defaultVolume: Float,
    val imagePath: String? = null,
    val audioPath: String? = null,
    val mixLayerCount: Int = 0,
)
@Immutable
data class Podcast(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val categories: List<String>,
    val durationSeconds: Int = 0,
    val imagePath: String? = null,
    val isPremium: Boolean = false,
    val isFavorite: Boolean = false,
    val publishedAt: String? = null,
    val language: String = "en",
    val chapters: List<PodcastChapter> = emptyList(),
)
@Immutable
data class PodcastChapter(
    val id: String,
    val podcastId: String,
    val title: String,
    val chapterIndex: Int,
    val durationSeconds: Int = 0,
    val audioPath: String? = null,
)
@Immutable
data class SleepPreset(
    val id: String,
    val name: String,
    val categories: List<String>,
    val isPremium: Boolean,
    val isFavorite: Boolean,
    val items: List<PresetSoundItem> = emptyList(),
    val imagePath: String? = null,
    val displaySymbolPath: String? = null,
    val isUserModified: Boolean = false,
)
@Immutable
data class PresetSoundItem(
    val soundId: String,
    val name: String,
    val volume: Float,
    val symbolPath: String? = null,
    val audioPath: String? = null,
    val isPremium: Boolean = false,
)
/**
 * Primary catalog content kinds supported by the audio engine.
 */
enum class ContentType {
    SOUND,
    SLEEP_PRESET,
    PODCAST,
}
