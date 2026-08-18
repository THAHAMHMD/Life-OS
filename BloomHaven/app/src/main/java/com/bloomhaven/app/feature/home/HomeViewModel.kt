package com.bloomhaven.app.feature.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bloomhaven.app.core.data.AppSettingsRepository
import com.bloomhaven.app.core.data.DEFAULT_HOME_SECTIONS
import com.bloomhaven.app.core.util.DateUtils
import com.bloomhaven.app.feature.eira.EiraContext
import com.bloomhaven.app.feature.eira.EiraDatabase
import com.bloomhaven.app.feature.eira.EiraMessageEntity
import com.bloomhaven.app.feature.eira.EiraMessageKind
import com.bloomhaven.app.feature.eira.EiraRepository
import com.bloomhaven.app.feature.flow.FlowDatabase
import com.bloomhaven.app.feature.gentletasks.GentleTasksDatabase
import com.bloomhaven.app.feature.gentletasks.TaskEntity
import com.bloomhaven.app.feature.habits.HabitsDatabase
import com.bloomhaven.app.feature.planner.PlannerDatabase
import com.bloomhaven.app.feature.planner.PlannerItemEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "",
    val greeting: String? = null,
    val quote: String? = null,
    val todaySchedule: List<PlannerItemEntity> = emptyList(),
    val todayTasks: List<TaskEntity> = emptyList(),
    val waterTotalMl: Int = 0,
    val waterGoalMl: Int = 2000,
    val habitsCompletedToday: Int = 0,
    val upcomingEvents: List<PlannerItemEntity> = emptyList(),
    val homeSections: List<String> = DEFAULT_HOME_SECTIONS,
    val isLoading: Boolean = true,
) {
    /** A small, honest summary of the day — omitted entirely if there's nothing coherent to say (spec 4.9). */
    val dailySummary: String?
        get() {
            val parts = mutableListOf<String>()
            if (habitsCompletedToday > 0) parts.add("$habitsCompletedToday habit${if (habitsCompletedToday == 1) "" else "s"} completed")
            val doneTasks = todayTasks.count { it.isCompleted }
            if (doneTasks > 0) parts.add("$doneTasks task${if (doneTasks == 1) "" else "s"} done")
            if (waterTotalMl > 0) parts.add("${waterTotalMl}ml of water logged")
            val doneSchedule = todaySchedule.count { it.isCompleted }
            if (doneSchedule > 0) parts.add("$doneSchedule schedule item${if (doneSchedule == 1) "" else "s"} completed")
            if (parts.isEmpty()) return null
            return "Today so far: ${parts.joinToString(", ")}."
        }
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepo = AppSettingsRepository(application)
    private val today = DateUtils.today()

    private val eiraFlow = EiraDatabase.getInstance(application).eiraDao().recent().map { messages ->
        val greeting = messages.firstOrNull { it.kind == EiraMessageKind.GREETING }
        val quote = messages.firstOrNull { it.kind == EiraMessageKind.QUOTE }
        greeting to quote
    }

    private val scheduleFlow = PlannerDatabase.getInstance(application).plannerDao().forDate(today)
    private val upcomingFlow = PlannerDatabase.getInstance(application).plannerDao().upcoming(today.plusDays(1), 5)
    private val tasksFlow = GentleTasksDatabase.getInstance(application).gentleTasksDao().incompleteOnly()
        .map { tasks -> tasks.filter { task -> task.dueDate != null && !task.dueDate.isAfter(today) } }
    private val waterFlow = FlowDatabase.getInstance(application).flowDao().totalForDate(today)
    private val habitsFlow = HabitsDatabase.getInstance(application).habitsDao().logsForDate(today)
        .map { logs -> logs.count { it.completed } }

    private data class PartialA(val greeting: EiraMessageEntity?, val quote: EiraMessageEntity?, val schedule: List<PlannerItemEntity>, val tasks: List<TaskEntity>)
    private data class PartialB(val water: Int, val habits: Int, val upcoming: List<PlannerItemEntity>)

    private val partialAFlow = combine(eiraFlow, scheduleFlow, tasksFlow) { (greeting, quote), schedule, tasks ->
        PartialA(greeting, quote, schedule, tasks)
    }
    private val partialBFlow = combine(waterFlow, habitsFlow, upcomingFlow) { water, habits, upcoming ->
        PartialB(water, habits, upcoming)
    }

    val uiState = combine(partialAFlow, partialBFlow, settingsRepo.settings) { a, b, settings ->
        HomeUiState(
            userName = settings.userName,
            greeting = a.greeting?.text,
            quote = a.quote?.text,
            todaySchedule = a.schedule,
            todayTasks = a.tasks,
            waterTotalMl = b.water,
            waterGoalMl = settings.waterDailyGoalMl,
            habitsCompletedToday = b.habits,
            upcomingEvents = b.upcoming,
            homeSections = settings.homeSections,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        // Home is the app's entry screen, so this is where Eira's once-per-window greeting
        // actually gets generated (spec: greets only when the app is opened).
        viewModelScope.launch {
            val settings = settingsRepo.settings.first()
            val eiraRepo = EiraRepository(EiraDatabase.getInstance(application).eiraDao())
            eiraRepo.ensureGreetingForNow(
                EiraContext(
                    userName = settings.userName,
                    habitsCompletedToday = habitsFlow.first(),
                    waterLoggedToday = waterFlow.first() > 0,
                    plannerItemsToday = scheduleFlow.first().size,
                ),
            )
        }
    }
}
