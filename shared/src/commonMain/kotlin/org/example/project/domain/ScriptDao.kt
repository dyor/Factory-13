package org.example.project.domain

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ScriptDao {
    @Query("SELECT * FROM script")
    fun getAllScripts(): Flow<List<Script>>

    @Query("SELECT * FROM script WHERE id = :id")
    suspend fun getScriptById(id: Long): Script?

    @Query("SELECT * FROM script WHERE isActive = 1 LIMIT 1")
    fun getActiveScript(): Flow<Script?>

    @Insert
    suspend fun insert(script: Script)

    @Update
    suspend fun update(script: Script)

    @Query("UPDATE script SET isActive = 0")
    suspend fun clearActiveScript()

    @Transaction
    suspend fun setActiveScript(id: Long) {
        clearActiveScript()
        setActiveScriptById(id)
    }

    @Query("UPDATE script SET isActive = 1 WHERE id = :id")
    suspend fun setActiveScriptById(id: Long)
}