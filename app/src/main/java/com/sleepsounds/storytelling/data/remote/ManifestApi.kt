package com.sleepsounds.storytelling.data.remote

import com.sleepsounds.storytelling.data.sync.ContentManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Performs HTTP downloads of catalog manifests, JSON descriptors, and binary audio assets from Cloudflare R2.
 */
@Singleton
class ManifestApi @Inject constructor(
    private val contentEndpoints: ContentEndpoints,
) {
    suspend fun fetchManifest(): ContentManifest = withContext(Dispatchers.IO) {
        val rawBody = fetchUtf8Text(contentEndpoints.manifestUrl())
        decodeManifestJson(rawBody)
    }

    suspend fun fetchJsonText(resourcePath: String): String = withContext(Dispatchers.IO) {
        fetchUtf8Text(contentEndpoints.jsonUrl(resourcePath))
    }

    fun getRemoteFileUrl(relativePath: String): String =
        contentEndpoints.fileUrl(relativePath)

    private fun decodeManifestJson(jsonBody: String): ContentManifest {
        val root = JSONObject(jsonBody)
        val resourcesNode = root.getJSONObject("resources")
        val hashesNode = root.optJSONObject("hashes") ?: JSONObject()

        val resourceMap = buildMap {
            resourcesNode.keys().forEach { key -> put(key, resourcesNode.getString(key)) }
        }
        val hashMap = buildMap {
            hashesNode.keys().forEach { key -> put(key, hashesNode.getString(key)) }
        }
        return ContentManifest(
            version = root.getInt("version"),
            minAppVersion = root.optInt("minAppVersion", 1),
            updatedAt = root.optString("updatedAt", ""),
            resources = resourceMap,
            hashes = hashMap,
        )
    }

    suspend fun downloadFile(relativePath: String, destination: File) =
        withContext(Dispatchers.IO) {
            val remoteUrl = contentEndpoints.fileUrl(relativePath)
            streamUrlToFileAtomically(remoteUrl, destination)
        }

    private fun fetchUtf8Text(url: String): String {
        val conn = createHttpConnection(url)
        try {
            val status = conn.responseCode
            if (status !in 200..299) {
                error("HTTP $status while requesting $url")
            }
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private fun streamUrlToFileAtomically(url: String, destination: File) {
        destination.parentFile?.mkdirs()
        val stagingFile = File(destination.parentFile, "${destination.name}.tmp")
        if (stagingFile.exists()) {
            stagingFile.delete()
        }
        val conn = createHttpConnection(url)
        try {
            val status = conn.responseCode
            if (status !in 200..299) {
                error("HTTP $status while downloading $url")
            }
            val declaredBytes = conn.contentLengthLong
            conn.inputStream.use { input ->
                stagingFile.outputStream().use { output -> input.copyTo(output) }
            }
            val writtenBytes = stagingFile.length()
            if (!stagingFile.exists() || writtenBytes <= MIN_AUDIO_PAYLOAD_BYTES) {
                error("Downloaded asset is empty or truncated ($writtenBytes bytes): $url")
            }
            if (declaredBytes > 0L && writtenBytes != declaredBytes) {
                error("Partial asset download ($writtenBytes / $declaredBytes bytes): $url")
            }
            if (destination.exists() && !destination.delete()) {
                error("Unable to overwrite existing file: ${destination.absolutePath}")
            }
            if (!stagingFile.renameTo(destination)) {
                stagingFile.copyTo(destination, overwrite = true)
                stagingFile.delete()
            }
        } finally {
            if (stagingFile.exists()) {
                stagingFile.delete()
            }
            conn.disconnect()
        }
    }

    private fun createHttpConnection(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", USER_AGENT)
        }

    companion object {
        private const val MIN_AUDIO_PAYLOAD_BYTES = 64L
        private const val CONNECT_TIMEOUT_MS = 30_000
        private const val READ_TIMEOUT_MS = 60_000
        private const val USER_AGENT = "DeepSleep/3.1"
    }
}
