package com.aprovecha.app.domain.model

import java.time.LocalDateTime

/**
 * Modelo de dominio para una reserva de pack realizada por un usuario consumidor.
 *
 * // @REQ-F04: El usuario debe poder reservar un pack de alimentos para ser retirados.
 * // @REQ-F05: El encargado del local debe poder marcar una reserva como "retirada".
 * // @REQ-F06: El usuario debe poder cancelar una reserva si ya no la desea.
 * // @REQ-NF01: Una reserva representa la apropiación exclusiva de un pack.
 *
 * @param id Identificador único de la reserva
 * @param packId Referencia al [FoodPack] reservado
 * @param userId Referencia al [User] que realizó la reserva
 * @param status Estado actual: RESERVED, WITHDRAWN o CANCELLED
 * @param fechaReserva Timestamp de creación de la reserva
 * @param fechaActualizacion Timestamp del último cambio de estado
 */
data class Reservation(
    val id: Long = 0,
    val packId: Long,
    val userId: Long,
    val status: ReservationStatus = ReservationStatus.RESERVED,
    val fechaReserva: LocalDateTime = LocalDateTime.now(),
    val fechaActualizacion: LocalDateTime = LocalDateTime.now()
)

/**
 * Estados posibles de una reserva.
 *
 * // @REQ-F04: RESERVED — estado inicial al reservar
 * // @REQ-F05: WITHDRAWN — el comercio confirma el retiro
 * // @REQ-F06: CANCELLED — el usuario cancela antes del retiro
 */
enum class ReservationStatus {
    RESERVED,
    WITHDRAWN,
    CANCELLED
}
