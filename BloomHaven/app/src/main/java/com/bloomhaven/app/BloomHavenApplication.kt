package com.bloomhaven.app

import android.app.Application
import com.bloomhaven.app.core.notifications.NotificationHelper

class BloomHavenApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
    }
}
