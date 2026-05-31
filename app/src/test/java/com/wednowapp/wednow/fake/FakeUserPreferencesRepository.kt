package com.wednowapp.wednow.fake

import com.wednowapp.wednow.domain.model.UserPreferences
import com.wednowapp.wednow.domain.repository.UserPreferencesRepository

class FakeUserPreferencesRepository : UserPreferencesRepository {

    private val store = mutableMapOf<String, UserPreferences>()

    /** Simulates a remote store (different device / fresh install) */
    val remoteStore = mutableMapOf<String, UserPreferences>()

    var fetchFromRemote = false
    var fetchCount = 0

    override suspend fun getPreferences(identityId: String): Result<UserPreferences?> {
        fetchCount++
        val source = if (fetchFromRemote) remoteStore else store
        return Result.success(source[identityId])
    }

    override suspend fun updateLastActiveWedding(
        identityId: String,
        weddingId: String,
    ): Result<Unit> {
        val current = store.getOrDefault(identityId, UserPreferences(identityId))
        store[identityId] = current.copy(
            lastActiveWeddingId = weddingId,
            updatedAt = System.currentTimeMillis(),
        )
        // Also update the "remote" store to simulate cross-device sync
        remoteStore[identityId] = store[identityId]!!
        return Result.success(Unit)
    }

    fun getLastActiveWedding(identityId: String): String? = store[identityId]?.lastActiveWeddingId

    /** Seeds local preferences (simulates the same-device scenario). */
    fun seedPreferences(prefs: UserPreferences) {
        store[prefs.identityId] = prefs
    }

    /**
     * Seeds preferences only in [remoteStore].
     * Enable [fetchFromRemote] before calling the use case to simulate a
     * fresh-install device that has no local store entry.
     */
    fun setRemotePreferences(identityId: String, prefs: UserPreferences) {
        remoteStore[identityId] = prefs
    }

    fun reset() {
        store.clear()
        remoteStore.clear()
        fetchFromRemote = false
        fetchCount = 0
    }
}
