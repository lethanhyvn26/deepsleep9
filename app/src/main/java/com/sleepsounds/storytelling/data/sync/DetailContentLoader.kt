package com.sleepsounds.storytelling.data.sync

import com.sleepsounds.storytelling.data.media.MediaFileRepository
import com.sleepsounds.storytelling.data.repository.UserPreferencesRepository
import com.sleepsounds.storytelling.util.R2Log
import com.sleepsounds.storytelling.util.YouTubeUrls
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Evicts stale cached files if remote paths changed and downloads audio on demand.
 */
@Singleton
class DetailContentLoader @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val mediaFileRepository: MediaFileRepository,
) {
    suspend fun prepareForDetail(
        trackId: String,
        audioPath: String?,
        imagePath: String?,
        isPremium: Boolean = false,
        userIsPremium: Boolean = true,
        downloadAudio: Boolean = false,
    ): DetailPrepareResult {
        val (previousAudioPath, _) = userPreferencesRepository.getCachedContentPaths(trackId)
        val currentAudioPath = audioPath.orEmpty()
        val currentImagePath = imagePath.orEmpty()

        if (!previousAudioPath.isNullOrBlank() && previousAudioPath != currentAudioPath) {
            mediaFileRepository.deleteLocal(previousAudioPath)
            R2Log.d("Evicted stale local audio cache for $trackId")
        }

        userPreferencesRepository.updateContentPathCache(
            trackId = trackId,
            audioPath = currentAudioPath,
            imagePath = currentImagePath,
        )

        val shouldDownloadFile = downloadAudio &&
            currentAudioPath.isNotBlank() &&
            !YouTubeUrls.isYouTubeUrl(currentAudioPath)

        if (shouldDownloadFile) {
            val downloadOutcome = mediaFileRepository.ensureDownloaded(currentAudioPath)
            if (downloadOutcome.isFailure) {
                R2Log.w("On-demand audio download failed for $trackId")
                return DetailPrepareResult.DownloadFailed
            }
        }

        return DetailPrepareResult.Ready
    }
}
sealed class DetailPrepareResult {
    data object Ready : DetailPrepareResult()
    data object DownloadFailed : DetailPrepareResult()
}
