package com.maritsa.app.core.di

import com.maritsa.app.data.repository.GuestbookRepositoryImpl
import com.maritsa.app.domain.repository.GuestbookRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GuestbookModule {

    @Binds
    @Singleton
    abstract fun bindGuestbookRepository(impl: GuestbookRepositoryImpl): GuestbookRepository
}
