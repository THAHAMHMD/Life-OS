package com.bloomhaven.app.core.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bloomhaven.app.core.data.AppSettingsRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalTime

/**
 * Generic gentle-nudge worker. Any module schedules one of these via [ReminderScheduler]
 * with a title/body; quiet hours are honoured before the notification is actually shown.
 */
class ReminderWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "Bloom Haven"
        val body = inputData.getString(KEY_BODY) ?: ""
        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, 0)

        val settingsRepo = AppSettingsRepository(applicationContext)
        val settings = settingsRepo.settings.firstOrNull()

        if (settings?.notificationsEnabled == false) return Result.success()

        val quiet = settings?.let {
            val now = LocalTime.now()
            val nowMin = now.hour * 60 + now.minute
            val start = parseMinutes(it.quietHoursStart)
            val end = parseMinutes(it.quietHoursEnd)
            NotificationHelper.isQuietHours(nowMin, start, end)
        } ?: false

        if (!quiet) {
            NotificationHelper.show(applicationContext, notificationId, title, body)
        }
        return Result.success()
    }

    private fun parseMinutes(hhmm: String): Int {
        val parts = hhmm.split(":")
        return (parts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (parts.getOrNull(1)?.toIntOrNull() ?: 0)
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_NOTIFICATION_ID = "notification_id"
    }
}
