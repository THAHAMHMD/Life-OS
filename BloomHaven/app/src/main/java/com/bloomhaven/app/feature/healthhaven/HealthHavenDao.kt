package com.bloomhaven.app.feature.healthhaven

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthHavenDao {
    // ---- Workouts ----
    @Insert
    suspend fun insertWorkout(entry: WorkoutEntity): Long

    @Update
    suspend fun updateWorkout(entry: WorkoutEntity)

    @Delete
    suspend fun deleteWorkout(entry: WorkoutEntity)

    @Query("SELECT * FROM workouts ORDER BY date DESC, createdAt DESC")
    fun allWorkouts(): Flow<List<WorkoutEntity>>

    // ---- Sleep ----
    @Insert
    suspend fun insertSleep(entry: SleepEntity): Long

    @Update
    suspend fun updateSleep(entry: SleepEntity)

    @Delete
    suspend fun deleteSleep(entry: SleepEntity)

    @Query("SELECT * FROM sleep_entries ORDER BY date DESC, createdAt DESC")
    fun allSleep(): Flow<List<SleepEntity>>

    // ---- Body measurements ----
    @Insert
    suspend fun insertMeasurement(entry: BodyMeasurementEntity): Long

    @Update
    suspend fun updateMeasurement(entry: BodyMeasurementEntity)

    @Delete
    suspend fun deleteMeasurement(entry: BodyMeasurementEntity)

    @Query("SELECT * FROM body_measurements ORDER BY date DESC, createdAt DESC")
    fun allMeasurements(): Flow<List<BodyMeasurementEntity>>

    // ---- Medical appointments ----
    @Insert
    suspend fun insertAppointment(entry: MedicalAppointmentEntity): Long

    @Update
    suspend fun updateAppointment(entry: MedicalAppointmentEntity)

    @Delete
    suspend fun deleteAppointment(entry: MedicalAppointmentEntity)

    @Query("SELECT * FROM medical_appointments ORDER BY date DESC, time DESC")
    fun allAppointments(): Flow<List<MedicalAppointmentEntity>>

    // ---- Medicines ----
    @Insert
    suspend fun insertMedicine(entry: MedicineEntity): Long

    @Update
    suspend fun updateMedicine(entry: MedicineEntity)

    @Delete
    suspend fun deleteMedicine(entry: MedicineEntity)

    @Query("SELECT * FROM medicines ORDER BY startDate DESC, createdAt DESC")
    fun allMedicines(): Flow<List<MedicineEntity>>

    // ---- Health notes ----
    @Insert
    suspend fun insertNote(entry: HealthNoteEntity): Long

    @Update
    suspend fun updateNote(entry: HealthNoteEntity)

    @Delete
    suspend fun deleteNote(entry: HealthNoteEntity)

    @Query("SELECT * FROM health_notes ORDER BY date DESC, createdAt DESC")
    fun allNotes(): Flow<List<HealthNoteEntity>>
}
