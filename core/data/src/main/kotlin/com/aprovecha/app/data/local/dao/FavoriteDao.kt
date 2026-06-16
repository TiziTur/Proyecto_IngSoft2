package com.aprovecha.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aprovecha.app.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: FavoriteEntity): Long

    @Query("DELETE FROM favorites WHERE userId = :userId AND packId = :packId")
    suspend fun removeFavorite(userId: Long, packId: Long)

    @Query("SELECT packId FROM favorites WHERE userId = :userId")
    fun getFavoritePackIds(userId: Long): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND packId = :packId)")
    suspend fun isFavorite(userId: Long, packId: Long): Boolean
}
