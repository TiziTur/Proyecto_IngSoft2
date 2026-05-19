package com.aprovecha.app.domain.usecase.pack

import com.aprovecha.app.common.annotation.Requirement
import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.FoodPack
import com.aprovecha.app.domain.model.PackStatus
import com.aprovecha.app.domain.repository.PackRepository

/**
 * Caso de uso: Publicar un pack de alimentos no vendidos.
 *
 * // @REQ-F02: El comercio debe poder publicar alimentos no vendidos en el dia con descuentos.
 */
class PublishPackUseCase(
    private val packRepository: PackRepository
) {
    /**
     * // @REQ-F02: Publicacion de pack con nombre, precio, descuento, foto y cantidad
     * Validaciones: nombre no vacio, discountPrice < originalPrice, quantity > 0
     */
    @Requirement("REQ-F02", "Comercio publica pack de alimentos no vendidos con descuento")
    suspend operator fun invoke(pack: FoodPack): Result<FoodPack> {
        if (pack.name.isBlank()) {
            return Result.Error(IllegalArgumentException("El nombre del pack no puede estar vacio"))
        }
        if (pack.discountPrice >= pack.originalPrice) {
            return Result.Error(
                IllegalArgumentException("El precio con descuento debe ser menor al precio original")
            )
        }
        if (pack.quantity <= 0) {
            return Result.Error(IllegalArgumentException("La cantidad debe ser mayor a cero"))
        }
        return packRepository.publishPack(pack.copy(status = PackStatus.AVAILABLE))
    }
}
