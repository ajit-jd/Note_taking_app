// E:\Kotlin2\Project4\app\src\main\java\com\example\project4\data\SubNotebook.kt
package com.example.project7.data // Ensure this matches your project's package

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sub_notebooks",
    foreignKeys = [ForeignKey(
        entity = Notebook::class,
        parentColumns = ["id"],
        childColumns = ["notebookId"],
        onDelete = ForeignKey.CASCADE // If a Notebook is deleted, its SubNotebooks are also deleted
    )],
    indices = [Index(value = ["notebookId"])] // Index for faster queries on notebookId
)
data class SubNotebook(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val notebookId: Int, // Foreign key to Notebook
    val name: String,
    val timestamp: Long = System.currentTimeMillis()
)