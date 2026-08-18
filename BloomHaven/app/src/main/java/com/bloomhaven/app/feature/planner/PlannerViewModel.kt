package com.bloomhaven.app.feature.planner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

enum class PlannerViewMode { DAY, WEEK, MONTH }

data class PlannerUiState(
    val viewMode: PlannerViewMode = PlannerViewMode.DAY,
    val selectedDate: LocalDate = LocalDate.now(),
    val dayItems: List<PlannerItemEntity> = emptyList(),
    val weekDatesWithItems: Set<LocalDate> = emptySet(),
    val monthDatesWithItems: Set<LocalDate> = emptySet(),
)

class PlannerViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PlannerRepository(application, PlannerDatabase.getInstance(application).plannerDao())

    private val viewMode = MutableStateFlow(PlannerViewMode.DAY)
    private val selectedDate = MutableStateFlow(LocalDate.now())

    private val dayItemsFlow = selectedDate.flatMapLatest { repo.forDate(it) }

    private val weekDatesFlow = selectedDate.flatMapLatest { date ->
        val start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val end = start.plusDays(6)
        repo.datesWithItems(start, end)
    }

    private val monthDatesFlow = selectedDate.flatMapLatest { date ->
        val start = date.withDayOfMonth(1)
        val end = date.withDayOfMonth(date.lengthOfMonth())
        repo.datesWithItems(start, end)
    }

    val uiState = combine(viewMode, selectedDate, dayItemsFlow, weekDatesFlow, monthDatesFlow) { mode, date, dayItems, weekDates, monthDates ->
        PlannerUiState(
            viewMode = mode,
            selectedDate = date,
            dayItems = dayItems,
            weekDatesWithItems = weekDates.toSet(),
            monthDatesWithItems = monthDates.toSet(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlannerUiState())

    fun setViewMode(mode: PlannerViewMode) {
        viewMode.value = mode
    }

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun addItem(title: String, date: LocalDate, time: LocalTime?, notes: String, category: String, recurrenceType: String, reminderEnabled: Boolean) {
        viewModelScope.launch { repo.addItem(title, date, time, notes, category, recurrenceType, reminderEnabled) }
    }

    fun updateItem(existing: PlannerItemEntity, title: String, date: LocalDate, time: LocalTime?, notes: String, category: String, reminderEnabled: Boolean) {
        viewModelScope.launch { repo.updateItem(existing, title, date, time, notes, category, reminderEnabled) }
    }

    fun toggleCompleted(item: PlannerItemEntity) {
        viewModelScope.launch { repo.toggleCompleted(item) }
    }

    fun deleteItem(item: PlannerItemEntity) {
        viewModelScope.launch { repo.deleteItem(item) }
    }
}
