package com.aprovecha.app.feature.products.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.FoodPack
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
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _packsState = MutableStateFlow<PacksUiState>(PacksUiState.Loading)
    val packsState: StateFlow<PacksUiState> = _packsState.asStateFlow()

    private val _selectedPack = MutableStateFlow<FoodPack?>(null)
    val selectedPack: StateFlow<FoodPack?> = _selectedPack.asStateFlow()

    private val _reserveState = MutableStateFlow<ReserveUiState>(ReserveUiState.Idle)
    val reserveState: StateFlow<ReserveUiState> = _reserveState.asStateFlow()

    init {
        loadNearbyPacks()
    }

    // @REQ-F03: Cargar packs cercanos (MVP: radio default 5km)
    fun loadNearbyPacks(lat: Double = -31.4, lng: Double = -64.18, radioKm: Double = 5.0) {
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
                is Result.Error -> { /* mantener null */ }
                else -> {}
            }
        }
    }

    // @REQ-F04: Reservar pack (userId hardcodeado = 1 para MVP)
    fun reservePack(packId: Long, userId: Long = 1L) {
        viewModelScope.launch {
            _reserveState.value = ReserveUiState.Loading
            _reserveState.value = when (val result = reservationRepository.createReservation(packId, userId)) {
                is Result.Success -> ReserveUiState.Success
                is Result.Error -> ReserveUiState.Error(result.exception.message ?: "Error al reservar")
                else -> ReserveUiState.Error("Error inesperado")
            }
        }
    }

    fun resetReserveState() { _reserveState.value = ReserveUiState.Idle }
}
