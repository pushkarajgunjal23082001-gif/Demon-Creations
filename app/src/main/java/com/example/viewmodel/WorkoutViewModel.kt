package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.WorkoutDatabase
import com.example.data.WorkoutRepository
import com.example.data.WorkoutSession
import com.example.model.WorkoutData
import com.example.model.WorkoutProgram
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ActiveWorkoutSession(
    val program: WorkoutProgram,
    val currentExerciseIndex: Int = -1, // -1 = 5s Warm up countdown
    val isResting: Boolean = false,
    val secondsRemaining: Int = 5, // 5 seconds warm up
    val isPaused: Boolean = false,
    val totalDurationSeconds: Int = 0,
    val isFinished: Boolean = false
)

data class WorkoutUiState(
    val programs: List<WorkoutProgram> = WorkoutData.programs,
    val selectedProgram: WorkoutProgram? = null,
    val activeSession: ActiveWorkoutSession? = null,
    val history: List<WorkoutSession> = emptyList(),
    val streakCount: Int = 0,
    val isCompletedToday: Boolean = false,
    val lastSevenDays: List<DayStatus> = emptyList()
)

data class DayStatus(
    val dayName: String, // e.g. "Mon"
    val dateString: String, // "yyyy-MM-dd"
    val isCompleted: Boolean,
    val isToday: Boolean
)

