package com.bloomhaven.app.feature.healthhaven

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

data class HealthHavenUiState(
    val workouts: List<WorkoutEntity> = emptyList(),
    val sleepEntries: List<SleepEntity> = emptyList(),
    val measurements: List<BodyMeasurementEntity> = emptyList(),
    val appointments: List<MedicalAppointmentEntity> = emptyList(),
    val medicines: List<MedicineEntity> = emptyList(),
    val notes: List<HealthNoteEntity> = emptyList(),
)

class HealthHavenViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = HealthHavenRepository(application, HealthHavenDatabase.getInstance(application).healthHavenDao())

    private val workoutsMeasurementsSleep = combine(
        repo.workouts(),
        repo.sleepEntries(),
        repo.measurements(),
    ) { workouts, sleep, measurements -> Triple(workouts, sleep, measurements) }

    private val appointmentsMedicinesNotes = combine(
        repo.appointments(),
        repo.medicines(),
        repo.notes(),
    ) { appointments, medicines, notes -> Triple(appointments, medicines, notes) }

    val uiState: StateFlow<HealthHavenUiState> = combine(
        workoutsMeasurementsSleep,
        appointmentsMedicinesNotes,
    ) { (workouts, sleep, measurements), (appointments, medicines, notes) ->
        HealthHavenUiState(
            workouts = workouts,
            sleepEntries = sleep,
            measurements = measurements,
            appointments = appointments,
            medicines = medicines,
            notes = notes,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HealthHavenUiState())

    // ---- Workouts ----
    fun addWorkout(date: LocalDate, type: String, durationMinutes: Int, notes: String) {
        viewModelScope.launch { repo.addWorkout(date, type, durationMinutes, notes) }
    }

    fun updateWorkout(entry: WorkoutEntity, date: LocalDate, type: String, durationMinutes: Int, notes: String) {
        viewModelScope.launch { repo.updateWorkout(entry, date, type, durationMinutes, notes) }
    }

    fun deleteWorkout(entry: WorkoutEntity) {
        viewModelScope.launch { repo.deleteWorkout(entry) }
    }

    // ---- Sleep ----
    fun addSleep(date: LocalDate, hours: Double, quality: String, notes: String) {
        viewModelScope.launch { repo.addSleep(date, hours, quality, notes) }
    }

    fun updateSleep(entry: SleepEntity, date: LocalDate, hours: Double, quality: String, notes: String) {
        viewModelScope.launch { repo.updateSleep(entry, date, hours, quality, notes) }
    }

    fun deleteSleep(entry: SleepEntity) {
        viewModelScope.launch { repo.deleteSleep(entry) }
    }

    // ---- Body measurements ----
    fun addMeasurement(date: LocalDate, weightKg: Double?, heightCm: Double?, notes: String) {
        viewModelScope.launch { repo.addMeasurement(date, weightKg, heightCm, notes) }
    }

    fun updateMeasurement(entry: BodyMeasurementEntity, date: LocalDate, weightKg: Double?, heightCm: Double?, notes: String) {
        viewModelScope.launch { repo.updateMeasurement(entry, date, weightKg, heightCm, notes) }
    }

    fun deleteMeasurement(entry: BodyMeasurementEntity) {
        viewModelScope.launch { repo.deleteMeasurement(entry) }
    }

    // ---- Medical appointments ----
    fun addAppointment(date: LocalDate, time: LocalTime?, title: String, doctorOrClinic: String, notes: String, reminderEnabled: Boolean) {
        viewModelScope.launch { repo.addAppointment(date, time, title, doctorOrClinic, notes, reminderEnabled) }
    }

    fun updateAppointment(
        entry: MedicalAppointmentEntity,
        date: LocalDate,
        time: LocalTime?,
        title: String,
        doctorOrClinic: String,
        notes: String,
        reminderEnabled: Boolean,
    ) {
        viewModelScope.launch { repo.updateAppointment(entry, date, time, title, doctorOrClinic, notes, reminderEnabled) }
    }

    fun deleteAppointment(entry: MedicalAppointmentEntity) {
        viewModelScope.launch { repo.deleteAppointment(entry) }
    }

    // ---- Medicines ----
    fun addMedicine(name: String, dosage: String, schedule: String, startDate: LocalDate, endDate: LocalDate?, notes: String, isActive: Boolean) {
        viewModelScope.launch { repo.addMedicine(name, dosage, schedule, startDate, endDate, notes, isActive) }
    }

    fun updateMedicine(
        entry: MedicineEntity,
        name: String,
        dosage: String,
        schedule: String,
        startDate: LocalDate,
        endDate: LocalDate?,
        notes: String,
        isActive: Boolean,
    ) {
        viewModelScope.launch { repo.updateMedicine(entry, name, dosage, schedule, startDate, endDate, notes, isActive) }
    }

    fun deleteMedicine(entry: MedicineEntity) {
        viewModelScope.launch { repo.deleteMedicine(entry) }
    }

    // ---- Health notes ----
    fun addNote(date: LocalDate, note: String) {
        viewModelScope.launch { repo.addNote(date, note) }
    }

    fun updateNote(entry: HealthNoteEntity, date: LocalDate, note: String) {
        viewModelScope.launch { repo.updateNote(entry, date, note) }
    }

    fun deleteNote(entry: HealthNoteEntity) {
        viewModelScope.launch { repo.deleteNote(entry) }
    }
}
