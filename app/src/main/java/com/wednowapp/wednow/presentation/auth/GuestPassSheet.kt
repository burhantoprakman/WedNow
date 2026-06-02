package com.wednowapp.wednow.presentation.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wednowapp.wednow.R
import com.wednowapp.wednow.ui.theme.Champagne
import com.wednowapp.wednow.ui.theme.ChampagneLight
import com.wednowapp.wednow.ui.theme.Gold
import com.wednowapp.wednow.ui.theme.GoldDeep
import com.wednowapp.wednow.ui.theme.Ivory
import com.wednowapp.wednow.ui.theme.WarmGray100
import com.wednowapp.wednow.ui.theme.WarmGray200
import com.wednowapp.wednow.ui.theme.WarmGray300
import com.wednowapp.wednow.ui.theme.WarmGray400
import com.wednowapp.wednow.ui.theme.WarmGray500
import com.wednowapp.wednow.ui.theme.WarmGray800

// ── Romantic script font ──────────────────────────────────────────────────────

private val _gpFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)
private val DancingScriptGP = FontFamily(
    Font(GoogleFont("Dancing Script"), _gpFontProvider, FontWeight.Normal),
    Font(GoogleFont("Dancing Script"), _gpFontProvider, FontWeight.SemiBold),
)

// ── Notification preference types ─────────────────────────────────────────────

enum class NotifPrefType(val emoji: String, val label: String) {
    WEDDING_UPDATES("✨", "Wedding Updates"),
    CHAT_MESSAGES("💬", "Chat Messages"),
    EVENT_REMINDERS("🎉", "Event Reminders"),
    PHOTO_LIKES("❤️", "Photo Likes"),
    NEW_MEMORIES("📸", "New Memories"),
}

// ── Guest Pass Sheet ──────────────────────────────────────────────────────────

