package com.wednowapp.wednow.presentation.guests

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.wednowapp.wednow.domain.model.Guest
import com.wednowapp.wednow.domain.model.GuestGroup
import com.wednowapp.wednow.domain.model.GuestMember
import com.wednowapp.wednow.domain.model.GuestRole
import com.wednowapp.wednow.domain.model.MemberRole
import com.wednowapp.wednow.domain.model.RSVPStatus
import com.wednowapp.wednow.domain.model.Wedding
import com.wednowapp.wednow.presentation.auth.LocalAuthViewModel
import com.wednowapp.wednow.presentation.auth.SignInBottomSheet
import com.wednowapp.wednow.presentation.share.RealInvitationCard
import com.wednowapp.wednow.presentation.share.buildInvitationPdfBytes
import com.wednowapp.wednow.ui.components.AvatarCircle
import com.wednowapp.wednow.ui.theme.BlushDeep
import com.wednowapp.wednow.ui.theme.BlushLight
import com.wednowapp.wednow.ui.theme.ChampagneLight
import com.wednowapp.wednow.ui.theme.ErrorRose
import com.wednowapp.wednow.ui.theme.ErrorRoseLight
import com.wednowapp.wednow.ui.theme.Gold
import com.wednowapp.wednow.ui.theme.GoldDeep
import com.wednowapp.wednow.ui.theme.GoldLight
import com.wednowapp.wednow.ui.theme.Ivory
import com.wednowapp.wednow.ui.theme.Spacing
import com.wednowapp.wednow.ui.theme.SuccessSage
import com.wednowapp.wednow.ui.theme.WarmGray100
import com.wednowapp.wednow.ui.theme.WarmGray200
import com.wednowapp.wednow.ui.theme.WarmGray300
import com.wednowapp.wednow.ui.theme.WarmGray400
import com.wednowapp.wednow.ui.theme.WarmGray50
import com.wednowapp.wednow.ui.theme.WarmGray500
import com.wednowapp.wednow.ui.theme.WarmGray600
import com.wednowapp.wednow.ui.theme.WarmGray700
import com.wednowapp.wednow.ui.theme.WarmGray800
import com.wednowapp.wednow.ui.theme.WarmWhite
import kotlinx.coroutines.launch
import java.io.File

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun GuestListScreen(
    onBack: () -> Unit,
    onNavigateToDm: (guestId: String) -> Unit = {},
    viewModel: GuestListViewModel = hiltViewModel(),
) {
    val guests by viewModel.guests.collectAsStateWithLifecycle()
    val currentGuest by viewModel.currentGuest.collectAsStateWithLifecycle()
    val guestGroupsById by viewModel.guestGroupsById.collectAsStateWithLifecycle()
    val isPrivileged by viewModel.isPrivileged.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val wedding by viewModel.wedding.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var activeShareGroup by remember { mutableStateOf<GuestGroup?>(null) }
    val captureGraphicsLayer = rememberGraphicsLayer()
    val captureScope = rememberCoroutineScope()

    // ── Auth gate ─────────────────────────────────────────────────────────────
    val authViewModel = LocalAuthViewModel.current
    var showSignIn by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun gatedAction(action: () -> Unit) {
        if (viewModel.isAuthenticatedAdmin) action()
        else {
            pendingAction = action; showSignIn = true
        }
    }

    val guestList = guests ?: emptyList()
    val groups =
        remember(guestGroupsById) { guestGroupsById.values.sortedBy { it.familyName.lowercase() } }
    val ungrouped = remember(guestList, guestGroupsById) {
        guestList.filter { it.groupId == null || it.groupId !in guestGroupsById }
            .sortedBy { it.name.lowercase() }
    }

    val allMemberStatuses = remember(groups, ungrouped) {
        groups.flatMap { g -> g.members.map { it.rsvpStatus } } +
                ungrouped.map { it.rsvpStatus }
    }
    val going = remember(allMemberStatuses) { allMemberStatuses.count { it == RSVPStatus.GOING } }
    val maybe = remember(allMemberStatuses) { allMemberStatuses.count { it == RSVPStatus.MAYBE } }
    val notGoing =
        remember(allMemberStatuses) { allMemberStatuses.count { it == RSVPStatus.NOT_GOING } }
    val pending = remember(allMemberStatuses) { allMemberStatuses.count { it.isNullOrBlank() } }
    val totalCount = groups.sumOf { it.members.size } + ungrouped.size

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            ChampagneLight.copy(alpha = 0.28f),
                            Ivory,
                            Ivory,
                            Ivory
                        )
                    )
                )
        )
        GuestListFlorals()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            GuestTopBar(onBack = onBack)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = if (isPrivileged) 96.dp else 24.dp),
            ) {
                item { GuestListHeader(guestCount = totalCount) }

                when {
                    guests == null -> item { LoadingGuestsState() }

                    groups.isEmpty() && ungrouped.isEmpty() -> item { EmptyGuestState(Modifier.fillMaxWidth()) }

                    else -> {
                        item {
                            RSVPStatusSummary(
                                going = going,
                                maybe = maybe,
                                notGoing = notGoing,
                                pending = pending,
                            )
                        }

                        // ── Family groups ──────────────────────────────────────────
                        if (groups.isNotEmpty()) {
                            item(key = "hdr_groups") {
                                SectionHeader(label = "Family Groups", count = groups.size)
                            }
                            items(groups, key = { it.id }) { group ->
                                val isCurrentGroup = guestList
                                    .find { it.id == currentGuest?.id }
                                    ?.groupId == group.id
                                val groupGuests = remember(guestList, group.id) {
                                    guestList.filter { it.groupId == group.id }
                                }
                                GuestGroupCard(
                                    group = group,
                                    expanded = group.id in viewModel.expandedIds,
                                    isCurrentGroup = isCurrentGroup,
                                    isPrivileged = isPrivileged,
                                    groupGuests = groupGuests,
                                    currentGuestId = currentGuest?.id,
                                    onNavigateToDm = onNavigateToDm,
                                    onToggle = { viewModel.toggleExpand(group.id) },
                                    onShare = { activeShareGroup = group },
                                    onShowQr = { gatedAction { viewModel.showQr(group) } },
                                    onEdit = { gatedAction { viewModel.openEditSheet(group) } },
                                    onDelete = { gatedAction { viewModel.requestDelete(group) } },
                                    modifier = Modifier.padding(
                                        horizontal = Spacing.screenHorizontal,
                                        vertical = 3.dp,
                                    ),
                                )
                            }
                            item(key = "sp_groups") { Spacer(Modifier.height(Spacing.xs)) }
                        }

                        // ── Individual guests (no group) ───────────────────────────
                        if (ungrouped.isNotEmpty()) {
                            item(key = "hdr_individual") {
                                SectionHeader(label = "Individual Guests", count = ungrouped.size)
                            }
                            items(ungrouped, key = { it.id }) { guest ->
                                IndividualGuestCard(
                                    guest = guest,
                                    isCurrentUser = guest.id == currentGuest?.id,
                                    onNavigateToDm = onNavigateToDm,
                                    modifier = Modifier.padding(
                                        horizontal = Spacing.screenHorizontal,
                                        vertical = 3.dp,
                                    ),
                                )
                            }
                            item(key = "sp_individual") { Spacer(Modifier.height(Spacing.xs)) }
                        }
                    }
                }
            }
        }

        // ── Add Guests FAB (admin / co-admin only) ────────────────────────────
        if (isPrivileged) {
            ExtendedFloatingActionButton(
                onClick = { gatedAction { viewModel.openAddSheet() } },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                    )
                },
                text = {
                    Text(
                        text = "Add Guests",
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = Spacing.screenHorizontal, bottom = Spacing.lg),
                containerColor = Gold,
                contentColor = Color.White,
            )
        }

        // ── Hidden invitation card — rendered in main window for reliable capture ──
        val captureGroup = activeShareGroup
        val captureWedding = wedding
        if (captureGroup != null && captureWedding != null) {
            val captureQr = rememberQrBitmap(captureGroup.invitationLink, size = 512)
            if (captureQr != null) {
                Box(modifier = Modifier.size(0.dp)) {
                    Box(
                        modifier = Modifier
                            .requiredWidth(400.dp)
                            .graphicsLayer { alpha = 0.002f }
                            .drawWithContent {
                                captureGraphicsLayer.record { this@drawWithContent.drawContent() }
                                drawLayer(captureGraphicsLayer)
                            },
                    ) {
                        RealInvitationCard(
                            wedding = captureWedding,
                            weddingId = captureWedding.id,
                            qrBitmap = captureQr,
                            deepLinkUrl = captureGroup.invitationLink,
                        )
                    }
                }
            }
        }

        // ── Dialogs & sheets ──────────────────────────────────────────────────

        if (activeShareGroup != null && wedding != null) {
            InvitationShareSheet(
                wedding = wedding!!,
                group = activeShareGroup!!,
                onShare = {
                    val group = activeShareGroup ?: return@InvitationShareSheet
                    captureScope.launch {
                        val bmp = captureGraphicsLayer.toImageBitmap().asAndroidBitmap()
                        val uri =
                            createGroupInvitationPdf(context, bmp, group.id, group.invitationLink)
                        if (uri != null) {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    "Wedding Invitation for ${group.familyName}"
                                )
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Invitation"))
                        }
                        activeShareGroup = null
                    }
                },
                onDismiss = { activeShareGroup = null },
            )
        }

        if (viewModel.qrTarget != null) {
            QrCodeDialog(
                group = viewModel.qrTarget!!,
                onDismiss = viewModel::dismissQr,
                onShare = {
                    val g = viewModel.qrTarget!!
                    viewModel.dismissQr()
                    activeShareGroup = g
                },
            )
        }

        if (viewModel.pendingDeleteGroup != null) {
            DeleteConfirmDialog(
                groupName = viewModel.pendingDeleteGroup!!.familyName,
                onConfirm = viewModel::confirmDelete,
                onDismiss = viewModel::cancelDelete,
            )
        }

        if (viewModel.showSheet) {
            AddEditGroupSheet(
                draft = viewModel.draft,
                isEditing = viewModel.editingGroupId != null,
                actionState = actionState,
                onFamilyNameChange = viewModel::onFamilyNameChange,
                onMemberNameChange = viewModel::onMemberNameChange,
                onMemberRoleChange = viewModel::onMemberRoleChange,
                onMemberPlusOneChange = viewModel::onMemberPlusOneChange,
                onAddMember = viewModel::addMember,
                onRemoveMember = viewModel::removeMember,
                onSave = viewModel::saveGroup,
                onDismiss = viewModel::dismissSheet,
            )
        }

        // ── Auth sign-in sheet (for unauthenticated admins / co-admins) ───────
        if (showSignIn) {
            SignInBottomSheet(
                authViewModel = authViewModel,
                reason = "Sign in to manage guests for this wedding.",
                onDismiss = {
                    showSignIn = false
                    pendingAction = null
                    authViewModel.clearError()
                },
                onSuccess = {
                    showSignIn = false
                    pendingAction?.invoke()
                    pendingAction = null
                },
            )
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun GuestTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(WarmGray50)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(20.dp),
                tint = WarmGray600,
            )
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun GuestListHeader(guestCount: Int) {
    val serifFamily = MaterialTheme.typography.displayLarge.fontFamily
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal)
            .padding(top = Spacing.sm, bottom = Spacing.sm),
    ) {
        Text(
            text  = "Wedding Guests",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Light),
            color = WarmGray800,
        )
        Spacer(Modifier.height(3.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                text  = "Celebrating together",
                style = TextStyle(
                    fontFamily = serifFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    color = WarmGray400
                ),
            )
            Box(modifier = Modifier
                .size(2.5.dp)
                .clip(CircleShape)
                .background(WarmGray200))
            Text(
                text = if (guestCount > 0) "$guestCount invited" else "",
                style = MaterialTheme.typography.bodySmall,
                color = WarmGray400,
            )
        }
        Spacer(Modifier.height(Spacing.md))
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(label: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal)
            .padding(top = Spacing.md, bottom = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(Gold.copy(alpha = 0.40f)))
        Spacer(Modifier.width(8.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Medium
            ),
            color = WarmGray500,
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = WarmGray300
        )
        Spacer(Modifier.width(Spacing.sm))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            WarmGray200,
                            WarmGray100,
                            Color.Transparent
                        )
                    )
                ),
        )
    }
}

