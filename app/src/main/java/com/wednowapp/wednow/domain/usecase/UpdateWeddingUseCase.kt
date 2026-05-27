package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.data.remote.FirestoreService
import com.wednowapp.wednow.domain.model.Wedding
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateWeddingUseCase @Inject constructor(
    private val firestoreService: FirestoreService,
) {
    suspend operator fun invoke(wedding: Wedding): Result<Unit> =
        firestoreService.updateWedding(wedding)
}
