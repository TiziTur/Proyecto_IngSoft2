package com.aprovecha.app.domain

import com.aprovecha.app.common.util.Result
import com.aprovecha.app.domain.model.FoodPack
import com.aprovecha.app.domain.model.PackStatus
import com.aprovecha.app.domain.repository.PackRepository
import com.aprovecha.app.domain.usecase.pack.PublishPackUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para [PublishPackUseCase].
 *
 * // @REQ-F02: El comercio debe poder publicar alimentos no vendidos con descuentos.
 */
class PublishPackUseCaseTest {

    private val packRepository: PackRepository = mockk()
    private lateinit var publishPackUseCase: PublishPackUseCase

    @Before
    fun setUp() {
        publishPackUseCase = PublishPackUseCase(packRepository)
    }

    private fun buildValidPack(
        name: String = "Pack Panadería Tarde",
        originalPrice: Double = 500.0,
        discountPrice: Double = 200.0,
        quantity: Int = 3
    ) = FoodPack(
        commerceId = 1L,
        name = name,
        description = "Pan del día",
        originalPrice = originalPrice,
        discountPrice = discountPrice,
        quantity = quantity
    )

    // ─── REQ-F02: Publicación exitosa ─────────────────────────────────────────

    /**
     * // @REQ-F02: Publicación exitosa de pack con todos los datos válidos
     */
    @Test
    fun givenValidPack_whenPublish_thenReturnsSuccessWithStatusAvailable() = runTest {
        // Given
        val pack = buildValidPack()
        val savedPack = pack.copy(id = 1L, status = PackStatus.AVAILABLE)
        coEvery { packRepository.publishPack(any()) } returns Result.Success(savedPack)

        // When
        val result = publishPackUseCase(pack)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(PackStatus.AVAILABLE, (result as Result.Success).data.status)
        // Verificar que siempre se publica con estado AVAILABLE independientemente del input
        coVerify { packRepository.publishPack(match { it.status == PackStatus.AVAILABLE }) }
    }

    /**
     * // @REQ-F02: El porcentaje de descuento se calcula correctamente
     */
    @Test
    fun givenPackWithPrices_whenPublish_thenDiscountPercentageIsCorrect() = runTest {
        // Given — precio original 500, descuento 200 → 60% de descuento
        val pack = buildValidPack(originalPrice = 500.0, discountPrice = 200.0)
        coEvery { packRepository.publishPack(any()) } returns Result.Success(pack)

        // When / Then — la propiedad derivada calcula correctamente
        assertEquals(60, pack.discountPercentage)
    }

    // ─── REQ-F02: Validaciones de negocio ────────────────────────────────────

    /**
     * // @REQ-F02: No debe publicar pack con nombre vacío
     */
    @Test
    fun givenEmptyName_whenPublish_thenReturnsError() = runTest {
        // When
        val result = publishPackUseCase(buildValidPack(name = ""))

        // Then
        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { packRepository.publishPack(any()) }
    }

    /**
     * // @REQ-F02: No debe publicar pack con precio descuento >= precio original
     */
    @Test
    fun givenDiscountPriceHigherThanOriginal_whenPublish_thenReturnsError() = runTest {
        // Given — precio descuento igual al original (sin descuento real)
        val result = publishPackUseCase(buildValidPack(originalPrice = 200.0, discountPrice = 200.0))

        // Then
        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { packRepository.publishPack(any()) }
    }

    /**
     * // @REQ-F02: No debe publicar pack con cantidad 0 o negativa
     */
    @Test
    fun givenZeroQuantity_whenPublish_thenReturnsError() = runTest {
        // When
        val result = publishPackUseCase(buildValidPack(quantity = 0))

        // Then
        assertTrue(result is Result.Error)
    }
}
