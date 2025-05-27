// E:\Kotlin2\Project7\app\src\main\java\com\example\project7\MainActivity.kt
package com.example.project7

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable // For saving theme state - will be removed for isDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.foundation.isSystemInDarkTheme
// import androidx.lifecycle.compose.collectAsStateWithLifecycle // Alternative
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.project7.data.ThemeDataStoreRepository
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.project7.ui.screens.LabelBrowseScreen
import com.example.project7.ui.screens.MainScreen
import com.example.project7.ui.screens.ManageLabelsScreen
import com.example.project7.ui.screens.NoteDetailScreen
import com.example.project7.ui.screens.SettingsScreen // Ensure this is imported
import com.example.project7.ui.theme.Project7Theme
import com.example.project7.viewmodel.NoteViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // Call before super.onCreate for API 31+ consistency
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
         Thread.sleep(300) // Generally avoid Thread.sleep on main thread for splash

        setContent {
            val themeRepository = ThemeDataStoreRepository(applicationContext)
            val currentThemeSettingString by themeRepository.themeSettingFlow.collectAsState(initial = "SYSTEM")

            var isDarkTheme by remember { mutableStateOf(false) } // Initial value updated by LaunchedEffect

            // Update isDarkTheme based on DataStore preference or system setting
            val systemIsDark = isSystemInDarkTheme() // Call isSystemInDarkTheme() at the top level of Composable
            LaunchedEffect(currentThemeSettingString, systemIsDark) { // Re-run if preference or system theme changes
                Log.d("ThemeApply", "Theme preference changed to: $currentThemeSettingString, System is dark: $systemIsDark")
                isDarkTheme = when (currentThemeSettingString) {
                    "DARK" -> true
                    "LIGHT" -> false
                    "SYSTEM" -> systemIsDark
                    else -> systemIsDark // Default to system for any unknown value
                }
                Log.d("ThemeApply", "isDarkTheme dynamically set to: $isDarkTheme")
            }
            
            // onThemeToggle is now a no-op as SettingsScreen saves to DataStore,
            // and MainActivity reacts to DataStore changes.
            val onThemeToggleLambda = { /* No-op */ }

            Project7Theme(darkTheme = isDarkTheme) { // Apply the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NoteAppNavigation(
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = onThemeToggleLambda, // Pass the potentially no-op lambda
                        themeRepository = themeRepository
                    )
                }
            }
        }
    }
}

@Composable
fun NoteAppNavigation(
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    themeRepository: ThemeDataStoreRepository
) {
    val navController: NavHostController = rememberNavController()
    val noteViewModel: NoteViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(route = Screen.Main.route) { navBackStackEntry ->
            Log.d("LifecycleDebug", "MainActivity: Composing MainScreen route. NBE State: ${navBackStackEntry.lifecycle.currentState}")
            CompositionLocalProvider(LocalLifecycleOwner provides navBackStackEntry) {
                MainScreen(
                    navController = navController,
                    viewModel = noteViewModel,
                    isDarkTheme = isDarkTheme,       // <<< 3. Pass down to MainScreen
                    onThemeToggle = onThemeToggle    // <<< 3. Pass down to MainScreen
                )
            }
        }

        composable(
            route = Screen.NoteDetail.route + "/{noteId}",
            arguments = listOf(navArgument("noteId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { navBackStackEntry ->
            Log.d("LifecycleDebug", "MainActivity: Composing NoteDetailScreen route. NBE State: ${navBackStackEntry.lifecycle.currentState}")
            val noteId = navBackStackEntry.arguments?.getInt("noteId") ?: -1
            CompositionLocalProvider(LocalLifecycleOwner provides navBackStackEntry) {
                NoteDetailScreen(
                    navController = navController,
                    viewModel = noteViewModel,
                    noteId = noteId
                )
            }
        }

        composable(route = Screen.LabelBrowse.route) { navBackStackEntry ->
            Log.d("LifecycleDebug", "MainActivity: Composing LabelBrowseScreen route. NBE State: ${navBackStackEntry.lifecycle.currentState}")
            CompositionLocalProvider(LocalLifecycleOwner provides navBackStackEntry) {
                LabelBrowseScreen(
                    navController = navController,
                    viewModel = noteViewModel
                )
            }
        }

        composable(route = Screen.ManageLabels.route) { navBackStackEntry ->
            Log.d("LifecycleDebug", "MainActivity: Composing ManageLabelsScreen route. NBE State: ${navBackStackEntry.lifecycle.currentState}")
            CompositionLocalProvider(LocalLifecycleOwner provides navBackStackEntry) {
                ManageLabelsScreen(
                    navController = navController,
                    viewModel = noteViewModel
                )
            }
        }

        composable(route = Screen.Settings.route) { navBackStackEntry ->
            Log.d("LifecycleDebug", "MainActivity: Composing SettingsScreen route. NBE State: ${navBackStackEntry.lifecycle.currentState}")
            CompositionLocalProvider(LocalLifecycleOwner provides navBackStackEntry) {
                SettingsScreen(
                    navController = navController,
                    isDarkTheme = isDarkTheme,       // <<< 4. Pass down to SettingsScreen
                    onThemeToggle = onThemeToggle,    // <<< 4. Pass down to SettingsScreen
                    themeRepository = themeRepository
                )
            }
        }
    }
}

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object NoteDetail : Screen("noteDetail")
    data object LabelBrowse : Screen("labelBrowse")
    data object ManageLabels : Screen("manageLabels")
    data object Settings : Screen("settings")
}