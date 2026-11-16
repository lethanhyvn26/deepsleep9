package com.sleepsounds.storytelling.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying Firebase Realtime Database snapshot parsing in [ContentCatalogMapper].
 */
class ContentCatalogMapperTest {

    @Test
    fun mapperRetainsFirebaseInsertionOrderAcrossAllCollections() {
        val mappedCatalog = ContentCatalogMapper.map(
            baseUrl = "https://cdn.example/",
            mediaCategoryChildren = linkedMapOf(
                "sleep" to mapOf("label" to "Sleep", "enabled" to true),
                "animals" to mapOf("label" to "Animals", "enabled" to true),
            ),
            podcastCategoryChildren = linkedMapOf(
                "history" to mapOf("label" to "History", "enabled" to true),
                "Audiobooks" to mapOf("label" to "Audiobooks", "enabled" to true),
            ),
            soundChildren = linkedMapOf(
                "zebra" to mapOf("title" to "Zebra", "file" to "sounds/zebra.ogg", "enabled" to true),
                "alpha" to mapOf("title" to "Alpha", "file" to "sounds/alpha.ogg", "enabled" to true),
            ),
            sceneChildren = linkedMapOf(
                "sc_z" to mapOf(
                    "title" to "Zebra Scene",
                    "enabled" to true,
                    "layers" to linkedMapOf(
                        "zebra" to mapOf("volume" to 0.2),
                        "alpha" to mapOf("volume" to 0.9),
                    ),
                ),
                "sc_a" to mapOf("title" to "Alpha Scene", "enabled" to true),
            ),
            podcastChildren = linkedMapOf(
                "z_pod" to mapOf(
                    "title" to "Zebra Pod",
                    "file" to "podcasts/z.ogg",
                    "duration_seconds" to 10,
                    "enabled" to true,
                ),
                "a_pod" to mapOf(
                    "title" to "Alpha Pod",
                    "file" to "podcasts/a.ogg",
                    "duration_seconds" to 10,
                    "enabled" to true,
                ),
            ),
        )

        assertEquals(listOf("sleep", "animals"), mappedCatalog.mediaCategories.map { it.id })
        assertEquals(listOf("history", "Audiobooks"), mappedCatalog.podcastCategories.map { it.id })
        assertEquals(listOf("zebra", "alpha"), mappedCatalog.sounds.map { it.id })
        assertEquals(listOf("sc_z", "sc_a"), mappedCatalog.scenes.map { it.id })
        assertEquals(listOf("zebra", "alpha"), mappedCatalog.scenes.first().layers.map { it.soundId })
        assertEquals(listOf("z_pod", "a_pod"), mappedCatalog.podcasts.map { it.id })
    }

    @Test
    fun mapperParsesCommaSeparatedCategoriesSceneLayersAndPodcastChapters() {
        val mappedCatalog = ContentCatalogMapper.map(
            baseUrl = "https://cdn.example/",
            mediaCategoryChildren = mapOf(
                "nature" to mapOf("label" to "Nature", "emoji" to "🌿", "enabled" to true),
                "rain" to mapOf("label" to "Rain", "enabled" to true),
            ),
            podcastCategoryChildren = mapOf(
                "sleep" to mapOf("label" to "Sleep", "enabled" to true),
            ),
            soundChildren = mapOf(
                "light_rain" to mapOf(
                    "title" to "Light Rain",
                    "file" to "sounds/light_rain/audio.ogg",
                    "symbol" to "sounds/light_rain/icon.webp",
                    "categories" to "nature,rain",
                    "default_volume" to 0.7,
                    "is_premium" to false,
                    "unlock_day" to 0,
                    "enabled" to true,
                ),
            ),
            sceneChildren = mapOf(
                "sc_campfire" to mapOf(
                    "title" to "Campfire",
                    "cover" to "scenes/sc_campfire.webp",
                    "categories" to "nature,sleep",
                    "is_premium" to true,
                    "enabled" to true,
                    "layers" to mapOf(
                        "light_rain" to mapOf("volume" to 0.8),
                        "thunder" to mapOf("volume" to 0.3),
                    ),
                ),
            ),
            podcastChildren = mapOf(
                "pc_sleep" to mapOf(
                    "title" to "Sleep Stories",
                    "author" to "Ada",
                    "cover" to "podcasts/pc_sleep/cover.webp",
                    "categories" to "sleep",
                    "is_premium" to true,
                    "free_chapters" to 1,
                    "language" to "en",
                    "enabled" to true,
                    "chapters" to mapOf(
                        "ch01" to mapOf(
                            "id" to "pc_sleep_ch01",
                            "title" to "Part 2",
                            "index" to 1,
                            "duration_seconds" to 100,
                            "file" to "podcasts/pc_sleep/ch01.ogg",
                        ),
                        "ch00" to mapOf(
                            "id" to "pc_sleep_ch00",
                            "title" to "Part 1",
                            "index" to 0L,
                            "duration_seconds" to 90,
                            "file" to "podcasts/pc_sleep/ch00.ogg",
                        ),
                    ),
                ),
            ),
        )

        assertEquals("https://cdn.example/", mappedCatalog.baseUrl)
        assertEquals(listOf("nature", "rain"), mappedCatalog.mediaCategories.map { it.id })
        assertEquals(listOf("nature", "rain"), mappedCatalog.sounds.single().categories)
        assertEquals("sounds/light_rain/audio.ogg", mappedCatalog.sounds.single().file)
        assertEquals(0.8f, mappedCatalog.scenes.single().layers[0].volume, 0.001f)
        assertEquals(listOf("light_rain", "thunder"), mappedCatalog.scenes.single().layers.map { it.soundId })
        assertEquals(listOf("pc_sleep_ch00", "pc_sleep_ch01"), mappedCatalog.podcasts.single().chapters.map { it.id })
        assertTrue(mappedCatalog.podcasts.single().isPremium)
        assertEquals(1, mappedCatalog.podcasts.single().freeChapters)
    }

