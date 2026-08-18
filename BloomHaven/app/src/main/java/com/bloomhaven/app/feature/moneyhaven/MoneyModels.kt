package com.bloomhaven.app.feature.moneyhaven

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

enum class TransactionType { INCOME, EXPENSE }

@Entity(tableName = "money_category")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val isDefault: Boolean = false,
)

@Entity(tableName = "money_transaction")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val type: TransactionType,
    val category: String,
    val amount: Double,
    val note: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val wasEdited: Boolean = false,
)

/** Preserves the previous value whenever a financially-important edit happens (spec 5.4). */
@Entity(tableName = "money_transaction_audit")
data class TransactionAuditEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long,
    val previousAmount: Double,
    val previousCategory: String,
    val previousNote: String,
    val changedAt: LocalDateTime = LocalDateTime.now(),
)

@Entity(tableName = "money_budget")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val monthlyLimit: Double,
)

@Entity(tableName = "money_savings_goal")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val targetDate: LocalDate? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

@Entity(tableName = "money_savings_contribution")
data class SavingsContributionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val amount: Double,
    val date: LocalDate,
    val note: String = "",
)

@Entity(tableName = "money_debt")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val principal: Double,
    val remainingAmount: Double,
    val interestRatePct: Double? = null,
    val dueDate: LocalDate? = null,
    val notes: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

@Entity(tableName = "money_debt_payment")
data class DebtPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debtId: Long,
    val amount: Double,
    val date: LocalDate,
    val note: String = "",
)

@Entity(tableName = "money_investment")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val amountInvested: Double,
    val currentValue: Double,
    val date: LocalDate,
    val notes: String = "",
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

val DEFAULT_EXPENSE_CATEGORIES = listOf(
    "Food", "Transport", "Rent", "Shopping", "Entertainment", "Health", "Bills", "Other",
)
val DEFAULT_INCOME_CATEGORIES = listOf("Salary", "Gift", "Other")
