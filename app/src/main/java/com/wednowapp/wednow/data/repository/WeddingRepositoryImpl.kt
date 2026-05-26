package com.wednowapp.wednow.data.repository

import com.wednowapp.wednow.data.remote.FirestoreService
import com.wednowapp.wednow.domain.model.Wedding
import com.wednowapp.wednow.domain.repository.WeddingRepository
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

    override suspend fun createWedding(wedding: Wedding): Result<String> =
        firestoreService.createWedding(wedding)
}
