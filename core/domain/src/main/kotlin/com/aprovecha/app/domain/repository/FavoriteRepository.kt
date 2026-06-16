package com.aprovecha.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getFavoritePackIds(userId: Long): Flow<Set<Long>>
    suspend fun toggleFavorite(userId: Long, packId: Long)
}
