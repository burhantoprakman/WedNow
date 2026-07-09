package com.maritsa.app.domain.usecase

import com.maritsa.app.domain.model.Wedding
import com.maritsa.app.domain.repository.WeddingRepository
import javax.inject.Inject

class GetWeddingByIdUseCase @Inject constructor(
    private val repository: WeddingRepository
) {
    suspend operator fun invoke(weddingId: String): Result<Wedding?> =
        repository.getWeddingById(weddingId)
}
