package com.bloomhaven.app.feature.habits

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitsUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val activeHabits: List<HabitEntity> = emptyList(),
    val logsForDate: Set<Long> = emptySet(),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val completedDates: Set<LocalDate> = emptySet(),
)

class HabitsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = HabitsRepository(HabitsDatabase.getInstance(application).habitsDao())

    private val selectedDate = MutableStateFlow(LocalDate.now())

    private val logsForDateFlow = selectedDate.flatMapLatest { repo.logsForDate(it) }

    val uiState = combine(
        selectedDate,
        repo.activeHabits(),
        logsForDateFlow,
        repo.allCompletedDates(),
        repo.totalCompletions(),
    ) { date, habits, logs, completedDates, total ->
        val datesSet = completedDates.toSet()
        HabitsUiState(
            selectedDate = date,
            activeHabits = habits,
            logsForDate = logs.filter { it.completed }.map { it.habitId }.toSet(),
            currentStreak = repo.currentStreak(datesSet),
            bestStreak = repo.bestStreak(datesSet),
            totalCompletions = total,
            completedDates = datesSet,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HabitsUiState())

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun toggleHabit(habitId: Long, completed: Boolean) {
        viewModelScope.launch { repo.setCompleted(habitId, selectedDate.value, completed) }
    }

    fun addHabit(name: String, emoji: String, repeatDays: String) {
        viewModelScope.launch { repo.addHabit(name, emoji, repeatDays) }
    }

    fun archiveHabit(habit: HabitEntity) {
        viewModelScope.launch { repo.archiveHabit(habit) }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch { repo.deleteHabit(habit) }
    }

    fun logsForHabit(habitId: Long) = repo.logsForHabit(habitId)
}
