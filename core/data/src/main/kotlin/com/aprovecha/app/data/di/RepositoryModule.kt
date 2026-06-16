package com.aprovecha.app.data.di

import com.aprovecha.app.data.repository.AuthRepositoryImpl
import com.aprovecha.app.data.repository.FavoriteRepositoryImpl
import com.aprovecha.app.data.repository.PackRepositoryImpl
import com.aprovecha.app.data.repository.ReservationRepositoryImpl
import com.aprovecha.app.domain.repository.AuthRepository
import com.aprovecha.app.domain.repository.FavoriteRepository
import com.aprovecha.app.domain.repository.PackRepository
import com.aprovecha.app.domain.repository.ReservationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // @REQ-F01
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    // @REQ-F02, @REQ-F03
    @Binds
    @Singleton
    abstract fun bindPackRepository(impl: PackRepositoryImpl): PackRepository

    // @REQ-F04, @REQ-F05, @REQ-F06, @REQ-NF01
    @Binds
    @Singleton
    abstract fun bindReservationRepository(impl: ReservationRepositoryImpl): ReservationRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository
}
