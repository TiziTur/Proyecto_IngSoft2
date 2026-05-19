package com.aprovecha.app.domain

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.Reservation
import com.aprovecha.app.domain.model.ReservationStatus
import com.aprovecha.app.domain.repository.ReservationRepository
import com.aprovecha.app.domain.usecase.reservation.ReservePackUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Tests unitarios para [ReservePackUseCase].
 *
 * // @REQ-F04: El usuario debe poder reservar un pack de alimentos para ser retirado.
 * // @REQ-NF01: El sistema debe garantizar que dos usuarios no reserven el mismo pack simultáneamente.
 *
 * El test de concurrencia (REQ-NF01) se encuentra en ReservationConcurrencyTest.kt
 */
class ReservePackUseCaseTest {

    private val reservationRepository: ReservationRepository = mockk()
    private lateinit var reservePackUseCase: ReservePackUseCase

    @Before
    fun setUp() {
        reservePackUseCase = ReservePackUseCase(reservationRepository)
    }

    private fun buildReservation(packId: Long = 1L, userId: Long = 1L) = Reservation(
        id = 1L,
        packId = packId,
        userId = userId,
        status = ReservationStatus.RESERVED,
        fechaReserva = LocalDateTime.now()
    )

    // ─── REQ-F04: Reserva exitosa ──────────────────────────────────────────────

    /**
     * // @REQ-F04: Reserva exitosa cuando el pack está disponible
     */
    @Test
    fun givenAvailablePack_whenUserReserves_thenReservationIsCreated() = runTest {
        // Given
        val expectedReservation = buildReservation(packId = 1L, userId = 42L)
        coEvery { reservationRepository.createReservation(1L, 42L) } returns Result.Success(expectedReservation)

        // When
        val result = reservePackUseCase(packId = 1L, userId = 42L)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(ReservationStatus.RESERVED, (result as Result.Success).data.status)
        assertEquals(1L, result.data.packId)
        assertEquals(42L, result.data.userId)
    }

    // ─── REQ-NF01: Concurrencia — pack ya reservado ───────────────────────────

    /**
     * // @REQ-NF01: Si el pack fue reservado por otro usuario (race condition),
     * //             el segundo intento debe retornar error
     */
    @Test
    fun givenAlreadyReservedPack_whenSecondUserTries_thenReturnsError() = runTest {
        // Given — el repositorio retorna error porque el pack ya fue reservado
        val concurrencyError = IllegalStateException("El pack ya fue reservado por otro usuario")
        coEvery { reservationRepository.createReservation(1L, any()) } returns Result.Error(concurrencyError)

        // When — segundo usuario intenta reservar el mismo pack
        val result = reservePackUseCase(packId = 1L, userId = 99L)

        // Then
        assertTrue(result is Result.Error)
        coVerify(exactly = 1) { reservationRepository.createReservation(1L, 99L) }
    }
}
