package com.bloomhaven.app.feature.myhaven

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bloomhaven.app.core.data.AppSettings
import com.bloomhaven.app.core.data.AppSettingsRepository
import com.bloomhaven.app.core.data.NightHavenMode
import com.bloomhaven.app.feature.commitments.CommitmentsDatabase
import com.bloomhaven.app.feature.eira.EiraDatabase
import com.bloomhaven.app.feature.flow.FlowDatabase
import com.bloomhaven.app.feature.gentletasks.GentleTasksDatabase
import com.bloomhaven.app.feature.habits.HabitsDatabase
import com.bloomhaven.app.feature.healthhaven.HealthHavenDatabase
import com.bloomhaven.app.feature.moneyhaven.MoneyDatabase
import com.bloomhaven.app.feature.petrova.PetrovaDatabase
import com.bloomhaven.app.feature.planner.PlannerDatabase
import com.bloomhaven.app.feature.reflections.ReflectionsDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyHavenViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepo = AppSettingsRepository(application)

    val settings = settingsRepo.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setUserName(name: String) = viewModelScope.launch { settingsRepo.setUserName(name) }
    fun setWorkScheduleNote(note: String) = viewModelScope.launch { settingsRepo.setWorkScheduleNote(note) }
    fun setNightHavenMode(mode: NightHavenMode) = viewModelScope.launch { settingsRepo.setNightHavenMode(mode) }
    fun setReducedMotion(enabled: Boolean) = viewModelScope.launch { settingsRepo.setReducedMotion(enabled) }
    fun setWaterGoal(ml: Int) = viewModelScope.launch { settingsRepo.setWaterDailyGoal(ml) }
    fun setBiometricLock(enabled: Boolean) = viewModelScope.launch { settingsRepo.setBiometricLockEnabled(enabled) }
    fun setOnlineAiConsent(enabled: Boolean) = viewModelScope.launch { settingsRepo.setOnlineAiConsent(enabled) }
    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepo.setNotificationsEnabled(enabled) }
    fun setQuietHours(start: String, end: String) = viewModelScope.launch { settingsRepo.setQuietHours(start, end) }

    fun toggleHomeSection(section: String) = viewModelScope.launch {
        val current = settings.value.homeSections
        val updated = if (current.contains(section)) current - section else current + section
        settingsRepo.setHomeSections(updated)
    }

    fun moveHomeSection(section: String, delta: Int) = viewModelScope.launch {
        val current = settings.value.homeSections.toMutableList()
        val index = current.indexOf(section)
        if (index < 0) return@launch
        val newIndex = (index + delta).coerceIn(0, current.size - 1)
        current.removeAt(index)
        current.add(newIndex, section)
        settingsRepo.setHomeSections(current)
    }

    /** Permanently clears every Bloom Haven module's local database — used only after explicit double confirmation. */
    fun resetAllData(onDone: () -> Unit) {
        viewModelScope.launch {
            val app = getApplication<android.app.Application>()
            listOf(
                MoneyDatabase.getInstance(app),
                PlannerDatabase.getInstance(app),
                GentleTasksDatabase.getInstance(app),
                HabitsDatabase.getInstance(app),
                FlowDatabase.getInstance(app),
                ReflectionsDatabase.getInstance(app),
                HealthHavenDatabase.getInstance(app),
                CommitmentsDatabase.getInstance(app),
                PetrovaDatabase.getInstance(app),
                EiraDatabase.getInstance(app),
            ).forEach { db -> db.clearAllTables() }
            onDone()
        }
    }
}
