package com.aprovecha.app.data.repository

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.data.local.dao.PackDao
import com.aprovecha.app.data.local.entity.PackEntity
import com.aprovecha.app.domain.model.FoodPack
import com.aprovecha.app.domain.model.PackStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

// @REQ-F02, @REQ-F03: Tests de PackRepositoryImpl

class PackRepositoryImplTest {

    private lateinit var packDao: PackDao
    private lateinit var repository: PackRepositoryImpl

    @Before
    fun setUp() {
        packDao = mockk()
        repository = PackRepositoryImpl(packDao)
    }

    private val now = LocalDateTime.now().toString()

    private fun buildPackEntity(id: Long = 1L, status: String = "AVAILABLE") = PackEntity(
        id = id,
        commerceId = 1L,
        nombre = "Pack Panadería",
        descripcion = "Pan del día",
        precioOriginal = 500.0,
        precioDescuento = 200.0,
        cantidad = 3,
        status = status,
        fechaPublicacion = now
    )

    private fun buildFoodPack(id: Long = 0L) = FoodPack(
        id = id,
        commerceId = 1L,
        name = "Pack Panadería",
        description = "Pan del día",
        originalPrice = 500.0,
        discountPrice = 200.0,
        quantity = 3,
        expirationTime = LocalDateTime.parse(now)
    )

    // ── publishPack ───────────────────────────────────────────────────────────

    /**
     * Given: pack válido
     * When: publishPack llamado
     * Then: Result.Success con id asignado por DAO
     */
    @Test
    fun `Given valid pack When publishPack called Then returns Success with assigned id`() = runTest {
        val pack = buildFoodPack()
        coEvery { packDao.insertPack(any()) } returns 99L

        val result = repository.publishPack(pack)

        assertTrue(result is Result.Success)
        assertEquals(99L, (result as Result.Success).data.id)
    }

    /**
     * Given: DAO lanza excepción
     * When: publishPack falla
     * Then: Result.Error
     */
    @Test
    fun `Given DAO throws When publishPack called Then returns Result Error`() = runTest {
        coEvery { packDao.insertPack(any()) } throws RuntimeException("DB error")

        val result = repository.publishPack(buildFoodPack())

        assertTrue(result is Result.Error)
    }

    /**
     * Given: pack insertado correctamente
     * When: publishPack verifica DAO
     * Then: insertPack fue llamado exactamente una vez
     */
    @Test
    fun `Given valid pack When publishPack Then DAO insertPack called once`() = runTest {
        coEvery { packDao.insertPack(any()) } returns 1L

        repository.publishPack(buildFoodPack())

        coVerify(exactly = 1) { packDao.insertPack(any()) }
    }

    // ── getAvailablePacksNearby ───────────────────────────────────────────────

    /**
     * Given: hay 3 packs disponibles
     * When: getAvailablePacksNearby llamado
     * Then: Flow emite lista con los 3 packs mapeados a dominio
     */
    @Test
    fun `Given available packs When getAvailablePacksNearby Then emits mapped domain list`() = runTest {
        val entities = listOf(buildPackEntity(1L), buildPackEntity(2L), buildPackEntity(3L))
        every { packDao.getAvailablePacks() } returns flowOf(entities)

        val result = repository.getAvailablePacksNearby(-31.4, -64.18, 5.0).first()

        assertEquals(3, result.size)
        assertEquals("Pack Panadería", result[0].name)
        assertEquals(PackStatus.AVAILABLE, result[0].status)
    }

    /**
     * Given: no hay packs disponibles
     * When: getAvailablePacksNearby llamado
     * Then: Flow emite lista vacía
     */
    @Test
    fun `Given no available packs When getAvailablePacksNearby Then emits empty list`() = runTest {
        every { packDao.getAvailablePacks() } returns flowOf(emptyList())

        val result = repository.getAvailablePacksNearby(-31.4, -64.18, 5.0).first()

        assertTrue(result.isEmpty())
    }

    // ── getPackById ───────────────────────────────────────────────────────────

    /**
     * Given: pack existe con id válido
     * When: getPackById llamado
     * Then: Result.Success con el pack correcto
     */
    @Test
    fun `Given existing pack When getPackById called Then returns Success`() = runTest {
        coEvery { packDao.getPackById(1L) } returns buildPackEntity(1L)

        val result = repository.getPackById(1L)

        assertTrue(result is Result.Success)
        assertEquals(1L, (result as Result.Success).data.id)
        assertEquals("Pack Panadería", result.data.name)
    }

    /**
     * Given: pack no existe
     * When: getPackById con id inválido
     * Then: Result.Error con NoSuchElementException
     */
    @Test
    fun `Given nonexistent pack When getPackById called Then returns Error`() = runTest {
        coEvery { packDao.getPackById(999L) } returns null

        val result = repository.getPackById(999L)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is NoSuchElementException)
    }

    // ── deletePack ────────────────────────────────────────────────────────────

    /**
     * Given: pack AVAILABLE existe
     * When: deletePack llamado
     * Then: Result.Success(Unit)
     */
    @Test
    fun `Given AVAILABLE pack When deletePack called Then returns Success`() = runTest {
        coEvery { packDao.deletePack(1L) } returns 1

        val result = repository.deletePack(1L)

        assertTrue(result is Result.Success)
    }

    /**
     * Given: pack no está AVAILABLE (ya reservado)
     * When: deletePack llamado
     * Then: Result.Error por no poder eliminar
     */
    @Test
    fun `Given RESERVED pack When deletePack called Then returns Error`() = runTest {
        coEvery { packDao.deletePack(any()) } returns 0

        val result = repository.deletePack(1L)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception.message!!.contains("disponible"))
    }

    // ── getPacksByCommerce ────────────────────────────────────────────────────

    /**
     * Given: comercio tiene 2 packs
     * When: getPacksByCommerce llamado
     * Then: Flow emite los 2 packs del comercio
     */
    @Test
    fun `Given commerce has packs When getPacksByCommerce Then emits commerce packs`() = runTest {
        val entities = listOf(buildPackEntity(1L), buildPackEntity(2L))
        every { packDao.getPacksByCommerce(1L) } returns flowOf(entities)

        val result = repository.getPacksByCommerce(1L).first()

        assertEquals(2, result.size)
        assertTrue(result.all { it.commerceId == 1L })
    }
}
