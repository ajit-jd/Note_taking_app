// E:\Kotlin2\Project6\app\src\main\java\com\example\project6\viewmodel\NoteViewModel.kt
package com.example.project7.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.project7.data.*
import com.example.project7.db.AppDatabase
import com.example.project7.db.LabelDao
import com.example.project7.db.NoteDao
import com.example.project7.db.NotebookDao
import com.example.project7.db.SubNotebookDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NotebookDrawerItem(
    val notebook: Notebook,
    val subNotebooks: List<SubNotebook>
)

sealed class ViewContext {
    data object AllNotes : ViewContext()
    data class NotebookView(val notebook: Notebook) : ViewContext()
    data class SubNotebookView(val subNotebook: SubNotebook, val parentNotebook: Notebook) : ViewContext()
    data class LabelView(val label: Label) : ViewContext()
}

@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var noteDao: NoteDao
    private lateinit var notebookDao: NotebookDao
    private lateinit var subNotebookDao: SubNotebookDao
    private lateinit var labelDao: LabelDao

    private val _allNotebooks = MutableStateFlow<List<Notebook>>(emptyList())
    val notebookDrawerItems: StateFlow<List<NotebookDrawerItem>> = _allNotebooks.flatMapLatest { notebooks ->
        if (notebooks.isEmpty() || !::subNotebookDao.isInitialized) {
            flowOf(emptyList())
        } else {
            combine(notebooks.map { notebook ->
                subNotebookDao.getSubNotebooksForNotebook(notebook.id)
                    .map { subNotebooks -> NotebookDrawerItem(notebook, subNotebooks) }
                    .catch { emit(NotebookDrawerItem(notebook, emptyList())) }
            }) { it.toList().filterNotNull() }
        }
    }.catch { e -> Log.e("VM_DEBUG", "Error in notebookDrawerItems: ${e.message}"); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _currentViewContext = MutableStateFlow<ViewContext>(ViewContext.AllNotes)
    val currentViewContext: StateFlow<ViewContext> = _currentViewContext.asStateFlow()

    val currentViewTitle: StateFlow<String> = _currentViewContext.map { context ->
        when (context) {
            is ViewContext.AllNotes -> "All Notes"
            is ViewContext.NotebookView -> context.notebook.name
            is ViewContext.SubNotebookView -> context.subNotebook.name
            is ViewContext.LabelView -> "Label: ${context.label.name}"
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, "All Notes")

    val notesForDisplay: StateFlow<List<Note>> = _currentViewContext.flatMapLatest { context ->
        Log.d("VM_NOTES", "NotesForDisplay: Context changed to $context")
        if (!::noteDao.isInitialized || !::labelDao.isInitialized) {
            flowOf(emptyList())
        } else {
            when (context) {
                is ViewContext.AllNotes -> noteDao.getAllNotes()
                is ViewContext.NotebookView -> noteDao.getNotesByNotebookId(context.notebook.id)
                is ViewContext.SubNotebookView -> noteDao.getNotesBySubNotebookId(context.subNotebook.id)
                is ViewContext.LabelView -> labelDao.getNotesByLabelId(context.label.id)
            }
        }
    }.catch { e -> Log.e("VM_DEBUG", "Error in notesForDisplay: ${e.message}"); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    lateinit var allLabelsCorrected: StateFlow<List<Label>>

    init {
        Log.d("VM_DEBUG", "NoteViewModel init - START")
        val db = AppDatabase.getDatabase(application)
        noteDao = db.noteDao()
        notebookDao = db.notebookDao()
        subNotebookDao = db.subNotebookDao()
        labelDao = db.labelDao()
        Log.d("VM_DEBUG", "DAOs initialized")

        allLabelsCorrected = labelDao.getAllLabels()
            .catch { e -> Log.e("VM_LABELS", "Error fetching all labels: ${e.message}"); emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        viewModelScope.launch {
            notebookDao.getAllNotebooks()
                .catch { e -> Log.e("VM_DEBUG", "Error collecting notebooks: $e"); emit(emptyList()) }
                .collect { notebooks -> _allNotebooks.value = notebooks }
        }

        viewModelScope.launch {
            try {
                if (notebookDao.getAllNotebooks().firstOrNull().isNullOrEmpty()) {
                    val defaultNotebookId = addNotebook("My First Notebook")
                    if (defaultNotebookId != -1L) {
                        addSubNotebook("Work Tasks", defaultNotebookId.toInt())
                    }
                }
                if (allLabelsCorrected.value.isEmpty() && labelDao.getAllLabels().firstOrNull().isNullOrEmpty()) {
                    Log.d("VM_SEED", "Seeding initial labels.")
                    addLabel("Important")
                    addLabel("Personal")
                }
            } catch (e: Exception) {
                Log.e("VM_SEED", "Error during initial data seeding: ${e.message}", e)
            }
        }
        Log.d("VM_DEBUG", "NoteViewModel init - END")
    }

    fun showAllNotes() { _currentViewContext.value = ViewContext.AllNotes }
    fun showNotebookView(notebook: Notebook) { _currentViewContext.value = ViewContext.NotebookView(notebook) }
    fun showSubNotebookView(subNotebook: SubNotebook, parentNotebook: Notebook) { _currentViewContext.value = ViewContext.SubNotebookView(subNotebook, parentNotebook) }
    fun showLabelView(label: Label) { _currentViewContext.value = ViewContext.LabelView(label) }

    suspend fun addLabel(name: String): Long {
        val label = Label(name = name.trim())
        return labelDao.insertLabel(label)
    }
    fun updateLabel(label: Label) = viewModelScope.launch { labelDao.updateLabel(label) }
    fun deleteLabel(label: Label) = viewModelScope.launch {
        labelDao.deleteLabel(label)
        if (_currentViewContext.value is ViewContext.LabelView && (_currentViewContext.value as ViewContext.LabelView).label.id == label.id) {
            showAllNotes()
        }
    }

    fun addLabelToNote(noteId: Int, labelId: Int) = viewModelScope.launch {
        labelDao.insertNoteLabelCrossRef(NoteLabelCrossRef(noteId, labelId))
    }
    fun removeLabelFromNote(noteId: Int, labelId: Int) = viewModelScope.launch {
        labelDao.deleteNoteLabelCrossRef(NoteLabelCrossRef(noteId, labelId))
    }
    fun getLabelsForNote(noteId: Int): Flow<List<Label>> = labelDao.getLabelsByNoteId(noteId)

    suspend fun addNotebook(name: String): Long {
        val notebook = Notebook(name = name.trim())
        Log.d(
            "VM_NOTEBOOK",
            "Inside addNotebook, about to insert: ${notebook.name}"
        ) // <<< ADD THIS LOG
        val id = notebookDao.insertNotebook(notebook)
        Log.d(
            "VM_NOTEBOOK",
            "Notebook inserted with ID: $id, Name: ${notebook.name}"
        ) // <<< ADD THIS LOG
        return id // This should be the new ID, or -1 if insert failed (depends on DAO)
    }

    // ... (other notebook/subnotebook methods remain same) ...
    suspend fun updateNotebook(notebook: Notebook){ notebookDao.updateNotebook(notebook)}
    suspend fun deleteNotebook(notebook: Notebook){
        noteDao.deleteNotesByNotebookId(notebook.id)
        subNotebookDao.deleteSubNotebooksByNotebookId(notebook.id)
        notebookDao.deleteNotebook(notebook)
        val currentCtx = _currentViewContext.value
        if ((currentCtx is ViewContext.NotebookView && currentCtx.notebook.id == notebook.id) ||
            (currentCtx is ViewContext.SubNotebookView && currentCtx.parentNotebook.id == notebook.id)) {
            showAllNotes()
        }
    }
    suspend fun addSubNotebook(name: String, notebookId: Int): Long{
        if (notebookId == 0) return -1L
        val subNotebook = SubNotebook(name = name.trim(), notebookId = notebookId)
        return subNotebookDao.insertSubNotebook(subNotebook)
    }
    suspend fun updateSubNotebook(subNotebook: SubNotebook){ subNotebookDao.updateSubNotebook(subNotebook)}
    suspend fun deleteSubNotebook(subNotebook: SubNotebook){
        noteDao.deleteNotesBySubNotebookId(subNotebook.id)
        subNotebookDao.deleteSubNotebook(subNotebook)
        val currentCtx = _currentViewContext.value
        if (currentCtx is ViewContext.SubNotebookView && currentCtx.subNotebook.id == subNotebook.id) {
            showNotebookView(currentCtx.parentNotebook)
        }
    }


    fun getNoteById(noteId: Int): Flow<Note?> = noteDao.getNoteById(noteId)
        .catch { e -> Log.e("VM_DEBUG", "Error in getNoteById: ${e.message}"); emit(null) }

    fun getNoteWithLabels(noteId: Int): Flow<NoteWithLabels?> = labelDao.getNoteWithLabels(noteId)
        .catch { e -> Log.e("VM_DEBUG", "Error in getNoteWithLabels: ${e.message}"); emit(null) }


    /**
     * Adds a new note or updates an existing one.
     * Returns the ID of the note (newly generated or existing).
     * Returns -1L on failure.
     */
    /**
     * Adds a new note or updates an existing one.
     * Returns the ID of the note (newly generated or existing).
     * Returns -1L on failure.
     */
    suspend fun addOrUpdateNoteAndGetId(
        title: String,
        content: String,
        existingNoteId: Int?,
        imageUri: String?,
        backgroundColor: Long? = null // Added parameter
    ): Long {
        val currentContext = _currentViewContext.value
        var notebookIdForSave: Int? = null
        var subNotebookIdForSave: Int? = null
        var noteIdToReturn: Long = existingNoteId?.toLong() ?: -1L

        when (currentContext) {
            is ViewContext.AllNotes, is ViewContext.LabelView -> {
                if (existingNoteId == null || existingNoteId == -1) {
                    val firstNotebook = _allNotebooks.value.firstOrNull()
                    notebookIdForSave = if (firstNotebook == null) {
                        val defaultNbId = addNotebook("Default")
                        if (defaultNbId != -1L) defaultNbId.toInt() else null
                    } else {
                        firstNotebook.id
                    }
                    if (notebookIdForSave == null) {
                        Log.e("VM_NOTE_SAVE", "Failed to assign a notebook for the new note.")
                        return -1L // Indicate failure
                    }
                }
            }
            is ViewContext.NotebookView -> notebookIdForSave = currentContext.notebook.id
            is ViewContext.SubNotebookView -> {
                notebookIdForSave = currentContext.parentNotebook.id
                subNotebookIdForSave = currentContext.subNotebook.id
            }
        }

        return try {
            if (existingNoteId != null && existingNoteId != -1) {
                val existingNote = noteDao.getNoteById(existingNoteId).firstOrNull()
                if (existingNote != null) {
                    val noteToSave = existingNote.copy(
                        title = title.trim(),
                        content = content.trim(),
                        imageUriString = imageUri,
                        backgroundColor = backgroundColor // Update backgroundColor
                    )
                    noteDao.updateNote(noteToSave)
                    Log.d("VM_NOTE_SAVE", "Note updated with ID $existingNoteId, BackgroundColor: $backgroundColor")
                    existingNoteId.toLong() // Return existing ID
                } else {
                    Log.e("VM_NOTE_SAVE", "Attempted to update non-existent note ID: $existingNoteId")
                    -1L // Indicate failure
                }
            } else {
                if (notebookIdForSave == null) {
                    Log.e("VM_NOTE_SAVE", "Critical: notebookIdForSave is null for a new note.")
                    return -1L // Indicate failure
                }
                val newNote = Note(
                    notebookId = notebookIdForSave,
                    subNotebookId = subNotebookIdForSave,
                    title = title.trim(),
                    content = content.trim(),
                    imageUriString = imageUri,
                    backgroundColor = backgroundColor // Set backgroundColor
                )
                val newNoteGeneratedId = noteDao.insertNote(newNote)
                Log.d("VM_NOTE_SAVE", "New note added with ID $newNoteGeneratedId, BackgroundColor: $backgroundColor")

                if (currentContext is ViewContext.LabelView && newNoteGeneratedId > 0) {
                    addLabelToNote(newNoteGeneratedId.toInt(), currentContext.label.id)
                }
                newNoteGeneratedId // Return new ID
            }
        } catch (e: Exception) {
            Log.e("VM_NOTE_SAVE", "Error in addOrUpdateNoteAndGetId: ${e.message}", e)
            -1L // Indicate failure
        }
    }

    fun addOrUpdateNote(
        title: String,
        content: String,
        existingNoteId: Int?,
        imageUri: String?,
        backgroundColor: Long? = null // Added parameter
    ) {
        viewModelScope.launch {
            addOrUpdateNoteAndGetId(title, content, existingNoteId, imageUri, backgroundColor)
        }
    }

    // Function to specifically update/remove an image for a note
    fun updateNoteImage(noteId: Int, imageUri: String?) {
        if (noteId == -1) return // Cannot update image for a note not yet saved
        viewModelScope.launch {
            val note = noteDao.getNoteById(noteId).firstOrNull() // Blocking, use with care or make suspend
            note?.let {
                val updatedNote = it.copy(imageUriString = imageUri)
                noteDao.updateNote(updatedNote)
                Log.d("VM_IMAGE_UPDATE", "Updated image for note $noteId to $imageUri")
            }
        }
    }


    fun deleteNoteById(noteId: Int) = viewModelScope.launch { /* ... same ... */ }
}