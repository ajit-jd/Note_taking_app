
// E:\Kotlin2\Project7\app\src\main\java\com\example\project7\ui\screens\MainScreen.kt

package com.example.project7.ui.screens
// ... (imports and helper function) ...


import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import com.example.project7.ui.utils.collectAsStateWithLifecycleManual
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable // For dialog input state
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.example.project7.data.Notebook // For Notebook type
import com.example.project7.data.Note // For NoteList
import com.example.project7.ui.screens.AppDrawer
import com.example.project7.viewmodel.NoteViewModel
import com.example.project7.viewmodel.NotebookDrawerItem
import kotlinx.coroutines.CoroutineScope // For passing scope to dialogs
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.project7.Screen
import com.example.project7.data.Label // For Label type (if still needed by AppDrawer)
import com.example.project7.data.SubNotebook // For SubNotebook type
import com.example.project7.ui.utils.collectAsStateWithLifecycleManual


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    isDarkTheme: Boolean,             // <<< ADD THIS PARAMETER
    onThemeToggle: (Boolean) -> Unit  // <<< ADD THIS PARAMETER
) {
    val currentLCO = LocalLifecycleOwner.current
    Log.d("LifecycleDebug", "MainScreen Composable: Start. LocalLCO: $currentLCO, State: ${currentLCO.lifecycle.currentState}")

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed) // <<< DEFINED HERE
    val scope = rememberCoroutineScope() // <<< DEFINED HERE

    val currentViewTitle by viewModel.currentViewTitle.collectAsStateWithLifecycleManual(
        initialValueOverride = "Loading...",
    )
    val notesForDisplay by viewModel.notesForDisplay.collectAsStateWithLifecycleManual( // <<< DEFINED HERE
        initialValueOverride = emptyList(),
    )
    val notebookDrawerItems by viewModel.notebookDrawerItems.collectAsStateWithLifecycleManual(
        initialValueOverride = emptyList(),
    )

    // Collect allLabels for the AppDrawer
    val allLabels: List<Label> by viewModel.allLabelsCorrected.collectAsStateWithLifecycleManual(
        initialValueOverride = emptyList()
    )

    // --- Dialog States ---
    var showAddNewNotebookDialog by remember { mutableStateOf(false) }
    var notebookToRename by remember { mutableStateOf<Notebook?>(null) }
    var notebookToDelete by remember { mutableStateOf<Notebook?>(null) }
    // --- Add New Notebook Dialog ---
    AnimatedVisibility(
        visible = showAddNewNotebookDialog,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        AddNewNotebookDialog(
            onDismiss = { showAddNewNotebookDialog = false },
            onConfirm = { notebookName ->
                scope.launch { viewModel.addNotebook(notebookName) } // Use defined scope
                showAddNewNotebookDialog = false
            }
        )
    }

    // --- Rename Notebook Dialog ---
    AnimatedVisibility(
        visible = notebookToRename != null,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        notebookToRename?.let { currentNotebook ->
            RenameNotebookDialog(
                notebook = currentNotebook,
                onDismiss = { notebookToRename = null },
                onConfirm = { newName ->
                scope.launch { viewModel.updateNotebook(currentNotebook.copy(name = newName)) } // Use defined scope
                notebookToRename = null
                }
            )
        }
    }

    // --- Confirm Delete Notebook Dialog ---
    AnimatedVisibility(
        visible = notebookToDelete != null,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        notebookToDelete?.let { currentNotebook ->
            ConfirmDeleteDialog(
                itemName = currentNotebook.name,
                itemType = "notebook",
                onDismiss = { notebookToDelete = null },
                onConfirm = {
                scope.launch { viewModel.deleteNotebook(currentNotebook) } // Use defined scope
                notebookToDelete = null
                }
            )
        }
    }


    // In MainScreen.kt

