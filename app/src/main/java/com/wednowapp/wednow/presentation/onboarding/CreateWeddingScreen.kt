package com.wednowapp.wednow.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wednowapp.wednow.ui.components.*
import com.wednowapp.wednow.ui.theme.Spacing

@Composable
fun CreateWeddingScreen(
    onWeddingCreated: (weddingId: String) -> Unit,
    onJoinWeddingClick: () -> Unit,
    viewModel: CreateWeddingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading = state is CreateWeddingState.Loading

    LaunchedEffect(state) {
        when (state) {
            is CreateWeddingState.Success ->
                onWeddingCreated((state as CreateWeddingState.Success).weddingId)
            is CreateWeddingState.Error ->
                snackbarHostState.showSnackbar((state as CreateWeddingState.Error).message)
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
            // Hero header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.background,
                            )
                        )
                    )
                    .padding(
                        horizontal = Spacing.screenHorizontal,
                        vertical = Spacing.xxxl,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        text = "Create Your Wedding",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = "Set up your special day and invite your guests.",
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
                    value = viewModel.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Wedding Name",
                    placeholder = "e.g. Sarah & James",
                    leadingIcon = Icons.Default.Favorite,
                    singleLine = true,
                )

                WedNowTextField(
                    value = viewModel.date,
                    onValueChange = viewModel::onDateChange,
                    label = "Wedding Date",
                    placeholder = "e.g. June 15, 2026",
                    leadingIcon = Icons.Default.CalendarMonth,
                    singleLine = true,
                )

                WedNowTextField(
                    value = viewModel.location,
                    onValueChange = viewModel::onLocationChange,
                    label = "Location",
                    placeholder = "e.g. Grand Ballroom, New York",
                    leadingIcon = Icons.Default.LocationOn,
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
                        text = "Create Wedding",
                        onClick = viewModel::submit,
                        enabled = viewModel.isSubmitEnabled,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    WedNowOutlinedButton(
                        text = "Join an Existing Wedding",
                        onClick = onJoinWeddingClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(Spacing.xl))
            }
        }
    }
}
