package com.bloomhaven.app.feature.healthhaven

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * A logged workout / activity session (free-text type, e.g. "Run", "Gym", "Yoga").
 *
 * `source` defaults to "manual" and future-proofs this table for wearable-imported records
 * (e.g. "wearable") without ever overwriting what the person entered by hand. No wearable
 * integration is implemented yet — the app must work fully with manual entry alone.
 */
@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val type: String,
    val durationMinutes: Int,
    val notes: String = "",
    val source: String = "manual",
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

/** One night's sleep entry. `quality` is one of the [SleepQuality.OPTIONS] labels. */
@Entity(tableName = "sleep_entries")
data class SleepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val hours: Double,
    val quality: String,
    val notes: String = "",
    val source: String = "manual",
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

/** The fixed set of sleep-quality labels offered in the Sleep form. */
object SleepQuality {
    val OPTIONS = listOf("Poor", "Okay", "Good", "Great")
}

/**
 * A body-measurement snapshot. Weight history is simply this table queried and displayed
 * ordered by date descending — no separate entity or chart is needed for that.
 */
@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val weightKg: Double? = null,
    val heightCm: Double? = null,
    val notes: String = "",
    val source: String = "manual",
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

/** A medical appointment or checkup, past or upcoming, with an optional local reminder flag. */
@Entity(tableName = "medical_appointments")
data class MedicalAppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val time: LocalTime? = null,
    val title: String,
    val doctorOrClinic: String = "",
    val notes: String = "",
    val reminderEnabled: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

/** A medicine or supplement course, current or historical. */
@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosage: String = "",
    val schedule: String = "",
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

/** A free-form health note or observation, not tied to any other record type. */
@Entity(tableName = "health_notes")
data class HealthNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val note: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
