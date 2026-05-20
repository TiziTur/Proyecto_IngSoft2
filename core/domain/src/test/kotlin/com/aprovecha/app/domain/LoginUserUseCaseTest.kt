package com.aprovecha.app.domain

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.User
import com.aprovecha.app.domain.model.UserRole
import com.aprovecha.app.domain.repository.AuthRepository
import com.aprovecha.app.domain.usecase.auth.LoginUserUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para [LoginUserUseCase].
 *
 * // @REQ-F01: Login de usuario registrado con email y contraseña.
 *
 * Convención: given[Contexto]_when[Acción]_then[Resultado]
 */
class LoginUserUseCaseTest {

    private val authRepository: AuthRepository = mockk()
    private lateinit var loginUserUseCase: LoginUserUseCase

    @Before
    fun setUp() {
        loginUserUseCase = LoginUserUseCase(authRepository)
    }

    // ─── REQ-F01: Login exitoso ────────────────────────────────────────────────

    /**
     * // @REQ-F01: Login exitoso con credenciales válidas de un CONSUMER
     */
    @Test
    fun givenValidConsumerCredentials_whenLogin_thenReturnsSuccessWithUser() = runTest {
        // Given
        val expectedUser = User(id = 1L, email = "user@test.com", name = "Juan", role = UserRole.CONSUMER)
        coEvery { authRepository.login("user@test.com", "password123") } returns Result.Success(expectedUser)

        // When
        val result = loginUserUseCase(email = "user@test.com", password = "password123")

        // Then
        assertTrue(result is Result.Success)
        assertEquals(expectedUser, (result as Result.Success).data)
        coVerify(exactly = 1) { authRepository.login("user@test.com", "password123") }
    }

    /**
     * // @REQ-F01: Login exitoso con credenciales válidas de un COMMERCE
     */
    @Test
    fun givenValidCommerceCredentials_whenLogin_thenReturnsSuccessWithCommerceRole() = runTest {
        // Given
        val expectedUser = User(id = 2L, email = "tienda@test.com", name = "Mi Tienda", role = UserRole.COMMERCE)
        coEvery { authRepository.login("tienda@test.com", "pass123") } returns Result.Success(expectedUser)

        // When
        val result = loginUserUseCase(email = "tienda@test.com", password = "pass123")

        // Then
        assertTrue(result is Result.Success)
        assertEquals(UserRole.COMMERCE, (result as Result.Success).data.role)
    }

    // ─── REQ-F01: Validaciones de input ───────────────────────────────────────

    /**
     * // @REQ-F01: No debe ejecutar login con email vacío
     */
    @Test
    fun givenBlankEmail_whenLogin_thenReturnsErrorWithoutCallingRepository() = runTest {
        // When
        val result = loginUserUseCase(email = "", password = "password123")

        // Then
        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    /**
     * // @REQ-F01: No debe ejecutar login con contraseña vacía
     */
    @Test
    fun givenBlankPassword_whenLogin_thenReturnsErrorWithoutCallingRepository() = runTest {
        // When
        val result = loginUserUseCase(email = "user@test.com", password = "")

        // Then
        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    /**
     * // @REQ-F01: No debe ejecutar login si ambos campos están vacíos
     */
    @Test
    fun givenBothFieldsBlank_whenLogin_thenReturnsError() = runTest {
        // When
        val result = loginUserUseCase(email = "   ", password = "   ")

        // Then
        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    // ─── REQ-F01: Propagación de errores del repositorio ──────────────────────

    /**
     * // @REQ-F01: Propaga error si el email no existe en el repositorio
     */
    @Test
    fun givenUnknownEmail_whenLogin_thenPropagatesRepositoryError() = runTest {
        // Given
        val error = IllegalArgumentException("Usuario no encontrado")
        coEvery { authRepository.login(any(), any()) } returns Result.Error(error)

        // When
        val result = loginUserUseCase(email = "noexiste@test.com", password = "password")

        // Then
        assertTrue(result is Result.Error)
        assertEquals("Usuario no encontrado", (result as Result.Error).exception.message)
    }

    /**
     * // @REQ-F01: Propaga error si la contraseña es incorrecta
     */
    @Test
    fun givenWrongPassword_whenLogin_thenPropagatesRepositoryError() = runTest {
        // Given
        val error = IllegalArgumentException("Contraseña incorrecta")
        coEvery { authRepository.login(any(), any()) } returns Result.Error(error)

        // When
        val result = loginUserUseCase(email = "user@test.com", password = "wrongpass")

        // Then
        assertTrue(result is Result.Error)
    }

    /**
     * // @REQ-F01: El email se recorta (trim) antes de enviar al repositorio
     */
    @Test
    fun givenEmailWithSpaces_whenLogin_thenEmailIsTrimmedBeforeRepository() = runTest {
        // Given
        val expectedUser = User(id = 1L, email = "user@test.com", name = "Juan", role = UserRole.CONSUMER)
        coEvery { authRepository.login("user@test.com", any()) } returns Result.Success(expectedUser)

        // When
        val result = loginUserUseCase(email = "  user@test.com  ", password = "password123")

        // Then
        assertTrue(result is Result.Success)
        coVerify { authRepository.login("user@test.com", "password123") }
    }
}
