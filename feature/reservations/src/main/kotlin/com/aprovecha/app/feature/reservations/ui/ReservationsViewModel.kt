package com.aprovecha.app.feature.reservations.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.FoodPack
import com.aprovecha.app.domain.model.Reservation
import com.aprovecha.app.domain.repository.PackRepository
import com.aprovecha.app.domain.repository.ReservationRepository
import com.aprovecha.app.domain.usecase.pack.PublishPackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

// @REQ-F04, F05, F06: ViewModel para reservas y operaciones del comercio

sealed class ReservationsUiState {
    data object Loading : ReservationsUiState()
    data class Success(val reservations: List<Reservation>) : ReservationsUiState()
    data class Error(val message: String) : ReservationsUiState()
}

sealed class CommercePacksState {
    data object Loading : CommercePacksState()
    data class Success(val packs: List<FoodPack>) : CommercePacksState()
    data class Error(val message: String) : CommercePacksState()
}

sealed class ActionState {
    data object Idle : ActionState()
    data object Loading : ActionState()
    data object Success : ActionState()
    data class Error(val message: String) : ActionState()
}

@HiltViewModel
class ReservationsViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val packRepository: PackRepository,
    private val publishPackUseCase: PublishPackUseCase
) : ViewModel() {

    private companion object {
        const val DEFAULT_USER_ID = 1L
        const val DEFAULT_COMMERCE_ID = 1L
        const val GENERIC_ERROR_MESSAGE = "Error"
        const val UNEXPECTED_ERROR_MESSAGE = "Error inesperado"
    }

    private val _reservationsState = MutableStateFlow<ReservationsUiState>(ReservationsUiState.Loading)
    val reservationsState: StateFlow<ReservationsUiState> = _reservationsState.asStateFlow()

    private val _commercePacksState = MutableStateFlow<CommercePacksState>(CommercePacksState.Loading)
    val commercePacksState: StateFlow<CommercePacksState> = _commercePacksState.asStateFlow()

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState.asStateFlow()

    // @REQ-F06: Cargar reservas del usuario (userId hardcodeado = 1 para MVP)
    fun loadUserReservations(userId: Long = DEFAULT_USER_ID) {
        viewModelScope.launch {
            reservationRepository.getReservationsByUser(userId)
                .catch {
                    _reservationsState.value = ReservationsUiState.Error(it.message ?: GENERIC_ERROR_MESSAGE)
                }
                .collect { list -> _reservationsState.value = ReservationsUiState.Success(list) }
        }
    }

    // Cargar packs del comercio (commerceId hardcodeado = 1 para MVP)
    fun loadCommercePacks(commerceId: Long = DEFAULT_COMMERCE_ID) {
        viewModelScope.launch {
            packRepository.getPacksByCommerce(commerceId)
                .catch {
                    _commercePacksState.value = CommercePacksState.Error(it.message ?: GENERIC_ERROR_MESSAGE)
                }
                .collect { list -> _commercePacksState.value = CommercePacksState.Success(list) }
        }
    }

    // @REQ-F06: Cancelar reserva
    fun cancelReservation(reservationId: Long) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            _actionState.value = when (val r = reservationRepository.cancelReservation(reservationId)) {
                is Result.Success -> ActionState.Success
                is Result.Error -> ActionState.Error(r.exception.message ?: "Error al cancelar")
                else -> ActionState.Error(UNEXPECTED_ERROR_MESSAGE)
            }
        }
    }

    // @REQ-F05: Marcar reserva como retirada
    fun markAsWithdrawn(reservationId: Long) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            _actionState.value = when (val r = reservationRepository.markAsWithdrawn(reservationId)) {
                is Result.Success -> ActionState.Success
                is Result.Error -> ActionState.Error(r.exception.message ?: GENERIC_ERROR_MESSAGE)
                else -> ActionState.Error(UNEXPECTED_ERROR_MESSAGE)
            }
        }
    }

    // @REQ-F02: Publicar nuevo pack
    fun publishPack(pack: FoodPack) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            _actionState.value = when (val r = publishPackUseCase(pack)) {
                is Result.Success -> ActionState.Success
                is Result.Error -> ActionState.Error(r.exception.message ?: "Error al publicar")
                else -> ActionState.Error(UNEXPECTED_ERROR_MESSAGE)
            }
        }
    }

    fun resetAction() { _actionState.value = ActionState.Idle }
}
