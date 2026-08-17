package com.bloomhaven.app.feature.myhaven

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloomhaven.app.BuildConfig
import com.bloomhaven.app.core.data.NightHavenMode
import com.bloomhaven.app.core.ui.BloomCard
import com.bloomhaven.app.core.ui.BloomSecondaryButton
import com.bloomhaven.app.core.ui.ScreenPadding
import com.bloomhaven.app.core.ui.SectionHeader

private val HOME_SECTION_LABELS = mapOf(
    "eira_greeting" to "Eira's greeting",
    "today_schedule" to "Today's schedule",
    "hydration" to "Hydration",
    "today_tasks" to "Today's tasks",
    "upcoming_events" to "Upcoming important events",
)

@Composable
fun MyHavenScreen(viewModel: MyHavenViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    var editingName by remember { mutableStateOf(false) }
    var editingWorkSchedule by remember { mutableStateOf(false) }
    var editingWaterGoal by remember { mutableStateOf(false) }
    var editingQuietHours by remember { mutableStateOf(false) }
    var showResetConfirm1 by remember { mutableStateOf(false) }
    var showResetConfirm2 by remember { mutableStateOf(false) }

    LazyColumn(contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Text("My Haven", style = MaterialTheme.typography.headlineMedium)
            Text("Your space, set up your way.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            SectionHeader("Profile")
            Spacer(Modifier.height(8.dp))
            BloomCard(onClick = { editingName = true }) {
                Text("Name", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(settings.userName.ifBlank { "Not set — tap to add" }, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(8.dp))
            BloomCard(onClick = { editingWorkSchedule = true }) {
                Text("Work schedule note", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(settings.workScheduleNote.ifBlank { "Not set — tap to add (e.g. shift pattern)" }, style = MaterialTheme.typography.titleSmall)
                Text("Bloom Haven never guesses your schedule — you tell it.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            SectionHeader("Home customization")
            Spacer(Modifier.height(8.dp))
            BloomCard {
                Text("Choose which sections show on Home, and their order.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                HOME_SECTION_LABELS.forEach { (key, label) ->
                    val enabled = settings.homeSections.contains(key)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.moveHomeSection(key, -1) }) { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up") }
                            IconButton(onClick = { viewModel.moveHomeSection(key, 1) }) { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down") }
                            Switch(checked = enabled, onCheckedChange = { viewModel.toggleHomeSection(key) })
                        }
                    }
                }
            }
        }

        item {
            SectionHeader("Appearance & Night Haven")
            Spacer(Modifier.height(8.dp))
            BloomCard {
                Text("Night Haven switches to a softer, darker look.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    NightHavenMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = settings.nightHavenMode == mode,
                            onClick = { viewModel.setNightHavenMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, NightHavenMode.entries.size),
                        ) { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Reduced motion", modifier = Modifier.weight(1f))
                    Switch(checked = settings.reducedMotion, onCheckedChange = { viewModel.setReducedMotion(it) })
                }
            }
        }

        item {
            SectionHeader("Eira & privacy")
            Spacer(Modifier.height(8.dp))
            BloomCard {
                Text(
                    "Eira greets you gently when you open the app, using only what's stored on this device. She never becomes a mandatory chatbot.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Allow online AI messages")
                        Text(
                            "Off by default. If online Eira features are ever added, this must be on before anything personal leaves your device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = settings.onlineAiConsent, onCheckedChange = { viewModel.setOnlineAiConsent(it) })
                }
            }
        }

        item {
            SectionHeader("Gentle Nudges")
            Spacer(Modifier.height(8.dp))
            BloomCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Allow notifications", modifier = Modifier.weight(1f))
                    Switch(checked = settings.notificationsEnabled, onCheckedChange = { viewModel.setNotificationsEnabled(it) })
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { editingQuietHours = true }) {
                    Text("Quiet hours: ${settings.quietHoursStart} – ${settings.quietHoursEnd}")
                }
                Text(
                    "Nudges stay quiet during these hours — a soft presence, never an alarm.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            SectionHeader("Security")
            Spacer(Modifier.height(8.dp))
            BloomCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Biometric app lock")
                        Text("Unlock with fingerprint, face, or device passcode.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = settings.biometricLockEnabled, onCheckedChange = { viewModel.setBiometricLock(it) })
                }
            }
        }

        item {
            SectionHeader("Permissions")
            Spacer(Modifier.height(8.dp))
            BloomCard {
                Text("Manage notification and photo access for Bloom Haven from system settings.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                BloomSecondaryButton(text = "Open app permissions", onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                })
            }
        }

        item {
            SectionHeader("Data management")
            Spacer(Modifier.height(8.dp))
            BloomCard {
                Text(
                    "Backup isn't available yet — before Bloom Haven implements one, it should ask how you'd like your data protected (an encrypted backup you control, phone-only storage, or something else). For now, everything lives only on this device.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = { showResetConfirm1 = true }) {
                    Text("Reset all Bloom Haven data…", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        item {
            SectionHeader("About")
            Spacer(Modifier.height(8.dp))
            BloomCard {
                Text("Bloom Haven", style = MaterialTheme.typography.titleSmall)
                Text("Version ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text("Your private digital home. Grow gently, live fully.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (editingName) {
        TextFieldDialog(title = "Your name", initial = settings.userName, onDismiss = { editingName = false }, onSave = { viewModel.setUserName(it); editingName = false })
    }
    if (editingWorkSchedule) {
        TextFieldDialog(title = "Work schedule note", initial = settings.workScheduleNote, onDismiss = { editingWorkSchedule = false }, onSave = { viewModel.setWorkScheduleNote(it); editingWorkSchedule = false })
    }
    if (editingQuietHours) {
        QuietHoursDialog(
            start = settings.quietHoursStart,
            end = settings.quietHoursEnd,
            onDismiss = { editingQuietHours = false },
            onSave = { s, e -> viewModel.setQuietHours(s, e); editingQuietHours = false },
        )
    }
    if (showResetConfirm1) {
        AlertDialog(
            onDismissRequest = { showResetConfirm1 = false },
            title = { Text("Reset all Bloom Haven data?") },
            text = { Text("This permanently deletes every record in every module — expenses, habits, planner items, Petrova's history, everything. This can't be undone.") },
            confirmButton = { TextButton(onClick = { showResetConfirm1 = false; showResetConfirm2 = true }) { Text("Continue", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showResetConfirm1 = false }) { Text("Cancel") } },
        )
    }
    if (showResetConfirm2) {
        AlertDialog(
            onDismissRequest = { showResetConfirm2 = false },
            title = { Text("Are you absolutely sure?") },
            text = { Text("There is no undo. Everything you've recorded in Bloom Haven will be gone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.resetAllData { showResetConfirm2 = false } }) {
                    Text("Yes, delete everything", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showResetConfirm2 = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun TextFieldDialog(title: String, initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = value, onValueChange = { value = it }, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { TextButton(onClick = { onSave(value.trim()) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun QuietHoursDialog(start: String, end: String, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var s by remember { mutableStateOf(start) }
    var e by remember { mutableStateOf(end) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quiet hours") },
        text = {
            Column {
                OutlinedTextField(value = s, onValueChange = { s = it }, label = { Text("Start (HH:mm)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = e, onValueChange = { e = it }, label = { Text("End (HH:mm)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onSave(s, e) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
