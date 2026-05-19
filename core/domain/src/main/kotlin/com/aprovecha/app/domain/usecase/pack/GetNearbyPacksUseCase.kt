package com.aprovecha.app.domain.usecase.pack

import com.aprovecha.app.common.annotation.Requirement
import com.aprovecha.app.domain.model.FoodPack
import com.aprovecha.app.domain.repository.PackRepository
import kotlinx.coroutines.flow.Flow

/**
 * Caso de uso: Obtener packs disponibles cercanos a la ubicación del usuario.
 *
 * // @REQ-F03: El usuario debe poder ver la lista de productos disponibles
 * //           en un radio cercano a su ubicación.
 *
 * @param packRepository Repositorio de packs inyectado por Hilt
 */
class GetNearbyPacksUseCase(
    private val packRepository: PackRepository
) {
    /**
     * Retorna un [Flow] reactivo de packs disponibles en el radio especificado.
     *
     * // @REQ-F03: Filtrado por ubicación GPS con radio configurable
     *
     * @param latitud Latitud actual del dispositivo (GPS)
     * @param longitud Longitud actual del dispositivo (GPS)
     * @param radioKm Radio de búsqueda en kilómetros (default: [DEFAULT_RADIO_KM])
     * @return Flow que emite la lista actualizada cada vez que cambian los packs
     */
    @Requirement("REQ-F03", "Lista de packs disponibles filtrados por radio GPS")
    operator fun invoke(
        latitud: Double,
        longitud: Double,
        radioKm: Double = DEFAULT_RADIO_KM
    ): Flow<List<FoodPack>> {
        return packRepository.getAvailablePacksNearby(latitud, longitud, radioKm)
    }

    companion object {
        /** Radio de búsqueda por defecto: 5 km */
        const val DEFAULT_RADIO_KM = 5.0
    }
}
