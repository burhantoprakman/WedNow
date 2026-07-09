package com.maritsa.app.presentation.rsvp

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.maritsa.app.R
import com.maritsa.app.domain.model.GuestMember
import com.maritsa.app.domain.model.MemberRole
import com.maritsa.app.domain.model.RSVPStatus
import com.maritsa.app.ui.theme.BlushDeep
import com.maritsa.app.ui.theme.BlushLight
import com.maritsa.app.ui.theme.ChampagneLight
import com.maritsa.app.ui.theme.ErrorRose
import com.maritsa.app.ui.theme.ErrorRoseLight
import com.maritsa.app.ui.theme.Gold
import com.maritsa.app.ui.theme.GoldDeep
import com.maritsa.app.ui.theme.Ivory
import com.maritsa.app.ui.theme.Spacing
import com.maritsa.app.ui.theme.WarmGray100
import com.maritsa.app.ui.theme.WarmGray300
import com.maritsa.app.ui.theme.WarmGray400
import com.maritsa.app.ui.theme.WarmGray50
import com.maritsa.app.ui.theme.WarmGray500
import com.maritsa.app.ui.theme.WarmGray600
import com.maritsa.app.ui.theme.WarmGray800
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
    val currentGroup by viewModel.currentGroup.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val guestLoaded by viewModel.guestLoaded.collectAsState()
    val snackbarState = remember { SnackbarHostState() }
    val isLoading = submitState == RsvpSubmitState.Loading

    LaunchedEffect(submitState) {
        when (val s = submitState) {
            is RsvpSubmitState.Success -> {
                snackbarState.showSnackbar("RSVP saved!")
                viewModel.resetSubmitState()
            }

            is RsvpSubmitState.Error -> {
                snackbarState.showSnackbar(s.message)
                viewModel.resetSubmitState()
            }

            else -> Unit
        }
    }

    val hasFamily = currentGroup != null && currentGroup!!.members.isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ivory)
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
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = WarmGray600,
                        modifier = Modifier.size(18.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 64.dp, bottom = 36.dp)
                        .padding(horizontal = Spacing.screenHorizontal),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    OrnamentLine()
                    Spacer(Modifier.height(18.dp))
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
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (hasFamily) "Set attendance for each family member."
                        else "Let the couple know if you can make it.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        color = WarmGray500,
                        textAlign = TextAlign.Center,
                    )
                    currentGuest?.rsvpUpdatedAt?.let { ts ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Last updated ${formatTimestamp(ts)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = Gold.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            // ── Body ──────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screenHorizontal)
                    .padding(top = Spacing.md, bottom = Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                when {
                    !guestLoaded && currentGuest == null && submitState !is RsvpSubmitState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                color = Gold,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    hasFamily -> {
                        currentGroup!!.members.forEachIndexed { index, member ->
                            FamilyMemberCard(
                                member = member,
                                isLoading = isLoading,
                                onSelect = { status -> viewModel.submitMemberRsvp(index, status) },
                            )
                        }
                        if (isLoading) {
                            Spacer(Modifier.height(Spacing.sm))
                            Box(Modifier.fillMaxWidth(), Alignment.Center) {
                                CircularProgressIndicator(
                                    color = Gold,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    else -> {
                        val selected = currentGuest?.rsvpStatus
                        SelfRsvpCard(
                            selected = selected,
                            isLoading = isLoading,
                            onSelect = { status -> if (!isLoading) viewModel.submit(status) },
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ── Self RSVP card ────────────────────────────────────────────────────────────

@Composable
private fun SelfRsvpCard(
    selected: String?,
    isLoading: Boolean,
    onSelect: (String) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Your Response",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                color = WarmGray400,
            )
            RsvpChoiceRow(
                selected = selected,
                isLoading = isLoading,
                onSelect = onSelect,
            )
        }
    }
}

// ── Family member card ────────────────────────────────────────────────────────

@Composable
private fun FamilyMemberCard(
    member: GuestMember,
    isLoading: Boolean,
    onSelect: (String) -> Unit,
) {
    val isChild = member.role == MemberRole.CHILD

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            // ── Identity row ──────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isChild) BlushLight else ChampagneLight),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = member.name.take(1).uppercase().ifBlank { "?" },
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isChild) BlushDeep else GoldDeep,
                    )
                }
                Text(
                    text = member.name.ifBlank { "Guest" },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = WarmGray800,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isChild) BlushLight else ChampagneLight)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = member.role,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (isChild) BlushDeep else GoldDeep,
                    )
                }
            }

            // ── Choice row ────────────────────────────────────────────────────
            RsvpChoiceRow(
                selected = member.rsvpStatus,
                isLoading = isLoading,
                onSelect = onSelect,
            )
        }
    }
}

