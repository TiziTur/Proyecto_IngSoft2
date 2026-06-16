package com.aprovecha.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para persistencia local de reservas.
 *
 * // @REQ-F04: El usuario debe poder reservar un pack de alimentos.
 * // @REQ-F05: El encargado del local marca una reserva como "retirada".
 * // @REQ-F06: El usuario puede cancelar una reserva.
 * // @REQ-NF01: La relación pack-reserva es clave para garantizar exclusividad.
 */
@Entity(
    tableName = "reservations",
    foreignKeys = [
        ForeignKey(
            entity = PackEntity::class,
            parentColumns = ["id"],
            childColumns = ["packId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("packId"), Index("userId"), Index("status")]
)
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packId: Long,
    val userId: Long,
    val cantidad: Int = 1,
    /** Estado: "RESERVED", "WITHDRAWN", "CANCELLED" */
    val status: String = "RESERVED",
    val fechaReserva: String,    // ISO-8601 LocalDateTime
    val fechaActualizacion: String // ISO-8601 LocalDateTime
)
