// E:\Kotlin2\Project4\app\src\main\java\com\example\project4\data\Label.kt
package com.example.project7.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "labels")
data class Label(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val timestamp: Long = System.currentTimeMillis()
)