class WorkoutViewModel(
    application: Application,
    private val repository: WorkoutRepository
) : AndroidViewModel(application) {

    private val _selectedProgram = MutableStateFlow<WorkoutProgram?>(null)
    private val _activeSession = MutableStateFlow<ActiveWorkoutSession?>(null)
    
    private var timerJob: Job? = null

    // Compose cohesive view state from selections, timers, and database flows
    val uiState: StateFlow<WorkoutUiState> = combine(
        _selectedProgram,
        _activeSession,
        repository.allSessions
    ) { selectedProto, activeProto, sessionsList ->
        val streak = calculateStreak(sessionsList)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val completedSet = sessionsList.map { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it.timestamp)) }.toSet()
        val completedToday = completedSet.contains(todayStr)
        val weeklyStatus = getWeeklyCompletionList(completedSet)

        WorkoutUiState(
            selectedProgram = selectedProto,
            activeSession = activeProto,
            history = sessionsList,
            streakCount = streak,
            isCompletedToday = completedToday,
            lastSevenDays = weeklyStatus
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkoutUiState()
    )

    fun selectProgram(program: WorkoutProgram?) {
        _selectedProgram.value = program
    }

    fun startWorkout(program: WorkoutProgram) {
        stopTimer()
        _activeSession.value = ActiveWorkoutSession(
            program = program,
            currentExerciseIndex = -1,
            isResting = false,
            secondsRemaining = 5, // 5-second initial countdown
            isPaused = false,
            totalDurationSeconds = 0,
            isFinished = false
        )
        startTimer()
    }

    fun pauseWorkout() {
        val current = _activeSession.value ?: return
        _activeSession.value = current.copy(isPaused = !current.isPaused)
    }

    fun skipCurrentStep() {
        val current = _activeSession.value ?: return
        timerTick(forceCompleteStep = true)
    }

    fun exitWorkout() {
        stopTimer()
        _activeSession.value = null
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _activeSession.value
                if (current != null && !current.isPaused && !current.isFinished) {
                    timerTick()
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun timerTick(forceCompleteStep: Boolean = false) {
        val session = _activeSession.value ?: return
        
        val currTimeLeft = session.secondsRemaining
        val shouldMoveToNext = currTimeLeft <= 1 || forceCompleteStep

        if (!shouldMoveToNext) {
            // Decrement seconds remaining, increment total workout time
            _activeSession.value = session.copy(
                secondsRemaining = currTimeLeft - 1,
                totalDurationSeconds = session.totalDurationSeconds + 1
            )
        } else {
            // Handle phase transitions
            val program = session.program
            val index = session.currentExerciseIndex

            if (index == -1) {
                // Done with initial warm up -> Move to first exercise!
                _activeSession.value = session.copy(
                    currentExerciseIndex = 0,
                    isResting = false,
                    secondsRemaining = program.exercises[0].durationSeconds,
                    totalDurationSeconds = session.totalDurationSeconds
                )
            } else {
                if (session.isResting) {
                    // Done resting -> Move to active exercise!
                    val nextExIndex = index + 1
                    if (nextExIndex < program.exercises.size) {
                        _activeSession.value = session.copy(
                            currentExerciseIndex = nextExIndex,
                            isResting = false,
                            secondsRemaining = program.exercises[nextExIndex].durationSeconds
                        )
                    }
                } else {
                    // Done with exercise -> Determine if there's rest or finished!
                    val nextExIndex = index + 1
                    if (nextExIndex < program.exercises.size) {
                        // Move to Rest phase
                        _activeSession.value = session.copy(
                            isResting = true,
                            secondsRemaining = 10 // 10 seconds REST limit
                        )
                    } else {
                        // WORKOUT COMPLETE!
                        stopTimer()
                        val finalSession = session.copy(
                            isFinished = true,
                            secondsRemaining = 0
                        )
                        _activeSession.value = finalSession

                        // Persist to Room database in secondary thread
                        viewModelScope.launch {
                            val loggedSession = WorkoutSession(
                                programId = program.id,
                                programName = program.name,
                                durationSeconds = finalSession.totalDurationSeconds,
                                caloriesBurned = program.estimatedCalories
                            )
                            repository.insertSession(loggedSession)
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }

    // --- Core Analytic Helper Methods ---

    private fun calculateStreak(sessions: List<WorkoutSession>): Int {
        if (sessions.isEmpty()) return 0
        
        val sdfObj = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val completedDays = sessions.map { sdfObj.format(Date(it.timestamp)) }.distinct().sortedDescending()
        
        if (completedDays.isEmpty()) return 0
        
        val todayStr = sdfObj.format(Date())
        val yesterdayStr = sdfObj.format(Date(System.currentTimeMillis() - 86400000L))
        
        val latestDay = completedDays.first()
        if (latestDay != todayStr && latestDay != yesterdayStr) {
            return 0 // Streak broken
        }
        
        var streak = 0
        var currentCheckDate = Date()
        val completedSet = completedDays.toSet()
        
        while (true) {
            val dateStr = sdfObj.format(currentCheckDate)
            if (completedSet.contains(dateStr)) {
                streak++
                currentCheckDate = Date(currentCheckDate.time - 86400000L)
            } else {
                if (streak == 0 && dateStr == todayStr) {
                    currentCheckDate = Date(currentCheckDate.time - 86400000L)
                    val yesterdayVal = sdfObj.format(currentCheckDate)
                    if (completedSet.contains(yesterdayVal)) {
                        streak++
                        currentCheckDate = Date(currentCheckDate.time - 86400000L)
                        continue
                    }
                }
                break
            }
        }
        return streak
    }

    private fun getWeeklyCompletionList(completedSet: Set<String>): List<DayStatus> {
        val sdfObj = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayFormat = SimpleDateFormat("EEE", Locale.US) // "Mon", "Tue"
        val todayStr = sdfObj.format(Date())

        val list = mutableListOf<DayStatus>()
        // Return status for today and previous 6 days
        for (i in 6 downTo 0) {
            val dayTime = System.currentTimeMillis() - (i * 86400000L)
            val dayDate = Date(dayTime)
            val dateStr = sdfObj.format(dayDate)
            val dayName = dayFormat.format(dayDate)

            list.add(
                DayStatus(
                    dayName = dayName,
                    dateString = dateStr,
                    isCompleted = completedSet.contains(dateStr),
                    isToday = dateStr == todayStr
                )
            )
        }
        return list
    }
}

class WorkoutViewModelFactory(
    private val application: Application,
    private val repository: WorkoutRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
