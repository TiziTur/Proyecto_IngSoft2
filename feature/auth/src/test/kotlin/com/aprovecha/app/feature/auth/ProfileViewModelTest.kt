package com.aprovecha.app.feature.auth

import com.aprovecha.app.domain.model.User
import com.aprovecha.app.domain.model.UserRole
import com.aprovecha.app.domain.repository.AuthRepository
import com.aprovecha.app.feature.auth.ui.ProfileViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildUser() = User(id = 1L, email = "user@test.com", name = "Test User", role = UserRole.CONSUMER)

    @Test
    fun `Given active session When ViewModel initializes Then user is loaded`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns buildUser()
        val viewModel = ProfileViewModel(authRepository)
        advanceUntilIdle()

        assertNotNull(viewModel.user.value)
        assertEquals("user@test.com", viewModel.user.value?.email)
    }

    @Test
    fun `Given no session When ViewModel initializes Then user is null`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null
        val viewModel = ProfileViewModel(authRepository)
        advanceUntilIdle()

        assertNull(viewModel.user.value)
    }

    @Test
    fun `Given active session When logout called Then repository logout invoked and user is null`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns buildUser()
        coEvery { authRepository.logout() } returns Unit
        val viewModel = ProfileViewModel(authRepository)
        advanceUntilIdle()

        var completed = false
        viewModel.logout { completed = true }
        advanceUntilIdle()

        coVerify(exactly = 1) { authRepository.logout() }
        assertNull(viewModel.user.value)
        assertTrue(completed)
    }
}
