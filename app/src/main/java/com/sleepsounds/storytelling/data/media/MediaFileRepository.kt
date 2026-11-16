package com.sleepsounds.storytelling.data.media

import android.content.Context
import com.sleepsounds.storytelling.data.remote.ManifestApi
import com.sleepsounds.storytelling.util.R2Log
import com.sleepsounds.storytelling.util.YouTubeUrls
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinates deduplicated background downloads of audio tracks and chapters from Cloudflare R2.
 */
@Singleton
class MediaFileRepository @Inject constructor(
    private val localFileStore: LocalFileStore,
    private val manifestApi: ManifestApi,
) {
    private val perPathLocks = mutableMapOf<String, Mutex>()
    private val registryMutex = Mutex()

    fun getRemoteUrl(relativePath: String): String =
        manifestApi.getRemoteFileUrl(relativePath)

    fun getLocalUri(relativePath: String): String? =
        localFileStore.localFileUri(relativePath)

    fun isLocal(relativePath: String): Boolean =
        localFileStore.hasLocalFile(relativePath)

    suspend fun ensureDownloaded(
        relativePath: String,
        expectedEtag: String? = null,
    ): Result<File> = withContext(Dispatchers.IO) {
        if (YouTubeUrls.isYouTubeUrl(relativePath)) {
            return@withContext Result.failure(
                IllegalArgumentException("YouTube streams are played directly and not cached as files"),
            )
        }
        val cleanKey = relativePath.trimStart('/')
        val targetFile = localFileStore.localFile(cleanKey)

        if (localFileStore.hasLocalFile(cleanKey)) {
            if (expectedEtag.isNullOrBlank()) {
                return@withContext Result.success(targetFile)
            }
            val computedSha = localFileStore.sha256(targetFile)
            val matchesHash = computedSha == expectedEtag ||
                expectedEtag.removePrefix("sha256:") == computedSha?.removePrefix("sha256:")
            if (matchesHash) {
                return@withContext Result.success(targetFile)
            }
            targetFile.delete()
        } else if (targetFile.exists()) {
            targetFile.delete()
        }

        val pathLock = registryMutex.withLock {
            perPathLocks.getOrPut(cleanKey) { Mutex() }
        }
        pathLock.withLock {
            if (localFileStore.hasLocalFile(cleanKey) && expectedEtag.isNullOrBlank()) {
                return@withLock Result.success(targetFile)
            }
            runCatching {
                manifestApi.downloadFile(cleanKey, targetFile)
                targetFile
            }
        }
    }

    suspend fun ensureSoundDownloaded(
        soundId: String,
        audioPath: String,
        etag: String? = null,
    ): Result<File> = ensureDownloaded(audioPath, etag)

    suspend fun ensureChapterDownloaded(
        audioPath: String,
        etag: String? = null,
    ): Result<File> = ensureDownloaded(audioPath, etag)

    fun deleteLocal(relativePath: String) {
        if (YouTubeUrls.isYouTubeUrl(relativePath)) return
        localFileStore.deleteLocalFile(relativePath)
    }

    fun downloadedSoundCount(): Int {
        val legacyMixFolder = localFileStore.localFile("mix")
        val legacyCount = legacyMixFolder.listFiles()?.count {
            it.isFile && !it.name.endsWith(".tmp") && it.length() > 64L
        } ?: 0

        val soundsFolder = localFileStore.localFile("sounds")
        val supportedExts = setOf("ogg", "mp3", "m4a", "aac")
        val currentCount = if (soundsFolder.exists()) {
            soundsFolder.walkTopDown().count { candidate ->
                candidate.isFile &&
                    !candidate.name.endsWith(".tmp") &&
                    candidate.length() > 64L &&
                    candidate.extension.lowercase() in supportedExts
            }
        } else {
            0
        }
        return legacyCount + currentCount
    }
}
/**
 * Manages on-device audio storage under `filesDir/media` and verifies SHA-256 integrity.
 */
@Singleton
class LocalFileStore @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    private val rootDirectory: File
        get() = File(appContext.filesDir, "media").apply { mkdirs() }

    fun localFile(relativePath: String): File =
        File(rootDirectory, relativePath.trimStart('/'))

    fun hasLocalFile(relativePath: String): Boolean =
        isCompleteMediaFile(localFile(relativePath))

    fun localFileUri(relativePath: String): String? {
        val target = localFile(relativePath)
        if (target.exists() && target.length() in 1..MIN_COMPLETE_FILE_BYTES) {
            R2Log.w("Purging truncated media file (${target.length()} bytes): $relativePath")
            target.delete()
            return null
        }
        return if (isCompleteMediaFile(target)) target.toURI().toString() else null
    }

    fun deleteLocalFile(relativePath: String) {
        localFile(relativePath).delete()
    }

    private fun isCompleteMediaFile(file: File): Boolean =
        file.exists() && file.isFile && file.length() > MIN_COMPLETE_FILE_BYTES

    fun sha256(file: File): String? {
        if (!isCompleteMediaFile(file)) return null
        val shaDigest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { stream ->
            val chunk = ByteArray(8192)
            while (true) {
                val count = stream.read(chunk)
                if (count < 0) break
                if (count > 0) shaDigest.update(chunk, 0, count)
            }
        }
        val hexHash = shaDigest.digest().joinToString("") { "%02x".format(it) }
        return "sha256:$hexHash"
    }

    companion object {
        private const val MIN_COMPLETE_FILE_BYTES = 64L
    }
}
