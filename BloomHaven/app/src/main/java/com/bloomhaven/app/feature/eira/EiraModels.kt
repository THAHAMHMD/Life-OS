package com.bloomhaven.app.feature.eira

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

enum class EiraMessageKind { GREETING, QUOTE }

/** Eira in v1 is a gentle presence — greetings/quotes only, persisted so history is real. */
@Entity(tableName = "eira_message")
data class EiraMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val text: String,
    val kind: EiraMessageKind,
)
