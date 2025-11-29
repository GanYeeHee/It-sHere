package com.example.itshere

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate("signup")
                },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("signup") {
            SignUpScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onSignUpSuccess = {
                navController.navigate("home") {
                    popUpTo("signup") { inclusive = true }
                }
            }
        )
        }

        composable("home") {

        }
    }
}