package com.aprovecha.app.feature.reservations.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.FoodPack
import com.aprovecha.app.domain.model.Reservation
import com.aprovecha.app.domain.repository.AuthRepository
import com.aprovecha.app.domain.repository.PackRepository
import com.aprovecha.app.domain.repository.ReservationRepository
import com.aprovecha.app.domain.usecase.pack.PublishPackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import com.aprovecha.app.domain.model.ReservationStatus
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

data class PendingReservation(
    val reservationId: Long,
    val packId: Long,
    val fechaReserva: java.time.LocalDateTime
)

data class CommerceStats(
    val pendingCount: Int = 0,
    val completedCount: Int = 0,
    val totalReservations: Int = 0
)

sealed class PendingReservationsUiState {
    data object Loading : PendingReservationsUiState()
    data class Success(val pending: List<PendingReservation>) : PendingReservationsUiState()
    data class Error(val message: String) : PendingReservationsUiState()
}

@HiltViewModel
class ReservationsViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val packRepository: PackRepository,
    private val publishPackUseCase: PublishPackUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private companion object {
        const val GENERIC_ERROR_MESSAGE = "Error"
        const val UNEXPECTED_ERROR_MESSAGE = "Error inesperado"
        const val SESSION_ERROR_MESSAGE = "Sesión no encontrada. Iniciá sesión nuevamente."
    }

    private val _reservationsState = MutableStateFlow<ReservationsUiState>(ReservationsUiState.Loading)
    val reservationsState: StateFlow<ReservationsUiState> = _reservationsState.asStateFlow()

    private val _commercePacksState = MutableStateFlow<CommercePacksState>(CommercePacksState.Loading)
    val commercePacksState: StateFlow<CommercePacksState> = _commercePacksState.asStateFlow()

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState.asStateFlow()

    private val _pendingReservationsState = MutableStateFlow<PendingReservationsUiState>(PendingReservationsUiState.Loading)
    val pendingReservationsState: StateFlow<PendingReservationsUiState> = _pendingReservationsState.asStateFlow()

    private val _commerceStatsState = MutableStateFlow(CommerceStats())
    val commerceStatsState: StateFlow<CommerceStats> = _commerceStatsState.asStateFlow()

    // @REQ-F06: Cargar reservas del usuario de la sesión activa
    fun loadUserReservations() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.id
            if (userId == null) {
                _reservationsState.value = ReservationsUiState.Error(SESSION_ERROR_MESSAGE)
                return@launch
            }
            reservationRepository.getReservationsByUser(userId)
                .catch {
                    _reservationsState.value = ReservationsUiState.Error(it.message ?: GENERIC_ERROR_MESSAGE)
                }
                .collect { list -> _reservationsState.value = ReservationsUiState.Success(list) }
        }
    }

    // Cargar packs del comercio de la sesión activa
    fun loadCommercePacks() {
        viewModelScope.launch {
            val commerceId = authRepository.getCurrentUser()?.id
            if (commerceId == null) {
                _commercePacksState.value = CommercePacksState.Error(SESSION_ERROR_MESSAGE)
                return@launch
            }
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

    // @REQ-F02: Publicar nuevo pack, usando el comercio de la sesión activa
    fun publishPack(pack: FoodPack) {
        viewModelScope.launch {
            val commerceId = authRepository.getCurrentUser()?.id
            if (commerceId == null) {
                _actionState.value = ActionState.Error(SESSION_ERROR_MESSAGE)
                return@launch
            }
            _actionState.value = ActionState.Loading
            _actionState.value = when (val r = publishPackUseCase(pack.copy(commerceId = commerceId))) {
                is Result.Success -> ActionState.Success
                is Result.Error -> ActionState.Error(r.exception.message ?: "Error al publicar")
                else -> ActionState.Error(UNEXPECTED_ERROR_MESSAGE)
            }
        }
    }

    fun resetAction() { _actionState.value = ActionState.Idle }

    // @REQ-F05: Cargar reservas pendientes del comercio (estado RESERVED)
    fun loadPendingReservations() {
        viewModelScope.launch {
            val commerceId = authRepository.getCurrentUser()?.id
            if (commerceId == null) {
                _pendingReservationsState.value = PendingReservationsUiState.Error(SESSION_ERROR_MESSAGE)
                return@launch
            }
            reservationRepository.getReservationsByCommerce(commerceId)
                .catch {
                    _pendingReservationsState.value = PendingReservationsUiState.Error(it.message ?: GENERIC_ERROR_MESSAGE)
                }
                .collect { list ->
                    val pending = list
                        .filter { it.status == ReservationStatus.RESERVED }
                        .map { PendingReservation(it.id, it.packId, it.fechaReserva) }
                    _pendingReservationsState.value = PendingReservationsUiState.Success(pending)
                    updateCommerceStats(list)
                }
        }
    }

    private fun updateCommerceStats(reservations: List<Reservation>) {
        _commerceStatsState.value = CommerceStats(
            pendingCount = reservations.count { it.status == ReservationStatus.RESERVED },
            completedCount = reservations.count { it.status == ReservationStatus.WITHDRAWN },
            totalReservations = reservations.size
        )
    }
}
