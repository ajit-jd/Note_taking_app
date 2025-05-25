// E:\Kotlin2\Project4\app\src\main\java\com\example\project4\data\LabelWithNotes.kt
package com.example.project7.data


import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class LabelWithNotes(
    @Embedded val label: Label,
    @Relation(
        parentColumn = "id", // From Label entity (refers to Label.id)
        entityColumn = "id", // From Note entity (refers to Note.id)
        associateBy = Junction(
            value = NoteLabelCrossRef::class,
            parentColumn = "labelId",   // Column in NoteLabelCrossRef linking to Label
            entityColumn = "noteId"     // Column in NoteLabelCrossRef linking to Note
        )
    )
    val notes: List<Note>
)