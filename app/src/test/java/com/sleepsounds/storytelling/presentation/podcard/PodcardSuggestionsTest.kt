package com.sleepsounds.storytelling.presentation.podcard

import com.sleepsounds.storytelling.domain.model.Podcast
import com.sleepsounds.storytelling.domain.model.PodcastChapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying related-story suggestion ordering in [rankSuggestions].
 */
class PodcardSuggestionsTest {

    private val mixedCatalog = listOf(
        buildPodcastStub("a", OGG_AUDIO_PATH),
        buildPodcastStub("b", YT_WATCH_URL),
        buildPodcastStub("c", OGG_AUDIO_PATH),
        buildPodcastStub("d", YT_SHORT_LINK),
        buildPodcastStub("e", OGG_AUDIO_PATH),
        buildPodcastStub("f", YT_SHORTS_PATH),
    )

    @Test
    fun youtubePodcastSuggestsRemainingYoutubeItemsInCircularOrder() {
        val afterD = rankSuggestions(mixedCatalog[3], mixedCatalog)
        assertEquals(listOf("f", "b"), afterD.map { it.id })

        val afterF = rankSuggestions(mixedCatalog[5], mixedCatalog)
        assertEquals(listOf("b", "d"), afterF.map { it.id })
    }

    @Test
    fun audioPodcastSuggestsRemainingAudioItemsInCircularOrder() {
        val afterA = rankSuggestions(mixedCatalog[0], mixedCatalog)
        assertEquals(listOf("c", "e"), afterA.map { it.id })

        val afterE = rankSuggestions(mixedCatalog[4], mixedCatalog)
        assertEquals(listOf("a", "c"), afterE.map { it.id })
    }

    @Test
    fun singleItemOfMatchingMediaTypeProducesEmptySuggestions() {
        val singleVideoCatalog = listOf(
            buildPodcastStub("a", OGG_AUDIO_PATH),
            buildPodcastStub("b", YT_WATCH_URL),
            buildPodcastStub("c", OGG_AUDIO_PATH),
        )
        assertTrue(rankSuggestions(singleVideoCatalog[1], singleVideoCatalog).isEmpty())
    }

    @Test
    fun unindexedPodcastReturnsAllMatchingTypeItemsInCatalogOrder() {
        val externalVideo = buildPodcastStub("unlisted", YT_WATCH_URL)
        assertEquals(
            listOf("b", "d", "f"),
            rankSuggestions(externalVideo, mixedCatalog).map { it.id },
        )
    }

    @Test
    fun nullCurrentPodcastYieldsEmptyList() {
        assertTrue(rankSuggestions(null, mixedCatalog).isEmpty())
    }

    @Test
    fun duplicateIdsInCatalogAreDeduplicatedBeforeRanking() {
        val doubled = mixedCatalog + mixedCatalog
        assertEquals(listOf("c", "e"), rankSuggestions(mixedCatalog[0], doubled).map { it.id })
    }

    private fun buildPodcastStub(id: String, streamPath: String): Podcast = Podcast(
        id = id,
        title = id,
        categories = emptyList(),
        chapters = listOf(
            PodcastChapter(
                id = "${id}_ch0",
                podcastId = id,
                title = id,
                chapterIndex = 0,
                audioPath = streamPath,
            ),
        ),
    )

    private companion object {
        const val OGG_AUDIO_PATH = "podcasts/sample/audio.ogg"
        const val YT_WATCH_URL = "https://www.youtube.com/watch?v=bbbbbbbbbbb"
        const val YT_SHORT_LINK = "https://youtu.be/ddddddddddd"
        const val YT_SHORTS_PATH = "https://www.youtube.com/shorts/fffffffffff"
    }
}
