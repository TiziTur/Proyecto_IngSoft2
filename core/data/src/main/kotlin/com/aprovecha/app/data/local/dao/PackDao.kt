package com.aprovecha.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.aprovecha.app.data.local.entity.PackEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de acceso a datos de packs de alimentos.
 *
 * // @REQ-F02: El comercio publica packs de alimentos no vendidos.
 * // @REQ-F03: Filtrado de packs disponibles (status = AVAILABLE).
 * // @REQ-NF01: reservePackAtomically() garantiza exclusividad bajo concurrencia.
 */
@Dao
interface PackDao {

    // @REQ-F02: Insertar un nuevo pack publicado por el comercio
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPack(pack: PackEntity): Long

    // @REQ-F03: Obtener todos los packs AVAILABLE (el filtro de radio se hace en el repo)
    @Query("SELECT * FROM packs WHERE status = 'AVAILABLE' ORDER BY fechaPublicacion DESC")
    fun getAvailablePacks(): Flow<List<PackEntity>>

    // @REQ-F02: Obtener packs de un comercio específico
    @Query("SELECT * FROM packs WHERE commerceId = :commerceId ORDER BY fechaPublicacion DESC")
    fun getPacksByCommerce(commerceId: Long): Flow<List<PackEntity>>

    // Obtener un pack por ID
    @Query("SELECT * FROM packs WHERE id = :packId LIMIT 1")
    suspend fun getPackById(packId: Long): PackEntity?

    // Actualizar pack
    @Update
    suspend fun updatePack(pack: PackEntity)

    /**
     * Operación atómica para reservar un pack.
     * Cambia el estado de AVAILABLE a RESERVED solo si actualmente está AVAILABLE.
     *
     * // @REQ-NF01: Garantiza que dos usuarios no reserven el mismo pack
     * //             simultáneamente — retorna 0 si el pack ya no está disponible.
     *
     * @return Número de filas actualizadas: 1 si fue exitoso, 0 si ya fue reservado
     */
    @Query("UPDATE packs SET status = 'RESERVED' WHERE id = :packId AND status = 'AVAILABLE'")
    suspend fun reservePackAtomically(packId: Long): Int

    // Actualizar estado de un pack
    @Query("UPDATE packs SET status = :status WHERE id = :packId")
    suspend fun updatePackStatus(packId: Long, status: String)

    // Eliminar pack (solo si está AVAILABLE)
    @Query("DELETE FROM packs WHERE id = :packId AND status = 'AVAILABLE'")
    suspend fun deletePack(packId: Long): Int
}
