// E:\Kotlin2\Project7\app\src\main\java\com\example\project7\ui\components\AppDrawer.kt
package com.example.project7.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.* // Add, ArrowDropDown, Description, DriveFileRenameOutline, Delete, Folder, MoreVert, Settings, Brightness4, Brightness7
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.project7.data.Notebook
import com.example.project7.data.SubNotebook
import com.example.project7.viewmodel.NotebookDrawerItem
// No import for com.example.project6.Screen needed here if Settings navigation is just a lambda

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(drawerItems: List<NotebookDrawerItem>,

              onAllNotesClicked: () -> Unit,
              onNotebookClicked: (notebook: Notebook, shouldCloseDrawer: Boolean) -> Unit,
              onSubNotebookClicked: (subNotebook: SubNotebook, parentNotebook: Notebook) -> Unit,
              onAddNewNotebookClicked: () -> Unit,
              onRenameNotebookClicked: (Notebook) -> Unit,
              onDeleteNotebookClicked: (Notebook) -> Unit,
              isDarkTheme: Boolean,               // For the direct theme toggle in drawer
              onThemeToggle: (Boolean) -> Unit,   // For the direct theme toggle in drawer
              onNavigateToSettingsScreen: () -> Unit, // For the "More Settings" item
              modifier: Modifier = Modifier
) {
    val expandedNotebooks = remember { mutableStateMapOf<Int, Boolean>() }
    var showNotebookMenuFor by remember { mutableStateOf<Notebook?>(null) }

    ModalDrawerSheet(
        modifier = modifier.fillMaxHeight() // Drawer typically takes full height
    ) {
        // This outer Column allows the entire drawer content to scroll if it overflows.
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp) // Padding at the very bottom of the scrollable content
        ) {
            // App Title / Header
            Text(
                "Notes App", // Replace with your app's actual name or logo
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp,end = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

            // All Notes Item
            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.Description, contentDescription = "All Notes") },
                label = { Text("All Notes") },
                selected = false, // TODO: Implement selection state based on current view model context
                onClick = onAllNotesClicked,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // --- Notebooks Section ---
            Text(
                "Notebooks",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Notebook items - part of the overall scrollable Column
            if (drawerItems.isEmpty()) {
                Text(
                    "No notebooks yet.",
                    modifier = Modifier.padding(start = 16.dp,end=16.dp, top = 4.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                drawerItems.forEach { item ->
                    Column { // Groups a notebook and its potential subnotebooks
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    var intendToCloseDrawer = true
                                    if (item.subNotebooks.isNotEmpty()) {
                                        expandedNotebooks[item.notebook.id] = !(expandedNotebooks[item.notebook.id] ?: false)
                                        intendToCloseDrawer = false // Expanding/collapsing doesn't close drawer
                                    }
                                    onNotebookClicked(item.notebook, intendToCloseDrawer)
                                }
                                .padding(NavigationDrawerItemDefaults.ItemPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (item.subNotebooks.isNotEmpty()) {
                                    if (expandedNotebooks[item.notebook.id] == true) Icons.Filled.ArrowDropDown else Icons.AutoMirrored.Filled.ArrowRight
                                } else {
                                    Icons.Filled.Folder
                                },
                                contentDescription = if (item.subNotebooks.isNotEmpty()) {
                                    if (expandedNotebooks[item.notebook.id] == true) "Collapse ${item.notebook.name}" else "Expand ${item.notebook.name}"
                                } else item.notebook.name,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(item.notebook.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                            IconButton(onClick = { showNotebookMenuFor = item.notebook }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "Options for ${item.notebook.name}")
                            }
                            DropdownMenu(
                                expanded = showNotebookMenuFor == item.notebook,
                                onDismissRequest = { showNotebookMenuFor = null }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Rename") },
                                    onClick = { onRenameNotebookClicked(item.notebook); showNotebookMenuFor = null },
                                    leadingIcon = { Icon(Icons.Filled.DriveFileRenameOutline, "Rename Notebook") }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = { onDeleteNotebookClicked(item.notebook); showNotebookMenuFor = null },
                                    leadingIcon = { Icon(Icons.Filled.Delete, "Delete Notebook") }
                                )
                            }
                        } // End Row for Notebook item

                        if (expandedNotebooks[item.notebook.id] == true && item.subNotebooks.isNotEmpty()) {
                            Column(modifier = Modifier.padding(start = 24.dp)) { // Indent sub-notebooks
                                item.subNotebooks.forEach { subNotebook: SubNotebook ->
                                    NavigationDrawerItem(
                                        icon = { Icon(Icons.AutoMirrored.Filled.Note, contentDescription = subNotebook.name, modifier = Modifier.padding(start = 12.dp)) },
                                        label = { Text(subNotebook.name) },
                                        selected = false, // TODO: Implement selection state
                                        onClick = { onSubNotebookClicked(subNotebook, item.notebook) },
                                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                    )
                                }
                            }
                        }
                    } // End Column for one notebook + its subnotebooks
                    if (drawerItems.isNotEmpty()) { // Add divider only if there are items
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                } // End forEach drawerItems
            }

            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.Add, "Add New Notebook") },
                label = { Text("Add New Notebook") },
                selected = false,
                onClick = onAddNewNotebookClicked,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // --- Settings Section ---
            // Spacer to push settings towards the bottom, but not necessarily pinned if content above is very long.
            // If Notebooks list is very long, settings will appear after it.
            // If you want settings pinned to bottom regardless of notebook list length,
            // the structure inside ModalDrawerSheet would need a Column with a weighted Spacer above settings.
            // For now, this makes the whole drawer content one scrollable list.
            Spacer(Modifier.weight(1f)) // This will push the following items to the bottom if possible

            HorizontalDivider(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))

            Text(
                "Settings",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Dark Theme Toggle Item
            NavigationDrawerItem(
                label = { Text("Dark Theme") },
                icon = {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Filled.Brightness7 else Icons.Filled.Brightness4, // Sun for light, Moon for dark
                        contentDescription = "Toggle Dark Theme"
                    )
                },
                selected = false,
                onClick = {
                    onThemeToggle(!isDarkTheme) // Clicking the item toggles the theme
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                badge = { // Place the Switch in the 'badge' slot
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = onThemeToggle // The lambda passed from parent updates the state
                    )
                }
            )

            // Navigation Item to a dedicated Settings Screen
            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.Settings, contentDescription = "More Settings") },
                label = { Text("More Settings") },
                selected = false, // TODO: Highlight if current route is SettingsScreen
                onClick = onNavigateToSettingsScreen, // This lambda navigates and closes the drawer
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

        } // End Main Column in ModalDrawerSheet
    } // End ModalDrawerSheet
}