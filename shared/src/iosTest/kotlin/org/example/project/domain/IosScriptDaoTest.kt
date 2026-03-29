package org.example.project.domain

import androidx.room.Room
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class IosScriptDaoTest : ScriptDaoTest() {
    override fun getInMemoryDatabase(): AppDatabase {
        val databaseBuilder = Room.inMemoryDatabaseBuilder<AppDatabase>()
        return getRoomDatabase(databaseBuilder)
    }

    @Test
    override fun testInsertAndGetScript() = runTest {
        super.testInsertAndGetScript()
    }
    
    @Test
    override fun testActiveScriptLogic() = runTest {
        super.testActiveScriptLogic()
    }
}