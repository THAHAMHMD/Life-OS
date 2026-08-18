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
fun MoneyInvestmentsTab(viewModel: MoneyViewModel, state: MoneyUiState) {
    var showAddNew by remember { mutableStateOf(false) }
    var updateValueFor by remember { mutableStateOf<InvestmentEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<InvestmentEntity?>(null) }

    LazyColumn(contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Investments", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { showAddNew = true }) { Text("Add investment") }
            }
        }
        if (state.investments.isEmpty()) {
            item { EmptyState(title = "No investments tracked", message = "Add an investment to track its value over time.", actionLabel = "Add an investment", onAction = { showAddNew = true }) }
        } else {
            items(state.investments, key = { it.id }) { inv ->
                val gain = inv.currentValue - inv.amountInvested
                BloomCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(inv.name, style = MaterialTheme.typography.titleSmall)
                            Text(inv.type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { pendingDelete = inv }) { Icon(Icons.Filled.Close, contentDescription = "Delete investment") }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Invested ${CurrencyUtils.format(inv.amountInvested)} · now ${CurrencyUtils.format(inv.currentValue)}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        (if (gain >= 0) "+" else "") + CurrencyUtils.format(gain),
                        color = if (gain >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text("Last updated ${DateUtils.friendlyDate(inv.updatedAt.toLocalDate())}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { updateValueFor = inv }) { Text("Update value") }
                }
            }
        }
    }

    if (showAddNew) {
        InvestmentFormDialog(onDismiss = { showAddNew = false }, onSave = { name, type, invested, current, notes ->
            viewModel.addInvestment(name, type, invested, current, DateUtils.today(), notes)
            showAddNew = false
        })
    }
    updateValueFor?.let { inv ->
        UpdateValueDialog(current = inv.currentValue, onDismiss = { updateValueFor = null }, onSave = { v -> viewModel.updateInvestmentValue(inv, v); updateValueFor = null })
    }
    pendingDelete?.let { inv ->
        ConfirmDeleteDialog(itemLabel = "this investment", onConfirm = { viewModel.deleteInvestment(inv); pendingDelete = null }, onDismiss = { pendingDelete = null })
    }
}

@Composable
private fun UpdateValueDialog(current: Double, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var value by remember { mutableStateOf(current.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update current value") },
        text = { OutlinedTextField(value = value, onValueChange = { value = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Current value") }) },
        confirmButton = { TextButton(onClick = { value.toDoubleOrNull()?.let { if (it >= 0) onSave(it) } }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun InvestmentFormDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Double, String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var invested by remember { mutableStateOf("") }
    var current by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add investment") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type (e.g. Mutual Fund, Stocks, FD)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = invested, onValueChange = { invested = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Amount invested") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = current, onValueChange = { current = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Current value") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (optional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val i = invested.toDoubleOrNull()
                val c = current.toDoubleOrNull() ?: i
                if (name.isNotBlank() && i != null && i >= 0 && c != null) onSave(name.trim(), type.ifBlank { "Other" }, i, c, notes)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
