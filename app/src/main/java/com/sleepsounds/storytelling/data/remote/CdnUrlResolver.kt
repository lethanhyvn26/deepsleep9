package com.sleepsounds.storytelling.data.remote

/**
 * Resolves relative media paths against Firebase RTDB `base_url` or Remote Config Cloudflare R2 / Worker URLs.
 */
object CdnUrlResolver {

    fun resolve(
        pathOrUrl: String,
        rtdbBase: String,
        rcBase: String,
        useWorkerRoutes: Boolean,
    ): String {
        val rawPath = pathOrUrl.trim()
        if (rawPath.isEmpty()) return ""
        if (rawPath.startsWith("http://", ignoreCase = true) ||
            rawPath.startsWith("https://", ignoreCase = true)
        ) {
            return rawPath
        }
        val rtdbPrefix = normalizeBase(rtdbBase)
        if (rtdbPrefix.isNotEmpty()) {
            return rtdbPrefix + rawPath.trimStart('/')
        }
        val rcPrefix = normalizeBase(rcBase)
        if (rcPrefix.isEmpty()) return rawPath
        val workerSegment = if (useWorkerRoutes) "files/" else ""
        return rcPrefix + workerSegment + rawPath.trimStart('/')
    }

    fun normalizeBase(url: String): String {
        val candidate = url.trim()
        if (candidate.isEmpty()) return ""
        return if (candidate.endsWith("/")) candidate else "$candidate/"
    }
}
