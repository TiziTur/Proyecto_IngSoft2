package com.aprovecha.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para persistencia local de packs de alimentos.
 *
 * // @REQ-F02: El comercio debe poder publicar alimentos no vendidos con descuentos.
 * // @REQ-F03: Incluye referencia a commerceId para filtrar por ubicación del comercio.
 * // @REQ-NF01: El campo `cantidad` es crítico para la gestión de concurrencia.
 *              Las actualizaciones deben hacerse en transacciones atómicas.
 */
@Entity(
    tableName = "packs",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["commerceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("commerceId"), Index("status")]
)
data class PackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val commerceId: Long,
    val nombre: String,
    val descripcion: String,
    val precioOriginal: Double,
    val precioDescuento: Double,
    val fotoUri: String? = null,
    // @REQ-NF01: cantidad debe actualizarse de forma atómica
    val cantidad: Int,
    /** Estado del pack: "AVAILABLE", "RESERVED", "WITHDRAWN", "CANCELLED" */
    val status: String = "AVAILABLE",
    val fechaPublicacion: String, // ISO-8601 LocalDateTime
    val retiroDesde: String = "",
    val retiroHasta: String = ""
)
