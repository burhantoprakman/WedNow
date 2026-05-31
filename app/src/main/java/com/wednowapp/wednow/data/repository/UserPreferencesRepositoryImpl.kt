package com.wednowapp.wednow.data.repository

import com.wednowapp.wednow.data.remote.UserPreferencesFirestoreService
import com.wednowapp.wednow.domain.model.UserPreferences
import com.wednowapp.wednow.domain.repository.UserPreferencesRepository
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
