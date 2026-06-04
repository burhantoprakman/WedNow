package com.wednowapp.wednow.presentation.auth

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
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

enum class NotifPrefType(val emoji: String, val label: String, val description: String) {
    WEDDING_UPDATES(
        "✨",
        "Wedding Updates",
        "Get notified when the date, venue, or schedule changes"
    ),
    CHAT_MESSAGES("💬", "Chat Messages", "Be alerted when new messages arrive in the group chat"),
    EVENT_REMINDERS("📣", "Announcements", "Receive important broadcasts from the hosts"),
    PHOTO_LIKES("❤️", "Photo Likes", "Know when someone likes one of your photos"),
    NEW_MEMORIES("📸", "Guestbook Entries", "See when a new memory is added to the guestbook"),
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestPassSheet(
    isSignedIn: Boolean,
    userName: String?,
    userEmail: String?,
    userInitial: Char?,
    guestRole: String = "guest",
    unreadNotificationCount: Int = 0,
    onDismiss: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateToRSVP: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onPrivacyPolicy: () -> Unit = {},
    onTermsAndConditions: () -> Unit = {},
    onContactSupport: () -> Unit = {},
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Notification permission state (Android 13+)
    var hasNotifPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    // Track if we've already shown the system dialog (to distinguish first-time from permanently denied)
    var hasAskedPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasNotifPermission = granted }

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

