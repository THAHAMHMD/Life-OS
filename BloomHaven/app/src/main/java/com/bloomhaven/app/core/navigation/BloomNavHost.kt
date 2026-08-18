package com.bloomhaven.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bloomhaven.app.feature.commitments.CommitmentsScreen
import com.bloomhaven.app.feature.eira.EiraScreen
import com.bloomhaven.app.feature.flow.FlowScreen
import com.bloomhaven.app.feature.gentletasks.GentleTasksScreen
import com.bloomhaven.app.feature.habits.HabitsScreen
import com.bloomhaven.app.feature.healthhaven.HealthHavenScreen
import com.bloomhaven.app.feature.home.HomeScreen
import com.bloomhaven.app.feature.moneyhaven.MoneyHavenScreen
import com.bloomhaven.app.feature.myhaven.MyHavenScreen
import com.bloomhaven.app.feature.petrova.PetrovaScreen
import com.bloomhaven.app.feature.planner.PlannerScreen
import com.bloomhaven.app.feature.reflections.ReflectionsScreen

@Composable
fun BloomNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = NavRoutes.HOME) {
        composable(NavRoutes.HOME) {
            HomeScreen(onOpenModule = { route -> navController.navigate(route) })
        }
        composable(NavRoutes.PLANNER) { PlannerScreen() }
        composable(NavRoutes.MONEY) { MoneyHavenScreen() }
        composable(NavRoutes.HABITS) { HabitsScreen() }
        composable(NavRoutes.MY_HAVEN) { MyHavenScreen() }

        composable(NavRoutes.EIRA) { EiraScreen() }
        composable(NavRoutes.GENTLE_TASKS) { GentleTasksScreen() }
        composable(NavRoutes.FLOW) { FlowScreen() }
        composable(NavRoutes.REFLECTIONS) { ReflectionsScreen() }
        composable(NavRoutes.HEALTH_HAVEN) { HealthHavenScreen() }
        composable(NavRoutes.PETROVA) { PetrovaScreen() }
        composable(NavRoutes.COMMITMENTS) { CommitmentsScreen() }
    }
}
