package com.sleepsounds.storytelling.service

import com.sleepsounds.storytelling.presentation.navigation.AppRoute

/**
 * Maps active playback states to in-app navigation routes for notification tap intents.
 */
object PlaybackDeepLinks {

    fun routeFor(playback: PlaybackState): String {
        val explicitRoute = playback.contentRoute?.takeIf { it.isNotBlank() }
        if (explicitRoute != null) return explicitRoute

        val activeId = playback.currentTrackId?.takeIf { it.isNotBlank() }
        return when (playback.mode) {
            PlaybackMode.PODCARD ->
                activeId?.let(AppRoute.PodcardDetail::withId) ?: AppRoute.NowPlaying.route
            PlaybackMode.MIXER ->
                activeId?.let(AppRoute.SceneDetail::withId) ?: AppRoute.NowPlaying.route
            PlaybackMode.SINGLE,
            PlaybackMode.IDLE -> AppRoute.NowPlaying.route
        }
    }

    fun sceneRoute(sceneId: String): String = AppRoute.SceneDetail.withId(sceneId)

    fun mixerRoute(presetId: String? = null): String = AppRoute.Mixer.route(presetId)

    fun podcardRoute(podcardId: String): String = AppRoute.PodcardDetail.withId(podcardId)
}
