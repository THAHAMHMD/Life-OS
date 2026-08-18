package com.bloomhaven.app.feature.flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.CircularProgressIndicator
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
import com.bloomhaven.app.core.ui.BloomPrimaryButton
import com.bloomhaven.app.core.ui.ConfirmDeleteDialog
import com.bloomhaven.app.core.ui.EmptyState
import com.bloomhaven.app.core.ui.ScreenPadding
import com.bloomhaven.app.core.util.DateUtils
import androidx.compose.foundation.layout.width

@Composable
fun FlowScreen(viewModel: FlowViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var customAmount by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<WaterLogEntity?>(null) }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
            contentPadding = ScreenPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text("Flow", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Gentle hydration tracking, day by day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { viewModel.selectDate(state.selectedDate.minusDays(1)) }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous day")
                    }
                    Text(DateUtils.friendlyDate(state.selectedDate), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { viewModel.selectDate(state.selectedDate.plusDays(1)) }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Next day")
                    }
                }
            }

            item {
                BloomCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { (state.totalMl.toFloat() / state.goalMl.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier.size(72.dp),
                                strokeWidth = 7.dp,
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.secondaryContainer,
                            )
                            Icon(Icons.Filled.Water, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("${state.totalMl} ml", style = MaterialTheme.typography.headlineSmall)
                            Text("of ${state.goalMl} ml goal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(150, 250, 500).forEach { amt ->
                            TextButton(onClick = { viewModel.logWater(amt) }) { Text("+${amt}ml") }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = customAmount,
                            onValueChange = { customAmount = it.filter { c -> c.isDigit() } },
                            label = { Text("Custom ml") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        Spacer(Modifier.width(10.dp))
                        BloomPrimaryButton(text = "Add", onClick = {
                            customAmount.toIntOrNull()?.let { if (it > 0) viewModel.logWater(it) }
                            customAmount = ""
                        })
                    }
                }
            }

            item {
                Text("Today's log", style = MaterialTheme.typography.titleMedium)
            }

            if (state.entries.isEmpty()) {
                item {
                    EmptyState(
                        title = "Nothing logged yet",
                        message = "Add your first glass of water for ${DateUtils.friendlyDate(state.selectedDate).lowercase()}.",
                    )
                }
            } else {
                items(state.entries, key = { it.id }) { entry ->
                    BloomCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text("${entry.amountMl} ml", style = MaterialTheme.typography.titleSmall)
                                Text(DateUtils.friendlyTime(entry.time), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { pendingDelete = entry }) {
                                Icon(Icons.Filled.Close, contentDescription = "Delete entry")
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { entry ->
        ConfirmDeleteDialog(
            itemLabel = "this water entry",
            onConfirm = { viewModel.delete(entry); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}
