package com.maritsa.app.data.repository

import com.maritsa.app.data.remote.UserPreferencesFirestoreService
import com.maritsa.app.domain.model.UserPreferences
import com.maritsa.app.domain.repository.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val service: UserPreferencesFirestoreService,
) : UserPreferencesRepository {

    override suspend fun getPreferences(identityId: String): Result<UserPreferences?> =
        service.getPreferences(identityId)

    override suspend fun updateLastActiveWedding(identityId: String, weddingId: String) =
        service.updateLastActiveWedding(identityId, weddingId)
}
