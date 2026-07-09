package com.maritsa.app.core.di

import com.maritsa.app.data.repository.DirectMessageRepositoryImpl
import com.maritsa.app.domain.repository.DirectMessageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DirectMessageModule {

    @Binds
    @Singleton
    abstract fun bindDirectMessageRepository(impl: DirectMessageRepositoryImpl): DirectMessageRepository
}
