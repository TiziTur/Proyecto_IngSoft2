package com.aprovecha.app.data.repository

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.data.local.dao.PackDao
import com.aprovecha.app.data.local.dao.ReservationDao
import com.aprovecha.app.data.local.entity.ReservationEntity
import com.aprovecha.app.domain.model.ReservationStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Tests unitarios para [ReservationRepositoryImpl].
 *
 * // @REQ-F04: Creación de reservas (atómica con PackDao)
 * // @REQ-F05: Marcado como WITHDRAWN
 * // @REQ-F06: Cancelación de reserva
 * // @REQ-NF01: Exclusividad mediante reservePackAtomically()
 */
class ReservationRepositoryImplTest {

    private lateinit var reservationDao: ReservationDao
    private lateinit var packDao: PackDao
    private lateinit var repository: ReservationRepositoryImpl

    private val now = LocalDateTime.now().toString()

    @Before
    fun setUp() {
        reservationDao = mockk()
        packDao = mockk()
        repository = ReservationRepositoryImpl(reservationDao, packDao)
    }

    private fun buildReservationEntity(
        id: Long = 1L,
        packId: Long = 1L,
        userId: Long = 42L,
        status: String = "RESERVED"
    ) = ReservationEntity(
        id = id,
        packId = packId,
        userId = userId,
        status = status,
        fechaReserva = now,
        fechaActualizacion = now
    )

    // ── createReservation (REQ-F04 + REQ-NF01) ───────────────────────────────

    /**
     * Given: pack disponible (reservePackAtomically retorna 1)
     * When: createReservation llamado
     * Then: Result.Success con reserva en estado RESERVED
     */
    @Test
    fun `Given available pack When createReservation called Then returns Success with RESERVED status`() = runTest {
        coEvery { packDao.reservePackAtomically(1L) } returns 1
        coEvery { reservationDao.insertReservation(any()) } returns 1L

        val result = repository.createReservation(packId = 1L, userId = 42L)

        assertTrue(result is Result.Success)
        val reservation = (result as Result.Success).data
        assertEquals(ReservationStatus.RESERVED, reservation.status)
        assertEquals(1L, reservation.packId)
        assertEquals(42L, reservation.userId)
    }

