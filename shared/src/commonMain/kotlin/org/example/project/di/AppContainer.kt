package org.example.project.di

import androidx.room.RoomDatabase
import org.example.project.domain.AppDatabase
import org.example.project.domain.getRoomDatabase
import org.example.project.domain.ScriptDao
import org.example.project.domain.gemini.GeminiClient

object AppContainer {
    private var _database: AppDatabase? = null
    val database: AppDatabase
        get() = _database ?: throw IllegalStateException("Database not initialized. Call AppContainer.init() from your platform entry point.")

    val scriptDao: ScriptDao
        get() = database.scriptDao()

    val geminiClient: GeminiClient by lazy { GeminiClient() }

    fun init(builder: RoomDatabase.Builder<AppDatabase>) {
        if (_database == null) {
            _database = getRoomDatabase(builder)
        }
    }
}