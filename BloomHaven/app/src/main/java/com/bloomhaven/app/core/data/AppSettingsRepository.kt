package com.bloomhaven.app.core.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.bloomDataStore by preferencesDataStore(name = "bloom_haven_settings")

enum class NightHavenMode { AUTO, ALWAYS, NEVER }

/** Which Home sections the user has chosen to keep prominent, in display order. */
val DEFAULT_HOME_SECTIONS = listOf(
    "eira_greeting", "today_schedule", "hydration", "today_tasks", "upcoming_events",
)

data class AppSettings(
    val userName: String = "",
    val workScheduleNote: String = "",
    val nightHavenMode: NightHavenMode = NightHavenMode.AUTO,
    val reducedMotion: Boolean = false,
    val homeSections: List<String> = DEFAULT_HOME_SECTIONS,
    val waterDailyGoalMl: Int = 2000,
    val biometricLockEnabled: Boolean = false,
    val onlineAiConsent: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val quietHoursStart: String = "21:30",
    val quietHoursEnd: String = "07:30",
    val onboardingComplete: Boolean = false,
)

class AppSettingsRepository(private val context: Context) {
    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val WORK_SCHEDULE_NOTE = stringPreferencesKey("work_schedule_note")
        val NIGHT_HAVEN_MODE = stringPreferencesKey("night_haven_mode")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val HOME_SECTIONS = stringPreferencesKey("home_sections")
        val WATER_GOAL = intPreferencesKey("water_daily_goal_ml")
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock_enabled")
        val ONLINE_AI_CONSENT = booleanPreferencesKey("online_ai_consent")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val QUIET_START = stringPreferencesKey("quiet_hours_start")
        val QUIET_END = stringPreferencesKey("quiet_hours_end")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }

    val settings: Flow<AppSettings> = context.bloomDataStore.data.map { prefs ->
        AppSettings(
            userName = prefs[Keys.USER_NAME] ?: "",
            workScheduleNote = prefs[Keys.WORK_SCHEDULE_NOTE] ?: "",
            nightHavenMode = prefs[Keys.NIGHT_HAVEN_MODE]?.let {
                runCatching { NightHavenMode.valueOf(it) }.getOrNull()
            } ?: NightHavenMode.AUTO,
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
            homeSections = prefs[Keys.HOME_SECTIONS]?.split(",")?.filter { it.isNotBlank() }
                ?: DEFAULT_HOME_SECTIONS,
            waterDailyGoalMl = prefs[Keys.WATER_GOAL] ?: 2000,
            biometricLockEnabled = prefs[Keys.BIOMETRIC_LOCK] ?: false,
            onlineAiConsent = prefs[Keys.ONLINE_AI_CONSENT] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            quietHoursStart = prefs[Keys.QUIET_START] ?: "21:30",
            quietHoursEnd = prefs[Keys.QUIET_END] ?: "07:30",
            onboardingComplete = prefs[Keys.ONBOARDING_COMPLETE] ?: false,
        )
    }

    suspend fun setUserName(name: String) = context.bloomDataStore.edit { it[Keys.USER_NAME] = name }
    suspend fun setWorkScheduleNote(note: String) = context.bloomDataStore.edit { it[Keys.WORK_SCHEDULE_NOTE] = note }
    suspend fun setNightHavenMode(mode: NightHavenMode) = context.bloomDataStore.edit { it[Keys.NIGHT_HAVEN_MODE] = mode.name }
    suspend fun setReducedMotion(enabled: Boolean) = context.bloomDataStore.edit { it[Keys.REDUCED_MOTION] = enabled }
    suspend fun setHomeSections(sections: List<String>) = context.bloomDataStore.edit { it[Keys.HOME_SECTIONS] = sections.joinToString(",") }
    suspend fun setWaterDailyGoal(ml: Int) = context.bloomDataStore.edit { it[Keys.WATER_GOAL] = ml }
    suspend fun setBiometricLockEnabled(enabled: Boolean) = context.bloomDataStore.edit { it[Keys.BIOMETRIC_LOCK] = enabled }
    suspend fun setOnlineAiConsent(enabled: Boolean) = context.bloomDataStore.edit { it[Keys.ONLINE_AI_CONSENT] = enabled }
    suspend fun setNotificationsEnabled(enabled: Boolean) = context.bloomDataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    suspend fun setQuietHours(start: String, end: String) = context.bloomDataStore.edit {
        it[Keys.QUIET_START] = start
        it[Keys.QUIET_END] = end
    }
    suspend fun setOnboardingComplete(complete: Boolean) = context.bloomDataStore.edit { it[Keys.ONBOARDING_COMPLETE] = complete }
}
