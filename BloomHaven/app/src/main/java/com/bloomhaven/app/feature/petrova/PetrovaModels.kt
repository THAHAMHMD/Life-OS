package com.bloomhaven.app.feature.petrova

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Petrova's profile — a single singleton row (id is always 1). Upserted via
 * OnConflictStrategy.REPLACE so editing the profile never creates a second row.
 */
@Entity(tableName = "petrova_profile")
data class PetrovaProfileEntity(
    @PrimaryKey val id: Long = 1,
    val name: String = "",
    val species: String = "",
    val breed: String = "",
    val birthDate: LocalDate? = null,
    val photoPath: String? = null,
)

/** A feeding log entry. Permanent unless the user deliberately deletes it. */
@Entity(tableName = "petrova_feeding")
data class PetrovaFeedingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val time: LocalTime,
    val food: String,
    val notes: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

/** A grooming session record (bath, nail trim, brushing, etc.). */
@Entity(tableName = "petrova_grooming")
data class PetrovaGroomingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val type: String,
    val notes: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

/** A vet visit record, optionally carrying a follow-up date. */
@Entity(tableName = "petrova_vet_visits")
data class PetrovaVetVisitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val reason: String,
    val notes: String,
    val nextVisitDate: LocalDate? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

/** A medicine dose or vaccination record — isVaccination distinguishes the two in one table. */
@Entity(tableName = "petrova_medicines")
data class PetrovaMedicineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val name: String,
    val isVaccination: Boolean = false,
    val notes: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

/** A single weight/health measurement, in kilograms. */
@Entity(tableName = "petrova_weight")
data class PetrovaWeightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val weightKg: Double,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

/** A Petrova-related expense. */
@Entity(tableName = "petrova_expenses")
data class PetrovaExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val amount: Double,
    val category: String,
    val note: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

/**
 * A single scrapbook entry. This one entity powers photos/memories, milestones
 * (isMilestone = true), and the growth timeline/story — the timeline is simply all
 * rows of this table ordered by date.
 */
@Entity(tableName = "petrova_memories")
data class PetrovaMemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val title: String,
    val note: String,
    val photoPath: String? = null,
    val isMilestone: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
