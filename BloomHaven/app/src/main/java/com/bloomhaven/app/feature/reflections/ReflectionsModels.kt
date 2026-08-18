package com.bloomhaven.app.feature.reflections

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime

/** A five-point mood scale used for reflections. Kept warm and non-clinical. */
enum class Mood(val emoji: String, val label: String) {
    GREAT("🌞", "Great"),
    GOOD("🙂", "Good"),
    OKAY("😐", "Okay"),
    LOW("😔", "Low"),
    HARD("😫", "Hard"),
}

/** Local Room TypeConverter for the [Mood] enum, scoped to this module's database. */
class MoodConverters {
    @TypeConverter
    fun fromMood(value: Mood?): String? = value?.name

    @TypeConverter
    fun toMood(value: String?): Mood? = value?.let { stored ->
        Mood.entries.firstOrNull { it.name == stored } ?: Mood.OKAY
    }
}

/** A single journal entry. Reflections history is permanent — never pruned. */
@Entity(tableName = "reflections")
data class ReflectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val mood: Mood,
    val text: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
