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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
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
import androidx.compose.material.icons.filled.PrivacyTip

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Chats : Screen("chats", "Chats", Icons.AutoMirrored.Filled.Chat)
    object Calls : Screen("calls", "Calls", Icons.Filled.Call)
    object Contacts : Screen("contacts", "Contacts", Icons.Filled.Contacts)
    object AI : Screen("ai", "Eureka AI", Icons.Filled.SmartToy)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    object Privacy : Screen("privacy", "Privacy", Icons.Filled.PrivacyTip)
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

class MainActivity : androidx.fragment.app.FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EurekaTheme {
                val authViewModel: AuthViewModel = viewModel()
                val authState by authViewModel.authState.collectAsState()

                val context = androidx.compose.ui.platform.LocalContext.current
                val fingerprintLockOn = remember { SecurityPreferences.isFingerprintLockEnabled(context) }
                var isAuthenticated by remember { mutableStateOf(!fingerprintLockOn) }

                androidx.compose.runtime.LaunchedEffect(authState) {
                    if (authState is AuthState.Authenticated && SecurityPreferences.isFingerprintLockEnabled(context) && !isAuthenticated) {
                        BiometricHelper.authenticate(
                            activity = this@MainActivity,
                            onSuccess = { isAuthenticated = true },
                            onError = { _ -> },
                            onFailed = {}
                        )
                    }
                }

                if (authState is AuthState.Authenticated) {
                    val currentLockState = SecurityPreferences.isFingerprintLockEnabled(context)
                    if (!currentLockState || isAuthenticated) {
                        MainScreen()
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(androidx.compose.material3.MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Locked",
                                    modifier = Modifier.size(64.dp),
                                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "EUREKA is Locked",
                                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Please authenticate with your fingerprint to continue.",
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
                                androidx.compose.material3.Button(
                                    onClick = {
                                        BiometricHelper.authenticate(
                                            activity = this@MainActivity,
                                            onSuccess = { isAuthenticated = true },
                                            onError = { _ -> },
                                            onFailed = {}
                                        )
                                    }
                                ) {
                                    Text("Unlock with Fingerprint")
                                }
                            }
                        }
                    }
                } else {
                    SignInScreen(
                        authViewModel = authViewModel,
                        onNavigateToMain = {}
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
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateToPrivacy = { navController.navigate(Screen.Privacy.route) }
                ) 
            }
            composable(Screen.Profile.route) {
                com.example.ui.screens.ProfileScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Privacy.route) {
                com.example.ui.screens.PrivacyScreen(onBack = { navController.popBackStack() })
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
