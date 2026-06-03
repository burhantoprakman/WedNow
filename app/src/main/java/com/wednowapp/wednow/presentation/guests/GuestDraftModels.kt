package com.wednowapp.wednow.presentation.guests

import com.wednowapp.wednow.domain.model.MemberRole

data class MemberDraft(
    val name: String = "",
    val role: String = MemberRole.ADULT,
    val plusOneAllowed: Boolean = false,
)

data class GroupDraft(
    val id: String = "",
    val familyName: String = "",
    val members: List<MemberDraft> = listOf(MemberDraft()),
)

sealed class GroupActionState {
    object Idle : GroupActionState()
    object Saving : GroupActionState()
    data class Error(val message: String) : GroupActionState()
}
