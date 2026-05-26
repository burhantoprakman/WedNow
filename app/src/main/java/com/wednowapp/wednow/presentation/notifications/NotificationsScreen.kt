package com.wednowapp.wednow.presentation.notifications

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wednowapp.wednow.domain.model.AppNotification
import com.wednowapp.wednow.domain.model.NotificationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val readIds by viewModel.readIds.collectAsState()
    val canSend by viewModel.canSendNotification.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
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
                                contentDescription = "Mark all as read"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (canSend) {
                ExtendedFloatingActionButton(
                    onClick = viewModel::showDialog,
                    icon = { Icon(Icons.Default.NotificationAdd, contentDescription = null) },
                    text = { Text("Notify") }
                )
            }
        }
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications, key = { it.id }) { notification ->
                    val isRead = notification.id in readIds
                    NotificationCard(
                        notification = notification,
                        isRead = isRead,
                        onClick = { if (!isRead) viewModel.markAsRead(notification.id) }
                    )
                }
            }
        }
    }

    if (viewModel.isDialogVisible) {
        ComposeNotificationDialog(
            title = viewModel.titleInput,
            body = viewModel.bodyInput,
            onTitleChange = viewModel::onTitleChange,
            onBodyChange = viewModel::onBodyChange,
            onConfirm = viewModel::sendNotification,
            onDismiss = viewModel::dismissDialog,
            isSending = viewModel.isSending,
            canConfirm = viewModel.canConfirmSend,
            error = viewModel.sendError
        )
    }
}

@Composable
private fun NotificationCard(
    notification: AppNotification,
    isRead: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isRead) MaterialTheme.colorScheme.surfaceVariant
        else MaterialTheme.colorScheme.primaryContainer,
        animationSpec = tween(durationMillis = 400),
        label = "card_color"
    )
    val contentColor = if (isRead) MaterialTheme.colorScheme.onSurfaceVariant
    else MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isRead) 0.dp else 3.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            NotificationTypeIcon(
                type = notification.type,
                tint = contentColor,
                isRead = isRead
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isRead) FontWeight.Normal else FontWeight.SemiBold,
                        color = contentColor,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isRead) {
                        BadgedBox(badge = { Badge() }) {}
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = if (isRead) 0.75f else 1f)
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    if (notification.sentByName.isNotBlank()) {
                        Text(
                            text = notification.sentByName,
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        text = formatTimestamp(notification.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationTypeIcon(type: String, tint: androidx.compose.ui.graphics.Color, isRead: Boolean) {
    val icon: ImageVector = when (type) {
        NotificationType.ANNOUNCEMENT -> Icons.Default.Campaign
        NotificationType.RSVP_UPDATE -> Icons.Default.HowToReg
        NotificationType.GUEST_JOINED -> Icons.Default.Groups
        NotificationType.SYSTEM -> Icons.Default.Settings
        else -> Icons.Default.Notifications
    }
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(22.dp),
        tint = tint.copy(alpha = if (isRead) 0.6f else 1f)
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "No notifications yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ComposeNotificationDialog(
    title: String,
    body: String,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isSending: Boolean,
    canConfirm: Boolean,
    error: String?
) {
    AlertDialog(
        onDismissRequest = { if (!isSending) onDismiss() },
        icon = { Icon(Icons.Default.NotificationAdd, contentDescription = null) },
        title = { Text("Send Notification") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Title") },
                    placeholder = { Text("e.g. Venue change") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSending
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = body,
                    onValueChange = onBodyChange,
                    label = { Text("Message") },
                    placeholder = { Text("Details for your guests…") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5,
                    enabled = !isSending
                )
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            if (isSending) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                TextButton(onClick = onConfirm, enabled = canConfirm) {
                    Text("Send")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSending) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTimestamp(millis: Long): String =
    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(millis))
