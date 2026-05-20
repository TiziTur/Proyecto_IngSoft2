package com.aprovecha.app.domain

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.User
import com.aprovecha.app.domain.model.UserRole
import com.aprovecha.app.domain.repository.AuthRepository
import com.aprovecha.app.domain.usecase.auth.RegisterUserUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para [RegisterUserUseCase].
 *
 * // @REQ-F01: El comercio debe poder registrarse en la plataforma.
 *
 * Convención: given[Contexto]_when[Acción]_then[Resultado]
 * Herramienta: JUnit4 + MockK (equivalente pytest en Python)
 */
class RegisterUserUseCaseTest {

    // Mocks — equivalente a los fixtures/mocks de pytest
    private val authRepository: AuthRepository = mockk()
    private lateinit var registerUserUseCase: RegisterUserUseCase

    @Before
    fun setUp() {
        registerUserUseCase = RegisterUserUseCase(authRepository)
    }

    // ─── REQ-F01: Registro exitoso de comercio ────────────────────────────────

    /**
     * // @REQ-F01: Registro exitoso de un comercio con datos válidos
     */
    @Test
    fun givenValidCommerceData_whenRegister_thenReturnsSuccessWithUser() = runTest {
        // Given
        val expectedUser = User(id = 1L, email = "comercio@test.com", name = "Panadería Test", role = UserRole.COMMERCE)
        coEvery { authRepository.register(any(), any(), any(), any()) } returns Result.Success(expectedUser)

        // When
        val result = registerUserUseCase(
            email = "comercio@test.com",
            password = "password123",
            name = "Panadería Test",
            role = UserRole.COMMERCE
        )

        // Then
        assertTrue(result is Result.Success)
        assertEquals(expectedUser, (result as Result.Success).data)
        coVerify(exactly = 1) {
            authRepository.register("comercio@test.com", "password123", "Panadería Test", UserRole.COMMERCE)
        }
    }

    /**
     * // @REQ-F01: Registro exitoso de un consumidor con datos válidos
     */
    @Test
    fun givenValidConsumerData_whenRegister_thenReturnsSuccessWithUser() = runTest {
        // Given
        val expectedUser = User(id = 2L, email = "user@test.com", name = "Juan Pérez", role = UserRole.CONSUMER)
        coEvery { authRepository.register(any(), any(), any(), any()) } returns Result.Success(expectedUser)

        // When
        val result = registerUserUseCase(
            email = "user@test.com",
            password = "password123",
            name = "Juan Pérez",
            role = UserRole.CONSUMER
        )

        // Then
        assertTrue(result is Result.Success)
    }

    // ─── REQ-F01: Validaciones de input ───────────────────────────────────────

    /**
     * // @REQ-F01: No debe permitir registro con email vacío
     */
    @Test
    fun givenEmptyEmail_whenRegister_thenReturnsError() = runTest {
        // Given — email vacío
        // When
        val result = registerUserUseCase(
            email = "",
            password = "password123",
            name = "Test",
            role = UserRole.CONSUMER
        )

        // Then
        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { authRepository.register(any(), any(), any(), any()) }
    }

    /**
     * // @REQ-F01: No debe permitir contraseña menor a 6 caracteres
     */
    @Test
    fun givenPasswordTooShort_whenRegister_thenReturnsError() = runTest {
        // Given — contraseña de 3 caracteres (< mínimo de 6)
        // When
        val result = registerUserUseCase(
            email = "test@test.com",
            password = "abc",
            name = "Test",
            role = UserRole.CONSUMER
        )

        // Then
        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { authRepository.register(any(), any(), any(), any()) }
    }

    /**
     * // @REQ-F01: No debe permitir registro con nombre vacío
     */
    @Test
    fun givenEmptyNombre_whenRegister_thenReturnsError() = runTest {
        // When
        val result = registerUserUseCase(
            email = "test@test.com",
            password = "password123",
            name = "   ",
            role = UserRole.CONSUMER
        )

        // Then
        assertTrue(result is Result.Error)
    }

    /**
     * // @REQ-F01: Debe propagar error del repositorio (ej: email duplicado)
     */
    @Test
    fun givenDuplicateEmail_whenRegister_thenPropagatesRepositoryError() = runTest {
        // Given — el repo lanza error de email duplicado
        val error = IllegalStateException("El email ya está registrado")
        coEvery { authRepository.register(any(), any(), any(), any()) } returns Result.Error(error)

        // When
        val result = registerUserUseCase("dup@test.com", "password123", "Test", UserRole.CONSUMER)

        // Then
        assertTrue(result is Result.Error)
    }
}
