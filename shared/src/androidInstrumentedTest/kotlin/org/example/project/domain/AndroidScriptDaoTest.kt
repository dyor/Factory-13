package org.example.project.domain

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class AndroidScriptDaoTest : ScriptDaoTest() {
    override fun getInMemoryDatabase(): AppDatabase {
        val builder = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
        return getRoomDatabase(builder)
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