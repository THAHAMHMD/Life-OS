package com.bloomhaven.app.feature.habits

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class HabitsRepository(private val dao: HabitsDao) {
    fun activeHabits(): Flow<List<HabitEntity>> = dao.activeHabits()
    fun allHabits(): Flow<List<HabitEntity>> = dao.allHabits()
    fun logsForDate(date: LocalDate): Flow<List<HabitLogEntity>> = dao.logsForDate(date)
    fun logsForHabit(habitId: Long): Flow<List<HabitLogEntity>> = dao.logsForHabit(habitId)
    fun logsBetween(start: LocalDate, end: LocalDate): Flow<List<HabitLogEntity>> = dao.logsBetween(start, end)
    fun allCompletedDates(): Flow<List<LocalDate>> = dao.allCompletedDates()
    fun totalCompletions(): Flow<Int> = dao.totalCompletions()

    suspend fun addHabit(name: String, emoji: String, repeatDays: String) {
        dao.insertHabit(HabitEntity(name = name, emoji = emoji, repeatDays = repeatDays))
    }

    suspend fun updateHabit(habit: HabitEntity) = dao.updateHabit(habit)
    suspend fun archiveHabit(habit: HabitEntity) = dao.updateHabit(habit.copy(isArchived = true))
    suspend fun deleteHabit(habit: HabitEntity) = dao.deleteHabit(habit)

    suspend fun setCompleted(habitId: Long, date: LocalDate, completed: Boolean) {
        if (completed) dao.upsertLog(HabitLogEntity(habitId = habitId, date = date, completed = true))
        else dao.deleteLog(habitId, date)
    }

    /** Longest run of consecutive days (up to today) with at least one habit completed. */
    fun currentStreak(completedDates: Set<LocalDate>, today: LocalDate = LocalDate.now()): Int {
        var streak = 0
        var day = today
        while (completedDates.contains(day)) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }

    fun bestStreak(completedDates: Set<LocalDate>): Int {
        if (completedDates.isEmpty()) return 0
        val sorted = completedDates.sorted()
        var best = 1
        var current = 1
        for (i in 1 until sorted.size) {
            current = if (sorted[i] == sorted[i - 1].plusDays(1)) current + 1 else 1
            if (current > best) best = current
        }
        return best
    }
}
