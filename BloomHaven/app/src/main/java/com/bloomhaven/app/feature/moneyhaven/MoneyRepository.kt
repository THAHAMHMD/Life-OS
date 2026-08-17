package com.bloomhaven.app.feature.moneyhaven

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

class MoneyRepository(private val db: MoneyDatabase) {
    // Categories
    fun categories(): Flow<List<CategoryEntity>> = db.categoryDao().all()
    suspend fun addCategory(name: String, type: TransactionType) = db.categoryDao().insert(CategoryEntity(name = name, type = type))
    suspend fun deleteCategory(category: CategoryEntity) = db.categoryDao().delete(category)

    // Transactions
    fun transactionsBetween(start: LocalDate, end: LocalDate): Flow<List<TransactionEntity>> = db.transactionDao().between(start, end)
    fun totalByType(type: TransactionType, start: LocalDate, end: LocalDate): Flow<Double> = db.transactionDao().totalByType(type, start, end)
    fun spendByCategory(type: TransactionType, start: LocalDate, end: LocalDate): Flow<List<CategorySpend>> = db.transactionDao().spendByCategory(type, start, end)
    fun auditFor(transactionId: Long): Flow<List<TransactionAuditEntity>> = db.transactionDao().auditFor(transactionId)

    suspend fun addTransaction(date: LocalDate, type: TransactionType, category: String, amount: Double, note: String) {
        db.transactionDao().insert(TransactionEntity(date = date, type = type, category = category, amount = amount, note = note))
    }

    suspend fun updateTransaction(existing: TransactionEntity, date: LocalDate, category: String, amount: Double, note: String) {
        val financiallyChanged = existing.amount != amount || existing.category != category
        if (financiallyChanged) {
            db.transactionDao().insertAudit(
                TransactionAuditEntity(
                    transactionId = existing.id,
                    previousAmount = existing.amount,
                    previousCategory = existing.category,
                    previousNote = existing.note,
                ),
            )
        }
        db.transactionDao().update(
            existing.copy(
                date = date,
                category = category,
                amount = amount,
                note = note,
                updatedAt = LocalDateTime.now(),
                wasEdited = existing.wasEdited || financiallyChanged,
            ),
        )
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = db.transactionDao().delete(transaction)

    // Budgets
    fun budgets(): Flow<List<BudgetEntity>> = db.budgetDao().all()
    suspend fun upsertBudget(existing: BudgetEntity?, category: String, monthlyLimit: Double) {
        if (existing != null) db.budgetDao().update(existing.copy(category = category, monthlyLimit = monthlyLimit))
        else db.budgetDao().insert(BudgetEntity(category = category, monthlyLimit = monthlyLimit))
    }
    suspend fun deleteBudget(budget: BudgetEntity) = db.budgetDao().delete(budget)

    // Savings
    fun savingsGoals(): Flow<List<SavingsGoalEntity>> = db.savingsDao().goals()
    fun contributionsFor(goalId: Long): Flow<List<SavingsContributionEntity>> = db.savingsDao().contributionsFor(goalId)
    fun totalFor(goalId: Long): Flow<Double> = db.savingsDao().totalFor(goalId)
    fun goalTotals(): Flow<List<GoalTotal>> = db.savingsDao().goalTotals()
    suspend fun addGoal(name: String, targetAmount: Double, targetDate: LocalDate?) {
        db.savingsDao().insertGoal(SavingsGoalEntity(name = name, targetAmount = targetAmount, targetDate = targetDate))
    }
    suspend fun deleteGoal(goal: SavingsGoalEntity) = db.savingsDao().deleteGoal(goal)
    suspend fun addContribution(goalId: Long, amount: Double, date: LocalDate, note: String) {
        db.savingsDao().insertContribution(SavingsContributionEntity(goalId = goalId, amount = amount, date = date, note = note))
    }
    suspend fun deleteContribution(contribution: SavingsContributionEntity) = db.savingsDao().deleteContribution(contribution)

    // Debts
    fun debts(): Flow<List<DebtEntity>> = db.debtDao().all()
    fun paymentsFor(debtId: Long): Flow<List<DebtPaymentEntity>> = db.debtDao().paymentsFor(debtId)
    suspend fun addDebt(name: String, principal: Double, interestRatePct: Double?, dueDate: LocalDate?, notes: String) {
        db.debtDao().insert(DebtEntity(name = name, principal = principal, remainingAmount = principal, interestRatePct = interestRatePct, dueDate = dueDate, notes = notes))
    }
    suspend fun deleteDebt(debt: DebtEntity) = db.debtDao().delete(debt)
    suspend fun addDebtPayment(debt: DebtEntity, amount: Double, date: LocalDate, note: String) {
        db.debtDao().insertPayment(DebtPaymentEntity(debtId = debt.id, amount = amount, date = date, note = note))
        val newRemaining = (debt.remainingAmount - amount).coerceAtLeast(0.0)
        db.debtDao().update(debt.copy(remainingAmount = newRemaining))
    }

    // Investments
    fun investments(): Flow<List<InvestmentEntity>> = db.investmentDao().all()
    suspend fun addInvestment(name: String, type: String, amountInvested: Double, currentValue: Double, date: LocalDate, notes: String) {
        db.investmentDao().insert(InvestmentEntity(name = name, type = type, amountInvested = amountInvested, currentValue = currentValue, date = date, notes = notes))
    }
    suspend fun updateInvestmentValue(investment: InvestmentEntity, newValue: Double) {
        db.investmentDao().update(investment.copy(currentValue = newValue, updatedAt = LocalDateTime.now()))
    }
    suspend fun deleteInvestment(investment: InvestmentEntity) = db.investmentDao().delete(investment)
}
