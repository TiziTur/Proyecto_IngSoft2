package com.aprovecha.app.data.local.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.aprovecha.app.domain.model.User
import com.aprovecha.app.domain.model.UserRole
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

// @REQ-F01: Tests de SessionManager (persistencia de sesion con DataStore)

class SessionManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun buildSessionManager(): SessionManager {
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { File(tempFolder.root, "session_test.preferences_pb") }
        )
        return SessionManager(dataStore)
    }

    @Test
    fun `Given no session When getCurrentUser called Then returns null`() = runTest {
        val sessionManager = buildSessionManager()

        assertNull(sessionManager.getCurrentUser())
    }

    @Test
    fun `Given saved session When getCurrentUser called Then returns same user`() = runTest {
        val sessionManager = buildSessionManager()
        val user = User(id = 7L, email = "user@test.com", name = "Test User", role = UserRole.COMMERCE)

        sessionManager.saveSession(user)

        assertEquals(user, sessionManager.getCurrentUser())
    }

    @Test
    fun `Given saved session When clearSession called Then getCurrentUser returns null`() = runTest {
        val sessionManager = buildSessionManager()
        sessionManager.saveSession(User(id = 1L, email = "a@test.com", name = "A", role = UserRole.CONSUMER))

        sessionManager.clearSession()

        assertNull(sessionManager.getCurrentUser())
    }

    @Test
    fun `Given consumer session saved When new session saved Then getCurrentUser returns new user`() = runTest {
        val sessionManager = buildSessionManager()
        sessionManager.saveSession(User(id = 1L, email = "a@test.com", name = "A", role = UserRole.CONSUMER))

        val newUser = User(id = 2L, email = "b@test.com", name = "B", role = UserRole.COMMERCE)
        sessionManager.saveSession(newUser)

        assertEquals(newUser, sessionManager.getCurrentUser())
    }
}
