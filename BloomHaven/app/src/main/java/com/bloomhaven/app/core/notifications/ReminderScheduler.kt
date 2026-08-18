package com.bloomhaven.app.core.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Thin wrapper feature modules use to schedule/cancel a single Gentle Nudge without
 * touching WorkManager APIs directly. Each reminder gets a stable [uniqueName] so
 * rescheduling (e.g. after an edit) simply replaces the previous request.
 */
object ReminderScheduler {

    fun schedule(
        context: Context,
        uniqueName: String,
        notificationId: Int,
        title: String,
        body: String,
        at: LocalDateTime,
    ) {
        val now = LocalDateTime.now()
        if (at.isBefore(now)) return
        val delay = Duration.between(now, at)
        val data = Data.Builder()
            .putString(ReminderWorker.KEY_TITLE, title)
            .putString(ReminderWorker.KEY_BODY, body)
            .putInt(ReminderWorker.KEY_NOTIFICATION_ID, notificationId)
            .build()
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context, uniqueName: String) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName)
    }
}
