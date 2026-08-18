package com.bloomhaven.app.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bloomhaven.app.MainActivity
import com.bloomhaven.app.R

object NotificationHelper {
    const val CHANNEL_GENTLE_NUDGES = "gentle_nudges"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_GENTLE_NUDGES,
                "Gentle Nudges",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Soft, supportive reminders from Bloom Haven — never urgent."
                enableVibration(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun show(context: Context, id: Int, title: String, body: String) {
        ensureChannels(context)
        val intent = android.content.Intent(context, MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context, id, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_GENTLE_NUDGES)
            .setSmallIcon(R.drawable.ic_bloom_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            NotificationManagerCompat.from(context).notify(id, notification)
        }
    }

    /** True while the current time falls inside the user's configured quiet hours. */
    fun isQuietHours(nowMinutes: Int, startMinutes: Int, endMinutes: Int): Boolean {
        return if (startMinutes <= endMinutes) {
            nowMinutes in startMinutes until endMinutes
        } else {
            nowMinutes >= startMinutes || nowMinutes < endMinutes
        }
    }
}
