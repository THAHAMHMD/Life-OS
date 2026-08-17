package com.bloomhaven.app.feature.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IconButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.bloomhaven.app.core.ui.ConfirmDeleteDialog
import com.bloomhaven.app.core.ui.EmptyState
import com.bloomhaven.app.core.ui.ScreenPadding
import com.bloomhaven.app.core.util.DateUtils
import java.time.DayOfWeek

@Composable
fun HabitsScreen(viewModel: HabitsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showAddHabit by remember { mutableStateOf(false) }
    var showManage by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddHabit = true }) { Icon(Icons.Filled.Add, contentDescription = "Add habit") }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = ScreenPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Bloom Habits", style = MaterialTheme.typography.headlineMedium)
                        Text("Small, repeated care for yourself.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { showManage = true }) { Text("Manage") }
                }
            }

            item { BloomGardenCard(state) }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.selectDate(state.selectedDate.minusDays(1)) }) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous day") }
                    Text(DateUtils.friendlyDate(state.selectedDate), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { viewModel.selectDate(state.selectedDate.plusDays(1)) }) { Icon(Icons.Filled.ChevronRight, contentDescription = "Next day") }
                }
            }

            val dueToday = state.activeHabits.filter { it.isDueOn(state.selectedDate) }
            if (dueToday.isEmpty()) {
                item { EmptyState(title = "No habits for this day", message = "Tap + to create your first gentle habit.") }
            } else {
                items(dueToday, key = { it.id }) { habit ->
                    val done = state.logsForDate.contains(habit.id)
                    BloomCard {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Checkbox(checked = done, onCheckedChange = { viewModel.toggleHabit(habit.id, it) })
                            Text(habit.emoji, style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.width(10.dp))
                            Text(habit.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    if (showAddHabit) {
        AddHabitDialog(onDismiss = { showAddHabit = false }, onSave = { name, emoji, days -> viewModel.addHabit(name, emoji, days); showAddHabit = false })
    }
    if (showManage) {
        ManageHabitsDialog(viewModel = viewModel, onDismiss = { showManage = false })
    }
}

@Composable
private fun BloomGardenCard(state: HabitsUiState) {
    BloomCard {
        Text("Bloom Garden", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            if (state.totalCompletions == 0) "Your garden is waiting for its first bloom." else "Grown from ${state.totalCompletions} completed habits.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val blooms = state.totalCompletions.coerceAtMost(40)
            repeat(blooms) { index ->
                val stage = when {
                    index >= blooms - 3 -> "🌸"
                    index >= blooms - 8 -> "🌷"
                    else -> "🌿"
                }
                Text(stage, style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column {
                Text("${state.currentStreak}", style = MaterialTheme.typography.titleLarge)
                Text("Current streak", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column {
                Text("${state.bestStreak}", style = MaterialTheme.typography.titleLarge)
                Text("Best streak", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AddHabitDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(HabitEmojiChoices.first()) }
    var everyDay by remember { mutableStateOf(true) }
    var selectedDays by remember { mutableStateOf(setOf<DayOfWeek>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New habit") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Habit name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HabitEmojiChoices.forEach { e ->
                        FilterChip(selected = emoji == e, onClick = { emoji = e }, label = { Text(e) })
                    }
                }
                Spacer(Modifier.height(10.dp))
                FilterChip(selected = everyDay, onClick = { everyDay = true }, label = { Text("Every day") })
                Spacer(Modifier.height(6.dp))
                if (!everyDay) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DayOfWeek.values().forEach { day ->
                            FilterChip(
                                selected = selectedDays.contains(day),
                                onClick = { selectedDays = if (selectedDays.contains(day)) selectedDays - day else selectedDays + day },
                                label = { Text(day.name.take(3)) },
                            )
                        }
                    }
                }
                TextButton(onClick = { everyDay = false }) { Text("Choose specific days") }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    val days = if (everyDay) "" else selectedDays.joinToString(",") { it.name }
                    onSave(name.trim(), emoji, days)
                }
            }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ManageHabitsDialog(viewModel: HabitsViewModel, onDismiss: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    var pendingDelete by remember { mutableStateOf<HabitEntity?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage habits") },
        text = {
            Column {
                if (state.activeHabits.isEmpty()) {
                    Text("No habits yet.", style = MaterialTheme.typography.bodyMedium)
                }
                state.activeHabits.forEach { habit ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${habit.emoji} ${habit.name}")
                        Row {
                            IconButton(onClick = { viewModel.archiveHabit(habit) }) { Icon(Icons.Filled.Archive, contentDescription = "Archive habit") }
                            IconButton(onClick = { pendingDelete = habit }) { Icon(Icons.Filled.Close, contentDescription = "Delete habit") }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
    )

    pendingDelete?.let { habit ->
        ConfirmDeleteDialog(
            itemLabel = "\"${habit.name}\"",
            extraMessage = "This deletes the habit and all of its completion history.",
            onConfirm = { viewModel.deleteHabit(habit); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}
