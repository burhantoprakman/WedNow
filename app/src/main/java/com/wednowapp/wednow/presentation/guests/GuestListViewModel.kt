package com.wednowapp.wednow.presentation.guests

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.wednowapp.wednow.core.navigation.Screen
import com.wednowapp.wednow.domain.model.Guest
import com.wednowapp.wednow.domain.model.GuestGroup
import com.wednowapp.wednow.domain.model.GuestMember
import com.wednowapp.wednow.domain.model.GuestRole
import com.wednowapp.wednow.domain.model.MemberRole
import com.wednowapp.wednow.domain.repository.GuestGroupRepository
import com.wednowapp.wednow.domain.usecase.AddGuestGroupUseCase
import com.wednowapp.wednow.domain.usecase.DeleteGuestGroupUseCase
import com.wednowapp.wednow.domain.usecase.GetCurrentGuestUseCase
import com.wednowapp.wednow.domain.usecase.GetGuestGroupsUseCase
import com.wednowapp.wednow.domain.usecase.GetGuestsUseCase
import com.wednowapp.wednow.domain.usecase.UpdateGuestGroupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GuestListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getGuestsUseCase: GetGuestsUseCase,
    private val getCurrentGuestUseCase: GetCurrentGuestUseCase,
    private val getGuestGroupsUseCase: GetGuestGroupsUseCase,
    private val addGuestGroupUseCase: AddGuestGroupUseCase,
    private val updateGuestGroupUseCase: UpdateGuestGroupUseCase,
    private val deleteGuestGroupUseCase: DeleteGuestGroupUseCase,
    private val guestGroupRepository: GuestGroupRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val weddingId: String = checkNotNull(savedStateHandle[Screen.GuestList.ARG])

    // ── Read-only state ───────────────────────────────────────────────────────

    val guests: StateFlow<List<Guest>?> = getGuestsUseCase(weddingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    val currentGuest: StateFlow<Guest?> = getCurrentGuestUseCase(weddingId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** groupId → GuestGroup, for looking up family members per guest. */
    val guestGroupsById: StateFlow<Map<String, GuestGroup>> = getGuestGroupsUseCase(weddingId)
        .map { groups -> groups.associateBy { it.id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    /** True when the current device user is an admin or co-admin. */
    val isPrivileged: StateFlow<Boolean> = getCurrentGuestUseCase(weddingId)
        .map { it?.role == GuestRole.ADMIN || it?.role == GuestRole.COADMIN }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // ── Admin check ───────────────────────────────────────────────────────────

    private val _isAdmin = MutableStateFlow(false)

    /**
     * True when the guest has admin/co-admin role AND is signed in via Firebase Auth.
     * Used to gate management actions behind SignInBottomSheet.
     */
    val isAuthenticatedAdmin: Boolean
        get() = _isAdmin.value && FirebaseAuth.getInstance().currentUser != null

    // ── Management action state ───────────────────────────────────────────────

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

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        viewModelScope.launch {
            val guest = getCurrentGuestUseCase(weddingId).first()
            _isAdmin.value = guest?.role == GuestRole.ADMIN || guest?.role == GuestRole.COADMIN
        }

        // Backfill inviteTokens reverse-lookup entries for groups that existed
        // before this feature was added. Runs once per session when groups first arrive.
        viewModelScope.launch {
            val existingGroups = getGuestGroupsUseCase(weddingId).first()
            guestGroupRepository.backfillInviteTokens(existingGroups)
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
            val currentGroups = guestGroupsById.value
            val token = if (isNew) generateToken() else {
                currentGroups[groupId]?.inviteToken ?: generateToken()
            }
            val link = "https://wednow.app/invite/$token"
            val existingGroup = currentGroups[groupId]
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
                rsvpStatus = if (isNew) null else existingGroup?.rsvpStatus,
                invitationOpened = if (isNew) false else existingGroup?.invitationOpened ?: false,
                createdAt = if (isNew) System.currentTimeMillis() else existingGroup?.createdAt
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
