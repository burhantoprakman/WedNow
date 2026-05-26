package com.wednowapp.wednow.domain.model

data class Wedding(
    val id: String = "",
    val name: String = "",
    val date: String = "",
    val location: String = "",
    val adminGuestId: String = "",
    val createdAt: Long = 0L
)