// ── Loading state ─────────────────────────────────────────────────────────────

@Composable
private fun LoadingGuestsState() {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = Gold,
                strokeWidth = 2.dp
            )
            Text(
                text = "Loading guests…",
                style = MaterialTheme.typography.bodySmall,
                color = WarmGray400
            )
        }
    }
}

// ── RSVP summary ──────────────────────────────────────────────────────────────

@Composable
private fun RSVPStatusSummary(going: Int, maybe: Int, notGoing: Int, pending: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal)
            .padding(bottom = Spacing.md),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RSVPStatItem(count = going, label = "Attending", color = SuccessSage)
            VerticalDivider(
                modifier = Modifier.height(28.dp),
                color = WarmGray100,
                thickness = 1.dp
            )
            RSVPStatItem(count = maybe, label = "Maybe", color = GoldDeep)
            VerticalDivider(
                modifier = Modifier.height(28.dp),
                color = WarmGray100,
                thickness = 1.dp
            )
            RSVPStatItem(count = pending, label = "Pending", color = WarmGray400)
            VerticalDivider(
                modifier = Modifier.height(28.dp),
                color = WarmGray100,
                thickness = 1.dp
            )
            RSVPStatItem(count = notGoing, label = "Declined", color = ErrorRose)
        }
    }
}

