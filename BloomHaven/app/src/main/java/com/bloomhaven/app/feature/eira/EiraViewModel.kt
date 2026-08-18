package com.bloomhaven.app.feature.eira

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bloomhaven.app.core.data.AppSettingsRepository
import com.bloomhaven.app.core.util.DateUtils
import com.bloomhaven.app.feature.flow.FlowDatabase
import com.bloomhaven.app.feature.habits.HabitsDatabase
import com.bloomhaven.app.feature.planner.PlannerDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EiraViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = EiraRepository(EiraDatabase.getInstance(application).eiraDao())
    private val settingsRepo = AppSettingsRepository(application)

    val messages = repo.recentMessages().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val today = DateUtils.today()
            val settings = settingsRepo.settings.first()
            val habitsCompleted = runCatching {
                HabitsDatabase.getInstance(getApplication()).habitsDao().logsForDate(today).first().count { it.completed }
            }.getOrDefault(0)
            val waterLogged = runCatching {
                FlowDatabase.getInstance(getApplication()).flowDao().totalForDate(today).first() > 0
            }.getOrDefault(false)
            val plannerCount = runCatching {
                PlannerDatabase.getInstance(getApplication()).plannerDao().forDate(today).first().size
            }.getOrDefault(0)

            val context = EiraContext(
                userName = settings.userName,
                habitsCompletedToday = habitsCompleted,
                waterLoggedToday = waterLogged,
                plannerItemsToday = plannerCount,
            )
            repo.ensureGreetingForNow(context)
        }
    }
}
