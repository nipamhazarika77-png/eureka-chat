package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AiChatScreen
import com.example.ui.screens.ArchivedChatsScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.theme.EurekaTheme
import com.example.viewmodels.AuthViewModel
import com.example.viewmodels.AuthState
import com.example.viewmodels.MainViewModel
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Archive

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Chats : Screen("chats", "Chats", Icons.AutoMirrored.Filled.Chat)
    object Calls : Screen("calls", "Calls", Icons.Filled.Call)
    object Contacts : Screen("contacts", "Contacts", Icons.Filled.Contacts)
    object AI : Screen("ai", "Eureka AI", Icons.Filled.SmartToy)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    object ArchivedChats : Screen("archived_chats", "Archived", Icons.Filled.Archive)
    object ChatDetail : Screen("chat_detail/{chatId}", "Chat Detail", Icons.AutoMirrored.Filled.Chat) {
        fun createRoute(chatId: String) = "chat_detail/$chatId"
    }
}

val bottomNavItems = listOf(
    Screen.Chats,
    Screen.Calls,
    Screen.Contacts,
    Screen.AI,
    Screen.Settings
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EurekaTheme {
                val authViewModel: AuthViewModel = viewModel()
                val authState by authViewModel.authState.collectAsState()

                if (authState is AuthState.Authenticated) {
                    MainScreen()
                } else {
                    SignInScreen(
                        authViewModel = authViewModel,
                        onNavigateToMain = {} // The LaunchedEffect in SignInScreen isn't strictly needed if we just conditionally render, but we can leave it empty or trigger something if we need
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White
                ) {
                    val currentDestination = navBackStackEntry?.destination
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                indicatorColor = com.example.ui.theme.NavSelectedBg,
                                selectedIconColor = com.example.ui.theme.DeepBlue,
                                selectedTextColor = com.example.ui.theme.DeepBlue,
                                unselectedIconColor = com.example.ui.theme.TextLightGray,
                                unselectedTextColor = com.example.ui.theme.TextLightGray
                            ),
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
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
            startDestination = Screen.Chats.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Chats.route) { 
                ChatListScreen(
                    viewModel = viewModel, 
                    onChatClick = { chatId ->
                        navController.navigate(Screen.ChatDetail.createRoute(chatId))
                    },
                    onNavigateToArchived = {
                        navController.navigate(Screen.ArchivedChats.route)
                    }
                ) 
            }
            composable(Screen.ArchivedChats.route) {
                ArchivedChatsScreen(
                    viewModel = viewModel,
                    onChatClick = { chatId ->
                        navController.navigate(Screen.ChatDetail.createRoute(chatId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Calls.route) { PlaceholderScreen("Calls (Coming Soon)") }
            composable(Screen.Contacts.route) { PlaceholderScreen("Contacts (Coming Soon)") }
            composable(Screen.AI.route) { AiChatScreen(viewModel = viewModel) }
            composable(Screen.Settings.route) { 
                SettingsScreen(
                    viewModel = viewModel, 
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                ) 
            }
            composable(Screen.Profile.route) {
                ProfileScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.ChatDetail.route) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
                ChatScreen(chatId = chatId, onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, fontWeight = FontWeight.Bold)
    }
}
