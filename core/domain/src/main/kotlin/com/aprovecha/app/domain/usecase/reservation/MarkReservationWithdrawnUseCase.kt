package com.aprovecha.app.domain.usecase.reservation

import com.aprovecha.app.common.annotation.Requirement
import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.Reservation
import com.aprovecha.app.domain.repository.ReservationRepository

/**
 * Caso de uso: El encargado del local marca una reserva como "retirada".
 *
 * // @REQ-F05: El encargado del local debe poder marcar una reserva como "retirada".
 *
 * @param reservationRepository Repositorio de reservas inyectado por Hilt
 */
class MarkReservationWithdrawnUseCase(
    private val reservationRepository: ReservationRepository
) {
    /**
     * Marca una reserva como WITHDRAWN (retirada).
     *
     * // @REQ-F05: Transición de estado RESERVED → WITHDRAWN
     *
     * @param reservationId ID de la reserva a marcar como retirada
     * @return [Result.Success] con la [Reservation] actualizada, o [Result.Error]
     *         si la reserva no existe o ya fue cancelada
     */
    @Requirement("REQ-F05", "Comercio marca reserva como retirada por el cliente")
    suspend operator fun invoke(reservationId: Long): Result<Reservation> {
        return reservationRepository.markAsWithdrawn(reservationId)
    }
}
