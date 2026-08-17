package com.bloomhaven.app.feature.gentletasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloomhaven.app.core.ui.BloomCard
import com.bloomhaven.app.core.ui.ConfirmDeleteDialog
import com.bloomhaven.app.core.ui.EmptyState
import com.bloomhaven.app.core.ui.ScreenPadding
import com.bloomhaven.app.core.util.DateUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun GentleTasksScreen(viewModel: GentleTasksViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    var isComposerOpen by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<TaskEntity?>(null) }
    var completedExpanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingTask = null
                isComposerOpen = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "New task")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
            contentPadding = ScreenPadding,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text("Gentle Tasks", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "A simple, no-pressure to-do list.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (state.toDo.isEmpty() && state.completed.isEmpty()) {
                item {
                    EmptyState(
                        title = "Nothing on your list",
                        message = "Add a task whenever you're ready — there's no rush.",
                        actionLabel = "New task",
                        onAction = {
                            editingTask = null
                            isComposerOpen = true
                        },
                    )
                }
            } else {
                if (state.toDo.isEmpty()) {
                    item {
                        EmptyState(
                            title = "All caught up",
                            message = "Nothing left to do right now. Nice work.",
                        )
                    }
                } else {
                    items(state.toDo, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            onToggle = { viewModel.setCompleted(task, it) },
                            onClick = { editingTask = task; isComposerOpen = true },
                            onDelete = { pendingDelete = task },
                        )
                    }
                }

                if (state.completed.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = { completedExpanded = !completedExpanded }) {
                                Text("Completed (${state.completed.size})")
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    if (completedExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                    if (completedExpanded) {
                        items(state.completed, key = { it.id }) { task ->
                            TaskRow(
                                task = task,
                                onToggle = { viewModel.setCompleted(task, it) },
                                onClick = { editingTask = task; isComposerOpen = true },
                                onDelete = { pendingDelete = task },
                            )
                        }
                    }
                }
            }
        }
    }

    if (isComposerOpen) {
        TaskComposerDialog(
            existing = editingTask,
            onDismiss = { isComposerOpen = false },
            onSave = { title, notes, dueDate ->
                val current = editingTask
                if (current == null) {
                    viewModel.addTask(title, notes, dueDate)
                } else {
                    viewModel.updateTask(current, title, notes, dueDate)
                }
                isComposerOpen = false
            },
        )
    }

    pendingDelete?.let { task ->
        ConfirmDeleteDialog(
            itemLabel = "this task",
            onConfirm = { viewModel.deleteTask(task); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun TaskRow(
    task: TaskEntity,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val isOverdue = task.dueDate != null && !task.isCompleted && task.dueDate.isBefore(DateUtils.today())

    BloomCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = task.isCompleted, onCheckedChange = onToggle)
            Spacer(Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleSmall,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                if (task.notes.isNotBlank()) {
                    Text(
                        task.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                if (task.dueDate != null) {
                    Spacer(Modifier.height(4.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text(if (isOverdue) "Overdue" else DateUtils.friendlyDate(task.dueDate)) },
                        colors = if (isOverdue) {
                            AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.error,
                            )
                        } else {
                            AssistChipDefaults.assistChipColors()
                        },
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete task")
            }
        }
    }
}

@Composable
private fun TaskComposerDialog(
    existing: TaskEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, LocalDate?) -> Unit,
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var dueDate by remember { mutableStateOf(existing?.dueDate) }
    var isDatePickerOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New task" else "Edit task") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = { isDatePickerOpen = true },
                        leadingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                        label = { Text(dueDate?.let { DateUtils.friendlyDate(it) } ?: "Add due date") },
                    )
                    if (dueDate != null) {
                        IconButton(onClick = { dueDate = null }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear due date")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title.trim(), notes.trim(), dueDate) },
                enabled = title.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )

    if (isDatePickerOpen) {
        val initialMillis = (dueDate ?: DateUtils.today())
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { isDatePickerOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        dueDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    isDatePickerOpen = false
                }) { Text("Set date") }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerOpen = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
