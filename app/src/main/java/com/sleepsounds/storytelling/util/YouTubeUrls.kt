package com.sleepsounds.storytelling.util

import java.net.URI
import java.net.URLDecoder

/**
 * Extracts 11-character YouTube video IDs from standard, mobile, music, and short links.
 */
object YouTubeUrls {
    private val videoIdRegex = Regex("^[A-Za-z0-9_-]{11}$")
    private val allowedHosts = setOf(
        "youtu.be",
        "youtube.com",
        "m.youtube.com",
        "music.youtube.com",
        "youtube-nocookie.com",
    )

    fun videoIdOrNull(raw: String?): String? {
        val parsedUri = parseUriOrNull(raw) ?: return null
        val normalizedHost = extractAllowedHost(parsedUri) ?: return null
        val uriPath = parsedUri.path.orEmpty()

        val candidateId = if (normalizedHost == "youtu.be") {
            uriPath.substringAfter('/').substringBefore('/')
        } else {
            when {
                uriPath == "/watch" || uriPath.startsWith("/watch/") -> readQueryParam(parsedUri, "v")
                uriPath.startsWith("/embed/") -> uriPath.removePrefix("/embed/").substringBefore('/')
                uriPath.startsWith("/shorts/") -> uriPath.removePrefix("/shorts/").substringBefore('/')
                uriPath.startsWith("/live/") -> uriPath.removePrefix("/live/").substringBefore('/')
                uriPath.startsWith("/v/") -> uriPath.removePrefix("/v/").substringBefore('/')
                else -> null
            }
        }
        return candidateId?.takeIf { videoIdRegex.matches(it) }
    }

    fun isYouTubeUrl(raw: String?): Boolean =
        parseUriOrNull(raw)?.let { extractAllowedHost(it) != null } == true

    private fun parseUriOrNull(raw: String?): URI? {
        val trimmed = raw?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        return runCatching { URI(trimmed) }.getOrNull()
    }

    private fun extractAllowedHost(uri: URI): String? {
        val scheme = uri.scheme?.lowercase() ?: return null
        if (scheme != "http" && scheme != "https") return null
        val host = uri.host?.lowercase()?.removePrefix("www.") ?: return null
        return host.takeIf { it in allowedHosts }
    }

    private fun readQueryParam(uri: URI, paramName: String): String? {
        val rawQuery = uri.rawQuery ?: return null
        return rawQuery.split('&').firstNotNullOfOrNull { pair ->
            val key = pair.substringBefore('=')
            if (key != paramName) return@firstNotNullOfOrNull null
            val encodedVal = pair.substringAfter('=', "")
            runCatching { URLDecoder.decode(encodedVal, Charsets.UTF_8.name()) }
                .getOrDefault(encodedVal)
        }
    }
}
