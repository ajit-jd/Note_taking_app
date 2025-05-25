// E:\Kotlin2\Project7\app\src\main\java\com\example\project7\db\SubNotebookDao.kt
package com.example.project7.db

import androidx.room.*
import com.example.project7.data.SubNotebook
import kotlinx.coroutines.flow.Flow

@Dao
interface SubNotebookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubNotebook(subNotebook: SubNotebook): Long

    @Update
    suspend fun updateSubNotebook(subNotebook: SubNotebook)

    @Delete
    suspend fun deleteSubNotebook(subNotebook: SubNotebook)

    @Query("SELECT * FROM sub_notebooks WHERE notebookId = :notebookId ORDER BY name ASC")
    fun getSubNotebooksForNotebook(notebookId: Int): Flow<List<SubNotebook>>

    @Query("SELECT * FROM sub_notebooks WHERE id = :id")
    fun getSubNotebookById(id: Int): Flow<SubNotebook?>

    @Query("DELETE FROM sub_notebooks WHERE notebookId = :notebookId")
    suspend fun deleteSubNotebooksByNotebookId(notebookId: Int) // For when a notebook is deleted
}