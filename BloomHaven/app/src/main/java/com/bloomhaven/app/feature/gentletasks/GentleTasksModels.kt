package com.bloomhaven.app.feature.gentletasks

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/** A simple to-do item. Completed tasks are kept as permanent history, never auto-deleted. */
@Entity(tableName = "gentle_tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val dueDate: LocalDate? = null,
    val isCompleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null,
)
