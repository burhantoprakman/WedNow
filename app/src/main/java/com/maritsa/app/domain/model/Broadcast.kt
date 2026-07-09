package com.maritsa.app.domain.model

data class Broadcast(
    val id: String = "",
    val message: String = "",
    val sentBy: String = "",
    val sentByName: String = "",
    val timestamp: Long = 0L
)
