package com.bloomhaven.app.feature.moneyhaven

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class CategorySpend(val category: String, val total: Double)

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("SELECT * FROM money_category ORDER BY name ASC")
    fun all(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM money_category WHERE name = :name AND type = :type")
    suspend fun countByName(name: String, type: TransactionType): Int
}

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Insert
    suspend fun insertAudit(audit: TransactionAuditEntity)

    @Query("SELECT * FROM money_transaction WHERE date BETWEEN :start AND :end ORDER BY date DESC, id DESC")
    fun between(start: LocalDate, end: LocalDate): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount),0) FROM money_transaction WHERE type = :type AND date BETWEEN :start AND :end")
    fun totalByType(type: TransactionType, start: LocalDate, end: LocalDate): Flow<Double>

    @Query("SELECT category, COALESCE(SUM(amount),0) as total FROM money_transaction WHERE type = :type AND date BETWEEN :start AND :end GROUP BY category ORDER BY total DESC")
    fun spendByCategory(type: TransactionType, start: LocalDate, end: LocalDate): Flow<List<CategorySpend>>

    @Query("SELECT * FROM money_transaction_audit WHERE transactionId = :transactionId ORDER BY changedAt DESC")
    fun auditFor(transactionId: Long): Flow<List<TransactionAuditEntity>>
}

@Dao
interface BudgetDao {
    @Insert
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM money_budget ORDER BY category ASC")
    fun all(): Flow<List<BudgetEntity>>
}

@Dao
interface SavingsDao {
    @Insert
    suspend fun insertGoal(goal: SavingsGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoalEntity)

    @Insert
    suspend fun insertContribution(contribution: SavingsContributionEntity): Long

    @Delete
    suspend fun deleteContribution(contribution: SavingsContributionEntity)

    @Query("SELECT * FROM money_savings_goal ORDER BY createdAt DESC")
    fun goals(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM money_savings_contribution WHERE goalId = :goalId ORDER BY date DESC")
    fun contributionsFor(goalId: Long): Flow<List<SavingsContributionEntity>>

    @Query("SELECT COALESCE(SUM(amount),0) FROM money_savings_contribution WHERE goalId = :goalId")
    fun totalFor(goalId: Long): Flow<Double>

    @Query("SELECT goalId, COALESCE(SUM(amount),0) as total FROM money_savings_contribution GROUP BY goalId")
    fun goalTotals(): Flow<List<GoalTotal>>
}

data class GoalTotal(val goalId: Long, val total: Double)

@Dao
interface DebtDao {
    @Insert
    suspend fun insert(debt: DebtEntity): Long

    @Update
    suspend fun update(debt: DebtEntity)

    @Delete
    suspend fun delete(debt: DebtEntity)

    @Insert
    suspend fun insertPayment(payment: DebtPaymentEntity): Long

    @Query("SELECT * FROM money_debt ORDER BY createdAt DESC")
    fun all(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM money_debt_payment WHERE debtId = :debtId ORDER BY date DESC")
    fun paymentsFor(debtId: Long): Flow<List<DebtPaymentEntity>>
}

@Dao
interface InvestmentDao {
    @Insert
    suspend fun insert(investment: InvestmentEntity): Long

    @Update
    suspend fun update(investment: InvestmentEntity)

    @Delete
    suspend fun delete(investment: InvestmentEntity)

    @Query("SELECT * FROM money_investment ORDER BY updatedAt DESC")
    fun all(): Flow<List<InvestmentEntity>>
}
