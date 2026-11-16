package com.sleepsounds.storytelling.data.repository

import androidx.compose.runtime.Immutable
import com.sleepsounds.storytelling.data.local.dao.SleepSessionDao
import com.sleepsounds.storytelling.data.local.dao.SmartAlarmDao
import com.sleepsounds.storytelling.domain.model.SleepSession
import com.sleepsounds.storytelling.domain.model.SleepStageSample
import com.sleepsounds.storytelling.domain.model.SleepStageType
import com.sleepsounds.storytelling.domain.model.SmartAlarm
import com.sleepsounds.storytelling.domain.model.WakeUpMood
import com.sleepsounds.storytelling.domain.model.toDomain
import com.sleepsounds.storytelling.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository managing overnight sleep sessions, hypnogram stage synthesis, and smart alarm persistence.
 */
@Singleton
class SleepTrackerRepository @Inject constructor(
    private val sleepSessionDao: SleepSessionDao,
    private val smartAlarmDao: SmartAlarmDao,
) {
    fun observeRecordedSessions(): Flow<List<SleepSession>> =
        sleepSessionDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    fun observeSessionsWithDemoFallback(): Flow<List<SleepSession>> =
        sleepSessionDao.observeAll().map { entities ->
            if (entities.isNotEmpty()) {
                entities.map { it.toDomain() }
            } else {
                listOf(buildSampleSleepSession())
            }
        }

    fun observeProfileStats(): Flow<SleepProfileStats> =
        sleepSessionDao.observeAll().map { entities ->
            if (entities.isEmpty()) {
                SleepProfileStats()
            } else {
                val meanScore = entities.map { it.sleepScore }.average().toInt().coerceIn(0, 100)
                val meanDuration = entities.map { it.durationMinutes }.average().toInt().coerceAtLeast(0)
                SleepProfileStats(
                    nightsCount = entities.size,
                    averageQualityScore = meanScore,
                    averageDurationMinutes = meanDuration,
                )
            }
        }

    fun observePrimaryAlarm(): Flow<SmartAlarm> =
        smartAlarmDao.observePrimaryAlarm().map { row ->
            row?.toDomain() ?: SmartAlarm()
        }

    suspend fun getPrimaryAlarm(): SmartAlarm =
        smartAlarmDao.getPrimaryAlarm()?.toDomain() ?: SmartAlarm()

    suspend fun savePrimaryAlarm(alarm: SmartAlarm) {
        smartAlarmDao.upsert(alarm.copy(id = 1).toEntity())
    }

    suspend fun recordCompletedSession(
        startedAtMs: Long,
        endedAtMs: Long,
        snoreCount: Int = 0,
        sleepNotes: List<String> = emptyList(),
        wakeUpMood: WakeUpMood = WakeUpMood.GOOD,
        stages: List<SleepStageSample> = emptyList(),
    ): SleepSession {
        val elapsedMinutes = ((endedAtMs - startedAtMs) / 60_000L).toInt().coerceAtLeast(15)
        val stageTimeline = stages.ifEmpty { generateCycleStages(elapsedMinutes) }
        val awakeMins = (elapsedMinutes * 0.05f).toInt().coerceAtLeast(5)
        val deepMins = (elapsedMinutes * 0.38f).toInt().coerceAtLeast(15)
        val lightMins = (elapsedMinutes - awakeMins - deepMins).coerceAtLeast(10)
        val computedScore = computeQualityScore(elapsedMinutes, awakeMins, deepMins, snoreCount)

        val recorded = SleepSession(
            id = UUID.randomUUID().toString(),
            startedAt = startedAtMs,
            endedAt = endedAtMs,
            durationMinutes = elapsedMinutes,
            sleepScore = computedScore,
            awakeMinutes = awakeMins,
            lightSleepMinutes = lightMins,
            deepSleepMinutes = deepMins,
            fellAsleepMinutes = 7,
            snoreMinutes = (snoreCount * 2).coerceAtMost(elapsedMinutes / 3),
            snoreCount = snoreCount,
            sleepNotes = sleepNotes,
            wakeUpMood = wakeUpMood,
            stages = stageTimeline,
            isDemoData = false,
        )
        sleepSessionDao.upsert(recorded.toEntity())
        return recorded
    }

    private fun computeQualityScore(
        totalMinutes: Int,
        awakeMinutes: Int,
        deepMinutes: Int,
        snoreCount: Int,
    ): Int {
        val durationComponent = (totalMinutes.coerceIn(180, 480) / 480f) * 55f
        val deepShare = (deepMinutes.toFloat() / totalMinutes.coerceAtLeast(1)).coerceIn(0f, 0.5f)
        val deepComponent = (deepShare / 0.35f).coerceAtMost(1f) * 30f
        val awakeDeduction = (awakeMinutes / 5).coerceAtMost(10)
        val snoreDeduction = (snoreCount / 4).coerceAtMost(8)
        return (durationComponent + deepComponent + 15f - awakeDeduction - snoreDeduction)
            .toInt()
            .coerceIn(45, 98)
    }

    private fun generateCycleStages(totalMinutes: Int): List<SleepStageSample> = buildList {
        val intervalMinutes = 15
        var minuteCursor = 0
        var sleepCycleIndex = 0
        while (minuteCursor <= totalMinutes) {
            val cycleOffset = minuteCursor % 90
            val stageType = when {
                minuteCursor == 0 || minuteCursor >= totalMinutes - 10 -> SleepStageType.AWAKE
                cycleOffset in 0..20 -> SleepStageType.LIGHT
                cycleOffset in 21..55 -> SleepStageType.DEEP
                cycleOffset in 56..75 -> SleepStageType.LIGHT
                sleepCycleIndex % 2 == 1 && cycleOffset > 80 -> SleepStageType.AWAKE
                else -> SleepStageType.LIGHT
            }
            add(SleepStageSample(minuteOffset = minuteCursor, stage = stageType))
            minuteCursor += intervalMinutes
            if (minuteCursor % 90 == 0) sleepCycleIndex++
        }
    }

    companion object {
        fun buildSampleSleepSession(nowMs: Long = System.currentTimeMillis()): SleepSession {
            val totalMin = 408
            val finishEpoch = nowMs - 3_600_000L
            val startEpoch = finishEpoch - totalMin * 60_000L
            val sampleStages = listOf(
                SleepStageSample(0, SleepStageType.AWAKE),
                SleepStageSample(15, SleepStageType.LIGHT),
                SleepStageSample(30, SleepStageType.DEEP),
                SleepStageSample(75, SleepStageType.DEEP),
                SleepStageSample(105, SleepStageType.LIGHT),
                SleepStageSample(135, SleepStageType.DEEP),
                SleepStageSample(180, SleepStageType.LIGHT),
                SleepStageSample(210, SleepStageType.AWAKE),
                SleepStageSample(225, SleepStageType.LIGHT),
                SleepStageSample(255, SleepStageType.DEEP),
                SleepStageSample(300, SleepStageType.DEEP),
                SleepStageSample(345, SleepStageType.LIGHT),
                SleepStageSample(390, SleepStageType.LIGHT),
                SleepStageSample(408, SleepStageType.AWAKE),
            )
            return SleepSession(
                id = "demo_sleep_report",
                startedAt = startEpoch,
                endedAt = finishEpoch,
                durationMinutes = totalMin,
                sleepScore = 88,
                awakeMinutes = 15,
                lightSleepMinutes = 219,
                deepSleepMinutes = 174,
                fellAsleepMinutes = 7,
                snoreMinutes = 0,
                snoreCount = 0,
                sleepNotes = listOf("reading", "meditation"),
                wakeUpMood = WakeUpMood.GOOD,
                stages = sampleStages,
                isDemoData = true,
            )
        }
    }
}
/**
 * Aggregated sleep quality and duration summary displayed on the Profile tab.
 */
@Immutable
data class SleepProfileStats(
    val nightsCount: Int = 0,
    val averageQualityScore: Int? = null,
    val averageDurationMinutes: Int = 0,
) {
    val formattedAverageDuration: String
        get() = "${averageDurationMinutes / 60}h ${averageDurationMinutes % 60}m"
}
