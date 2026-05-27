package com.wednowapp.wednow.domain.model

data class DressCodeData(
    val style: String = "",
    val colorHexes: List<String> = emptyList(),
    val colorLabels: List<String> = emptyList(),
    val suggested: List<String> = emptyList(),
    val avoid: List<String> = emptyList(),
)
