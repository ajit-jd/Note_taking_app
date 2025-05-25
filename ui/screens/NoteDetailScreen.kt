package com.example.project7.ui.screens

// E:\Kotlin2\Project6\app\src\main\java\com\example\project6\ui\screens\NoteDetailScreen.kt

import androidx.compose.ui.platform.LocalContext // Added for contex
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi // For combinedClickable (long press)
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable // For long press
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatIndentDecrease
import androidx.compose.material.icons.automirrored.filled.FormatIndentIncrease
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.project7.data.Label
import com.example.project7.data.Note
import com.example.project7.viewmodel.NoteViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import android.content.Intent
import android.speech.RecognizerIntent
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import android.Manifest
//import android.util.Log

import com.example.project7.ui.utils.* // Imports all utils: helpers, constants, FormattingButton
//import java.util.jar.Manifest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun NoteDetailScreen( navController: NavController, viewModel: NoteViewModel, noteId: Int )
{ val lifecycleOwner = LocalLifecycleOwner.current
    Log.d("NoteDetailScreen", "Recomposing. NoteID: $noteId")
    val isNewNote = noteId == -1
    var currentNoteIdInternal by remember(noteId) { mutableStateOf(if (isNewNote) -1 else noteId) }
    var titleTfv by remember { mutableStateOf(TextFieldValue("")) }
    var contentTfv by remember { mutableStateOf(TextFieldValue(AnnotatedString(""))) }
    var initialDataApplied by remember { mutableStateOf(false) }
    var noteImageUriString by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val autoSaveDelayMillis = 1500L
    var titleAutoSaveJob by remember { mutableStateOf<Job?>(null) }
    var contentAutoSaveJob by remember { mutableStateOf<Job?>(null) }


    val labelPrefix = "--"
    var showLabelSuggestions by remember { mutableStateOf(false) }
    var currentLabelQuery by remember { mutableStateOf("") }
    val allLabels: List<Label> by viewModel.allLabelsCorrected.collectAsStateWithLifecycleManual(initialValueOverride = emptyList())
    val suggestedLabels = remember(currentLabelQuery, allLabels, showLabelSuggestions) {
        if (currentLabelQuery.isBlank() || !showLabelSuggestions) emptyList()
        else allLabels.filter { it.name.contains(currentLabelQuery, ignoreCase = true) && it.name.lowercase() != currentLabelQuery.lowercase() }
    }
    var existingNoteLabels by remember { mutableStateOf<List<Label>>(emptyList()) }
    val labelsToAddToNewNote = remember { mutableStateListOf<Int>() }

// Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            Log.d("ImageUpload", "Image URI selected: $uri")
            val newImageUriString = uri.toString()
            noteImageUriString = newImageUriString
            if (initialDataApplied) {
                contentAutoSaveJob?.cancel()
                titleAutoSaveJob?.cancel()
                val titleToSave = titleTfv.text
                val contentToSave = contentTfv.annotatedString.text
                Log.d("AutoSave", "Image picked: Triggering immediate save. ID: $currentNoteIdInternal")
                scope.launch {
                    val savedId = viewModel.addOrUpdateNoteAndGetId(
                        titleToSave,
                        contentToSave,
                        if (currentNoteIdInternal == -1) null else currentNoteIdInternal,
                        newImageUriString
                    )
                    if (currentNoteIdInternal == -1 && savedId > 0) {
                        currentNoteIdInternal = savedId.toInt()
                        labelsToAddToNewNote.forEach { labelId -> viewModel.addLabelToNote(currentNoteIdInternal, labelId) }
                        labelsToAddToNewNote.clear()
                    }
                }
            }
        } else {
            Log.d("ImageUpload", "No image selected.")
        }
    }
    var selectedBackgroundColor by remember { mutableStateOf(Color.Transparent) }
