package com.wednowapp.wednow.presentation.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wednowapp.wednow.R
import com.wednowapp.wednow.domain.model.AppNotification
import com.wednowapp.wednow.domain.model.NotificationType
import com.wednowapp.wednow.ui.theme.Champagne
import com.wednowapp.wednow.ui.theme.ChampagneLight
import com.wednowapp.wednow.ui.theme.Gold
import com.wednowapp.wednow.ui.theme.GoldDeep
import com.wednowapp.wednow.ui.theme.Ivory
import com.wednowapp.wednow.ui.theme.WarmGray200
import com.wednowapp.wednow.ui.theme.WarmGray300
import com.wednowapp.wednow.ui.theme.WarmGray400
import com.wednowapp.wednow.ui.theme.WarmGray500
import com.wednowapp.wednow.ui.theme.WarmGray600
import com.wednowapp.wednow.ui.theme.WarmGray800
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Script font ───────────────────────────────────────────────────────────────

private val _nGfProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)
private val DancingScriptN = FontFamily(
    Font(GoogleFont("Dancing Script"), _nGfProvider, FontWeight.SemiBold),
)

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    navController: NavController? = null,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val sections by viewModel.sections.collectAsState()
    val readIds by viewModel.readIds.collectAsState()
    val canSend by viewModel.canSendAnnouncement.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    val isEmpty = sections.all { it.items.isEmpty() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0xFFCDD8E8),
                        0.50f to Color(0xFFE8D8C0),
                        1.00f to Color(0xFFF5EDE0),
                    ),
                    start = Offset(1400f, 0f),
                    end = Offset(0f, 2400f),
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Notifications",
                                style = TextStyle(
                                    fontFamily = DancingScriptN,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 28.sp,
                                    color = WarmGray800,
                                ),
                            )
                            if (unreadCount > 0) {
                                Spacer(Modifier.width(8.dp))
                                UnreadPill(count = unreadCount)
                            }
                        }
                    },
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            ChampagneLight.copy(alpha = 0.92f),
                                            Champagne.copy(alpha = 0.55f),
                                        )
                                    )
                                )
                                .border(0.8.dp, Gold.copy(alpha = 0.28f), CircleShape)
                                .clickable(onClick = onBack),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = GoldDeep,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    },
                    actions = {
                        if (unreadCount > 0) {
                            TextButton(
                                onClick = viewModel::markAllAsRead,
                                modifier = Modifier.padding(end = 8.dp),
                            ) {
                                Text(
                                    text = "Mark all read",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GoldDeep,
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                    ),
                )
            },
            floatingActionButton = {
                if (canSend) {
                    Box(
                        modifier = Modifier
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = GoldDeep.copy(alpha = 0.22f),
                                spotColor = GoldDeep.copy(alpha = 0.28f),
                            )
                            .clip(RoundedCornerShape(28.dp))
                            .background(Brush.linearGradient(listOf(Gold, GoldDeep)))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = viewModel::openComposer,
                            )
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = "Announce",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = Color.White,
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            if (isEmpty) {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    sections.forEach { section ->
                        item(key = "header_${section.label}") {
                            DateSectionHeader(label = section.label)
                        }
                        items(section.items, key = { it.id }) { notification ->
                            val isRead = notification.id in readIds
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(260)) + expandVertically(tween(260)),
                            ) {
                                NotificationCard(
                                    notification = notification,
                                    isRead = isRead,
                                    onClick = {
                                        if (!isRead) viewModel.markAsRead(notification)
                                        viewModel.resolveDeepLinkRoute(notification)?.let { route ->
                                            navController?.navigate(route)
                                        }
                                    },
                                )
                            }
                        }
                        item(key = "gap_${section.label}") { Spacer(Modifier.height(4.dp)) }
                    }
                }
            }
        }
    }

    if (viewModel.isComposerVisible) {
        AnnouncementComposer(
            title = viewModel.titleInput,
            body = viewModel.bodyInput,
            onTitle = viewModel::onTitleChange,
            onBody = viewModel::onBodyChange,
            onSend = viewModel::sendAnnouncement,
            onDismiss = viewModel::closeComposer,
            isSending = viewModel.isSending,
            canSend = viewModel.canSend,
            error = viewModel.sendError,
        )
    }
}

// ── Notification Card ─────────────────────────────────────────────────────────

