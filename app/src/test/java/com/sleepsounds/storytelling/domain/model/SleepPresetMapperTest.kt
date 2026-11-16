package com.sleepsounds.storytelling.domain.model

import com.sleepsounds.storytelling.data.local.entity.SleepPresetEntity
import com.sleepsounds.storytelling.data.local.entity.SleepPresetItemEntity
import com.sleepsounds.storytelling.data.local.entity.SoundEntity
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests verifying category JSON parsing and [SleepPreset] to [AudioTrack] mapping.
 */
class SleepPresetMapperTest {

    @Test
    fun parseCategoriesExtractsTrimmedTokensFromJsonArrayString() {
        val categories = parseCategories("[\"NIGHT\", \"WATER\", \"RELAX\"]")
        assertEquals(listOf("NIGHT", "WATER", "RELAX"), categories)
    }

    @Test
    fun convertingSleepPresetToAudioTrackPreservesLayerCountAndType() {
        val soundMap = mapOf(
            "fire" to buildSoundEntity("fire", "Fire"),
            "crickets" to buildSoundEntity("crickets", "Crickets"),
            "rain_on_roof" to buildSoundEntity("rain_on_roof", "Rain On Roof"),
        )
        val layerRows = listOf(
            SleepPresetItemEntity("sc_test", "fire", 0.8f),
            SleepPresetItemEntity("sc_test", "crickets", 0.5f),
            SleepPresetItemEntity("sc_test", "rain_on_roof", 0.4f),
        )
        val domainPreset = SleepPresetEntity(
            id = "sc_test",
            name = "Test Preset",
            categories = "[\"sleep\"]",
            isFavorite = 0,
            isPremium = 0,
        ).toSleepPreset(items = layerRows, soundsById = soundMap)

        val audioTrack = domainPreset.toAudioTrack()
        assertEquals(ContentType.SLEEP_PRESET, audioTrack.type)
        assertEquals(3, audioTrack.mixLayerCount)
        assertEquals("sc_test", audioTrack.id)
    }

    private fun buildSoundEntity(soundId: String, displayName: String): SoundEntity = SoundEntity(
        id = soundId,
        name = displayName,
        volume = 0.7f,
        categories = "[]",
        isFavorite = 0,
        symbolPath = "images/symbols/$soundId.webp",
        audioPath = "mix/$soundId.ogg",
        unlockDay = 0,
        isPremium = 0,
    )
}
