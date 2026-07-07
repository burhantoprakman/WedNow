package com.wednowapp.wednow.presentation.guestmanagement

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
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
import com.wednowapp.wednow.domain.model.GuestGroup
import com.wednowapp.wednow.domain.model.MemberRole
import com.wednowapp.wednow.domain.model.RSVPStatus
import com.wednowapp.wednow.presentation.auth.LocalAuthViewModel
import com.wednowapp.wednow.presentation.auth.SignInBottomSheet
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
import java.io.File

// ── Entry point ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestManagementScreen(
    onBack: () -> Unit,
    /** Non-null during onboarding — shows a "Continue →" bar at the bottom. */
    onContinue: (() -> Unit)? = null,
    viewModel: GuestManagementViewModel = hiltViewModel(),
) {
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // ── Auth gate ─────────────────────────────────────────────────────────────
    val authViewModel = LocalAuthViewModel.current
    var showSignIn by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    /**
     * Run [action] immediately if the admin is already authenticated,
     * otherwise store it and open the sign-in sheet.
     */
    fun gatedAction(action: () -> Unit) {
        if (viewModel.isAuthenticatedAdmin) action()
        else {
            pendingAction = action; showSignIn = true
        }
    }

    val isOnboarding = onContinue != null
    // Extra bottom padding so list content isn't hidden behind the Continue bar
    val listBottomPad = if (isOnboarding) 88.dp else Spacing.xxl

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ivory),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            // ── Header ────────────────────────────────────────────────────────
            GuestManagementHeader(
                groupCount = groups?.size ?: 0,
                isAdmin = isAdmin,
                isOnboarding = isOnboarding,
                onBack = onBack,
                onAdd = { gatedAction { viewModel.openAddSheet() } },
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.screenHorizontal,
                    end = Spacing.screenHorizontal,
                    top = Spacing.md,
                    bottom = listBottomPad,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                // Onboarding hint banner
                if (isOnboarding) {
                    item(key = "onboarding_hint") {
                        OnboardingHintBanner()
                        Spacer(Modifier.height(Spacing.xs))
                    }
                }

                // Event QR card
                item(key = "event_qr") {
                    EventQrCard(
                        content = viewModel.eventQrContent(),
                        onShare = {
                            shareText(
                                context,
                                viewModel.eventQrContent(),
                                "Wedding Event QR"
                            )
                        },
                    )
                    Spacer(Modifier.height(Spacing.xs))
                }

                when {
                    groups == null -> item { LoadingGroupsState() }
                    groups!!.isEmpty() -> item {
                        EmptyGroupsState(
                            isAdmin = isAdmin,
                            onAdd = { gatedAction { viewModel.openAddSheet() } })
                    }

                    else -> items(groups!!, key = { it.id }) { group ->
                        GuestGroupCard(
                            group = group,
                            expanded = group.id in viewModel.expandedIds,
                            isAdmin = isAdmin,
                            onToggle = { viewModel.toggleExpand(group.id) },
                            onShare = { shareInvitation(context, group) },
                            onShowQr = { viewModel.showQr(group) },
                            onEdit = { gatedAction { viewModel.openEditSheet(group) } },
                            onDelete = { gatedAction { viewModel.requestDelete(group) } },
                        )
                    }
                }
            }
        }

        // ── Sticky Continue bar (onboarding only) ─────────────────────────────
        if (isOnboarding) {
            OnboardingContinueBar(
                hasGroups = (groups?.size ?: 0) > 0,
                onContinue = onContinue!!,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        // ── Dialogs & sheets ──────────────────────────────────────────────────

        if (viewModel.qrTarget != null) {
            QrCodeDialog(
                group = viewModel.qrTarget!!,
                onDismiss = viewModel::dismissQr,
                onShare = { shareInvitation(context, viewModel.qrTarget!!) },
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

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun GuestManagementHeader(
    groupCount: Int,
    isAdmin: Boolean,
    isOnboarding: Boolean,
    onBack: () -> Unit,
    onAdd: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = WarmGray700,
            )
        }
        Column(modifier = Modifier
            .weight(1f)
            .padding(start = 4.dp)) {
            Text(
                text = if (isOnboarding) "Add Your Guests" else "Guests",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = WarmGray800,
            )
            Text(
                text = if (isOnboarding) "Personalise each invitation" else
                    if (groupCount > 0) "$groupCount ${if (groupCount == 1) "group" else "groups"}" else "",
                style = MaterialTheme.typography.bodySmall,
                color = WarmGray400,
            )
        }
        if (isAdmin) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Gold)
                    .clickable(onClick = onAdd),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add group",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ── Onboarding hint banner ────────────────────────────────────────────────────

@Composable
private fun OnboardingHintBanner() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ChampagneLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Add families and couples to generate personalised invitations for each group",
                style = MaterialTheme.typography.bodySmall,
                color = GoldDeep,
            )
        }
    }
}

