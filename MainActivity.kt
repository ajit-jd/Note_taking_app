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
import androidx.compose.runtime.* // For remember, mutableStateOf, by
import androidx.compose.runtime.saveable.rememberSaveable // For saving theme state
import androidx.compose.ui.Modifier
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
            // 1. Manage theme state here
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }
            val onThemeToggleLambda = { newThemeState: Boolean -> isDarkTheme = newThemeState }

            Project7Theme(darkTheme = isDarkTheme) { // Apply the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 2. Pass the state and toggle function to NoteAppNavigation
                    NoteAppNavigation(
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = onThemeToggleLambda
                    )
                }
            }
        }
    }
}

@Composable
fun NoteAppNavigation(
    isDarkTheme: Boolean,             // <<< Now correctly received
    onThemeToggle: (Boolean) -> Unit  // <<< Now correctly received
) {
    val navController: NavHostController = rememberNavController()
    val noteViewModel: NoteViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(
            route = Screen.Main.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { it / 2 }) },
            exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { -it / 2 }) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { -it / 2 }) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { it / 2 }) }
        ) { navBackStackEntry ->
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
            }),
            enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { it / 2 }) },
            exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { -it / 2 }) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { -it / 2 }) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { it / 2 }) }
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

        composable(
            route = Screen.LabelBrowse.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { it / 2 }) },
            exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { -it / 2 }) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { -it / 2 }) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { it / 2 }) }
        ) { navBackStackEntry ->
            Log.d("LifecycleDebug", "MainActivity: Composing LabelBrowseScreen route. NBE State: ${navBackStackEntry.lifecycle.currentState}")
            CompositionLocalProvider(LocalLifecycleOwner provides navBackStackEntry) {
                LabelBrowseScreen(
                    navController = navController,
                    viewModel = noteViewModel
                )
            }
        }

        composable(
            route = Screen.ManageLabels.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { it / 2 }) },
            exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { -it / 2 }) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { -it / 2 }) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { it / 2 }) }
        ) { navBackStackEntry ->
            Log.d("LifecycleDebug", "MainActivity: Composing ManageLabelsScreen route. NBE State: ${navBackStackEntry.lifecycle.currentState}")
            CompositionLocalProvider(LocalLifecycleOwner provides navBackStackEntry) {
                ManageLabelsScreen(
                    navController = navController,
                    viewModel = noteViewModel
                )
            }
        }

        composable(
            route = Screen.Settings.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { it / 2 }) },
            exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { -it / 2 }) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { -it / 2 }) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { it / 2 }) }
        ) { navBackStackEntry ->
            Log.d("LifecycleDebug", "MainActivity: Composing SettingsScreen route. NBE State: ${navBackStackEntry.lifecycle.currentState}")
            CompositionLocalProvider(LocalLifecycleOwner provides navBackStackEntry) {
                SettingsScreen(
                    navController = navController,
                    isDarkTheme = isDarkTheme,       // <<< 4. Pass down to SettingsScreen
                    onThemeToggle = onThemeToggle    // <<< 4. Pass down to SettingsScreen
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