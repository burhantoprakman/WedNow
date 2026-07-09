package com.maritsa.app.core.di

import com.maritsa.app.data.repository.IdentityRepositoryImpl
import com.maritsa.app.data.repository.MembershipRepositoryImpl
import com.maritsa.app.data.repository.UserPreferencesRepositoryImpl
import com.maritsa.app.domain.repository.IdentityRepository
import com.maritsa.app.domain.repository.MembershipRepository
import com.maritsa.app.domain.repository.UserPreferencesRepository
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
