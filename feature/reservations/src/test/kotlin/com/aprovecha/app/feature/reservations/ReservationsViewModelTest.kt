package com.aprovecha.app.feature.reservations

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.FoodPack
import com.aprovecha.app.domain.model.Reservation
import com.aprovecha.app.domain.model.ReservationStatus
import com.aprovecha.app.domain.repository.PackRepository
import com.aprovecha.app.domain.repository.ReservationRepository
import com.aprovecha.app.domain.usecase.pack.PublishPackUseCase
import com.aprovecha.app.feature.reservations.ui.ActionState
import com.aprovecha.app.feature.reservations.ui.ReservationsUiState
import com.aprovecha.app.feature.reservations.ui.ReservationsViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

// @REQ-F02, @REQ-F05, @REQ-F06

@OptIn(ExperimentalCoroutinesApi::class)
class ReservationsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var reservationRepository: ReservationRepository
    private lateinit var packRepository: PackRepository
    private lateinit var publishPackUseCase: PublishPackUseCase
    private lateinit var viewModel: ReservationsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        reservationRepository = mockk()
        packRepository = mockk()
        publishPackUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildReservation(id: Long = 1L, status: ReservationStatus = ReservationStatus.RESERVED) =
        Reservation(
            id = id,
            packId = 1L,
            userId = 1L,
            status = status,
            fechaReserva = LocalDateTime.now()
        )

    private fun buildPack(id: Long = 1L) = FoodPack(
        id = id,
        commerceId = 1L,
        name = "Pack Tarde",
        description = "Pan del día",
        originalPrice = 500.0,
        discountPrice = 200.0,
        quantity = 3
    )

    private fun createViewModel(): ReservationsViewModel {
        every { reservationRepository.getReservationsByUser(any()) } returns flowOf(emptyList())
        every { packRepository.getPacksByCommerce(any()) } returns flowOf(emptyList())
        return ReservationsViewModel(reservationRepository, packRepository, publishPackUseCase)
    }

    // ── loadUserReservations ─────────────────────────────────────────────────

    /**
     * Given: usuario tiene 2 reservas
     * When: se llama loadUserReservations
     * Then: reservationsState emite Success con las 2 reservas
     */
    @Test
    fun `Given user has reservations When loadUserReservations called Then state emits Success`() = runTest {
        val reservations = listOf(buildReservation(1L), buildReservation(2L))
        every { reservationRepository.getReservationsByUser(1L) } returns flowOf(reservations)
        every { packRepository.getPacksByCommerce(any()) } returns flowOf(emptyList())

        viewModel = ReservationsViewModel(reservationRepository, packRepository, publishPackUseCase)
        viewModel.loadUserReservations(1L)
        advanceUntilIdle()

        val state = viewModel.reservationsState.value
        assertTrue(state is ReservationsUiState.Success)
        assertEquals(2, (state as ReservationsUiState.Success).reservations.size)
    }

    /**
     * Given: repositorio lanza excepción
     * When: loadUserReservations falla
     * Then: reservationsState emite Error
     */
    @Test
    fun `Given repository throws When loadUserReservations fails Then state emits Error`() = runTest {
        every {
            reservationRepository.getReservationsByUser(any())
        } returns kotlinx.coroutines.flow.flow { throw RuntimeException("DB error") }
        every { packRepository.getPacksByCommerce(any()) } returns flowOf(emptyList())

        viewModel = ReservationsViewModel(reservationRepository, packRepository, publishPackUseCase)
        viewModel.loadUserReservations(1L)
        advanceUntilIdle()

        val state = viewModel.reservationsState.value
        assertTrue(state is ReservationsUiState.Error)
    }

    // ── cancelReservation ────────────────────────────────────────────────────

    /**
     * Given: reserva RESERVED existe
     * When: se cancela la reserva
     * Then: actionState emite Success
     */
    @Test
    fun `Given RESERVED reservation When cancelReservation called Then actionState emits Success`() = runTest {
        coEvery { reservationRepository.cancelReservation(1L) } returns
            Result.Success(buildReservation(1L, ReservationStatus.CANCELLED))

        viewModel = createViewModel()
        viewModel.cancelReservation(1L)
        advanceUntilIdle()

        assertTrue(viewModel.actionState.value is ActionState.Success)
    }

    /**
     * Given: reserva ya WITHDRAWN
     * When: se intenta cancelar
     * Then: actionState emite Error
     */
    @Test
    fun `Given WITHDRAWN reservation When cancelReservation called Then actionState emits Error`() = runTest {
        coEvery {
            reservationRepository.cancelReservation(1L)
        } returns Result.Error(IllegalStateException("No se puede cancelar una reserva ya retirada"))

        viewModel = createViewModel()
        viewModel.cancelReservation(1L)
        advanceUntilIdle()

        val state = viewModel.actionState.value
        assertTrue(state is ActionState.Error)
        assertTrue((state as ActionState.Error).message.contains("cancelar"))
    }

    // ── markAsWithdrawn ──────────────────────────────────────────────────────

    /**
     * Given: reserva RESERVED
     * When: se marca como retirada
     * Then: actionState emite Success
     */
    @Test
    fun `Given RESERVED reservation When markAsWithdrawn called Then actionState emits Success`() = runTest {
        coEvery { reservationRepository.markAsWithdrawn(1L) } returns
            Result.Success(buildReservation(1L, ReservationStatus.WITHDRAWN))

        viewModel = createViewModel()
        viewModel.markAsWithdrawn(1L)
        advanceUntilIdle()

        assertTrue(viewModel.actionState.value is ActionState.Success)
    }

    /**
     * Given: reserva no existe
     * When: se intenta marcar como retirada
     * Then: actionState emite Error
     */
    @Test
    fun `Given nonexistent reservation When markAsWithdrawn called Then actionState emits Error`() = runTest {
        coEvery {
            reservationRepository.markAsWithdrawn(999L)
        } returns Result.Error(NoSuchElementException("Reserva no encontrada"))

        viewModel = createViewModel()
        viewModel.markAsWithdrawn(999L)
        advanceUntilIdle()

        val state = viewModel.actionState.value
        assertTrue(state is ActionState.Error)
        assertTrue((state as ActionState.Error).message.contains("encontrada"))
    }

    // ── publishPack ──────────────────────────────────────────────────────────

    /**
     * Given: pack válido
     * When: se publica el pack
     * Then: actionState emite Success
     */
    @Test
    fun `Given valid pack When publishPack called Then actionState emits Success`() = runTest {
        val pack = buildPack()
        coEvery { publishPackUseCase(pack) } returns Result.Success(pack)

        viewModel = createViewModel()
        viewModel.publishPack(pack)
        advanceUntilIdle()

        assertTrue(viewModel.actionState.value is ActionState.Success)
    }

    /**
     * Given: pack con precio inválido
     * When: publishPack falla validación
     * Then: actionState emite Error con mensaje de validación
     */
    @Test
    fun `Given invalid pack When publishPack fails validation Then actionState emits Error`() = runTest {
        val invalidPack = buildPack().copy(discountPrice = 600.0) // mayor que originalPrice
        coEvery { publishPackUseCase(invalidPack) } returns
            Result.Error(IllegalArgumentException("El precio de descuento debe ser menor al precio original"))

        viewModel = createViewModel()
        viewModel.publishPack(invalidPack)
        advanceUntilIdle()

        val state = viewModel.actionState.value
        assertTrue(state is ActionState.Error)
        assertTrue((state as ActionState.Error).message.contains("descuento"))
    }

    /**
     * Given: actionState es Success
     * When: se llama resetAction
     * Then: actionState vuelve a Idle
     */
    @Test
    fun `Given Success actionState When resetAction called Then returns to Idle`() = runTest {
        coEvery { reservationRepository.cancelReservation(any()) } returns
            Result.Success(buildReservation())

        viewModel = createViewModel()
        viewModel.cancelReservation(1L)
        advanceUntilIdle()

        viewModel.resetAction()
        assertTrue(viewModel.actionState.value is ActionState.Idle)
    }
}