@Composable
private fun RSVPStatItem(count: Int, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text  = count.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (count > 0) color else WarmGray200,
        )
        Text(
            text      = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 0.2.sp
            ),
            color = WarmGray400,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Unified group card ────────────────────────────────────────────────────────

@Composable
private fun GuestGroupCard(
    group: GuestGroup,
    expanded: Boolean,
    isCurrentGroup: Boolean,
    isPrivileged: Boolean,
    groupGuests: List<Guest>,
    currentGuestId: String?,
    onNavigateToDm: (String) -> Unit,
    onToggle: () -> Unit,
    onShare: () -> Unit,
    onShowQr: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val arrowAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(280),
        label = "arrow",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentGroup) ChampagneLight else WarmWhite,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // ── Header row ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = Spacing.cardMd, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ChampagneLight),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (group.members.size > 1) Icons.Default.Groups else Icons.Default.Person,
                        contentDescription = null,
                        tint = GoldDeep,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(Spacing.sm))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = group.familyName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = WarmGray800,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (isCurrentGroup) YouChip()
                    }
                    Text(
                        text = "${group.members.size} ${if (group.members.size == 1) "guest" else "guests"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmGray400,
                    )
                }

                GroupRsvpBadge(status = groupRsvpSummary(group))
                Spacer(Modifier.width(Spacing.sm))

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = WarmGray400,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(arrowAngle),
                )
            }

            // ── Expanded section ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(280)),
                exit = shrinkVertically(animationSpec = tween(280)),
            ) {
                Column {
                    HorizontalDivider(thickness = 0.5.dp, color = WarmGray100)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.cardMd, vertical = Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        group.members.forEach { member ->
                            val matchedGuest = groupGuests.find {
                                it.name.trim().equals(member.name.trim(), ignoreCase = true)
                            }
                            MemberRsvpRow(
                                member = member,
                                matchedGuest = matchedGuest,
                                currentGuestId = currentGuestId,
                                onNavigateToDm = onNavigateToDm,
                            )
                        }
                    }

                    // ── Admin action chips ─────────────────────────────────────
                    if (isPrivileged) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = Spacing.cardMd),
                            thickness = 0.5.dp,
                            color = WarmGray100,
                        )
                        GroupActionRow(
                            onShare = onShare,
                            onShowQr = onShowQr,
                            onEdit = onEdit,
                            onDelete = onDelete,
                        )
                    }
                }
            }
        }
    }
}

