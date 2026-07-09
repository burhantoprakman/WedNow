package com.maritsa.app.data.repository

import com.maritsa.app.data.remote.FirestoreService
import com.maritsa.app.domain.model.Wedding
import com.maritsa.app.domain.repository.WeddingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeddingRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService
) : WeddingRepository {

    override fun getWeddings(): Flow<List<Wedding>> =
        firestoreService.getWeddings()

    override suspend fun getWeddingById(weddingId: String): Result<Wedding?> =
        firestoreService.getWeddingById(weddingId)

    override suspend fun getWeddingByShortCode(shortCode: String): Result<Wedding?> =
        firestoreService.getWeddingByShortCode(shortCode)

    override suspend fun createWedding(wedding: Wedding): Result<String> =
        firestoreService.createWedding(wedding)
}
