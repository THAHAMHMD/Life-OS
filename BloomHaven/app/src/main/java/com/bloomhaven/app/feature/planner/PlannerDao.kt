package com.bloomhaven.app.feature.planner

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PlannerDao {
    @Insert
    suspend fun insert(item: PlannerItemEntity): Long

    @Insert
    suspend fun insertAll(items: List<PlannerItemEntity>): List<Long>

    @Update
    suspend fun update(item: PlannerItemEntity)

    @Delete
    suspend fun delete(item: PlannerItemEntity)

    @Query("SELECT * FROM planner_item WHERE date = :date ORDER BY time IS NULL, time ASC")
    fun forDate(date: LocalDate): Flow<List<PlannerItemEntity>>

    @Query("SELECT * FROM planner_item WHERE date BETWEEN :start AND :end ORDER BY date ASC, time IS NULL, time ASC")
    fun between(start: LocalDate, end: LocalDate): Flow<List<PlannerItemEntity>>

    @Query("SELECT * FROM planner_item WHERE date >= :fromDate ORDER BY date ASC, time IS NULL, time ASC LIMIT :limit")
    fun upcoming(fromDate: LocalDate, limit: Int): Flow<List<PlannerItemEntity>>

    @Query("SELECT DISTINCT date FROM planner_item WHERE date BETWEEN :start AND :end")
    fun datesWithItems(start: LocalDate, end: LocalDate): Flow<List<LocalDate>>
}
