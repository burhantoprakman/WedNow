package com.maritsa.app.core.di

import com.maritsa.app.data.repository.GuestGroupRepositoryImpl
import com.maritsa.app.domain.repository.GuestGroupRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GuestGroupModule {

    @Binds
    @Singleton
    abstract fun bindGuestGroupRepository(impl: GuestGroupRepositoryImpl): GuestGroupRepository
}
