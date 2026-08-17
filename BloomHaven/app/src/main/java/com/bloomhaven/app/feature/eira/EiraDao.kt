package com.bloomhaven.app.feature.eira

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EiraDao {
    @Insert
    suspend fun insert(message: EiraMessageEntity): Long

    @Query("SELECT * FROM eira_message ORDER BY timestamp DESC LIMIT 100")
    fun recent(): Flow<List<EiraMessageEntity>>

    @Query("SELECT * FROM eira_message ORDER BY timestamp DESC LIMIT 1")
    suspend fun latest(): EiraMessageEntity?
}
