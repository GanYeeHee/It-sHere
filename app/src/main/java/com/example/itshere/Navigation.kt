package com.example.itshere

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.itshere.Data.PostType
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()

    val isUserLoggedIn by remember {
        mutableStateOf(auth.currentUser != null)
    }

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
            HomePage(
                navController = navController,
                onCreatePostClick = {
                    navController.navigate("create_post/found")
                },
                onPostClick = { postId ->
                    navController.navigate("post_details/$postId")
                },
                onLogoutSuccess = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "create_post/{postType}",
            arguments = listOf(
                navArgument("postType") {
                    type = NavType.StringType
                    defaultValue = "found"
                }
            )
        ) { backStackEntry ->
            val postTypeString = backStackEntry.arguments?.getString("postType") ?: "found"
            val postType = if (postTypeString == "lost") PostType.LOST else PostType.FOUND

            CreatePostPage(
                postType = postType,
                onBackClick = {
                    navController.popBackStack()
                },
                onPostSuccess = {
                    navController.navigate("home") {
                        popUpTo("home") {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = "post_details/{postId}",
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""

            PostDetailsScreen(
                postId = postId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("about_us") {
            AboutUsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("notification"){
            NotificationsScreen()
        }
    }
}