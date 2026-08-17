package com.bloomhaven.app.feature.petrova

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.bloomhaven.app.core.ui.BloomCard
import com.bloomhaven.app.core.ui.BloomSecondaryButton
import com.bloomhaven.app.core.ui.ConfirmDeleteDialog
import com.bloomhaven.app.core.ui.EmptyState
import com.bloomhaven.app.core.ui.ScreenPadding
import com.bloomhaven.app.core.ui.SectionHeader
import com.bloomhaven.app.core.util.CurrencyUtils
import com.bloomhaven.app.core.util.DateUtils
import com.bloomhaven.app.core.util.PhotoStorage
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.time.ZoneOffset

private enum class PetrovaTab(val label: String, val icon: ImageVector) {
    FEEDING("Feeding", Icons.Filled.Restaurant),
    GROOMING("Grooming", Icons.Filled.ContentCut),
    VET("Vet Visits", Icons.Filled.LocalHospital),
    MEDICINE("Medicines", Icons.Filled.Medication),
    WEIGHT("Weight", Icons.Filled.MonitorWeight),
    EXPENSES("Expenses", Icons.Filled.Payments),
}

@Composable
fun PetrovaScreen(viewModel: PetrovaViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    var showEditProfile by remember { mutableStateOf(false) }
    var showAddMemory by remember { mutableStateOf(false) }
    var fullScreenPhotoPath by remember { mutableStateOf<String?>(null) }
    var pendingDeleteMemory by remember { mutableStateOf<PetrovaMemoryEntity?>(null) }

    var selectedTab by remember { mutableStateOf(PetrovaTab.FEEDING) }
    var showAddRecord by remember { mutableStateOf(false) }

    var pendingDeleteFeeding by remember { mutableStateOf<PetrovaFeedingEntity?>(null) }
    var pendingDeleteGrooming by remember { mutableStateOf<PetrovaGroomingEntity?>(null) }
    var pendingDeleteVet by remember { mutableStateOf<PetrovaVetVisitEntity?>(null) }
    var pendingDeleteMedicine by remember { mutableStateOf<PetrovaMedicineEntity?>(null) }
    var pendingDeleteWeight by remember { mutableStateOf<PetrovaWeightEntity?>(null) }
    var pendingDeleteExpense by remember { mutableStateOf<PetrovaExpenseEntity?>(null) }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
            contentPadding = ScreenPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text("Petrova", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Her whole story, one gentle scrapbook.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                ProfileCard(profile = state.profile, onEdit = { showEditProfile = true })
            }

            item {
                SectionHeader(title = "Timeline", action = "Add moment", onAction = { showAddMemory = true })
            }

            if (state.memories.isEmpty()) {
                item {
                    EmptyState(
                        title = "No moments yet",
                        message = "Add Petrova's first memory to start the scrapbook.",
                        actionLabel = "Add moment",
                        onAction = { showAddMemory = true },
                    )
                }
            } else {
                items(state.memories, key = { "memory_${it.id}" }) { memory ->
                    MemoryCard(
                        memory = memory,
                        onPhotoClick = { fullScreenPhotoPath = it },
                        onDelete = { pendingDeleteMemory = memory },
                    )
                }
            }

            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.background,
                ) {
                    PetrovaTab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = { Text(tab.label) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                        )
                    }
                }
            }

            item {
                SectionHeader(title = selectedTab.label, action = "Add", onAction = { showAddRecord = true })
            }

            when (selectedTab) {
                PetrovaTab.FEEDING -> feedingSection(state.feedings, onAdd = { showAddRecord = true }, onDelete = { pendingDeleteFeeding = it })
                PetrovaTab.GROOMING -> groomingSection(state.groomings, onAdd = { showAddRecord = true }, onDelete = { pendingDeleteGrooming = it })
                PetrovaTab.VET -> vetSection(state.vetVisits, onAdd = { showAddRecord = true }, onDelete = { pendingDeleteVet = it })
                PetrovaTab.MEDICINE -> medicineSection(state.medicines, onAdd = { showAddRecord = true }, onDelete = { pendingDeleteMedicine = it })
                PetrovaTab.WEIGHT -> weightSection(state.weights, onAdd = { showAddRecord = true }, onDelete = { pendingDeleteWeight = it })
                PetrovaTab.EXPENSES -> expenseSection(state.expenses, onAdd = { showAddRecord = true }, onDelete = { pendingDeleteExpense = it })
            }
        }
    }

    if (showEditProfile) {
        EditProfileDialog(
            profile = state.profile,
            onDismiss = { showEditProfile = false },
            onSave = { name, species, breed, birthDate, photoPath ->
                viewModel.saveProfile(name, species, breed, birthDate, photoPath)
            },
        )
    }

    if (showAddMemory) {
        AddMemoryDialog(
            onDismiss = { showAddMemory = false },
            onSave = { date, title, note, photoPath, isMilestone ->
                viewModel.addMemory(date, title, note, photoPath, isMilestone)
            },
        )
    }

    if (showAddRecord) {
        when (selectedTab) {
            PetrovaTab.FEEDING -> AddFeedingDialog(
                onDismiss = { showAddRecord = false },
                onSave = { date, time, food, notes -> viewModel.addFeeding(date, time, food, notes) },
            )
            PetrovaTab.GROOMING -> AddGroomingDialog(
                onDismiss = { showAddRecord = false },
                onSave = { date, type, notes -> viewModel.addGrooming(date, type, notes) },
            )
            PetrovaTab.VET -> AddVetVisitDialog(
                onDismiss = { showAddRecord = false },
                onSave = { date, reason, notes, nextVisit -> viewModel.addVetVisit(date, reason, notes, nextVisit) },
            )
            PetrovaTab.MEDICINE -> AddMedicineDialog(
                onDismiss = { showAddRecord = false },
                onSave = { date, name, isVaccination, notes -> viewModel.addMedicine(date, name, isVaccination, notes) },
            )
            PetrovaTab.WEIGHT -> AddWeightDialog(
                onDismiss = { showAddRecord = false },
                onSave = { date, weightKg -> viewModel.addWeight(date, weightKg) },
            )
            PetrovaTab.EXPENSES -> AddExpenseDialog(
                onDismiss = { showAddRecord = false },
                onSave = { date, amount, category, note -> viewModel.addExpense(date, amount, category, note) },
            )
        }
    }

    fullScreenPhotoPath?.let { path ->
        PhotoFullScreenDialog(path = path, onDismiss = { fullScreenPhotoPath = null })
    }

    pendingDeleteMemory?.let { memory ->
        ConfirmDeleteDialog(
            itemLabel = "this memory",
            onConfirm = { viewModel.deleteMemory(memory); pendingDeleteMemory = null },
            onDismiss = { pendingDeleteMemory = null },
        )
    }
    pendingDeleteFeeding?.let { entry ->
        ConfirmDeleteDialog(
            itemLabel = "this feeding entry",
            onConfirm = { viewModel.deleteFeeding(entry); pendingDeleteFeeding = null },
            onDismiss = { pendingDeleteFeeding = null },
        )
    }
    pendingDeleteGrooming?.let { entry ->
        ConfirmDeleteDialog(
            itemLabel = "this grooming entry",
            onConfirm = { viewModel.deleteGrooming(entry); pendingDeleteGrooming = null },
            onDismiss = { pendingDeleteGrooming = null },
        )
    }
    pendingDeleteVet?.let { entry ->
        ConfirmDeleteDialog(
            itemLabel = "this vet visit",
            onConfirm = { viewModel.deleteVetVisit(entry); pendingDeleteVet = null },
            onDismiss = { pendingDeleteVet = null },
        )
    }
    pendingDeleteMedicine?.let { entry ->
        ConfirmDeleteDialog(
            itemLabel = "this record",
            onConfirm = { viewModel.deleteMedicine(entry); pendingDeleteMedicine = null },
            onDismiss = { pendingDeleteMedicine = null },
        )
    }
    pendingDeleteWeight?.let { entry ->
        ConfirmDeleteDialog(
            itemLabel = "this weight entry",
            onConfirm = { viewModel.deleteWeight(entry); pendingDeleteWeight = null },
            onDismiss = { pendingDeleteWeight = null },
        )
    }
    pendingDeleteExpense?.let { entry ->
        ConfirmDeleteDialog(
            itemLabel = "this expense",
            onConfirm = { viewModel.deleteExpense(entry); pendingDeleteExpense = null },
            onDismiss = { pendingDeleteExpense = null },
        )
    }
}

