package com.example.project7.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.project7.data.Label
import com.example.project7.data.Note
import com.example.project7.data.NoteLabelCrossRef
import com.example.project7.data.Notebook
import com.example.project7.data.SubNotebook

@Database(
    entities = [
        Note::class,
        Notebook::class,
        SubNotebook::class,
        Label::class,
        NoteLabelCrossRef::class
    ],
    version = 5, // Incremented from 4 to 5
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun notebookDao(): NotebookDao
    abstract fun subNotebookDao(): SubNotebookDao
    abstract fun labelDao(): LabelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // MIGRATION from version 3 to 4: Add imageUriString column to notes table
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN imageUriString TEXT")
            }
        }

        // MIGRATION from version 4 to 5: Add backgroundColor column to notes table
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN backgroundColor INTEGER")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "note_database"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration() // Add temporarily
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}