// ── Onboarding continue bar ───────────────────────────────────────────────────

@Composable
private fun OnboardingContinueBar(
    hasGroups: Boolean,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Ivory.copy(alpha = 0.97f))
            .navigationBarsPadding()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.White),
        ) {
            Text(
                text = "Continue to Wedding →",
                style = MaterialTheme.typography.labelLarge,
            )
        }
        if (!hasGroups) {
            TextButton(
                onClick = onContinue,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "Skip for now",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmGray400,
                )
            }
        }
    }
}

// ── Event QR card ─────────────────────────────────────────────────────────────

@Composable
private fun EventQrCard(
    content: String,
    onShare: () -> Unit,
) {
    val qrBitmap = rememberQrBitmap(content, size = 256)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.cardMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            // QR preview
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, WarmGray100, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Event QR",
                        modifier = Modifier.size(60.dp),
                    )
                } else {
                    Icon(Icons.Default.QrCode, null, Modifier.size(32.dp), WarmGray300)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Event QR Code",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = WarmGray800,
                )
                Text(
                    text = "For bulk invitations & venue cards",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmGray400,
                    maxLines = 2,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = Gold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Share button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ChampagneLight)
                    .clickable(onClick = onShare),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Share, null, Modifier.size(16.dp), GoldDeep)
            }
        }
    }
}

// ── Group card (expandable) ───────────────────────────────────────────────────

@Composable
private fun GuestGroupCard(
    group: GuestGroup,
    expanded: Boolean,
    isAdmin: Boolean,
    onToggle: () -> Unit,
    onShare: () -> Unit,
    onShowQr: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val arrowAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(280),
        label = "arrow",
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            // ── Collapsed header row ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = Spacing.cardMd, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Icon circle
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

                // Name + member count
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.familyName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = WarmGray800,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${group.members.size} ${if (group.members.size == 1) "guest" else "guests"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmGray400,
                    )
                }

                // RSVP badge
                RsvpBadge(status = group.rsvpStatus)
                Spacer(Modifier.width(Spacing.sm))

                // Share button (quick access even when collapsed)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(WarmGray50)
                        .clickable(onClick = onShare),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Share, null, Modifier.size(14.dp), WarmGray500)
                }
                Spacer(Modifier.width(6.dp))

                // Expand arrow
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = WarmGray400,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(arrowAngle),
                )
            }

            // ── Expanded section ──────────────────────────────────────────────
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(280)),
                exit = shrinkVertically(animationSpec = tween(280)),
            ) {
                Column {
                    HorizontalDivider(thickness = 0.5.dp, color = WarmGray100)

                    // Member list
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.cardMd, vertical = Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        group.members.forEach { member ->
                            MemberRow(member = member)
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = Spacing.cardMd),
                        thickness = 0.5.dp,
                        color = WarmGray100,
                    )

                    // Invite link chip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.cardMd, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(GoldLight.copy(alpha = 0.35f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = group.invitationLink,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = GoldDeep,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    // Action row (admin only)
                    if (isAdmin) {
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

@Composable
private fun MemberRow(member: com.wednowapp.wednow.domain.model.GuestMember) {
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
            text = member.name,
            style = MaterialTheme.typography.bodySmall,
            color = WarmGray700,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = member.role,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = WarmGray400,
        )
        if (member.plusOneAllowed) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(BlushLight)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "+1",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = BlushDeep,
                )
            }
        }
    }
}

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
            modifier = Modifier.weight(1f)
        )
        ActionChip(
            label = "QR Code",
            icon = Icons.Default.QrCode,
            tint = GoldDeep,
            bg = GoldLight.copy(alpha = 0.4f),
            onClick = onShowQr,
            modifier = Modifier.weight(1f)
        )
        ActionChip(
            label = "Edit",
            icon = Icons.Default.Edit,
            tint = WarmGray600,
            bg = WarmGray50,
            onClick = onEdit,
            modifier = Modifier.weight(1f)
        )
        // Delete — no label, just icon, red tint
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
                modifier = Modifier.size(16.dp)
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
            color = tint
        )
    }
}

// ── RSVP badge ────────────────────────────────────────────────────────────────

@Composable
private fun RsvpBadge(status: String?) {
    val (label, bg, fg) = when (status) {
        RSVPStatus.GOING -> Triple("Going", Color(0xFFE8F5E8), SuccessSage)
        RSVPStatus.NOT_GOING -> Triple("Declined", ErrorRoseLight, ErrorRose)
        RSVPStatus.MAYBE -> Triple("Maybe", GoldLight.copy(alpha = 0.4f), GoldDeep)
        else -> Triple("Pending", WarmGray100, WarmGray500)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            ),
            color = fg
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
                    // Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
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

                    // QR code
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

                    // Invite link
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

                    // Share button
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
                color = WarmGray800
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
            // Title
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

            // Family name
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

            // Members section
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

            // Error
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

            // Save button
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
                    disabledContainerColor = WarmGray200
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

        // Role toggle + plus one
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
            // +1 toggle (adults only)
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

// ── Empty / loading states ────────────────────────────────────────────────────

@Composable
private fun LoadingGroupsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Gold, strokeWidth = 2.dp, modifier = Modifier.size(32.dp))
    }
}

