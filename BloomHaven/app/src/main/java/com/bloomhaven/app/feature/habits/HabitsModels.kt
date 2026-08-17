package com.bloomhaven.app.feature.habits

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/** Emoji-based icon keeps this simple — no custom icon picker infrastructure needed. */
val HabitEmojiChoices = listOf("🌱", "💧", "📖", "🧘", "🏃", "🛌", "🥗", "✍️", "🧹", "🎨")

@Entity(tableName = "habit")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "🌱",
    /** Empty = every day. Otherwise comma-separated DayOfWeek names, e.g. "MONDAY,WEDNESDAY". */
    val repeatDays: String = "",
    val isArchived: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    fun isDueOn(date: LocalDate): Boolean {
        if (repeatDays.isBlank()) return true
        val days = repeatDays.split(",").mapNotNull { runCatching { DayOfWeek.valueOf(it) }.getOrNull() }
        return days.contains(date.dayOfWeek)
    }
}

@Entity(
    tableName = "habit_log",
    indices = [Index(value = ["habitId", "date"], unique = true)],
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: LocalDate,
    val completed: Boolean = true,
)
