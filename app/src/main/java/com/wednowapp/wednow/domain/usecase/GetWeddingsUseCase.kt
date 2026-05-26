package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.model.Wedding
import com.wednowapp.wednow.domain.repository.WeddingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWeddingsUseCase @Inject constructor(
    private val repository: WeddingRepository
) {
    operator fun invoke(): Flow<List<Wedding>> = repository.getWeddings()
}
