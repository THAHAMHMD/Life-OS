package com.bloomhaven.app.core.ui

import com.bloomhaven.app.core.data.NightHavenMode
import java.time.LocalTime

/** Night Haven auto-window: soft/darker appearance from 7pm to 6am. */
fun resolveNightHaven(mode: NightHavenMode, now: LocalTime = LocalTime.now()): Boolean = when (mode) {
    NightHavenMode.ALWAYS -> true
    NightHavenMode.NEVER -> false
    NightHavenMode.AUTO -> now.isAfter(LocalTime.of(19, 0)) || now.isBefore(LocalTime.of(6, 0))
}
