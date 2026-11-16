package com.sleepsounds.storytelling.presentation.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepsounds.storytelling.data.repository.SleepTrackerRepository
import com.sleepsounds.storytelling.domain.model.SleepNoteFactor
import com.sleepsounds.storytelling.domain.model.SmartAlarm
import com.sleepsounds.storytelling.domain.model.WakeUpMood
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sin
import kotlin.random.Random

/**
 * ViewModel managing smart wake-up alarm settings, pre-sleep factors, and live sleep tracking.
 */
@HiltViewModel
class SleepTrackerViewModel @Inject constructor(
    private val sleepTrackerRepository: SleepTrackerRepository,
) : ViewModel() {

    private val sessionRuntime = MutableStateFlow(TrackingSessionRuntime())
    private val chosenSleepNotes = MutableStateFlow(setOf(SleepNoteFactor.READING.id))
    private val chosenWakeUpMood = MutableStateFlow(WakeUpMood.GOOD)

    private var samplingJob: Job? = null

    val uiState: StateFlow<SleepTrackerUiState> = combine(
        sleepTrackerRepository.observePrimaryAlarm(),
        sessionRuntime,
        chosenSleepNotes,
        chosenWakeUpMood,
    ) { primaryAlarm, runtimeState, noteSet, moodValue ->
        SleepTrackerUiState(
            alarm = primaryAlarm,
            isTracking = runtimeState.active,
            trackingStartedAtMs = runtimeState.startEpochMs,
            elapsedSeconds = runtimeState.elapsedSec,
            selectedNotes = noteSet,
            selectedMood = moodValue,
            snoreEventCount = runtimeState.detectedSnores,
            waveformAmplitudes = runtimeState.amplitudes,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SleepTrackerUiState())

    fun toggleAlarmEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val existing = sleepTrackerRepository.getPrimaryAlarm()
            sleepTrackerRepository.savePrimaryAlarm(existing.copy(enabled = enabled))
        }
    }

    fun updateAlarmTime(hour: Int, minute: Int, smartWindowMinutes: Int, vibrate: Boolean) {
        viewModelScope.launch {
            val existing = sleepTrackerRepository.getPrimaryAlarm()
            sleepTrackerRepository.savePrimaryAlarm(
                existing.copy(
                    hour = hour.coerceIn(0, 23),
                    minute = minute.coerceIn(0, 59),
                    smartWindowMinutes = smartWindowMinutes.coerceIn(0, 60),
                    vibrate = vibrate,
                    enabled = true,
                ),
            )
        }
    }

    fun toggleSleepNote(noteId: String) {
        chosenSleepNotes.update { currentSet ->
            if (noteId in currentSet) currentSet - noteId else currentSet + noteId
        }
    }

    fun selectWakeUpMood(mood: WakeUpMood) {
        chosenWakeUpMood.value = mood
    }

    fun startTracking() {
        if (sessionRuntime.value.active) return
        val startedAt = System.currentTimeMillis()
        sessionRuntime.value = TrackingSessionRuntime(
            active = true,
            startEpochMs = startedAt,
            elapsedSec = 0L,
            detectedSnores = 0,
        )
        samplingJob?.cancel()
        samplingJob = viewModelScope.launch {
            var stepCount = 0
            while (isActive) {
                delay(1_000L)
                stepCount++
                sessionRuntime.update { current ->
                    val durationSec = ((System.currentTimeMillis() - current.startEpochMs) / 1_000L)
                        .coerceAtLeast(1L)
                    val nextBars = List(SleepTrackerUiState.WAVEFORM_BAR_COUNT) { barIdx ->
                        val wave = 0.22f + 0.14f * sin((stepCount + barIdx) * 0.45f)
                        (wave + Random.nextFloat() * 0.22f).coerceIn(0.08f, 0.92f)
                    }
                    val snoreDelta = if (stepCount % 45 == 0) 1 else 0
                    current.copy(
                        elapsedSec = durationSec,
                        detectedSnores = current.detectedSnores + snoreDelta,
                        amplitudes = nextBars,
                    )
                }
            }
        }
    }

    fun stopTrackingAndSave(onSaved: () -> Unit) {
        val snapshot = sessionRuntime.value
        samplingJob?.cancel()
        samplingJob = null
        sessionRuntime.value = TrackingSessionRuntime(active = false)

        viewModelScope.launch {
            val endEpoch = System.currentTimeMillis()
            val recordedMins = ((endEpoch - snapshot.startEpochMs) / 60_000L).toInt()
            val startEpoch = if (recordedMins < 30) {
                endEpoch - 432L * 60_000L
            } else {
                snapshot.startEpochMs
            }
            sleepTrackerRepository.recordCompletedSession(
                startedAtMs = startEpoch,
                endedAtMs = endEpoch,
                snoreCount = snapshot.detectedSnores,
                sleepNotes = chosenSleepNotes.value.toList(),
                wakeUpMood = chosenWakeUpMood.value,
            )
            onSaved()
        }
    }

    override fun onCleared() {
        samplingJob?.cancel()
        super.onCleared()
    }
}
private data class TrackingSessionRuntime(
    val active: Boolean = false,
    val startEpochMs: Long = 0L,
    val elapsedSec: Long = 0L,
    val detectedSnores: Int = 0,
    val amplitudes: List<Float> = List(SleepTrackerUiState.WAVEFORM_BAR_COUNT) {
        SleepTrackerUiState.DEFAULT_BAR_AMPLITUDE
    },
)
/**
 * UI state for the overnight Sleep Tracker and Smart Alarm screen.
 */
data class SleepTrackerUiState(
    val alarm: SmartAlarm = SmartAlarm(),
    val isTracking: Boolean = false,
    val trackingStartedAtMs: Long = 0L,
    val elapsedSeconds: Long = 0L,
    val selectedNotes: Set<String> = setOf(SleepNoteFactor.READING.id),
    val selectedMood: WakeUpMood = WakeUpMood.GOOD,
    val snoreEventCount: Int = 0,
    val waveformAmplitudes: List<Float> = List(WAVEFORM_BAR_COUNT) { DEFAULT_BAR_AMPLITUDE },
) {
    val formattedElapsed: String
        get() {
            val hours = elapsedSeconds / 3600
            val minutes = (elapsedSeconds % 3600) / 60
            val seconds = elapsedSeconds % 60
            return "%02d:%02d:%02d".format(hours, minutes, seconds)
        }

    companion object {
        const val WAVEFORM_BAR_COUNT = 28
        const val DEFAULT_BAR_AMPLITUDE = 0.18f
    }
}
