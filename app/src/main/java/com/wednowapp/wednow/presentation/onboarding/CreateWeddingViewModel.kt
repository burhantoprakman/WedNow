package com.wednowapp.wednow.presentation.onboarding

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.core.session.WeddingSessionManager
import com.wednowapp.wednow.data.remote.PhotoStorageService
import com.wednowapp.wednow.domain.model.DressCodeData
import com.wednowapp.wednow.domain.model.MenuCourseData
import com.wednowapp.wednow.domain.model.TimelineEventData
import com.wednowapp.wednow.domain.usecase.CreateWeddingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class CreateWeddingViewModel @Inject constructor(
    private val createWeddingUseCase: CreateWeddingUseCase,
    private val photoStorageService: PhotoStorageService,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    companion object {
        const val TOTAL_STEPS = 9

        val DEFAULT_MENU = listOf(
            MenuCourseData("Starter", "🥗"),
            MenuCourseData("Main Course", "🍽️"),
            MenuCourseData("Dessert", "🍰"),
            MenuCourseData("Drinks", "🥂"),
        )

        val DRESS_CODE_OPTIONS = listOf(
            "Black Tie", "Formal", "Cocktail", "Semi-Formal",
            "Smart Casual", "Garden Party", "Beach Casual",
        )

        val TIMELINE_ICONS = listOf(
            "groups", "wine_bar", "favorite", "local_bar",
            "restaurant", "music_note", "cake", "celebration",
            "nights_stay", "schedule", "camera", "star",
        )
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    var step by mutableIntStateOf(0); private set

    // ── Step 1: Couple Info ───────────────────────────────────────────────────
    var coupleName by mutableStateOf(""); private set
    var coupleSubtitle by mutableStateOf(""); private set

    // ── Step 2: Date & Time ───────────────────────────────────────────────────
    var formattedDate by mutableStateOf(""); private set
    var formattedTime by mutableStateOf(""); private set
    var showDatePicker by mutableStateOf(false); private set
    var showTimePicker by mutableStateOf(false); private set

    /** Raw millis of the user-selected date — fed back into DatePickerState so
     *  the picker opens on the already-chosen date rather than the current month. */
    var selectedDateMillis by mutableStateOf<Long?>(null); private set
    private var selectedTimeHour: Int = 0
    private var selectedTimeMinute: Int = 0

    // ── Step 3: Venue ─────────────────────────────────────────────────────────
    var venue by mutableStateOf(""); private set
    var isVenueSearching by mutableStateOf(false); private set
    var isVenueConfirmed by mutableStateOf(false); private set
    private var venueSearchJob: Job? = null

    // ── Step 4: Cover Image ───────────────────────────────────────────────────
    var coverImageUri by mutableStateOf<Uri?>(null); private set

    // ── Step 5: Menu ─────────────────────────────────────────────────────────
    var menuCourses by mutableStateOf(DEFAULT_MENU); private set

    // ── Step 6: Dress Code ────────────────────────────────────────────────────
    var dressCodeStyle by mutableStateOf(""); private set

    // ── Step 7: Timeline ──────────────────────────────────────────────────────
    var timelineEvents by mutableStateOf(listOf<TimelineEventData>()); private set

    // ── Submit state ──────────────────────────────────────────────────────────
    private val _createState = MutableStateFlow<CreateWeddingState>(CreateWeddingState.Idle)
    val createState: StateFlow<CreateWeddingState> = _createState.asStateFlow()

    // ── Navigation ────────────────────────────────────────────────────────────

    fun goNext() {
        step = (step + 1).coerceAtMost(TOTAL_STEPS - 1)
    }

    fun goBack() {
        step = (step - 1).coerceAtLeast(0)
    }

    fun isNextEnabled(): Boolean = when (step) {
        1 -> coupleName.isNotBlank()
        2 -> formattedDate.isNotBlank()
        3 -> venue.isNotBlank()
        else -> true
    }

    // ── Step 1 ────────────────────────────────────────────────────────────────

    fun onCoupleNameChange(v: String) {
        coupleName = v
    }

    fun onCoupleSubtitleChange(v: String) {
        coupleSubtitle = v
    }

    // ── Step 2 ────────────────────────────────────────────────────────────────

    fun openDatePicker() {
        showDatePicker = true
    }

    fun closeDatePicker() {
        showDatePicker = false
    }

    fun openTimePicker() {
        showTimePicker = true
    }

    fun closeTimePicker() {
        showTimePicker = false
    }

    fun onDateSelected(ms: Long) {
        selectedDateMillis = ms
        // DatePickerState always returns midnight UTC. Using the device's local
        // timezone here would shift the date backwards in UTC+ regions (e.g.
        // Turkey UTC+3: midnight UTC = 21:00 the previous day locally).
        // Force UTC so the displayed date matches exactly what the user tapped.
        formattedDate = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(ms))
        showDatePicker = false
    }

    fun onTimeSelected(hour: Int, minute: Int) {
        selectedTimeHour = hour
        selectedTimeMinute = minute
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        formattedTime = SimpleDateFormat("h:mm a", Locale.ENGLISH).format(cal.time)
        showTimePicker = false
    }

    // ── Step 3 ────────────────────────────────────────────────────────────────

    fun onVenueChange(v: String) {
        venue = v
        isVenueConfirmed = false
        venueSearchJob?.cancel()
        if (v.length >= 3) {
            isVenueSearching = true
            venueSearchJob = viewModelScope.launch {
                delay(650) // debounce — simulate "looking up" the venue
                isVenueSearching = false
                isVenueConfirmed = true
            }
        } else {
            isVenueSearching = false
        }
    }

    // ── Step 4 ────────────────────────────────────────────────────────────────

    fun onCoverImageSelected(uri: Uri) {
        coverImageUri = uri
    }

    // ── Step 5 ────────────────────────────────────────────────────────────────

    fun onMenuItemAdd(courseIdx: Int, item: String) {
        if (item.isBlank()) return
        menuCourses = menuCourses.mapIndexed { i, c ->
            if (i == courseIdx) c.copy(items = c.items + item.trim()) else c
        }
    }

    fun onMenuItemRemove(courseIdx: Int, itemIdx: Int) {
        menuCourses = menuCourses.mapIndexed { i, c ->
            if (i == courseIdx) c.copy(items = c.items.filterIndexed { j, _ -> j != itemIdx }) else c
        }
    }

    // ── Step 6 ────────────────────────────────────────────────────────────────

    fun onDressCodeSelected(style: String) {
        dressCodeStyle = if (dressCodeStyle == style) "" else style
    }

    // ── Step 7 ────────────────────────────────────────────────────────────────

    fun onTimelineEventAdd(event: TimelineEventData) {
        timelineEvents = (timelineEvents + event).sortedBy { it.time }
    }

    fun onTimelineEventRemove(idx: Int) {
        timelineEvents = timelineEvents.filterIndexed { i, _ -> i != idx }
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    fun submit() {
        viewModelScope.launch {
            _createState.value = CreateWeddingState.Loading
            val adminGuestId = GuestSessionManager.getGuestId(context)
            val weddingTimestamp = selectedDateMillis?.let { datePart ->
                datePart + selectedTimeHour * 3_600_000L + selectedTimeMinute * 60_000L
            } ?: 0L

            // Upload cover image first (use a temp placeholder ID, swap after weddingId is known)
            val coverImageUrl = coverImageUri?.let { uri ->
                photoStorageService.uploadPhoto(
                    weddingId = "temp_${System.currentTimeMillis()}",
                    photoId = "cover",
                    uri = uri,
                ).getOrNull() ?: ""
            } ?: ""

            createWeddingUseCase(
                name = coupleName.trim(),
                date = weddingTimestamp,
                location = venue.trim(),
                adminGuestId = adminGuestId,
                coverImageUrl = coverImageUrl,
                menu = menuCourses.filter { it.items.isNotEmpty() },
                dressCode = DressCodeData(style = dressCodeStyle),
                timeline = timelineEvents,
            ).onSuccess { weddingId ->
                WeddingSessionManager.saveWeddingId(context, weddingId)
                _createState.value = CreateWeddingState.Success(weddingId)
            }.onFailure {
                _createState.value = CreateWeddingState.Error(it.message ?: "Something went wrong")
            }
        }
    }
}

sealed class CreateWeddingState {
    object Idle : CreateWeddingState()
    object Loading : CreateWeddingState()
    data class Success(val weddingId: String) : CreateWeddingState()
    data class Error(val message: String) : CreateWeddingState()
}
