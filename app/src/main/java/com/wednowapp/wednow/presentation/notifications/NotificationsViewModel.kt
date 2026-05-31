package com.wednowapp.wednow.presentation.notifications

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wednowapp.wednow.core.identity.IdentityManager
import com.wednowapp.wednow.core.navigation.Screen
import com.wednowapp.wednow.core.session.NotificationReadManager
import com.wednowapp.wednow.domain.model.AppNotification
import com.wednowapp.wednow.domain.model.GuestRole
import com.wednowapp.wednow.domain.model.NotificationTargetScreen
import com.wednowapp.wednow.domain.usecase.GetCurrentGuestUseCase
import com.wednowapp.wednow.domain.usecase.GetNotificationsUseCase
import com.wednowapp.wednow.domain.usecase.MarkAllNotificationsReadUseCase
import com.wednowapp.wednow.domain.usecase.MarkNotificationReadUseCase
import com.wednowapp.wednow.domain.usecase.SendAnnouncementUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

// ── UI model ──────────────────────────────────────────────────────────────────

/** One date section header + its items, for the lazy list. */
data class NotificationSection(
    val label: String,
    val items: List<AppNotification>,
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getNotificationsUseCase: GetNotificationsUseCase,
    getCurrentGuestUseCase: GetCurrentGuestUseCase,
    private val sendAnnouncementUseCase: SendAnnouncementUseCase,
    private val markReadUseCase: MarkNotificationReadUseCase,
    private val markAllReadUseCase: MarkAllNotificationsReadUseCase,
    private val identityManager: IdentityManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.Notifications.ARG])

    private val currentIdentityId: String
        get() = identityManager.currentIdentityId

    // ── Raw notification stream ───────────────────────────────────────────────

    private val rawNotifications: StateFlow<List<AppNotification>> =
        getNotificationsUseCase(weddingId, currentIdentityId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Local read set (SharedPreferences — fast, offline-first) ─────────────

    private val _localReadIds = MutableStateFlow(NotificationReadManager.getReadIds(context))
    val readIds: StateFlow<Set<String>> = _localReadIds.asStateFlow()

    // ── Unread count (drives home-screen badge) ───────────────────────────────

    val unreadCount: StateFlow<Int> = combine(rawNotifications, _localReadIds) { notifs, readIds ->
        notifs.count { it.id !in readIds }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // ── Date-grouped sections for the UI ─────────────────────────────────────

    val sections: StateFlow<List<NotificationSection>> =
        rawNotifications.map { groupByDate(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Permission gate ───────────────────────────────────────────────────────

    val canSendAnnouncement: StateFlow<Boolean> = getCurrentGuestUseCase(weddingId)
        .map { guest -> guest?.role == GuestRole.ADMIN || guest?.role == GuestRole.COADMIN }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // ── Announcement composer state ───────────────────────────────────────────

    var titleInput by mutableStateOf(""); private set
    var bodyInput by mutableStateOf(""); private set
    var isComposerVisible by mutableStateOf(false); private set
    var isSending by mutableStateOf(false); private set
    var sendError by mutableStateOf<String?>(null); private set

    val canSend: Boolean
        get() = titleInput.isNotBlank() && bodyInput.isNotBlank() && !isSending

    fun onTitleChange(text: String) { titleInput = text }
    fun onBodyChange(text: String) {
        bodyInput = text
    }

    fun openComposer() {
        titleInput = ""; bodyInput = ""; sendError = null; isComposerVisible = true
    }

    fun closeComposer() {
        if (isSending) return
        isComposerVisible = false; titleInput = ""; bodyInput = ""; sendError = null
    }

    fun sendAnnouncement() {
        if (!canSend) return
        val title = titleInput;
        val body = bodyInput
        viewModelScope.launch {
            isSending = true; sendError = null
            sendAnnouncementUseCase(weddingId, title, body)
                .onSuccess { closeComposer() }
                .onFailure { sendError = it.message ?: "Failed to send announcement" }
            isSending = false
        }
    }

    // ── Mark as read ──────────────────────────────────────────────────────────

    fun markAsRead(notification: AppNotification) {
        NotificationReadManager.markAsRead(context, notification.id)
        _localReadIds.value = NotificationReadManager.getReadIds(context)
        viewModelScope.launch {
            markReadUseCase(weddingId, notification.id, currentIdentityId)
        }
    }

    fun markAllAsRead() {
        val ids = rawNotifications.value.map { it.id }
        ids.forEach { NotificationReadManager.markAsRead(context, it) }
        _localReadIds.value = NotificationReadManager.getReadIds(context)
        viewModelScope.launch { markAllReadUseCase(weddingId, ids, currentIdentityId) }
    }

    // ── Deep-link navigation helper ───────────────────────────────────────────

    /** Returns the route to navigate to when a notification is tapped, or null. */
    fun resolveDeepLinkRoute(notification: AppNotification): String? =
        when (notification.targetScreen) {
            NotificationTargetScreen.PHOTOS -> Screen.Photos.createRoute(weddingId)
            NotificationTargetScreen.GUESTBOOK -> Screen.Guestbook.createRoute(weddingId)
            NotificationTargetScreen.CHAT -> Screen.Chat.createRoute(weddingId)
            NotificationTargetScreen.WEDDING_INFO -> Screen.WeddingInfo.createRoute(weddingId)
            else -> null
        }

    // ── Date grouping ─────────────────────────────────────────────────────────

    private fun groupByDate(notifications: List<AppNotification>): List<NotificationSection> {
        if (notifications.isEmpty()) return emptyList()

        val todayCal = Calendar.getInstance().also {
            it.set(Calendar.HOUR_OF_DAY, 0); it.set(Calendar.MINUTE, 0)
            it.set(Calendar.SECOND, 0); it.set(Calendar.MILLISECOND, 0)
        }
        val todayMs = todayCal.timeInMillis
        val yesterdayMs = todayCal.also { it.add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis

        val today = notifications.filter { it.createdAt >= todayMs }
        val yesterday = notifications.filter { it.createdAt in yesterdayMs until todayMs }
        val earlier = notifications.filter { it.createdAt < yesterdayMs }

        return buildList {
            if (today.isNotEmpty()) add(NotificationSection("Today", today))
            if (yesterday.isNotEmpty()) add(NotificationSection("Yesterday", yesterday))
            if (earlier.isNotEmpty()) add(NotificationSection("Earlier", earlier))
        }
    }
}
