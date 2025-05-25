

// E:\Kotlin2\Project4\app\src\main\java\com\example\project4\data\Notebook.kt
package com.example.project7.data // Ensure this matches your project's package

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notebooks")
data class Notebook(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val timestamp: Long = System.currentTimeMillis()
)