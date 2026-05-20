package com.aprovecha.app.domain

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.Reservation
import com.aprovecha.app.domain.model.ReservationStatus
import com.aprovecha.app.domain.repository.ReservationRepository
import com.aprovecha.app.domain.usecase.reservation.ReservePackUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

/**
 * Tests de concurrencia para la gestión de reservas.
 *
 * // @REQ-NF01: El sistema debe garantizar la disponibilidad y consistencia de
 * //             las reservas, evitando que dos usuarios reserven el mismo pack
 * //             de alimentos simultáneamente.
 *
 * Estos tests simulan múltiples coroutines accediendo concurrentemente al mismo
 * pack, verificando que solo una reserva sea exitosa.
 */
class ReservationConcurrencyTest {

    private val reservationRepository: ReservationRepository = mockk()
    private lateinit var reservePackUseCase: ReservePackUseCase

    @Before
    fun setUp() {
        reservePackUseCase = ReservePackUseCase(reservationRepository)
    }

    // ─── REQ-NF01: Exclusividad bajo concurrencia ─────────────────────────────

    /**
     * // @REQ-NF01: Solo un usuario puede reservar el pack cuando múltiples
     * //             coroutines intentan hacerlo simultáneamente.
     *
     * Este test simula 10 usuarios intentando reservar el mismo pack al mismo
     * tiempo. Solo 1 debe tener éxito; los otros 9 deben recibir error.
     */
    @Test
    fun givenSingleAvailablePack_whenMultipleUsersReserveConcurrently_thenOnlyOneSucceeds() = runTest {
        // Given — contador atómico: el primer llamado tiene éxito, los demás fallan
        val successCount = AtomicInteger(0)
        val packId = 1L
        val concurrentUsers = 10

        coEvery { reservationRepository.createReservation(packId, any()) } coAnswers {
            // Simula la atomicidad de la BD: solo el primero en llegar tiene éxito
            if (successCount.compareAndSet(0, 1)) {
                Result.Success(
                    Reservation(
                        id = 1L, packId = packId, userId = arg(1),
                        status = ReservationStatus.RESERVED,
                        fechaReserva = LocalDateTime.now()
                    )
                )
            } else {
                Result.Error(IllegalStateException("Pack ya reservado (REQ-NF01)"))
            }
        }

        // When — 10 usuarios intentan reservar concurrentemente
        val results = withContext(Dispatchers.Default) {
            (1..concurrentUsers).map { userId ->
                async { reservePackUseCase(packId = packId, userId = userId.toLong()) }
            }.awaitAll()
        }

        // Then
        val successResults = results.filterIsInstance<Result.Success<Reservation>>()
        val errorResults = results.filterIsInstance<Result.Error>()

        // Solo 1 debe haber tenido éxito
        assertEquals("REQ-NF01: Solo 1 reserva exitosa esperada", 1, successResults.size)
        // Los otros 9 deben haber fallado
        assertEquals("REQ-NF01: 9 usuarios deben recibir error", concurrentUsers - 1, errorResults.size)
    }

    /**
     * // @REQ-NF01: La reserva exitosa tiene el estado correcto
     */
    @Test
    fun givenAvailablePack_whenReserved_thenReservationHasCorrectStatus() = runTest {
        // Given
        coEvery { reservationRepository.createReservation(1L, 1L) } returns Result.Success(
            Reservation(
                id = 1L,
                packId = 1L,
                userId = 1L,
                status = ReservationStatus.RESERVED,
                fechaReserva = LocalDateTime.now()
            )
        )

        // When
        val result = reservePackUseCase(packId = 1L, userId = 1L)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(ReservationStatus.RESERVED, (result as Result.Success).data.status)
    }
}
