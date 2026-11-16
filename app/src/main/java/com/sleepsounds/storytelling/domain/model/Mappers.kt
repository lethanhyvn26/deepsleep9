package com.sleepsounds.storytelling.domain.model

import com.sleepsounds.storytelling.data.local.entity.MediaCategoryEntity
import com.sleepsounds.storytelling.data.local.entity.PlaybackHistoryEntity
import com.sleepsounds.storytelling.data.local.entity.PodcastCategoryEntity
import com.sleepsounds.storytelling.data.local.entity.PodcastChapterEntity
import com.sleepsounds.storytelling.data.local.entity.PodcastEntity
import com.sleepsounds.storytelling.data.local.entity.SleepPresetEntity
import com.sleepsounds.storytelling.data.local.entity.SleepPresetItemEntity
import com.sleepsounds.storytelling.data.local.entity.SleepSessionEntity
import com.sleepsounds.storytelling.data.local.entity.SmartAlarmEntity
import com.sleepsounds.storytelling.data.local.entity.SoundEntity
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

internal fun serializeSleepStageSamples(samples: List<SleepStageSample>): String {
    val jsonArray = JSONArray()
    for (sample in samples) {
        val node = JSONObject()
        node.put("t", sample.minuteOffset)
        node.put("s", sample.stage.name)
        jsonArray.put(node)
    }
    return jsonArray.toString()
}
internal fun parseSleepStageSamples(rawJson: String): List<SleepStageSample> = runCatching {
    val jsonArray = JSONArray(rawJson)
    buildList {
        for (idx in 0 until jsonArray.length()) {
            val node = jsonArray.optJSONObject(idx) ?: continue
            val minuteOffset = node.optInt("t", idx * 15)
            val stageCode = node.optString("s", "LIGHT").uppercase()
            val stageType = runCatching { SleepStageType.valueOf(stageCode) }
                .getOrDefault(SleepStageType.LIGHT)
            add(SleepStageSample(minuteOffset = minuteOffset, stage = stageType))
        }
    }
}.getOrDefault(emptyList())
fun SmartAlarm.toEntity(): SmartAlarmEntity = SmartAlarmEntity(
    id = id,
    hour = hour.coerceIn(0, 23),
    minute = minute.coerceIn(0, 59),
    enabled = if (enabled) 1 else 0,
    smartWindowMinutes = smartWindowMinutes.coerceIn(0, 60),
    daysOfWeekMask = daysOfWeekMask,
    ringtoneSoundId = ringtoneSoundId.ifBlank { "morning_relaxation" },
    vibrate = if (vibrate) 1 else 0,
    snoozeMinutes = snoozeMinutes.coerceIn(1, 30),
    label = label.ifBlank { "Wake-up Alarm" },
)
fun SmartAlarmEntity.toDomain(): SmartAlarm = SmartAlarm(
    id = id,
    hour = hour.coerceIn(0, 23),
    minute = minute.coerceIn(0, 59),
    enabled = enabled == 1,
    smartWindowMinutes = smartWindowMinutes.coerceIn(0, 60),
    daysOfWeekMask = daysOfWeekMask,
    ringtoneSoundId = ringtoneSoundId.ifBlank { "morning_relaxation" },
    vibrate = vibrate == 1,
    snoozeMinutes = snoozeMinutes.coerceIn(1, 30),
    label = label.ifBlank { "Wake-up Alarm" },
)
fun SleepSession.toEntity(): SleepSessionEntity = SleepSessionEntity(
    id = id,
    startedAt = startedAt,
    endedAt = endedAt,
    durationMinutes = durationMinutes,
    sleepScore = sleepScore,
    awakeMinutes = awakeMinutes,
    lightSleepMinutes = lightSleepMinutes,
    deepSleepMinutes = deepSleepMinutes,
    fellAsleepMinutes = fellAsleepMinutes,
    snoreMinutes = snoreMinutes,
    snoreCount = snoreCount,
    sleepNotesCsv = sleepNotes.joinToString(","),
    wakeUpMood = wakeUpMood.name,
    stagesJson = serializeSleepStageSamples(stages),
)
fun SleepSessionEntity.toDomain(): SleepSession {
    val parsedNotes = sleepNotesCsv.split(',')
        .map { it.trim() }
        .filter { it.isNotBlank() }
    val parsedMood = runCatching { WakeUpMood.valueOf(wakeUpMood.uppercase()) }
        .getOrDefault(WakeUpMood.GOOD)
    return SleepSession(
        id = id,
        startedAt = startedAt,
        endedAt = endedAt,
        durationMinutes = durationMinutes,
        sleepScore = sleepScore,
        awakeMinutes = awakeMinutes,
        lightSleepMinutes = lightSleepMinutes,
        deepSleepMinutes = deepSleepMinutes,
        fellAsleepMinutes = fellAsleepMinutes,
        snoreMinutes = snoreMinutes,
        snoreCount = snoreCount,
        sleepNotes = parsedNotes,
        wakeUpMood = parsedMood,
        stages = parseSleepStageSamples(stagesJson),
        isDemoData = false,
    )
}
fun PlaybackHistoryEntity.toDomain(): PlaybackHistoryItem {
    val parsedType = runCatching { HistoryItemType.valueOf(itemType.uppercase()) }
        .getOrDefault(HistoryItemType.SOUND)
    return PlaybackHistoryItem(
        itemKey = itemKey,
        itemId = itemId,
        itemType = parsedType,
        title = title,
        subtitle = subtitle,
        imagePath = imagePath,
        durationSeconds = durationSeconds,
        isPremium = isPremium == 1,
        playedAt = playedAt,
    )
}
fun PodcastCategoryEntity.toCategoryLabel(): String = label.ifBlank { id }
fun MediaCategoryEntity.toCategoryLabel(): String = label.ifBlank { id }
fun PodcastEntity.toPodcast(chapters: List<PodcastChapter> = emptyList()): Podcast {
    val chapterSumSeconds = chapters.sumOf { it.durationSeconds }
    val effectiveDuration = when {
        chapters.size > 1 && chapterSumSeconds > 0 -> chapterSumSeconds
        durationSeconds > 0 -> durationSeconds
        chapterSumSeconds > 0 -> chapterSumSeconds
        else -> 0
    }
    return Podcast(
        id = id,
        title = title,
        subtitle = author ?: narrator,
        description = summary,
        categories = parseCategories(categories),
        durationSeconds = effectiveDuration,
        imagePath = imagePath,
        isPremium = isPremium == 1,
        isFavorite = isFavorite == 1,
        publishedAt = null,
        language = language.ifBlank { "en" },
        chapters = chapters,
    )
}
fun PodcastChapterEntity.toPodcastChapter(): PodcastChapter = PodcastChapter(
    id = id,
    podcastId = podcastId,
    title = title,
    chapterIndex = chapterIndex,
    durationSeconds = durationSeconds,
    audioPath = audioPath,
)
fun SoundEntity.toMixState(
    isActive: Boolean = false,
    volume: Float = this.volume,
): MixSoundState = MixSoundState(
    soundId = id,
    name = name,
    isActive = isActive,
    volume = volume,
    isPremium = isPremium == 1,
    categories = parseCategories(categories),
    symbolPath = symbolPath,
    audioPath = audioPath,
)
fun SleepPreset.toAudioTrack(): AudioTrack = AudioTrack(
    id = id,
    name = name,
    type = ContentType.SLEEP_PRESET,
    categories = categories,
    isPremium = isPremium,
    isFavorite = isFavorite,
    defaultVolume = items.firstOrNull()?.volume ?: 0.7f,
    imagePath = imagePath?.takeIf { it.isNotBlank() } ?: displaySymbolPath,
    audioPath = null,
    mixLayerCount = items.size,
)
fun SleepPresetEntity.toSleepPreset(
    items: List<SleepPresetItemEntity>,
    soundsById: Map<String, SoundEntity>,
    random: Random = Random.Default,
): SleepPreset {
    val mappedLayers = items.mapNotNull { entry ->
        val soundEntity = soundsById[entry.soundId] ?: return@mapNotNull null
        PresetSoundItem(
            soundId = soundEntity.id,
            name = soundEntity.name,
            volume = entry.volume,
            symbolPath = soundEntity.symbolPath,
            audioPath = soundEntity.audioPath,
            isPremium = soundEntity.isPremium == 1,
        )
    }
    val previewSymbol = mappedLayers.randomOrNull(random)?.symbolPath
    return SleepPreset(
        id = id,
        name = name,
        categories = parseCategories(categories),
        isPremium = isPremium == 1,
        isFavorite = isFavorite == 1,
        items = mappedLayers,
        imagePath = imagePath.takeIf { it.isNotBlank() },
        displaySymbolPath = previewSymbol,
    )
}
fun SoundEntity.toAudioTrack(): AudioTrack = AudioTrack(
    id = id,
    name = name,
    type = ContentType.SOUND,
    categories = parseCategories(categories),
    isPremium = isPremium == 1,
    isFavorite = isFavorite == 1,
    defaultVolume = volume,
    imagePath = symbolPath,
    audioPath = audioPath,
)
/**
 * Parses a JSON array or comma-delimited category string into normalized uppercase tags.
 */
fun parseCategories(raw: String): List<String> = runCatching {
    val jsonArray = JSONArray(raw)
    buildList {
        for (idx in 0 until jsonArray.length()) {
            val token = jsonArray.getString(idx).trim()
            if (token.contains(',')) {
                token.split(',')
                    .map { it.trim().trim('"').uppercase() }
                    .filter { it.isNotBlank() }
                    .forEach(::add)
            } else {
                val normalized = token.trim('"').uppercase()
                if (normalized.isNotBlank()) add(normalized)
            }
        }
    }.distinct()
}.getOrElse {
    raw.trim('[', ']')
        .split(',')
        .map { it.trim().trim('"').uppercase() }
        .filter { it.isNotBlank() }
}
