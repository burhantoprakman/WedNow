package com.wednowapp.wednow.domain.model

data class MenuCourseData(
    val courseName: String = "",
    val emoji: String = "",
    val items: List<String> = emptyList(),
)