// ... (viewModel, navController, scope, drawerState are defined) ...
// ... (notebookDrawerItems, isDarkTheme, onThemeToggle are defined and passed into MainScreen) ...

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                // Parameters for Notebooks
                drawerItems = notebookDrawerItems, // This is List<NotebookDrawerItem>
                onAllNotesClicked = {
                    viewModel.showAllNotes()
                    scope.launch { drawerState.close() }
                },
                onNotebookClicked = { notebook, shouldClose ->
                    viewModel.showNotebookView(notebook)
                    if (shouldClose) { scope.launch { drawerState.close() } }
                },
                onSubNotebookClicked = { subNotebook, parentNotebook ->
                    viewModel.showSubNotebookView(subNotebook, parentNotebook)
                    scope.launch { drawerState.close() }
                },
                onAddNewNotebookClicked = {
                    showAddNewNotebookDialog = true // Assuming showAddNewNotebookDialog state is in MainScreen
                },
                onRenameNotebookClicked = { notebook ->
                    notebookToRename = notebook // Assuming notebookToRename state is in MainScreen
                    scope.launch { drawerState.close() }
                },
                onDeleteNotebookClicked = { notebook ->
                    notebookToDelete = notebook // Assuming notebookToDelete state is in MainScreen
                    scope.launch { drawerState.close() }
                },

                // --- New Parameters ---
                getNotesForNotebookFlow = viewModel::getNotesByNotebookIdFlow, // Pass the function reference
                onNoteClickedInDrawer = { noteId ->
                    navController.navigate(Screen.NoteDetail.route + "/$noteId")
                    scope.launch { drawerState.close() }
                    // expandedNotebookNotesId in AppDrawer is already set to null by its own click handler
                },
                // --- End New Parameters ---

                // Parameters for the Settings section that ARE in AppDrawer
                isDarkTheme = isDarkTheme,         // Pass this from MainScreen's parameters
                onThemeToggle = onThemeToggle,     // Pass this from MainScreen's parameters
                onNavigateToSettingsScreen = {     // For the "More Settings" item in AppDrawer
                    navController.navigate(Screen.Settings.route)
                    scope.launch { drawerState.close() }
                }
                // NO MORE: allLabels, onLabelClicked, onManageLabelsClicked, onSettingsClicked (if it was separate)
                // modifier = Modifier // Optional
            )
        }
    )  {
        Scaffold(
            topBar = {
                Log.d("TopBarDebug", "TopAppBar IS BEING COMPOSED")
                TopAppBar(
                    title = { Text(currentViewTitle) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open Navigation Drawer")
                        }
                    },
                    actions = {
                        Log.d("TopBarDebug", "ACTIONS LAMBDA IS BEING CALLED")
                        IconButton(onClick = {
                            Log.d("TopBarDebug", "Label IconButton Clicked!")
                            navController.navigate(Screen.LabelBrowse.route)
                        }) {
                            Log.d("TopBarDebug", "Icon FOR LABEL IS BEING COMPOSED")
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Label,
                                contentDescription = "Browse Labels"
                                // tint = Color.Red // Keep for testing if icon still not visible
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer // Changed back from .error for now
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    navController.navigate(Screen.NoteDetail.route + "/-1")
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Note")
                }
            }
        ) { paddingValues ->
            if (notesForDisplay.isEmpty()) {
                EmptyState(Modifier.padding(paddingValues).toString())
            } else {
                NoteList(
                    notes = notesForDisplay,
                    onNoteClick = { noteId ->
                        navController.navigate(Screen.NoteDetail.route + "/$noteId")
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
    Log.d("LifecycleDebug", "MainScreen Composable: End")
}

// --- Dialog Composable Functions (These are fine as they take scope/viewModel via their lambdas) ---
@Composable
fun AddNewNotebookDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit // Lambda will be called with scope from MainScreen
) {
    var notebookName by rememberSaveable { mutableStateOf("") }
    AlertDialog( /* ... same as before ... */
        onDismissRequest = onDismiss,
        title = { Text("Add New Notebook") },
        text = {
            OutlinedTextField(
                value = notebookName,
                onValueChange = { notebookName = it },
                label = { Text("Notebook Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (notebookName.isNotBlank()) onConfirm(notebookName) },
                enabled = notebookName.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun RenameNotebookDialog(
    notebook: Notebook,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit // Lambda will be called with scope from MainScreen
) {
    var newName by rememberSaveable(notebook.name) { mutableStateOf(notebook.name) }
    AlertDialog( /* ... same as before ... */
        onDismissRequest = onDismiss,
        title = { Text("Rename Notebook") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("New Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newName.isNotBlank() && newName != notebook.name) onConfirm(newName)
                    else if (newName == notebook.name) onDismiss()
                },
                enabled = newName.isNotBlank()
            ) { Text("Rename") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ConfirmDeleteDialog(
    itemName: String,
    itemType: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit // Lambda will be called with scope from MainScreen
) {
    AlertDialog( /* ... same as before ... */
        onDismissRequest = onDismiss,
        title = { Text("Delete $itemType?") },
        text = { Text("Are you sure you want to delete \"$itemName\"? This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Text("Delete")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
