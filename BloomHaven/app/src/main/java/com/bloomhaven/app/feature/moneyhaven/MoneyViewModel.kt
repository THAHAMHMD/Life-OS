package com.bloomhaven.app.feature.moneyhaven

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class SavingsGoalUi(val goal: SavingsGoalEntity, val savedAmount: Double)

data class MoneyUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val categorySpend: List<CategorySpend> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val savingsGoals: List<SavingsGoalUi> = emptyList(),
    val debts: List<DebtEntity> = emptyList(),
    val investments: List<InvestmentEntity> = emptyList(),
) {
    val net: Double get() = income - expense
    fun spendFor(category: String): Double = categorySpend.firstOrNull { it.category == category }?.total ?: 0.0
}

private data class MonthData(
    val income: Double,
    val expense: Double,
    val categorySpend: List<CategorySpend>,
    val transactions: List<TransactionEntity>,
)

private data class StaticData(
    val categories: List<CategoryEntity>,
    val budgets: List<BudgetEntity>,
    val goals: List<SavingsGoalEntity>,
    val goalTotals: List<GoalTotal>,
    val debts: List<DebtEntity>,
    val investments: List<InvestmentEntity>,
)

class MoneyViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = MoneyRepository(MoneyDatabase.getInstance(application))

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    private val monthDataFlow = selectedMonth.flatMapLatest { month ->
        val start = month.atDay(1)
        val end = month.atEndOfMonth()
        combine(
            repo.totalByType(TransactionType.INCOME, start, end),
            repo.totalByType(TransactionType.EXPENSE, start, end),
            repo.spendByCategory(TransactionType.EXPENSE, start, end),
            repo.transactionsBetween(start, end),
        ) { income, expense, catSpend, txns -> MonthData(income, expense, catSpend, txns) }
    }

    private data class PartialStaticData(
        val categories: List<CategoryEntity>,
        val budgets: List<BudgetEntity>,
        val goals: List<SavingsGoalEntity>,
        val goalTotals: List<GoalTotal>,
        val debts: List<DebtEntity>,
    )

    private val staticDataFlow = combine(
        repo.categories(),
        repo.budgets(),
        repo.savingsGoals(),
        repo.goalTotals(),
        repo.debts(),
    ) { categories, budgets, goals, goalTotals, debts ->
        PartialStaticData(categories, budgets, goals, goalTotals, debts)
    }.combine(repo.investments()) { partial, investments ->
        StaticData(
            categories = partial.categories,
            budgets = partial.budgets,
            goals = partial.goals,
            goalTotals = partial.goalTotals,
            debts = partial.debts,
            investments = investments,
        )
    }

    val uiState = combine(monthDataFlow, staticDataFlow, selectedMonth) { monthData, staticData, month ->
        val totalsByGoal = staticData.goalTotals.associate { it.goalId to it.total }
        MoneyUiState(
            selectedMonth = month,
            income = monthData.income,
            expense = monthData.expense,
            categorySpend = monthData.categorySpend,
            transactions = monthData.transactions,
            categories = staticData.categories,
            budgets = staticData.budgets,
            savingsGoals = staticData.goals.map { SavingsGoalUi(it, totalsByGoal[it.id] ?: 0.0) },
            debts = staticData.debts,
            investments = staticData.investments,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MoneyUiState())

    fun changeMonth(delta: Long) {
        selectedMonth.value = selectedMonth.value.plusMonths(delta)
    }

    fun addTransaction(date: LocalDate, type: TransactionType, category: String, amount: Double, note: String) {
        viewModelScope.launch { repo.addTransaction(date, type, category, amount, note) }
    }

    fun updateTransaction(existing: TransactionEntity, date: LocalDate, category: String, amount: Double, note: String) {
        viewModelScope.launch { repo.updateTransaction(existing, date, category, amount, note) }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch { repo.deleteTransaction(transaction) }
    }

    fun addCategory(name: String, type: TransactionType) {
        viewModelScope.launch { repo.addCategory(name, type) }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch { repo.deleteCategory(category) }
    }

    fun upsertBudget(existing: BudgetEntity?, category: String, limit: Double) {
        viewModelScope.launch { repo.upsertBudget(existing, category, limit) }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch { repo.deleteBudget(budget) }
    }

    fun addGoal(name: String, target: Double, targetDate: LocalDate?) {
        viewModelScope.launch { repo.addGoal(name, target, targetDate) }
    }

    fun deleteGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch { repo.deleteGoal(goal) }
    }

    fun addContribution(goalId: Long, amount: Double, date: LocalDate, note: String) {
        viewModelScope.launch { repo.addContribution(goalId, amount, date, note) }
    }

    fun contributionsFor(goalId: Long) = repo.contributionsFor(goalId)

    fun deleteContribution(contribution: SavingsContributionEntity) {
        viewModelScope.launch { repo.deleteContribution(contribution) }
    }

    fun addDebt(name: String, principal: Double, interestRatePct: Double?, dueDate: LocalDate?, notes: String) {
        viewModelScope.launch { repo.addDebt(name, principal, interestRatePct, dueDate, notes) }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch { repo.deleteDebt(debt) }
    }

    fun addDebtPayment(debt: DebtEntity, amount: Double, date: LocalDate, note: String) {
        viewModelScope.launch { repo.addDebtPayment(debt, amount, date, note) }
    }

    fun paymentsFor(debtId: Long) = repo.paymentsFor(debtId)

    fun addInvestment(name: String, type: String, invested: Double, currentValue: Double, date: LocalDate, notes: String) {
        viewModelScope.launch { repo.addInvestment(name, type, invested, currentValue, date, notes) }
    }

    fun updateInvestmentValue(investment: InvestmentEntity, newValue: Double) {
        viewModelScope.launch { repo.updateInvestmentValue(investment, newValue) }
    }

    fun deleteInvestment(investment: InvestmentEntity) {
        viewModelScope.launch { repo.deleteInvestment(investment) }
    }
}
