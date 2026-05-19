package com.aprovecha.app.domain

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.Reservation
import com.aprovecha.app.domain.model.ReservationStatus
import com.aprovecha.app.domain.repository.ReservationRepository
import com.aprovecha.app.domain.usecase.reservation.MarkReservationWithdrawnUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Tests unitarios para [MarkReservationWithdrawnUseCase].
 *
 * // @REQ-F05: El encargado del local debe poder marcar una reserva como "retirada".
 */
class MarkReservationWithdrawnUseCaseTest {

    private val reservationRepository: ReservationRepository = mockk()
    private lateinit var markWithdrawnUseCase: MarkReservationWithdrawnUseCase

    @Before
    fun setUp() {
        markWithdrawnUseCase = MarkReservationWithdrawnUseCase(reservationRepository)
    }

    // ─── REQ-F05: Marcar como retirada exitosamente ───────────────────────────

    /**
     * // @REQ-F05: El comercio marca exitosamente una reserva RESERVED como WITHDRAWN
     */
    @Test
    fun givenReservedReservation_whenCommerceMarksWithdrawn_thenStatusIsWithdrawn() = runTest {
        // Given
        val withdrawnReservation = Reservation(
            id = 1L, packId = 1L, userId = 42L,
            status = ReservationStatus.WITHDRAWN,
            fechaReserva = LocalDateTime.now()
        )
        coEvery { reservationRepository.markAsWithdrawn(1L) } returns Result.Success(withdrawnReservation)

        // When
        val result = markWithdrawnUseCase(reservationId = 1L)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(ReservationStatus.WITHDRAWN, (result as Result.Success).data.status)
    }

    /**
     * // @REQ-F05: No se puede marcar como retirada una reserva CANCELLED
     */
    @Test
    fun givenCancelledReservation_whenCommerceMarksWithdrawn_thenReturnsError() = runTest {
        // Given
        val error = IllegalStateException("No se puede marcar como retirada una reserva cancelada")
        coEvery { reservationRepository.markAsWithdrawn(1L) } returns Result.Error(error)

        // When
        val result = markWithdrawnUseCase(reservationId = 1L)

        // Then
        assertTrue(result is Result.Error)
    }
}
