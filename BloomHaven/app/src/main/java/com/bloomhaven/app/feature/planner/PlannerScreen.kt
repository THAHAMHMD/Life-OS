package com.bloomhaven.app.feature.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloomhaven.app.core.ui.BloomCard
import com.bloomhaven.app.core.ui.ConfirmDeleteDialog
import com.bloomhaven.app.core.ui.EmptyState
import com.bloomhaven.app.core.ui.ScreenPadding
import com.bloomhaven.app.core.util.DateUtils
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun PlannerScreen(viewModel: PlannerViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showAddNew by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<PlannerItemEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<PlannerItemEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddNew = true }) { Icon(Icons.Filled.Add, contentDescription = "Add planner item") }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text("Life Planner", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Your schedule, shifts, and important dates.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    PlannerViewMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = state.viewMode == mode,
                            onClick = { viewModel.setViewMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, PlannerViewMode.entries.size),
                        ) { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    }
                }
            }

            when (state.viewMode) {
                PlannerViewMode.DAY -> DayView(state, viewModel, onEdit = { editing = it }, onDelete = { pendingDelete = it })
                PlannerViewMode.WEEK -> WeekView(state, viewModel, onEdit = { editing = it }, onDelete = { pendingDelete = it })
                PlannerViewMode.MONTH -> MonthView(state, viewModel)
            }
        }
    }

    if (showAddNew) {
        PlannerFormDialog(existing = null, defaultDate = state.selectedDate, onDismiss = { showAddNew = false }, onSave = { title, date, time, notes, category, recurrence, reminder ->
            viewModel.addItem(title, date, time, notes, category, recurrence, reminder)
            showAddNew = false
        })
    }
    editing?.let { item ->
        PlannerFormDialog(existing = item, defaultDate = item.date, onDismiss = { editing = null }, onSave = { title, date, time, notes, category, _, reminder ->
            viewModel.updateItem(item, title, date, time, notes, category, reminder)
            editing = null
        })
    }
    pendingDelete?.let { item ->
        ConfirmDeleteDialog(
            itemLabel = "\"${item.title}\"",
            extraMessage = if (item.recurrenceGroupId != null) "This deletes only this occurrence — other occurrences of this repeating item are kept." else null,
            onConfirm = { viewModel.deleteItem(item); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun DayView(
    state: PlannerUiState,
    viewModel: PlannerViewModel,
    onEdit: (PlannerItemEntity) -> Unit,
    onDelete: (PlannerItemEntity) -> Unit,
) {
    LazyColumn(contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.selectDate(state.selectedDate.minusDays(1)) }) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous day") }
                Text(DateUtils.friendlyDateLong(state.selectedDate), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { viewModel.selectDate(state.selectedDate.plusDays(1)) }) { Icon(Icons.Filled.ChevronRight, contentDescription = "Next day") }
            }
        }
        if (state.dayItems.isEmpty()) {
            item { EmptyState(title = "Nothing planned", message = "Tap + to add a shift, appointment, or event for this day.") }
        } else {
            items(state.dayItems, key = { it.id }) { item ->
                PlannerItemCard(item, onToggle = { viewModel.toggleCompleted(item) }, onEdit = { onEdit(item) }, onDelete = { onDelete(item) })
            }
        }
    }
}

@Composable
fun PlannerItemCard(item: PlannerItemEntity, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    BloomCard(onClick = onEdit) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Checkbox(checked = item.isCompleted, onCheckedChange = { onToggle() })
                Column {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleSmall,
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null,
                    )
                    val timeLabel = item.time?.let { DateUtils.friendlyTime(it) } ?: "All day"
                    Text("$timeLabel · ${item.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Close, contentDescription = "Delete") }
        }
    }
}

@Composable
private fun PlannerFormDialog(
    existing: PlannerItemEntity?,
    defaultDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (String, LocalDate, LocalTime?, String, String, String, Boolean) -> Unit,
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var date by remember { mutableStateOf(existing?.date ?: defaultDate) }
    var hasTime by remember { mutableStateOf(existing?.time != null) }
    var time by remember { mutableStateOf(existing?.time ?: LocalTime.of(9, 0)) }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: PlannerCategories.first()) }
    var recurrence by remember { mutableStateOf(existing?.recurrenceType ?: Recurrence.NONE) }
    var reminder by remember { mutableStateOf(existing?.reminderEnabled ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New planner item" else "Edit item") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Row {
                    TextButton(onClick = { date = date.minusDays(1) }) { Text("◀") }
                    Text(DateUtils.friendlyDateLong(date), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(top = 10.dp))
                    TextButton(onClick = { date = date.plusDays(1) }) { Text("▶") }
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Set a time", modifier = Modifier.weight(1f))
                    Switch(checked = hasTime, onCheckedChange = { hasTime = it })
                }
                if (hasTime) {
                    Row {
                        TextButton(onClick = { time = time.minusMinutes(30) }) { Text("-30m") }
                        Text(DateUtils.friendlyTime(time), modifier = Modifier.weight(1f).padding(top = 10.dp))
                        TextButton(onClick = { time = time.plusMinutes(30) }) { Text("+30m") }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text("Category", style = MaterialTheme.typography.bodySmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PlannerCategories) { cat ->
                        FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (optional)") }, modifier = Modifier.fillMaxWidth())
                if (existing == null) {
                    Spacer(Modifier.height(10.dp))
                    Text("Repeat", style = MaterialTheme.typography.bodySmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(Recurrence.options) { opt ->
                            FilterChip(selected = recurrence == opt, onClick = { recurrence = opt }, label = { Text(Recurrence.label(opt)) })
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Remind me", modifier = Modifier.weight(1f))
                    Switch(checked = reminder, onCheckedChange = { reminder = it }, enabled = hasTime)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) onSave(title.trim(), date, if (hasTime) time else null, notes, category, recurrence, reminder && hasTime)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
