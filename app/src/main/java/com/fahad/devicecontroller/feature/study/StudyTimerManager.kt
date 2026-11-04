package com.fahad.devicecontroller.feature.study

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * High-focus Pomodoro Study Timer.
 * Integrates with Android Enterprise Lock Task Mode when Device Owner is active.
 */
class StudyTimerManager(private val coroutineScope: CoroutineScope) {

    data class StudyTimerState(
        val isRunning: Boolean = false,
        val totalSeconds: Int = 25 * 60,
        val remainingSeconds: Int = 25 * 60,
        val isBreak: Boolean = false,
        val sessionsCompleted: Int = 0,
        val isLockTaskEnabled: Boolean = false
    )

    private val _state = MutableStateFlow(StudyTimerState())
    val state: StateFlow<StudyTimerState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun startTimer(durationMinutes: Int = 25, enableLockTask: Boolean = false) {
        val seconds = durationMinutes * 60
        _state.value = _state.value.copy(
            isRunning = true,
            totalSeconds = seconds,
            remainingSeconds = seconds,
            isBreak = false,
            isLockTaskEnabled = enableLockTask
        )

        timerJob?.cancel()
        timerJob = coroutineScope.launch {
            while (_state.value.remainingSeconds > 0 && _state.value.isRunning) {
                delay(1000L)
                val newRemaining = _state.value.remainingSeconds - 1
                _state.value = _state.value.copy(remainingSeconds = newRemaining)
            }

            if (_state.value.remainingSeconds == 0) {
                // Session completed
                _state.value = _state.value.copy(
                    isRunning = false,
                    sessionsCompleted = _state.value.sessionsCompleted + 1
                )
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _state.value = _state.value.copy(isRunning = false)
    }

    fun resumeTimer() {
        if (_state.value.remainingSeconds <= 0) return
        _state.value = _state.value.copy(isRunning = true)

        timerJob?.cancel()
        timerJob = coroutineScope.launch {
            while (_state.value.remainingSeconds > 0 && _state.value.isRunning) {
                delay(1000L)
                val newRemaining = _state.value.remainingSeconds - 1
                _state.value = _state.value.copy(remainingSeconds = newRemaining)
            }
            if (_state.value.remainingSeconds == 0) {
                _state.value = _state.value.copy(
                    isRunning = false,
                    sessionsCompleted = _state.value.sessionsCompleted + 1
                )
            }
        }
    }

    fun resetTimer(defaultMinutes: Int = 25) {
        timerJob?.cancel()
        val seconds = defaultMinutes * 60
        _state.value = _state.value.copy(
            isRunning = false,
            totalSeconds = seconds,
            remainingSeconds = seconds,
            isBreak = false
        )
    }

    fun setLockTaskOption(enabled: Boolean) {
        _state.value = _state.value.copy(isLockTaskEnabled = enabled)
    }
}

