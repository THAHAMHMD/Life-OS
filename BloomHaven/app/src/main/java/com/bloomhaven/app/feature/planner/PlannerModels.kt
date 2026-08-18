package com.bloomhaven.app.feature.planner

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Recurring items are materialized as individual rows up front (spec: keep the planner simple). */
object Recurrence {
    const val NONE = "NONE"
    const val DAILY = "DAILY"
    const val WEEKLY = "WEEKLY"
    const val MONTHLY = "MONTHLY"
    const val YEARLY = "YEARLY"

    val options = listOf(NONE, DAILY, WEEKLY, MONTHLY, YEARLY)
    fun label(value: String) = when (value) {
        DAILY -> "Daily"
        WEEKLY -> "Weekly"
        MONTHLY -> "Monthly"
        YEARLY -> "Yearly"
        else -> "Doesn't repeat"
    }
}

val PlannerCategories = listOf("Work Shift", "Appointment", "Event", "Important Date", "Reminder", "Task")

@Entity(tableName = "planner_item")
data class PlannerItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: LocalDate,
    val time: LocalTime? = null,
    val notes: String = "",
    val category: String = "Event",
    val recurrenceGroupId: String? = null,
    val recurrenceType: String = Recurrence.NONE,
    val reminderEnabled: Boolean = false,
    val isCompleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