/**
 * The "Guest Pass" personal area — a premium floating sheet that expands
 * when the guest taps their avatar in the top-right corner.
 *
 * Purely presentational: all auth actions arrive as callbacks.
 *
 * @param isSignedIn          True when a Firebase user is authenticated.
 * @param userName            Display name for the signed-in user, null if guest.
 * @param userEmail           Email address, null if guest or unavailable.
 * @param userInitial         First character of the display name for the avatar circle.
 * @param guestRole           "guest" | "admin" | "coadmin"
 * @param onDismiss           Dismiss the sheet.
 * @param onSignIn            Trigger the sign-in flow (caller decides which provider).
 * @param onSignOut           Sign the user out.
 * @param onPrivacyPolicy     Open Privacy Policy.
 * @param onTermsAndConditions Open Terms & Conditions.
 * @param onContactSupport    Open Contact Support.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GuestPassSheet(
    isSignedIn: Boolean,
    userName: String?,
    userEmail: String?,
    userInitial: Char?,
    guestRole: String = "guest",
    onDismiss: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onPrivacyPolicy: () -> Unit = {},
    onTermsAndConditions: () -> Unit = {},
    onContactSupport: () -> Unit = {},
) {
    // Local notification preferences — lives for the duration of this sheet session
    val notifMap = remember {
        mutableStateMapOf<NotifPrefType, Boolean>().apply {
            NotifPrefType.entries.forEach { put(it, true) }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = Ivory,
        scrimColor = Color.Black.copy(alpha = 0.30f),
        dragHandle = {
            // Champagne gold gradient pill
            Box(
                modifier = Modifier
                    .padding(top = 14.dp, bottom = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Gold.copy(alpha = 0.20f),
                                Gold.copy(alpha = 0.55f),
                                Gold.copy(alpha = 0.20f),
                            )
                        )
                    ),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 44.dp),
        ) {
            // ── Pass header ───────────────────────────────────────────────────
            GuestPassHeader(
                isSignedIn = isSignedIn,
                userName = userName,
                userEmail = userEmail,
                userInitial = userInitial,
                guestRole = guestRole,
            )

            Spacer(Modifier.height(32.dp))

            // ── Notifications ─────────────────────────────────────────────────
            PassSectionLabel("Notifications")
            Spacer(Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NotifPrefType.entries.forEach { type ->
                    NotifPill(
                        spec = type,
                        isEnabled = notifMap[type] != false,
                        onToggle = { notifMap[type] = !(notifMap[type] ?: true) },
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Information ───────────────────────────────────────────────────
            PassSectionLabel("Information")
            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoGlassCard(
                    title = "Privacy Policy",
                    subtitle = "How we protect your information",
                    onClick = onPrivacyPolicy,
                )
                InfoGlassCard(
                    title = "Terms & Conditions",
                    subtitle = "Guidelines for using WedNow",
                    onClick = onTermsAndConditions,
                )
                InfoGlassCard(
                    title = "Contact Support",
                    subtitle = "Reach out to the WedNow team",
                    onClick = onContactSupport,
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Account ───────────────────────────────────────────────────────
            PassSectionLabel("Account")
            Spacer(Modifier.height(12.dp))

            AccountCard(
                isSignedIn = isSignedIn,
                onSignIn = onSignIn,
                onSignOut = onSignOut,
            )
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun GuestPassHeader(
    isSignedIn: Boolean,
    userName: String?,
    userEmail: String?,
    userInitial: Char?,
    guestRole: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(4.dp))

        // Gold ornament
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(
                Modifier
                    .width(28.dp)
                    .height(0.5.dp)
                    .background(
                        Brush.horizontalGradient(listOf(Color.Transparent, Gold.copy(0.38f)))
                    )
            )
            Text("✦", fontSize = 7.sp, color = Gold.copy(0.50f))
            Text("♡", fontSize = 11.sp, color = Gold.copy(0.68f))
            Text("✦", fontSize = 7.sp, color = Gold.copy(0.50f))
            Box(
                Modifier
                    .width(28.dp)
                    .height(0.5.dp)
                    .background(
                        Brush.horizontalGradient(listOf(Gold.copy(0.38f), Color.Transparent))
                    )
            )
        }

        Spacer(Modifier.height(20.dp))

        // Avatar circle
        Box(
            modifier = Modifier
                .size(82.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    ambientColor = Gold.copy(alpha = 0.18f),
                    spotColor = Gold.copy(alpha = 0.18f),
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(ChampagneLight.copy(0.95f), Champagne.copy(0.55f))
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(Gold.copy(0.60f), Gold.copy(0.18f))
                    ),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSignedIn && userInitial != null) {
                Text(
                    text = userInitial.toString(),
                    style = TextStyle(
                        fontFamily = DancingScriptGP,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 38.sp,
                        color = GoldDeep,
                    ),
                )
            } else {
                // Elegant star for anonymous guests
                Text(
                    text = "✦",
                    fontSize = 26.sp,
                    color = Gold.copy(0.72f),
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Name
        Text(
            text = if (isSignedIn && !userName.isNullOrBlank()) userName else "Valued Guest",
            style = TextStyle(
                fontFamily = DancingScriptGP,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                color = WarmGray800,
            ),
            textAlign = TextAlign.Center,
        )

        // Email — only when signed in
        if (isSignedIn && !userEmail.isNullOrBlank()) {
            Spacer(Modifier.height(3.dp))
            Text(
                text = userEmail,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = WarmGray400,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(10.dp))

        // Role badge — pill-shaped, champagne gradient
        val roleLabel = when (guestRole.lowercase()) {
            "admin" -> "Admin"
            "coadmin" -> "Co‑Host"
            else -> "Guest Pass"
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(40.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(ChampagneLight, Champagne.copy(0.65f))
                    )
                )
                .border(
                    width = 0.8.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Gold.copy(0.45f), Gold.copy(0.18f))
                    ),
                    shape = RoundedCornerShape(40.dp),
                )
                .padding(horizontal = 16.dp, vertical = 5.dp),
        ) {
            Text(
                text = roleLabel.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = GoldDeep,
            )
        }
    }
}

// ── Section label ─────────────────────────────────────────────────────────────

@Composable
private fun PassSectionLabel(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = WarmGray400,
        )
        // Fading rule to the right
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(WarmGray200, Color.Transparent)
                    )
                ),
        )
    }
}

// ── Notification pill ─────────────────────────────────────────────────────────

@Composable
private fun NotifPill(
    spec: NotifPrefType,
    isEnabled: Boolean,
    onToggle: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Spring press scale
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "pillScale",
    )

    // Animated chip colours
    val bgColor by animateColorAsState(
        targetValue = if (isEnabled) Gold.copy(alpha = 0.10f) else WarmGray100,
        animationSpec = tween(260),
        label = "pillBg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isEnabled) Gold.copy(alpha = 0.52f) else WarmGray200,
        animationSpec = tween(260),
        label = "pillBorder",
    )
    val labelColor by animateColorAsState(
        targetValue = if (isEnabled) GoldDeep else WarmGray500,
        animationSpec = tween(260),
        label = "pillLabel",
    )
    val glowElevation by animateDpAsState(
        targetValue = if (isEnabled) 6.dp else 0.dp,
        animationSpec = tween(260),
        label = "pillGlow",
    )

    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation = glowElevation,
                shape = RoundedCornerShape(50.dp),
                ambientColor = Gold.copy(alpha = 0.28f),
                spotColor = Gold.copy(alpha = 0.22f),
            )
            .clip(RoundedCornerShape(50.dp))
            .background(bgColor)
            .border(0.8.dp, borderColor, RoundedCornerShape(50.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggle,
            )
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = spec.emoji, fontSize = 13.sp)
            Text(
                text = spec.label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isEnabled) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 12.sp,
                    letterSpacing = 0.2.sp,
                ),
                color = labelColor,
            )
        }
    }
}

// ── Information glass card ────────────────────────────────────────────────────

@Composable
private fun InfoGlassCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Lift → compress on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "infoScale",
    )
    // Shadow rises when resting, flattens on press (lift illusion)
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 4.dp,
        animationSpec = tween(180),
        label = "infoElevation",
    )
    // Arrow slides right on press
    val arrowOffset by animateFloatAsState(
        targetValue = if (isPressed) 5f else 0f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "arrowOffset",
    )

    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .fillMaxWidth()
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.05f),
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(0.84f), Color.White.copy(0.62f))
                )
            )
            .border(
                width = 0.8.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(0.92f), WarmGray200.copy(0.55f))
                ),
                shape = RoundedCornerShape(18.dp),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    ),
                    color = WarmGray800,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        letterSpacing = 0.1.sp,
                    ),
                    color = WarmGray400,
                )
            }

            // Animated arrow in a champagne circle
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Champagne.copy(0.45f))
                    .graphicsLayer { translationX = arrowOffset },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Gold.copy(0.85f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// ── Account card ──────────────────────────────────────────────────────────────

@Composable
private fun AccountCard(
    isSignedIn: Boolean,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(0.84f), Color.White.copy(0.62f))
                )
            )
            .border(
                width = 0.8.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(0.92f), WarmGray200.copy(0.55f))
                ),
                shape = RoundedCornerShape(18.dp),
            ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (isSignedIn) {
                AccountRow(
                    emoji = "🔄",
                    label = "Switch Account",
                    textColor = WarmGray800,
                    onClick = onSignIn,
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    thickness = 0.5.dp,
                    color = WarmGray200,
                )
                AccountRow(
                    emoji = "👋",
                    label = "Sign Out",
                    textColor = Color(0xFFBA3A3A),
                    onClick = onSignOut,
                )
            } else {
                AccountRow(
                    emoji = "✨",
                    label = "Sign In",
                    textColor = GoldDeep,
                    onClick = onSignIn,
                )
            }
        }
    }
}

// ── Single account row ────────────────────────────────────────────────────────

@Composable
private fun AccountRow(
    emoji: String,
    label: String,
    textColor: Color,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.045f else 0f,
        animationSpec = tween(140),
        label = "acctRowBg",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gold.copy(alpha = bgAlpha))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = emoji, fontSize = 16.sp)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
            color = textColor,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = WarmGray300,
            modifier = Modifier.size(18.dp),
        )
    }
}
