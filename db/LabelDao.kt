// E:\Kotlin2\Project7\app\src\main\java\com\example\project7\db\AppDatabase.kt
package com.example.project7.db

import androidx.room.*
import com.example.project7.data.Label
import com.example.project7.data.LabelWithNotes
import com.example.project7.data.NoteLabelCrossRef
import com.example.project7.data.NoteWithLabels
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {
    // Label CRUD
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLabel(label: Label): Long

    @Update
    suspend fun updateLabel(label: Label)

    @Delete
    suspend fun deleteLabel(label: Label) // Cascade will handle NoteLabelCrossRef

    @Query("SELECT * FROM labels ORDER BY name ASC")
    fun getAllLabels(): Flow<List<Label>>

    @Query("SELECT * FROM labels WHERE id = :labelId")
    fun getLabelById(labelId: Int): Flow<Label?>

    // Note-Label Relationship Management
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteLabelCrossRef(crossRef: NoteLabelCrossRef)

    @Delete
    suspend fun deleteNoteLabelCrossRef(crossRef: NoteLabelCrossRef)

    // Remove all labels for a specific note (useful when updating note's labels)
    @Query("DELETE FROM note_label_cross_ref WHERE noteId = :noteId")
    suspend fun clearLabelsForNote(noteId: Int)

    // Get Note with its Labels
    @Transaction
    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun getNoteWithLabels(noteId: Int): Flow<NoteWithLabels?>

    // Get Label with its Notes
    @Transaction
    @Query("SELECT * FROM labels WHERE id = :labelId")
    fun getLabelWithNotes(labelId: Int): Flow<LabelWithNotes?>

    // Get notes for a specific label (alternative to getLabelWithNotes().notes)
    @Query("""
        SELECT notes.* FROM notes
        INNER JOIN note_label_cross_ref ON notes.id = note_label_cross_ref.noteId
        WHERE note_label_cross_ref.labelId = :labelId
        ORDER BY notes.timestamp DESC
    """)
    fun getNotesByLabelId(labelId: Int): Flow<List<com.example.project7.data.Note>>

    // Get labels for a specific note (alternative to getNoteWithLabels().labels)
    @Query("""
        SELECT labels.* FROM labels
        INNER JOIN note_label_cross_ref ON labels.id = note_label_cross_ref.labelId
        WHERE note_label_cross_ref.noteId = :noteId
        ORDER BY labels.name ASC
    """)
    fun getLabelsByNoteId(noteId: Int): Flow<List<Label>>
}