// --- Section builders (one per tab) ---

private fun LazyListScope.feedingSection(
    entries: List<PetrovaFeedingEntity>,
    onAdd: () -> Unit,
    onDelete: (PetrovaFeedingEntity) -> Unit,
) {
    if (entries.isEmpty()) {
        item {
            EmptyState(
                title = "No feeding logged yet",
                message = "Log Petrova's first meal.",
                actionLabel = "Add feeding",
                onAction = onAdd,
            )
        }
    } else {
        items(entries, key = { "feeding_${it.id}" }) { entry ->
            BloomCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.food, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${DateUtils.friendlyDate(entry.date)} · ${DateUtils.friendlyTime(entry.time)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (entry.notes.isNotBlank()) {
                            Text(entry.notes, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    IconButton(onClick = { onDelete(entry) }) { Icon(Icons.Filled.Close, contentDescription = "Delete") }
                }
            }
        }
    }
}

private fun LazyListScope.groomingSection(
    entries: List<PetrovaGroomingEntity>,
    onAdd: () -> Unit,
    onDelete: (PetrovaGroomingEntity) -> Unit,
) {
    if (entries.isEmpty()) {
        item {
            EmptyState(
                title = "No grooming logged yet",
                message = "Log Petrova's first grooming session.",
                actionLabel = "Add grooming",
                onAction = onAdd,
            )
        }
    } else {
        items(entries, key = { "grooming_${it.id}" }) { entry ->
            BloomCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.type, style = MaterialTheme.typography.titleSmall)
                        Text(DateUtils.friendlyDate(entry.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (entry.notes.isNotBlank()) {
                            Text(entry.notes, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    IconButton(onClick = { onDelete(entry) }) { Icon(Icons.Filled.Close, contentDescription = "Delete") }
                }
            }
        }
    }
}

private fun LazyListScope.vetSection(
    entries: List<PetrovaVetVisitEntity>,
    onAdd: () -> Unit,
    onDelete: (PetrovaVetVisitEntity) -> Unit,
) {
    if (entries.isEmpty()) {
        item {
            EmptyState(
                title = "No vet visits yet",
                message = "Log Petrova's first vet visit.",
                actionLabel = "Add vet visit",
                onAction = onAdd,
            )
        }
    } else {
        items(entries, key = { "vet_${it.id}" }) { entry ->
            BloomCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.reason, style = MaterialTheme.typography.titleSmall)
                        Text(DateUtils.friendlyDate(entry.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (entry.notes.isNotBlank()) {
                            Text(entry.notes, style = MaterialTheme.typography.bodyMedium)
                        }
                        entry.nextVisitDate?.let {
                            Text(
                                "Next visit: ${DateUtils.friendlyDate(it)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }
                    IconButton(onClick = { onDelete(entry) }) { Icon(Icons.Filled.Close, contentDescription = "Delete") }
                }
            }
        }
    }
}

private fun LazyListScope.medicineSection(
    entries: List<PetrovaMedicineEntity>,
    onAdd: () -> Unit,
    onDelete: (PetrovaMedicineEntity) -> Unit,
) {
    if (entries.isEmpty()) {
        item {
            EmptyState(
                title = "No medicines logged yet",
                message = "Log a medicine dose or vaccination for Petrova.",
                actionLabel = "Add record",
                onAction = onAdd,
            )
        }
    } else {
        items(entries, key = { "medicine_${it.id}" }) { entry ->
            BloomCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.name, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${DateUtils.friendlyDate(entry.date)} · ${if (entry.isVaccination) "Vaccination" else "Medicine"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (entry.notes.isNotBlank()) {
                            Text(entry.notes, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    IconButton(onClick = { onDelete(entry) }) { Icon(Icons.Filled.Close, contentDescription = "Delete") }
                }
            }
        }
    }
}

private fun LazyListScope.weightSection(
    entries: List<PetrovaWeightEntity>,
    onAdd: () -> Unit,
    onDelete: (PetrovaWeightEntity) -> Unit,
) {
    if (entries.isEmpty()) {
        item {
            EmptyState(
                title = "No weight logged yet",
                message = "Log Petrova's first weight measurement.",
                actionLabel = "Add weight",
                onAction = onAdd,
            )
        }
    } else {
        items(entries, key = { "weight_${it.id}" }) { entry ->
            BloomCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${entry.weightKg} kg", style = MaterialTheme.typography.titleSmall)
                        Text(DateUtils.friendlyDate(entry.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onDelete(entry) }) { Icon(Icons.Filled.Close, contentDescription = "Delete") }
                }
            }
        }
    }
}

private fun LazyListScope.expenseSection(
    entries: List<PetrovaExpenseEntity>,
    onAdd: () -> Unit,
    onDelete: (PetrovaExpenseEntity) -> Unit,
) {
    if (entries.isEmpty()) {
        item {
            EmptyState(
                title = "No expenses logged yet",
                message = "Log Petrova's first expense.",
                actionLabel = "Add expense",
                onAction = onAdd,
            )
        }
    } else {
        items(entries, key = { "expense_${it.id}" }) { entry ->
            BloomCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(CurrencyUtils.format(entry.amount), style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${DateUtils.friendlyDate(entry.date)} · ${entry.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (entry.note.isNotBlank()) {
                            Text(entry.note, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    IconButton(onClick = { onDelete(entry) }) { Icon(Icons.Filled.Close, contentDescription = "Delete") }
                }
            }
        }
    }
}

// --- Cards ---

@Composable
private fun ProfileCard(profile: PetrovaProfileEntity, onEdit: () -> Unit) {
    BloomCard(onClick = onEdit) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (profile.photoPath != null) {
                AsyncImage(
                    model = File(profile.photoPath),
                    contentDescription = "Petrova's photo",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(profile.name.ifBlank { "Petrova" }, style = MaterialTheme.typography.titleLarge)
                val breedLine = listOf(profile.species, profile.breed).filter { it.isNotBlank() }.joinToString(" · ")
                if (breedLine.isNotBlank()) {
                    Text(breedLine, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                profile.birthDate?.let {
                    Text(ageLabel(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Filled.Edit, contentDescription = "Edit profile", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MemoryCard(memory: PetrovaMemoryEntity, onPhotoClick: (String) -> Unit, onDelete: () -> Unit) {
    BloomCard {
        val photoPath = memory.photoPath
        if (photoPath != null) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = memory.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onPhotoClick(photoPath) },
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.height(12.dp))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (memory.isMilestone) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Milestone",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(memory.title, style = MaterialTheme.typography.titleSmall)
                }
                Text(DateUtils.friendlyDate(memory.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (memory.note.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(memory.note, style = MaterialTheme.typography.bodyMedium)
                }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Close, contentDescription = "Delete memory") }
        }
    }
}

@Composable
private fun PhotoFullScreenDialog(path: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            AsyncImage(
                model = File(path),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

// --- Add / edit dialogs ---

@Composable
private fun EditProfileDialog(
    profile: PetrovaProfileEntity,
    onDismiss: () -> Unit,
    onSave: (name: String, species: String, breed: String, birthDate: LocalDate?, photoPath: String?) -> Unit,
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(profile.name) }
    var species by remember { mutableStateOf(profile.species) }
    var breed by remember { mutableStateOf(profile.breed) }
    var birthDate by remember { mutableStateOf(profile.birthDate) }
    var photoPath by remember { mutableStateOf(profile.photoPath) }
    var showDatePicker by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { photoPath = PhotoStorage.copyIntoPrivateStorage(context, it) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Petrova's profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val currentPhoto = photoPath
                    if (currentPhoto != null) {
                        AsyncImage(
                            model = File(currentPhoto),
                            contentDescription = null,
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .clickable { photoLauncher.launch("image/*") },
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable { photoLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.PhotoCamera, contentDescription = "Add photo", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = species, onValueChange = { species = it }, label = { Text("Species") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = breed, onValueChange = { breed = it }, label = { Text("Breed") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(birthDate?.let { DateUtils.friendlyDateLong(it) } ?: "Set birth date")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name.trim(), species.trim(), breed.trim(), birthDate, photoPath); onDismiss() }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )

    if (showDatePicker) {
        BloomDatePickerDialog(
            initialDate = birthDate ?: DateUtils.today(),
            onDismiss = { showDatePicker = false },
            onConfirm = { birthDate = it; showDatePicker = false },
        )
    }
}

@Composable
private fun AddMemoryDialog(
    onDismiss: () -> Unit,
    onSave: (date: LocalDate, title: String, note: String, photoPath: String?, isMilestone: Boolean) -> Unit,
) {
    val context = LocalContext.current
    var date by remember { mutableStateOf(DateUtils.today()) }
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var isMilestone by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { photoPath = PhotoStorage.copyIntoPrivateStorage(context, it) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a moment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) { Text(DateUtils.friendlyDate(date)) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isMilestone, onCheckedChange = { isMilestone = it })
                    Text("Mark as milestone")
                }
                val currentPhoto = photoPath
                if (currentPhoto != null) {
                    AsyncImage(
                        model = File(currentPhoto),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop,
                    )
                }
                BloomSecondaryButton(
                    text = if (currentPhoto == null) "Add photo" else "Change photo",
                    onClick = { photoLauncher.launch("image/*") },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(date, title.trim(), note.trim(), photoPath, isMilestone); onDismiss() },
                enabled = title.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )

    if (showDatePicker) {
        BloomDatePickerDialog(initialDate = date, onDismiss = { showDatePicker = false }, onConfirm = { date = it; showDatePicker = false })
    }
}

@Composable
private fun AddFeedingDialog(
    onDismiss: () -> Unit,
    onSave: (date: LocalDate, time: LocalTime, food: String, notes: String) -> Unit,
) {
    var date by remember { mutableStateOf(DateUtils.today()) }
    var time by remember { mutableStateOf(LocalTime.now()) }
    var food by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add feeding") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = food, onValueChange = { food = it }, label = { Text("Food") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) { Text(DateUtils.friendlyDate(date)) }
                OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.fillMaxWidth()) { Text(DateUtils.friendlyTime(time)) }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(date, time, food.trim(), notes.trim()); onDismiss() }, enabled = food.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )

    if (showDatePicker) {
        BloomDatePickerDialog(initialDate = date, onDismiss = { showDatePicker = false }, onConfirm = { date = it; showDatePicker = false })
    }
    if (showTimePicker) {
        BloomTimePickerDialog(initialTime = time, onDismiss = { showTimePicker = false }, onConfirm = { time = it; showTimePicker = false })
    }
}

@Composable
private fun AddGroomingDialog(
    onDismiss: () -> Unit,
    onSave: (date: LocalDate, type: String, notes: String) -> Unit,
) {
    var date by remember { mutableStateOf(DateUtils.today()) }
    var type by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add grooming") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type (bath, nail trim…)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) { Text(DateUtils.friendlyDate(date)) }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(date, type.trim(), notes.trim()); onDismiss() }, enabled = type.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )

    if (showDatePicker) {
        BloomDatePickerDialog(initialDate = date, onDismiss = { showDatePicker = false }, onConfirm = { date = it; showDatePicker = false })
    }
}

@Composable
private fun AddVetVisitDialog(
    onDismiss: () -> Unit,
    onSave: (date: LocalDate, reason: String, notes: String, nextVisitDate: LocalDate?) -> Unit,
) {
    var date by remember { mutableStateOf(DateUtils.today()) }
    var reason by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var nextVisitDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showNextVisitPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add vet visit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) { Text(DateUtils.friendlyDate(date)) }
                OutlinedButton(onClick = { showNextVisitPicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(nextVisitDate?.let { "Next visit: ${DateUtils.friendlyDate(it)}" } ?: "Set next visit date (optional)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(date, reason.trim(), notes.trim(), nextVisitDate); onDismiss() }, enabled = reason.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )

    if (showDatePicker) {
        BloomDatePickerDialog(initialDate = date, onDismiss = { showDatePicker = false }, onConfirm = { date = it; showDatePicker = false })
    }
    if (showNextVisitPicker) {
        BloomDatePickerDialog(
            initialDate = nextVisitDate ?: date,
            onDismiss = { showNextVisitPicker = false },
            onConfirm = { nextVisitDate = it; showNextVisitPicker = false },
        )
    }
}

@Composable
private fun AddMedicineDialog(
    onDismiss: () -> Unit,
    onSave: (date: LocalDate, name: String, isVaccination: Boolean, notes: String) -> Unit,
) {
    var date by remember { mutableStateOf(DateUtils.today()) }
    var name by remember { mutableStateOf("") }
    var isVaccination by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add medicine or vaccination") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isVaccination, onCheckedChange = { isVaccination = it })
                    Text("This is a vaccination")
                }
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) { Text(DateUtils.friendlyDate(date)) }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(date, name.trim(), isVaccination, notes.trim()); onDismiss() }, enabled = name.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )

    if (showDatePicker) {
        BloomDatePickerDialog(initialDate = date, onDismiss = { showDatePicker = false }, onConfirm = { date = it; showDatePicker = false })
    }
}

@Composable
private fun AddWeightDialog(
    onDismiss: () -> Unit,
    onSave: (date: LocalDate, weightKg: Double) -> Unit,
) {
    var date by remember { mutableStateOf(DateUtils.today()) }
    var weightText by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val weightKg = weightText.toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add weight") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { input -> weightText = input.filter { it.isDigit() || it == '.' } },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) { Text(DateUtils.friendlyDate(date)) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { weightKg?.let { onSave(date, it) }; onDismiss() },
                enabled = weightKg != null && weightKg > 0,
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )

    if (showDatePicker) {
        BloomDatePickerDialog(initialDate = date, onDismiss = { showDatePicker = false }, onConfirm = { date = it; showDatePicker = false })
    }
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (date: LocalDate, amount: Double, category: String, note: String) -> Unit,
) {
    var date by remember { mutableStateOf(DateUtils.today()) }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val amount = amountText.toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input -> amountText = input.filter { it.isDigit() || it == '.' } },
                    label = { Text("Amount") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) { Text(DateUtils.friendlyDate(date)) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { amount?.let { onSave(date, it, category.trim(), note.trim()) }; onDismiss() },
                enabled = amount != null && amount > 0 && category.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )

    if (showDatePicker) {
        BloomDatePickerDialog(initialDate = date, onDismiss = { showDatePicker = false }, onConfirm = { date = it; showDatePicker = false })
    }
}

// --- Shared date/time pickers ---

@Composable
private fun BloomDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val initialMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis
                if (millis != null) {
                    onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                } else {
                    onDismiss()
                }
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DatePicker(state = state)
    }
}

@Composable
private fun BloomTimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    val state = rememberTimePickerState(initialHour = initialTime.hour, initialMinute = initialTime.minute, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select time") },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun ageLabel(birthDate: LocalDate): String {
    val period = Period.between(birthDate, DateUtils.today())
    val years = period.years
    val months = period.months
    return when {
        years > 0 && months > 0 -> "$years yr $months mo old"
        years > 0 -> "$years yr old"
        months > 0 -> "$months mo old"
        else -> "${period.days.coerceAtLeast(0)} d old"
    }
}
