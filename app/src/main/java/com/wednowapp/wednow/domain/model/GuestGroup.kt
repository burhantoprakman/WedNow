package com.wednowapp.wednow.domain.model

data class GuestGroup(
    val id: String = "",
    val weddingId: String = "",
    val familyName: String = "",
    val inviteToken: String = "",
    val invitationLink: String = "",
    val members: List<GuestMember> = emptyList(),
    val rsvpStatus: String? = null,
    val invitationOpened: Boolean = false,
    val createdAt: Long = 0L,
)

data class GuestMember(
    val name: String = "",
    val role: String = MemberRole.ADULT,
    val plusOneAllowed: Boolean = false,
    val rsvpStatus: String? = null,
)

object MemberRole {
    const val ADULT = "Adult"
    const val CHILD = "Child"
}
