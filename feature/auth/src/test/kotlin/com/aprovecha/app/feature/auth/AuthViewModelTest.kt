package com.aprovecha.app.feature.auth

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.User
import com.aprovecha.app.domain.model.UserRole
import com.aprovecha.app.domain.repository.AuthRepository
import com.aprovecha.app.feature.auth.ui.AuthUiState
import com.aprovecha.app.feature.auth.ui.AuthViewModel
import io.mockk.coEvery
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

/**
 * Tests unitarios para [AuthViewModel].
 *
 * // @REQ-F01: Login y registro de usuarios con roles CONSUMER y COMMERCE.
 *
 * Convención: given[Contexto]_when[Acción]_then[Resultado]
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        viewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildUser(role: UserRole = UserRole.CONSUMER) = User(
        id = 1L,
        email = "user@test.com",
        name = "Test User",
        role = role
    )

    // ─── Login ────────────────────────────────────────────────────────────────

    /**
     * // @REQ-F01: Login exitoso con credenciales válidas emite Success con el rol
     */
    @Test
    fun `Given valid credentials When login called Then uiState emits Success with role`() = runTest {
        coEvery { authRepository.login("user@test.com", "pass123") } returns Result.Success(buildUser())

        viewModel.login("user@test.com", "pass123")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Success)
        assertEquals("CONSUMER", (state as AuthUiState.Success).role)
    }

    /**
     * // @REQ-F01: Login exitoso con rol COMMERCE emite Success con rol COMMERCE
     */
    @Test
    fun `Given commerce credentials When login called Then uiState emits Success with COMMERCE role`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.Success(buildUser(UserRole.COMMERCE))

        viewModel.login("tienda@test.com", "pass123")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Success)
        assertEquals("COMMERCE", (state as AuthUiState.Success).role)
    }

    /**
     * // @REQ-F01: Login con email vacío emite Error sin llamar al repositorio
     */
    @Test
    fun `Given blank email When login called Then uiState emits Error immediately`() = runTest {
        viewModel.login("", "pass123")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
    }

    /**
     * // @REQ-F01: Login con contraseña vacía emite Error sin llamar al repositorio
     */
    @Test
    fun `Given blank password When login called Then uiState emits Error immediately`() = runTest {
        viewModel.login("user@test.com", "")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
    }

    /**
     * // @REQ-F01: Error del repositorio se propaga al uiState
     */
    @Test
    fun `Given invalid credentials When login called Then uiState emits Error with message`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns
            Result.Error(IllegalArgumentException("Usuario no encontrado"))

        viewModel.login("noexiste@test.com", "pass123")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertTrue((state as AuthUiState.Error).message.isNotBlank())
    }

    // ─── Register ─────────────────────────────────────────────────────────────

    /**
     * // @REQ-F01: Registro exitoso de CONSUMER emite Success con rol CONSUMER
     */
    @Test
    fun `Given valid data When register consumer Then uiState emits Success with CONSUMER role`() = runTest {
        coEvery { authRepository.register(any(), any(), any(), any()) } returns Result.Success(buildUser())

        viewModel.register("user@test.com", "pass123", "Juan", UserRole.CONSUMER)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Success)
        assertEquals("CONSUMER", (state as AuthUiState.Success).role)
    }

    /**
     * // @REQ-F01: Registro exitoso de COMMERCE emite Success con rol COMMERCE
     */
    @Test
    fun `Given valid data When register commerce Then uiState emits Success with COMMERCE role`() = runTest {
        coEvery { authRepository.register(any(), any(), any(), any()) } returns
            Result.Success(buildUser(UserRole.COMMERCE))

        viewModel.register("tienda@test.com", "pass123", "Mi Tienda", UserRole.COMMERCE)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Success)
        assertEquals("COMMERCE", (state as AuthUiState.Success).role)
    }

    /**
     * // @REQ-F01: Registro con campos vacíos emite Error inmediatamente
     */
    @Test
    fun `Given blank fields When register called Then uiState emits Error`() = runTest {
        viewModel.register("", "pass123", "Juan", UserRole.CONSUMER)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
    }

    /**
     * // @REQ-F01: Contraseña menor a 6 caracteres emite Error
     */
    @Test
    fun `Given password too short When register called Then uiState emits Error`() = runTest {
        viewModel.register("user@test.com", "abc", "Juan", UserRole.CONSUMER)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertTrue((state as AuthUiState.Error).message.contains("6"))
    }

    /**
     * // @REQ-F01: Error del repositorio en registro se propaga al uiState
     */
    @Test
    fun `Given duplicate email When register called Then uiState emits Error`() = runTest {
        coEvery { authRepository.register(any(), any(), any(), any()) } returns
            Result.Error(IllegalStateException("El email ya está registrado"))

        viewModel.register("existente@test.com", "pass123", "Juan", UserRole.CONSUMER)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
    }

    // ─── resetState ───────────────────────────────────────────────────────────

    /**
     * // @REQ-F01: resetState devuelve el uiState a Idle
     */
    @Test
    fun `Given Success state When resetState called Then uiState returns to Idle`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.Success(buildUser())

        viewModel.login("user@test.com", "pass123")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AuthUiState.Success)

        viewModel.resetState()
        assertTrue(viewModel.uiState.value is AuthUiState.Idle)
    }
}
