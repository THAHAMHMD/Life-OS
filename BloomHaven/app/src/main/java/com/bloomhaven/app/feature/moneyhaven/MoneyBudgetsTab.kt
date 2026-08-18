@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.bloomhaven.app.feature.moneyhaven

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bloomhaven.app.core.ui.BloomCard
import com.bloomhaven.app.core.ui.BloomPrimaryButton
import com.bloomhaven.app.core.ui.ConfirmDeleteDialog
import com.bloomhaven.app.core.ui.EmptyState
import com.bloomhaven.app.core.ui.ScreenPadding
import com.bloomhaven.app.core.util.CurrencyUtils
import androidx.compose.foundation.layout.width

@Composable
fun MoneyBudgetsTab(viewModel: MoneyViewModel, state: MoneyUiState) {
    var showAdd by remember { mutableStateOf<BudgetEntity?>(null) }
    var showAddNew by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<BudgetEntity?>(null) }

    LazyColumn(contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("This month's budgets", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { showAddNew = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.height(18.dp))
                    Text("Add budget")
                }
            }
        }
        if (state.budgets.isEmpty()) {
            item { EmptyState(title = "No budgets yet", message = "Set a monthly limit for a category to keep a gentle eye on spending.", actionLabel = "Add a budget", onAction = { showAddNew = true }) }
        } else {
            items(state.budgets, key = { it.id }) { budget ->
                val spent = state.spendFor(budget.category)
                val progress = (spent / budget.monthlyLimit.coerceAtLeast(0.01)).coerceIn(0.0, 1.0)
                BloomCard(onClick = { showAdd = budget }) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(budget.category, style = MaterialTheme.typography.titleSmall)
                        IconButton(onClick = { pendingDelete = budget }) { Icon(Icons.Filled.Close, contentDescription = "Delete budget") }
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (progress >= 1.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${CurrencyUtils.format(spent)} of ${CurrencyUtils.format(budget.monthlyLimit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            Text("Categories", style = MaterialTheme.typography.titleMedium)
        }
        items(state.categories, key = { it.id }) { category ->
            BloomCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${category.name} (${if (category.type == TransactionType.EXPENSE) "Expense" else "Income"})", style = MaterialTheme.typography.bodyMedium)
                    if (!category.isDefault) {
                        IconButton(onClick = { viewModel.deleteCategory(category) }) { Icon(Icons.Filled.Close, contentDescription = "Remove category") }
                    }
                }
            }
        }
        item { AddCategoryRow(onAdd = viewModel::addCategory) }
    }

    if (showAddNew || showAdd != null) {
        BudgetFormDialog(
            existing = showAdd,
            categories = state.categories.filter { it.type == TransactionType.EXPENSE },
            onDismiss = { showAdd = null; showAddNew = false },
            onSave = { category, limit ->
                viewModel.upsertBudget(showAdd, category, limit)
                showAdd = null; showAddNew = false
            },
        )
    }
    pendingDelete?.let { budget ->
        ConfirmDeleteDialog(itemLabel = "this budget", onConfirm = { viewModel.deleteBudget(budget); pendingDelete = null }, onDismiss = { pendingDelete = null })
    }
}

@Composable
private fun AddCategoryRow(onAdd: (String, TransactionType) -> Unit) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    BloomCard {
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("New category") }, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = { type = if (type == TransactionType.EXPENSE) TransactionType.INCOME else TransactionType.EXPENSE }) {
                Text(if (type == TransactionType.EXPENSE) "Expense" else "Income")
            }
        }
        BloomPrimaryButton(text = "Add category", onClick = {
            if (name.isNotBlank()) { onAdd(name.trim(), type); name = "" }
        })
    }
}

@Composable
private fun BudgetFormDialog(
    existing: BudgetEntity?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit,
) {
    var category by remember { mutableStateOf(existing?.category ?: "") }
    var limit by remember { mutableStateOf(existing?.monthlyLimit?.toString() ?: "") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add budget" else "Edit budget") },
        text = {
            Column {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat.name) }, onClick = { category = cat.name; expanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = limit, onValueChange = { limit = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Monthly limit") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val l = limit.toDoubleOrNull()
                if (l != null && l > 0 && category.isNotBlank()) onSave(category, l)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
