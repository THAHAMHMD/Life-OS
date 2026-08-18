package com.bloomhaven.app.feature.flow

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** A single manual water-logging entry. Flow (hydration) history is permanent — never pruned. */
@Entity(tableName = "water_log")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val amountMl: Int,
    val time: LocalTime,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
