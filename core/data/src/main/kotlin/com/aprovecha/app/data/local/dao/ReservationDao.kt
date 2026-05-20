package com.aprovecha.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aprovecha.app.data.local.entity.ReservationEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de acceso a datos de reservas.
 *
 * // @REQ-F04: Creación de reservas.
 * // @REQ-F05: Marcado como retirado.
 * // @REQ-F06: Cancelación de reservas.
 * // @REQ-NF01: insertReservation se llama dentro de una transacción junto con PackDao.reservePackAtomically.
 */
@Dao
interface ReservationDao {

    // @REQ-F04: Insertar una nueva reserva
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReservation(reservation: ReservationEntity): Long

    // @REQ-F05: Marcar reserva como WITHDRAWN (retirada)
    @Query(
        "UPDATE reservations SET status = 'WITHDRAWN', fechaActualizacion = :timestamp " +
            "WHERE id = :reservationId AND status = 'RESERVED'"
    )
    suspend fun markAsWithdrawn(reservationId: Long, timestamp: String): Int

    // @REQ-F06: Cancelar reserva (libre hasta antes de WITHDRAWN)
    @Query(
        "UPDATE reservations SET status = 'CANCELLED', fechaActualizacion = :timestamp " +
            "WHERE id = :reservationId AND status = 'RESERVED'"
    )
    suspend fun cancelReservation(reservationId: Long, timestamp: String): Int

    // Obtener reservas de un usuario consumidor
    @Query("SELECT * FROM reservations WHERE userId = :userId ORDER BY fechaReserva DESC")
    fun getReservationsByUser(userId: Long): Flow<List<ReservationEntity>>

    // Obtener reservas de los packs de un comercio (JOIN con packs)
    @Query("""
        SELECT r.* FROM reservations r
        INNER JOIN packs p ON r.packId = p.id
        INNER JOIN commerces c ON p.commerceId = c.id
        WHERE c.id = :commerceId
        ORDER BY r.fechaReserva DESC
    """)
    fun getReservationsByCommerce(commerceId: Long): Flow<List<ReservationEntity>>

    // Obtener una reserva por ID
    @Query("SELECT * FROM reservations WHERE id = :reservationId LIMIT 1")
    suspend fun getReservationById(reservationId: Long): ReservationEntity?

    // Obtener reservas por pack (para ProductsViewModel)
    @Query("SELECT * FROM reservations WHERE packId = :packId ORDER BY fechaReserva DESC")
    fun getReservationsByPack(packId: Long): Flow<List<ReservationEntity>>
}
