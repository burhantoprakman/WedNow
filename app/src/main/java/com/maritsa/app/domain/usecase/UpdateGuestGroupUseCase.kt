package com.maritsa.app.domain.usecase

import com.maritsa.app.domain.model.GuestGroup
import com.maritsa.app.domain.repository.GuestGroupRepository
import javax.inject.Inject

class UpdateGuestGroupUseCase @Inject constructor(
    private val repository: GuestGroupRepository,
) {
    suspend operator fun invoke(group: GuestGroup): Result<Unit> =
        repository.updateGuestGroup(group)
}
