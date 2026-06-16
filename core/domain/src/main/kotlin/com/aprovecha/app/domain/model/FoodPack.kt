package com.aprovecha.app.domain.model

import java.time.LocalDateTime

/**
 * Modelo de dominio para un pack de alimentos publicado por un comercio.
 *
 * // @REQ-F02: El comercio debe poder publicar alimentos no vendidos con descuentos.
 * // @REQ-F03: Usado en la lista de productos disponibles cercanos al usuario.
 * // @REQ-NF01: quantity debe manejarse con control de concurrencia.
 */
data class FoodPack(
    val id: Long = 0,
    val commerceId: Long,
    val name: String,
    val description: String,
    val originalPrice: Double,
    val discountPrice: Double,
    val photoUrl: String? = null,
    val quantity: Int,
    val status: PackStatus = PackStatus.AVAILABLE,
    val expirationTime: LocalDateTime = LocalDateTime.now(),
    val retiroDesde: String = "",
    val retiroHasta: String = ""
) {
    private companion object {
        const val PERCENT_SCALE = 100
    }

    // @REQ-F02: Validacion: precio descuento < precio original
    val discountPercentage: Int
        get() = if (originalPrice > 0) {
            ((1 - discountPrice / originalPrice) * PERCENT_SCALE).toInt()
        } else 0
}
