package com.wednowapp.wednow.presentation.rsvp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wednowapp.wednow.domain.model.RSVPStatus
import com.wednowapp.wednow.ui.components.RsvpOptionCard
import com.wednowapp.wednow.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RSVPScreen(
    onBack: () -> Unit,
    viewModel: RSVPViewModel = hiltViewModel()
) {
    val currentGuest by viewModel.currentGuest.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading = submitState == RsvpSubmitState.Loading

    LaunchedEffect(submitState) {
        when (val state = submitState) {
            is RsvpSubmitState.Success -> {
                snackbarHostState.showSnackbar("RSVP saved!")
                viewModel.resetSubmitState()
            }
            is RsvpSubmitState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetSubmitState()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Header gradient section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.secondaryContainer,
                                MaterialTheme.colorScheme.background,
                            )
                        )
                    )
                    .padding(top = Spacing.xl, bottom = Spacing.xl)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = Spacing.sm, top = Spacing.xs)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screenHorizontal),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(Spacing.lg))
                    Text(
                        text = "Will you be there?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = "Let the couple know if you can make it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    currentGuest?.rsvpUpdatedAt?.let { ts ->
                        Spacer(Modifier.height(Spacing.xs))
                        Text(
                            text = "Last updated: ${formatTimestamp(ts)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Options
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screenHorizontal)
                    .padding(top = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                if (currentGuest == null && submitState !is RsvpSubmitState.Error) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp,
                        )
                    }
                } else {
                    val selectedStatus = currentGuest?.rsvpStatus

                    RsvpOptionCard(
                        icon = Icons.Default.CheckCircle,
                        label = "I'll be there!",
                        description = "Confirm your attendance",
                        selected = selectedStatus == RSVPStatus.GOING,
                        onClick = { if (!isLoading) viewModel.submit(RSVPStatus.GOING) },
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    RsvpOptionCard(
                        icon = Icons.Default.Cancel,
                        label = "Can't make it",
                        description = "You won't be attending",
                        selected = selectedStatus == RSVPStatus.NOT_GOING,
                        onClick = { if (!isLoading) viewModel.submit(RSVPStatus.NOT_GOING) },
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedContentColor = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    RsvpOptionCard(
                        icon = Icons.Default.HelpOutline,
                        label = "Maybe",
                        description = "You're still deciding",
                        selected = selectedStatus == RSVPStatus.MAYBE,
                        onClick = { if (!isLoading) viewModel.submit(RSVPStatus.MAYBE) },
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (isLoading) {
                        Spacer(Modifier.height(Spacing.sm))
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault()).format(Date(millis))
