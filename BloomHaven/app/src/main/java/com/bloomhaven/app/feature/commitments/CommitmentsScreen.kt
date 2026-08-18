package com.bloomhaven.app.feature.commitments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloomhaven.app.core.ui.BloomCard
import com.bloomhaven.app.core.ui.BloomPrimaryButton
import com.bloomhaven.app.core.ui.BloomSecondaryButton
import com.bloomhaven.app.core.ui.ConfirmDeleteDialog
import com.bloomhaven.app.core.ui.EmptyState
import com.bloomhaven.app.core.ui.ScreenPadding
import com.bloomhaven.app.core.util.CurrencyUtils
import com.bloomhaven.app.core.util.DateUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@Composable
fun CommitmentsScreen(viewModel: CommitmentsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    var showForm by remember { mutableStateOf(false) }
    var editingCommitment by remember { mutableStateOf<CommitmentEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<CommitmentEntity?>(null) }

    fun openAddForm() {
        editingCommitment = null
        showForm = true
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openAddForm() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add a commitment")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
            contentPadding = ScreenPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text("Commitments", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Subscriptions and bills, tracked gently.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (state.commitments.isEmpty()) {
                item {
                    EmptyState(
                        title = "Nothing here yet",
                        message = "Add a subscription or bill to keep a gentle eye on what's due and when.",
                        actionLabel = "Add a commitment",
                        onAction = { openAddForm() },
                    )
                }
            } else {
                items(state.commitments, key = { it.id }) { commitment ->
                    CommitmentCard(
                        commitment = commitment,
                        onMarkPaid = { viewModel.markAsPaid(commitment) },
                        onEdit = { editingCommitment = commitment; showForm = true },
                        onDelete = { pendingDelete = commitment },
                    )
                }
            }

            if (state.paymentHistory.isNotEmpty()) {
                item { HistorySection(payments = state.paymentHistory) }
            }
        }
    }

    if (showForm) {
        CommitmentFormSheet(
            existing = editingCommitment,
            onDismiss = { showForm = false },
            onSave = { commitment ->
                viewModel.save(commitment)
                showForm = false
            },
        )
    }

    pendingDelete?.let { commitment ->
        ConfirmDeleteDialog(
            itemLabel = commitment.name,
            extraMessage = "This will permanently delete \"${commitment.name}\" and its payment history. This can't be undone.",
            onConfirm = {
                viewModel.deleteCommitment(commitment)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun CommitmentCard(
    commitment: CommitmentEntity,
    onMarkPaid: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val daysUntilDue = ChronoUnit.DAYS.between(DateUtils.today(), commitment.nextDueDate)
    val dueSoon = daysUntilDue <= 3
    val dueColor = if (dueSoon) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant

    BloomCard(onClick = onEdit) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(commitment.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    commitment.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Close, contentDescription = "Delete ${commitment.name}")
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(CurrencyUtils.format(commitment.amount), style = MaterialTheme.typography.headlineSmall)
                Text(
                    Recurrence.label(commitment.recurrence),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Due ${DateUtils.friendlyDate(commitment.nextDueDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = dueColor,
                )
                if (dueSoon) {
                    Text("Due soon", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }

        if (commitment.notes.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                commitment.notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(12.dp))
        BloomPrimaryButton(text = "Mark as paid", onClick = onMarkPaid, icon = Icons.Filled.Check)
    }
}

@Composable
private fun HistorySection(payments: List<CommitmentPaymentWithName>) {
    var expanded by remember { mutableStateOf(false) }

    BloomCard(onClick = { expanded = !expanded }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("History", style = MaterialTheme.typography.titleMedium)
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Collapse history" else "Expand history",
            )
        }

        if (expanded) {
            Spacer(Modifier.height(12.dp))
            val grouped = payments.groupBy { DateUtils.monthLabel(it.paidDate) }
            grouped.forEach { (month, entries) ->
                Text(month, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                entries.forEach { payment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(payment.commitmentName, style = MaterialTheme.typography.bodyMedium)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(CurrencyUtils.format(payment.amount), style = MaterialTheme.typography.bodyMedium)
                            Text(
                                DateUtils.friendlyDate(payment.paidDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommitmentFormSheet(
    existing: CommitmentEntity?,
    onDismiss: () -> Unit,
    onSave: (CommitmentEntity) -> Unit,
) {
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var category by remember(existing) { mutableStateOf(existing?.category ?: "") }
    var amountText by remember(existing) {
        mutableStateOf(
            existing?.amount?.let { amount ->
                if (amount == amount.toLong().toDouble()) amount.toLong().toString() else amount.toString()
            } ?: "",
        )
    }
    var recurrence by remember(existing) { mutableStateOf(existing?.recurrence ?: Recurrence.MONTHLY) }
    var nextDueDate by remember(existing) { mutableStateOf(existing?.nextDueDate ?: DateUtils.today()) }
    var remindersEnabled by remember(existing) { mutableStateOf(existing?.remindersEnabled ?: true) }
    var notes by remember(existing) { mutableStateOf(existing?.notes ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                if (existing == null) "Add a commitment" else "Edit commitment",
                style = MaterialTheme.typography.titleLarge,
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                placeholder = { Text("Netflix, rent, phone bill…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Column {
                Text(
                    "Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = category == "Subscription",
                        onClick = { category = "Subscription" },
                        label = { Text("Subscription") },
                    )
                    FilterChip(
                        selected = category == "Bill",
                        onClick = { category = "Bill" },
                        label = { Text("Bill") },
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { input -> amountText = input.filter { it.isDigit() || it == '.' } },
                label = { Text("Amount") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Column {
                Text(
                    "Repeats",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Recurrence.all.forEach { option ->
                        FilterChip(
                            selected = recurrence == option,
                            onClick = { recurrence = option },
                            label = { Text(Recurrence.label(option)) },
                        )
                    }
                }
            }

            Column {
                Text(
                    "Next due date",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                BloomSecondaryButton(
                    text = DateUtils.friendlyDateLong(nextDueDate),
                    onClick = { showDatePicker = true },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Remind me", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "A gentle nudge the day before it's due",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = remindersEnabled, onCheckedChange = { remindersEnabled = it })
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )

            BloomPrimaryButton(
                text = if (existing == null) "Add commitment" else "Save changes",
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amount > 0.0) {
                        val base = existing ?: CommitmentEntity(
                            name = "",
                            category = "",
                            amount = 0.0,
                            recurrence = Recurrence.MONTHLY,
                            nextDueDate = DateUtils.today(),
                        )
                        onSave(
                            base.copy(
                                name = name.trim(),
                                category = category.trim().ifBlank { "Other" },
                                amount = amount,
                                recurrence = recurrence,
                                nextDueDate = nextDueDate,
                                remindersEnabled = remindersEnabled,
                                notes = notes.trim(),
                            ),
                        )
                    }
                },
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = nextDueDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        nextDueDate = dateFromMillis(millis)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun dateFromMillis(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
