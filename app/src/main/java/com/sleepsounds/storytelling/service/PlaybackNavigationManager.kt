package com.sleepsounds.storytelling.service

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Event bus for foreground playback navigation requests and media notification deep links.
 */
@Singleton
class PlaybackNavigationManager @Inject constructor() {

    private val nowPlayingChannel = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val navigateToNowPlaying: SharedFlow<Unit> = nowPlayingChannel.asSharedFlow()
    val openNowPlayingEvents: SharedFlow<Unit> get() = navigateToNowPlaying

    private val deepLinkChannel = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val deepLinkRoute: SharedFlow<String> = deepLinkChannel.asSharedFlow()
    val deepLinkRoutes: SharedFlow<String> get() = deepLinkRoute

    fun requestNowPlaying() {
        nowPlayingChannel.tryEmit(Unit)
    }

    fun requestOpenNowPlaying() {
        nowPlayingChannel.tryEmit(Unit)
    }

    fun requestDeepLink(targetRoute: String) {
        val cleaned = targetRoute.trim()
        if (cleaned.isNotEmpty()) {
            deepLinkChannel.tryEmit(cleaned)
        }
    }
}