// Auto-Save Helper
    val triggerAutoSave = { isTitleChange: Boolean ->
        val titleToSave = titleTfv.text
        val contentToSave = contentTfv.annotatedString.text
        val currentImageUriToSave = noteImageUriString
        if (initialDataApplied && (titleToSave.isNotBlank() || contentToSave.isNotBlank() || currentImageUriToSave != null || currentNoteIdInternal != -1)) {
            Log.d("AutoSave", "Queuing save. Title: '$titleToSave', Image: $currentImageUriToSave, ID: $currentNoteIdInternal")
            if (isTitleChange) titleAutoSaveJob?.cancel() else contentAutoSaveJob?.cancel()
            val newJob = scope.launch {
                delay(autoSaveDelayMillis)
                Log.d("AutoSave", "Debounced save executing for ID: $currentNoteIdInternal")
                val savedId = viewModel.addOrUpdateNoteAndGetId(
                    titleToSave,
                    contentToSave,
                    if (currentNoteIdInternal == -1) null else currentNoteIdInternal,
                    currentImageUriToSave
                )
                if (currentNoteIdInternal == -1 && savedId > 0) {
                    currentNoteIdInternal = savedId.toInt()
                    labelsToAddToNewNote.forEach { labelId -> viewModel.addLabelToNote(currentNoteIdInternal, labelId) }
                    labelsToAddToNewNote.clear()
                }
            }
            if (isTitleChange) titleAutoSaveJob = newJob else contentAutoSaveJob = newJob
        }
    }

// Load existing note
    if (!isNewNote && currentNoteIdInternal != -1) {
        LaunchedEffect(currentNoteIdInternal, viewModel, lifecycleOwner) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    viewModel.getNoteById(currentNoteIdInternal).collect { noteFromFlow ->
                        if (noteFromFlow != null && !initialDataApplied) {
                            titleTfv = TextFieldValue(noteFromFlow.title, TextRange(noteFromFlow.title.length))
                            contentTfv = TextFieldValue(AnnotatedString(noteFromFlow.content), TextRange(noteFromFlow.content.length))
                            noteImageUriString = noteFromFlow.imageUriString
                            selectedBackgroundColor = noteFromFlow.backgroundColor?.let { Color(it) } ?: Color.Transparent
                            initialDataApplied = true
                        } else if (noteFromFlow == null && initialDataApplied) navController.navigateUp()
                    }
                }
                launch { viewModel.getLabelsForNote(currentNoteIdInternal).collect { labels -> existingNoteLabels = labels } }
            }
        }
    } else {
        LaunchedEffect(Unit) {
            if (!initialDataApplied) {
                titleTfv = TextFieldValue("")
                contentTfv = TextFieldValue(AnnotatedString(""))
                noteImageUriString = null
                initialDataApplied = true
            }
        }
    }

