package com.maritsa.app.domain.repository

import com.maritsa.app.domain.model.Wedding
import kotlinx.coroutines.flow.Flow

interface WeddingRepository {
    fun getWeddings(): Flow<List<Wedding>>
    suspend fun getWeddingById(weddingId: String): Result<Wedding?>
    suspend fun getWeddingByShortCode(shortCode: String): Result<Wedding?>
    suspend fun createWedding(wedding: Wedding): Result<String>
}
