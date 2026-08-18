package com.bloomhaven.app.feature.flow

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface FlowDao {
    @Insert
    suspend fun insert(entry: WaterLogEntity): Long

    @Update
    suspend fun update(entry: WaterLogEntity)

    @Delete
    suspend fun delete(entry: WaterLogEntity)

    @Query("SELECT * FROM water_log WHERE date = :date ORDER BY time DESC")
    fun forDate(date: LocalDate): Flow<List<WaterLogEntity>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_log WHERE date = :date")
    fun totalForDate(date: LocalDate): Flow<Int>

    @Query("SELECT DISTINCT date FROM water_log ORDER BY date DESC")
    fun loggedDates(): Flow<List<LocalDate>>

    @Query("SELECT date, COALESCE(SUM(amountMl),0) as total FROM water_log WHERE date BETWEEN :start AND :end GROUP BY date")
    fun totalsBetween(start: LocalDate, end: LocalDate): Flow<List<DailyTotal>>
}

data class DailyTotal(val date: LocalDate, val total: Int)
