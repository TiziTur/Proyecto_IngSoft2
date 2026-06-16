package com.aprovecha.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aprovecha.app.data.local.dao.FavoriteDao
import com.aprovecha.app.data.local.dao.PackDao
import com.aprovecha.app.data.local.dao.ReservationDao
import com.aprovecha.app.data.local.dao.UserDao
import com.aprovecha.app.data.local.entity.CommerceEntity
import com.aprovecha.app.data.local.entity.FavoriteEntity
import com.aprovecha.app.data.local.entity.PackEntity
import com.aprovecha.app.data.local.entity.ReservationEntity
import com.aprovecha.app.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CommerceEntity::class,
        PackEntity::class,
        ReservationEntity::class,
        FavoriteEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class AprovechaDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun packDao(): PackDao
    abstract fun reservationDao(): ReservationDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val DATABASE_NAME = "aprovecha_db"
    }
}
