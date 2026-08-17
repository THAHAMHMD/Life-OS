package com.bloomhaven.app.feature.planner

import android.content.Context
import com.bloomhaven.app.core.notifications.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

class PlannerRepository(private val context: Context, private val dao: PlannerDao) {
    fun forDate(date: LocalDate): Flow<List<PlannerItemEntity>> = dao.forDate(date)
    fun between(start: LocalDate, end: LocalDate): Flow<List<PlannerItemEntity>> = dao.between(start, end)
    fun upcoming(from: LocalDate, limit: Int = 20): Flow<List<PlannerItemEntity>> = dao.upcoming(from, limit)
    fun datesWithItems(start: LocalDate, end: LocalDate): Flow<List<LocalDate>> = dao.datesWithItems(start, end)

    suspend fun addItem(
        title: String,
        date: LocalDate,
        time: LocalTime?,
        notes: String,
        category: String,
        recurrenceType: String,
        reminderEnabled: Boolean,
    ) {
        val groupId = if (recurrenceType != Recurrence.NONE) UUID.randomUUID().toString() else null
        val occurrences = generateOccurrenceDates(date, recurrenceType)
        val items = occurrences.map { occDate ->
            PlannerItemEntity(
                title = title,
                date = occDate,
                time = time,
                notes = notes,
                category = category,
                recurrenceGroupId = groupId,
                recurrenceType = recurrenceType,
                reminderEnabled = reminderEnabled,
            )
        }
        val ids = dao.insertAll(items)
        if (reminderEnabled && time != null) {
            items.forEachIndexed { index, item -> scheduleReminder(item.copy(id = ids[index])) }
        }
    }

    suspend fun updateItem(existing: PlannerItemEntity, title: String, date: LocalDate, time: LocalTime?, notes: String, category: String, reminderEnabled: Boolean) {
        val updated = existing.copy(title = title, date = date, time = time, notes = notes, category = category, reminderEnabled = reminderEnabled)
        dao.update(updated)
        if (reminderEnabled && time != null) scheduleReminder(updated) else cancelReminder(updated)
    }

    suspend fun toggleCompleted(item: PlannerItemEntity) {
        dao.update(item.copy(isCompleted = !item.isCompleted))
    }

    suspend fun deleteItem(item: PlannerItemEntity) {
        dao.delete(item)
        cancelReminder(item)
    }

    private fun scheduleReminder(item: PlannerItemEntity) {
        val time = item.time ?: return
        ReminderScheduler.schedule(
            context = context,
            uniqueName = "planner_${item.id}",
            notificationId = ("planner_${item.id}").hashCode(),
            title = item.title,
            body = if (item.notes.isNotBlank()) item.notes else item.category,
            at = LocalDateTime.of(item.date, time),
        )
    }

    private fun cancelReminder(item: PlannerItemEntity) {
        ReminderScheduler.cancel(context, "planner_${item.id}")
    }

    private fun generateOccurrenceDates(start: LocalDate, recurrenceType: String): List<LocalDate> = when (recurrenceType) {
        Recurrence.DAILY -> (0 until 60).map { start.plusDays(it.toLong()) }
        Recurrence.WEEKLY -> (0 until 26).map { start.plusWeeks(it.toLong()) }
        Recurrence.MONTHLY -> (0 until 12).map { start.plusMonths(it.toLong()) }
        Recurrence.YEARLY -> (0 until 3).map { start.plusYears(it.toLong()) }
        else -> listOf(start)
    }
}
