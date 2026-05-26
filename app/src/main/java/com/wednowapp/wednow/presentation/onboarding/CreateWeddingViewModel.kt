package com.wednowapp.wednow.presentation.onboarding

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.core.session.WeddingSessionManager
import com.wednowapp.wednow.domain.usecase.CreateWeddingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateWeddingViewModel @Inject constructor(
    private val createWeddingUseCase: CreateWeddingUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var name by mutableStateOf("")
        private set
    var date by mutableStateOf("")
        private set
    var location by mutableStateOf("")
        private set

    private val _state = MutableStateFlow<CreateWeddingState>(CreateWeddingState.Idle)
    val state: StateFlow<CreateWeddingState> = _state.asStateFlow()

    val isSubmitEnabled: Boolean
        get() = name.isNotBlank() && date.isNotBlank() && location.isNotBlank()

    fun onNameChange(value: String) { name = value }
    fun onDateChange(value: String) { date = value }
    fun onLocationChange(value: String) { location = value }

    fun submit() {
        viewModelScope.launch {
            _state.value = CreateWeddingState.Loading
            val adminGuestId = GuestSessionManager.getGuestId(context)
            createWeddingUseCase(
                name = name.trim(),
                date = date.trim(),
                location = location.trim(),
                adminGuestId = adminGuestId
            ).onSuccess { weddingId ->
                WeddingSessionManager.saveWeddingId(context, weddingId)
                _state.value = CreateWeddingState.Success(weddingId)
            }.onFailure { error ->
                _state.value = CreateWeddingState.Error(error.message ?: "Failed to create wedding")
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
