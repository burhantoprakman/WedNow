package com.maritsa.app.presentation.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maritsa.app.core.identity.IdentityManager
import com.maritsa.app.core.navigation.Screen
import com.maritsa.app.domain.model.GuestRole
import com.maritsa.app.domain.model.Wedding
import com.maritsa.app.domain.repository.NotificationRepository
import com.maritsa.app.domain.usecase.GetCurrentGuestUseCase
import com.maritsa.app.domain.usecase.GetWeddingByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWeddingByIdUseCase: GetWeddingByIdUseCase,
    private val getCurrentGuestUseCase: GetCurrentGuestUseCase,
    private val notificationRepository: NotificationRepository,
    private val identityManager: IdentityManager,
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.WeddingHome.ARG])

    private val _state = MutableStateFlow<WeddingDetailState>(WeddingDetailState.Loading)
    val state: StateFlow<WeddingDetailState> = _state.asStateFlow()

    /** Live unread notification count — drives the badge in the nav hub. */
    val unreadNotificationCount: StateFlow<Int> =
        notificationRepository.observeUnreadCount(
            weddingId = weddingId,
            recipientId = identityManager.currentIdentityId,
        ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = 0)

    init {
        loadWedding()
    }

    fun retry() {
        _state.value = WeddingDetailState.Loading
        loadWedding()
    }

    private fun loadWedding() {
        viewModelScope.launch {
            getWeddingByIdUseCase(weddingId)
                .onSuccess { wedding ->
                    if (wedding == null) {
                        _state.value = WeddingDetailState.Error("Wedding not found")
                        return@onSuccess
                    }
                    val guest =
                        runCatching { getCurrentGuestUseCase(weddingId).first() }.getOrNull()
                    val guestRole = guest?.role ?: GuestRole.GUEST
                    val isPrivileged =
                        guestRole == GuestRole.ADMIN || guestRole == GuestRole.COADMIN
                    _state.value = WeddingDetailState.Success(wedding, isPrivileged, guestRole)
                }
                .onFailure { error ->
                    _state.value = WeddingDetailState.Error(
                        error.message ?: "Failed to load wedding details"
                    )
                }
        }
    }
}

sealed class WeddingDetailState {
    object Loading : WeddingDetailState()
    data class Success(
        val wedding: Wedding,
        val isPrivileged: Boolean,
        val guestRole: String = GuestRole.GUEST,
    ) : WeddingDetailState()

    data class Error(val message: String) : WeddingDetailState()
}
