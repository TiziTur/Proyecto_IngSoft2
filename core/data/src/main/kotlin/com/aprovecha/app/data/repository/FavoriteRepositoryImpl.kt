package com.aprovecha.app.data.repository

import com.aprovecha.app.data.local.dao.FavoriteDao
import com.aprovecha.app.data.local.entity.FavoriteEntity
import com.aprovecha.app.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao
) : FavoriteRepository {

    override fun getFavoritePackIds(userId: Long): Flow<Set<Long>> =
        favoriteDao.getFavoritePackIds(userId).map { it.toSet() }

    override suspend fun toggleFavorite(userId: Long, packId: Long) {
        if (favoriteDao.isFavorite(userId, packId)) {
            favoriteDao.removeFavorite(userId, packId)
        } else {
            favoriteDao.addFavorite(FavoriteEntity(userId = userId, packId = packId))
        }
    }
}
