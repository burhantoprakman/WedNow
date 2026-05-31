package com.wednowapp.wednow.core.di

import com.wednowapp.wednow.data.repository.IdentityRepositoryImpl
import com.wednowapp.wednow.data.repository.MembershipRepositoryImpl
import com.wednowapp.wednow.data.repository.UserPreferencesRepositoryImpl
import com.wednowapp.wednow.domain.repository.IdentityRepository
import com.wednowapp.wednow.domain.repository.MembershipRepository
import com.wednowapp.wednow.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds the three new identity-layer repositories.
 *
 * [IdentityManager], [IdentityMigrationService], and [PermissionService] are
 * @Singleton classes with @Inject constructors so Hilt provides them automatically
 * without needing explicit @Provides / @Binds entries here.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class IdentityModule {

    @Binds
    @Singleton
    abstract fun bindIdentityRepository(
        impl: IdentityRepositoryImpl,
    ): IdentityRepository

    @Binds
    @Singleton
    abstract fun bindMembershipRepository(
        impl: MembershipRepositoryImpl,
    ): MembershipRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl,
    ): UserPreferencesRepository
}
