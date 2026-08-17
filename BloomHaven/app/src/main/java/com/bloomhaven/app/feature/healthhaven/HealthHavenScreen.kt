@file:OptIn(ExperimentalMaterial3Api::class)

package com.bloomhaven.app.feature.healthhaven

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloomhaven.app.core.ui.BloomCard
import com.bloomhaven.app.core.ui.BloomDot
import com.bloomhaven.app.core.ui.ConfirmDeleteDialog
import com.bloomhaven.app.core.ui.EmptyState
import com.bloomhaven.app.core.ui.ScreenPadding
import com.bloomhaven.app.core.ui.SectionHeader
import com.bloomhaven.app.core.util.DateUtils
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Top-level Health Haven screen. Wired into navigation by function name/signature —
 * see the module spec for the exact contract another engineer relies on.
 */
@Composable
fun HealthHavenScreen(viewModel: HealthHavenViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Workouts", "Sleep", "Measurements", "Appointments", "Medicines", "Notes")

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text("Health Haven", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Your workouts, sleep, body, appointments, medicines, and notes — all in one place.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 20.dp) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(label) },
                    )
                }
            }

            when (selectedTab) {
                0 -> WorkoutsTab(viewModel, state.workouts)
                1 -> SleepTab(viewModel, state.sleepEntries)
                2 -> MeasurementsTab(viewModel, state.measurements)
                3 -> AppointmentsTab(viewModel, state.appointments)
                4 -> MedicinesTab(viewModel, state.medicines)
                5 -> NotesTab(viewModel, state.notes)
            }
        }
    }
}

// ============================================================================================
// Shared form helpers
// ============================================================================================

