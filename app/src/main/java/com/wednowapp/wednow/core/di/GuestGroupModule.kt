package com.wednowapp.wednow.core.di

import com.wednowapp.wednow.data.repository.GuestGroupRepositoryImpl
import com.wednowapp.wednow.domain.repository.GuestGroupRepository
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
