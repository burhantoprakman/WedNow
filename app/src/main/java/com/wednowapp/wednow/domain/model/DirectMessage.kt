package com.wednowapp.wednow.domain.model

data class DirectMessage(
    val id: String = "",
    val message: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val timestamp: Long = 0L
)
