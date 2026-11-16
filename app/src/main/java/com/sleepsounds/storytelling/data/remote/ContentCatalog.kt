package com.sleepsounds.storytelling.data.remote

data class ContentCatalog(
    val baseUrl: String = "",
    val minAppVersion: Int = 0,
    val mediaCategories: List<ContentCategory> = emptyList(),
    val podcastCategories: List<ContentCategory> = emptyList(),
    val sounds: List<ContentSound> = emptyList(),
    val scenes: List<ContentScene> = emptyList(),
    val podcasts: List<ContentPodcast> = emptyList(),
) {
    val enabledSounds: List<ContentSound>
        get() = sounds.filter { it.enabled }

    val enabledScenes: List<ContentScene>
        get() = scenes.filter { it.enabled }

    val enabledPodcasts: List<ContentPodcast>
        get() = podcasts.filter { it.enabled }
}
data class ContentPodcast(
    val id: String,
    val title: String,
    val author: String?,
    val narrator: String?,
    val summary: String?,
    val cover: String,
    val file: String = "",
    val durationSeconds: Int = 0,
    val categories: List<String>,
    val isPremium: Boolean,
    val freeChapters: Int,
    val language: String,
    val enabled: Boolean,
    val backgroundSoundId: String?,
    val chapters: List<ContentChapter>,
)
data class ContentChapter(
    val id: String,
    val title: String,
    val index: Int,
    val durationSeconds: Int,
    val file: String,
)
data class ContentScene(
    val id: String,
    val title: String,
    val cover: String,
    val categories: List<String>,
    val isPremium: Boolean,
    val enabled: Boolean,
    val layers: List<ContentSceneLayer>,
)
data class ContentSceneLayer(
    val soundId: String,
    val volume: Float,
)
data class ContentSound(
    val id: String,
    val title: String,
    val file: String,
    val symbol: String,
    val categories: List<String>,
    val defaultVolume: Float,
    val isPremium: Boolean,
    val unlockDay: Int,
    val enabled: Boolean,
)
/**
 * Remote catalog DTOs parsed from the Firebase Realtime Database `/content` tree.
 */
data class ContentCategory(
    val id: String,
    val label: String,
    val emoji: String? = null,
    val enabled: Boolean = true,
)
