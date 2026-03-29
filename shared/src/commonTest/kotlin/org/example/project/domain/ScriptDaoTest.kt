package org.example.project.domain

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

abstract class ScriptDaoTest {
    abstract fun getInMemoryDatabase(): AppDatabase

    private lateinit var database: AppDatabase
    private lateinit var dao: ScriptDao

    @BeforeTest
    fun setup() {
        database = getInMemoryDatabase()
        dao = database.scriptDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    open fun testInsertAndGetScript() = runTest {
        val script = Script(title = "Test Script", content = "This is a test script.")
        dao.insert(script)

        // Note: With Room KMP, we don't always get the generated ID back from insert directly yet.
        // We'll just fetch all and verify it's there.
        val scripts = dao.getAllScripts().first()
        assertTrue(scripts.isNotEmpty(), "Scripts list should not be empty")
        
        val insertedScript = scripts.first()
        assertEquals("Test Script", insertedScript.title)
        assertEquals("This is a test script.", insertedScript.content)
        assertEquals(false, insertedScript.isActive)
    }

    @Test
    open fun testActiveScriptLogic() = runTest {
        val script1 = Script(title = "Script 1", content = "Content 1")
        val script2 = Script(title = "Script 2", content = "Content 2", isActive = true)
        
        dao.insert(script1)
        dao.insert(script2)

        val activeScript = dao.getActiveScript().first()
        assertNotNull(activeScript, "Active script should not be null")
        assertEquals("Script 2", activeScript.title)

        dao.clearActiveScript()
        
        val activeAfterClear = dao.getActiveScript().first()
        assertEquals(null, activeAfterClear, "Active script should be null after clearing")
    }
}