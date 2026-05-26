package com.wednowapp.wednow.domain.model

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = NotificationType.ANNOUNCEMENT,
    val timestamp: Long = 0L,
    val sentBy: String = "",
    val sentByName: String = ""
)

object NotificationType {
    const val ANNOUNCEMENT = "announcement"
    const val RSVP_UPDATE = "rsvp_update"
    const val GUEST_JOINED = "guest_joined"
    const val SYSTEM = "system"
}