@Composable
fun NotificationCard(
    notification: AppNotification,
    isRead: Boolean,
    onClick: () -> Unit,
) {
    val isHighPriority = notification.type in NotificationType.highPriority

    val cardBg = if (isRead)
        Brush.verticalGradient(listOf(Color(0xFFF8F5F0), Color(0xFFF2ECE4)))
    else
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.95f),
                Color.White.copy(alpha = 0.82f)
            )
        )

    val cardBorder = if (isRead)
        Brush.verticalGradient(
            listOf(
                WarmGray200.copy(alpha = 0.55f),
                WarmGray200.copy(alpha = 0.25f)
            )
        )
    else if (isHighPriority)
        Brush.linearGradient(listOf(Gold.copy(alpha = 0.65f), Gold.copy(alpha = 0.28f)))
    else
        Brush.linearGradient(
            listOf(
                Gold.copy(alpha = 0.40f),
                Gold.copy(alpha = 0.14f),
                Color.White.copy(alpha = 0.50f)
            )
        )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .shadow(
                elevation = if (isRead) 0.dp else if (isHighPriority) 6.dp else 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Gold.copy(alpha = if (isRead) 0f else 0.12f),
                spotColor = Gold.copy(alpha = if (isRead) 0f else 0.08f),
            )
            .background(cardBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = if (isRead) 14.dp else 11.dp,
                    end = 14.dp,
                    top = if (isHighPriority && !isRead) 0.dp else 14.dp,
                    bottom = 14.dp,
                ),
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Notification type icon inside champagne circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isRead)
                                Brush.radialGradient(
                                    listOf(Color(0xFFF0EBE3), Color(0xFFE8E0D6).copy(alpha = 0.8f))
                                )
                            else
                                Brush.radialGradient(
                                    listOf(ChampagneLight, Champagne.copy(alpha = 0.72f))
                                )
                        )
                        .border(
                            width = 0.8.dp,
                            color = if (isRead) WarmGray200.copy(alpha = 0.55f) else Gold.copy(alpha = 0.32f),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    NotifTypeIcon(
                        type = notification.type,
                        tint = if (isRead) WarmGray400 else Gold,
                    )
                }

                // Text content
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (isRead) FontWeight.Normal else FontWeight.SemiBold,
                                fontSize = 13.sp,
                            ),
                            color = if (isRead) WarmGray500 else WarmGray800,
                            modifier = Modifier.weight(1f),
                        )
                        if (!isRead) {
                            Spacer(Modifier.width(8.dp))
                            // Gold unread dot
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Gold)
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = notification.body,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = if (isRead) WarmGray400 else WarmGray600,
                    )
                    Spacer(Modifier.height(8.dp))

                    // Sender + timestamp row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (notification.senderName.isNotBlank()) {
                            Text(
                                text = notification.senderName,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (isRead) WarmGray300 else WarmGray400,
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = formatTimestamp(notification.createdAt),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (isRead) WarmGray300 else WarmGray400,
                        )
                    }
                }
            }
        }
    }
}

// ── Unread badge / pill ───────────────────────────────────────────────────────

@Composable
fun UnreadBadge(count: Int, modifier: Modifier = Modifier) {
    if (count <= 0) return
    UnreadPill(count = count, modifier = modifier)
}

@Composable
private fun UnreadPill(count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(Gold, GoldDeep)))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = Color.White,
        )
    }
}

// ── Announcement Composer ─────────────────────────────────────────────────────

@Composable
fun AnnouncementComposer(
    title: String,
    body: String,
    onTitle: (String) -> Unit,
    onBody: (String) -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit,
    isSending: Boolean,
    canSend: Boolean,
    error: String?,
) {
    AlertDialog(
        onDismissRequest = { if (!isSending) onDismiss() },
        icon = {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(ChampagneLight, Champagne.copy(alpha = 0.65f))
                        )
                    )
                    .border(0.8.dp, Gold.copy(alpha = 0.28f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(22.dp),
                )
            }
        },
        title = {
            Text(
                text = "Send Announcement",
                style = TextStyle(
                    fontFamily = DancingScriptN,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    color = WarmGray800,
                ),
            )
        },
        text = {
            Column {
                Text(
                    text = "This will be sent as a push notification to all wedding guests.",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmGray400,
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitle,
                    label = { Text("Heading") },
                    placeholder = { Text("e.g. Ceremony starts soon") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSending,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = WarmGray200,
                        focusedLabelColor = GoldDeep,
                        unfocusedLabelColor = WarmGray400,
                    ),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = body,
                    onValueChange = onBody,
                    label = { Text("Message") },
                    placeholder = { Text("Details for your guests…") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5,
                    enabled = !isSending,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = WarmGray200,
                        focusedLabelColor = GoldDeep,
                        unfocusedLabelColor = WarmGray400,
                    ),
                )
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFBA3A3A),
                    )
                }
            }
        },
        confirmButton = {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Gold,
                    strokeWidth = 2.dp,
                )
            } else {
                TextButton(onClick = onSend, enabled = canSend) {
                    Text(
                        text = "Send",
                        color = if (canSend) GoldDeep else WarmGray300,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSending) {
                Text(text = "Cancel", color = WarmGray400)
            }
        },
        containerColor = Ivory,
        iconContentColor = Gold,
        titleContentColor = WarmGray800,
        textContentColor = WarmGray500,
        shape = RoundedCornerShape(24.dp),
    )
}

