package com.aprovecha.app.data.repository

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.data.local.dao.UserDao
import com.aprovecha.app.data.local.datastore.SessionManager
import com.aprovecha.app.data.local.entity.UserEntity
import com.aprovecha.app.domain.model.User
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
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        userDao = mockk()
        sessionManager = mockk(relaxed = true)
        repository = AuthRepositoryImpl(userDao, sessionManager)
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

    /**
     * Given: registro exitoso
     * When: se completa el registro
     * Then: sessionManager guarda la sesion del nuevo usuario
     */
    @Test
    fun `Given successful register When called Then sessionManager saveSession invoked with new user`() = runTest {
        coEvery { userDao.getUserByEmail(any()) } returns null
        coEvery { userDao.insertUser(any()) } returns 5L

        val result = repository.register("nuevo@test.com", "pass123", "Nuevo Usuario", UserRole.CONSUMER)

        val user = (result as Result.Success).data
        coVerify(exactly = 1) { sessionManager.saveSession(user) }
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
     * Given: login exitoso
     * When: se completa el login
     * Then: sessionManager guarda la sesion del usuario autenticado
     */
    @Test
    fun `Given successful login When called Then sessionManager saveSession invoked with user`() = runTest {
        val entity = buildUserEntity(password = "pass123")
        coEvery { userDao.getUserByEmail(entity.email) } returns entity

        val result = repository.login(entity.email, "pass123")

        val user = (result as Result.Success).data
        coVerify(exactly = 1) { sessionManager.saveSession(user) }
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

    // ── logout / getCurrentUser ─────────────────────────────────────────────

    /**
     * Given: hay una sesion guardada
     * When: se llama getCurrentUser
     * Then: devuelve el usuario que retorna sessionManager
     */
    @Test
    fun `Given saved session When getCurrentUser called Then returns sessionManager user`() = runTest {
        val user = User(id = 3L, email = "session@test.com", name = "Session User", role = UserRole.COMMERCE)
        coEvery { sessionManager.getCurrentUser() } returns user

        val result = repository.getCurrentUser()

        assertEquals(user, result)
    }

    /**
     * Given: no hay sesion guardada
     * When: se llama getCurrentUser
     * Then: devuelve null
     */
    @Test
    fun `Given no saved session When getCurrentUser called Then returns null`() = runTest {
        coEvery { sessionManager.getCurrentUser() } returns null

        assertNull(repository.getCurrentUser())
    }

    /**
     * Given: hay una sesion activa
     * When: se llama logout
     * Then: sessionManager limpia la sesion
     */
    @Test
    fun `Given active session When logout called Then sessionManager clearSession invoked`() = runTest {
        repository.logout()

        coVerify(exactly = 1) { sessionManager.clearSession() }
    }
}
