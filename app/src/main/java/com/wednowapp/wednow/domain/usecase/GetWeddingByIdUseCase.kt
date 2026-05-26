package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.model.Wedding
import com.wednowapp.wednow.domain.repository.WeddingRepository
import javax.inject.Inject

class GetWeddingByIdUseCase @Inject constructor(
    private val repository: WeddingRepository
) {
    suspend operator fun invoke(weddingId: String): Result<Wedding?> =
        repository.getWeddingById(weddingId)
}
