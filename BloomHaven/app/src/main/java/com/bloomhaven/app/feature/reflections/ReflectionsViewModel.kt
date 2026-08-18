package com.bloomhaven.app.feature.reflections

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ReflectionsUiState(
    val entries: List<ReflectionEntity> = emptyList(),
)

class ReflectionsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ReflectionsRepository(ReflectionsDatabase.getInstance(application).reflectionsDao())

    val uiState: StateFlow<ReflectionsUiState> = repo.all()
        .map { ReflectionsUiState(entries = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReflectionsUiState())

    fun addReflection(date: LocalDate, mood: Mood, text: String) {
        viewModelScope.launch { repo.add(date, mood, text) }
    }

    fun updateReflection(entry: ReflectionEntity, date: LocalDate, mood: Mood, text: String) {
        viewModelScope.launch { repo.update(entry, date, mood, text) }
    }

    fun deleteReflection(entry: ReflectionEntity) {
        viewModelScope.launch { repo.delete(entry) }
    }
}
