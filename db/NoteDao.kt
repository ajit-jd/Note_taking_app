// E:\Kotlin2\Project7\app\src\main\java\com\example\project7\db\NoteDao.kt
package com.example.project7.db

import androidx.room.*
import com.example.project7.data.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Changed from just @Insert
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note): Int

    @Delete
    suspend fun deleteNote(note: Note): Int

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int): Int

    // Existing methods
    @Query("SELECT * FROM notes ORDER BY timestamp DESC") // Keep this for "All Notes"
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    fun getNoteById(noteId: Int): Flow<Note?>

    // New methods for context-based note fetching
    @Query("SELECT * FROM notes WHERE notebookId = :notebookId ORDER BY timestamp DESC")
    fun getNotesByNotebookId(notebookId: Int): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE subNotebookId = :subNotebookId ORDER BY timestamp DESC")
    fun getNotesBySubNotebookId(subNotebookId: Int): Flow<List<Note>>

    // If you need notes directly under a notebook (not in any of its sub-notebooks)
    @Query("SELECT * FROM notes WHERE notebookId = :notebookId AND subNotebookId IS NULL ORDER BY timestamp DESC")
    fun getNotesDirectlyInNotebook(notebookId: Int): Flow<List<Note>>

    @Query("DELETE FROM notes WHERE notebookId = :notebookId")
    suspend fun deleteNotesByNotebookId(notebookId: Int) // For when a notebook is deleted

    @Query("DELETE FROM notes WHERE subNotebookId = :subNotebookId")
    suspend fun deleteNotesBySubNotebookId(subNotebookId: Int) // For when a sub-notebook is deleted
}