package com.wednowapp.wednow.domain.model

data class Wedding(
    val id: String = "",
    val shortCode: String = "",          // 6-character join code shown on invitations
    val name: String = "",
    val date: Long = 0L,
    val location: String = "",
    val venueName: String = "",
    val venueLat: Double = 0.0,
    val venueLng: Double = 0.0,
    val adminGuestId: String = "",
    val createdAt: Long = 0L,
    val coverImageUrl: String = "",
    val menu: List<MenuCourseData> = emptyList(),
    val dressCode: DressCodeData = DressCodeData(),
    val timeline: List<TimelineEventData> = emptyList(),
)
