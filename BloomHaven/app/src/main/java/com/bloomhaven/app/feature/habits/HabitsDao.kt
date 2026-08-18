package com.bloomhaven.app.feature.habits

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HabitsDao {
    @Insert
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("SELECT * FROM habit WHERE isArchived = 0 ORDER BY createdAt ASC")
    fun activeHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habit ORDER BY createdAt ASC")
    fun allHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: HabitLogEntity)

    @Query("DELETE FROM habit_log WHERE habitId = :habitId AND date = :date")
    suspend fun deleteLog(habitId: Long, date: LocalDate)

    @Query("SELECT * FROM habit_log WHERE date = :date")
    fun logsForDate(date: LocalDate): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_log WHERE habitId = :habitId ORDER BY date DESC")
    fun logsForHabit(habitId: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_log WHERE date BETWEEN :start AND :end")
    fun logsBetween(start: LocalDate, end: LocalDate): Flow<List<HabitLogEntity>>

    @Query("SELECT DISTINCT date FROM habit_log WHERE completed = 1 ORDER BY date DESC")
    fun allCompletedDates(): Flow<List<LocalDate>>

    @Query("SELECT COUNT(*) FROM habit_log WHERE completed = 1")
    fun totalCompletions(): Flow<Int>
}
