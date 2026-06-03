package com.wednowapp.wednow.domain.model

data class Guest(
    val id: String = "",
    val name: String = "",
    val role: String = GuestRole.GUEST,
    val rsvpStatus: String? = null,
    val rsvpUpdatedAt: Long? = null,
    val groupId: String? = null,
)

object GuestRole {
    const val GUEST = "guest"
    const val ADMIN = "admin"
    const val COADMIN = "coadmin"
}
