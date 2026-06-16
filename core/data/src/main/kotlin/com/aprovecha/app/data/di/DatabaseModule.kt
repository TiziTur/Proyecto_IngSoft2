package com.aprovecha.app.data.di

import android.content.Context
import androidx.room.Room
import com.aprovecha.app.data.local.dao.FavoriteDao
import com.aprovecha.app.data.local.dao.PackDao
import com.aprovecha.app.data.local.dao.ReservationDao
import com.aprovecha.app.data.local.dao.UserDao
import com.aprovecha.app.data.local.database.AprovechaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AprovechaDatabase =
        Room.databaseBuilder(
            context,
            AprovechaDatabase::class.java,
            AprovechaDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideUserDao(db: AprovechaDatabase): UserDao = db.userDao()

    @Provides
    fun providePackDao(db: AprovechaDatabase): PackDao = db.packDao()

    @Provides
    fun provideReservationDao(db: AprovechaDatabase): ReservationDao = db.reservationDao()

    @Provides
    fun provideFavoriteDao(db: AprovechaDatabase): FavoriteDao = db.favoriteDao()
}
