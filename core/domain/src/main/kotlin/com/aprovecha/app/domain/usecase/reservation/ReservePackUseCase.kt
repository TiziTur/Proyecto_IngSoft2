package com.aprovecha.app.domain.usecase.reservation

import com.aprovecha.app.common.annotation.Requirement
import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.Reservation
import com.aprovecha.app.domain.repository.ReservationRepository

/**
 * Caso de uso: El usuario reserva un pack de alimentos disponible.
 *
 * // @REQ-F04: El usuario debe poder reservar un pack de alimentos para ser retirado.
 * // @REQ-NF01: El sistema debe garantizar que dos usuarios no reserven
 * //             el mismo pack simultáneamente.
 *
 * La garantía de exclusividad (REQ-NF01) se implementa en la capa de datos
 * mediante una transacción atómica en Room con verificación de estado AVAILABLE.
 *
 * @param reservationRepository Repositorio de reservas inyectado por Hilt
 */
class ReservePackUseCase(
    private val reservationRepository: ReservationRepository
) {
    /**
     * Ejecuta la reserva de un pack.
     *
     * // @REQ-F04: Creación de reserva vinculando pack y usuario
     * // @REQ-NF01: La llamada al repositorio es atómica
     *
     * @param packId ID del pack a reservar (debe estar en estado AVAILABLE)
     * @param userId ID del usuario que realiza la reserva
     * @return [Result.Success] con la [Reservation] creada, o [Result.Error]
     *         si el pack ya fue reservado (concurrencia) o no existe
     */
    @Requirement("REQ-F04", "Usuario reserva un pack disponible")
    suspend operator fun invoke(packId: Long, userId: Long): Result<Reservation> {
        return reservationRepository.createReservation(packId, userId)
    }
}
