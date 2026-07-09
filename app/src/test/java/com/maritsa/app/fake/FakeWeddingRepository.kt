package com.maritsa.app.fake

import com.maritsa.app.domain.model.Wedding
import com.maritsa.app.domain.repository.WeddingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeWeddingRepository : WeddingRepository {

    private val weddings = mutableMapOf<String, Wedding>()

    /** shortCode → weddingId reverse index */
    private val shortCodeIndex = mutableMapOf<String, String>()

    private val _weddingsFlow = MutableStateFlow<List<Wedding>>(emptyList())

    var createShouldFail = false

    override fun getWeddings(): Flow<List<Wedding>> = _weddingsFlow.asStateFlow()

    override suspend fun getWeddingById(weddingId: String): Result<Wedding?> =
        Result.success(weddings[weddingId])

    override suspend fun getWeddingByShortCode(code: String): Result<Wedding?> {
        val id = shortCodeIndex[code] ?: return Result.success(null)
        return Result.success(weddings[id])
    }

    override suspend fun createWedding(wedding: Wedding): Result<String> {
        if (createShouldFail) return Result.failure(RuntimeException("Fake create failure"))
        weddings[wedding.id] = wedding
        shortCodeIndex[wedding.shortCode] = wedding.id
        _weddingsFlow.value = weddings.values.toList()
        return Result.success(wedding.id)
    }

    fun seedWedding(wedding: Wedding) {
        weddings[wedding.id] = wedding
        shortCodeIndex[wedding.shortCode] = wedding.id
        _weddingsFlow.value = weddings.values.toList()
    }

    fun reset() {
        weddings.clear()
        shortCodeIndex.clear()
        _weddingsFlow.value = emptyList()
        createShouldFail = false
    }
}