// ── Private helpers ───────────────────────────────────────────────────────────

@Composable
private fun DateSectionHeader(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = WarmGray400,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(
                    Brush.horizontalGradient(listOf(WarmGray200, Color.Transparent))
                ),
        )
    }
}

@Composable
private fun NotifTypeIcon(type: String, tint: Color) {
    val icon: ImageVector = when (type) {
        NotificationType.WEDDING_UPDATE -> Icons.Default.Update
        NotificationType.ANNOUNCEMENT -> Icons.Default.Campaign
        NotificationType.PHOTO_LIKE -> Icons.Default.Favorite
        NotificationType.PHOTO_COMMENT -> Icons.Default.Edit
        NotificationType.GUESTBOOK_LIKE -> Icons.Default.Favorite
        NotificationType.GUEST_JOINED -> Icons.Default.Groups
        NotificationType.RSVP_UPDATE -> Icons.Default.HowToReg
        NotificationType.SYSTEM -> Icons.Default.Settings
        else -> Icons.Default.NotificationsActive
    }
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = tint,
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                ChampagneLight.copy(alpha = 0.88f),
                                Champagne.copy(alpha = 0.42f)
                            )
                        )
                    )
                    .border(1.dp, Gold.copy(alpha = 0.22f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = Gold.copy(alpha = 0.55f),
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "All quiet here",
                style = TextStyle(
                    fontFamily = DancingScriptN,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 26.sp,
                    color = WarmGray800,
                ),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Wedding updates and important\nmoments will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmGray400,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(14.dp))

            // Gold ornamental divider
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(0.5.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Gold.copy(alpha = 0.35f))
                            )
                        )
                )
                Spacer(Modifier.width(6.dp))
                Text("✦", fontSize = 8.sp, color = Gold.copy(alpha = 0.45f))
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(0.5.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Gold.copy(alpha = 0.35f), Color.Transparent)
                            )
                        )
                )
            }
        }
    }
}

private fun formatTimestamp(millis: Long): String {
    if (millis <= 0L) return ""
    return SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(millis))
}

// ── Previews ──────────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    name = "Notification Card – Announcement (Unread)"
)
@Composable
private fun NotificationCardAnnouncementPreview() {
    com.wednowapp.wednow.ui.theme.WedNowTheme {
        NotificationCard(
            notification = com.wednowapp.wednow.domain.model.AppNotification(
                id = "1", type = com.wednowapp.wednow.domain.model.NotificationType.ANNOUNCEMENT,
                senderName = "Sophie & James",
                title = "Ceremony Update 💍",
                body = "The ceremony has moved to the garden terrace. Please arrive by 11 AM.",
                createdAt = System.currentTimeMillis() - 3_600_000,
            ),
            isRead = false,
            onClick = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    name = "Notification Card – Photo Like (Read)"
)
@Composable
private fun NotificationCardPhotoLikePreview() {
    com.wednowapp.wednow.ui.theme.WedNowTheme {
        NotificationCard(
            notification = com.wednowapp.wednow.domain.model.AppNotification(
                id = "2", type = com.wednowapp.wednow.domain.model.NotificationType.PHOTO_LIKE,
                senderName = "Emma Davis",
                title = "Emma liked your photo",
                body = "Your photo from the ceremony received a like.",
                createdAt = System.currentTimeMillis() - 7_200_000,
            ),
            isRead = true,
            onClick = {},
        )
    }
}
