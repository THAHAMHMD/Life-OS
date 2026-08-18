package com.bloomhaven.app.feature.flow

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bloomhaven.app.core.data.AppSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class FlowUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val entries: List<WaterLogEntity> = emptyList(),
    val totalMl: Int = 0,
    val goalMl: Int = 2000,
    val loggedDates: Set<LocalDate> = emptySet(),
)

class FlowViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = FlowRepository(FlowDatabase.getInstance(application).flowDao())
    private val settingsRepo = AppSettingsRepository(application)

    private val selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<FlowUiState> = combine(
        selectedDate.flatMapLatest { date -> repo.forDate(date) },
        selectedDate.flatMapLatest { date -> repo.totalForDate(date) },
        settingsRepo.settings,
        selectedDate,
        repo.loggedDates(),
    ) { entries, total, settings, date, dates ->
        FlowUiState(
            selectedDate = date,
            entries = entries,
            totalMl = total,
            goalMl = settings.waterDailyGoalMl,
            loggedDates = dates.toSet(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FlowUiState())

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun logWater(amountMl: Int) {
        viewModelScope.launch { repo.log(selectedDate.value, amountMl) }
    }

    fun delete(entry: WaterLogEntity) {
        viewModelScope.launch { repo.delete(entry) }
    }

    fun setGoal(ml: Int) {
        viewModelScope.launch { settingsRepo.setWaterDailyGoal(ml) }
    }
}
