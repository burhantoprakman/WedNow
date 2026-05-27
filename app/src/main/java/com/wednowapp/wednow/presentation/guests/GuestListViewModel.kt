package com.wednowapp.wednow.presentation.guests

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wednowapp.wednow.core.navigation.Screen
import com.wednowapp.wednow.domain.model.Guest
import com.wednowapp.wednow.domain.usecase.GetCurrentGuestUseCase
import com.wednowapp.wednow.domain.usecase.GetGuestsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GuestListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getGuestsUseCase: GetGuestsUseCase,
    getCurrentGuestUseCase: GetCurrentGuestUseCase
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.GuestList.ARG])

    /** Null = first Firestore snapshot not yet received (loading). Empty list = no guests. */
    val guests: StateFlow<List<Guest>?> = getGuestsUseCase(weddingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    val currentGuest: StateFlow<Guest?> = getCurrentGuestUseCase(weddingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
