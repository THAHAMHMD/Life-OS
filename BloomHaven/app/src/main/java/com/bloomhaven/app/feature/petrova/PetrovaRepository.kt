package com.bloomhaven.app.feature.petrova

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

class PetrovaRepository(private val dao: PetrovaDao) {

    // --- Profile ---

    fun profile(): Flow<PetrovaProfileEntity?> = dao.profile()

    suspend fun saveProfile(
        name: String,
        species: String,
        breed: String,
        birthDate: LocalDate?,
        photoPath: String?,
    ) {
        dao.upsertProfile(
            PetrovaProfileEntity(
                id = 1,
                name = name,
                species = species,
                breed = breed,
                birthDate = birthDate,
                photoPath = photoPath,
            ),
        )
    }

    // --- Timeline / scrapbook ---

    fun memories(): Flow<List<PetrovaMemoryEntity>> = dao.memories()

    suspend fun addMemory(date: LocalDate, title: String, note: String, photoPath: String?, isMilestone: Boolean) {
        dao.insertMemory(
            PetrovaMemoryEntity(date = date, title = title, note = note, photoPath = photoPath, isMilestone = isMilestone),
        )
    }

    suspend fun deleteMemory(memory: PetrovaMemoryEntity) = dao.deleteMemory(memory)

    // --- Feeding ---

    fun feedings(): Flow<List<PetrovaFeedingEntity>> = dao.feedings()

    suspend fun addFeeding(date: LocalDate, time: LocalTime, food: String, notes: String) {
        dao.insertFeeding(PetrovaFeedingEntity(date = date, time = time, food = food, notes = notes))
    }

    suspend fun deleteFeeding(entry: PetrovaFeedingEntity) = dao.deleteFeeding(entry)

    // --- Grooming ---

    fun groomings(): Flow<List<PetrovaGroomingEntity>> = dao.groomings()

    suspend fun addGrooming(date: LocalDate, type: String, notes: String) {
        dao.insertGrooming(PetrovaGroomingEntity(date = date, type = type, notes = notes))
    }

    suspend fun deleteGrooming(entry: PetrovaGroomingEntity) = dao.deleteGrooming(entry)

    // --- Vet visits ---

    fun vetVisits(): Flow<List<PetrovaVetVisitEntity>> = dao.vetVisits()

    suspend fun addVetVisit(date: LocalDate, reason: String, notes: String, nextVisitDate: LocalDate?) {
        dao.insertVetVisit(PetrovaVetVisitEntity(date = date, reason = reason, notes = notes, nextVisitDate = nextVisitDate))
    }

    suspend fun deleteVetVisit(entry: PetrovaVetVisitEntity) = dao.deleteVetVisit(entry)

    // --- Medicines / vaccinations ---

    fun medicines(): Flow<List<PetrovaMedicineEntity>> = dao.medicines()

    suspend fun addMedicine(date: LocalDate, name: String, isVaccination: Boolean, notes: String) {
        dao.insertMedicine(PetrovaMedicineEntity(date = date, name = name, isVaccination = isVaccination, notes = notes))
    }

    suspend fun deleteMedicine(entry: PetrovaMedicineEntity) = dao.deleteMedicine(entry)

    // --- Weight / health ---

    fun weights(): Flow<List<PetrovaWeightEntity>> = dao.weights()

    suspend fun addWeight(date: LocalDate, weightKg: Double) {
        dao.insertWeight(PetrovaWeightEntity(date = date, weightKg = weightKg))
    }

    suspend fun deleteWeight(entry: PetrovaWeightEntity) = dao.deleteWeight(entry)

    // --- Expenses ---

    fun expenses(): Flow<List<PetrovaExpenseEntity>> = dao.expenses()

    suspend fun addExpense(date: LocalDate, amount: Double, category: String, note: String) {
        dao.insertExpense(PetrovaExpenseEntity(date = date, amount = amount, category = category, note = note))
    }

    suspend fun deleteExpense(entry: PetrovaExpenseEntity) = dao.deleteExpense(entry)
}
