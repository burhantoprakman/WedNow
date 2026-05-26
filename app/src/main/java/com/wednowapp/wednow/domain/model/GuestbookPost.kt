package com.wednowapp.wednow.domain.model

data class GuestbookPost(
    val id: String = "",
    val guestId: String = "",
    val message: String = "",
    val timestamp: Long = 0L
)
