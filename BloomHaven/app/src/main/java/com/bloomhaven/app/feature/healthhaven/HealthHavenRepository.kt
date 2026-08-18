package com.bloomhaven.app.feature.healthhaven

import android.content.Context
import com.bloomhaven.app.core.notifications.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Wraps [HealthHavenDao] for all six Health Haven record types. All history is permanent. */
class HealthHavenRepository(private val context: Context, private val dao: HealthHavenDao) {

    // ---- Workouts ----
    fun workouts(): Flow<List<WorkoutEntity>> = dao.allWorkouts()

    suspend fun addWorkout(date: LocalDate, type: String, durationMinutes: Int, notes: String) {
        dao.insertWorkout(WorkoutEntity(date = date, type = type, durationMinutes = durationMinutes, notes = notes))
    }

    suspend fun updateWorkout(entry: WorkoutEntity, date: LocalDate, type: String, durationMinutes: Int, notes: String) {
        dao.updateWorkout(entry.copy(date = date, type = type, durationMinutes = durationMinutes, notes = notes))
    }

    suspend fun deleteWorkout(entry: WorkoutEntity) = dao.deleteWorkout(entry)

    // ---- Sleep ----
    fun sleepEntries(): Flow<List<SleepEntity>> = dao.allSleep()

    suspend fun addSleep(date: LocalDate, hours: Double, quality: String, notes: String) {
        dao.insertSleep(SleepEntity(date = date, hours = hours, quality = quality, notes = notes))
    }

    suspend fun updateSleep(entry: SleepEntity, date: LocalDate, hours: Double, quality: String, notes: String) {
        dao.updateSleep(entry.copy(date = date, hours = hours, quality = quality, notes = notes))
    }

    suspend fun deleteSleep(entry: SleepEntity) = dao.deleteSleep(entry)

    // ---- Body measurements ----
    fun measurements(): Flow<List<BodyMeasurementEntity>> = dao.allMeasurements()

    suspend fun addMeasurement(date: LocalDate, weightKg: Double?, heightCm: Double?, notes: String) {
        dao.insertMeasurement(BodyMeasurementEntity(date = date, weightKg = weightKg, heightCm = heightCm, notes = notes))
    }

    suspend fun updateMeasurement(entry: BodyMeasurementEntity, date: LocalDate, weightKg: Double?, heightCm: Double?, notes: String) {
        dao.updateMeasurement(entry.copy(date = date, weightKg = weightKg, heightCm = heightCm, notes = notes))
    }

    suspend fun deleteMeasurement(entry: BodyMeasurementEntity) = dao.deleteMeasurement(entry)

    // ---- Medical appointments ----
    fun appointments(): Flow<List<MedicalAppointmentEntity>> = dao.allAppointments()

    suspend fun addAppointment(
        date: LocalDate,
        time: LocalTime?,
        title: String,
        doctorOrClinic: String,
        notes: String,
        reminderEnabled: Boolean,
    ) {
        val id = dao.insertAppointment(
            MedicalAppointmentEntity(
                date = date,
                time = time,
                title = title,
                doctorOrClinic = doctorOrClinic,
                notes = notes,
                reminderEnabled = reminderEnabled,
            ),
        )
        scheduleAppointmentReminder(id, date, time, title, doctorOrClinic, reminderEnabled)
    }

    suspend fun updateAppointment(
        entry: MedicalAppointmentEntity,
        date: LocalDate,
        time: LocalTime?,
        title: String,
        doctorOrClinic: String,
        notes: String,
        reminderEnabled: Boolean,
    ) {
        dao.updateAppointment(
            entry.copy(
                date = date,
                time = time,
                title = title,
                doctorOrClinic = doctorOrClinic,
                notes = notes,
                reminderEnabled = reminderEnabled,
            ),
        )
        scheduleAppointmentReminder(entry.id, date, time, title, doctorOrClinic, reminderEnabled)
    }

    suspend fun deleteAppointment(entry: MedicalAppointmentEntity) {
        dao.deleteAppointment(entry)
        ReminderScheduler.cancel(context, "health_appointment_${entry.id}")
    }

    private fun scheduleAppointmentReminder(
        id: Long,
        date: LocalDate,
        time: LocalTime?,
        title: String,
        doctorOrClinic: String,
        reminderEnabled: Boolean,
    ) {
        val uniqueName = "health_appointment_$id"
        if (reminderEnabled) {
            ReminderScheduler.schedule(
                context = context,
                uniqueName = uniqueName,
                notificationId = uniqueName.hashCode(),
                title = title,
                body = if (doctorOrClinic.isNotBlank()) "With $doctorOrClinic" else "Appointment reminder",
                at = LocalDateTime.of(date, time ?: LocalTime.of(9, 0)),
            )
        } else {
            ReminderScheduler.cancel(context, uniqueName)
        }
    }

    // ---- Medicines ----
    fun medicines(): Flow<List<MedicineEntity>> = dao.allMedicines()

    suspend fun addMedicine(
        name: String,
        dosage: String,
        schedule: String,
        startDate: LocalDate,
        endDate: LocalDate?,
        notes: String,
        isActive: Boolean,
    ) {
        dao.insertMedicine(
            MedicineEntity(
                name = name,
                dosage = dosage,
                schedule = schedule,
                startDate = startDate,
                endDate = endDate,
                notes = notes,
                isActive = isActive,
            ),
        )
    }

    suspend fun updateMedicine(
        entry: MedicineEntity,
        name: String,
        dosage: String,
        schedule: String,
        startDate: LocalDate,
        endDate: LocalDate?,
        notes: String,
        isActive: Boolean,
    ) {
        dao.updateMedicine(
            entry.copy(
                name = name,
                dosage = dosage,
                schedule = schedule,
                startDate = startDate,
                endDate = endDate,
                notes = notes,
                isActive = isActive,
            ),
        )
    }

    suspend fun deleteMedicine(entry: MedicineEntity) = dao.deleteMedicine(entry)

    // ---- Health notes ----
    fun notes(): Flow<List<HealthNoteEntity>> = dao.allNotes()

    suspend fun addNote(date: LocalDate, note: String) {
        dao.insertNote(HealthNoteEntity(date = date, note = note))
    }

    suspend fun updateNote(entry: HealthNoteEntity, date: LocalDate, note: String) {
        dao.updateNote(entry.copy(date = date, note = note))
    }

    suspend fun deleteNote(entry: HealthNoteEntity) = dao.deleteNote(entry)
}
