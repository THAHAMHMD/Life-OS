package com.bloomhaven.app.feature.petrova

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PetrovaDao {

    // --- Profile (singleton row, id = 1) ---

    @Query("SELECT * FROM petrova_profile WHERE id = 1")
    fun profile(): Flow<PetrovaProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: PetrovaProfileEntity)

    // --- Timeline / scrapbook (memories + milestones) ---

    @Insert
    suspend fun insertMemory(memory: PetrovaMemoryEntity): Long

    @Delete
    suspend fun deleteMemory(memory: PetrovaMemoryEntity)

    @Query("SELECT * FROM petrova_memories ORDER BY date DESC, createdAt DESC")
    fun memories(): Flow<List<PetrovaMemoryEntity>>

    // --- Feeding ---

    @Insert
    suspend fun insertFeeding(entry: PetrovaFeedingEntity): Long

    @Delete
    suspend fun deleteFeeding(entry: PetrovaFeedingEntity)

    @Query("SELECT * FROM petrova_feeding ORDER BY date DESC, time DESC")
    fun feedings(): Flow<List<PetrovaFeedingEntity>>

    // --- Grooming ---

    @Insert
    suspend fun insertGrooming(entry: PetrovaGroomingEntity): Long

    @Delete
    suspend fun deleteGrooming(entry: PetrovaGroomingEntity)

    @Query("SELECT * FROM petrova_grooming ORDER BY date DESC")
    fun groomings(): Flow<List<PetrovaGroomingEntity>>

    // --- Vet visits ---

    @Insert
    suspend fun insertVetVisit(entry: PetrovaVetVisitEntity): Long

    @Delete
    suspend fun deleteVetVisit(entry: PetrovaVetVisitEntity)

    @Query("SELECT * FROM petrova_vet_visits ORDER BY date DESC")
    fun vetVisits(): Flow<List<PetrovaVetVisitEntity>>

    // --- Medicines / vaccinations ---

    @Insert
    suspend fun insertMedicine(entry: PetrovaMedicineEntity): Long

    @Delete
    suspend fun deleteMedicine(entry: PetrovaMedicineEntity)

    @Query("SELECT * FROM petrova_medicines ORDER BY date DESC")
    fun medicines(): Flow<List<PetrovaMedicineEntity>>

    // --- Weight / health ---

    @Insert
    suspend fun insertWeight(entry: PetrovaWeightEntity): Long

    @Delete
    suspend fun deleteWeight(entry: PetrovaWeightEntity)

    @Query("SELECT * FROM petrova_weight ORDER BY date DESC")
    fun weights(): Flow<List<PetrovaWeightEntity>>

    // --- Expenses ---

    @Insert
    suspend fun insertExpense(entry: PetrovaExpenseEntity): Long

    @Delete
    suspend fun deleteExpense(entry: PetrovaExpenseEntity)

    @Query("SELECT * FROM petrova_expenses ORDER BY date DESC")
    fun expenses(): Flow<List<PetrovaExpenseEntity>>
}