@Composable
private fun EmptyGroupsState(isAdmin: Boolean, onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Icon(Icons.Default.Groups, null, Modifier.size(48.dp), WarmGray300)
        Text(
            text = "No guests yet",
            style = MaterialTheme.typography.titleSmall,
            color = WarmGray500,
            textAlign = TextAlign.Center,
        )
        if (isAdmin) {
            Text(
                text = "Add families and couples to generate personalized invitations",
                style = MaterialTheme.typography.bodySmall,
                color = WarmGray300,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Spacing.xl),
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onAdd,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = Color.White
                ),
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add First Guest")
            }
        }
    }
}

// ── QR bitmap helper ──────────────────────────────────────────────────────────

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

private fun shareInvitation(context: Context, group: GuestGroup) {
    val memberNames = group.members
        .filter { it.role == MemberRole.ADULT }
        .joinToString(" & ") { it.name.substringBefore(" ") }
        .ifBlank { group.familyName }

    val caption = buildString {
        appendLine("💛 You're invited, $memberNames!")
        appendLine()
        appendLine("We would love to celebrate our special day with you.")
        appendLine()
        appendLine("Scan the QR code or use your personal link to RSVP:")
        appendLine(group.invitationLink)
        append("Code: ${group.inviteToken}")
    }

    val qrBitmap = generateQrBitmap(group.invitationLink, size = 512)
    val imageUri = qrBitmap?.let { saveGroupQrToCache(context, it, group.id) }

    if (imageUri != null) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, caption)
            putExtra(Intent.EXTRA_SUBJECT, "Wedding Invitation for ${group.familyName}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Invitation"))
    } else {
        // Fallback to text if QR generation fails
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, caption)
            putExtra(Intent.EXTRA_SUBJECT, "Wedding Invitation for ${group.familyName}")
        }
        context.startActivity(Intent.createChooser(intent, "Share Invitation"))
    }
}

private fun saveGroupQrToCache(
    context: Context,
    bitmap: Bitmap,
    groupId: String
): android.net.Uri? =
    runCatching {
        val dir = File(context.cacheDir, "invitations").also { it.mkdirs() }
        val file = File(dir, "qr_$groupId.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }.getOrNull()

private fun shareText(context: Context, text: String, title: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, title))
}

// ── Previews ──────────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    name = "Guest Group Card – Expanded"
)
@androidx.compose.runtime.Composable
private fun GuestGroupCardExpandedPreview() {
    com.wednowapp.wednow.ui.theme.WedNowTheme {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
        ) {
            GuestGroupCard(
                group = com.wednowapp.wednow.domain.model.GuestGroup(
                    id = "g1", weddingId = "w1",
                    familyName = "Walker Family",
                    inviteToken = "WLK001",
                    members = listOf(
                        com.wednowapp.wednow.domain.model.GuestMember(
                            "Sophie Walker",
                            com.wednowapp.wednow.domain.model.MemberRole.ADULT,
                            rsvpStatus = "going"
                        ),
                        com.wednowapp.wednow.domain.model.GuestMember(
                            "James Walker",
                            com.wednowapp.wednow.domain.model.MemberRole.ADULT,
                            rsvpStatus = "maybe"
                        ),
                        com.wednowapp.wednow.domain.model.GuestMember(
                            "Lily Walker",
                            com.wednowapp.wednow.domain.model.MemberRole.CHILD,
                            rsvpStatus = null
                        ),
                    ),
                    rsvpStatus = "going",
                ),
                expanded = true,
                isAdmin = true,
                onToggle = {}, onShare = {}, onShowQr = {}, onEdit = {}, onDelete = {},
            )
            GuestGroupCard(
                group = com.wednowapp.wednow.domain.model.GuestGroup(
                    id = "g2", weddingId = "w1",
                    familyName = "Davis Family",
                    inviteToken = "DVS002",
                    members = listOf(
                        com.wednowapp.wednow.domain.model.GuestMember(
                            "Emma Davis",
                            com.wednowapp.wednow.domain.model.MemberRole.ADULT,
                            rsvpStatus = "not_going"
                        ),
                    ),
                    rsvpStatus = "not_going",
                ),
                expanded = false,
                isAdmin = true,
                onToggle = {}, onShare = {}, onShowQr = {}, onEdit = {}, onDelete = {},
            )
        }
    }
}
