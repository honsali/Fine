package app.fine.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.fine.R
import app.fine.data.ExpenseRepository
import app.fine.ui.add.AddRoute
import app.fine.ui.add.AddScreenEvent
import app.fine.ui.history.HistoryRoute
import app.fine.ui.manage.ManageRoute
import app.fine.ui.theme.FineTheme
import kotlinx.coroutines.launch

@Composable
fun FineApp(
    repository: ExpenseRepository
) {
    FineTheme {
        val navController = rememberNavController()
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val factory = remember(repository) { FineViewModelFactory(repository) }

        val destinations = remember {
            listOf(
                BottomDestination(
                    route = Routes.Add,
                    labelRes = R.string.tab_add,
                    icon = Icons.Filled.Add
                ),
                BottomDestination(
                    route = Routes.History,
                    labelRes = R.string.tab_history,
                    icon = Icons.Filled.History
                ),
                BottomDestination(
                    route = Routes.Manage,
                    labelRes = R.string.tab_manage,
                    icon = Icons.Filled.Settings
                )
            )
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                val currentDestination by navController.currentBackStackEntryAsState()
                val destination = currentDestination?.destination
                NavigationBar {
                    destinations.forEach { item ->
                        val selected = destination.isRouteSelected(item.route)
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
                            label = { Text(text = stringResource(id = item.labelRes)) },
                            icon = { Icon(imageVector = item.icon, contentDescription = null) }
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Routes.Add,
                modifier = Modifier.padding(padding)
            ) {
                composable(Routes.Add) { backStackEntry ->
                    val addViewModel: app.fine.ui.add.AddExpenseViewModel =
                        viewModel(backStackEntry, factory = factory)
                    AddRoute(
                        viewModel = addViewModel,
                        onEvent = { event ->
                            when (event) {
                                is AddScreenEvent.ShowMessage -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(event.message)
                                    }
                                }
                            }
                        }
                    )
                }
                composable(Routes.History) { backStackEntry ->
                    val historyViewModel: app.fine.ui.history.HistoryViewModel =
                        viewModel(backStackEntry, factory = factory)
                    HistoryRoute(viewModel = historyViewModel)
                }
                composable(Routes.Manage) { backStackEntry ->
                    val manageViewModel: app.fine.ui.manage.ManageViewModel =
                        viewModel(backStackEntry, factory = factory)
                    ManageRoute(
                        viewModel = manageViewModel,
                        onMessage = { message ->
                            scope.launch {
                                snackbarHostState.showSnackbar(message)
                            }
                        }
                    )
                }
            }
        }
    }
}

private data class BottomDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private object Routes {
    const val Add: String = "route_add"
    const val History: String = "route_history"
    const val Manage: String = "route_manage"
}

private fun NavDestination?.isRouteSelected(route: String): Boolean =
    this?.hierarchy?.any { it.route == route } == true
