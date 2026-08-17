package com.bloomhaven.app.feature.moneyhaven

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bloomhaven.app.core.ui.BloomCard
import com.bloomhaven.app.core.ui.ConfirmDeleteDialog
import com.bloomhaven.app.core.ui.EmptyState
import com.bloomhaven.app.core.ui.ScreenPadding
import com.bloomhaven.app.core.util.CurrencyUtils
import com.bloomhaven.app.core.util.DateUtils

@Composable
fun MoneySavingsTab(viewModel: MoneyViewModel, state: MoneyUiState) {
    var showAddNew by remember { mutableStateOf(false) }
    var openGoal by remember { mutableStateOf<SavingsGoalUi?>(null) }
    var pendingDelete by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    LazyColumn(contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Savings goals", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { showAddNew = true }) { Text("New goal") }
            }
        }
        if (state.savingsGoals.isEmpty()) {
            item { EmptyState(title = "No savings goals yet", message = "Create one — an emergency fund, a trip, anything.", actionLabel = "Create a goal", onAction = { showAddNew = true }) }
        } else {
            items(state.savingsGoals, key = { it.goal.id }) { goalUi ->
                val progress = (goalUi.savedAmount / goalUi.goal.targetAmount.coerceAtLeast(0.01)).coerceIn(0.0, 1.0)
                BloomCard(onClick = { openGoal = goalUi }) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(goalUi.goal.name, style = MaterialTheme.typography.titleSmall)
                        IconButton(onClick = { pendingDelete = goalUi.goal }) { Icon(Icons.Filled.Close, contentDescription = "Delete goal") }
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(progress = { progress.toFloat() }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${CurrencyUtils.format(goalUi.savedAmount)} of ${CurrencyUtils.format(goalUi.goal.targetAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    if (showAddNew) {
        GoalFormDialog(onDismiss = { showAddNew = false }, onSave = { name, target, date -> viewModel.addGoal(name, target, date); showAddNew = false })
    }
    openGoal?.let { goalUi ->
        GoalDetailDialog(viewModel = viewModel, goalUi = goalUi, onDismiss = { openGoal = null })
    }
    pendingDelete?.let { goal ->
        ConfirmDeleteDialog(itemLabel = "this savings goal", extraMessage = "This will permanently delete the goal and its contribution history.", onConfirm = { viewModel.deleteGoal(goal); pendingDelete = null }, onDismiss = { pendingDelete = null })
    }
}

@Composable
private fun GoalDetailDialog(viewModel: MoneyViewModel, goalUi: SavingsGoalUi, onDismiss: () -> Unit) {
    val contributions by viewModel.contributionsFor(goalUi.goal.id).collectAsState(initial = emptyList())
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(goalUi.goal.name) },
        text = {
            Column {
                Text("Saved so far: ${CurrencyUtils.format(goalUi.savedAmount)} of ${CurrencyUtils.format(goalUi.goal.targetAmount)}")
                Spacer(Modifier.height(12.dp))
                Row {
                    OutlinedTextField(value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Add contribution") }, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        amount.toDoubleOrNull()?.let { if (it > 0) { viewModel.addContribution(goalUi.goal.id, it, DateUtils.today(), ""); amount = "" } }
                    }) { Text("Add") }
                }
                Spacer(Modifier.height(12.dp))
                Text("History", style = MaterialTheme.typography.titleSmall)
                LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                    items(contributions, key = { it.id }) { c ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(DateUtils.friendlyDate(c.date), style = MaterialTheme.typography.bodySmall)
                            Text(CurrencyUtils.format(c.amount), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
    )
}

@Composable
private fun GoalFormDialog(onDismiss: () -> Unit, onSave: (String, Double, java.time.LocalDate?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New savings goal") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Goal name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = target, onValueChange = { target = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Target amount") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val t = target.toDoubleOrNull()
                if (name.isNotBlank() && t != null && t > 0) onSave(name.trim(), t, null)
            }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