// ── Individual guest card (no group) ─────────────────────────────────────────

@Composable
private fun IndividualGuestCard(
    guest: Guest,
    isCurrentUser: Boolean,
    onNavigateToDm: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) ChampagneLight else WarmWhite,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.cardMd, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarCircle(
                name = guest.name.ifBlank { "?" },
                size = 40.dp,
                backgroundColor = if (isCurrentUser) ChampagneLight else BlushLight.copy(alpha = 0.7f),
                textColor = if (isCurrentUser) GoldDeep else BlushDeep,
            )
            Spacer(Modifier.width(Spacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = guest.name.ifBlank { "Anonymous" },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = WarmGray800,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (isCurrentUser) YouChip()
                    if (guest.role == GuestRole.ADMIN || guest.role == GuestRole.COADMIN) {
                        RoleIndicator(guest.role)
                    }
                }
                Spacer(Modifier.height(3.dp))
                RSVPBadge(rsvpStatus = guest.rsvpStatus)
            }

            if (!isCurrentUser && guest.id.isNotBlank()) {
                Spacer(Modifier.width(8.dp))
                DmIconButton(onClick = { onNavigateToDm(guest.id) })
            }
        }
    }
}

// ── Member row with RSVP ──────────────────────────────────────────────────────

@Composable
private fun MemberRsvpRow(
    member: GuestMember,
    matchedGuest: Guest? = null,
    currentGuestId: String? = null,
    onNavigateToDm: (String) -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (member.role == MemberRole.CHILD) BlushDeep else Gold),
        )
        Text(
            text = member.name.ifBlank { "Guest" },
            style = MaterialTheme.typography.bodySmall,
            color = WarmGray700,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = member.role,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = WarmGray400,
        )
        RSVPBadge(rsvpStatus = member.rsvpStatus)
        if (matchedGuest != null && matchedGuest.id != currentGuestId) {
            DmIconButton(onClick = { onNavigateToDm(matchedGuest.id) })
        }
    }
}

