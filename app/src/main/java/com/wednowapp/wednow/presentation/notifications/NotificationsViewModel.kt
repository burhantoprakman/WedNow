package com.wednowapp.wednow.presentation.notifications

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wednowapp.wednow.core.navigation.Screen
import com.wednowapp.wednow.core.session.NotificationReadManager
import com.wednowapp.wednow.domain.model.AppNotification
import com.wednowapp.wednow.domain.model.GuestRole
import com.wednowapp.wednow.domain.usecase.GetCurrentGuestUseCase
import com.wednowapp.wednow.domain.usecase.GetNotificationsUseCase
import com.wednowapp.wednow.domain.usecase.SendNotificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getNotificationsUseCase: GetNotificationsUseCase,
    getCurrentGuestUseCase: GetCurrentGuestUseCase,
    private val sendNotificationUseCase: SendNotificationUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.Notifications.ARG])

    val notifications: StateFlow<List<AppNotification>> = getNotificationsUseCase(weddingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val canSendNotification: StateFlow<Boolean> = getCurrentGuestUseCase(weddingId)
        .map { guest -> guest?.role == GuestRole.ADMIN || guest?.role == GuestRole.COADMIN }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _readIds = MutableStateFlow(NotificationReadManager.getReadIds(context))
    val readIds: StateFlow<Set<String>> = _readIds.asStateFlow()

    val unreadCount: StateFlow<Int> = notifications
        .map { list -> list.count { it.id !in _readIds.value } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // ── Compose dialog state ───────────────────────────────────────────────

    var titleInput by mutableStateOf("")
        private set
    var bodyInput by mutableStateOf("")
        private set
    var isDialogVisible by mutableStateOf(false)
        private set
    var isSending by mutableStateOf(false)
        private set
    var sendError by mutableStateOf<String?>(null)
        private set

    val canConfirmSend: Boolean
        get() = titleInput.isNotBlank() && bodyInput.isNotBlank() && !isSending

    fun onTitleChange(text: String) { titleInput = text }
    fun onBodyChange(text: String) { bodyInput = text }

    fun showDialog() {
        titleInput = ""
        bodyInput = ""
        sendError = null
        isDialogVisible = true
    }

    fun dismissDialog() {
        if (isSending) return
        isDialogVisible = false
        titleInput = ""
        bodyInput = ""
        sendError = null
    }

    fun sendNotification() {
        if (!canConfirmSend) return
        viewModelScope.launch {
            isSending = true
            sendError = null
            sendNotificationUseCase(weddingId, titleInput, bodyInput)
                .onSuccess { dismissDialog() }
                .onFailure { sendError = it.message ?: "Failed to send notification" }
            isSending = false
        }
    }

    fun markAsRead(notificationId: String) {
        NotificationReadManager.markAsRead(context, notificationId)
        _readIds.value = NotificationReadManager.getReadIds(context)
    }

    fun markAllAsRead() {
        notifications.value.forEach { NotificationReadManager.markAsRead(context, it.id) }
        _readIds.value = NotificationReadManager.getReadIds(context)
    }
}
