// E:\Kotlin2\Project7\app\src\main\java\com\example\project7\db\NotebookDao.kt
package com.example.project7.db

import androidx.room.*
import com.example.project7.data.Notebook
import kotlinx.coroutines.flow.Flow

@Dao
interface NotebookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotebook(notebook: Notebook): Long

    @Update
    suspend fun updateNotebook(notebook: Notebook)

    @Delete
    suspend fun deleteNotebook(notebook: Notebook)

    @Query("SELECT * FROM notebooks ORDER BY name ASC")
    fun getAllNotebooks(): Flow<List<Notebook>>

    @Query("SELECT * FROM notebooks WHERE id = :id")
    fun getNotebookById(id: Int): Flow<Notebook?>

    // You might add more complex queries later, e.g., to get notebooks with a count of their notes
}