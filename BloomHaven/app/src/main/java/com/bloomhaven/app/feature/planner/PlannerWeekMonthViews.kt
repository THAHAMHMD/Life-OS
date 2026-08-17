package com.bloomhaven.app.feature.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bloomhaven.app.core.ui.BloomDot
import com.bloomhaven.app.core.ui.EmptyState
import com.bloomhaven.app.core.ui.ScreenPadding
import com.bloomhaven.app.core.util.DateUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeekView(
    state: PlannerUiState,
    viewModel: PlannerViewModel,
    onEdit: (PlannerItemEntity) -> Unit,
    onDelete: (PlannerItemEntity) -> Unit,
) {
    val weekStart = state.selectedDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val days = (0..6).map { weekStart.plusDays(it.toLong()) }

    Column {
        androidx.compose.foundation.lazy.LazyRow(
            contentPadding = ScreenPadding,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(days) { day ->
                val selected = day == state.selectedDate
                val hasItems = state.weekDatesWithItems.contains(day)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                        .clickable { viewModel.selectDate(day) }
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                ) {
                    Text(day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()), style = MaterialTheme.typography.bodySmall)
                    Text("${day.dayOfMonth}", style = MaterialTheme.typography.titleSmall)
                    if (hasItems) BloomDot(color = MaterialTheme.colorScheme.primary, size = 6.dp)
                }
            }
        }
        DayViewList(state, viewModel, onEdit, onDelete)
    }
}

@Composable
private fun DayViewList(state: PlannerUiState, viewModel: PlannerViewModel, onEdit: (PlannerItemEntity) -> Unit, onDelete: (PlannerItemEntity) -> Unit) {
    LazyColumn(contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (state.dayItems.isEmpty()) {
            item { EmptyState(title = "Nothing planned", message = "Nothing for ${DateUtils.friendlyDate(state.selectedDate).lowercase()} yet.") }
        } else {
            items(state.dayItems, key = { it.id }) { item ->
                PlannerItemCard(item, onToggle = { viewModel.toggleCompleted(item) }, onEdit = { onEdit(item) }, onDelete = { onDelete(item) })
            }
        }
    }
}

@Composable
fun MonthView(state: PlannerUiState, viewModel: PlannerViewModel) {
    val monthStart = state.selectedDate.withDayOfMonth(1)
    val firstCell = monthStart.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val daysInGrid = (0 until 42).map { firstCell.plusDays(it.toLong()) }

    Column(Modifier.padding(horizontal = 20.dp)) {
        Text(DateUtils.monthLabel(state.selectedDate), style = MaterialTheme.typography.titleMedium)
        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
            items(daysInGrid) { day: LocalDate ->
                val inMonth = day.month == monthStart.month
                val hasItems = state.monthDatesWithItems.contains(day)
                val isToday = day == LocalDate.now()
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                day == state.selectedDate -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surface
                            },
                        )
                        .clickable { viewModel.selectDate(day); viewModel.setViewMode(PlannerViewMode.DAY) },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${day.dayOfMonth}",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = if (day == state.selectedDate) MaterialTheme.colorScheme.onPrimary
                            else if (!inMonth) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.onSurface,
                        )
                        if (hasItems) {
                            BloomDot(
                                color = if (day == state.selectedDate) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary,
                                size = 4.dp,
                            )
                        }
                    }
                }
            }
        }
    }
}
