package com.wednowapp.wednow.presentation.rsvp

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wednowapp.wednow.R
import com.wednowapp.wednow.domain.model.RSVPStatus
import com.wednowapp.wednow.ui.components.RsvpOptionCard
import com.wednowapp.wednow.ui.theme.BlushDeep
import com.wednowapp.wednow.ui.theme.BlushLight
import com.wednowapp.wednow.ui.theme.ChampagneLight
import com.wednowapp.wednow.ui.theme.ErrorRose
import com.wednowapp.wednow.ui.theme.ErrorRoseLight
import com.wednowapp.wednow.ui.theme.Gold
import com.wednowapp.wednow.ui.theme.GoldDeep
import com.wednowapp.wednow.ui.theme.Ivory
import com.wednowapp.wednow.ui.theme.Spacing
import com.wednowapp.wednow.ui.theme.WarmGray100
import com.wednowapp.wednow.ui.theme.WarmGray400
import com.wednowapp.wednow.ui.theme.WarmGray500
import com.wednowapp.wednow.ui.theme.WarmGray600
import com.wednowapp.wednow.ui.theme.WarmGray800
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
fun RSVPScreen(
    onBack: () -> Unit,
    viewModel: RSVPViewModel = hiltViewModel(),
) {
    val currentGuest by viewModel.currentGuest.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val snackbarState = remember { SnackbarHostState() }
    val isLoading = submitState == RsvpSubmitState.Loading

    LaunchedEffect(submitState) {
        when (val s = submitState) {
            is RsvpSubmitState.Success -> {
                snackbarState.showSnackbar("RSVP saved! 🎉")
                viewModel.resetSubmitState()
            }
            is RsvpSubmitState.Error -> {
                snackbarState.showSnackbar(s.message)
                viewModel.resetSubmitState()
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
                    .background(
                        Brush.verticalGradient(
                            listOf(BlushLight, Ivory)
                        )
                    ),
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

                // Title content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 64.dp, bottom = 40.dp)
                        .padding(horizontal = Spacing.screenHorizontal),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Ornament line
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
                        text = "Will you be there?",
                        style = TextStyle(
                            fontFamily = DancingScript,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 38.sp,
                            color = WarmGray800,
                        ),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Let the couple know if you can make it.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = FontStyle.Italic,
                        ),
                        color = WarmGray500,
                        textAlign = TextAlign.Center,
                    )

                    currentGuest?.rsvpUpdatedAt?.let { ts ->
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Last updated ${formatTimestamp(ts)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.5.sp,
                                fontSize = 10.sp,
                            ),
                            color = Gold.copy(alpha = 0.75f),
                        )
                    }
                }
            }

            // ── RSVP option cards ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screenHorizontal)
                    .padding(top = Spacing.lg, bottom = Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                if (currentGuest == null && submitState !is RsvpSubmitState.Error) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = Gold,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                } else {
                    val selected = currentGuest?.rsvpStatus

                    // Going
                    RsvpOptionCard(
                        icon = Icons.Default.CheckCircle,
                        label = "I'll be there!",
                        description = "Confirm your attendance",
                        selected = selected == RSVPStatus.GOING,
                        onClick = { if (!isLoading) viewModel.submit(RSVPStatus.GOING) },
                        selectedContainerColor = ChampagneLight,
                        selectedContentColor = GoldDeep,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Not going
                    RsvpOptionCard(
                        icon = Icons.Default.Cancel,
                        label = "Can't make it",
                        description = "You won't be attending",
                        selected = selected == RSVPStatus.NOT_GOING,
                        onClick = { if (!isLoading) viewModel.submit(RSVPStatus.NOT_GOING) },
                        selectedContainerColor = ErrorRoseLight,
                        selectedContentColor = ErrorRose,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Maybe
                    RsvpOptionCard(
                        icon = Icons.Default.HelpOutline,
                        label = "Maybe",
                        description = "Still deciding…",
                        selected = selected == RSVPStatus.MAYBE,
                        onClick = { if (!isLoading) viewModel.submit(RSVPStatus.MAYBE) },
                        selectedContainerColor = BlushLight,
                        selectedContentColor = BlushDeep,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (isLoading) {
                        Spacer(Modifier.height(Spacing.sm))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                color = Gold,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                    }
                }
            }
        }

        // ── Snackbar ──────────────────────────────────────────────────────────
        SnackbarHost(
            hostState = snackbarState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

private fun formatTimestamp(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault()).format(Date(millis))
