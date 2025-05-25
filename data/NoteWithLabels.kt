// E:\Kotlin2\Project4\app\src\main\java\com\example\project4\data\NoteWithLabels.kt
package com.example.project7.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class NoteWithLabels(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "id", // From Note entity (refers to Note.id)
        entityColumn = "id", // From Label entity (refers to Label.id)
        associateBy = Junction(
            value = NoteLabelCrossRef::class,
            parentColumn = "noteId",    // Column in NoteLabelCrossRef linking to Note
            entityColumn = "labelId"    // Column in NoteLabelCrossRef linking to Label
        )
    )
    val labels: List<Label>
)