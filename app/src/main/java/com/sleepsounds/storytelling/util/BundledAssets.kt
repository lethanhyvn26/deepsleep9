package com.sleepsounds.storytelling.util

/**
 * Resolves APK-bundled asset paths (`file:///android_asset/...`) for offline backgrounds and icons.
 */
object BundledAssets {
    private val bundledPrefixes = listOf(
        "images/backgrounds/",
        "images/icons/",
        "images/notification/",
    )

    fun isBundled(relativePath: String): Boolean =
        bundledPrefixes.any { prefix -> relativePath.startsWith(prefix) }

    fun bundledUri(relativePath: String): String {
        val clean = relativePath.trimStart('/')
        if (!isBundled(clean)) {
            R2Log.w("Requested asset path outside standard bundled prefixes: $clean")
        }
        return "file:///android_asset/$clean"
    }
}
