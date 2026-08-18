package com.bloomhaven.app.feature.commitments

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Recurrence cadence for a commitment. Kept to three simple options for v1 — no custom
 * intervals, no background auto-rollover. "Mark as paid" is what advances the due date.
 */
object Recurrence {
    const val WEEKLY = "WEEKLY"
    const val MONTHLY = "MONTHLY"
    const val YEARLY = "YEARLY"

    val all = listOf(WEEKLY, MONTHLY, YEARLY)

    fun label(value: String): String = when (value) {
        WEEKLY -> "Weekly"
        MONTHLY -> "Monthly"
        YEARLY -> "Yearly"
        else -> value
    }
}

/**
 * A recurring financial obligation — a subscription (Netflix, Spotify) or a recurring bill
 * (rent, electricity, phone). This module is the single source of truth for these; it is used
 * by (but does not depend on) Money Haven, so it stays fully self-contained.
 */
@Entity(tableName = "commitments")
data class CommitmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val amount: Double,
    val recurrence: String,
    val nextDueDate: LocalDate,
    val isActive: Boolean = true,
    val remindersEnabled: Boolean = true,
    val notes: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

/**
 * A permanent record of one payment against a commitment, logged by "Mark as paid". History is
 * never pruned — it is only removed alongside its parent commitment on delete.
 */
@Entity(tableName = "commitment_payments")
data class CommitmentPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val commitmentId: Long,
    val paidDate: LocalDate,
    val amount: Double,
)
