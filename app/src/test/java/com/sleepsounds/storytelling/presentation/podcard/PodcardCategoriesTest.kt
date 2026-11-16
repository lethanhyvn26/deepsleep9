package com.sleepsounds.storytelling.presentation.podcard

import com.sleepsounds.storytelling.data.remote.ContentCategory
import com.sleepsounds.storytelling.presentation.player.formatPlaybackTime
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests verifying podcast category filtering and playback duration formatting.
 */
class PodcardCategoriesTest {

    @Test
    fun resolvePodcastCategoriesExcludesEmptyCategoriesCaseInsensitively() {
        val definedCategories = listOf(
            ContentCategory(id = "sleep", label = "Sleep"),
            ContentCategory(id = "kids", label = "Kids"),
            ContentCategory(id = "empty", label = "Empty"),
        )
        val filtered = resolvePodcastCategories(
            catalogCategories = definedCategories,
            availableCategoryIds = listOf("SLEEP", "KIDS"),
        )
        assertEquals(listOf("sleep", "kids"), filtered.map { it.id })
    }

    @Test
    fun formatPlaybackTimeHandlesSubHourAndMultiHourDurations() {
        assertEquals("2:21:34", formatPlaybackTime(8_494_000L))
        assertEquals("47:36", formatPlaybackTime(2_856_000L))
        assertEquals("1:00:00", formatPlaybackTime(3_600_000L))
        assertEquals("0:45", formatPlaybackTime(45_000L))
        assertEquals("0:00", formatPlaybackTime(0L))
    }
}
