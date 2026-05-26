package com.wednowapp.wednow.core.di

import com.wednowapp.wednow.data.repository.BroadcastRepositoryImpl
import com.wednowapp.wednow.domain.repository.BroadcastRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BroadcastModule {

    @Binds
    @Singleton
    abstract fun bindBroadcastRepository(impl: BroadcastRepositoryImpl): BroadcastRepository
}
