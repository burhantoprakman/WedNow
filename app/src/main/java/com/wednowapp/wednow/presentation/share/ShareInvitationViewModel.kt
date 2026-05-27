package com.wednowapp.wednow.presentation.share

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wednowapp.wednow.core.navigation.Screen
import com.wednowapp.wednow.domain.model.GuestRole
import com.wednowapp.wednow.domain.model.Wedding
import com.wednowapp.wednow.domain.usecase.GetCurrentGuestUseCase
import com.wednowapp.wednow.domain.usecase.GetWeddingByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ShareInvitationState {
    object Loading : ShareInvitationState()
    data class Success(val wedding: Wedding, val isPrivileged: Boolean) : ShareInvitationState()
    data class Error(val message: String) : ShareInvitationState()
}

@HiltViewModel
class ShareInvitationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWeddingByIdUseCase: GetWeddingByIdUseCase,
    private val getCurrentGuestUseCase: GetCurrentGuestUseCase,
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.ShareInvitation.ARG])

    private val _state = MutableStateFlow<ShareInvitationState>(ShareInvitationState.Loading)
    val state: StateFlow<ShareInvitationState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            getWeddingByIdUseCase(weddingId)
                .onSuccess { wedding ->
                    if (wedding == null) {
                        _state.value = ShareInvitationState.Error("Wedding not found")
                        return@onSuccess
                    }
                    val guest =
                        runCatching { getCurrentGuestUseCase(weddingId).first() }.getOrNull()
                    val isPrivileged =
                        guest?.role == GuestRole.ADMIN || guest?.role == GuestRole.COADMIN
                    _state.value = ShareInvitationState.Success(wedding, isPrivileged)
                }
                .onFailure {
                    _state.value = ShareInvitationState.Error(it.message ?: "Failed to load")
                }
        }
    }
}
