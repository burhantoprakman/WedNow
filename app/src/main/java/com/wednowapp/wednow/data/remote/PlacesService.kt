package com.wednowapp.wednow.data.remote

import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.wednowapp.wednow.domain.model.VenuePlace
import com.wednowapp.wednow.domain.model.VenueSuggestion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlacesService @Inject constructor(
    private val placesClient: PlacesClient,
) {
    private var sessionToken = AutocompleteSessionToken.newInstance()

    suspend fun fetchSuggestions(query: String): Result<List<VenueSuggestion>> {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setSessionToken(sessionToken)
            .build()
        return try {
            val predictions = placesClient.findAutocompletePredictions(request).await()
                .autocompletePredictions
                .map { pred ->
                    VenueSuggestion(
                        placeId = pred.placeId,
                        primaryText = pred.getPrimaryText(null).toString(),
                        secondaryText = pred.getSecondaryText(null).toString(),
                    )
                }
            Result.success(predictions)
        } catch (e: CancellationException) {
            throw e  // never swallow coroutine cancellation
        } catch (e: Exception) {
            Timber.e(e, "fetchSuggestions failed")
            Result.failure(e)
        }
    }

    suspend fun fetchPlaceDetails(placeId: String): VenuePlace? {
        val fields = listOf(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.FORMATTED_ADDRESS,
            Place.Field.LOCATION,
        )
        val request = FetchPlaceRequest.newInstance(placeId, fields)
        return try {
            val response = placesClient.fetchPlace(request).await()
            val place = response.place
            // Reset session token after a detail fetch (billing best practice)
            sessionToken = AutocompleteSessionToken.newInstance()
            VenuePlace(
                placeId = place.id ?: placeId,
                name = place.displayName.orEmpty(),
                address = place.formattedAddress.orEmpty(),
                lat = place.location?.latitude ?: 0.0,
                lng = place.location?.longitude ?: 0.0,
            )
        } catch (e: CancellationException) {
            throw e  // never swallow coroutine cancellation
        } catch (e: Exception) {
            Timber.e(e, "fetchPlaceDetails failed for placeId=$placeId")
            null
        }
    }
}
