
// E:\Kotlin2\Project4\app\src\main\java\com\example\project4\data\Note.kt
package com.example.project7.data // Ensure this matches your project's package

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Notebook::class,
            parentColumns = ["id"],
            childColumns = ["notebookId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SubNotebook::class,
            parentColumns = ["id"],
            childColumns = ["subNotebookId"], // This field will be nullable
            onDelete = ForeignKey.CASCADE // If a SubNotebook is deleted, its Notes are also deleted
        )
    ],
    indices = [Index(value = ["notebookId"]), Index(value = ["subNotebookId"])]
)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val notebookId: Int,      // Every note must belong to a Notebook
    val subNotebookId: Int?,  // Nullable: if null, note is directly in Notebook; else, in SubNotebook
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUriString: String? = null, // <<< NEW: To store the URI of one image
    val backgroundColor: Long? // New column
)