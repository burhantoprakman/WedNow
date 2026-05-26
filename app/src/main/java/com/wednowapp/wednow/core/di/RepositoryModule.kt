package com.wednowapp.wednow.core.di

import com.wednowapp.wednow.data.repository.WeddingRepositoryImpl
import com.wednowapp.wednow.domain.repository.WeddingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWeddingRepository(impl: WeddingRepositoryImpl): WeddingRepository
}
