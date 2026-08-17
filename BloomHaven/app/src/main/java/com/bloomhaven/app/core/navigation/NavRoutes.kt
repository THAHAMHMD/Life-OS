package com.bloomhaven.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Water
import androidx.compose.ui.graphics.vector.ImageVector

/** Top-level route names. Each feature module owns its own nested routes/args separately. */
object NavRoutes {
    const val HOME = "home"
    const val PLANNER = "planner"
    const val MONEY = "money_haven"
    const val HABITS = "bloom_habits"
    const val MY_HAVEN = "my_haven"

    const val EIRA = "eira"
    const val GENTLE_TASKS = "gentle_tasks"
    const val FLOW = "flow"
    const val REFLECTIONS = "reflections"
    const val HEALTH_HAVEN = "health_haven"
    const val PETROVA = "petrova"
    const val COMMITMENTS = "commitments"
}

data class BottomDestination(val route: String, val label: String, val icon: ImageVector)

val BottomDestinations = listOf(
    BottomDestination(NavRoutes.HOME, "Home", Icons.Filled.Home),
    BottomDestination(NavRoutes.PLANNER, "Planner", Icons.Filled.CalendarMonth),
    BottomDestination(NavRoutes.MONEY, "Money", Icons.Filled.AccountBalanceWallet),
    BottomDestination(NavRoutes.HABITS, "Habits", Icons.Filled.LocalFlorist),
    BottomDestination(NavRoutes.MY_HAVEN, "My Haven", Icons.Filled.Settings),
)

data class MoreDestination(val route: String, val label: String, val icon: ImageVector)

val MoreDestinations = listOf(
    MoreDestination(NavRoutes.EIRA, "Eira", Icons.Filled.Favorite),
    MoreDestination(NavRoutes.GENTLE_TASKS, "Gentle Tasks", Icons.Filled.Checklist),
    MoreDestination(NavRoutes.FLOW, "Flow", Icons.Filled.Water),
    MoreDestination(NavRoutes.REFLECTIONS, "Reflections", Icons.Filled.MenuBook),
    MoreDestination(NavRoutes.HEALTH_HAVEN, "Health Haven", Icons.Filled.Spa),
    MoreDestination(NavRoutes.PETROVA, "Petrova", Icons.Filled.Pets),
    MoreDestination(NavRoutes.COMMITMENTS, "Commitments", Icons.Filled.Repeat),
)
