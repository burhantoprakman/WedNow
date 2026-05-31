package com.wednowapp.wednow.presentation.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wednowapp.wednow.domain.model.AppNotification
import com.wednowapp.wednow.domain.model.NotificationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Notifications")
                        if (unreadCount > 0) {
                            Spacer(Modifier.width(8.dp))
                            UnreadBadge(count = unreadCount)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        IconButton(onClick = viewModel::markAllAsRead) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark all as read",
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (canSend) {
                ExtendedFloatingActionButton(
                    onClick = viewModel::openComposer,
                    icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
                    text = { Text("Announce") },
                )
            }
        },
    ) { innerPadding ->

        val isEmpty = sections.all { it.items.isEmpty() }

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
                    // Date header
                    item(key = "header_${section.label}") {
                        DateSectionHeader(label = section.label)
                    }

                    items(section.items, key = { it.id }) { notification ->
                        val isRead = notification.id in readIds

                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically(),
                        ) {
                            NotificationCard(
                                notification = notification,
                                isRead = isRead,
                                onClick = {
                                    if (!isRead) viewModel.markAsRead(notification)
                                    // Navigate to target screen if deep link available
                                    viewModel.resolveDeepLinkRoute(notification)?.let { route ->
                                        navController?.navigate(route)
                                    }
                                },
                            )
                        }
                    }

                    // Small gap between sections
                    item(key = "gap_${section.label}") { Spacer(Modifier.height(4.dp)) }
                }
            }
        }
    }

    // ── Announcement Composer dialog ──────────────────────────────────────────

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
    val containerColor by animateColorAsState(
        targetValue = if (isRead) MaterialTheme.colorScheme.surfaceVariant
        else MaterialTheme.colorScheme.primaryContainer,
        animationSpec = tween(400),
        label = "card_color",
    )
    val contentColor = if (isRead) MaterialTheme.colorScheme.onSurfaceVariant
    else MaterialTheme.colorScheme.onPrimaryContainer

    val isHighPriority = notification.type in NotificationType.highPriority

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isRead) 0.dp else if (isHighPriority) 4.dp else 2.dp,
        ),
    ) {
        // High-priority top accent strip
        if (isHighPriority && !isRead) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }

        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            NotificationIcon(
                type = notification.type,
                tint = contentColor,
                isRead = isRead,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isRead) FontWeight.Normal else FontWeight.SemiBold,
                        color = contentColor,
                        modifier = Modifier.weight(1f),
                    )
                    if (!isRead) {
                        BadgedBox(badge = { Badge() }) {}
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Read",
                            modifier = Modifier.size(14.dp),
                            tint = contentColor.copy(alpha = 0.3f),
                        )
                    }
                }

                Spacer(Modifier.height(3.dp))
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = if (isRead) 0.75f else 1f),
                )
                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (notification.senderName.isNotBlank()) {
                        Text(
                            text = notification.senderName,
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.65f),
                        )
                    }
                    Text(
                        text = formatTimestamp(notification.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.55f),
                    )
                }
            }
        }
    }
}

// ── UnreadBadge ───────────────────────────────────────────────────────────────

@Composable
fun UnreadBadge(count: Int, modifier: Modifier = Modifier) {
    if (count <= 0) return
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.error,
        tonalElevation = 2.dp,
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onError,
        )
    }
}

// ── AnnouncementComposer ──────────────────────────────────────────────────────

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
        icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
        title = { Text("Send Announcement") },
        text = {
            Column {
                Text(
                    text = "Announcements are sent as push notifications to all guests.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitle,
                    label = { Text("Heading") },
                    placeholder = { Text("e.g. Ceremony starts soon") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSending,
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
                )
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            if (isSending) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                TextButton(onClick = onSend, enabled = canSend) { Text("Send") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSending) { Text("Cancel") }
        },
    )
}

// ── Private helpers ───────────────────────────────────────────────────────────

@Composable
private fun DateSectionHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 6.dp),
    )
}

@Composable
private fun NotificationIcon(type: String, tint: Color, isRead: Boolean) {
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
        modifier = Modifier.size(22.dp),
        tint = tint.copy(alpha = if (isRead) 0.55f else 1f),
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No notifications yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Wedding updates and important moments\nwill appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            )
        }
    }
}

private fun formatTimestamp(millis: Long): String {
    if (millis <= 0L) return ""
    return SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(millis))
}
