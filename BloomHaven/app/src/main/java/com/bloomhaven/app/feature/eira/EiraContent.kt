package com.bloomhaven.app.feature.eira

import com.bloomhaven.app.core.util.GreetingWindow
import java.time.LocalDate

/** Small local context Eira may gently reference — never invented, only passed in from real data. */
data class EiraContext(
    val userName: String = "",
    val habitsCompletedToday: Int = 0,
    val waterLoggedToday: Boolean = false,
    val plannerItemsToday: Int = 0,
)

private val MORNING_GREETINGS = listOf(
    "Good morning{name}. Here's to a gentle start.",
    "Morning{name}. Take today one soft step at a time.",
    "Good morning{name} — I'm glad you're here.",
)
private val AFTERNOON_GREETINGS = listOf(
    "Hope your day is treating you kindly{name}.",
    "Good afternoon{name}. However today is going, it's okay.",
)
private val EVENING_GREETINGS = listOf(
    "Good evening{name}. Time to slow down a little.",
    "Evening{name} — however today went, you made it through.",
)
private val NIGHT_GREETINGS = listOf(
    "Good night{name}. Rest well — you've earned it.",
    "Sleep well{name}. Tomorrow can wait until tomorrow.",
)

private val QUOTES = listOf(
    "Growth is quiet. You don't have to rush it.",
    "You're allowed to have a slow day.",
    "Small, steady steps still move you forward.",
    "Rest is not the opposite of progress — it's part of it.",
    "You don't need to earn softness with productivity.",
    "Whatever today held, you're still here. That counts.",
    "No pressure, just presence.",
)

object EiraContent {
    fun greeting(window: GreetingWindow, date: LocalDate, context: EiraContext): String {
        val pool = when (window) {
            GreetingWindow.MORNING -> MORNING_GREETINGS
            GreetingWindow.AFTERNOON -> AFTERNOON_GREETINGS
            GreetingWindow.EVENING -> EVENING_GREETINGS
            GreetingWindow.NIGHT -> NIGHT_GREETINGS
        }
        val seed = date.toEpochDay().toInt() + window.ordinal
        val base = pool[Math.floorMod(seed, pool.size)]
        val namePart = if (context.userName.isNotBlank()) ", ${context.userName}" else ""
        var text = base.replace("{name}", namePart)

        val notice = personalNotice(window, context)
        if (notice != null) text = "$text $notice"
        return text
    }

    fun quote(date: LocalDate): String {
        val seed = date.toEpochDay().toInt()
        return QUOTES[Math.floorMod(seed, QUOTES.size)]
    }

    private fun personalNotice(window: GreetingWindow, context: EiraContext): String? = when {
        window == GreetingWindow.EVENING && context.habitsCompletedToday > 0 ->
            "I noticed you kept up with ${context.habitsCompletedToday} habit${if (context.habitsCompletedToday == 1) "" else "s"} today."
        window == GreetingWindow.MORNING && context.plannerItemsToday > 0 ->
            "You have ${context.plannerItemsToday} thing${if (context.plannerItemsToday == 1) "" else "s"} on today's schedule — no need to rush into it."
        else -> null
    }
}
