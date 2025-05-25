// E:\Kotlin2\Project7\app\src\main\java\com\example\project7\ui\screens\LabelBrowseScreen.kt
package com.example.project7.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue // For 'by' delegation
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner // Still needed if helper uses it implicitly
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.project7.data.Label
import com.example.project7.viewmodel.NoteViewModel
import com.example.project7.Screen
import com.example.project7.ui.utils.collectAsStateWithLifecycleManual // <<< IMPORT HELPER

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelBrowseScreen(
    navController: NavController,
    viewModel: NoteViewModel
) {
    val currentLCO = LocalLifecycleOwner.current // Just for logging
    Log.d("LifecycleDebug", "LabelBrowseScreen Composable: Start. LocalLCO: $currentLCO, State: ${currentLCO.lifecycle.currentState}")

    // Use the helper function (imported from utils or another file)
    val allLabels by viewModel.allLabelsCorrected.collectAsStateWithLifecycleManual(
        initialValueOverride = emptyList()
        // lifecycleState = Lifecycle.State.CREATED // Default in helper is CREATED
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browse Labels") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        navController.navigate(Screen.ManageLabels.route)
                    }) {
                        Text("Manage")
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Filled.Edit, contentDescription = "Manage Labels")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (allLabels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No labels created yet. Tap 'Manage' to add some!",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allLabels, key = { label -> label.id }) { label -> // Corrected key
                    LabelItem(
                        label = label,
                        onClick = {
                            viewModel.showLabelView(label)
                            navController.popBackStack(Screen.Main.route, inclusive = false)
                        }
                    )
                }
            }
        }
    }
    Log.d("LifecycleDebug", "LabelBrowseScreen Composable: End")
}

@Composable
fun LabelItem(label: Label, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = label.name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}