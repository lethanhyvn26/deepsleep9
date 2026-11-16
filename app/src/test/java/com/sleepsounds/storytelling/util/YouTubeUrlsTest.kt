package com.sleepsounds.storytelling.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying YouTube video ID extraction across standard, mobile, shorts, and embed URLs.
 */
class YouTubeUrlsTest {

    @Test
    fun standardWatchUrlWithPlaylistParametersExtractsElevenCharVideoId() {
        val watchLink =
            "https://www.youtube.com/watch?v=NaQnFcDKFiM&list=RDMMNaQnFcDKFiM&start_radio=1"
        assertTrue(YouTubeUrls.isYouTubeUrl(watchLink))
        assertEquals("NaQnFcDKFiM", YouTubeUrls.videoIdOrNull(watchLink))
    }

    @Test
    fun queryStringWithVideoParameterSecondStillExtractsId() {
        val mobileLink = "https://m.youtube.com/watch?list=PLabc1234567&v=NaQnFcDKFiM"
        assertEquals("NaQnFcDKFiM", YouTubeUrls.videoIdOrNull(mobileLink))
    }

    @Test
    fun shortLinksShortsEmbedsAndYoutubeMusicUrlsExtractCleanId() {
        val expectedId = "abcdefghijk"
        assertEquals(expectedId, YouTubeUrls.videoIdOrNull("https://youtu.be/$expectedId?si=xyz"))
        assertEquals(expectedId, YouTubeUrls.videoIdOrNull("https://www.youtube.com/shorts/$expectedId"))
        assertEquals(expectedId, YouTubeUrls.videoIdOrNull("https://www.youtube.com/embed/$expectedId"))
        assertEquals(expectedId, YouTubeUrls.videoIdOrNull("https://music.youtube.com/watch?v=$expectedId"))
        assertEquals(expectedId, YouTubeUrls.videoIdOrNull("https://www.youtube-nocookie.com/embed/$expectedId"))
    }

    @Test
    fun playlistUrlWithoutVideoIdIsRecognizedAsYouTubeHostButReturnsNullId() {
        val playlistLink = "https://www.youtube.com/playlist?list=PLabc1234567"
        assertTrue(YouTubeUrls.isYouTubeUrl(playlistLink))
        assertNull(YouTubeUrls.videoIdOrNull(playlistLink))
    }

    @Test
    fun relativeCdnAudioPathIsNotTreatedAsYouTubeUrl() {
        val r2Path = "podcasts/pc_ocean_lullaby/audio.ogg"
        assertFalse(YouTubeUrls.isYouTubeUrl(r2Path))
        assertNull(YouTubeUrls.videoIdOrNull(r2Path))
    }
}