// Save on pause
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE && initialDataApplied) {
                titleAutoSaveJob?.cancel()
                contentAutoSaveJob?.cancel()
                val title = titleTfv.text
                val content = contentTfv.annotatedString.text
                val imageUriToSave = noteImageUriString
                if (title.isNotBlank() || content.isNotBlank() || imageUriToSave != null || currentNoteIdInternal != -1) {
                    scope.launch {
//                        Log.d("NoteFormatting", "Saving note with ID: $currentNoteIdInternal, Color: ${colorValue.value.toLong()}")
                        val savedId = viewModel.addOrUpdateNoteAndGetId(
                            title,
                            content,
                            if (currentNoteIdInternal == -1) null else currentNoteIdInternal,
                            imageUriToSave,
                            selectedBackgroundColor.value.toLong()
                        )
                        if (currentNoteIdInternal == -1 && savedId > 0) {
                            currentNoteIdInternal = savedId.toInt()
                            labelsToAddToNewNote.forEach { viewModel.addLabelToNote(currentNoteIdInternal, it) }
                            labelsToAddToNewNote.clear()
                        }
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

// Formatting state
    val currentSelection = contentTfv.selection
    val currentAnnotatedString = contentTfv.annotatedString

    val isBoldNow by remember(currentAnnotatedString, currentSelection) {
        derivedStateOf {
            if (currentSelection.collapsed) {
                currentAnnotatedString.spanStyles.any {
                    it.item.fontWeight == FontWeight.Bold &&
                            currentSelection.start > it.start &&
                            currentSelection.start <= it.end
                }
            } else {
                currentAnnotatedString.spanStyles.any {
                    it.item.fontWeight == FontWeight.Bold &&
                            currentSelection.min < it.end &&
                            currentSelection.max > it.start
                }
            }
        }
    }

    val isItalicNow by remember(currentAnnotatedString, currentSelection) {
        derivedStateOf {
            if (currentSelection.collapsed) {
                currentAnnotatedString.spanStyles.any {
                    it.item.fontStyle == FontStyle.Italic &&
                            currentSelection.start > it.start &&
                            currentSelection.start <= it.end
                }
            } else {
                currentAnnotatedString.spanStyles.any {
                    it.item.fontStyle == FontStyle.Italic &&
                            currentSelection.min < it.end &&
                            currentSelection.max > it.start
                }
            }
        }
    }

    val isUnderlinedNow by remember(currentAnnotatedString, currentSelection) {
        derivedStateOf {
            if (currentSelection.collapsed) {
                currentAnnotatedString.spanStyles.any {
                    it.item.textDecoration == TextDecoration.Underline &&
                            currentSelection.start > it.start &&
                            currentSelection.start <= it.end
                }
            } else {
                currentAnnotatedString.spanStyles.any {
                    it.item.textDecoration == TextDecoration.Underline &&
                            currentSelection.min < it.end &&
                            currentSelection.max > it.start
                }
            }
        }
    }

    val isBulletedNow by remember(currentAnnotatedString.text, currentSelection) {
        derivedStateOf {
            val checkPosition = if (currentSelection.collapsed) currentSelection.start else currentSelection.min
            val lineStart = findLineStart(currentAnnotatedString.text, checkPosition)
            currentAnnotatedString.text.substring(lineStart).startsWith(BULLET_PREFIX)
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isNewNote && currentNoteIdInternal == -1) "New Note"
                        else (titleTfv.text.takeIf { it.isNotBlank() }?.take(30) ?: "Note"),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        titleAutoSaveJob?.cancel()
                        contentAutoSaveJob?.cancel()
                        val title = titleTfv.text
                        val content = contentTfv.annotatedString.text
                        val imageUriToSave = noteImageUriString
                        if (initialDataApplied && (title.isNotBlank() || content.isNotBlank() || imageUriToSave != null || currentNoteIdInternal != -1)) {
                            scope.launch {
                                val savedId = viewModel.addOrUpdateNoteAndGetId(
                                    title,
                                    content,
                                    if (currentNoteIdInternal == -1) null else currentNoteIdInternal,
                                    imageUriToSave
                                )
                                if (currentNoteIdInternal == -1 && savedId > 0) {
                                    currentNoteIdInternal = savedId.toInt()
                                    labelsToAddToNewNote.forEach { viewModel.addLabelToNote(currentNoteIdInternal, it) }
                                    labelsToAddToNewNote.clear()
                                }
                                navController.navigateUp()
                            }
                        } else {
                            navController.navigateUp()
                        }
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    if (currentNoteIdInternal != -1) {
                        IconButton(onClick = { viewModel.deleteNoteById(currentNoteIdInternal); navController.navigateUp() }) {
                            Icon(Icons.Filled.Delete, "Delete Note")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(scrollState)
                .background(selectedBackgroundColor)
        ) {
            BasicTextField(
                value = titleTfv,
                onValueChange = { newTfv ->
                    titleTfv = newTfv
                    if (initialDataApplied) triggerAutoSave(true)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused && initialDataApplied) {
                            titleAutoSaveJob?.cancel()
                            scope.launch {
                                val id = viewModel.addOrUpdateNoteAndGetId(
                                    titleTfv.text,
                                    contentTfv.annotatedString.text,
                                    if (currentNoteIdInternal == -1) null else currentNoteIdInternal,
                                    noteImageUriString
                                )
                                if (currentNoteIdInternal == -1 && id > 0) currentNoteIdInternal = id.toInt()
                            }
                        }
                    },
                textStyle = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onSurface),
                decorationBox = { inner ->
                    if (titleTfv.text.isEmpty()) {
                        Text(
                            "Title",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    inner()
                },
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )

            // Formatting Toolbar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    // Bold Button
                    FormattingButton(
                        icon = Icons.Filled.FormatBold,
                        desc = "Bold",
                        isSelected = isBoldNow,
                        onClick = {
                            if (!currentSelection.collapsed) {
                                val builder = AnnotatedString.Builder()
                                val originalText = contentTfv.annotatedString
                                if (isBoldNow) {
                                    builder.append(originalText.subSequence(0, currentSelection.min))
                                    val selectedPart = originalText.subSequence(currentSelection.min, currentSelection.max)
                                    val newSelectedPart = AnnotatedString.Builder()
                                    selectedPart.text.forEachIndexed { index, char ->
                                        val styles = selectedPart.spanStyles.filter { it.start <= index && it.end > index }
                                        val mergedStyle = styles
                                            .map { it.item }
                                            .filter { it.fontWeight != FontWeight.Bold }
                                            .fold(SpanStyle()) { acc, style -> acc.merge(style) }
                                        newSelectedPart.withStyle(mergedStyle) { append(char.toString()) }
                                    }
                                    builder.append(newSelectedPart.toAnnotatedString())
                                    builder.append(originalText.subSequence(currentSelection.max, originalText.length))
                                    contentTfv = contentTfv.copy(
                                        annotatedString = builder.toAnnotatedString(),
                                        selection = currentSelection
                                    )
                                } else {
                                    builder.append(originalText.subSequence(0, currentSelection.min))
                                    builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(originalText.subSequence(currentSelection.min, currentSelection.max))
                                    }
                                    builder.append(originalText.subSequence(currentSelection.max, originalText.length))
                                    contentTfv = contentTfv.copy(
                                        annotatedString = builder.toAnnotatedString(),
                                        selection = currentSelection
                                    )
                                }
                                if (initialDataApplied) triggerAutoSave(false)
                            }
                        }
                    )

                    // Italic Button
                    FormattingButton(
                        icon = Icons.Filled.FormatItalic,
                        desc = "Italic",
                        isSelected = isItalicNow,
                        onClick = {
                            if (!currentSelection.collapsed) {
                                val builder = AnnotatedString.Builder()
                                val originalText = contentTfv.annotatedString
                                builder.append(originalText.subSequence(0, currentSelection.min))
                                val selectedPartOriginal = originalText.subSequence(currentSelection.min, currentSelection.max)
                                val selectedPartBuilder = AnnotatedString.Builder()
                                selectedPartOriginal.text.forEachIndexed { index, char ->
                                    val existingStylesOnChar = selectedPartOriginal.spanStyles
                                        .filter { it.start <= index && it.end > index }
                                        .map { it.item }
                                    val mergedStyle = existingStylesOnChar.fold(SpanStyle()) { acc, style -> acc.merge(style) }
                                    val toggledFontStyle = if (mergedStyle.fontStyle == FontStyle.Italic) FontStyle.Normal else FontStyle.Italic
                                    val finalStyle = mergedStyle.copy(fontStyle = toggledFontStyle)
                                    selectedPartBuilder.withStyle(finalStyle) { append(char.toString()) }
                                }
                                builder.append(selectedPartBuilder.toAnnotatedString())
                                builder.append(originalText.subSequence(currentSelection.max, originalText.length))
                                contentTfv = contentTfv.copy(
                                    annotatedString = builder.toAnnotatedString(),
                                    selection = currentSelection
                                )
                                if (initialDataApplied) triggerAutoSave(false)
                            }
                        }
                    )

                    // Underline Button
                    FormattingButton(
                        icon = Icons.Filled.FormatUnderlined,
                        desc = "Underline",
                        isSelected = isUnderlinedNow,
                        onClick = {
                            if (!currentSelection.collapsed) {
                                val builder = AnnotatedString.Builder()
                                val originalText = contentTfv.annotatedString
                                builder.append(originalText.subSequence(0, currentSelection.min))
                                val selectedPartOriginal = originalText.subSequence(currentSelection.min, currentSelection.max)
                                val selectedPartBuilder = AnnotatedString.Builder()
                                selectedPartOriginal.text.forEachIndexed { index, char ->
                                    val existingStylesOnChar = selectedPartOriginal.spanStyles
                                        .filter { it.start <= index && it.end > index }
                                        .map { it.item }
                                    val newSpanStyle = existingStylesOnChar.fold(SpanStyle()) { acc, style -> acc.merge(style) }
                                    val currentTextDecoration = newSpanStyle.textDecoration
                                    val toggledTextDecoration = if (currentTextDecoration == TextDecoration.Underline) {
                                        TextDecoration.None
                                    } else {
                                        TextDecoration.Underline
                                    }
                                    selectedPartBuilder.withStyle(newSpanStyle.copy(textDecoration = toggledTextDecoration)) {
                                        append(char.toString())
                                    }
                                }
                                builder.append(selectedPartBuilder.toAnnotatedString())
                                builder.append(originalText.subSequence(currentSelection.max, originalText.length))
                                contentTfv = contentTfv.copy(
                                    annotatedString = builder.toAnnotatedString(),
                                    selection = currentSelection
                                )
                                if (initialDataApplied) triggerAutoSave(false)
                            }
                        }
                    )

                    // List Button
                    FormattingButton(
                        icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                        desc = "List",
                        isSelected = isBulletedNow,
                        onClick = {
                            val selection = contentTfv.selection
                            val textToModify = contentTfv.annotatedString
                            val builder = AnnotatedString.Builder()
                            val lineStartIndex = findLineStart(textToModify.text, selection.start)
                            val lineEndIndex = findLineEnd(textToModify.text, selection.start)
                            val lineContent = textToModify.subSequence(lineStartIndex, lineEndIndex)
                            builder.append(textToModify.subSequence(0, lineStartIndex))
                            var newCursorPositionAfterAction = selection.start
                            if (lineContent.text.startsWith(BULLET_PREFIX)) {
                                builder.append(lineContent.subSequence(BULLET_PREFIX.length, lineContent.length))
                                if (selection.start >= lineStartIndex + BULLET_PREFIX.length) {
                                    newCursorPositionAfterAction = (selection.start - BULLET_PREFIX.length).coerceAtLeast(lineStartIndex)
                                } else {
                                    newCursorPositionAfterAction = lineStartIndex
                                }
                            } else {
                                builder.append(BULLET_PREFIX)
                                builder.append(lineContent)
                                newCursorPositionAfterAction = selection.start + BULLET_PREFIX.length
                            }
                            builder.append(textToModify.subSequence(lineEndIndex, textToModify.length))
                            val newSelectionEnd = if (!selection.collapsed) {
                                val originalLength = selection.length
                                val changeInPrefix = if (lineContent.text.startsWith(BULLET_PREFIX)) -BULLET_PREFIX.length else BULLET_PREFIX.length
                                (newCursorPositionAfterAction + originalLength + (if (selection.start == lineStartIndex) changeInPrefix else 0)).coerceAtMost(builder.length)
                            } else {
                                newCursorPositionAfterAction
                            }
                            contentTfv = TextFieldValue(
                                annotatedString = builder.toAnnotatedString(),
                                selection = TextRange(newCursorPositionAfterAction, newSelectionEnd.coerceAtLeast(newCursorPositionAfterAction))
                            )
                            if (initialDataApplied) triggerAutoSave(false)
                        }
                    )

                    // Indent Button
                    FormattingButton(
                        icon = Icons.AutoMirrored.Filled.FormatIndentIncrease,
                        desc = "Indent (Tab)",
                        isSelected = false,
                        onClick = {
                            val selection = contentTfv.selection
                            val currentText = contentTfv.annotatedString
                            val builder = AnnotatedString.Builder()
                            if (selection.collapsed) {
                                builder.append(currentText.subSequence(0, selection.start))
                                builder.append(TAB_SPACES)
                                builder.append(currentText.subSequence(selection.start, currentText.length))
                                contentTfv = TextFieldValue(
                                    builder.toAnnotatedString(),
                                    TextRange(selection.start + TAB_SPACES.length)
                                )
                            } else {
                                val lineStart = findLineStart(currentText.text, selection.min)
                                builder.append(currentText.subSequence(0, lineStart))
                                builder.append(TAB_SPACES)
                                builder.append(currentText.subSequence(lineStart, currentText.length))
                                contentTfv = TextFieldValue(
                                    builder.toAnnotatedString(),
                                    TextRange(selection.end + TAB_SPACES.length)
                                )
                            }
                            if (initialDataApplied) triggerAutoSave(false)
                        }
                    )

                    // Outdent Button
                    FormattingButton(
                        icon = Icons.AutoMirrored.Filled.FormatIndentDecrease,
                        desc = "Outdent",
                        isSelected = false,
                        onClick = {
                            val selection = contentTfv.selection
                            val currentText = contentTfv.annotatedString
                            val builder = AnnotatedString.Builder()
                            val lineStart = findLineStart(currentText.text, selection.start)
                            val lineContent = currentText.text.substring(lineStart, findLineEnd(currentText.text, selection.start))
                            builder.append(currentText.subSequence(0, lineStart))
                            if (lineContent.startsWith(TAB_SPACES)) {
                                builder.append(lineContent.substring(TAB_SPACES.length))
                                builder.append(currentText.subSequence(findLineEnd(currentText.text, selection.start), currentText.length))
                                val newCursorPosition = if (selection.start >= lineStart + TAB_SPACES.length) {
                                    (selection.start - TAB_SPACES.length).coerceAtLeast(lineStart)
                                } else {
                                    selection.start
                                }
                                contentTfv = TextFieldValue(
                                    builder.toAnnotatedString(),
                                    TextRange(newCursorPosition)
                                )
                            } else {
                                builder.append(currentText.subSequence(lineStart, currentText.length))
                                contentTfv = TextFieldValue(
                                    builder.toAnnotatedString(),
                                    selection
                                )
                            }
                            if (initialDataApplied) triggerAutoSave(false)
                        }
                    )

                    VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))

                    // Image Upload Button
                    FormattingButton(
                        icon = Icons.Filled.Image,
                        desc = "Add Image",
                        isSelected = false,
                        onClick = {
                            Log.d("NoteFormatting", "Add Image clicked - launching picker")
                            try {
                                imagePickerLauncher.launch("image/*")
                            } catch (e: Exception) {
                                Log.e("ImageUpload", "Error launching image picker: ${e.message}")
                            }
                        }
                    )

                    // BACKGROUND COLOR BUTTON
                    var showBgColorMenu by remember { mutableStateOf(false) }
                    Box {
                        FormattingButton(
                            icon = Icons.Filled.Palette,
                            desc = "Change Background Color",
                            isSelected = showBgColorMenu,
                            onClick = {
                                Log.d("NoteFormatting", "Change Background Color clicked")
                                showBgColorMenu = true
                            }
                        )
                        DropdownMenu(
                            expanded = showBgColorMenu,
                            onDismissRequest = { showBgColorMenu = false }
                        ) {
                            val noteBackgroundColors = listOf(
                                "Default" to Color.Transparent,
                                "Light Yellow" to Color(0xFFFFF9C4),
                                "Light Blue" to Color(0xFFB3E5FC),
                                "Light Green" to Color(0xFFC8E6C9),
                                "Light Pink" to Color(0xFFF8BBD0)
                            )
                            noteBackgroundColors.forEach { (name, colorValue) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .background(
                                                        if (colorValue == Color.Transparent)
                                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                                        else colorValue,
                                                        CircleShape
                                                    )
                                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(name)
                                        }
                                    },
                                    onClick = {
                                        Log.d("NoteFormatting", "Selected background color: $name")
                                        selectedBackgroundColor = colorValue
                                        if (initialDataApplied) {
                                            contentAutoSaveJob?.cancel()
                                            titleAutoSaveJob?.cancel()
                                            scope.launch {
                                                val savedId = viewModel.addOrUpdateNoteAndGetId(
                                                    titleTfv.text,
                                                    contentTfv.annotatedString.text,
                                                    if (currentNoteIdInternal == -1) null else currentNoteIdInternal,
                                                    noteImageUriString,
                                                    colorValue.value.toLong()
                                                )
                                                if (currentNoteIdInternal == -1 && savedId > 0) {
                                                    currentNoteIdInternal = savedId.toInt()
                                                    labelsToAddToNewNote.forEach { viewModel.addLabelToNote(currentNoteIdInternal, it) }
                                                    labelsToAddToNewNote.clear()
                                                }
                                            }
                                        }
                                        showBgColorMenu = false
                                    }
                                )
                            }
                        }
                    }
                    // MIC BUTTON (VOICE INPUT)
                    var isRecording by remember { mutableStateOf(false) }
                    val context = LocalContext.current // Get Context in composable scope
                    val speechRecognizerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        if (result.resultCode == Activity.RESULT_OK) {
                            val data = result.data
                            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                            results?.firstOrNull()?.let { recognizedText ->
                                Log.d("VoiceInput", "Recognized text: $recognizedText")
                                val currentContent = contentTfv.annotatedString.text
                                val newContent = if (currentContent.isEmpty()) recognizedText else "$currentContent $recognizedText"
                                contentTfv = TextFieldValue(
                                    text = AnnotatedString(newContent).toString(),
                                    selection = TextRange(newContent.length)
                                )
                                triggerAutoSave(false) // Trigger autosave
                            }
                        }
                        isRecording = false
                    }
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) {
                            Log.d("VoiceInput", "RECORD_AUDIO permission granted")
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your note...")
                            }
                            speechRecognizerLauncher.launch(intent)
                            isRecording = true
                        } else {
                            Log.d("VoiceInput", "RECORD_AUDIO permission denied")
                            // Optional: Show Snackbar (requires SnackbarHostState)
                        }
                    }
                    FormattingButton(
                        icon = Icons.Filled.Mic,
                        desc = "Voice Input",
                        isSelected = isRecording,
                        onClick = {
                            Log.d("VoiceInput", "Mic button clicked")
                            if (isRecording) {
                                isRecording = false // System dialog handles stopping
                            } else {
                                val permission = Manifest.permission.RECORD_AUDIO
                                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) { // Use context
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your note...")
                                    }
                                    speechRecognizerLauncher.launch(intent)
                                    isRecording = true
                                } else {
                                    permissionLauncher.launch(permission)
                                }
                            }
                        }
                    )
                }
            }

            // Content and Label Suggestions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            ) {
                BasicTextField(
                    value = contentTfv,
                    onValueChange = { newTfv ->
                        val oldAnnotatedText = contentTfv.annotatedString
                        val oldSelectionEnd = contentTfv.selection.end
                        contentTfv = newTfv
                        if (initialDataApplied) triggerAutoSave(false)

                        // Handle Enter key for lists
                        if (newTfv.annotatedString.text.length > oldAnnotatedText.text.length &&
                            oldSelectionEnd < newTfv.annotatedString.text.length &&
                            newTfv.annotatedString.text[oldSelectionEnd] == '\n' &&
                            newTfv.selection.collapsed && newTfv.selection.start == oldSelectionEnd + 1
                        ) {
                            val lineStartBeforeNewline = findLineStart(oldAnnotatedText.text, oldSelectionEnd)
                            val currentLineContentBeforeNewline = oldAnnotatedText.text.substring(lineStartBeforeNewline, oldSelectionEnd)
                            if (currentLineContentBeforeNewline.startsWith(BULLET_PREFIX)) {
                                val builder = AnnotatedString.Builder()
                                if (currentLineContentBeforeNewline == BULLET_PREFIX) {
                                    builder.append(oldAnnotatedText.subSequence(0, lineStartBeforeNewline))
                                    val textAfterNewlineInNewTfv = newTfv.annotatedString.subSequence(oldSelectionEnd + 1, newTfv.annotatedString.length)
                                    builder.append(textAfterNewlineInNewTfv)
                                    contentTfv = TextFieldValue(builder.toAnnotatedString(), TextRange(lineStartBeforeNewline))
                                } else {
                                    val builderContinueList = AnnotatedString.Builder()
                                    builderContinueList.append(newTfv.annotatedString.subSequence(0, newTfv.selection.start))
                                    builderContinueList.append(BULLET_PREFIX)
                                    if (newTfv.selection.start < newTfv.annotatedString.length) {
                                        builderContinueList.append(newTfv.annotatedString.subSequence(newTfv.selection.start, newTfv.annotatedString.length))
                                    }
                                    contentTfv = TextFieldValue(
                                        annotatedString = builderContinueList.toAnnotatedString(),
                                        selection = TextRange(newTfv.selection.start + BULLET_PREFIX.length)
                                    )
                                }
                            }
                        }

                        // Label suggestion logic
                        val textForLabel = newTfv.annotatedString.text
                        val cursorPos = newTfv.selection.end
                        val prefixStart = textForLabel.lastIndexOf(labelPrefix, startIndex = cursorPos - labelPrefix.length)
                        if (prefixStart != -1) {
                            val queryStart = prefixStart + labelPrefix.length
                            if (cursorPos >= queryStart) {
                                val query = textForLabel.substring(queryStart, cursorPos)
                                if (query.isNotEmpty() && !query.contains(" ") && !query.contains("\n")) {
                                    currentLabelQuery = query
                                    showLabelSuggestions = true
                                } else {
                                    currentLabelQuery = ""
                                    showLabelSuggestions = false
                                }
                            } else {
                                showLabelSuggestions = false
                            }
                        } else {
                            showLabelSuggestions = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused && initialDataApplied) {
                                contentAutoSaveJob?.cancel()
                                scope.launch {
                                    val id = viewModel.addOrUpdateNoteAndGetId(
                                        titleTfv.text,
                                        contentTfv.annotatedString.text,
                                        if (currentNoteIdInternal == -1) null else currentNoteIdInternal,
                                        noteImageUriString
                                    )
                                    if (currentNoteIdInternal == -1 && id > 0) currentNoteIdInternal = id.toInt()
                                }
                            }
                        },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    decorationBox = { inner ->
                        if (contentTfv.annotatedString.isEmpty()) {
                            Text(
                                "Note",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        inner()
                    },
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )

                // Label Suggestion DropdownMenu
                DropdownMenu(
                    expanded = showLabelSuggestions && suggestedLabels.isNotEmpty(),
                    onDismissRequest = { showLabelSuggestions = false },
                    properties = PopupProperties(focusable = false)
                ) {
                    suggestedLabels.take(5).forEach { label ->
                        DropdownMenuItem(
                            text = { Text(label.name) },
                            onClick = {
                                val textAnn = contentTfv.annotatedString
                                val cursor = contentTfv.selection.end
                                val prefixStart = textAnn.text.lastIndexOf(labelPrefix, startIndex = cursor - labelPrefix.length)
                                if (prefixStart != -1) {
                                    val builder = AnnotatedString.Builder()
                                    builder.append(textAnn.subSequence(0, prefixStart))
                                    builder.append(label.name + " ")
                                    builder.append(textAnn.subSequence(cursor, textAnn.length))
                                    val newCursorPos = prefixStart + label.name.length + 1
                                    contentTfv = TextFieldValue(builder.toAnnotatedString(), TextRange(newCursorPos))
                                    if (currentNoteIdInternal != -1) {
                                        scope.launch { viewModel.addLabelToNote(currentNoteIdInternal, label.id) }
                                    } else if (!labelsToAddToNewNote.contains(label.id)) {
                                        labelsToAddToNewNote.add(label.id)
                                    }
                                }
                                showLabelSuggestions = false
                                currentLabelQuery = ""
                            }
                        )
                    }
                }
            }

            // Display Selected Image
            noteImageUriString?.let { uriString ->
                val imageUri = try { Uri.parse(uriString) } catch (e: Exception) { null }
                imageUri?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .heightIn(max = 250.dp)
                            .combinedClickable(
                                onClick = {
                                    Log.d("ImageDisplay", "Image tapped (short press)")
                                    // TODO: Implement full-screen image viewer
                                },
                                onLongClick = {
                                    Log.d("ImageDisplay", "Image long pressed - removing")
                                    noteImageUriString = null
                                    contentAutoSaveJob?.cancel()
                                    scope.launch {
                                        Log.d("AutoSave", "Image removed: Triggering immediate save. ID: $currentNoteIdInternal")
                                        val savedId = viewModel.addOrUpdateNoteAndGetId(
                                            titleTfv.text,
                                            contentTfv.annotatedString.text,
                                            if (currentNoteIdInternal == -1) null else currentNoteIdInternal,
                                            null
                                        )
                                        if (currentNoteIdInternal == -1 && savedId > 0) {
                                            currentNoteIdInternal = savedId.toInt()
                                            labelsToAddToNewNote.forEach { viewModel.addLabelToNote(currentNoteIdInternal, it) }
                                            labelsToAddToNewNote.clear()
                                        }
                                    }
                                }
                            )
                    ) {
                        AsyncImage(
                            model = it,
                            contentDescription = "Attached image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // Display Existing Labels
            if (existingNoteLabels.isNotEmpty() && !WindowInsets.isImeVisible) {
                HorizontalDivider(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp))
                Text(
                    "Labels:",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 16.dp, top = 0.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    existingNoteLabels.forEach { labelObject ->
                        InputChip(
                            selected = false,
                            onClick = {
                                Log.d("LabelChip", "Clicked on label: ${labelObject.name} to remove.")
                                if (currentNoteIdInternal != -1) {
                                    scope.launch { viewModel.removeLabelFromNote(currentNoteIdInternal, labelObject.id) }
                                } else {
                                    labelsToAddToNewNote.remove(labelObject.id)
                                }
                            },
                            label = {
                                Text(labelObject.name, style = MaterialTheme.typography.labelSmall)
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Remove label ${labelObject.name}",
                                    modifier = Modifier.size(InputChipDefaults.IconSize)
                                )
                            }
                        )
                    }
                }
            } else if (existingNoteLabels.isNotEmpty() && WindowInsets.isImeVisible) {
                Log.d("LabelsVisibility", "Keyboard is visible, hiding existing labels section.")
            }
        }
    }
    Log.d("LifecycleDebug", "NoteDetailScreen Composable: End (noteId: $currentNoteIdInternal)")
}