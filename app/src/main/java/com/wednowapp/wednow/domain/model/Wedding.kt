package com.wednowapp.wednow.domain.model

data class Wedding(
    val id: String = "",
    val name: String = "",
    val date: String = "",
    val location: String = "",
    val adminGuestId: String = "",
    val createdAt: Long = 0L,
    val coverImageUrl: String = "",
    val menu: List<MenuCourseData> = emptyList(),
    val dressCode: DressCodeData = DressCodeData(),
    val timeline: List<TimelineEventData> = emptyList(),
)
