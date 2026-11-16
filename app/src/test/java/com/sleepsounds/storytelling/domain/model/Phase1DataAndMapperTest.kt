package com.sleepsounds.storytelling.domain.model

import com.sleepsounds.storytelling.data.local.DreamInterpretationCatalog
import com.sleepsounds.storytelling.data.local.entity.PlaybackHistoryEntity
import com.sleepsounds.storytelling.data.repository.SleepTrackerRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying offline dream dictionary lookups, sleep session stage calculations,
 * smart alarm entity mapping, and playback history domain conversion.
 */
class Phase1DataAndMapperTest {

    @Test
    fun offlineDreamCatalogContainsValidSymbolsAndSearchMatches() {
        val allSymbols = DreamInterpretationCatalog.SYMBOLS
        assertTrue(allSymbols.size >= 10)

        val fireEntry = DreamInterpretationCatalog.findById("fire")
        assertNotNull(fireEntry)
        assertEquals("campfire_glow", fireEntry?.recommendedSoundId)
        assertEquals("sc_campfire", fireEntry?.recommendedSceneId)

        val matchedTeeth = DreamInterpretationCatalog.search("teeth")
        assertEquals(1, matchedTeeth.size)
        assertEquals("losing_teeth", matchedTeeth.first().id)
    }

    @Test
    fun synthesizedDemoSleepSessionHasBalancedStagePercentages() {
        val sampleSession = SleepTrackerRepository.buildSampleSleepSession(nowMs = 1_760_000_000_000L)
        assertTrue(sampleSession.isDemoData)
        assertEquals(408, sampleSession.durationMinutes)
        assertEquals(15, sampleSession.awakeMinutes)
        assertEquals(219, sampleSession.lightSleepMinutes)
        assertEquals(174, sampleSession.deepSleepMinutes)
        assertEquals(
            100,
            sampleSession.awakePercent + sampleSession.lightSleepPercent + sampleSession.deepSleepPercent,
        )
    }

    @Test
    fun sleepSessionRoundTripThroughRoomEntityRetainsAllFields() {
        val sourceSession = SleepTrackerRepository.buildSampleSleepSession(nowMs = 1_760_000_000_000L)
            .copy(id = "session_1", isDemoData = false)
        val roundTripped = sourceSession.toEntity().toDomain()

        assertEquals(sourceSession.id, roundTripped.id)
        assertEquals(sourceSession.durationMinutes, roundTripped.durationMinutes)
        assertEquals(sourceSession.sleepScore, roundTripped.sleepScore)
        assertEquals(sourceSession.sleepNotes, roundTripped.sleepNotes)
        assertEquals(sourceSession.wakeUpMood, roundTripped.wakeUpMood)
        assertEquals(sourceSession.stages, roundTripped.stages)
    }

    @Test
    fun smartAlarmFormatsTwoDigitTimeAndRoundTripsCleanly() {
        val originalAlarm = SmartAlarm(
            id = 1,
            hour = 8,
            minute = 30,
            enabled = true,
            smartWindowMinutes = 20,
        )
        assertEquals("08:30", originalAlarm.formattedTime)
        assertEquals(originalAlarm, originalAlarm.toEntity().toDomain())
    }

    @Test
    fun playbackHistoryEntityConvertsToDomainModelAccurately() {
        val historyRow = PlaybackHistoryEntity(
            itemKey = "SOUND:seagull",
            itemId = "seagull",
            itemType = "SOUND",
            title = "Seagull",
            subtitle = "White Noise",
            imagePath = "sounds/seagull/icon.webp",
            durationSeconds = 0,
            isPremium = 0,
            mixLayersJson = null,
            playedAt = 123_456_789L,
        )
        val model = historyRow.toDomain()
        assertEquals(HistoryItemType.SOUND, model.itemType)
        assertEquals("seagull", model.itemId)
        assertEquals("Seagull", model.title)
    }
}
