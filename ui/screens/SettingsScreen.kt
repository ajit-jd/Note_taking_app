// E:\Kotlin2\Project7\app\src\main\java\com\example\project7\ui\screens\SettingsScreen.kt
package com.example.project7.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.project7.data.ThemeDataStoreRepository
import kotlinx.coroutines.launch

// Enum for Theme options
enum class ThemeSetting {
    SYSTEM, LIGHT, DARK
}

// TODO: This state should ideally be hoisted to an AppViewModel or saved in DataStore
// For now, it's a local remember state for demonstration.
// You'll need to pass down a way to change the actual app theme from your MainActivity/App composable.
var currentThemeSetting = mutableStateOf(ThemeSetting.SYSTEM) // Default

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    isDarkTheme: Boolean, // <<< Must match
    onThemeToggle: (Boolean) -> Unit, // <<< Must match
    themeRepository: ThemeDataStoreRepository
    // TODO: Pass a lambda to actually change the app's theme:
    // onThemeChange: (ThemeSetting) -> Unit
) {
    // Local state for this screen's UI, reflecting the global setting
    var selectedTheme by remember { currentThemeSetting }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Theme", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // Radio buttons for theme selection
            Column(Modifier.selectableGroup()) {
                ThemeSetting.entries.forEach { theme ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (theme == selectedTheme),
                                onClick = {
                                    selectedTheme = theme
                                    currentThemeSetting.value = theme // Update the global-like state

                                scope.launch {
                                    themeRepository.saveThemeSetting(theme.name)
                                    Log.d("SettingsScreen", "Theme saved: ${theme.name}")
                                }
                                    // TODO: Call onThemeChange(theme) to actually change the app theme
                                    // This would trigger a recomposition of your top-level Project6Theme
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == selectedTheme),
                            onClick = null // null recommended for RadioButton when Row is selectable
                        )
                        Text(
                            text = theme.name.lowercase().replaceFirstChar { it.titlecase() },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
            // Add more settings here later
        }
    }
}