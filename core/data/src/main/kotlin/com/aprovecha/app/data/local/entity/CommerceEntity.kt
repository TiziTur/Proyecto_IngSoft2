package com.aprovecha.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para persistencia local de comercios.
 *
 * // @REQ-F01: El comercio debe poder registrarse en la plataforma.
 * // @REQ-F03: lat/lng usados para calcular distancia al usuario.
 */
@Entity(
    tableName = "commerces",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class CommerceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val nombre: String,
    val direccion: String,
    // @REQ-F03: coordenadas para filtrado por radio GPS
    val latitud: Double,
    val longitud: Double,
    val telefono: String? = null
)
