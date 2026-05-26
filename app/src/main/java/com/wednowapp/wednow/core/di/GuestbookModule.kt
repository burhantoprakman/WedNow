package com.wednowapp.wednow.core.di

import com.wednowapp.wednow.data.repository.GuestbookRepositoryImpl
import com.wednowapp.wednow.domain.repository.GuestbookRepository
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
