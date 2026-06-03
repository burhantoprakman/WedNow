package com.wednowapp.wednow.presentation.broadcast

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wednowapp.wednow.domain.model.Broadcast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcastScreen(
    onBack: () -> Unit,
    viewModel: BroadcastViewModel = hiltViewModel()
) {
    val broadcasts by viewModel.broadcasts.collectAsState()
    val canSend by viewModel.canSendBroadcast.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Announcements") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (canSend) {
                ExtendedFloatingActionButton(
                    onClick = viewModel::showDialog,
                    icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
                    text = { Text("Announce") }
                )
            }
        }
    ) { innerPadding ->
        if (broadcasts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "No announcements yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(broadcasts, key = { it.id }) { broadcast ->
                    BroadcastCard(broadcast)
                }
            }
        }
    }

    if (viewModel.isDialogVisible) {
        ComposeDialog(
            text = viewModel.composeText,
            onTextChange = viewModel::onComposeTextChange,
            onConfirm = viewModel::sendBroadcast,
            onDismiss = viewModel::dismissDialog,
            isSending = viewModel.isSending,
            error = viewModel.sendError
        )
    }
}

@Composable
private fun BroadcastCard(broadcast: Broadcast) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = broadcast.sentByName.ifBlank { "Anonymous" },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = formatTimestamp(broadcast.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = broadcast.message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun ComposeDialog(
    text: String,
    onTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isSending: Boolean,
    error: String?
) {
    AlertDialog(
        onDismissRequest = { if (!isSending) onDismiss() },
        icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
        title = { Text("New Announcement") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    placeholder = { Text("Write your announcement…") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 6,
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
                TextButton(
                    onClick = onConfirm,
                    enabled = text.isNotBlank()
                ) {
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

// ── Previews ──────────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Broadcast Card")
@androidx.compose.runtime.Composable
private fun BroadcastCardPreview() {
    com.wednowapp.wednow.ui.theme.WedNowTheme {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
        ) {
            BroadcastCard(
                broadcast = com.wednowapp.wednow.domain.model.Broadcast(
                    id = "1", sentByName = "Sophie & James",
                    message = "Welcome everyone! 🎉 We're so excited to celebrate this special day with you all. Please head to the garden terrace for the ceremony.",
                    timestamp = System.currentTimeMillis() - 1_800_000,
                )
            )
            BroadcastCard(
                broadcast = com.wednowapp.wednow.domain.model.Broadcast(
                    id = "2", sentByName = "James Walker",
                    message = "Dinner is now being served in the main hall. Please make your way there.",
                    timestamp = System.currentTimeMillis() - 300_000,
                )
            )
        }
    }
}
