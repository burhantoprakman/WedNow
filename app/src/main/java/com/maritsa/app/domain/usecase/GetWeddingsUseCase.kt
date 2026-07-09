package com.maritsa.app.domain.usecase

import com.maritsa.app.domain.model.Wedding
import com.maritsa.app.domain.repository.WeddingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWeddingsUseCase @Inject constructor(
    private val repository: WeddingRepository
) {
    operator fun invoke(): Flow<List<Wedding>> = repository.getWeddings()
}
