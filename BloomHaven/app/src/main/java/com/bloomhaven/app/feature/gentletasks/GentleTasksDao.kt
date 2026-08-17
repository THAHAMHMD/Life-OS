package com.bloomhaven.app.feature.gentletasks

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GentleTasksDao {
    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("SELECT * FROM gentle_tasks WHERE isCompleted = 0 ORDER BY (dueDate IS NULL), dueDate ASC, createdAt ASC")
    fun incompleteOnly(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM gentle_tasks WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun completedOnly(): Flow<List<TaskEntity>>
}
