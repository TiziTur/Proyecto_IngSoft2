package com.aprovecha.app.domain.repository

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.Reservation
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio de reservas.
 *
 * // @REQ-F04: El usuario debe poder reservar un pack de alimentos para ser retirado.
 * // @REQ-F05: El encargado del local debe poder marcar una reserva como "retirada".
 * // @REQ-F06: El usuario debe poder cancelar una reserva si ya no la desea.
 * // @REQ-NF01: createReservation debe garantizar exclusividad bajo concurrencia.
 */
interface ReservationRepository {

    // @REQ-F04: Crear una reserva — debe ser atómica (REQ-NF01)
    suspend fun createReservation(packId: Long, userId: Long): Result<Reservation>

    // @REQ-F05: El comercio marca la reserva como "retirada"
    suspend fun markAsWithdrawn(reservationId: Long): Result<Reservation>

    // @REQ-F06: El usuario cancela la reserva (libre hasta antes del retiro)
    suspend fun cancelReservation(reservationId: Long): Result<Reservation>

    // Obtener todas las reservas de un usuario consumidor
    fun getReservationsByUser(userId: Long): Flow<List<Reservation>>

    // Obtener todas las reservas de los packs de un comercio
    fun getReservationsByCommerce(commerceId: Long): Flow<List<Reservation>>

    // Obtener una reserva por su ID
    suspend fun getReservationById(reservationId: Long): Result<Reservation>
}
