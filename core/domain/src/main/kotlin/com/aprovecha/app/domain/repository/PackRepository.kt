package com.aprovecha.app.domain.repository

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.FoodPack
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio de packs de alimentos.
 *
 * // @REQ-F02: El comercio debe poder publicar alimentos no vendidos con descuentos.
 * // @REQ-F03: El usuario debe poder ver la lista de productos en un radio cercano.
 * // @REQ-NF01: publishPack y los cambios de cantidad deben ser thread-safe.
 */
interface PackRepository {

    // @REQ-F02: Publicar un nuevo pack desde el comercio
    suspend fun publishPack(pack: FoodPack): Result<FoodPack>

    // @REQ-F03: Obtener packs disponibles dentro de un radio (en km) desde una ubicación
    fun getAvailablePacksNearby(
        latitud: Double,
        longitud: Double,
        radioKm: Double = 5.0
    ): Flow<List<FoodPack>>

    // @REQ-F02: Obtener todos los packs publicados por un comercio específico
    fun getPacksByCommerce(commerceId: Long): Flow<List<FoodPack>>

    // Obtener un pack por su ID
    suspend fun getPackById(packId: Long): Result<FoodPack>

    // Actualizar un pack existente (precio, descripción, cantidad)
    suspend fun updatePack(pack: FoodPack): Result<FoodPack>

    // Eliminar un pack (solo si está en estado AVAILABLE)
    suspend fun deletePack(packId: Long): Result<Unit>
}
