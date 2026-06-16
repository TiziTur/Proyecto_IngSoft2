package com.aprovecha.app.feature.products.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.FoodPack
import com.aprovecha.app.domain.repository.AuthRepository
import com.aprovecha.app.domain.repository.FavoriteRepository
import com.aprovecha.app.domain.repository.PackRepository
import com.aprovecha.app.domain.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

// @REQ-F03: Lista de packs disponibles
// @REQ-F04: Reservar un pack

sealed class PacksUiState {
    data object Loading : PacksUiState()
    data class Success(val packs: List<FoodPack>) : PacksUiState()
    data class Error(val message: String) : PacksUiState()
}

sealed class ReserveUiState {
    data object Idle : ReserveUiState()
    data object Loading : ReserveUiState()
    data object Success : ReserveUiState()
    data class Error(val message: String) : ReserveUiState()
}

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val packRepository: PackRepository,
    private val reservationRepository: ReservationRepository,
    private val authRepository: AuthRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private companion object {
        const val DEFAULT_LATITUDE = -31.4
        const val DEFAULT_LONGITUDE = -64.18
        const val DEFAULT_RADIUS_KM = 5.0
        const val SESSION_ERROR_MESSAGE = "Sesión no encontrada. Iniciá sesión nuevamente."
    }

    private val _packsState = MutableStateFlow<PacksUiState>(PacksUiState.Loading)
    val packsState: StateFlow<PacksUiState> = _packsState.asStateFlow()

    private val _selectedPack = MutableStateFlow<FoodPack?>(null)
    val selectedPack: StateFlow<FoodPack?> = _selectedPack.asStateFlow()

    private val _reserveState = MutableStateFlow<ReserveUiState>(ReserveUiState.Idle)
    val reserveState: StateFlow<ReserveUiState> = _reserveState.asStateFlow()

    private val _favoritePackIds = MutableStateFlow<Set<Long>>(emptySet())
    val favoritePackIds: StateFlow<Set<Long>> = _favoritePackIds.asStateFlow()

    init {
        loadNearbyPacks()
        loadFavoriteIds()
    }

    private fun loadFavoriteIds() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.id ?: return@launch
            favoriteRepository.getFavoritePackIds(userId)
                .catch { }
                .collect { ids -> _favoritePackIds.value = ids }
        }
    }

    fun loadNearbyPacks(
        lat: Double = DEFAULT_LATITUDE,
        lng: Double = DEFAULT_LONGITUDE,
        radioKm: Double = DEFAULT_RADIUS_KM
    ) {
        viewModelScope.launch {
            packRepository.getAvailablePacksNearby(lat, lng, radioKm)
                .catch { _packsState.value = PacksUiState.Error(it.message ?: "Error") }
                .collect { packs -> _packsState.value = PacksUiState.Success(packs) }
        }
    }

    fun loadPackDetail(packId: Long) {
        viewModelScope.launch {
            when (val result = packRepository.getPackById(packId)) {
                is Result.Success -> _selectedPack.value = result.data
                is Result.Error -> { }
                else -> {}
            }
        }
    }

    fun reservePack(packId: Long) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.id
            if (userId == null) {
                _reserveState.value = ReserveUiState.Error(SESSION_ERROR_MESSAGE)
                return@launch
            }
            _reserveState.value = ReserveUiState.Loading
            _reserveState.value = when (val result = reservationRepository.createReservation(packId, userId)) {
                is Result.Success -> ReserveUiState.Success
                is Result.Error -> ReserveUiState.Error(result.exception.message ?: "Error al reservar")
                else -> ReserveUiState.Error("Error inesperado")
            }
        }
    }

    fun toggleFavorite(packId: Long) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.id ?: return@launch
            favoriteRepository.toggleFavorite(userId, packId)
        }
    }

    fun resetReserveState() { _reserveState.value = ReserveUiState.Idle }
}
