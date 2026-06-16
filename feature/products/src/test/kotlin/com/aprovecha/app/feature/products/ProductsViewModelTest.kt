package com.aprovecha.app.feature.products

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.FoodPack
import com.aprovecha.app.domain.model.Reservation
import com.aprovecha.app.domain.model.ReservationStatus
import com.aprovecha.app.domain.model.User
import com.aprovecha.app.domain.model.UserRole
import com.aprovecha.app.domain.repository.AuthRepository
import com.aprovecha.app.domain.repository.FavoriteRepository
import com.aprovecha.app.domain.repository.PackRepository
import com.aprovecha.app.domain.repository.ReservationRepository
import com.aprovecha.app.feature.products.ui.PacksUiState
import com.aprovecha.app.feature.products.ui.ProductsViewModel
import com.aprovecha.app.feature.products.ui.ReserveUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
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

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var packRepository: PackRepository
    private lateinit var reservationRepository: ReservationRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var favoriteRepository: FavoriteRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        packRepository = mockk()
        reservationRepository = mockk()
        authRepository = mockk()
        favoriteRepository = mockk()
        every { packRepository.getAvailablePacksNearby(any(), any(), any()) } returns flowOf(emptyList())
        coEvery { authRepository.getCurrentUser() } returns null
        every { favoriteRepository.getFavoritePackIds(any()) } returns flowOf(emptySet())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildPack(id: Long = 1L) = FoodPack(
        id = id, commerceId = 1L, name = "Pack Test",
        description = "Descripción", originalPrice = 500.0, discountPrice = 200.0, quantity = 3
    )

    private fun buildReservation() = Reservation(
        id = 1L, packId = 1L, userId = 1L,
        status = ReservationStatus.RESERVED, fechaReserva = LocalDateTime.now()
    )

    private fun buildUser(id: Long = 1L) = User(
        id = id, email = "user@test.com", name = "Test User", role = UserRole.CONSUMER
    )

    private fun createViewModel() = ProductsViewModel(
        packRepository, reservationRepository, authRepository, favoriteRepository
    )

    @Test
    fun `Given available packs When ViewModel initializes Then packsState emits Success`() = runTest {
        val packs = listOf(buildPack(1L), buildPack(2L))
        every { packRepository.getAvailablePacksNearby(any(), any(), any()) } returns flowOf(packs)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.packsState.value
        assertTrue(state is PacksUiState.Success)
        assertEquals(2, (state as PacksUiState.Success).packs.size)
    }

    @Test
    fun `Given repository throws When loading packs Then packsState emits Error`() = runTest {
        every {
            packRepository.getAvailablePacksNearby(any(), any(), any())
        } returns flow { throw RuntimeException("Network error") }

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.packsState.value is PacksUiState.Error)
    }

    @Test
    fun `Given valid packId When loadPackDetail called Then selectedPack is set`() = runTest {
        val pack = buildPack(42L)
        coEvery { packRepository.getPackById(42L) } returns Result.Success(pack)

        val viewModel = createViewModel()
        viewModel.loadPackDetail(42L)
        advanceUntilIdle()

        assertEquals(pack, viewModel.selectedPack.value)
    }

    @Test
    fun `Given invalid packId When loadPackDetail fails Then selectedPack remains null`() = runTest {
        coEvery { packRepository.getPackById(any()) } returns Result.Error(NoSuchElementException())

        val viewModel = createViewModel()
        viewModel.loadPackDetail(999L)
        advanceUntilIdle()

        assertNull(viewModel.selectedPack.value)
    }

    @Test
    fun `Given successful reservation When reservePack called Then reserveState emits Success`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns buildUser(1L)
        every { favoriteRepository.getFavoritePackIds(1L) } returns flowOf(emptySet())
        coEvery { reservationRepository.createReservation(1L, 1L) } returns Result.Success(buildReservation())

        val viewModel = createViewModel()
        viewModel.reservePack(1L)
        advanceUntilIdle()

        assertTrue(viewModel.reserveState.value is ReserveUiState.Success)
    }

    @Test
    fun `Given pack unavailable When reservePack fails Then reserveState emits Error`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns buildUser(1L)
        every { favoriteRepository.getFavoritePackIds(1L) } returns flowOf(emptySet())
        coEvery {
            reservationRepository.createReservation(any(), any())
        } returns Result.Error(IllegalStateException("Pack ya reservado (REQ-NF01)"))

        val viewModel = createViewModel()
        viewModel.reservePack(1L)
        advanceUntilIdle()

        val state = viewModel.reserveState.value
        assertTrue(state is ReserveUiState.Error)
        assertTrue((state as ReserveUiState.Error).message.contains("Pack ya reservado"))
    }

    @Test
    fun `Given no session When reservePack called Then reserveState emits Error`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null

        val viewModel = createViewModel()
        viewModel.reservePack(1L)
        advanceUntilIdle()

        val state = viewModel.reserveState.value
        assertTrue(state is ReserveUiState.Error)
        assertTrue((state as ReserveUiState.Error).message.contains("Sesión"))
    }

    @Test
    fun `Given Success state When resetReserveState called Then state returns to Idle`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns buildUser(1L)
        every { favoriteRepository.getFavoritePackIds(1L) } returns flowOf(emptySet())
        coEvery { reservationRepository.createReservation(any(), any()) } returns Result.Success(buildReservation())

        val viewModel = createViewModel()
        viewModel.reservePack(1L)
        advanceUntilIdle()
        viewModel.resetReserveState()

        assertTrue(viewModel.reserveState.value is ReserveUiState.Idle)
    }

    @Test
    fun `Given active session When toggleFavorite called Then favoriteRepository toggleFavorite is invoked`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns buildUser(1L)
        every { favoriteRepository.getFavoritePackIds(1L) } returns flowOf(emptySet())
        coEvery { favoriteRepository.toggleFavorite(1L, 5L) } returns Unit

        val viewModel = createViewModel()
        viewModel.toggleFavorite(5L)
        advanceUntilIdle()

        coVerify(exactly = 1) { favoriteRepository.toggleFavorite(1L, 5L) }
    }

    @Test
    fun `Given no session When toggleFavorite called Then favoriteRepository is not invoked`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null

        val viewModel = createViewModel()
        viewModel.toggleFavorite(5L)
        advanceUntilIdle()

        coVerify(exactly = 0) { favoriteRepository.toggleFavorite(any(), any()) }
    }
}
