package com.aprovecha.app.data.repository

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.data.local.dao.UserDao
import com.aprovecha.app.data.local.entity.UserEntity
import com.aprovecha.app.domain.model.UserRole
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

// @REQ-F01: Tests de AuthRepositoryImpl

class AuthRepositoryImplTest {

    private lateinit var userDao: UserDao
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        userDao = mockk()
        repository = AuthRepositoryImpl(userDao)
    }

    private fun buildUserEntity(
        id: Long = 1L,
        email: String = "test@test.com",
        password: String = "pass123",
        name: String = "Test User",
        role: String = "CONSUMER"
    ) = UserEntity(
        id = id,
        email = email,
        passwordHash = password.hashCode().toString(),
        nombre = name,
        role = role
    )

    // ── register ─────────────────────────────────────────────────────────────

    /**
     * Given: email no existe en BD
     * When: se registra nuevo usuario CONSUMER
     * Then: Result.Success con user correcto
     */
    @Test
    fun `Given new email When register consumer Then returns Success with user`() = runTest {
        val email = "nuevo@test.com"
        coEvery { userDao.getUserByEmail(email) } returns null
        coEvery { userDao.insertUser(any()) } returns 5L

        val result = repository.register(email, "pass123", "Nuevo Usuario", UserRole.CONSUMER)

        assertTrue(result is Result.Success)
        val user = (result as Result.Success).data
        assertEquals(email, user.email)
        assertEquals("Nuevo Usuario", user.name)
        assertEquals(UserRole.CONSUMER, user.role)
        assertEquals(5L, user.id)
    }

    /**
     * Given: email no existe en BD
     * When: se registra nuevo comercio
     * Then: Result.Success con rol COMMERCE
     */
    @Test
    fun `Given new email When register commerce Then returns Success with COMMERCE role`() = runTest {
        coEvery { userDao.getUserByEmail(any()) } returns null
        coEvery { userDao.insertUser(any()) } returns 10L

        val result = repository.register("comercio@test.com", "pass", "Mi Local", UserRole.COMMERCE)

        assertTrue(result is Result.Success)
        assertEquals(UserRole.COMMERCE, (result as Result.Success).data.role)
    }

    /**
     * Given: email ya registrado
     * When: se intenta registrar con el mismo email
     * Then: Result.Error con mensaje de email duplicado
     */
    @Test
    fun `Given existing email When register called Then returns Error with duplicate message`() = runTest {
        val existingUser = buildUserEntity(email = "existente@test.com")
        coEvery { userDao.getUserByEmail("existente@test.com") } returns existingUser

        val result = repository.register("existente@test.com", "pass", "User", UserRole.CONSUMER)

        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error.message!!.contains("registrado"))
    }

    /**
     * Given: DAO lanza excepción
     * When: register falla por error de DB
     * Then: Result.Error con la excepción
     */
    @Test
    fun `Given DAO throws When register called Then returns Result Error`() = runTest {
        coEvery { userDao.getUserByEmail(any()) } returns null
        coEvery { userDao.insertUser(any()) } throws RuntimeException("DB constraint violation")

        val result = repository.register("test@test.com", "pass", "User", UserRole.CONSUMER)

        assertTrue(result is Result.Error)
    }

    // ── login ─────────────────────────────────────────────────────────────────

    /**
     * Given: usuario registrado con credenciales correctas
     * When: login con email y password correctos
     * Then: Result.Success con datos del usuario
     */
    @Test
    fun `Given valid credentials When login called Then returns Success with user`() = runTest {
        val password = "pass123"
        val entity = buildUserEntity(password = password)
        coEvery { userDao.getUserByEmail(entity.email) } returns entity

        val result = repository.login(entity.email, password)

        assertTrue(result is Result.Success)
        val user = (result as Result.Success).data
        assertEquals(entity.email, user.email)
        assertEquals(UserRole.CONSUMER, user.role)
    }

    /**
     * Given: email no registrado
     * When: login con email inexistente
     * Then: Result.Error con mensaje "no encontrado"
     */
    @Test
    fun `Given unknown email When login called Then returns Error user not found`() = runTest {
        coEvery { userDao.getUserByEmail(any()) } returns null

        val result = repository.login("noexiste@test.com", "pass")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception.message!!.contains("encontrado"))
    }

    /**
     * Given: email correcto pero password incorrecto
     * When: login con password equivocado
     * Then: Result.Error con mensaje de contraseña incorrecta
     */
    @Test
    fun `Given wrong password When login called Then returns Error wrong password`() = runTest {
        val entity = buildUserEntity(password = "correctPass")
        coEvery { userDao.getUserByEmail(entity.email) } returns entity

        val result = repository.login(entity.email, "wrongPass")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception.message!!.contains("Contraseña"))
    }

    /**
     * Given: registro exitoso
     * When: se verifica llamada al DAO
     * Then: insertUser fue llamado exactamente una vez
     */
    @Test
    fun `Given successful register When insert called Then DAO insertUser called once`() = runTest {
        coEvery { userDao.getUserByEmail(any()) } returns null
        coEvery { userDao.insertUser(any()) } returns 1L

        repository.register("test@test.com", "pass", "User", UserRole.CONSUMER)

        coVerify(exactly = 1) { userDao.insertUser(any()) }
    }
}
