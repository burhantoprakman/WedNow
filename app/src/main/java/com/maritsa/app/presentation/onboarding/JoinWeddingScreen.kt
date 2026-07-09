package com.maritsa.app.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maritsa.app.R
import com.maritsa.app.presentation.auth.LocalAuthViewModel
import com.maritsa.app.presentation.auth.SignInBottomSheet
import com.maritsa.app.presentation.identity.LocalIdentityViewModel
import com.maritsa.app.ui.theme.BlushLight
import com.maritsa.app.ui.theme.ChampagneLight
import com.maritsa.app.ui.theme.Gold
import com.maritsa.app.ui.theme.GoldDeep
import com.maritsa.app.ui.theme.Ivory
import com.maritsa.app.ui.theme.Spacing
import com.maritsa.app.ui.theme.WarmGray100
import com.maritsa.app.ui.theme.WarmGray200
import com.maritsa.app.ui.theme.WarmGray400
import com.maritsa.app.ui.theme.WarmGray500
import com.maritsa.app.ui.theme.WarmGray600
import com.maritsa.app.ui.theme.WarmGray800

private val _gfProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)
private val DancingScript = FontFamily(
    Font(GoogleFont("Dancing Script"), _gfProvider, FontWeight.Normal),
    Font(GoogleFont("Dancing Script"), _gfProvider, FontWeight.SemiBold),
)

