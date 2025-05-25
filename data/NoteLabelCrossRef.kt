package com.example.project7.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "note_label_cross_ref",
    primaryKeys = ["noteId", "labelId"],
    foreignKeys = [
        ForeignKey(
            entity = Note::class, // Assuming Note.kt is in com.example.project6.data
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Label::class,
            parentColumns = ["id"],
            childColumns = ["labelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["labelId"]), Index(value = ["noteId"])] // Added index for noteId too
)
data class NoteLabelCrossRef(
    val noteId: Int,
    val labelId: Int
)