package com.bloomhaven.app.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloomhaven.app.core.navigation.MoreDestinations
import com.bloomhaven.app.core.navigation.NavRoutes
import com.bloomhaven.app.core.ui.BloomCard
import com.bloomhaven.app.core.ui.ScreenPadding
import com.bloomhaven.app.core.util.DateUtils
import com.bloomhaven.app.feature.gentletasks.TaskEntity
import com.bloomhaven.app.feature.planner.PlannerItemEntity

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel(), onOpenModule: (String) -> Unit = {}) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text(
                DateUtils.friendlyDateLong(DateUtils.today()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        state.homeSections.forEach { section ->
            when (section) {
                "eira_greeting" -> item { EiraGreetingSection(state, onOpenModule) }
                "today_schedule" -> item { TodayScheduleSection(state, onOpenModule) }
                "hydration" -> item { HydrationSection(state, onOpenModule) }
                "today_tasks" -> item { TodayTasksSection(state, onOpenModule) }
                "upcoming_events" -> item { UpcomingEventsSection(state, onOpenModule) }
            }
        }

        state.dailySummary?.let { summary ->
            item {
                BloomCard {
                    Text("Your day so far", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Text("More in Bloom Haven", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(MoreDestinations) { dest ->
                    androidx.compose.foundation.layout.Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onOpenModule(dest.route) }
                            .padding(8.dp),
                    ) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(52.dp)) {
                            androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                                Icon(dest.icon, contentDescription = dest.label, tint = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(dest.label, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun EiraGreetingSection(state: HomeUiState, onOpenModule: (String) -> Unit) {
    BloomCard(onClick = { onOpenModule(NavRoutes.EIRA) }) {
        Text(state.greeting ?: "Welcome back to Bloom Haven.", style = MaterialTheme.typography.headlineSmall)
        state.quote?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TodayScheduleSection(state: HomeUiState, onOpenModule: (String) -> Unit) {
    BloomCard(onClick = { onOpenModule(NavRoutes.PLANNER) }) {
        Text("Today's schedule", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        if (state.todaySchedule.isEmpty()) {
            Text("Nothing scheduled today.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            state.todaySchedule.take(4).forEach { item: PlannerItemEntity ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.title, style = MaterialTheme.typography.bodyMedium)
                    Text(item.time?.let { DateUtils.friendlyTime(it) } ?: "All day", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (state.todaySchedule.size > 4) {
                Text("+${state.todaySchedule.size - 4} more", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HydrationSection(state: HomeUiState, onOpenModule: (String) -> Unit) {
    BloomCard(onClick = { onOpenModule(NavRoutes.FLOW) }) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            CircularProgressIndicator(
                progress = { (state.waterTotalMl.toFloat() / state.waterGoalMl.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.size(40.dp),
                strokeWidth = 4.dp,
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Hydration", style = MaterialTheme.typography.titleSmall)
                Text("${state.waterTotalMl}ml of ${state.waterGoalMl}ml", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TodayTasksSection(state: HomeUiState, onOpenModule: (String) -> Unit) {
    BloomCard(onClick = { onOpenModule(NavRoutes.GENTLE_TASKS) }) {
        Text("Today's tasks", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        if (state.todayTasks.isEmpty()) {
            Text("Nothing due today.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            state.todayTasks.take(4).forEach { task: TaskEntity ->
                Text("• ${task.title}", style = MaterialTheme.typography.bodyMedium)
            }
            if (state.todayTasks.size > 4) {
                Text("+${state.todayTasks.size - 4} more", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun UpcomingEventsSection(state: HomeUiState, onOpenModule: (String) -> Unit) {
    BloomCard(onClick = { onOpenModule(NavRoutes.PLANNER) }) {
        Text("Upcoming important events", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        val important = state.upcomingEvents.filter { it.category in listOf("Important Date", "Event", "Appointment") }
        val toShow = important.ifEmpty { state.upcomingEvents }
        if (toShow.isEmpty()) {
            Text("Nothing coming up yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            toShow.take(4).forEach { item: PlannerItemEntity ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.title, style = MaterialTheme.typography.bodyMedium)
                    Text(DateUtils.friendlyDate(item.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
