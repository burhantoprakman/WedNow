package com.wednowapp.wednow.domain.model

data class GuestbookPost(
    val id: String = "",
    val guestId: String = "",
    val senderName: String = "",
    val message: String = "",
    val photoUrls: List<String> = emptyList(),
    val timestamp: Long = 0L,
)
