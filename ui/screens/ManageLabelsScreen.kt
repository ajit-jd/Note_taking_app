// E:\Kotlin2\Project7\app\src\main\java\com\example\project7\ui\screens\ManageLabelsScreen.kt
package com.example.project7.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete // For delete icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner // Still needed if helper is top-level in this file
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle // Still needed if helper is top-level in this file
import androidx.navigation.NavController
import com.example.project7.data.Label
import com.example.project7.viewmodel.NoteViewModel
import com.example.project7.ui.utils.collectAsStateWithLifecycleManual // <<< IMPORT THE HELPER
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageLabelsScreen(
    navController: NavController,
    viewModel: NoteViewModel
) {
    val currentLCO = LocalLifecycleOwner.current // For logging, if needed
    Log.d("LifecycleDebug", "ManageLabelsScreen Composable: Start. LocalLCO: $currentLCO, State: ${currentLCO.lifecycle.currentState}")

    // Use the imported helper function
    val allLabels by viewModel.allLabelsCorrected.collectAsStateWithLifecycleManual(
        initialValueOverride = emptyList()
    )
    var newLabelName by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Labels") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Input field for new label
            OutlinedTextField(
                value = newLabelName,
                onValueChange = { newLabelName = it },
                label = { Text("New label name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (newLabelName.isNotBlank()) {
                        scope.launch {
                            viewModel.addLabel(newLabelName)
                            newLabelName = "" // Clear input field
                            focusManager.clearFocus() // Dismiss keyboard
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.AddCircle,
                    contentDescription = "Add Label Icon",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Add Label")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Existing Labels", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) // Use HorizontalDivider

            if (allLabels.isEmpty()) {
                Text("No labels yet. Add one above!", modifier = Modifier.padding(top = 16.dp))
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(allLabels, key = { label -> label.id }) { label -> // Corrected key
                        ExistingLabelItem(
                            label = label,
                            onDeleteClicked = {
                                // Consider adding a confirmation dialog here
                                viewModel.deleteLabel(label)
                            }
                            // Edit functionality can be added later
                        )
                        HorizontalDivider() // Use HorizontalDivider
                    }
                }
            }
        }
    }
    Log.d("LifecycleDebug", "ManageLabelsScreen Composable: End")
}

@Composable
fun ExistingLabelItem(
    label: Label,
    onDeleteClicked: () -> Unit
    // onEditClicked: () -> Unit // For future
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp), // Increased padding for better touch target
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        IconButton(onClick = onDeleteClicked) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete Label ${label.name}")
        }
        // IconButton(onClick = onEditClicked) { // For future edit functionality
        //     Icon(Icons.Filled.Edit, contentDescription = "Edit Label ${label.name}")
        // }
    }
}