            // ── My Wedding ────────────────────────────────────────────────────
            PassSectionLabel("My Wedding")
            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoGlassCard(
                    title = "RSVP",
                    subtitle = "Confirm your attendance",
                    onClick = onNavigateToRSVP,
                )
                InfoGlassCard(
                    title = "Notifications",
                    subtitle = if (unreadNotificationCount > 0)
                        "$unreadNotificationCount unread update${if (unreadNotificationCount > 1) "s" else ""}"
                    else "Wedding updates & alerts",
                    badge = unreadNotificationCount,
                    onClick = onNavigateToNotifications,
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Notifications ─────────────────────────────────────────────────
            PassSectionLabel("Notifications")
            Spacer(Modifier.height(12.dp))

            // Permission banner when POST_NOTIFICATIONS not yet granted
            if (!hasNotifPermission) {
                NotifPermissionBanner(
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val canShowRationale = activity?.let {
                                ActivityCompat.shouldShowRequestPermissionRationale(
                                    it, Manifest.permission.POST_NOTIFICATIONS
                                )
                            } ?: false
                            // After first ask with no rationale = permanently denied → open settings
                            if (hasAskedPermission && !canShowRationale) {
                                context.startActivity(
                                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                )
                            } else {
                                hasAskedPermission = true
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    },
                )
                Spacer(Modifier.height(8.dp))
            }

            if (hasNotifPermission) {
                NotifTypeExpandableCard(
                    hasPermission = true,
                    notifMap = notifMap,
                    onRequestPermission = {},
                    onToggle = { type -> notifMap[type] = !(notifMap[type] ?: true) },
                )
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
                    subtitle = "Guidelines for using Maritsa",
                    onClick = onTermsAndConditions,
                )
                InfoGlassCard(
                    title = "Contact Support",
                    subtitle = "Reach out to the Maritsa team",
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

// ── Notification permission banner — InfoGlassCard style ─────────────────────

@Composable
private fun NotifPermissionBanner(onRequestPermission: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.97f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "bScale"
    )
    val elevation by animateDpAsState(if (isPressed) 0.dp else 4.dp, tween(180), label = "bElev")
    val arrowOffset by animateFloatAsState(
        if (isPressed) 5f else 0f,
        spring(Spring.DampingRatioMediumBouncy),
        label = "bArrow"
    )

    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .fillMaxWidth()
            .shadow(
                elevation,
                RoundedCornerShape(18.dp),
                ambientColor = Color.Black.copy(0.04f),
                spotColor = Color.Black.copy(0.05f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(0.84f),
                        Color.White.copy(0.62f)
                    )
                )
            )
            .border(
                0.8.dp,
                Brush.verticalGradient(listOf(Color.White.copy(0.92f), WarmGray200.copy(0.55f))),
                RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onRequestPermission
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    "Notifications are off",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = WarmGray800
                )
                Text(
                    "Tap to allow Maritsa to send you updates",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        letterSpacing = 0.1.sp
                    ),
                    color = WarmGray400
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Champagne.copy(0.45f))
                    .graphicsLayer { translationX = arrowOffset },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    null,
                    tint = Gold.copy(0.85f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ── Notification type expandable card ─────────────────────────────────────────

@Composable
private fun NotifTypeExpandableCard(
    hasPermission: Boolean,
    notifMap: MutableMap<NotifPrefType, Boolean>,
    onRequestPermission: () -> Unit,
    onToggle: (NotifPrefType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.97f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "ntScale"
    )
    val elevation by animateDpAsState(if (isPressed) 0.dp else 4.dp, tween(180), label = "ntElev")
    val arrowAngle by animateFloatAsState(if (expanded) 90f else 0f, tween(220), label = "ntAngle")

    Column {
        // ── Header card (identical to InfoGlassCard) ──────────────────────────
        Box(
            modifier = Modifier
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .fillMaxWidth()
                .shadow(
                    elevation,
                    RoundedCornerShape(18.dp),
                    ambientColor = Color.Black.copy(0.04f),
                    spotColor = Color.Black.copy(0.05f)
                )
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(0.84f),
                            Color.White.copy(0.62f)
                        )
                    )
                )
                .border(
                    0.8.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(0.92f),
                            WarmGray200.copy(0.55f)
                        )
                    ),
                    RoundedCornerShape(18.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        if (hasPermission) expanded = !expanded else onRequestPermission()
                    },
                )
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Type of Notifications",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = WarmGray800,
                    )
                    Text(
                        text = "Choose which updates you'd like to receive",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            letterSpacing = 0.1.sp
                        ),
                        color = WarmGray400,
                    )
                }
                // Arrow circle — rotates 90° when expanded
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Champagne.copy(0.45f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Gold.copy(0.85f),
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(arrowAngle),
                    )
                }
            }
        }

        // ── Expanded notification rows ─────────────────────────────────────────
        AnimatedVisibility(
            visible = expanded && hasPermission,
            enter = expandVertically(tween(280)) + fadeIn(tween(280)),
            exit = shrinkVertically(tween(200)) + fadeOut(tween(200)),
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White),
            ) {
                NotifPrefType.entries.forEachIndexed { index, type ->
                    NotifInlineRow(
                        spec = type,
                        isEnabled = notifMap[type] != false,
                        onToggle = { onToggle(type) },
                    )
                    if (index < NotifPrefType.entries.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 18.dp),
                            thickness = 0.5.dp,
                            color = WarmGray100,
                        )
                    }
                }
            }
        }
    }
}

// ── Inline notification row (inside expanded card) ────────────────────────────

@Composable
private fun NotifInlineRow(
    spec: NotifPrefType,
    isEnabled: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = spec.emoji + "  " + spec.label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isEnabled) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 14.sp,
                ),
                color = if (isEnabled) WarmGray800 else WarmGray500,
            )
            Text(
                text = spec.description,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    letterSpacing = 0.1.sp
                ),
                color = WarmGray400,
            )
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Gold,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = WarmGray200,
                uncheckedBorderColor = WarmGray200,
            ),
        )
    }
}

// ── Information glass card ────────────────────────────────────────────────────

@Composable
private fun InfoGlassCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    badge: Int = 0,
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

            // Badge count (e.g. unread notifications)
            if (badge > 0) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Gold.copy(alpha = 0.88f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (badge > 9) "9+" else "$badge",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Color(0xFF3D2A18),
                    )
                }
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
