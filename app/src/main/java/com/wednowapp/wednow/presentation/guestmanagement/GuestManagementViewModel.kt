package com.wednowapp.wednow.presentation.guestmanagement

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wednowapp.wednow.core.navigation.Screen
import com.wednowapp.wednow.domain.model.GuestGroup
import com.wednowapp.wednow.domain.model.GuestMember
import com.wednowapp.wednow.domain.model.GuestRole
import com.wednowapp.wednow.domain.model.MemberRole
import com.wednowapp.wednow.domain.usecase.AddGuestGroupUseCase
import com.wednowapp.wednow.domain.usecase.DeleteGuestGroupUseCase
import com.wednowapp.wednow.domain.usecase.GetCurrentGuestUseCase
import com.wednowapp.wednow.domain.usecase.GetGuestGroupsUseCase
import com.wednowapp.wednow.domain.usecase.UpdateGuestGroupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// ── Draft models used only inside the sheet ───────────────────────────────────

data class MemberDraft(
    val name: String = "",
    val role: String = MemberRole.ADULT,
    val plusOneAllowed: Boolean = false,
)

data class GroupDraft(
    val id: String = "",
    val familyName: String = "",
    val members: List<MemberDraft> = listOf(MemberDraft()),
)

// ── Action state ──────────────────────────────────────────────────────────────

sealed class GroupActionState {
    object Idle : GroupActionState()
    object Saving : GroupActionState()
    data class Error(val message: String) : GroupActionState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class GuestManagementViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getGuestGroupsUseCase: GetGuestGroupsUseCase,
    private val addGuestGroupUseCase: AddGuestGroupUseCase,
    private val updateGuestGroupUseCase: UpdateGuestGroupUseCase,
    private val deleteGuestGroupUseCase: DeleteGuestGroupUseCase,
    private val getCurrentGuestUseCase: GetCurrentGuestUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.GuestManagement.ARG])

    /** Null = first snapshot not yet arrived. */
    val groups: StateFlow<List<GuestGroup>?> = getGuestGroupsUseCase(weddingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _actionState = MutableStateFlow<GroupActionState>(GroupActionState.Idle)
    val actionState: StateFlow<GroupActionState> = _actionState.asStateFlow()

    // ── Local UI state ────────────────────────────────────────────────────────
    var expandedIds by mutableStateOf<Set<String>>(emptySet())
        private set

    var showSheet by mutableStateOf(false)
        private set

    var draft by mutableStateOf(GroupDraft())
        private set

    /** Non-null while editing an existing group. */
    var editingGroupId by mutableStateOf<String?>(null)
        private set

    /** Non-null while the QR dialog is open. */
    var qrTarget by mutableStateOf<GuestGroup?>(null)
        private set

    /** Non-null while delete confirmation is pending. */
    var pendingDeleteGroup by mutableStateOf<GuestGroup?>(null)
        private set

    init {
        viewModelScope.launch {
            val guest = getCurrentGuestUseCase(weddingId).first()
            _isAdmin.value = guest?.role == GuestRole.ADMIN || guest?.role == GuestRole.COADMIN
        }
    }

    // ── Expand / collapse ─────────────────────────────────────────────────────

    fun toggleExpand(groupId: String) {
        expandedIds = if (groupId in expandedIds) expandedIds - groupId else expandedIds + groupId
    }

    // ── Sheet ─────────────────────────────────────────────────────────────────

    fun openAddSheet() {
        draft = GroupDraft()
        editingGroupId = null
        showSheet = true
    }

    fun openEditSheet(group: GuestGroup) {
        draft = GroupDraft(
            id = group.id,
            familyName = group.familyName,
            members = group.members.map { MemberDraft(it.name, it.role, it.plusOneAllowed) }
                .ifEmpty { listOf(MemberDraft()) },
        )
        editingGroupId = group.id
        showSheet = true
    }

    fun dismissSheet() {
        showSheet = false
        _actionState.value = GroupActionState.Idle
    }

    // ── Draft mutations ───────────────────────────────────────────────────────

    fun onFamilyNameChange(value: String) {
        draft = draft.copy(familyName = value)
    }

    fun onMemberNameChange(index: Int, value: String) {
        draft = draft.copy(
            members = draft.members.toMutableList()
                .also { it[index] = it[index].copy(name = value) })
    }

    fun onMemberRoleChange(index: Int, role: String) {
        draft = draft.copy(members = draft.members.toMutableList().also {
            it[index] = it[index].copy(
                role = role,
                plusOneAllowed = if (role == MemberRole.CHILD) false else it[index].plusOneAllowed
            )
        })
    }

    fun onMemberPlusOneChange(index: Int, value: Boolean) {
        draft = draft.copy(
            members = draft.members.toMutableList()
                .also { it[index] = it[index].copy(plusOneAllowed = value) })
    }

    fun addMember() {
        draft = draft.copy(members = draft.members + MemberDraft())
    }

    fun removeMember(index: Int) {
        if (draft.members.size <= 1) return
        draft = draft.copy(members = draft.members.toMutableList().also { it.removeAt(index) })
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    fun saveGroup() {
        if (draft.familyName.isBlank()) return
        val validMembers = draft.members.filter { it.name.isNotBlank() }
        if (validMembers.isEmpty()) return
        viewModelScope.launch {
            _actionState.value = GroupActionState.Saving
            val isNew = editingGroupId == null
            val groupId = if (isNew) UUID.randomUUID().toString() else editingGroupId!!
            val token = if (isNew) generateToken() else {
                groups.value?.find { it.id == groupId }?.inviteToken ?: generateToken()
            }
            val link = "https://wednow.app/invite/$token"
            val group = GuestGroup(
                id = groupId,
                weddingId = weddingId,
                familyName = draft.familyName.trim(),
                inviteToken = token,
                invitationLink = link,
                members = validMembers.map {
                    GuestMember(
                        it.name.trim(),
                        it.role,
                        it.plusOneAllowed
                    )
                },
                rsvpStatus = if (isNew) null else groups.value?.find { it.id == groupId }?.rsvpStatus,
                invitationOpened = if (isNew) false else groups.value?.find { it.id == groupId }?.invitationOpened
                    ?: false,
                createdAt = if (isNew) System.currentTimeMillis() else groups.value?.find { it.id == groupId }?.createdAt
                    ?: System.currentTimeMillis(),
            )
            val result = if (isNew) addGuestGroupUseCase(group) else updateGuestGroupUseCase(group)
            _actionState.value = result.fold(
                onSuccess = { showSheet = false; GroupActionState.Idle },
                onFailure = { GroupActionState.Error(it.message ?: "Failed to save") },
            )
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    fun requestDelete(group: GuestGroup) {
        pendingDeleteGroup = group
    }

    fun confirmDelete() {
        val group = pendingDeleteGroup ?: return
        pendingDeleteGroup = null
        viewModelScope.launch {
            deleteGuestGroupUseCase(weddingId, group.id)
        }
    }

    fun cancelDelete() {
        pendingDeleteGroup = null
    }

    // ── QR dialog ─────────────────────────────────────────────────────────────

    fun showQr(group: GuestGroup) {
        qrTarget = group
    }

    fun dismissQr() {
        qrTarget = null
    }

    fun resetActionState() {
        _actionState.value = GroupActionState.Idle
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun eventQrContent(): String = "https://wednow.app/join/$weddingId"

    private fun generateToken(): String =
        UUID.randomUUID().toString().replace("-", "").take(6).uppercase()
}
