package com.maritsa.app.presentation.identity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maritsa.app.core.identity.IdentityManager
import com.maritsa.app.domain.model.Identity
import com.maritsa.app.domain.model.ProtectedAction
import com.maritsa.app.domain.model.WeddingMembership
import com.maritsa.app.domain.usecase.GetWeddingMembershipsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity-scoped ViewModel that exposes [Identity] state and coordinates the
 * protected-action gate for the entire navigation graph.
 *
 * Provided to the Compose tree via [LocalIdentityViewModel] so any screen can:
 *  • Read [identity] for display purposes.
 *  • Call [requestProtectedAction] to gate sign-in-required operations.
 *  • Call [consumePendingAction] after sign-in completes to resume the action.
 *
 * ── Protected-action UX flow ─────────────────────────────────────────────────
 *  1. Screen calls  requestProtectedAction(action)
 *     • Returns true  → identity is already USER; screen proceeds immediately.
 *     • Returns false → action is stored; screen shows the sign-in sheet.
 *  2. After sign-in succeeds the screen / NavGraph calls
 *     consumePendingAction() and handles the returned action.
 */
@HiltViewModel
class IdentityViewModel @Inject constructor(
    private val identityManager: IdentityManager,
    private val getWeddingMembershipsUseCase: GetWeddingMembershipsUseCase,
) : ViewModel() {

    /** The active identity.  Never null — starts as GUEST. */
    val identity: StateFlow<Identity> = identityManager.identity
        .stateIn(viewModelScope, SharingStarted.Eagerly, identityManager.currentIdentity)

    val isAuthenticated: Boolean get() = identityManager.isAuthenticated
    val currentIdentityId: String get() = identityManager.currentIdentityId

    // ── Protected-action gate ─────────────────────────────────────────────────

    private val _pendingAction = MutableStateFlow<ProtectedAction?>(null)
    val pendingAction: StateFlow<ProtectedAction?> = _pendingAction.asStateFlow()

    /**
     * Returns true if the action can proceed immediately (USER identity).
     * Returns false and stores [action] as pending when authentication is required.
     */
    fun requestProtectedAction(action: ProtectedAction): Boolean {
        if (identityManager.isAuthenticated) return true
        _pendingAction.value = action
        return false
    }

    /**
     * Retrieves and clears the pending action.
     * Call this immediately after a successful sign-in to resume the original intent.
     */
    fun consumePendingAction(): ProtectedAction? {
        val action = _pendingAction.value
        _pendingAction.value = null
        return action
    }

    // ── Multi-wedding switcher ────────────────────────────────────────────────

    private val _memberships = MutableStateFlow<List<WeddingMembership>>(emptyList())
    val memberships: StateFlow<List<WeddingMembership>> = _memberships.asStateFlow()

    /** Refreshes the list of weddings the current identity belongs to. */
    fun refreshMemberships() {
        viewModelScope.launch {
            _memberships.value = getWeddingMembershipsUseCase()
        }
    }
}