// ── DM icon button ────────────────────────────────────────────────────────────

@Composable
private fun DmIconButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(ChampagneLight)
            .border(0.7.dp, Gold.copy(alpha = 0.30f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Chat,
            contentDescription = "Send message",
            tint = GoldDeep,
            modifier = Modifier.size(14.dp),
        )
    }
}

// ── Admin action row ──────────────────────────────────────────────────────────

@Composable
private fun GroupActionRow(
    onShare: () -> Unit,
    onShowQr: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ActionChip(
            label = "Share",
            icon = Icons.Default.Share,
            tint = GoldDeep,
            bg = ChampagneLight,
            onClick = onShare,
            modifier = Modifier.weight(1f),
        )
        ActionChip(
            label = "QR Code",
            icon = Icons.Default.QrCode,
            tint = GoldDeep,
            bg = GoldLight.copy(alpha = 0.4f),
            onClick = onShowQr,
            modifier = Modifier.weight(1f),
        )
        ActionChip(
            label = "Edit",
            icon = Icons.Default.Edit,
            tint = WarmGray600,
            bg = WarmGray50,
            onClick = onEdit,
            modifier = Modifier.weight(1f),
        )
        // Delete — icon only, red tint
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ErrorRoseLight)
                .clickable(onClick = onDelete),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = ErrorRose,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun ActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    bg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, Modifier.size(13.dp), tint)
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = tint,
        )
    }
}

// ── Group RSVP summary badge ──────────────────────────────────────────────────

private fun groupRsvpSummary(group: GuestGroup): String? {
    val statuses = group.members.map { it.rsvpStatus }
    if (statuses.isEmpty()) return null
    val going = statuses.count { it == RSVPStatus.GOING }
    val notGoing = statuses.count { it == RSVPStatus.NOT_GOING }
    val maybe = statuses.count { it == RSVPStatus.MAYBE }
    return when {
        statuses.all { it.isNullOrBlank() } -> null
        going == statuses.size -> RSVPStatus.GOING
        notGoing == statuses.size -> RSVPStatus.NOT_GOING
        maybe == statuses.size -> RSVPStatus.MAYBE
        going > 0 -> "partial"
        else -> null
    }
}

@Composable
private fun GroupRsvpBadge(status: String?) {
    val (label, bg, fg) = when (status) {
        RSVPStatus.GOING -> Triple("All Going", Color(0xFFE8F5E8), SuccessSage)
        RSVPStatus.NOT_GOING -> Triple("Declined", ErrorRoseLight, ErrorRose)
        RSVPStatus.MAYBE -> Triple("Maybe", GoldLight.copy(alpha = 0.4f), GoldDeep)
        "partial" -> Triple("Partial", ChampagneLight, GoldDeep)
        else -> Triple("Pending", WarmGray100, WarmGray500)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            ),
            color = fg,
        )
    }
}

// ── RSVP badge (per member/guest) ─────────────────────────────────────────────

