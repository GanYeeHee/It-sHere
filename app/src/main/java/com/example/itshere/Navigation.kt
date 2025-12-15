package com.example.itshere

import android.R.attr.divider
import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.itshere.Data.PostType
import com.example.itshere.viewModel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()

    val context = LocalContext.current
    val application = context.applicationContext as Application // Safely get Application instance


    val loginViewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                // 2. Instantiate LoginViewModel with the Application instance
                return LoginViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    val loginViewModel: LoginViewModel = viewModel(factory = loginViewModelFactory)


    val isUserLoggedIn by remember {
        mutableStateOf(auth.currentUser != null)
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToSignUp = {
                    loginViewModel.clearForm()
                    navController.navigate("signup")
                },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },

                onNavigateToAdmin = {
                    navController.navigate("admin_home") {
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
                },
                onGoToLoginWithPrefill = { email, password ->
                    loginViewModel.prefillCredentials(email, password)
                    navController.popBackStack("login", false)
                }
            )
        }

        composable("admin_home") {
            AdminHomeScreenWrapper(
                onLogout = {
                    // This is the navigation action passed down to the Wrapper,
                    // and then passed to the Logout button inside the drawer.
                    auth.signOut() // Log out the user from Firebase (good practice)
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
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

// Placeholder for the sliding sidebar content (Replace with your actual drawer Composable later)
@Composable
fun AdminDrawerContent(
    onLogout: () -> Unit,
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToUserList: () -> Unit = {},
    onNavigateToReports: () -> Unit = {}
    // Add other navigation functions here later
) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp) // Set a width to match the image
    ) {
        Text("Admin Sidebar Menu", modifier = Modifier.padding(16.dp))
        Text("Admin1231", modifier = Modifier.padding(horizontal = 16.dp))
        Text("Active now •", modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp), color = Color.Green)
        Divider()

        NavigationDrawerItem(
            label = { Text("Dashboard") },
            selected = false,
            onClick = onNavigateToDashboard
        )
        NavigationDrawerItem(
            label = { Text("User list") },
            selected = false,
            onClick = onNavigateToUserList
        )
        NavigationDrawerItem(
            label = { Text("Reports") },
            selected = false,
            onClick = onNavigateToReports
        )

        Divider()
        NavigationDrawerItem(
            label = { Text("Logout") },
            selected = false,
            onClick = onLogout
        )
        // Add Dashboard, User list, Reports items here
    }
}

@Composable
fun AdminHomeScreenWrapper(
    onLogout: () -> Unit // Logout function passed from NavHost
) {
    // State and Scope to manage the opening and closing of the drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AdminDrawerContent(
                onLogout = {
                    scope.launch { drawerState.close() } // Close drawer on click
                    onLogout() // Execute logout action
                }
            )
        },
        content = {
            // AdminHome needs the function to trigger the drawer open
            AdminHome(
                onLogout = onLogout,
                onOpenDrawer = {
                    scope.launch {
                        drawerState.open()
                    }
                }
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewDrawer(){
    AdminDrawerContent(onLogout = {}, onNavigateToReports = {})
}
