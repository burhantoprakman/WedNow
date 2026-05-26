package com.wednowapp.wednow.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wednowapp.wednow.ui.components.*
import com.wednowapp.wednow.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinWeddingScreen(
    onWeddingJoined: (weddingId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: JoinWeddingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading = state is JoinWeddingState.Loading

    LaunchedEffect(state) {
        when (state) {
            is JoinWeddingState.Success ->
                onWeddingJoined((state as JoinWeddingState.Success).weddingId)
            is JoinWeddingState.Error ->
                snackbarHostState.showSnackbar((state as JoinWeddingState.Error).message)
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
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Hero header with back button
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
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = Spacing.sm, top = Spacing.sm),
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
                        .padding(
                            horizontal = Spacing.screenHorizontal,
                            vertical = Spacing.xxxl,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Join a Wedding",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = "Enter the code shared by the couple.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                WedNowTextField(
                    value = viewModel.weddingCode,
                    onValueChange = viewModel::onWeddingCodeChange,
                    label = "Wedding Code",
                    placeholder = "Paste the code here",
                    leadingIcon = Icons.Default.Key,
                    singleLine = true,
                )

                WedNowTextField(
                    value = viewModel.guestName,
                    onValueChange = viewModel::onGuestNameChange,
                    label = "Your Name (optional)",
                    placeholder = "e.g. Alex Johnson",
                    leadingIcon = Icons.Default.Person,
                    singleLine = true,
                )

                Spacer(Modifier.height(Spacing.xs))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp,
                        )
                    }
                } else {
                    WedNowPrimaryButton(
                        text = "Join Wedding",
                        onClick = viewModel::submit,
                        enabled = viewModel.isSubmitEnabled,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(Spacing.xl))
            }
        }
    }
}