// ── Shared 3-pill row ─────────────────────────────────────────────────────────

@Composable
private fun RsvpChoiceRow(
    selected: String?,
    isLoading: Boolean,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        RsvpPill(
            label = "Going",
            icon = Icons.Default.CheckCircle,
            selected = selected == RSVPStatus.GOING,
            selectedColor = GoldDeep,
            selectedBg = ChampagneLight,
            onClick = { if (!isLoading) onSelect(RSVPStatus.GOING) },
            modifier = Modifier.weight(1f),
        )
        RsvpPill(
            label = "Maybe",
            icon = Icons.Default.HelpOutline,
            selected = selected == RSVPStatus.MAYBE,
            selectedColor = BlushDeep,
            selectedBg = BlushLight,
            onClick = { if (!isLoading) onSelect(RSVPStatus.MAYBE) },
            modifier = Modifier.weight(1f),
        )
        RsvpPill(
            label = "Can't go",
            icon = Icons.Default.Cancel,
            selected = selected == RSVPStatus.NOT_GOING,
            selectedColor = ErrorRose,
            selectedBg = ErrorRoseLight,
            onClick = { if (!isLoading) onSelect(RSVPStatus.NOT_GOING) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RsvpPill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    selectedColor: Color,
    selectedBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg by animateColorAsState(
        targetValue = if (selected) selectedBg else WarmGray50,
        animationSpec = tween(180),
        label = "pillBg",
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) selectedColor.copy(alpha = 0.30f) else Color.Transparent,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (selected) selectedColor else WarmGray300,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                ),
                color = if (selected) selectedColor else WarmGray400,
            )
        }
    }
}

// ── Ornament ──────────────────────────────────────────────────────────────────

@Composable
private fun OrnamentLine() {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Gold.copy(alpha = 0.35f)
                        )
                    )
                )
        )
        Text("  ♡  ", style = MaterialTheme.typography.labelSmall, color = Gold.copy(alpha = 0.55f))
        Box(
            Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Gold.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

private fun formatTimestamp(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault()).format(Date(millis))

// ── Previews ──────────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    showSystemUi = true,
    name = "RSVP – Self"
)
@Composable
private fun RsvpSelfPreview() {
    com.maritsa.app.ui.theme.WedNowTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Ivory)
                .padding(Spacing.screenHorizontal)
                .padding(top = 32.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            SelfRsvpCard(selected = RSVPStatus.GOING, isLoading = false, onSelect = {})
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    showSystemUi = true,
    name = "RSVP – Family of 3"
)
@Composable
private fun RsvpFamilyPreview() {
    com.maritsa.app.ui.theme.WedNowTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Ivory)
                .padding(Spacing.screenHorizontal)
                .padding(top = 32.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            listOf(
                GuestMember("Sophie Walker", MemberRole.ADULT, rsvpStatus = RSVPStatus.GOING),
                GuestMember("James Walker", MemberRole.ADULT, rsvpStatus = RSVPStatus.MAYBE),
                GuestMember("Lily Walker", MemberRole.CHILD, rsvpStatus = null),
            ).forEachIndexed { i, member ->
                FamilyMemberCard(member = member, isLoading = false, onSelect = {})
            }
        }
    }
}