@Composable
private fun DatePickerRow(label: String, date: LocalDate, onDateChange: (LocalDate) -> Unit) {
    var show by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { show = true }, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("$label: ${DateUtils.friendlyDate(date)}")
    }
    if (show) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onDateChange(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    show = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun OptionalDatePickerRow(label: String, date: LocalDate?, onDateChange: (LocalDate?) -> Unit) {
    var show by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { show = true }, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(if (date != null) "$label: ${DateUtils.friendlyDate(date)}" else "$label (optional)")
    }
    if (show) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = (date ?: DateUtils.today()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onDateChange(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    show = false
                }) { Text("OK") }
            },
            dismissButton = {
                Row {
                    if (date != null) {
                        TextButton(onClick = { onDateChange(null); show = false }) { Text("Clear") }
                    }
                    TextButton(onClick = { show = false }) { Text("Cancel") }
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun OptionalTimePickerRow(label: String, time: LocalTime?, onTimeChange: (LocalTime?) -> Unit) {
    var show by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { show = true }, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(if (time != null) "$label: ${DateUtils.friendlyTime(time)}" else "$label (optional)")
    }
    if (show) {
        val pickerState = rememberTimePickerState(
            initialHour = time?.hour ?: 9,
            initialMinute = time?.minute ?: 0,
        )
        AlertDialog(
            onDismissRequest = { show = false },
            title = { Text(label) },
            text = { TimePicker(state = pickerState) },
            confirmButton = {
                TextButton(onClick = {
                    onTimeChange(LocalTime.of(pickerState.hour, pickerState.minute))
                    show = false
                }) { Text("OK") }
            },
            dismissButton = {
                Row {
                    if (time != null) {
                        TextButton(onClick = { onTimeChange(null); show = false }) { Text("Clear") }
                    }
                    TextButton(onClick = { show = false }) { Text("Cancel") }
                }
            },
        )
    }
}

// ============================================================================================
// Workouts
// ============================================================================================

@Composable
private fun WorkoutsTab(viewModel: HealthHavenViewModel, workouts: List<WorkoutEntity>) {
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<WorkoutEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<WorkoutEntity?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = ScreenPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionHeader(title = "Workouts", action = "Add", onAction = { editing = null; showForm = true })
        }
        if (workouts.isEmpty()) {
            item {
                EmptyState(
                    title = "No workouts logged",
                    message = "Log a run, gym session, or any activity to start your history.",
                    actionLabel = "Log workout",
                    onAction = { editing = null; showForm = true },
                )
            }
        } else {
            items(workouts, key = { it.id }) { workout ->
                BloomCard(onClick = { editing = workout; showForm = true }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(workout.type, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${DateUtils.friendlyDate(workout.date)} · ${workout.durationMinutes} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (workout.notes.isNotBlank()) {
                                Text(workout.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { pendingDelete = workout }) {
                            Icon(Icons.Filled.Close, contentDescription = "Delete workout")
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        WorkoutFormDialog(
            existing = editing,
            onDismiss = { showForm = false },
            onSave = { date, type, duration, notes ->
                val current = editing
                if (current == null) viewModel.addWorkout(date, type, duration, notes)
                else viewModel.updateWorkout(current, date, type, duration, notes)
                showForm = false
            },
        )
    }

    pendingDelete?.let { workout ->
        ConfirmDeleteDialog(
            itemLabel = "this workout",
            onConfirm = { viewModel.deleteWorkout(workout); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun WorkoutFormDialog(
    existing: WorkoutEntity?,
    onDismiss: () -> Unit,
    onSave: (LocalDate, String, Int, String) -> Unit,
) {
    var date by remember { mutableStateOf(existing?.date ?: DateUtils.today()) }
    var type by remember { mutableStateOf(existing?.type ?: "") }
    var duration by remember { mutableStateOf(existing?.durationMinutes?.toString() ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Log workout" else "Edit workout") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DatePickerRow(label = "Date", date = date, onDateChange = { date = it })
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Type (e.g. Run, Gym, Yoga)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it.filter { c -> c.isDigit() } },
                    label = { Text("Duration (minutes)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(date, type.trim(), duration.toIntOrNull() ?: 0, notes.trim()) },
                enabled = type.isNotBlank() && (duration.toIntOrNull() ?: 0) > 0,
            ) { Text(if (existing == null) "Save" else "Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

// ============================================================================================
// Sleep
// ============================================================================================

@Composable
private fun SleepTab(viewModel: HealthHavenViewModel, entries: List<SleepEntity>) {
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<SleepEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<SleepEntity?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = ScreenPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionHeader(title = "Sleep", action = "Add", onAction = { editing = null; showForm = true })
        }
        if (entries.isEmpty()) {
            item {
                EmptyState(
                    title = "No sleep logged",
                    message = "Track how many hours you slept and how it felt.",
                    actionLabel = "Log sleep",
                    onAction = { editing = null; showForm = true },
                )
            }
        } else {
            items(entries, key = { it.id }) { entry ->
                BloomCard(onClick = { editing = entry; showForm = true }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("${entry.hours} hrs · ${entry.quality}", style = MaterialTheme.typography.titleSmall)
                            Text(
                                DateUtils.friendlyDate(entry.date),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (entry.notes.isNotBlank()) {
                                Text(entry.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { pendingDelete = entry }) {
                            Icon(Icons.Filled.Close, contentDescription = "Delete sleep entry")
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        SleepFormDialog(
            existing = editing,
            onDismiss = { showForm = false },
            onSave = { date, hours, quality, notes ->
                val current = editing
                if (current == null) viewModel.addSleep(date, hours, quality, notes)
                else viewModel.updateSleep(current, date, hours, quality, notes)
                showForm = false
            },
        )
    }

    pendingDelete?.let { entry ->
        ConfirmDeleteDialog(
            itemLabel = "this sleep entry",
            onConfirm = { viewModel.deleteSleep(entry); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun SleepFormDialog(
    existing: SleepEntity?,
    onDismiss: () -> Unit,
    onSave: (LocalDate, Double, String, String) -> Unit,
) {
    var date by remember { mutableStateOf(existing?.date ?: DateUtils.today()) }
    var hours by remember { mutableStateOf(existing?.hours?.toString() ?: "") }
    var quality by remember { mutableStateOf(existing?.quality ?: SleepQuality.OPTIONS[2]) }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Log sleep" else "Edit sleep entry") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DatePickerRow(label = "Date", date = date, onDateChange = { date = it })
                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Hours slept") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Quality", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SleepQuality.OPTIONS.forEach { option ->
                        FilterChip(selected = quality == option, onClick = { quality = option }, label = { Text(option) })
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(date, hours.toDoubleOrNull() ?: 0.0, quality, notes.trim()) },
                enabled = (hours.toDoubleOrNull() ?: 0.0) > 0.0,
            ) { Text(if (existing == null) "Save" else "Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

// ============================================================================================
// Body measurements (also serves as weight history: date — weight, newest first)
// ============================================================================================

@Composable
private fun MeasurementsTab(viewModel: HealthHavenViewModel, measurements: List<BodyMeasurementEntity>) {
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<BodyMeasurementEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<BodyMeasurementEntity?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = ScreenPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionHeader(title = "Measurements", action = "Add", onAction = { editing = null; showForm = true })
        }
        if (measurements.isEmpty()) {
            item {
                EmptyState(
                    title = "No measurements yet",
                    message = "Log your weight or other measurements to build a history over time.",
                    actionLabel = "Log measurement",
                    onAction = { editing = null; showForm = true },
                )
            }
        } else {
            items(measurements, key = { it.id }) { entry ->
                BloomCard(onClick = { editing = entry; showForm = true }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            val summary = listOfNotNull(
                                entry.weightKg?.let { "${it} kg" },
                                entry.heightCm?.let { "${it} cm" },
                            ).joinToString(" · ").ifBlank { "No values" }
                            Text(summary, style = MaterialTheme.typography.titleSmall)
                            Text(
                                DateUtils.friendlyDate(entry.date),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (entry.notes.isNotBlank()) {
                                Text(entry.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { pendingDelete = entry }) {
                            Icon(Icons.Filled.Close, contentDescription = "Delete measurement")
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        MeasurementFormDialog(
            existing = editing,
            onDismiss = { showForm = false },
            onSave = { date, weight, height, notes ->
                val current = editing
                if (current == null) viewModel.addMeasurement(date, weight, height, notes)
                else viewModel.updateMeasurement(current, date, weight, height, notes)
                showForm = false
            },
        )
    }

    pendingDelete?.let { entry ->
        ConfirmDeleteDialog(
            itemLabel = "this measurement",
            onConfirm = { viewModel.deleteMeasurement(entry); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun MeasurementFormDialog(
    existing: BodyMeasurementEntity?,
    onDismiss: () -> Unit,
    onSave: (LocalDate, Double?, Double?, String) -> Unit,
) {
    var date by remember { mutableStateOf(existing?.date ?: DateUtils.today()) }
    var weight by remember { mutableStateOf(existing?.weightKg?.toString() ?: "") }
    var height by remember { mutableStateOf(existing?.heightCm?.toString() ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Log measurement" else "Edit measurement") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DatePickerRow(label = "Date", date = date, onDateChange = { date = it })
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Weight (kg, optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Height (cm, optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(date, weight.toDoubleOrNull(), height.toDoubleOrNull(), notes.trim()) },
                enabled = weight.toDoubleOrNull() != null || height.toDoubleOrNull() != null,
            ) { Text(if (existing == null) "Save" else "Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

// ============================================================================================
// Medical appointments
// ============================================================================================

@Composable
private fun AppointmentsTab(viewModel: HealthHavenViewModel, appointments: List<MedicalAppointmentEntity>) {
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<MedicalAppointmentEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<MedicalAppointmentEntity?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = ScreenPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionHeader(title = "Appointments", action = "Add", onAction = { editing = null; showForm = true })
        }
        if (appointments.isEmpty()) {
            item {
                EmptyState(
                    title = "No appointments yet",
                    message = "Add a checkup or medical appointment to keep track of it.",
                    actionLabel = "Add appointment",
                    onAction = { editing = null; showForm = true },
                )
            }
        } else {
            items(appointments, key = { it.id }) { entry ->
                BloomCard(onClick = { editing = entry; showForm = true }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(entry.title, style = MaterialTheme.typography.titleSmall)
                            val dateTime = DateUtils.friendlyDate(entry.date) + (entry.time?.let { " · ${DateUtils.friendlyTime(it)}" } ?: "")
                            Text(dateTime, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (entry.doctorOrClinic.isNotBlank()) {
                                Text(entry.doctorOrClinic, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (entry.notes.isNotBlank()) {
                                Text(entry.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (entry.reminderEnabled) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    BloomDot(color = MaterialTheme.colorScheme.secondary, size = 8.dp)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Reminder on", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                        IconButton(onClick = { pendingDelete = entry }) {
                            Icon(Icons.Filled.Close, contentDescription = "Delete appointment")
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        AppointmentFormDialog(
            existing = editing,
            onDismiss = { showForm = false },
            onSave = { date, time, title, doctor, notes, reminder ->
                val current = editing
                if (current == null) viewModel.addAppointment(date, time, title, doctor, notes, reminder)
                else viewModel.updateAppointment(current, date, time, title, doctor, notes, reminder)
                showForm = false
            },
        )
    }

    pendingDelete?.let { entry ->
        ConfirmDeleteDialog(
            itemLabel = "this appointment",
            onConfirm = { viewModel.deleteAppointment(entry); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun AppointmentFormDialog(
    existing: MedicalAppointmentEntity?,
    onDismiss: () -> Unit,
    onSave: (LocalDate, LocalTime?, String, String, String, Boolean) -> Unit,
) {
    var date by remember { mutableStateOf(existing?.date ?: DateUtils.today()) }
    var time by remember { mutableStateOf(existing?.time) }
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var doctor by remember { mutableStateOf(existing?.doctorOrClinic ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var reminder by remember { mutableStateOf(existing?.reminderEnabled ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add appointment" else "Edit appointment") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g. Dentist checkup)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                DatePickerRow(label = "Date", date = date, onDateChange = { date = it })
                OptionalTimePickerRow(label = "Time", time = time, onTimeChange = { time = it })
                OutlinedTextField(
                    value = doctor,
                    onValueChange = { doctor = it },
                    label = { Text("Doctor / clinic (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Remind me", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = reminder, onCheckedChange = { reminder = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(date, time, title.trim(), doctor.trim(), notes.trim(), reminder) },
                enabled = title.isNotBlank(),
            ) { Text(if (existing == null) "Save" else "Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

// ============================================================================================
// Medicines
// ============================================================================================

@Composable
private fun MedicinesTab(viewModel: HealthHavenViewModel, medicines: List<MedicineEntity>) {
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<MedicineEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<MedicineEntity?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = ScreenPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionHeader(title = "Medicines", action = "Add", onAction = { editing = null; showForm = true })
        }
        if (medicines.isEmpty()) {
            item {
                EmptyState(
                    title = "No medicines added",
                    message = "Keep track of what you're taking, dosage, and schedule.",
                    actionLabel = "Add medicine",
                    onAction = { editing = null; showForm = true },
                )
            }
        } else {
            items(medicines, key = { it.id }) { entry ->
                BloomCard(onClick = { editing = entry; showForm = true }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BloomDot(
                                    color = if (entry.isActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant,
                                    size = 8.dp,
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(entry.name, style = MaterialTheme.typography.titleSmall)
                            }
                            val dosageSchedule = listOf(entry.dosage, entry.schedule).filter { it.isNotBlank() }.joinToString(" · ")
                            if (dosageSchedule.isNotBlank()) {
                                Text(dosageSchedule, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            val range = "Since ${DateUtils.friendlyDate(entry.startDate)}" +
                                (entry.endDate?.let { " until ${DateUtils.friendlyDate(it)}" } ?: "")
                            Text(range, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (entry.notes.isNotBlank()) {
                                Text(entry.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { pendingDelete = entry }) {
                            Icon(Icons.Filled.Close, contentDescription = "Delete medicine")
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        MedicineFormDialog(
            existing = editing,
            onDismiss = { showForm = false },
            onSave = { name, dosage, schedule, startDate, endDate, notes, isActive ->
                val current = editing
                if (current == null) viewModel.addMedicine(name, dosage, schedule, startDate, endDate, notes, isActive)
                else viewModel.updateMedicine(current, name, dosage, schedule, startDate, endDate, notes, isActive)
                showForm = false
            },
        )
    }

    pendingDelete?.let { entry ->
        ConfirmDeleteDialog(
            itemLabel = "this medicine",
            onConfirm = { viewModel.deleteMedicine(entry); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun MedicineFormDialog(
    existing: MedicineEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, LocalDate, LocalDate?, String, Boolean) -> Unit,
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var dosage by remember { mutableStateOf(existing?.dosage ?: "") }
    var schedule by remember { mutableStateOf(existing?.schedule ?: "") }
    var startDate by remember { mutableStateOf(existing?.startDate ?: DateUtils.today()) }
    var endDate by remember { mutableStateOf(existing?.endDate) }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var isActive by remember { mutableStateOf(existing?.isActive ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add medicine" else "Edit medicine") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage (e.g. 500mg)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = schedule,
                    onValueChange = { schedule = it },
                    label = { Text("Schedule (e.g. Twice daily)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                DatePickerRow(label = "Start date", date = startDate, onDateChange = { startDate = it })
                OptionalDatePickerRow(label = "End date", date = endDate, onDateChange = { endDate = it })
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Currently taking", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), dosage.trim(), schedule.trim(), startDate, endDate, notes.trim(), isActive) },
                enabled = name.isNotBlank(),
            ) { Text(if (existing == null) "Save" else "Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

// ============================================================================================
// Health notes
// ============================================================================================

@Composable
private fun NotesTab(viewModel: HealthHavenViewModel, notes: List<HealthNoteEntity>) {
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<HealthNoteEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<HealthNoteEntity?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = ScreenPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionHeader(title = "Health notes", action = "Add", onAction = { editing = null; showForm = true })
        }
        if (notes.isEmpty()) {
            item {
                EmptyState(
                    title = "No health notes yet",
                    message = "Jot down anything worth remembering about how you're feeling.",
                    actionLabel = "Add note",
                    onAction = { editing = null; showForm = true },
                )
            }
        } else {
            items(notes, key = { it.id }) { entry ->
                BloomCard(onClick = { editing = entry; showForm = true }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                DateUtils.friendlyDate(entry.date),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(entry.note, style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = { pendingDelete = entry }) {
                            Icon(Icons.Filled.Close, contentDescription = "Delete note")
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        NoteFormDialog(
            existing = editing,
            onDismiss = { showForm = false },
            onSave = { date, note ->
                val current = editing
                if (current == null) viewModel.addNote(date, note)
                else viewModel.updateNote(current, date, note)
                showForm = false
            },
        )
    }

    pendingDelete?.let { entry ->
        ConfirmDeleteDialog(
            itemLabel = "this note",
            onConfirm = { viewModel.deleteNote(entry); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun NoteFormDialog(
    existing: HealthNoteEntity?,
    onDismiss: () -> Unit,
    onSave: (LocalDate, String) -> Unit,
) {
    var date by remember { mutableStateOf(existing?.date ?: DateUtils.today()) }
    var note by remember { mutableStateOf(existing?.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add health note" else "Edit health note") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DatePickerRow(label = "Date", date = date, onDateChange = { date = it })
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(date, note.trim()) },
                enabled = note.isNotBlank(),
            ) { Text(if (existing == null) "Save" else "Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
