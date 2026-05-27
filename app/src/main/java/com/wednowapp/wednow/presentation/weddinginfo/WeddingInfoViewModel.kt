package com.wednowapp.wednow.presentation.weddinginfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wednowapp.wednow.core.navigation.Screen
import com.wednowapp.wednow.domain.model.GuestRole
import com.wednowapp.wednow.domain.model.Wedding
import com.wednowapp.wednow.domain.usecase.GetCurrentGuestUseCase
import com.wednowapp.wednow.domain.usecase.GetWeddingByIdUseCase
import com.wednowapp.wednow.domain.usecase.UpdateWeddingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WeddingInfoState {
    object Loading : WeddingInfoState()
    data class Success(val wedding: Wedding) : WeddingInfoState()
    data class Error(val message: String) : WeddingInfoState()
}

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    object Saved : SaveState()
    data class Error(val message: String) : SaveState()
}

@HiltViewModel
class WeddingInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWeddingByIdUseCase: GetWeddingByIdUseCase,
    private val updateWeddingUseCase: UpdateWeddingUseCase,
    private val getCurrentGuestUseCase: GetCurrentGuestUseCase,
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.WeddingInfo.ARG])

    private val _state = MutableStateFlow<WeddingInfoState>(WeddingInfoState.Loading)
    val state: StateFlow<WeddingInfoState> = _state.asStateFlow()

    private val _isPrivileged = MutableStateFlow(false)
    val isPrivileged: StateFlow<Boolean> = _isPrivileged.asStateFlow()

    private val _editMode = MutableStateFlow(false)
    val editMode: StateFlow<Boolean> = _editMode.asStateFlow()

    private val _draft = MutableStateFlow<Wedding?>(null)
    val draft: StateFlow<Wedding?> = _draft.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    init {
        loadWedding()
        observePrivilege()
    }

    fun retry() {
        _state.value = WeddingInfoState.Loading
        loadWedding()
    }

    private fun loadWedding() {
        viewModelScope.launch {
            getWeddingByIdUseCase(weddingId)
                .onSuccess { wedding ->
                    _state.value = if (wedding != null)
                        WeddingInfoState.Success(wedding)
                    else
                        WeddingInfoState.Error("Wedding not found")
                }
                .onFailure { err ->
                    _state.value = WeddingInfoState.Error(err.message ?: "Failed to load")
                }
        }
    }

    private fun observePrivilege() {
        viewModelScope.launch {
            getCurrentGuestUseCase(weddingId).collect { guest ->
                _isPrivileged.value =
                    guest?.role == GuestRole.ADMIN || guest?.role == GuestRole.COADMIN
            }
        }
    }

    fun enterEditMode() {
        val wedding = (state.value as? WeddingInfoState.Success)?.wedding ?: return
        _draft.value = wedding
        _editMode.value = true
        _saveState.value = SaveState.Idle
    }

    fun exitEditMode() {
        _draft.value = null
        _editMode.value = false
        _saveState.value = SaveState.Idle
    }

    fun updateDraft(wedding: Wedding) {
        _draft.value = wedding
    }

    fun saveChanges() {
        val draft = _draft.value ?: return
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            updateWeddingUseCase(draft)
                .onSuccess {
                    _state.value = WeddingInfoState.Success(draft)
                    _draft.value = null
                    _editMode.value = false
                    _saveState.value = SaveState.Saved
                }
                .onFailure {
                    _saveState.value = SaveState.Error(it.message ?: "Failed to save")
                }
        }
    }
}
