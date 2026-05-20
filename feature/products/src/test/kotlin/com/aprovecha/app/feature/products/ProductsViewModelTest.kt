package com.aprovecha.app.feature.products

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.FoodPack
import com.aprovecha.app.domain.model.PackStatus
import com.aprovecha.app.domain.model.Reservation
import com.aprovecha.app.domain.model.ReservationStatus
import com.aprovecha.app.domain.repository.PackRepository
import com.aprovecha.app.domain.repository.ReservationRepository
import com.aprovecha.app.feature.products.ui.PacksUiState
import com.aprovecha.app.feature.products.ui.ProductsViewModel
import com.aprovecha.app.feature.products.ui.ReserveUiState
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

// @REQ-F03, @REQ-F04

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var packRepository: PackRepository
    private lateinit var reservationRepository: ReservationRepository
    private lateinit var viewModel: ProductsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        packRepository = mockk()
        reservationRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildPack(id: Long = 1L) = FoodPack(
        id = id,
        commerceId = 1L,
        name = "Pack Test",
        description = "Descripción",
        originalPrice = 500.0,
        discountPrice = 200.0,
        quantity = 3
    )

    private fun buildReservation(id: Long = 1L) = Reservation(
        id = id,
        packId = 1L,
        userId = 1L,
        status = ReservationStatus.RESERVED,
        fechaReserva = LocalDateTime.now()
    )

    // ── loadNearbyPacks ──────────────────────────────────────────────────────

    /**
     * Given: packRepository retorna lista de packs
     * When: ViewModel se inicializa
     * Then: packsState emite Success con la lista
     */
    @Test
    fun `Given available packs When ViewModel initializes Then packsState emits Success`() = runTest {
        val packs = listOf(buildPack(1L), buildPack(2L))
        every { packRepository.getAvailablePacksNearby(any(), any(), any()) } returns flowOf(packs)
        every { packRepository.getPacksByCommerce(any()) } returns flowOf(emptyList())

        viewModel = ProductsViewModel(packRepository, reservationRepository)
        advanceUntilIdle()

        val state = viewModel.packsState.value
        assertTrue(state is PacksUiState.Success)
        assertEquals(2, (state as PacksUiState.Success).packs.size)
    }

    /**
     * Given: packRepository lanza excepción
     * When: ViewModel intenta cargar packs
     * Then: packsState emite Error
     */
    @Test
    fun `Given repository throws When loading packs Then packsState emits Error`() = runTest {
        every {
            packRepository.getAvailablePacksNearby(any(), any(), any())
        } returns kotlinx.coroutines.flow.flow { throw RuntimeException("Network error") }
        every { packRepository.getPacksByCommerce(any()) } returns flowOf(emptyList())

        viewModel = ProductsViewModel(packRepository, reservationRepository)
        advanceUntilIdle()

        val state = viewModel.packsState.value
        assertTrue(state is PacksUiState.Error)
        assertEquals("Network error", (state as PacksUiState.Error).message)
    }

    // ── loadPackDetail ───────────────────────────────────────────────────────

    /**
     * Given: pack existe en repositorio
     * When: se llama loadPackDetail con packId válido
     * Then: selectedPack emite el pack correcto
     */
    @Test
    fun `Given valid packId When loadPackDetail called Then selectedPack is set`() = runTest {
        val pack = buildPack(42L)
        every { packRepository.getAvailablePacksNearby(any(), any(), any()) } returns flowOf(emptyList())
        coEvery { packRepository.getPackById(42L) } returns Result.Success(pack)

        viewModel = ProductsViewModel(packRepository, reservationRepository)
        viewModel.loadPackDetail(42L)
        advanceUntilIdle()

        assertEquals(pack, viewModel.selectedPack.value)
    }

    /**
     * Given: pack no existe
     * When: loadPackDetail retorna Error
     * Then: selectedPack permanece null
     */
    @Test
    fun `Given invalid packId When loadPackDetail fails Then selectedPack remains null`() = runTest {
        every { packRepository.getAvailablePacksNearby(any(), any(), any()) } returns flowOf(emptyList())
        coEvery { packRepository.getPackById(any()) } returns Result.Error(NoSuchElementException())

        viewModel = ProductsViewModel(packRepository, reservationRepository)
        viewModel.loadPackDetail(999L)
        advanceUntilIdle()

        assertNull(viewModel.selectedPack.value)
    }

    // ── reservePack ──────────────────────────────────────────────────────────

    /**
     * Given: reserva exitosa
     * When: se llama reservePack
     * Then: reserveState emite Success
     */
    @Test
    fun `Given successful reservation When reservePack called Then reserveState emits Success`() = runTest {
        every { packRepository.getAvailablePacksNearby(any(), any(), any()) } returns flowOf(emptyList())
        coEvery { reservationRepository.createReservation(1L, 1L) } returns Result.Success(buildReservation())

        viewModel = ProductsViewModel(packRepository, reservationRepository)
        viewModel.reservePack(1L, 1L)
        advanceUntilIdle()

        assertTrue(viewModel.reserveState.value is ReserveUiState.Success)
    }

    /**
     * Given: pack agotado
     * When: se llama reservePack y falla
     * Then: reserveState emite Error con mensaje
     */
    @Test
    fun `Given pack unavailable When reservePack fails Then reserveState emits Error`() = runTest {
        every { packRepository.getAvailablePacksNearby(any(), any(), any()) } returns flowOf(emptyList())
        coEvery {
            reservationRepository.createReservation(any(), any())
        } returns Result.Error(IllegalStateException("Pack ya reservado (REQ-NF01)"))

        viewModel = ProductsViewModel(packRepository, reservationRepository)
        viewModel.reservePack(1L, 1L)
        advanceUntilIdle()

        val state = viewModel.reserveState.value
        assertTrue(state is ReserveUiState.Error)
        assertTrue((state as ReserveUiState.Error).message.contains("Pack ya reservado"))
    }

    /**
     * Given: reserveState es Success
     * When: se llama resetReserveState
     * Then: reserveState vuelve a Idle
     */
    @Test
    fun `Given Success state When resetReserveState called Then state returns to Idle`() = runTest {
        every { packRepository.getAvailablePacksNearby(any(), any(), any()) } returns flowOf(emptyList())
        coEvery { reservationRepository.createReservation(any(), any()) } returns Result.Success(buildReservation())

        viewModel = ProductsViewModel(packRepository, reservationRepository)
        viewModel.reservePack(1L, 1L)
        advanceUntilIdle()

        viewModel.resetReserveState()

        assertTrue(viewModel.reserveState.value is ReserveUiState.Idle)
    }
}
