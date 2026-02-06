package com.app.reader.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.reader.home.HomeScreen
import com.app.reader.rsvp.RSVPScreen
import com.app.reader.rsvp.RunnerScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        
        composable("home") {
            HomeScreen(
                onStartReading = {
                    navController.navigate("reader")
                }
            )
        }

        composable("reader") {
            RSVPScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("runner") {
            RunnerScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}