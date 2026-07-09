package com.maritsa.app.core.di

import com.maritsa.app.data.repository.WeddingRepositoryImpl
import com.maritsa.app.domain.repository.WeddingRepository
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
