package com.bloomhaven.app.feature.reflections

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

class ReflectionsRepository(private val dao: ReflectionsDao) {
    fun all(): Flow<List<ReflectionEntity>> = dao.all()
    fun forDate(date: LocalDate): Flow<List<ReflectionEntity>> = dao.forDate(date)
    fun entryDates(): Flow<List<LocalDate>> = dao.entryDates()

    suspend fun add(date: LocalDate, mood: Mood, text: String) {
        val now = LocalDateTime.now()
        dao.insert(ReflectionEntity(date = date, mood = mood, text = text, createdAt = now, updatedAt = now))
    }

    suspend fun update(entry: ReflectionEntity, date: LocalDate, mood: Mood, text: String) {
        dao.update(entry.copy(date = date, mood = mood, text = text, updatedAt = LocalDateTime.now()))
    }

    suspend fun delete(entry: ReflectionEntity) = dao.delete(entry)
}
