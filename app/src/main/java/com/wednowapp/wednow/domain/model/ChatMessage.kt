package com.wednowapp.wednow.domain.model

data class ChatMessage(
    val id: String = "",
    val message: String = "",
    val guestId: String = "",
    val guestName: String = "",
    val timestamp: Long = 0L
)
