package com.aprovecha.app.data.di

import com.aprovecha.app.domain.repository.PackRepository
import com.aprovecha.app.domain.repository.ReservationRepository
import com.aprovecha.app.domain.usecase.pack.PublishPackUseCase
import com.aprovecha.app.domain.usecase.reservation.CancelReservationUseCase
import com.aprovecha.app.domain.usecase.reservation.MarkReservationWithdrawnUseCase
import com.aprovecha.app.domain.usecase.reservation.ReservePackUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt que provee los use cases del dominio.
 * Los use cases viven en :core:domain y no dependen de Hilt directamente,
 * por eso se proveen desde :core:data que sí tiene la dependencia de Hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // @REQ-F02
    @Provides
    @Singleton
    fun providePublishPackUseCase(packRepository: PackRepository): PublishPackUseCase =
        PublishPackUseCase(packRepository)

    // @REQ-F04, @REQ-NF01
    @Provides
    @Singleton
    fun provideReservePackUseCase(reservationRepository: ReservationRepository): ReservePackUseCase =
        ReservePackUseCase(reservationRepository)

    // @REQ-F05
    @Provides
    @Singleton
    fun provideMarkReservationWithdrawnUseCase(reservationRepository: ReservationRepository): MarkReservationWithdrawnUseCase =
        MarkReservationWithdrawnUseCase(reservationRepository)

    // @REQ-F06
    @Provides
    @Singleton
    fun provideCancelReservationUseCase(reservationRepository: ReservationRepository): CancelReservationUseCase =
        CancelReservationUseCase(reservationRepository)
}