@Composable
fun RSVPBadge(rsvpStatus: String?, modifier: Modifier = Modifier) {
    val normalised = rsvpStatus?.takeIf { it.isNotBlank() }
    val label = when (normalised) {
        RSVPStatus.GOING     -> "Attending"
        RSVPStatus.MAYBE     -> "Maybe"
        RSVPStatus.NOT_GOING -> "Not Attending"
        else                 -> "Awaiting Reply"
    }
    val textColor = when (normalised) {
        RSVPStatus.GOING -> SuccessSage
        RSVPStatus.MAYBE     -> GoldDeep
        RSVPStatus.NOT_GOING -> ErrorRose
        else -> WarmGray300
    }
    val bgColor = when (normalised) {
        RSVPStatus.GOING -> Color(0xFFE8F5E8)
        RSVPStatus.MAYBE -> GoldLight.copy(alpha = 0.4f)
        RSVPStatus.NOT_GOING -> ErrorRoseLight
        else                 -> WarmGray50
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 0.1.sp
            ),
            color = textColor,
        )
    }
}

// ── You chip ──────────────────────────────────────────────────────────────────

@Composable
private fun YouChip() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Gold.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text  = "You",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            ),
            color = GoldDeep,
        )
    }
}

// ── Role indicator ────────────────────────────────────────────────────────────

@Composable
private fun RoleIndicator(role: String) {
    val (label, color) = when (role) {
        GuestRole.ADMIN   -> "Host" to Gold
        GuestRole.COADMIN -> "Co-host" to Gold.copy(alpha = 0.7f)
        else              -> return
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ChampagneLight)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp
            ),
            color = color,
        )
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyGuestState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = Spacing.xxl, horizontal = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Spacer(Modifier.height(Spacing.xl))
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(ChampagneLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.PeopleAlt,
                contentDescription = null,
                tint = Gold.copy(alpha = 0.45f),
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(Spacing.md))
        Text(
            text = "No guests yet",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Light),
            color = WarmGray700,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Guests will appear here\nonce they join the wedding",
            style = MaterialTheme.typography.bodyMedium,
            color = WarmGray400,
            textAlign = TextAlign.Center
        )
    }
}

// ── Background florals ────────────────────────────────────────────────────────

@Composable
private fun GuestListFlorals() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color(0xFFEDD9B8).copy(alpha = 0.15f),
            radius = 80.dp.toPx(),
            center = Offset(-15.dp.toPx(), 20.dp.toPx())
        )
        drawCircle(
            color = Color(0xFFEDD9B8).copy(alpha = 0.12f),
            radius = 65.dp.toPx(),
            center = Offset(size.width, size.height - 50.dp.toPx())
        )
    }
}

// ── Add / Edit sheet ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditGroupSheet(
    draft: GroupDraft,
    isEditing: Boolean,
    actionState: GroupActionState,
    onFamilyNameChange: (String) -> Unit,
    onMemberNameChange: (Int, String) -> Unit,
    onMemberRoleChange: (Int, String) -> Unit,
    onMemberPlusOneChange: (Int, Boolean) -> Unit,
    onAddMember: () -> Unit,
    onRemoveMember: (Int) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Ivory,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
                .navigationBarsPadding(),
        ) {
            Text(
                text = if (isEditing) "Edit Group" else "Add Guests",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = WarmGray800,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isEditing) "Update the group details below" else "Add a family, couple, or individual",
                style = MaterialTheme.typography.bodySmall,
                color = WarmGray400,
            )

            Spacer(Modifier.height(Spacing.lg))

            Text(
                "Group / Family Name",
                style = MaterialTheme.typography.labelMedium,
                color = WarmGray600
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = draft.familyName,
                onValueChange = onFamilyNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. The Johnson Family", color = WarmGray300) },
                shape = RoundedCornerShape(14.dp),
                colors = elegantTextFieldColors(),
                singleLine = true,
            )

            Spacer(Modifier.height(Spacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Members",
                    style = MaterialTheme.typography.labelMedium,
                    color = WarmGray600,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = onAddMember,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Icon(Icons.Default.PersonAddAlt, null, Modifier.size(14.dp), Gold)
                    Spacer(Modifier.width(4.dp))
                    Text("Add", style = MaterialTheme.typography.labelSmall, color = Gold)
                }
            }

            Spacer(Modifier.height(8.dp))

            draft.members.forEachIndexed { index, member ->
                MemberInputRow(
                    index = index,
                    member = member,
                    canRemove = draft.members.size > 1,
                    onNameChange = { onMemberNameChange(index, it) },
                    onRoleChange = { onMemberRoleChange(index, it) },
                    onPlusOne = { onMemberPlusOneChange(index, it) },
                    onRemove = { onRemoveMember(index) },
                )
                if (index < draft.members.lastIndex) Spacer(Modifier.height(10.dp))
            }

            if (actionState is GroupActionState.Error) {
                Spacer(Modifier.height(Spacing.sm))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ErrorRoseLight)
                        .padding(Spacing.sm),
                ) {
                    Text(
                        actionState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRose
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            Button(
                onClick = onSave,
                enabled = actionState !is GroupActionState.Saving && draft.familyName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = Color.White,
                    disabledContainerColor = WarmGray200,
                ),
            ) {
                if (actionState is GroupActionState.Saving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (isEditing) "Save Changes" else "Create Group",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(Modifier.height(Spacing.md))
        }
    }
}

