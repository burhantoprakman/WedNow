package com.maritsa.app.core.di

import com.maritsa.app.data.repository.GuestRepositoryImpl
import com.maritsa.app.domain.repository.GuestRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GuestModule {

    @Binds
    @Singleton
    abstract fun bindGuestRepository(impl: GuestRepositoryImpl): GuestRepository
}
