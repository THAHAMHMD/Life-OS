package com.bloomhaven.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bloomhaven.app.core.data.AppSettings
import com.bloomhaven.app.core.data.AppSettingsRepository
import com.bloomhaven.app.core.navigation.BloomNavHost
import com.bloomhaven.app.core.navigation.BottomDestinations
import com.bloomhaven.app.core.ui.BiometricLockGate
import com.bloomhaven.app.core.ui.BloomHavenTheme
import com.bloomhaven.app.core.ui.resolveNightHaven

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsRepo = remember { AppSettingsRepository(applicationContext) }
            val settings by settingsRepo.settings.collectAsState(initial = AppSettings())
            val nightHaven = resolveNightHaven(settings.nightHavenMode)

            BloomHavenTheme(nightHaven = nightHaven) {
                BiometricLockGate(enabled = settings.biometricLockEnabled) {
                    BloomHavenApp()
                }
            }
        }
    }
}

@Composable
private fun BloomHavenApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomDestinations.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                    )
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            BloomNavHost(navController = navController)
        }
    }
}
