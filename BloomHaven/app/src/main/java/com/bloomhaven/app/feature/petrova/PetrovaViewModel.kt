package com.bloomhaven.app.feature.petrova

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

data class PetrovaUiState(
    val profile: PetrovaProfileEntity = PetrovaProfileEntity(),
    val memories: List<PetrovaMemoryEntity> = emptyList(),
    val feedings: List<PetrovaFeedingEntity> = emptyList(),
    val groomings: List<PetrovaGroomingEntity> = emptyList(),
    val vetVisits: List<PetrovaVetVisitEntity> = emptyList(),
    val medicines: List<PetrovaMedicineEntity> = emptyList(),
    val weights: List<PetrovaWeightEntity> = emptyList(),
    val expenses: List<PetrovaExpenseEntity> = emptyList(),
)

/** First half of the combined state — kotlinx combine tops out at 5 flows per call. */
private data class ProfileAndTimeline(
    val profile: PetrovaProfileEntity,
    val memories: List<PetrovaMemoryEntity>,
    val feedings: List<PetrovaFeedingEntity>,
    val groomings: List<PetrovaGroomingEntity>,
    val vetVisits: List<PetrovaVetVisitEntity>,
)

private data class HealthAndMoney(
    val medicines: List<PetrovaMedicineEntity>,
    val weights: List<PetrovaWeightEntity>,
    val expenses: List<PetrovaExpenseEntity>,
)

class PetrovaViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PetrovaRepository(PetrovaDatabase.getInstance(application).petrovaDao())

    private val profileAndTimeline = combine(
        repo.profile().map { it ?: PetrovaProfileEntity() },
        repo.memories(),
        repo.feedings(),
        repo.groomings(),
        repo.vetVisits(),
    ) { profile, memories, feedings, groomings, vetVisits ->
        ProfileAndTimeline(profile, memories, feedings, groomings, vetVisits)
    }

    private val healthAndMoney = combine(
        repo.medicines(),
        repo.weights(),
        repo.expenses(),
    ) { medicines, weights, expenses ->
        HealthAndMoney(medicines, weights, expenses)
    }

    val uiState: StateFlow<PetrovaUiState> = combine(
        profileAndTimeline,
        healthAndMoney,
    ) { part1, part2 ->
        PetrovaUiState(
            profile = part1.profile,
            memories = part1.memories,
            feedings = part1.feedings,
            groomings = part1.groomings,
            vetVisits = part1.vetVisits,
            medicines = part2.medicines,
            weights = part2.weights,
            expenses = part2.expenses,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PetrovaUiState())

    // --- Profile ---

    fun saveProfile(name: String, species: String, breed: String, birthDate: LocalDate?, photoPath: String?) {
        viewModelScope.launch { repo.saveProfile(name, species, breed, birthDate, photoPath) }
    }

    // --- Timeline / scrapbook ---

    fun addMemory(date: LocalDate, title: String, note: String, photoPath: String?, isMilestone: Boolean) {
        viewModelScope.launch { repo.addMemory(date, title, note, photoPath, isMilestone) }
    }

    fun deleteMemory(memory: PetrovaMemoryEntity) {
        viewModelScope.launch { repo.deleteMemory(memory) }
    }

    // --- Feeding ---

    fun addFeeding(date: LocalDate, time: LocalTime, food: String, notes: String) {
        viewModelScope.launch { repo.addFeeding(date, time, food, notes) }
    }

    fun deleteFeeding(entry: PetrovaFeedingEntity) {
        viewModelScope.launch { repo.deleteFeeding(entry) }
    }

    // --- Grooming ---

    fun addGrooming(date: LocalDate, type: String, notes: String) {
        viewModelScope.launch { repo.addGrooming(date, type, notes) }
    }

    fun deleteGrooming(entry: PetrovaGroomingEntity) {
        viewModelScope.launch { repo.deleteGrooming(entry) }
    }

    // --- Vet visits ---

    fun addVetVisit(date: LocalDate, reason: String, notes: String, nextVisitDate: LocalDate?) {
        viewModelScope.launch { repo.addVetVisit(date, reason, notes, nextVisitDate) }
    }

    fun deleteVetVisit(entry: PetrovaVetVisitEntity) {
        viewModelScope.launch { repo.deleteVetVisit(entry) }
    }

    // --- Medicines / vaccinations ---

    fun addMedicine(date: LocalDate, name: String, isVaccination: Boolean, notes: String) {
        viewModelScope.launch { repo.addMedicine(date, name, isVaccination, notes) }
    }

    fun deleteMedicine(entry: PetrovaMedicineEntity) {
        viewModelScope.launch { repo.deleteMedicine(entry) }
    }

    // --- Weight / health ---

    fun addWeight(date: LocalDate, weightKg: Double) {
        viewModelScope.launch { repo.addWeight(date, weightKg) }
    }

    fun deleteWeight(entry: PetrovaWeightEntity) {
        viewModelScope.launch { repo.deleteWeight(entry) }
    }

    // --- Expenses ---

    fun addExpense(date: LocalDate, amount: Double, category: String, note: String) {
        viewModelScope.launch { repo.addExpense(date, amount, category, note) }
    }

    fun deleteExpense(entry: PetrovaExpenseEntity) {
        viewModelScope.launch { repo.deleteExpense(entry) }
    }
}
