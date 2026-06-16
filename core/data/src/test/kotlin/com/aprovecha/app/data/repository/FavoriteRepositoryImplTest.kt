package com.aprovecha.app.data.repository

import com.aprovecha.app.data.local.dao.FavoriteDao
import com.aprovecha.app.data.local.entity.FavoriteEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FavoriteRepositoryImplTest {

    private lateinit var favoriteDao: FavoriteDao
    private lateinit var repository: FavoriteRepositoryImpl

    @Before
    fun setUp() {
        favoriteDao = mockk()
        repository = FavoriteRepositoryImpl(favoriteDao)
    }

    @Test
    fun `Given user has favorites When getFavoritePackIds called Then returns Set of ids`() = runTest {
        every { favoriteDao.getFavoritePackIds(1L) } returns flowOf(listOf(10L, 20L, 30L))

        val result = repository.getFavoritePackIds(1L).first()

        assertEquals(setOf(10L, 20L, 30L), result)
    }

    @Test
    fun `Given pack not favorited When toggleFavorite called Then addFavorite is invoked`() = runTest {
        coEvery { favoriteDao.isFavorite(1L, 5L) } returns false
        coEvery { favoriteDao.addFavorite(any()) } returns 1L

        repository.toggleFavorite(1L, 5L)

        coVerify(exactly = 1) { favoriteDao.addFavorite(FavoriteEntity(userId = 1L, packId = 5L)) }
    }

    @Test
    fun `Given pack already favorited When toggleFavorite called Then removeFavorite is invoked`() = runTest {
        coEvery { favoriteDao.isFavorite(1L, 5L) } returns true
        coEvery { favoriteDao.removeFavorite(1L, 5L) } returns Unit

        repository.toggleFavorite(1L, 5L)

        coVerify(exactly = 1) { favoriteDao.removeFavorite(1L, 5L) }
    }

    @Test
    fun `Given user has no favorites When getFavoritePackIds called Then returns empty Set`() = runTest {
        every { favoriteDao.getFavoritePackIds(1L) } returns flowOf(emptyList())

        val result = repository.getFavoritePackIds(1L).first()

        assertTrue(result.isEmpty())
    }
}
