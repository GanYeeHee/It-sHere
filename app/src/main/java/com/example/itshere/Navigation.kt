package com.example.itshere


import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onCloseDrawer: () -> Unit,
    onNavigateToUserList: () -> Unit = {},
    onNavigateToReports: () -> Unit = {}
    // Add other navigation functions here later
) {
    ModalDrawerSheet(
        modifier = Modifier
            .padding(bottom = 100.dp)
            .width(300.dp),// Set a width to match the image
        drawerContainerColor = Color(0xFFfacdcd)

    ) {
        Image(
            painter = painterResource(R.drawable.back_arrow),
            contentDescription = null,
            modifier = Modifier
                .size(70.dp)
                .padding(top = 40.dp )
                .clickable{onCloseDrawer()}
        )
        Row(
            modifier = Modifier.padding(top = 30.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.people),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .padding(start = 16.dp)
            )
            Column {
                Text("Admin1231",
                    modifier = Modifier.padding(start = 16.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Active now",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 2.dp),
                    color = Color.Green
                )
            }
        }
        NavigationDrawerItem(
            label = {
                Text(
                    "Dashboard",
                    fontWeight = FontWeight.Bold
                ) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.dashboard), // <-- R.drawable reference
                    contentDescription = "Dashboard Icon",
                    modifier = Modifier.size(24.dp) // Set a size, as drawables don't have a default size
                )
            },
            colors = NavigationDrawerItemDefaults.colors(
                Color(0xFF9c8181),
                unselectedContainerColor = Color.Transparent
            ),
            selected = false,
            onClick = onCloseDrawer
        )
        NavigationDrawerItem(
            label = {
                Text(
                    "User List",
                    fontWeight = FontWeight.Bold
                ) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.group), // <-- R.drawable reference
                    contentDescription = "Dashboard Icon",
                    modifier = Modifier.size(24.dp) // Set a size, as drawables don't have a default size
                )
            },
            colors = NavigationDrawerItemDefaults.colors(
                Color(0xFF9c8181),
                unselectedContainerColor = Color.Transparent
            ),
            selected = false,
            onClick = onNavigateToUserList
        )
        NavigationDrawerItem(
            label = {
                Text(
                    "Report",
                    fontWeight = FontWeight.Bold
                ) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.file), // <-- R.drawable reference
                    contentDescription = "Dashboard Icon",
                    modifier = Modifier.size(24.dp) // Set a size, as drawables don't have a default size
                )
            },
            colors = NavigationDrawerItemDefaults.colors(
                Color(0xFF9c8181),
                unselectedContainerColor = Color.Transparent
            ),
            selected = false,
            onClick = onNavigateToReports
        )

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        Text(
            "Log out",
            fontWeight = FontWeight.Bold,
            color = Color.Red,
            modifier = Modifier
                .padding(top = 16.dp, start = 15.dp)
                .clickable { onLogout() }
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

                onCloseDrawer = {
                    scope.launch {
                        drawerState.close()
                    }
                },
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

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewDrawer(){
    AdminDrawerContent(
        onLogout = {},
        onCloseDrawer = {}
    )
}
