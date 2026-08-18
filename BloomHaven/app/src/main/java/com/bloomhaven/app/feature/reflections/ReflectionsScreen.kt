package com.bloomhaven.app.feature.reflections

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloomhaven.app.core.ui.BloomCard
import com.bloomhaven.app.core.ui.ConfirmDeleteDialog
import com.bloomhaven.app.core.ui.EmptyState
import com.bloomhaven.app.core.ui.ScreenPadding
import com.bloomhaven.app.core.util.DateUtils

@Composable
fun ReflectionsScreen(viewModel: ReflectionsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    var isComposerOpen by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<ReflectionEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<ReflectionEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingEntry = null
                isComposerOpen = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "New reflection")
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
                Text("Reflections", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "A quiet place to note how you're doing, whenever you'd like.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (state.entries.isEmpty()) {
                item {
                    EmptyState(
                        title = "No reflections yet",
                        message = "Your first entry is just a tap away — there's no wrong way to start.",
                        actionLabel = "New reflection",
                        onAction = {
                            editingEntry = null
                            isComposerOpen = true
                        },
                    )
                }
            } else {
                items(state.entries, key = { it.id }) { entry ->
                    BloomCard(onClick = {
                        editingEntry = entry
                        isComposerOpen = true
                    }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(entry.mood.emoji, style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(entry.mood.label, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        DateUtils.friendlyDate(entry.date),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            IconButton(onClick = { pendingDelete = entry }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete reflection")
                            }
                        }
                        if (entry.text.isNotBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                entry.text,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }

    if (isComposerOpen) {
        ReflectionComposerDialog(
            existing = editingEntry,
            onDismiss = { isComposerOpen = false },
            onSave = { mood, text ->
                val current = editingEntry
                if (current == null) {
                    viewModel.addReflection(DateUtils.today(), mood, text)
                } else {
                    viewModel.updateReflection(current, current.date, mood, text)
                }
                isComposerOpen = false
            },
        )
    }

    pendingDelete?.let { entry ->
        ConfirmDeleteDialog(
            itemLabel = "this reflection",
            onConfirm = { viewModel.deleteReflection(entry); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun ReflectionComposerDialog(
    existing: ReflectionEntity?,
    onDismiss: () -> Unit,
    onSave: (Mood, String) -> Unit,
) {
    var selectedMood by remember { mutableStateOf(existing?.mood) }
    var text by remember { mutableStateOf(existing?.text ?: "") }

    val canSave = selectedMood != null || text.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New reflection" else "Edit reflection") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (existing != null) {
                    Text(
                        DateUtils.friendlyDate(existing.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(10.dp))
                }
                Text("How are you feeling?", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Mood.entries.forEach { mood ->
                        FilterChip(
                            selected = selectedMood == mood,
                            onClick = { selectedMood = if (selectedMood == mood) null else mood },
                            label = { Text(mood.emoji) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            ),
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("What's on your mind?") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { selectedMood?.let { onSave(it, text) } ?: onSave(Mood.OKAY, text) }, enabled = canSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
