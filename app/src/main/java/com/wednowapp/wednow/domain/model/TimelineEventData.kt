package com.wednowapp.wednow.domain.model

data class TimelineEventData(
    val time: String = "",
    val title: String = "",
    val description: String = "",
    val iconName: String = "",
    val status: String = "upcoming",
)
