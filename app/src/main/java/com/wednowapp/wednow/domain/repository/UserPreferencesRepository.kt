package com.wednowapp.wednow.domain.repository

import com.wednowapp.wednow.domain.model.UserPreferences

interface UserPreferencesRepository {
    suspend fun getPreferences(identityId: String): Result<UserPreferences?>
    suspend fun updateLastActiveWedding(identityId: String, weddingId: String): Result<Unit>
}