    /**
     * Given: pack ya reservado (reservePackAtomically retorna 0)
     * When: createReservation llamado
     * Then: Result.Error (REQ-NF01 — exclusividad)
     */
    @Test
    fun `Given pack already reserved When createReservation called Then returns Error`() = runTest {
        coEvery { packDao.reservePackAtomically(1L) } returns 0

        val result = repository.createReservation(packId = 1L, userId = 99L)

        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error.message!!.contains("REQ-NF01"))
    }

    /**
     * Given: DAO lanza excepción
     * When: createReservation falla
     * Then: Result.Error con la excepción
     */
    @Test
    fun `Given DAO throws When createReservation called Then returns Result Error`() = runTest {
        coEvery { packDao.reservePackAtomically(any()) } throws RuntimeException("DB error")

        val result = repository.createReservation(packId = 1L, userId = 1L)

        assertTrue(result is Result.Error)
    }

    /**
     * Given: reserva exitosa
     * When: se verifica llamada al DAO
     * Then: reservePackAtomically e insertReservation son llamados una vez cada uno
     */
    @Test
    fun `Given successful reservation When verifying DAO calls Then both DAOs called once`() = runTest {
        coEvery { packDao.reservePackAtomically(1L) } returns 1
        coEvery { reservationDao.insertReservation(any()) } returns 1L

        repository.createReservation(packId = 1L, userId = 42L)

        coVerify(exactly = 1) { packDao.reservePackAtomically(1L) }
        coVerify(exactly = 1) { reservationDao.insertReservation(any()) }
    }

    // ── markAsWithdrawn (REQ-F05) ─────────────────────────────────────────────

    /**
     * Given: reserva en estado RESERVED
     * When: markAsWithdrawn llamado
     * Then: Result.Success con estado WITHDRAWN
     */
    @Test
    fun `Given RESERVED reservation When markAsWithdrawn called Then returns Success with WITHDRAWN`() = runTest {
        val entity = buildReservationEntity(status = "RESERVED")
        coEvery { reservationDao.getReservationById(1L) } returns entity
        coEvery { reservationDao.markAsWithdrawn(1L, any()) } returns 1

        val result = repository.markAsWithdrawn(reservationId = 1L)

        assertTrue(result is Result.Success)
        assertEquals(ReservationStatus.WITHDRAWN, (result as Result.Success).data.status)
    }

    /**
     * Given: reserva no existe
     * When: markAsWithdrawn llamado
     * Then: Result.Error con NoSuchElementException
     */
    @Test
    fun `Given nonexistent reservation When markAsWithdrawn called Then returns Error not found`() = runTest {
        coEvery { reservationDao.getReservationById(999L) } returns null

        val result = repository.markAsWithdrawn(reservationId = 999L)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is NoSuchElementException)
    }

    /**
     * Given: reserva en estado CANCELLED (no RESERVED)
     * When: markAsWithdrawn llamado
     * Then: Result.Error — no se puede marcar CANCELLED como retirada
     */
    @Test
    fun `Given CANCELLED reservation When markAsWithdrawn called Then returns Error`() = runTest {
        val entity = buildReservationEntity(status = "CANCELLED")
        coEvery { reservationDao.getReservationById(1L) } returns entity

        val result = repository.markAsWithdrawn(reservationId = 1L)

        assertTrue(result is Result.Error)
        val msg = (result as Result.Error).exception.message ?: ""
        assertTrue(msg.contains("RESERVED"))
    }

    // ── cancelReservation (REQ-F06) ───────────────────────────────────────────

    /**
     * Given: reserva en estado RESERVED
     * When: cancelReservation llamado
     * Then: Result.Success con estado CANCELLED
     */
    @Test
    fun `Given RESERVED reservation When cancelReservation called Then returns Success with CANCELLED`() = runTest {
        val entity = buildReservationEntity(status = "RESERVED")
        coEvery { reservationDao.getReservationById(1L) } returns entity
        coEvery { reservationDao.cancelReservation(1L, any()) } returns 1

        val result = repository.cancelReservation(reservationId = 1L)

        assertTrue(result is Result.Success)
        assertEquals(ReservationStatus.CANCELLED, (result as Result.Success).data.status)
    }

    /**
     * Given: reserva en estado WITHDRAWN
     * When: cancelReservation llamado
     * Then: Result.Error — no se puede cancelar una reserva ya retirada
     */
    @Test
    fun `Given WITHDRAWN reservation When cancelReservation called Then returns Error`() = runTest {
        val entity = buildReservationEntity(status = "WITHDRAWN")
        coEvery { reservationDao.getReservationById(1L) } returns entity

        val result = repository.cancelReservation(reservationId = 1L)

        assertTrue(result is Result.Error)
        val msg = (result as Result.Error).exception.message ?: ""
        assertTrue(msg.contains("cancelar"))
    }

    /**
     * Given: reserva no existe
     * When: cancelReservation llamado
     * Then: Result.Error con NoSuchElementException
     */
    @Test
    fun `Given nonexistent reservation When cancelReservation called Then returns Error not found`() = runTest {
        coEvery { reservationDao.getReservationById(999L) } returns null

        val result = repository.cancelReservation(reservationId = 999L)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is NoSuchElementException)
    }

    // ── getReservationsByUser ─────────────────────────────────────────────────

    /**
     * Given: usuario tiene reservas
     * When: getReservationsByUser llamado
     * Then: Flow emite lista con reservas mapeadas a dominio
     */
    @Test
    fun `Given user has reservations When getReservationsByUser called Then emits mapped list`() = runTest {
        val entities = listOf(
            buildReservationEntity(id = 1L, userId = 42L),
            buildReservationEntity(id = 2L, userId = 42L)
        )
        every { reservationDao.getReservationsByUser(42L) } returns flowOf(entities)

        val result = repository.getReservationsByUser(42L).first()

        assertEquals(2, result.size)
        assertTrue(result.all { it.userId == 42L })
        assertTrue(result.all { it.status == ReservationStatus.RESERVED })
    }

    // ── getReservationById ────────────────────────────────────────────────────

    /**
     * Given: reserva existe
     * When: getReservationById llamado
     * Then: Result.Success con la reserva correcta
     */
    @Test
    fun `Given existing reservation When getReservationById called Then returns Success`() = runTest {
        val entity = buildReservationEntity(id = 5L)
        coEvery { reservationDao.getReservationById(5L) } returns entity

        val result = repository.getReservationById(5L)

        assertTrue(result is Result.Success)
        assertEquals(5L, (result as Result.Success).data.id)
    }

    /**
     * Given: reserva no existe
     * When: getReservationById llamado
     * Then: Result.Error con NoSuchElementException
     */
    @Test
    fun `Given nonexistent reservation When getReservationById called Then returns Error`() = runTest {
        coEvery { reservationDao.getReservationById(999L) } returns null

        val result = repository.getReservationById(999L)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is NoSuchElementException)
    }
}
