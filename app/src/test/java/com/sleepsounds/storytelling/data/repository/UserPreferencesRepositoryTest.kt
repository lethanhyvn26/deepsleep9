package com.sleepsounds.storytelling.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests verifying legacy sound ID mapping to preset IDs in [UserPreferencesRepository].
 */
class UserPreferencesRepositoryTest {

    @Test
    fun legacySoundIdentifiersAreMappedToScenePresets() {
        val rainMapped = UserPreferencesRepository.legacyMidnightPresetId("light_rain")
        val noiseMapped = UserPreferencesRepository.legacyMidnightPresetId("white_noise")
        val fireMapped = UserPreferencesRepository.legacyMidnightPresetId("fire")

        assertEquals("sc_rainy_day_comfort", rainMapped)
        assertEquals(UserPreferencesRepository.DEFAULT_MIDNIGHT_PRESET_ID, noiseMapped)
        assertEquals("sc_cozy_cabin_fireplace", fireMapped)
    }

    @Test
    fun modernScenePresetIdentifierPassesThroughUnmodified() {
        val sceneId = "sc_space_travel"
        assertEquals(sceneId, UserPreferencesRepository.legacyMidnightPresetId(sceneId))
    }
}
