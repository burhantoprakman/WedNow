package com.maritsa.app.domain.usecase

import com.maritsa.app.data.remote.FirestoreService
import com.maritsa.app.domain.model.Wedding
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateWeddingUseCase @Inject constructor(
    private val firestoreService: FirestoreService,
) {
    suspend operator fun invoke(wedding: Wedding): Result<Unit> =
        firestoreService.updateWedding(wedding)
}
