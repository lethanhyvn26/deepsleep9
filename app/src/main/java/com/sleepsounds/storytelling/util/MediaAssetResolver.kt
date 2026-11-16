package com.sleepsounds.storytelling.util

import com.sleepsounds.storytelling.data.remote.ManifestApi
import com.sleepsounds.storytelling.domain.model.ContentType
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Resolves cover art, sound symbols, audio relative paths, and bundled wallpapers to loadable URIs.
 */
@Singleton
class MediaAssetResolver @Inject constructor(
    private val manifestApi: ManifestApi,
) {
    fun audioRelativePath(trackId: String, dbPath: String?): String =
        dbPath?.takeIf { it.isNotBlank() } ?: "sounds/$trackId/audio.ogg"

    fun resolveImageUri(
        trackId: String,
        contentType: ContentType,
        dbPath: String?,
        categories: List<String>,
    ): String? {
        val candidatePaths = buildCoverCandidates(trackId, contentType, dbPath)
        resolveFirstRemoteUrl(candidatePaths)?.let { return it }

        return buildFallbackCandidates(contentType, categories)
            .firstNotNullOfOrNull(::resolveBundledOrRemoteUri)
    }

    fun resolveImagePath(
        trackId: String,
        contentType: ContentType,
        dbPath: String?,
        categories: List<String>,
    ): String? = resolveImageUri(trackId, contentType, dbPath, categories)

    fun resolveSymbolUri(dbPath: String?): String? {
        if (dbPath.isNullOrBlank()) return null
        val inferredSoundId = dbPath.substringAfterLast('/').substringBeforeLast('.')
        return resolveSoundSymbolUri(inferredSoundId, dbPath)
    }

    fun resolveSoundSymbolUri(soundId: String, dbPath: String?): String? =
        resolveFirstRemoteUrl(buildSymbolCandidates(soundId, dbPath))

    fun bundledUri(relativePath: String): String =
        BundledAssets.bundledUri(relativePath)

    fun resolveRemoteUri(relativePath: String): String? =
        resolveFirstRemoteUrl(listOf(relativePath))

    fun iconUriForCategory(category: String): String? =
        CategoryIcons.forCategory(category)?.let(::resolveBundledOrRemoteUri)

    fun iconUriForTrack(categories: List<String>): String? =
        categories.firstNotNullOfOrNull { cat ->
            CategoryIcons.forCategory(cat)?.let(::resolveBundledOrRemoteUri)
        }

    fun toCoilUri(pathOrUri: String?): String? {
        if (pathOrUri.isNullOrBlank()) return null
        return when {
            pathOrUri.startsWith("http://") ||
                pathOrUri.startsWith("https://") ||
                pathOrUri.startsWith("file://") -> pathOrUri
            BundledAssets.isBundled(pathOrUri) -> bundledUri(pathOrUri)
            else -> resolveRemoteUri(pathOrUri)
        }
    }

    fun resolveMixBackgroundUri(soundIds: List<String>, coverPaths: List<String>): String? {
        if (coverPaths.isEmpty()) return null
        val stableHash = abs(soundIds.sorted().joinToString(",").hashCode())
        val selectedPath = coverPaths[stableHash % coverPaths.size]
        return if (
            selectedPath.startsWith("http://") ||
            selectedPath.startsWith("https://") ||
            selectedPath.startsWith("file://")
        ) {
            selectedPath
        } else {
            resolveCoverUri(selectedPath)
        }
    }

    fun resolveCoverUri(path: String): String? {
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("file://")) {
            return path
        }
        return resolveFirstRemoteUrl(buildCoverCandidates("", ContentType.SLEEP_PRESET, path))
    }

    fun resolveBackgroundUri(backgroundId: String = AppBackgrounds.DEFAULT_ID): String =
        AppBackgrounds.uriFor(backgroundId)

    private fun resolveBundledOrRemoteUri(path: String): String? =
        if (BundledAssets.isBundled(path)) bundledUri(path) else resolveFirstRemoteUrl(listOf(path))

    private fun resolveFirstRemoteUrl(candidates: List<String>): String? {
        val rawPath = candidates.firstOrNull { it.isNotBlank() } ?: return null
        return if (rawPath.startsWith("http://") || rawPath.startsWith("https://")) {
            rawPath
        } else {
            manifestApi.getRemoteFileUrl(rawPath)
        }
    }

    private fun defaultSymbolPath(soundId: String): String = "sounds/$soundId/icon.webp"

    private fun canonicalizeSymbolPath(path: String): String =
        if (path.startsWith("mix/symbols/")) {
            path.replaceFirst("mix/symbols/", "images/symbols/")
        } else {
            path
        }

    private fun buildSymbolCandidates(soundId: String, dbPath: String?): List<String> = buildList {
        dbPath?.takeIf { it.isNotBlank() }?.let { add(canonicalizeSymbolPath(it)) }
        add(defaultSymbolPath(soundId))
        add("images/symbols/$soundId.webp")
        add("mix/symbols/$soundId.webp")
    }

    private fun buildCoverCandidates(
        trackId: String,
        contentType: ContentType,
        dbPath: String?,
    ): List<String> = buildList {
        dbPath?.takeIf { it.isNotBlank() }?.let(::add)
        when (contentType) {
            ContentType.SLEEP_PRESET -> {
                add("scenes/$trackId.webp")
                add("scenes/$trackId/cover.webp")
                add("images/covers/$trackId.webp")
                add("images/covers/$trackId.jpg")
            }
            ContentType.SOUND -> {
                add(defaultSymbolPath(trackId))
                add("images/symbols/$trackId.webp")
                add("mix/symbols/$trackId.webp")
            }
            ContentType.PODCAST -> {
                add("podcasts/$trackId/cover.webp")
                add("podcast/$trackId.webp")
                add("images/podcast/$trackId.webp")
                add("images/podcast/$trackId.jpg")
            }
        }
    }

    private fun buildFallbackCandidates(
        contentType: ContentType,
        categories: List<String>,
    ): List<String> = buildList {
        CategoryIcons.forCategory(categories.firstOrNull().orEmpty())?.let(::add)
        if (contentType == ContentType.SLEEP_PRESET || contentType == ContentType.PODCAST) {
            add("images/backgrounds/bg_free_forest.jpg")
        }
    }
}
