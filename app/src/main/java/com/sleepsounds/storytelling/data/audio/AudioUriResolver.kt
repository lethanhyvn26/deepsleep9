package com.sleepsounds.storytelling.data.audio

import com.sleepsounds.storytelling.data.local.dao.SoundDao
import com.sleepsounds.storytelling.data.media.MediaFileRepository
import com.sleepsounds.storytelling.data.remote.ManifestApi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves local downloaded files first and falls back to Cloudflare R2 / Worker URLs.
 * All audio tracks are freely accessible without entitlement restrictions.
 */
@Singleton
class AudioUriResolver @Inject constructor(
    private val mediaFileRepository: MediaFileRepository,
    private val manifestApi: ManifestApi,
    private val soundDao: SoundDao,
) {
    suspend fun resolveForPlayback(
        trackId: String,
        audioFilePath: String? = null,
        isPremiumTrack: Boolean = false,
        userIsPremium: Boolean = true,
    ): ResolvedAudio {
        val storedAudioPath = if (audioFilePath.isNullOrBlank()) {
            soundDao.getById(trackId)?.audioPath?.takeIf { it.isNotBlank() }
        } else {
            audioFilePath
        }
        val normalizedPath = storedAudioPath?.takeIf { it.isNotBlank() }
            ?: "sounds/$trackId/audio.ogg"

        val remoteCandidateUrl = if (
            normalizedPath.startsWith("http://", ignoreCase = true) ||
            normalizedPath.startsWith("https://", ignoreCase = true)
        ) {
            normalizedPath
        } else {
            manifestApi.getRemoteFileUrl(normalizedPath)
        }

        mediaFileRepository.getLocalUri(normalizedPath)?.let { localUri ->
            return ResolvedAudio(
                uri = localUri,
                sourceType = AudioSourceType.LOCAL_FILE,
                trackId = trackId,
                relativePath = normalizedPath,
                remoteFallbackUri = remoteCandidateUrl,
            )
        }

        val resolvedRemote = remoteCandidateUrl.ifBlank {
            mediaFileRepository.getRemoteUrl(normalizedPath)
        }
        return ResolvedAudio(
            uri = resolvedRemote,
            sourceType = AudioSourceType.REMOTE_CACHED,
            trackId = trackId,
            relativePath = normalizedPath,
            remoteFallbackUri = resolvedRemote,
        )
    }
}
data class ResolvedAudio(
    val uri: String,
    val sourceType: AudioSourceType,
    val trackId: String,
    val relativePath: String,
    val remoteFallbackUri: String? = null,
)
enum class AudioSourceType {
    LOCAL_FILE,
    REMOTE_CACHED,
}
