package com.shiftmate.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.shiftmate.ui.dashboard.DashboardScreen
import com.shiftmate.ui.help.HelpScreen
import com.shiftmate.ui.profile.ProfileScreen
import com.shiftmate.ui.request.RequestScreen
import com.shiftmate.ui.rules.RulesScreen
import com.shiftmate.ui.shift.ShiftScreen
import com.shiftmate.ui.staff.StaffScreen
import com.shiftmate.ui.title.TitleScreen

// ── Outer nav (Title → App / Guide) ───────────────────────────────
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "title") {
        composable("title") {
            TitleScreen(
                onGuide = { navController.navigate("guide") },
                onStart = {
                    navController.navigate("app") {
                        popUpTo("title") { inclusive = false }
                    }
                }
            )
        }
        composable("guide") {
            HelpScreen(onBack = { navController.popBackStack() })
        }
        composable("app") {
            ShiftMateNavHost()
        }
    }
}

// ── Inner nav (Bottom tabs) ────────────────────────────────────────
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Staff     : Screen("staff",     "スタッフ", Icons.Filled.People)
    object Rules     : Screen("rules",     "ルール",   Icons.Filled.Settings)
    object Request   : Screen("request",   "希望休",   Icons.Filled.CalendarToday)
    object Shift     : Screen("shift",     "シフト",   Icons.Filled.TableChart)
    object Dashboard : Screen("dashboard", "集計",     Icons.Filled.Analytics)
}

private val bottomNavItems = listOf(
    Screen.Staff, Screen.Rules, Screen.Request, Screen.Shift, Screen.Dashboard
)

@Composable
fun ShiftMateNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute !in listOf("help", "profile")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val currentDest = navBackStackEntry?.destination
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDest?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Staff.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Staff.route) {
                StaffScreen(
                    onNavigateToHelp = { navController.navigate("help") },
                    onNavigateToProfile = { navController.navigate("profile") }
                )
            }
            composable(Screen.Rules.route)     { RulesScreen() }
            composable(Screen.Request.route)   { RequestScreen() }
            composable(Screen.Shift.route)     { ShiftScreen() }
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable("help")    { HelpScreen(onBack = { navController.popBackStack() }) }
            composable("profile") { ProfileScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
