package com.example.clock.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.clock.Screen

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun BottomNavigationBar(
    items: List<BottomNavItem>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    NavigationBar(modifier = modifier) {
        items.forEach { item ->
            val selected = item.route == backStackEntry.value?.destination?.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    },
                label = {
                   Text(text = item.name)
                  },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.name
                    )
                },
            )
        }
    }
}

val bottomBarItems =  listOf(
    BottomNavItem(
        name = "Alarm",
        route = Screen.AlarmsList.route,
        icon = Icons.Default.Alarm
    ),
    BottomNavItem(
        name = "Current Time",
        route = Screen.WorldClock.route,
        icon = Icons.Default.Language
    ),
    BottomNavItem(
        name = "Stopwatch",
        route = Screen.Stopwatch.route,
        icon = Icons.Default.Timer
    ),
    BottomNavItem(
        name = "Timer",
        route = Screen.Timer.route,
        icon = Icons.Default.HourglassEmpty
    ),
)