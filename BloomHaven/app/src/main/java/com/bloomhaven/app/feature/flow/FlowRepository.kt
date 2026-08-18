package com.bloomhaven.app.feature.flow

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class FlowRepository(private val dao: FlowDao) {
    fun forDate(date: LocalDate): Flow<List<WaterLogEntity>> = dao.forDate(date)
    fun totalForDate(date: LocalDate): Flow<Int> = dao.totalForDate(date)
    fun loggedDates(): Flow<List<LocalDate>> = dao.loggedDates()
    fun totalsBetween(start: LocalDate, end: LocalDate): Flow<List<DailyTotal>> = dao.totalsBetween(start, end)

    suspend fun log(date: LocalDate, amountMl: Int, time: java.time.LocalTime = java.time.LocalTime.now()) {
        dao.insert(WaterLogEntity(date = date, amountMl = amountMl, time = time))
    }

    suspend fun delete(entry: WaterLogEntity) = dao.delete(entry)
    suspend fun update(entry: WaterLogEntity) = dao.update(entry)
}
