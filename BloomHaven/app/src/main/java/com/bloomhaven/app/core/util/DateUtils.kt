package com.bloomhaven.app.core.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateUtils {
    val isoDate: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val isoTime: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun today(): LocalDate = LocalDate.now()
    fun now(): LocalDateTime = LocalDateTime.now()

    fun dateToStorage(date: LocalDate): String = date.format(isoDate)
    fun storageToDate(value: String): LocalDate = LocalDate.parse(value, isoDate)

    fun timeToStorage(time: LocalTime): String = time.format(isoTime)
    fun storageToTime(value: String): LocalTime = LocalTime.parse(value, isoTime)

    fun friendlyDate(date: LocalDate): String {
        val today = today()
        return when (date) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            today.plusDays(1) -> "Tomorrow"
            else -> date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
        }
    }

    fun friendlyDateLong(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))

    fun friendlyTime(time: LocalTime): String =
        time.format(DateTimeFormatter.ofPattern("h:mm a"))

    fun monthLabel(date: LocalDate): String =
        "${date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${date.year}"

    fun greetingWindow(time: LocalTime = LocalTime.now()): GreetingWindow = when {
        time.isBefore(LocalTime.of(5, 0)) -> GreetingWindow.NIGHT
        time.isBefore(LocalTime.of(12, 0)) -> GreetingWindow.MORNING
        time.isBefore(LocalTime.of(17, 0)) -> GreetingWindow.AFTERNOON
        time.isBefore(LocalTime.of(21, 0)) -> GreetingWindow.EVENING
        else -> GreetingWindow.NIGHT
    }
}

enum class GreetingWindow { MORNING, AFTERNOON, EVENING, NIGHT }