// ── Member input row ──────────────────────────────────────────────────────────

@Composable
private fun MemberInputRow(
    index: Int,
    member: MemberDraft,
    canRemove: Boolean,
    onNameChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onPlusOne: (Boolean) -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WarmGray50)
            .padding(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = member.name,
                onValueChange = onNameChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "Full name",
                        color = WarmGray300,
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = elegantTextFieldColors(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
            )
            if (canRemove) {
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ErrorRoseLight)
                        .clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Close, null, Modifier.size(14.dp), ErrorRose)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RoleToggle(
                selected = member.role == MemberRole.ADULT,
                label = "Adult",
                onClick = { onRoleChange(MemberRole.ADULT) },
            )
            RoleToggle(
                selected = member.role == MemberRole.CHILD,
                label = "Child",
                onClick = { onRoleChange(MemberRole.CHILD) },
            )
            Spacer(Modifier.weight(1f))
            if (member.role == MemberRole.ADULT) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (member.plusOneAllowed) BlushLight else WarmGray100)
                        .clickable { onPlusOne(!member.plusOneAllowed) }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "+1",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (member.plusOneAllowed) BlushDeep else WarmGray400,
                    )
                }
            }
        }
    }
}

// ── Role toggle ───────────────────────────────────────────────────────────────

@Composable
private fun RoleToggle(selected: Boolean, label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) Gold else WarmGray100)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Color.White else WarmGray500,
        )
    }
}

// ── QR code dialog ────────────────────────────────────────────────────────────