    @Test
    fun singleFilePodcastSynthesizesDefaultChapter() {
        val mappedCatalog = ContentCatalogMapper.map(
            baseUrl = "https://cdn.example/",
            mediaCategoryChildren = emptyMap(),
            podcastCategoryChildren = emptyMap(),
            soundChildren = emptyMap(),
            sceneChildren = emptyMap(),
            podcastChildren = mapOf(
                "pc_sleep" to mapOf(
                    "title" to "Sleep Stories for Restless Nights",
                    "cover" to "podcasts/pc_sleep/cover.webp",
                    "file" to "podcasts/pc_sleep/audio.ogg",
                    "duration_seconds" to 3600,
                    "categories" to "sleep",
                    "is_premium" to false,
                    "language" to "en",
                    "enabled" to true,
                ),
            ),
        )
        val item = mappedCatalog.podcasts.single()
        assertEquals("podcasts/pc_sleep/audio.ogg", item.file)
        assertEquals(0, item.durationSeconds)
        assertEquals(listOf("pc_sleep_audio"), item.chapters.map { it.id })
        assertEquals("podcasts/pc_sleep/audio.ogg", item.chapters.single().file)
    }

    @Test
    fun youtubeVideoUrlInPodcastFileIsPreservedInSynthesizedChapter() {
        val youtubeUrl =
            "https://www.youtube.com/watch?v=NaQnFcDKFiM&list=RDMMNaQnFcDKFiM&start_radio=1"
        val mappedCatalog = ContentCatalogMapper.map(
            baseUrl = "https://cdn.example/",
            mediaCategoryChildren = emptyMap(),
            podcastCategoryChildren = emptyMap(),
            soundChildren = emptyMap(),
            sceneChildren = emptyMap(),
            podcastChildren = mapOf(
                "pc_night" to mapOf(
                    "title" to "Night Drive",
                    "cover" to "podcasts/pc_night/cover.webp",
                    "file" to youtubeUrl,
                    "duration_seconds" to 200,
                    "categories" to "sleep",
                    "is_premium" to true,
                    "enabled" to true,
                ),
            ),
        )
        val item = mappedCatalog.podcasts.single()
        assertEquals(youtubeUrl, item.file)
        assertEquals(youtubeUrl, item.chapters.single().file)
        assertTrue(item.isPremium)
    }

    @Test
    fun disabledCatalogNodesAreRetainedForImporterFiltering() {
        val mappedCatalog = ContentCatalogMapper.map(
            baseUrl = "https://cdn.example",
            mediaCategoryChildren = mapOf(
                "hidden" to mapOf("label" to "Hidden", "enabled" to false),
            ),
            podcastCategoryChildren = emptyMap(),
            soundChildren = mapOf(
                "off" to mapOf("title" to "Off", "file" to "sounds/off/audio.ogg", "enabled" to false),
                "on" to mapOf("title" to "On", "file" to "sounds/on/audio.ogg", "enabled" to true),
            ),
            sceneChildren = mapOf(
                "sc_off" to mapOf("title" to "Hidden scene", "enabled" to false),
            ),
            podcastChildren = emptyMap(),
        )
        assertEquals("https://cdn.example/", mappedCatalog.baseUrl)
        assertFalse(mappedCatalog.mediaCategories.single().enabled)
        assertEquals(listOf("on"), mappedCatalog.enabledSounds.map { it.id })
        assertTrue(mappedCatalog.enabledScenes.isEmpty())
    }

    @Test
    fun parseCategoryIdsHandlesCsvStringsAndBooleanMaps() {
        assertEquals(listOf("nature", "rain"), parseCategoryIds("nature,rain"))
        assertEquals(listOf("sleep"), parseCategoryIds(mapOf("sleep" to true, "off" to false)))
    }
}
