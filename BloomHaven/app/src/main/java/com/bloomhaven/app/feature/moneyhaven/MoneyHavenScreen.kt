package com.bloomhaven.app.feature.moneyhaven

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloomhaven.app.core.ui.BloomCard
import com.bloomhaven.app.core.ui.BloomPrimaryButton
import com.bloomhaven.app.core.ui.ConfirmDeleteDialog
import com.bloomhaven.app.core.ui.EmptyState
import com.bloomhaven.app.core.ui.ScreenPadding
import com.bloomhaven.app.core.util.CurrencyUtils
import com.bloomhaven.app.core.util.DateUtils
import java.time.LocalDate

private val TABS = listOf("Overview", "Transactions", "Budgets", "Savings", "Debts", "Investments")

@Composable
fun MoneyHavenScreen(viewModel: MoneyViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var tabIndex by remember { mutableIntStateOf(0) }
    var showAddTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var showAddTransactionNew by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (tabIndex == 0 || tabIndex == 1) {
                FloatingActionButton(onClick = { showAddTransactionNew = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add transaction")
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text("Money Haven", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Everything about your money, in one calm place.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TabRow(selectedTabIndex = tabIndex) {
                TABS.forEachIndexed { index, label ->
                    Tab(selected = tabIndex == index, onClick = { tabIndex = index }, text = { Text(label) })
                }
            }
            when (tabIndex) {
                0 -> MoneyOverviewTab(state = state, onChangeMonth = viewModel::changeMonth)
                1 -> MoneyTransactionsTab(state = state, onEdit = { showAddTransaction = it }, onDelete = viewModel::deleteTransaction)
                2 -> MoneyBudgetsTab(viewModel = viewModel, state = state)
                3 -> MoneySavingsTab(viewModel = viewModel, state = state)
                4 -> MoneyDebtsTab(viewModel = viewModel, state = state)
                5 -> MoneyInvestmentsTab(viewModel = viewModel, state = state)
            }
        }
    }

    if (showAddTransactionNew) {
        TransactionFormDialog(
            state = state,
            existing = null,
            onDismiss = { showAddTransactionNew = false },
            onSave = { date, type, category, amount, note ->
                viewModel.addTransaction(date, type, category, amount, note)
                showAddTransactionNew = false
            },
        )
    }
    showAddTransaction?.let { existing ->
        TransactionFormDialog(
            state = state,
            existing = existing,
            onDismiss = { showAddTransaction = null },
            onSave = { date, type, category, amount, note ->
                viewModel.updateTransaction(existing, date, category, amount, note)
                showAddTransaction = null
            },
        )
    }
}

@Composable
private fun MoneyOverviewTab(state: MoneyUiState, onChangeMonth: (Long) -> Unit) {
    LazyColumn(contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { onChangeMonth(-1) }) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month") }
                Text(DateUtils.monthLabel(state.selectedMonth.atDay(1)), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { onChangeMonth(1) }) { Icon(Icons.Filled.ChevronRight, contentDescription = "Next month") }
            }
        }
        item {
            BloomCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Income", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(CurrencyUtils.format(state.income), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                    Column {
                        Text("Expenses", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(CurrencyUtils.format(state.expense), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                    }
                    Column {
                        Text("Net", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(CurrencyUtils.format(state.net), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        item { Text("Spending by category", style = MaterialTheme.typography.titleMedium) }
        if (state.categorySpend.isEmpty()) {
            item { EmptyState(title = "No expenses yet", message = "Log an expense this month to see your breakdown here.") }
        } else {
            val total = state.categorySpend.sumOf { it.total }.coerceAtLeast(0.01)
            items(state.categorySpend, key = { it.category }) { spend ->
                BloomCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(spend.category, style = MaterialTheme.typography.titleSmall)
                        Text(CurrencyUtils.format(spend.total), style = MaterialTheme.typography.titleSmall)
                    }
                    Text(
                        "${((spend.total / total) * 100).toInt()}% of monthly spending",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun MoneyTransactionsTab(
    state: MoneyUiState,
    onEdit: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    LazyColumn(contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (state.transactions.isEmpty()) {
            item { EmptyState(title = "No transactions this month", message = "Tap + to add your first income or expense.") }
        } else {
            items(state.transactions, key = { it.id }) { txn ->
                BloomCard(onClick = { onEdit(txn) }) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(txn.category, style = MaterialTheme.typography.titleSmall)
                            Text(DateUtils.friendlyDate(txn.date) + if (txn.note.isNotBlank()) " · ${txn.note}" else "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                (if (txn.type == TransactionType.EXPENSE) "-" else "+") + CurrencyUtils.format(txn.amount),
                                color = if (txn.type == TransactionType.EXPENSE) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.titleSmall,
                            )
                            IconButton(onClick = { pendingDelete = txn }) { Icon(Icons.Filled.Close, contentDescription = "Delete") }
                        }
                    }
                }
            }
        }
    }
    pendingDelete?.let { txn ->
        ConfirmDeleteDialog(itemLabel = "this transaction", onConfirm = { onDelete(txn); pendingDelete = null }, onDismiss = { pendingDelete = null })
    }
}

@Composable
private fun TransactionFormDialog(
    state: MoneyUiState,
    existing: TransactionEntity?,
    onDismiss: () -> Unit,
    onSave: (LocalDate, TransactionType, String, Double, String) -> Unit,
) {
    var type by remember { mutableStateOf(existing?.type ?: TransactionType.EXPENSE) }
    var category by remember { mutableStateOf(existing?.category ?: "") }
    var amount by remember { mutableStateOf(existing?.amount?.toString() ?: "") }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var date by remember { mutableStateOf(existing?.date ?: DateUtils.today()) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    val availableCategories = state.categories.filter { it.type == type }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add transaction" else "Edit transaction") },
        text = {
            Column {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val types = TransactionType.entries.toList()
                    types.forEachIndexed { index, t ->
                        SegmentedButton(
                            selected = type == t,
                            onClick = { type = t; category = "" },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                        ) { Text(if (t == TransactionType.INCOME) "Income" else "Expense") }
                    }
                }
                Spacer(Modifier.height(12.dp))
                ExposedDropdownMenuBox(expanded = categoryMenuExpanded, onExpandedChange = { categoryMenuExpanded = it }) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }) {
                        availableCategories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat.name) }, onClick = { category = cat.name; categoryMenuExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note (optional)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                Text("Date: ${DateUtils.friendlyDateLong(date)}", style = MaterialTheme.typography.bodyMedium)
                Row {
                    TextButton(onClick = { date = date.minusDays(1) }) { Text("Earlier") }
                    TextButton(onClick = { date = DateUtils.today() }) { Text("Today") }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amount.toDoubleOrNull()
                if (amt != null && amt > 0 && category.isNotBlank()) {
                    onSave(date, type, category, amt, note)
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
