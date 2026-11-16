package com.sleepsounds.storytelling.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

/**
 * UI and service state for the automatic sleep countdown timer.
 */
data class SleepTimerState(
    val isActive: Boolean = false,
    val totalSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val isFading: Boolean = false,
    val isPaused: Boolean = false,
    val lastSelectedMinutes: Int = DEFAULT_TIMER_MINUTES,
) {
    companion object {
        const val DEFAULT_TIMER_MINUTES = 90
    }
}

/**
 * Coordinates the sleep timer countdown, gradual audio fade-out, and playback stop.
 */
@Singleton
class SleepTimerManager @Inject constructor(
    private val audioPlayerManager: AudioPlayerManager,
) {
    private val timerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var countdownJob: Job? = null
    private var pausedByPlayback = false
    private var lastChosenMinutes: Int = SleepTimerState.DEFAULT_TIMER_MINUTES

    private val _state = MutableStateFlow(SleepTimerState())
    val state: StateFlow<SleepTimerState> = _state.asStateFlow()

    val presetMinutes: List<Int> = listOf(15, 30, 45, 60, 90)

    init {
        timerScope.launch {
            audioPlayerManager.playbackState.collect { playback ->
                if (!_state.value.isActive) return@collect
                val currentlyPlaying = playback.mode != PlaybackMode.IDLE && playback.isPlaying
                pausedByPlayback = !currentlyPlaying
                _state.value = _state.value.copy(isPaused = pausedByPlayback)
            }
        }
    }

    fun startTimer(minutes: Int) {
        val validMinutes = minutes.coerceIn(MIN_TIMER_MINUTES, MAX_TIMER_MINUTES)
        lastChosenMinutes = validMinutes
        cancelTimer(keepPreferredMinutes = true)

        val durationSeconds = validMinutes * 60
        val currentPlayback = audioPlayerManager.playbackState.value
        pausedByPlayback = currentPlayback.mode != PlaybackMode.IDLE && !currentPlayback.isPlaying

        _state.value = SleepTimerState(
            isActive = true,
            totalSeconds = durationSeconds,
            remainingSeconds = durationSeconds,
            isPaused = pausedByPlayback,
            lastSelectedMinutes = lastChosenMinutes,
        )

        countdownJob = timerScope.launch {
            var secondsLeft = durationSeconds
            while (secondsLeft > 0) {
                while (pausedByPlayback) {
                    delay(PAUSE_POLL_MS)
                }

                val fadingNow = secondsLeft <= FADE_OUT_WINDOW_SECONDS
                _state.value = _state.value.copy(
                    remainingSeconds = secondsLeft,
                    isFading = fadingNow,
                    isPaused = false,
                )

                if (fadingNow) {
                    val scale = (secondsLeft.toFloat() / FADE_OUT_WINDOW_SECONDS).coerceIn(0f, 1f)
                    audioPlayerManager.applyVolumeScale(scale)
                }

                awaitOneSecondWhenUnpaused()
                if (!pausedByPlayback) {
                    secondsLeft -= 1
                }
            }

            audioPlayerManager.stopAll()
            cancelTimer(keepPreferredMinutes = true)
        }
    }

    fun cancelTimer(keepPreferredMinutes: Boolean = true) {
        countdownJob?.cancel()
        countdownJob = null
        pausedByPlayback = false
        audioPlayerManager.resetVolumeScale()
        _state.value = SleepTimerState(
            lastSelectedMinutes = if (keepPreferredMinutes) {
                lastChosenMinutes
            } else {
                SleepTimerState.DEFAULT_TIMER_MINUTES
            },
        )
    }

    private suspend fun awaitOneSecondWhenUnpaused() {
        var accumulatedMs = 0L
        while (accumulatedMs < ONE_SECOND_MS) {
            while (pausedByPlayback) {
                delay(PAUSE_POLL_MS)
            }
            val stepMs = min(PAUSE_POLL_MS, ONE_SECOND_MS - accumulatedMs)
            delay(stepMs)
            if (!pausedByPlayback) {
                accumulatedMs += stepMs
            }
        }
    }

    fun formatRemaining(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%d:%02d".format(mins, secs)
    }

    companion object {
        private const val MIN_TIMER_MINUTES = 5
        private const val MAX_TIMER_MINUTES = 180
        private const val FADE_OUT_WINDOW_SECONDS = 60
        private const val PAUSE_POLL_MS = 100L
        private const val ONE_SECOND_MS = 1000L
    }
}
