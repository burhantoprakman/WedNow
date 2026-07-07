package com.wednowapp.wednow.domain.model

data class VenueSuggestion(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String,
)

data class VenuePlace(
    val placeId: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
)
