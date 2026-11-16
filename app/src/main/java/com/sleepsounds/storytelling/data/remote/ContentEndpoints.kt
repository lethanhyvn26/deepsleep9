package com.sleepsounds.storytelling.data.remote

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds Cloudflare R2 or Worker URLs for manifests, JSON resources, and media files.
 */
@Singleton
class ContentEndpoints @Inject constructor(
    private val remoteConfigRepository: RemoteConfigRepository,
    private val contentCatalogRepository: ContentCatalogRepository,
) {
    private fun activeRtdbBaseUrl(): String {
        val candidate = contentCatalogRepository.baseUrl.value.trim()
        if (candidate.isEmpty()) return ""
        return if (candidate.endsWith("/")) candidate else "$candidate/"
    }

    private fun effectiveBaseUrl(): String {
        val fromRtdb = activeRtdbBaseUrl()
        return if (fromRtdb.isNotEmpty()) fromRtdb else remoteConfigRepository.getContentBaseUrl()
    }

    fun manifestUrl(): String {
        val base = effectiveBaseUrl()
        val workerRouting = remoteConfigRepository.useWorkerRoutes() && activeRtdbBaseUrl().isEmpty()
        return if (workerRouting) "${base}manifest" else "${base}manifest.json"
    }

    fun jsonUrl(resourcePath: String): String {
        val cleanPath = resourcePath.trimStart('/')
        val base = effectiveBaseUrl()
        val workerRouting = remoteConfigRepository.useWorkerRoutes() && activeRtdbBaseUrl().isEmpty()
        return if (workerRouting) {
            "${base}json/${cleanPath.removePrefix("json/")}"
        } else {
            "$base$cleanPath"
        }
    }

    fun fileUrl(relativePath: String): String =
        CdnUrlResolver.resolve(
            pathOrUrl = relativePath,
            rtdbBase = activeRtdbBaseUrl(),
            rcBase = remoteConfigRepository.getContentBaseUrl(),
            useWorkerRoutes = remoteConfigRepository.useWorkerRoutes(),
        )

    fun syncUrl(sinceVersion: Int): String =
        "${effectiveBaseUrl()}sync?since=$sinceVersion"
}
