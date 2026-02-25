package com.example.pet.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pet.presentation.home.HomeScreen
import com.example.pet.presentation.taskdetail.TaskDetailScreen

/**
 * Граф навигации приложения.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onTaskClick = { task ->
                    navController.navigate(Screen.TaskDetail.createRoute(task.id))
                }
            )
        }
        
        composable(
            route = Screen.TaskDetail.route,
            arguments = Screen.TaskDetail.arguments
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TaskDetailScreen(
                taskId = taskId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Определение экранов приложения.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    
    data object TaskDetail : Screen("task_detail/{taskId}") {
        val arguments = listOf(
            navArgument("taskId") {
                type = NavType.StringType
            }
        )
        
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
}

