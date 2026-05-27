package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.model.GuestGroup
import com.wednowapp.wednow.domain.repository.GuestGroupRepository
import javax.inject.Inject

class UpdateGuestGroupUseCase @Inject constructor(
    private val repository: GuestGroupRepository,
) {
    suspend operator fun invoke(group: GuestGroup): Result<Unit> =
        repository.updateGuestGroup(group)
}
