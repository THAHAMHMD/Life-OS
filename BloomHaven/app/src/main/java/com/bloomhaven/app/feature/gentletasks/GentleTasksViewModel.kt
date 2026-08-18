package com.bloomhaven.app.feature.gentletasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class GentleTasksUiState(
    val toDo: List<TaskEntity> = emptyList(),
    val completed: List<TaskEntity> = emptyList(),
)

class GentleTasksViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = GentleTasksRepository(GentleTasksDatabase.getInstance(application).gentleTasksDao())

    val uiState: StateFlow<GentleTasksUiState> = combine(
        repo.incomplete(),
        repo.completed(),
    ) { toDo, completed ->
        GentleTasksUiState(toDo = toDo, completed = completed)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GentleTasksUiState())

    fun addTask(title: String, notes: String, dueDate: LocalDate?) {
        viewModelScope.launch { repo.add(title, notes, dueDate) }
    }

    fun updateTask(task: TaskEntity, title: String, notes: String, dueDate: LocalDate?) {
        viewModelScope.launch { repo.update(task, title, notes, dueDate) }
    }

    fun setCompleted(task: TaskEntity, completed: Boolean) {
        viewModelScope.launch { repo.setCompleted(task, completed) }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch { repo.delete(task) }
    }
}
