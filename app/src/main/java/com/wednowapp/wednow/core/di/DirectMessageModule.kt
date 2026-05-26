package com.wednowapp.wednow.core.di

import com.wednowapp.wednow.data.repository.DirectMessageRepositoryImpl
import com.wednowapp.wednow.domain.repository.DirectMessageRepository
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
