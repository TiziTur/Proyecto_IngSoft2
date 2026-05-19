package com.aprovecha.app.domain

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.Reservation
import com.aprovecha.app.domain.model.ReservationStatus
import com.aprovecha.app.domain.repository.ReservationRepository
import com.aprovecha.app.domain.usecase.reservation.CancelReservationUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Tests unitarios para [CancelReservationUseCase].
 *
 * // @REQ-F06: El usuario debe poder cancelar una reserva si ya no la desea.
 */
class CancelReservationUseCaseTest {

    private val reservationRepository: ReservationRepository = mockk()
    private lateinit var cancelReservationUseCase: CancelReservationUseCase

    @Before
    fun setUp() {
        cancelReservationUseCase = CancelReservationUseCase(reservationRepository)
    }

    // ─── REQ-F06: Cancelación exitosa ─────────────────────────────────────────

    /**
     * // @REQ-F06: Cancelación exitosa de una reserva en estado RESERVED
     */
    @Test
    fun givenActiveReservation_whenUserCancels_thenStatusIsCancelled() = runTest {
        // Given
        val cancelledReservation = Reservation(
            id = 1L, packId = 1L, userId = 42L,
            status = ReservationStatus.CANCELLED,
            fechaReserva = LocalDateTime.now()
        )
        coEvery { reservationRepository.cancelReservation(1L) } returns Result.Success(cancelledReservation)

        // When
        val result = cancelReservationUseCase(reservationId = 1L)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(ReservationStatus.CANCELLED, (result as Result.Success).data.status)
    }

    // ─── REQ-F06: No se puede cancelar una reserva ya retirada ───────────────

    /**
     * // @REQ-F06: No se puede cancelar una reserva que ya fue marcada como WITHDRAWN
     */
    @Test
    fun givenWithdrawnReservation_whenUserCancels_thenReturnsError() = runTest {
        // Given — el repositorio rechaza la cancelación porque ya fue retirada
        val error = IllegalStateException("No se puede cancelar una reserva ya retirada")
        coEvery { reservationRepository.cancelReservation(1L) } returns Result.Error(error)

        // When
        val result = cancelReservationUseCase(reservationId = 1L)

        // Then
        assertTrue(result is Result.Error)
    }
}