@Composable
fun JoinWeddingScreen(
    onWeddingJoined: (weddingId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: JoinWeddingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading = state is JoinWeddingState.Loading

    // Joining a wedding requires an account so the guest can be recognised on other
    // devices and, if they've already joined before, resumed instead of re-added.
    val authViewModel = LocalAuthViewModel.current
    val identityViewModel = LocalIdentityViewModel.current
    val identity by identityViewModel.identity.collectAsState()
    var showSignInGate by remember { mutableStateOf(false) }
    var pendingSubmit by remember { mutableStateOf(false) }

    // Prompt sign-in as soon as the screen opens, not just on submit.
    LaunchedEffect(Unit) {
        if (!authViewModel.isSignedIn) {
            showSignInGate = true
        }
    }

    // Once signed in, prefer resuming the account's most recently joined wedding
    // over making them re-enter a code — covers both "just signed in from the
    // gate" and "already signed in when the screen opened" cases.
    //
    // Keyed off `identity` (IdentityManager's resolved USER/GUEST state), not the
    // raw Firebase `authState` — currentIdentityId is what membership lookups use,
    // and it's updated by a separate listener chain that can lag a frame or two
    // behind authState flipping non-null. Reading it too early returns the stale
    // pre-sign-in guest identityId and misses existing memberships.
    LaunchedEffect(identity) {
        if (!identity.isAuthenticated) return@LaunchedEffect
        showSignInGate = false
        val existingWeddingId = viewModel.latestJoinedWeddingId()
        if (existingWeddingId != null) {
            pendingSubmit = false
            onWeddingJoined(existingWeddingId)
        } else if (pendingSubmit) {
            pendingSubmit = false
            viewModel.submit()
        }
    }

    LaunchedEffect(state) {
        when (val s = state) {
            is JoinWeddingState.Success -> onWeddingJoined(s.weddingId)
            is JoinWeddingState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetState()
            }

            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ivory),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Hero header ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(BlushLight, Ivory))),
            ) {
                // Back button
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 20.dp, top = 16.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(WarmGray100.copy(alpha = 0.80f))
                        .clickable(onClick = onBack)
                        .align(Alignment.TopStart),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = WarmGray600,
                        modifier = Modifier.size(18.dp),
                    )
                }

                // Title
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 64.dp, bottom = 40.dp)
                        .padding(horizontal = Spacing.screenHorizontal),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Gold ornament row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .weight(1f)
                                .height(0.5.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, Gold.copy(alpha = 0.35f))
                                    )
                                )
                        )
                        Text(
                            text = "  ♡  ",
                            style = MaterialTheme.typography.labelSmall,
                            color = Gold.copy(alpha = 0.55f),
                        )
                        Box(
                            Modifier
                                .weight(1f)
                                .height(0.5.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Gold.copy(alpha = 0.35f), Color.Transparent)
                                    )
                                )
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Join the Celebration",
                        style = TextStyle(
                            fontFamily = DancingScript,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 36.sp,
                            color = WarmGray800,
                        ),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Enter the 6-character code from your invitation.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = FontStyle.Italic,
                        ),
                        color = WarmGray500,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // ── OTP + form ────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screenHorizontal)
                    .padding(top = Spacing.lg, bottom = Spacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {

                // 6-box OTP input
                OtpCodeInput(
                    value = viewModel.weddingCode,
                    onValueChange = viewModel::onWeddingCodeChange,
                )

                Spacer(Modifier.height(Spacing.xs))

                // Guest name
                OutlinedTextField(
                    value = viewModel.guestName,
                    onValueChange = viewModel::onGuestNameChange,
                    label = {
                        Text(
                            "Your Name (optional)",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    placeholder = {
                        Text("e.g. Alex Johnson", color = WarmGray400)
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = WarmGray400,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold.copy(alpha = 0.6f),
                        unfocusedBorderColor = WarmGray200,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = Gold,
                        focusedTextColor = WarmGray800,
                        unfocusedTextColor = WarmGray800,
                        focusedLabelColor = Gold,
                        unfocusedLabelColor = WarmGray400,
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium,
                )

                Spacer(Modifier.height(Spacing.xs))

                // Submit button / progress
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Gold,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(28.dp),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (viewModel.isSubmitEnabled)
                                    Brush.linearGradient(listOf(Gold, GoldDeep))
                                else
                                    Brush.linearGradient(
                                        listOf(WarmGray100, WarmGray200)
                                    )
                            )
                            .clickable(
                                enabled = viewModel.isSubmitEnabled,
                                onClick = {
                                    if (authViewModel.isSignedIn) {
                                        viewModel.submit()
                                    } else {
                                        pendingSubmit = true
                                        showSignInGate = true
                                    }
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Join Wedding",
                            style = MaterialTheme.typography.labelLarge.copy(
                                letterSpacing = 0.5.sp,
                            ),
                            color = if (viewModel.isSubmitEnabled) Color.White else WarmGray400,
                        )
                    }
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (showSignInGate) {
        SignInBottomSheet(
            authViewModel = authViewModel,
            reason = "Sign in to join this wedding.",
            onDismiss = {
                showSignInGate = false
                pendingSubmit = false
                authViewModel.clearError()
            },
            onSuccess = { showSignInGate = false },
        )
    }
}

// ── 6-box OTP input ───────────────────────────────────────────────────────────

@Composable
private fun OtpCodeInput(
    value: String,
    onValueChange: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    BasicTextField(
        value = value,
        onValueChange = { raw ->
            onValueChange(raw.filter { it.isLetterOrDigit() }.uppercase().take(6))
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            keyboardType = KeyboardType.Text,
        ),
        cursorBrush = SolidColor(Gold),
        modifier = Modifier.focusRequester(focusRequester),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                repeat(6) { index ->
                    val char = value.getOrNull(index)
                    val isActive = index == value.length && index < 6
                    OtpCell(char = char, isActive = isActive)
                }
            }
        },
    )
}

@Composable
private fun OtpCell(char: Char?, isActive: Boolean) {
    val borderBrush = when {
        isActive -> Brush.verticalGradient(listOf(Gold, GoldDeep))
        char != null -> Brush.verticalGradient(listOf(Gold.copy(0.45f), Gold.copy(0.20f)))
        else -> Brush.verticalGradient(listOf(WarmGray200, WarmGray100))
    }
    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (char != null) ChampagneLight else Color.White)
            .border(
                width = if (isActive) 1.5.dp else 0.8.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(12.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (char != null) {
            Text(
                text = char.toString(),
                style = TextStyle(
                    fontFamily = DancingScript,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 26.sp,
                    color = GoldDeep,
                ),
                textAlign = TextAlign.Center,
            )
        } else if (isActive) {
            // blinking cursor bar
            Box(
                Modifier
                    .size(width = 2.dp, height = 24.dp)
                    .background(Gold.copy(alpha = 0.60f))
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Join Wedding Screen"
)
@Composable
private fun JoinWeddingPreview() {
    com.maritsa.app.ui.theme.WedNowTheme {
        OtpCodeInput(
            value = "WED",
            onValueChange = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "OTP Input – Filled")
@Composable
private fun OtpFilledPreview() {
    com.maritsa.app.ui.theme.WedNowTheme {
        OtpCodeInput(
            value = "ABC123",
            onValueChange = {},
        )
    }
}
