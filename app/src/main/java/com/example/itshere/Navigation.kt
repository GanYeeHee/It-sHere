package com.example.itshere

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.itshere.Data.PostType

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
            HomePage(
                onCreatePostClick = {
                    navController.navigate("create_post/found")
                },
                onPostClick = { postId ->
                    // TODO: 導航到帖子詳情頁
                    // navController.navigate("post_detail/$postId")
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
                    // 創建成功後返回首頁
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}