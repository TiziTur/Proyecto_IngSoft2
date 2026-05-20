package com.aprovecha.app.domain

import com.aprovecha.app.domain.model.FoodPack
import com.aprovecha.app.domain.model.PackStatus
import com.aprovecha.app.domain.repository.PackRepository
import com.aprovecha.app.domain.usecase.pack.GetNearbyPacksUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Tests unitarios para [GetNearbyPacksUseCase].
 *
 * // @REQ-F03: El usuario debe poder ver la lista de productos disponibles
 * //           en un radio cercano a su ubicación.
 *
 * Convención: given[Contexto]_when[Acción]_then[Resultado]
 */
class GetNearbyPacksUseCaseTest {

    private val packRepository: PackRepository = mockk()
    private lateinit var getNearbyPacksUseCase: GetNearbyPacksUseCase

    @Before
    fun setUp() {
        getNearbyPacksUseCase = GetNearbyPacksUseCase(packRepository)
    }

    private fun buildPack(id: Long = 1L) = FoodPack(
        id = id,
        commerceId = 1L,
        name = "Pack Panadería",
        description = "Pan del día",
        originalPrice = 500.0,
        discountPrice = 200.0,
        quantity = 3,
        status = PackStatus.AVAILABLE,
        expirationTime = LocalDateTime.now()
    )

    // ─── REQ-F03: Listado de packs cercanos ───────────────────────────────────

    /**
     * // @REQ-F03: Retorna un Flow con packs disponibles cuando se invoca con coordenadas válidas
     */
    @Test
    fun givenAvailablePacks_whenInvokedWithCoordinates_thenEmitsList() = runTest {
        // Given
        val packs = listOf(buildPack(1L), buildPack(2L), buildPack(3L))
        every { packRepository.getAvailablePacksNearby(-31.4, -64.18, 5.0) } returns flowOf(packs)

        // When
        val result = getNearbyPacksUseCase(latitud = -31.4, longitud = -64.18, radioKm = 5.0).first()

        // Then
        assertEquals(3, result.size)
        assertTrue(result.all { it.status == PackStatus.AVAILABLE })
    }

    /**
     * // @REQ-F03: Cuando no hay packs disponibles, el Flow emite lista vacía
     */
    @Test
    fun givenNoPacks_whenInvoked_thenEmitsEmptyList() = runTest {
        // Given
        every { packRepository.getAvailablePacksNearby(any(), any(), any()) } returns flowOf(emptyList())

        // When
        val result = getNearbyPacksUseCase(latitud = -31.4, longitud = -64.18).first()

        // Then
        assertTrue(result.isEmpty())
    }

    /**
     * // @REQ-F03: El radio por defecto es 5 km cuando no se especifica
     */
    @Test
    fun givenNoRadiusSpecified_whenInvoked_thenUsesDefaultRadius() = runTest {
        // Given
        every {
            packRepository.getAvailablePacksNearby(-31.4, -64.18, GetNearbyPacksUseCase.DEFAULT_RADIO_KM)
        } returns flowOf(emptyList())

        // When
        getNearbyPacksUseCase(latitud = -31.4, longitud = -64.18).first()

        // Then — verifica que se usó el radio por defecto
        verify { packRepository.getAvailablePacksNearby(-31.4, -64.18, GetNearbyPacksUseCase.DEFAULT_RADIO_KM) }
        assertEquals(5.0, GetNearbyPacksUseCase.DEFAULT_RADIO_KM, 0.001)
    }

    /**
     * // @REQ-F03: El radio configurable se pasa correctamente al repositorio
     */
    @Test
    fun givenCustomRadius_whenInvoked_thenPassesRadiusToRepository() = runTest {
        // Given
        val customRadius = 10.0
        every { packRepository.getAvailablePacksNearby(any(), any(), customRadius) } returns flowOf(emptyList())

        // When
        getNearbyPacksUseCase(latitud = -31.4, longitud = -64.18, radioKm = customRadius).first()

        // Then
        verify { packRepository.getAvailablePacksNearby(-31.4, -64.18, customRadius) }
    }

    /**
     * // @REQ-F03: El use case delega correctamente al repositorio (no tiene lógica propia de filtro)
     */
    @Test
    fun givenPacksInRepository_whenInvoked_thenDelegatesToRepository() = runTest {
        // Given
        val packs = listOf(buildPack(1L))
        every { packRepository.getAvailablePacksNearby(any(), any(), any()) } returns flowOf(packs)

        // When
        val result = getNearbyPacksUseCase(-31.4, -64.18).first()

        // Then
        assertEquals(1, result.size)
        verify(exactly = 1) { packRepository.getAvailablePacksNearby(any(), any(), any()) }
    }
}
