package com.aprovecha.app.data.repository

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.data.local.dao.PackDao
import com.aprovecha.app.data.local.entity.PackEntity
import com.aprovecha.app.domain.model.FoodPack
import com.aprovecha.app.domain.model.PackStatus
import com.aprovecha.app.domain.repository.PackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

// @REQ-F02: Publicación de packs de alimentos
// @REQ-F03: Consulta de packs disponibles (Flow reactivo)

class PackRepositoryImpl @Inject constructor(
    private val packDao: PackDao
) : PackRepository {

    // @REQ-F02: Publicar un nuevo pack
    override suspend fun publishPack(pack: FoodPack): Result<FoodPack> = try {
        val id = packDao.insertPack(pack.toEntity())
        Result.Success(pack.copy(id = id))
    } catch (e: Exception) {
        Result.Error(e)
    }

    // @REQ-F03: Packs disponibles — en MVP se filtra en memoria (GPS real pendiente)
    override fun getAvailablePacksNearby(
        latitud: Double,
        longitud: Double,
        radioKm: Double
    ): Flow<List<FoodPack>> =
        packDao.getAvailablePacks().map { list -> list.map { it.toDomain() } }

    override suspend fun getPackById(packId: Long): Result<FoodPack> = try {
        val entity = packDao.getPackById(packId)
        if (entity != null) Result.Success(entity.toDomain())
        else Result.Error(NoSuchElementException("Pack no encontrado"))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun updatePack(pack: FoodPack): Result<FoodPack> = try {
        packDao.updatePack(pack.toEntity())
        Result.Success(pack)
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun deletePack(packId: Long): Result<Unit> = try {
        val rows = packDao.deletePack(packId)
        if (rows > 0) Result.Success(Unit)
        else Result.Error(IllegalStateException("No se puede eliminar: pack no disponible"))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override fun getPacksByCommerce(commerceId: Long): Flow<List<FoodPack>> =
        packDao.getPacksByCommerce(commerceId).map { list -> list.map { it.toDomain() } }
}

private fun PackEntity.toDomain() = FoodPack(
    id = id,
    commerceId = commerceId,
    name = nombre,
    description = descripcion,
    originalPrice = precioOriginal,
    discountPrice = precioDescuento,
    quantity = cantidad,
    photoUrl = fotoUri,
    status = PackStatus.valueOf(status),
    expirationTime = LocalDateTime.parse(fechaPublicacion),
    retiroDesde = retiroDesde,
    retiroHasta = retiroHasta
)

private fun FoodPack.toEntity() = PackEntity(
    id = id,
    commerceId = commerceId,
    nombre = name,
    descripcion = description,
    precioOriginal = originalPrice,
    precioDescuento = discountPrice,
    cantidad = quantity,
    fotoUri = photoUrl,
    status = status.name,
    fechaPublicacion = expirationTime.toString(),
    retiroDesde = retiroDesde,
    retiroHasta = retiroHasta
)
