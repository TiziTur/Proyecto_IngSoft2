package com.aprovecha.app.domain.usecase.reservation

import com.aprovecha.app.common.annotation.Requirement
import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.Reservation
import com.aprovecha.app.domain.repository.ReservationRepository

/**
 * Caso de uso: El usuario cancela una reserva existente.
 *
 * // @REQ-F06: El usuario debe poder cancelar una reserva si ya no la desea.
 * Política: la cancelación es libre hasta que el comercio marque la reserva
 * como WITHDRAWN — no hay ventana de tiempo mínima.
 *
 * @param reservationRepository Repositorio de reservas inyectado por Hilt
 */
class CancelReservationUseCase(
    private val reservationRepository: ReservationRepository
) {
    /**
     * Cancela una reserva existente en estado RESERVED.
     *
     * // @REQ-F06: Transición de estado RESERVED → CANCELLED
     *
     * @param reservationId ID de la reserva a cancelar
     * @return [Result.Success] con la [Reservation] cancelada, o [Result.Error]
     *         si la reserva ya fue retirada (WITHDRAWN) o no existe
     */
    @Requirement("REQ-F06", "Usuario cancela reserva antes del retiro")
    suspend operator fun invoke(reservationId: Long): Result<Reservation> {
        return reservationRepository.cancelReservation(reservationId)
    }
}
