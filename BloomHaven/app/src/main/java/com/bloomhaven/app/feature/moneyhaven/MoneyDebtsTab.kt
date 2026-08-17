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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
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
import com.bloomhaven.app.core.ui.ConfirmDeleteDialog
import com.bloomhaven.app.core.ui.EmptyState
import com.bloomhaven.app.core.ui.ScreenPadding
import com.bloomhaven.app.core.util.CurrencyUtils
import com.bloomhaven.app.core.util.DateUtils

@Composable
fun MoneyDebtsTab(viewModel: MoneyViewModel, state: MoneyUiState) {
    var showAddNew by remember { mutableStateOf(false) }
    var payDebt by remember { mutableStateOf<DebtEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<DebtEntity?>(null) }

    LazyColumn(contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Debts & loans", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { showAddNew = true }) { Text("Add debt") }
            }
        }
        if (state.debts.isEmpty()) {
            item { EmptyState(title = "No debts tracked", message = "Add a loan or debt to keep track of what's left to pay.", actionLabel = "Add a debt", onAction = { showAddNew = true }) }
        } else {
            items(state.debts, key = { it.id }) { debt ->
                val progress = (1 - (debt.remainingAmount / debt.principal.coerceAtLeast(0.01))).coerceIn(0.0, 1.0)
                BloomCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(debt.name, style = MaterialTheme.typography.titleSmall)
                        IconButton(onClick = { pendingDelete = debt }) { Icon(Icons.Filled.Close, contentDescription = "Delete debt") }
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(progress = { progress.toFloat() }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${CurrencyUtils.format(debt.remainingAmount)} remaining of ${CurrencyUtils.format(debt.principal)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    debt.dueDate?.let { Text("Due ${DateUtils.friendlyDate(it)}", style = MaterialTheme.typography.bodySmall) }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { payDebt = debt }) { Text("Log a payment") }
                }
            }
        }
    }

    if (showAddNew) {
        DebtFormDialog(onDismiss = { showAddNew = false }, onSave = { name, principal, rate, due, notes ->
            viewModel.addDebt(name, principal, rate, due, notes)
            showAddNew = false
        })
    }
    payDebt?.let { debt ->
        PaymentDialog(onDismiss = { payDebt = null }, onSave = { amt -> viewModel.addDebtPayment(debt, amt, DateUtils.today(), ""); payDebt = null })
    }
    pendingDelete?.let { debt ->
        ConfirmDeleteDialog(itemLabel = "this debt", onConfirm = { viewModel.deleteDebt(debt); pendingDelete = null }, onDismiss = { pendingDelete = null })
    }
}

@Composable
private fun PaymentDialog(onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log a payment") },
        text = { OutlinedTextField(value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Amount paid") }) },
        confirmButton = { TextButton(onClick = { amount.toDoubleOrNull()?.let { if (it > 0) onSave(it) } }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun DebtFormDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double, Double?, java.time.LocalDate?, String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var principal by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add debt / loan") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = principal, onValueChange = { principal = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Total amount") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = rate, onValueChange = { rate = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Interest rate % (optional)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (optional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val p = principal.toDoubleOrNull()
                if (name.isNotBlank() && p != null && p > 0) onSave(name.trim(), p, rate.toDoubleOrNull(), null, notes)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