@Composable
private fun QrCodeDialog(
    group: GuestGroup,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
) {
    val qrBitmap = rememberQrBitmap(group.invitationLink)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                modifier = Modifier
                    .padding(Spacing.xl)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Ivory),
                elevation = CardDefaults.cardElevation(8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(WarmGray100)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Close, null, Modifier.size(16.dp), WarmGray500)
                        }
                    }

                    Icon(
                        Icons.Default.Favorite,
                        null,
                        Modifier.size(20.dp),
                        Gold.copy(alpha = 0.7f)
                    )
                    Text(
                        text = group.familyName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = WarmGray800,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "${group.members.size} ${if (group.members.size == 1) "guest" else "guests"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmGray400,
                    )

                    Spacer(Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, WarmGray100, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Invitation QR",
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            CircularProgressIndicator(
                                color = Gold,
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(WarmGray50)
                            .padding(horizontal = Spacing.md, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = group.invitationLink,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = GoldDeep,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Text(
                        text = "Token: ${group.inviteToken}",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                        color = WarmGray400,
                    )

                    Spacer(Modifier.height(4.dp))

                    Button(
                        onClick = onShare,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Gold,
                            contentColor = Color.White
                        ),
                    ) {
                        Icon(Icons.Default.Share, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Share Invitation", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

// ── Delete confirm dialog ──────────────────────────────────────────────────────

@Composable
private fun DeleteConfirmDialog(
    groupName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Ivory,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                "Remove group?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = WarmGray800,
            )
        },
        text = {
            Text(
                "\"$groupName\" and all their invitation details will be permanently removed.",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmGray500,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Remove", color = ErrorRose, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = WarmGray500)
            }
        },
    )
}

// ── QR bitmap helpers ─────────────────────────────────────────────────────────

private fun generateQrBitmap(content: String, size: Int = 512): Bitmap? = runCatching {
    val hints = mapOf(EncodeHintType.MARGIN to 1)
    val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
    Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also { bmp ->
        for (x in 0 until size) for (y in 0 until size) {
            bmp.setPixel(
                x,
                y,
                if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            )
        }
    }
}.getOrNull()

@Composable
private fun rememberQrBitmap(content: String, size: Int = 512): Bitmap? =
    remember(content, size) { generateQrBitmap(content, size) }

// ── Sharing helpers ───────────────────────────────────────────────────────────

@Composable
private fun elegantTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Gold,
    unfocusedBorderColor = WarmGray200,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    cursorColor = Gold,
    focusedTextColor = WarmGray800,
    unfocusedTextColor = WarmGray700,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvitationShareSheet(
    wedding: Wedding,
    group: GuestGroup,
    onShare: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val qrBitmap = rememberQrBitmap(group.invitationLink, size = 512)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Ivory,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
                .navigationBarsPadding()
                .padding(bottom = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(
                text = "Invitation Preview",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = WarmGray800,
            )

            // Preview only — capture happens in the main window (see hidden card above)
            if (qrBitmap != null) {
                RealInvitationCard(
                    wedding = wedding,
                    weddingId = wedding.id,
                    qrBitmap = qrBitmap,
                    deepLinkUrl = group.invitationLink,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = Gold,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            Button(
                onClick = onShare,
                enabled = qrBitmap != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = Color.White,
                    disabledContainerColor = WarmGray200,
                ),
            ) {
                Icon(Icons.Default.Share, null, Modifier.size(16.dp))
                Spacer(Modifier.width(Spacing.sm))
                Text("Share Invitation", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

private fun createGroupInvitationPdf(
    context: Context,
    bitmap: Bitmap,
    groupId: String,
    joinUrl: String,
): android.net.Uri? = runCatching {
    val pdfBytes = buildInvitationPdfBytes(bitmap, joinUrl) ?: return@runCatching null
    val dir = File(context.cacheDir, "invitations").also { it.mkdirs() }
    val file = File(dir, "invitation_$groupId.pdf")
    file.writeBytes(pdfBytes)
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}.getOrNull()

// ── Previews ──────────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Guest List – Groups"
)
@Composable
private fun GuestListGroupsPreview() {
    com.wednowapp.wednow.ui.theme.WedNowTheme {
        // Preview scaffold (no viewmodel)
        val groups = mapOf(
            "grp1" to GuestGroup(
                id = "grp1", weddingId = "w1", familyName = "Walker Family",
                members = listOf(
                    GuestMember("Sophie Walker", MemberRole.ADULT, rsvpStatus = RSVPStatus.GOING),
                    GuestMember("James Walker", MemberRole.ADULT, rsvpStatus = RSVPStatus.MAYBE),
                    GuestMember("Lily Walker", MemberRole.CHILD, rsvpStatus = null),
                ),
            ),
            "grp2" to GuestGroup(
                id = "grp2", weddingId = "w1", familyName = "Brown Family",
                members = listOf(
                    GuestMember("Oliver Brown", MemberRole.ADULT, rsvpStatus = RSVPStatus.GOING),
                    GuestMember("Amelia Brown", MemberRole.ADULT, rsvpStatus = RSVPStatus.GOING),
                ),
            ),
        )
        val sortedGroups = groups.values.sortedBy { it.familyName.lowercase() }
        val allStatuses = sortedGroups.flatMap { g -> g.members.map { it.rsvpStatus } }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            ChampagneLight.copy(alpha = 0.28f),
                            Ivory,
                            Ivory,
                            Ivory
                        )
                    )
                )
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item { GuestListHeader(guestCount = allStatuses.size) }
                item {
                    RSVPStatusSummary(
                        going = allStatuses.count { it == RSVPStatus.GOING },
                        maybe = allStatuses.count { it == RSVPStatus.MAYBE },
                        notGoing = allStatuses.count { it == RSVPStatus.NOT_GOING },
                        pending = allStatuses.count { it.isNullOrBlank() },
                    )
                }
                item { SectionHeader(label = "Family Groups", count = sortedGroups.size) }
                items(sortedGroups, key = { it.id }) { group ->
                    GuestGroupCard(
                        group = group,
                        expanded = group.id == "grp1",
                        isCurrentGroup = group.id == "grp1",
                        isPrivileged = true,
                        groupGuests = emptyList(),
                        currentGuestId = null,
                        onNavigateToDm = {},
                        onToggle = {},
                        onShare = {},
                        onShowQr = {},
                        onEdit = {},
                        onDelete = {},
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 3.dp),
                    )
                }
            }
        }
    }
}
