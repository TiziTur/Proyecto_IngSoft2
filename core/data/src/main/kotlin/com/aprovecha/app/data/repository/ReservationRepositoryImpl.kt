package com.aprovecha.app.data.repository

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.data.local.dao.PackDao
import com.aprovecha.app.data.local.dao.ReservationDao
import com.aprovecha.app.data.local.entity.ReservationEntity
import com.aprovecha.app.domain.model.Reservation
import com.aprovecha.app.domain.model.ReservationStatus
import com.aprovecha.app.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

// @REQ-F04: Creación atómica de reservas
// @REQ-F05: Marcado de reserva como retirada
// @REQ-F06: Cancelación de reserva
// @REQ-NF01: Exclusividad garantizada por transacción atómica en Room

class ReservationRepositoryImpl @Inject constructor(
    private val reservationDao: ReservationDao,
    private val packDao: PackDao
) : ReservationRepository {

    private companion object {
        const val RESERVATION_NOT_FOUND_MESSAGE = "Reserva no encontrada"
    }

    // @REQ-F04 + @REQ-NF01
    override suspend fun createReservation(packId: Long, userId: Long): Result<Reservation> = try {
        val now = LocalDateTime.now().toString()
        val rowsAffected = packDao.reservePackAtomically(packId)
        if (rowsAffected == 0) {
            Result.Error(IllegalStateException("Pack ya reservado (REQ-NF01)"))
        } else {
            val entity = ReservationEntity(
                packId = packId,
                userId = userId,
                status = ReservationStatus.RESERVED.name,
                fechaReserva = now,
                fechaActualizacion = now
            )
            val id = reservationDao.insertReservation(entity)
            Result.Success(entity.copy(id = id).toDomain())
        }
    } catch (e: Exception) {
        Result.Error(e)
    }

    // @REQ-F05: RESERVED → WITHDRAWN
    override suspend fun markAsWithdrawn(reservationId: Long): Result<Reservation> {
        return try {
            val entity = reservationDao.getReservationById(reservationId)
                ?: return Result.Error(NoSuchElementException(RESERVATION_NOT_FOUND_MESSAGE))
            if (entity.status != ReservationStatus.RESERVED.name) {
                return Result.Error(IllegalStateException("Solo se pueden retirar reservas RESERVED"))
            }
            val now = LocalDateTime.now().toString()
            reservationDao.markAsWithdrawn(reservationId, now)
            Result.Success(entity.copy(status = ReservationStatus.WITHDRAWN.name, fechaActualizacion = now).toDomain())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // @REQ-F06: RESERVED → CANCELLED (libre hasta antes del retiro)
    override suspend fun cancelReservation(reservationId: Long): Result<Reservation> {
        return try {
            val entity = reservationDao.getReservationById(reservationId)
                ?: return Result.Error(NoSuchElementException(RESERVATION_NOT_FOUND_MESSAGE))
            if (entity.status == ReservationStatus.WITHDRAWN.name) {
                return Result.Error(IllegalStateException("No se puede cancelar una reserva ya retirada"))
            }
            val now = LocalDateTime.now().toString()
            reservationDao.cancelReservation(reservationId, now)
            Result.Success(entity.copy(status = ReservationStatus.CANCELLED.name, fechaActualizacion = now).toDomain())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getReservationsByUser(userId: Long): Flow<List<Reservation>> =
        reservationDao.getReservationsByUser(userId).map { list -> list.map { it.toDomain() } }

    override fun getReservationsByCommerce(commerceId: Long): Flow<List<Reservation>> =
        reservationDao.getReservationsByCommerce(commerceId).map { list -> list.map { it.toDomain() } }

    override suspend fun getReservationById(reservationId: Long): Result<Reservation> = try {
        val entity = reservationDao.getReservationById(reservationId)
        if (entity != null) Result.Success(entity.toDomain())
        else Result.Error(NoSuchElementException(RESERVATION_NOT_FOUND_MESSAGE))
    } catch (e: Exception) {
        Result.Error(e)
    }
}

private fun ReservationEntity.toDomain() = Reservation(
    id = id,
    packId = packId,
    userId = userId,
    status = ReservationStatus.valueOf(status),
    fechaReserva = LocalDateTime.parse(fechaReserva)
)
