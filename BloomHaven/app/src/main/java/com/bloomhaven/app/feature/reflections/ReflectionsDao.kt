package com.bloomhaven.app.feature.reflections

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ReflectionsDao {
    @Insert
    suspend fun insert(entry: ReflectionEntity): Long

    @Update
    suspend fun update(entry: ReflectionEntity)

    @Delete
    suspend fun delete(entry: ReflectionEntity)

    @Query("SELECT * FROM reflections ORDER BY date DESC, createdAt DESC")
    fun all(): Flow<List<ReflectionEntity>>

    @Query("SELECT * FROM reflections WHERE date = :date ORDER BY createdAt DESC")
    fun forDate(date: LocalDate): Flow<List<ReflectionEntity>>

    @Query("SELECT DISTINCT date FROM reflections ORDER BY date DESC")
    fun entryDates(): Flow<List<LocalDate>>
}
