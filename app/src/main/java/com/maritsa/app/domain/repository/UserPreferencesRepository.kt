package com.maritsa.app.domain.repository

import com.maritsa.app.domain.model.UserPreferences

interface UserPreferencesRepository {
    suspend fun getPreferences(identityId: String): Result<UserPreferences?>
    suspend fun updateLastActiveWedding(identityId: String, weddingId: String): Result<Unit>
}
