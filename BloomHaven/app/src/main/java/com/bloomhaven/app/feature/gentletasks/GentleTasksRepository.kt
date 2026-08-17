package com.bloomhaven.app.feature.gentletasks

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

class GentleTasksRepository(private val dao: GentleTasksDao) {
    fun incomplete(): Flow<List<TaskEntity>> = dao.incompleteOnly()
    fun completed(): Flow<List<TaskEntity>> = dao.completedOnly()

    suspend fun add(title: String, notes: String, dueDate: LocalDate?) {
        dao.insert(TaskEntity(title = title, notes = notes, dueDate = dueDate))
    }

    suspend fun update(task: TaskEntity, title: String, notes: String, dueDate: LocalDate?) {
        dao.update(task.copy(title = title, notes = notes, dueDate = dueDate))
    }

    suspend fun setCompleted(task: TaskEntity, completed: Boolean) {
        dao.update(task.copy(isCompleted = completed, completedAt = if (completed) LocalDateTime.now() else null))
    }

    suspend fun delete(task: TaskEntity) = dao.delete(task)
}
