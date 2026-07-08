package com.wednowapp.wednow.presentation.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wednowapp.wednow.R
import com.wednowapp.wednow.domain.model.GuestRole
import com.wednowapp.wednow.domain.model.Wedding
import com.wednowapp.wednow.presentation.auth.GuestPassSheet
import com.wednowapp.wednow.presentation.auth.LocalAuthViewModel
import com.wednowapp.wednow.presentation.auth.SignInBottomSheet
import com.wednowapp.wednow.ui.components.WedNowErrorScreen
import com.wednowapp.wednow.ui.components.WedNowLoadingScreen
import com.wednowapp.wednow.ui.theme.Champagne
import com.wednowapp.wednow.ui.theme.ChampagneLight
import com.wednowapp.wednow.ui.theme.CormorantGaramond
import com.wednowapp.wednow.ui.theme.DmSans
import com.wednowapp.wednow.ui.theme.Gold
import com.wednowapp.wednow.ui.theme.GoldDeep
import com.wednowapp.wednow.ui.theme.Spacing
import com.wednowapp.wednow.ui.theme.WarmGray400
import com.wednowapp.wednow.ui.theme.WarmGray500
import com.wednowapp.wednow.ui.theme.WarmGray800
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// ── Script font for romantic subtitle ─────────────────────────────────────────

private val _gfProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val DancingScript = FontFamily(
    Font(GoogleFont("Dancing Script"), _gfProvider, FontWeight.Normal),
    Font(GoogleFont("Dancing Script"), _gfProvider, FontWeight.SemiBold),
)

private data class CountdownTime(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
)

// ── Entry point ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRSVP: () -> Unit,
    onNavigateToGuestbook: () -> Unit,
    onNavigateToPhotos: () -> Unit,
    onNavigateToWeddingInfo: () -> Unit,
    onNavigateToGuests: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToBroadcasts: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToTimeline: () -> Unit,
    onNavigateToShareInvitation: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationCount.collectAsState()

    when (state) {
        is WeddingDetailState.Loading -> WedNowLoadingScreen()

        is WeddingDetailState.Error -> WedNowErrorScreen(
            message = (state as WeddingDetailState.Error).message,
            onRetry = viewModel::retry,
        )

        is WeddingDetailState.Success -> {
            val s = state as WeddingDetailState.Success
            HomeContent(
                wedding = s.wedding,
                isPrivileged = s.isPrivileged,
                guestRole = s.guestRole,
                unreadNotificationCount = unreadCount,
                onNavigateToRSVP = onNavigateToRSVP,
                onNavigateToGuestbook = onNavigateToGuestbook,
                onNavigateToPhotos = onNavigateToPhotos,
                onNavigateToWeddingInfo = onNavigateToWeddingInfo,
                onNavigateToGuests = onNavigateToGuests,
                onNavigateToChat = onNavigateToChat,
                onNavigateToTimeline = onNavigateToTimeline,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToShareInvitation = onNavigateToShareInvitation,
            )
        }
    }
}

// ── Root layout ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    wedding: Wedding,
    isPrivileged: Boolean,
    guestRole: String = GuestRole.GUEST,
    unreadNotificationCount: Int,
    onNavigateToRSVP: () -> Unit,
    onNavigateToGuestbook: () -> Unit,
    onNavigateToPhotos: () -> Unit,
    onNavigateToWeddingInfo: () -> Unit,
    onNavigateToGuests: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToTimeline: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToShareInvitation: () -> Unit,
) {
    val authViewModel = LocalAuthViewModel.current
    val authState by authViewModel.authState.collectAsState()

    var showNavHub by remember { mutableStateOf(false) }
    var showAccountSheet by remember { mutableStateOf(false) }
    var showSignInSheet by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val heroAlpha by animateFloatAsState(
        if (visible) 1f else 0f,
        tween(800, 0, FastOutSlowInEasing)
    )
    val countAlpha by animateFloatAsState(
        if (visible) 1f else 0f,
        tween(800, 300, FastOutSlowInEasing)
    )
    val ctaAlpha by animateFloatAsState(
        if (visible) 1f else 0f,
        tween(800, 550, FastOutSlowInEasing)
    )
    val previewAlpha by animateFloatAsState(
        if (visible) 1f else 0f,
        tween(800, 750, FastOutSlowInEasing)
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // Full-screen background — diagonal gradient matching design.png
        // Warm ivory (bottom-left) → champagne (centre) → soft blue-grey (top-right)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0.00f to Color(0xFFCDD8E8),
                            0.50f to Color(0xFFE8D8C0),
                            1.00f to Color(0xFFF5EDE0),
                        ),
                        start = Offset(1400f, 0f),
                        end = Offset(0f, 2400f),
                    )
                )
        )

        // Scrollable content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(top = 56.dp, bottom = 56.dp),
        ) {
            item { HeroSection(wedding, Modifier.alpha(heroAlpha)) }
            item { Spacer(Modifier.height(Spacing.xl)) }
            item { CountdownSection(wedding.date, Modifier.alpha(countAlpha)) }
            item { Spacer(Modifier.height(Spacing.lg)) }
            item {
                WeddingDetailsRow(
                    wedding,
                    onNavigateToWeddingInfo,
                    onNavigateToTimeline,
                    Modifier.alpha(countAlpha)
                )
            }
            item { Spacer(Modifier.height(Spacing.lg)) }
            item {
                FeaturePreviewSection(
                    onNavigateToPhotos = onNavigateToPhotos,
                    onNavigateToGuestbook = onNavigateToGuestbook,
                    onNavigateToWeddingInfo = onNavigateToWeddingInfo,
                    onNavigateToGuests = onNavigateToGuests,
                    modifier = Modifier.alpha(previewAlpha),
                )
            }
            item { Spacer(Modifier.height(Spacing.lg)) }
            item {
                ChatWhisperRow(
                    onClick = onNavigateToChat,
                    modifier = Modifier.alpha(previewAlpha),
                )
            }
            item { Spacer(Modifier.height(Spacing.xl)) }
            item {
                GuestPassCard(
                    userName = authState?.displayName,
                    guestRole = guestRole,
                    onClick = { showAccountSheet = true },
                    modifier = Modifier
                        .alpha(previewAlpha)
                        .padding(horizontal = Spacing.screenHorizontal),
                )
            }
            item { Spacer(Modifier.height(Spacing.lg)) }
            item { ElegantFooter(Modifier.alpha(previewAlpha)) }
            item { Spacer(Modifier.height(Spacing.xl)) }
        }

    }

    if (showAccountSheet) {
        val context = LocalContext.current
        GuestPassSheet(
            isSignedIn = authState != null,
            userName = authState?.displayName,
            userEmail = authState?.email,
            userInitial = authState?.displayName?.firstOrNull()?.uppercaseChar(),
            guestRole = guestRole,
            unreadNotificationCount = unreadNotificationCount,
            onDismiss = { showAccountSheet = false },
            onSignIn = { showAccountSheet = false; showSignInSheet = true },
            onSignOut = { authViewModel.signOut(); showAccountSheet = false },
            onSwitchAccount = {
                authViewModel.prepareForAccountSwitch()
                showAccountSheet = false
                showSignInSheet = true
            },
            onNavigateToRSVP = { showAccountSheet = false; onNavigateToRSVP() },
            onNavigateToNotifications = { showAccountSheet = false; onNavigateToNotifications() },
            onPrivacyPolicy = {
                context.startActivity(
                    android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://maritsa.app/privacy"),
                    )
                )
            },
            onTermsAndConditions = {
                context.startActivity(
                    android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://maritsa.app/terms"),
                    )
                )
            },
            onContactSupport = {
                context.startActivity(
                    android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("mailto:hello@maritsa.app"),
                    )
                )
            },
        )
    }

    if (showSignInSheet) {
        SignInBottomSheet(
            authViewModel = authViewModel,
            reason = "Sign in to save your RSVP, share photos, and stay connected.",
            onDismiss = { showSignInSheet = false },
            onSuccess = { showSignInSheet = false },
        )
    }

}

// ── Guest Pass Card ───────────────────────────────────────────────────────────

/**
 * A miniature wedding-invitation-style card that lives near the bottom of the
 * home feed.  Tapping it expands [GuestPassSheet].
 *
 * Collapsed state shows:
 *   • Invitation ornament (✦ line ♡) on the left
 *   • "YOUR GUEST PASS" label + guest name in the script font
 *   • Role badge pill (Guest / Admin / Co-Host)
 *   • Subtle right chevron
 */
@Composable
private fun GuestPassCard(
    userName: String?,
    guestRole: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "passCardScale",
    )

    val displayName = if (!userName.isNullOrBlank()) userName else "Valued Guest"
    val roleLabel = when (guestRole.lowercase()) {
        "admin" -> "Admin"
        "coadmin" -> "Co‑Host"
        else -> "Guest"
    }

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .fillMaxWidth()
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Gold.copy(alpha = 0.14f),
                spotColor = Gold.copy(alpha = 0.09f),
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.90f),
                        ChampagneLight.copy(alpha = 0.52f),
                        Color.White.copy(alpha = 0.82f),
                    )
                )
            )
            .border(
                width = 0.8.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Gold.copy(alpha = 0.52f),
                        Gold.copy(alpha = 0.20f),
                        Gold.copy(alpha = 0.48f),
                    )
                ),
                shape = RoundedCornerShape(24.dp),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            // ── Invitation ornament (left) ─────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.width(18.dp),
            ) {
                Text("✦", fontSize = 7.sp, color = Gold.copy(0.58f))
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(0.8.dp)
                        .height(14.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Gold.copy(0.44f), Gold.copy(0.10f))
                            )
                        )
                )
                Spacer(Modifier.height(2.dp))
                Text("♡", fontSize = 8.sp, color = Gold.copy(0.62f))
            }

            Spacer(Modifier.width(14.dp))

            // ── Guest name ────────────────────────────────────────────────
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "YOUR GUEST PASS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 7.sp,
                        letterSpacing = 1.8.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    color = WarmGray400,
                )
                Text(
                    text = displayName,
                    style = TextStyle(
                        fontFamily = DancingScript,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = WarmGray800,
                    ),
                )
            }

            Spacer(Modifier.width(10.dp))

            // ── Role badge ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(ChampagneLight, Champagne.copy(0.68f))
                        )
                    )
                    .border(
                        width = 0.8.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Gold.copy(0.40f), Gold.copy(0.16f))
                        ),
                        shape = RoundedCornerShape(20.dp),
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = roleLabel.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = GoldDeep,
                )
            }

            Spacer(Modifier.width(8.dp))

            // ── Open indicator ─────────────────────────────────────────────
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open Guest Pass",
                tint = Gold.copy(0.68f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// ── Hero section ──────────────────────────────────────────────────────────────

@Composable
private fun HeroSection(wedding: Wedding, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── "WE'RE GETTING MARRIED" — outside the image ──────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screenHorizontal),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
            Spacer(Modifier.width(Spacing.sm))
            Icon(Icons.Default.Favorite, null, Modifier.size(9.dp), Gold.copy(alpha = 0.65f))
            Spacer(Modifier.width(6.dp))
            Text(
                text = "WE'RE GETTING MARRIED",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp,
                    fontSize = 12.sp
                ),
                color = Gold.copy(alpha = 0.85f),
            )
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Default.Favorite, null, Modifier.size(9.dp), Gold.copy(alpha = 0.65f))
            Spacer(Modifier.width(Spacing.sm))
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

        Spacer(Modifier.height(Spacing.md))

        // ── Bride & groom photo card with blended names ──────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screenHorizontal)
                .height(400.dp)
                .clip(RoundedCornerShape(28.dp)),
        ) {
            // Bride & groom photo — prefer uploaded cover image, fallback to placeholder
            if (wedding.coverImageUrl.isNotBlank()) {
                AsyncImage(
                    model = wedding.coverImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(R.drawable.home_placeholder),
                    placeholder = painterResource(R.drawable.home_placeholder),
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.home_placeholder),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            if (wedding.coverImageUrl.isNotBlank()) {
                // Bottom fade — main text zone
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.00f to Color.Transparent,
                                0.30f to Color.Transparent,
                                1.00f to Color.Black.copy(alpha = 0.62f),
                            )
                        )
                )
                // Left edge fade
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                0.00f to Color.Black.copy(alpha = 0.38f),
                                0.22f to Color.Transparent,
                            )
                        )
                )
                // Right edge fade
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                0.78f to Color.Transparent,
                                1.00f to Color.Black.copy(alpha = 0.38f),
                            )
                        )
                )
                // Top edge fade — very subtle, seals the top
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.00f to Color.Black.copy(alpha = 0.28f),
                                0.18f to Color.Transparent,
                            )
                        )
                )
            }


            // Names in negative space (bottom) — Screen blended, low opacity, no UI chrome
            val serifFamily = MaterialTheme.typography.displayLarge.fontFamily
            val ampIdx = wedding.name.indexOf(" & ")

            if (ampIdx != -1) {
                val first = wedding.name.substring(0, ampIdx)
                val second = wedding.name.substring(ampIdx + 3)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                            blendMode = BlendMode.Screen
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = first,
                        style = TextStyle(
                            fontFamily = DancingScript,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 54.sp,
                            color = Color.White.copy(alpha = 0.40f)
                        ),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "&",
                        style = TextStyle(
                            fontFamily = serifFamily,
                            fontStyle = FontStyle.Italic,
                            fontSize = 20.sp,
                            color = Color.White.copy(alpha = 0.28f),
                            letterSpacing = 6.sp
                        ),
                    )
                    Text(
                        text = second,
                        style = TextStyle(
                            fontFamily = DancingScript,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 54.sp,
                            color = Color.White.copy(alpha = 0.40f)
                        ),
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Text(
                    text = wedding.name,
                    style = TextStyle(
                        fontFamily = DancingScript,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 54.sp,
                        color = Color.White.copy(alpha = 0.40f)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                            blendMode = BlendMode.Screen
                        },
                )
            }
        }
    }
}

// ── Countdown section ─────────────────────────────────────────────────────────

@Composable
private fun CountdownSection(dateMs: Long, modifier: Modifier = Modifier) {
    var countdown by remember { mutableStateOf(timeUntilWedding(dateMs)) }

    LaunchedEffect(dateMs) {
        while (true) {
            countdown = timeUntilWedding(dateMs)
            delay(1_000L)
        }
    }

    val isZero =
        countdown == null || countdown!!.run { days == 0L && hours == 0L && minutes == 0L && seconds == 0L }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isZero) {
            Text(
                text = "It's time to celebrate! 🎉",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF1C1C1E),
                textAlign = TextAlign.Center,
            )
        } else {
            val cd = countdown!!
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
            ) {
                CountdownUnit(cd.days, "DAYS")
                CountdownBar()
                CountdownUnit(cd.hours, "HRS")
                CountdownBar()
                CountdownUnit(cd.minutes, "MINS")
                CountdownBar()
                CountdownUnit(cd.seconds, "SECS")
            }
        }
    }
}

@Composable
private fun CountdownUnit(value: Long, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(58.dp),
    ) {
        Text(
            text = "%02d".format(value),
            style = TextStyle(
                fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                fontWeight = FontWeight.Light,
                fontSize = 38.sp,
                color = Color(0xFF1C1C1E),
            ),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.8.sp,
                fontSize = 8.sp
            ),
            color = Color(0xFFAAAAAA),
        )
    }
}

@Composable
private fun CountdownBar() {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(1.dp)
            .height(38.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
    )
}

// ── Wedding details — scrollable minimal glass chips ─────────────────────────

@Composable
private fun WeddingDetailsRow(
    wedding: Wedding,
    onNavigateToWeddingInfo: () -> Unit,
    onNavigateToTimeline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Spacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item {
            WeddingDetailCard(
                icon = Icons.Default.CalendarToday,
                label = "Date",
                value = if (wedding.date == 0L) "TBA" else formatWeddingDate(wedding.date),
                gradient = Brush.linearGradient(listOf(Color(0xFFFAF9F7), Color(0xFFF3F1ED))),
                iconTint = Color(0xFFC9A84C),
                onClick = { openCalendar(context, wedding) },
            )
        }
        item {
            WeddingDetailCard(
                icon = Icons.Default.LocationOn,
                label = "Venue",
                value = wedding.location.ifBlank { "TBA" },
                gradient = Brush.linearGradient(listOf(Color(0xFFFAF9F7), Color(0xFFF3F1ED))),
                iconTint = Color(0xFFC9A84C),
                onClick = {
                    if (wedding.location.isNotBlank()) openMaps(
                        context,
                        wedding.location
                    )
                },
            )
        }
        item {
            WeddingDetailCard(
                icon = Icons.Default.Restaurant,
                label = "Menu",
                value = if (wedding.menu.isEmpty()) "TBA" else "${wedding.menu.size} courses",
                gradient = Brush.linearGradient(listOf(Color(0xFFFAF9F7), Color(0xFFF3F1ED))),
                iconTint = Color(0xFFC9A84C),
                onClick = onNavigateToWeddingInfo,
            )
        }
        item {
            WeddingDetailCard(
                icon = Icons.Default.Checkroom,
                label = "Dress Code",
                value = wedding.dressCode.style.ifBlank { "TBA" },
                gradient = Brush.linearGradient(listOf(Color(0xFFFAF9F7), Color(0xFFF3F1ED))),
                iconTint = Color(0xFFC9A84C),
                onClick = onNavigateToWeddingInfo,
            )
        }
        item {
            WeddingDetailCard(
                icon = Icons.Default.Schedule,
                label = "Schedule",
                value = if (wedding.timeline.isEmpty()) "TBA" else "${wedding.timeline.size} events",
                gradient = Brush.linearGradient(listOf(Color(0xFFFAF9F7), Color(0xFFF3F1ED))),
                iconTint = Color(0xFFC9A84C),
                onClick = onNavigateToTimeline,
            )
        }
    }
}

@Composable
private fun WeddingDetailCard(
    icon: ImageVector,
    label: String,
    value: String,
    gradient: Brush,
    iconTint: Color,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
        label = "chipScale",
    )

    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .width(130.dp)
            .height(88.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(gradient)
            .border(
                width = 0.8.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.80f), Color.White.copy(alpha = 0.10f))
                ),
                shape = RoundedCornerShape(18.dp),
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint.copy(alpha = 0.75f),
                modifier = Modifier.size(15.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 7.sp,
                        letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    color = Color(0xFFAAAAAA),
                )
                Text(
                    text = value,
                    style = TextStyle(
                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = Color(0xFF2C2C2C),
                        letterSpacing = (-0.2).sp,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ── Feature preview section ───────────────────────────────────────────────────

@Composable
private fun FeaturePreviewSection(
    onNavigateToPhotos: () -> Unit,
    onNavigateToGuestbook: () -> Unit,
    onNavigateToWeddingInfo: () -> Unit,
    onNavigateToGuests: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        // ── Section ornament ──────────────────────────────────────────────────
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
                            listOf(Color.Transparent, Gold.copy(alpha = 0.30f))
                        )
                    )
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Memories & Moments",
                style = TextStyle(
                    fontFamily = DancingScript,
                    fontSize = 17.sp,
                    color = WarmGray500,
                ),
            )
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier
                    .weight(1f)
                    .height(0.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Gold.copy(alpha = 0.30f), Color.Transparent)
                        )
                    )
            )
        }

        Spacer(Modifier.height(4.dp))

        // ── Row 1: Photos + Guestbook ─────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            MemoryCard(
                title = "Photo Gallery",
                description = "Every frame, a forever memory",
                badgeText = "MEMORIES",
                onClick = onNavigateToPhotos,
                modifier = Modifier
                    .weight(1f)
                    .height(200.dp),
            )
            MemoryCard(
                title = "Guestbook",
                description = "Love notes from those who matter most",
                badgeText = "WISHES",
                onClick = onNavigateToGuestbook,
                modifier = Modifier
                    .weight(1f)
                    .height(200.dp),
            )
        }

        // ── Row 2: Wedding Info + Guests ──────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            MemoryCard(
                title = "Wedding Info",
                description = "Venue, menu & all the details",
                badgeText = "DETAILS",
                onClick = onNavigateToWeddingInfo,
                modifier = Modifier
                    .weight(1f)
                    .height(200.dp),
            )
            MemoryCard(
                title = "Our Guests",
                description = "Those celebrating with us",
                badgeText = "GUESTS",
                onClick = onNavigateToGuests,
                modifier = Modifier
                    .weight(1f)
                    .height(200.dp),
            )
        }
    }
}

// ── Memory card — design.png style: light ivory card, corner brackets, serif title ──

@Composable
private fun MemoryCard(
    title: String,
    description: String,
    badgeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
        label = "cardScale",
    )

    // Use first letter of badge label as watermark (M, W, D, G …)
    val watermarkLetter = badgeText.firstOrNull()?.toString() ?: ""

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Gold.copy(alpha = 0.10f),
                spotColor = Color(0xFF8B7355).copy(alpha = 0.08f),
            )
            .clip(RoundedCornerShape(20.dp))
            // Warm ivory card background — matches design.png card surface
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFCFAF6), Color(0xFFF0E8DC))
                )
            )
            .border(
                width = 0.6.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Gold.copy(alpha = 0.30f),
                        Color.White.copy(alpha = 0.70f),
                        Gold.copy(alpha = 0.18f),
                    )
                ),
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
    ) {

        // ── Large watermark letter — same tone as card, barely visible ────────
        Text(
            text = watermarkLetter,
            style = TextStyle(
                fontFamily = CormorantGaramond,
                fontWeight = FontWeight.Bold,
                fontSize = 64.sp,
                color = Color(0xFFE5DDD2).copy(alpha = 0.2f),
            ),
            modifier = Modifier.align(Alignment.TopEnd),
        )

        // ── Corner bracket ornaments — thin gold L-shapes, top-left & top-right
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bLen = 14.dp.toPx()
            val bW = 1.2.dp.toPx()
            val pad = 13.dp.toPx()
            val col = Color(0xFFB8975A).copy(alpha = 0.45f)

            // Top-left bracket
            drawLine(col, Offset(pad, pad), Offset(pad + bLen, pad), bW, StrokeCap.Round)
            drawLine(col, Offset(pad, pad), Offset(pad, pad + bLen), bW, StrokeCap.Round)
        }

        // ── Content: label → serif title → description ────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            // Small-caps gold label  e.g. "MEMORIES"
            Text(
                text = badgeText,
                style = TextStyle(
                    fontFamily = DmSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 8.sp,
                    letterSpacing = 1.8.sp,
                    color = Gold,
                ),
            )
            Spacer(Modifier.height(5.dp))

            // Large Cormorant Garamond title — matches design.png card headings
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    lineHeight = 28.sp,
                    letterSpacing = (-0.2).sp,
                    color = Color(0xFF2A1F14),
                ),
            )
            Spacer(Modifier.height(4.dp))

            // Short description in warm gray
            Text(
                text = description,
                style = TextStyle(
                    fontFamily = DmSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF8C6E5A),
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}


// ── Chat whisper row — subtle live chat entry point ───────────────────────────

@Composable
private fun ChatWhisperRow(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val bgAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.10f else 0.05f,
        animationSpec = tween(150),
        label = "chatWhisperBg",
    )
    val transition = rememberInfiniteTransition(label = "float")
    val floatY by transition.animateFloat(
        initialValue = 0f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "float_y",
    )


    Row(
        modifier = modifier
            .fillMaxWidth()
            .offset(y = floatY.dp)
            .padding(horizontal = Spacing.screenHorizontal)
            .clip(RoundedCornerShape(50.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF8B7355).copy(alpha = bgAlpha),
                        Gold.copy(alpha = bgAlpha * 1.4f),
                        Color(0xFF8B7355).copy(alpha = bgAlpha),
                    )
                )
            )
            .border(
                width = 0.6.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        Gold.copy(alpha = 0.14f),
                        Gold.copy(alpha = 0.26f),
                        Gold.copy(alpha = 0.14f),
                    )
                ),
                shape = RoundedCornerShape(50.dp),
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Chat,
            contentDescription = "Live chat",
            tint = Gold.copy(alpha = if (isPressed) 0.70f else 0.48f),
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = "Live Moments",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                letterSpacing = 0.6.sp,
            ),
            color = WarmGray500.copy(alpha = if (isPressed) 0.85f else 0.62f),
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Gold.copy(alpha = if (isPressed) 0.55f else 0.28f),
            modifier = Modifier.size(13.dp),
        )
    }
}

// ── Elegant footer ────────────────────────────────────────────────────────────

@Composable
private fun ElegantFooter(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = Gold.copy(alpha = 0.55f),
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = "Thank you for being part of our special day ✦",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.md))
        // Thin gold accent line
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Gold.copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                    )
                ),
        )
    }
}


@Composable
private fun PetalBurstSymbol(color: Color) {
    val ts = rememberInfiniteTransition(label = "burst")
    val rot by ts.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart),
        label = "sRot",
    )
    val pulse by ts.animateFloat(
        initialValue = 0.88f, targetValue = 1.00f,
        animationSpec = infiniteRepeatable(
            tween(2200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "sPulse",
    )
    Canvas(modifier = Modifier.size(60.dp, 50.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        for (i in 0 until 8) {
            val isLong = i % 2 == 0
            val petalH = (if (isLong) size.height * 0.37f else size.height * 0.22f) * pulse
            val petalW = (if (isLong) 5.2f else 3.4f) * pulse
            val alpha = if (isLong) 0.82f else 0.48f
            withTransform({
                rotate(i * 45f + rot, Offset(cx, cy))
            }) {
                drawOval(
                    color = color.copy(alpha = alpha),
                    topLeft = Offset(cx - petalW / 2f, cy - petalH),
                    size = Size(petalW, petalH * 0.65f),
                )
            }
        }
        drawCircle(color.copy(alpha = 0.88f), 4.6f * pulse, Offset(cx, cy))
        drawCircle(Color.White.copy(alpha = 0.55f), 2.0f * pulse, Offset(cx, cy))
    }
}

// ── Share Invitation pill — "Spread the Joy" ──────────────────────────────────

@Composable
private fun GlassNavPill(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
        label = "pillScale",
    )

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(400L)
        entered = true
    }
    val entryAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(340, easing = FastOutSlowInEasing),
        label = "pillAlpha",
    )
    val entrySlide by animateFloatAsState(
        targetValue = if (entered) 0f else 18f,
        animationSpec = tween(340, easing = FastOutSlowInEasing),
        label = "pillSlide",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale; scaleY = scale
                alpha = entryAlpha
                translationY = entrySlide
            }
            .shadow(
                elevation = if (isPressed) 2.dp else 8.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = GoldDeep.copy(alpha = 0.18f),
                spotColor = GoldDeep.copy(alpha = 0.28f),
            )
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFBF5E4), Color(0xFFF5EDD4), Color(0xFFFAF4E6))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Gold.copy(alpha = 0.72f),
                        Color(0xFFFFE599).copy(alpha = 0.44f),
                        Gold.copy(alpha = 0.58f),
                    )
                ),
                shape = RoundedCornerShape(22.dp),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PetalBurstSymbol(color = GoldDeep)

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "Spread the Joy",
                style = TextStyle(
                    fontFamily = DancingScript,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color(0xFF2E2017),
                    letterSpacing = 0.sp,
                ),
            )
            Text(
                text = "Share invitation  ·  QR code  ·  PDF",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp, letterSpacing = 0.3.sp,
                ),
                color = Color(0xFF9B8467).copy(alpha = 0.88f),
            )
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Gold.copy(alpha = 0.16f))
                .border(0.8.dp, Gold.copy(alpha = 0.40f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = GoldDeep,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// ── Intent helpers ────────────────────────────────────────────────────────────

private fun openCalendar(context: Context, wedding: Wedding) {
    if (wedding.date == 0L) return
    val startMs = wedding.date
    val endMs = startMs + 8 * 60 * 60 * 1000L
    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, "Wedding Day — ${wedding.name}")
        putExtra(CalendarContract.Events.EVENT_LOCATION, wedding.location)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMs)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMs)
        putExtra(CalendarContract.Events.DESCRIPTION, "Celebrating the wedding of ${wedding.name}")
    }
    runCatching { context.startActivity(intent) }
}

private fun openMaps(context: Context, location: String) {
    val encoded = Uri.encode(location)
    val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$encoded"))
        .setPackage("com.google.android.apps.maps")
    if (mapsIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapsIntent)
    } else {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encoded")))
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatWeddingDate(ms: Long): String {
    if (ms == 0L) return ""
    val utc = java.util.TimeZone.getTimeZone("UTC")
    val date = Date(ms)
    val dateFmt = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).apply { timeZone = utc }
    val cal = Calendar.getInstance(utc).apply { time = date }
    if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0) return dateFmt.format(
        date
    )
    val timeFmt = SimpleDateFormat("h:mm a", Locale.ENGLISH).apply { timeZone = utc }
    return "${dateFmt.format(date)} • ${timeFmt.format(date)}"
}

private fun timeUntilWedding(dateMs: Long): CountdownTime? {
    if (dateMs == 0L) return null
    val diff = dateMs - System.currentTimeMillis()
    if (diff <= 0L) return CountdownTime(0, 0, 0, 0)
    return CountdownTime(
        days = TimeUnit.MILLISECONDS.toDays(diff),
        hours = TimeUnit.MILLISECONDS.toHours(diff) % 24L,
        minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60L,
        seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60L,
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val _previewWedding = com.wednowapp.wednow.domain.model.Wedding(
    id = "w1", shortCode = "WED123",
    name = "Sophie & James",
    date = 1781481600000L,
    location = "Grand Ballroom, New York",
    adminGuestId = "g1",
    createdAt = 1_700_000_000_000L,
)

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    name = "Guest Pass Card – Guest"
)
@Composable
private fun GuestPassCardPreview() {
    com.wednowapp.wednow.ui.theme.WedNowTheme {
        GuestPassCard(
            userName = "James Walker",
            guestRole = com.wednowapp.wednow.domain.model.GuestRole.GUEST,
            onClick = {},
            modifier = androidx.compose.ui.Modifier.padding(16.dp),
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    name = "Guest Pass Card – Admin"
)
@Composable
private fun GuestPassCardAdminPreview() {
    com.wednowapp.wednow.ui.theme.WedNowTheme {
        GuestPassCard(
            userName = "Sophie Walker",
            guestRole = com.wednowapp.wednow.domain.model.GuestRole.ADMIN,
            onClick = {},
            modifier = androidx.compose.ui.Modifier.padding(16.dp),
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Hero Section")
@Composable
private fun HeroSectionPreview() {
    com.wednowapp.wednow.ui.theme.WedNowTheme {
        HeroSection(wedding = _previewWedding)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Countdown – Future")
@Composable
private fun CountdownPreview() {
    com.wednowapp.wednow.ui.theme.WedNowTheme {
        CountdownSection(dateMs = System.currentTimeMillis() + 86_400_000L * 47)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Countdown – Future")
@Composable
private fun MemoryCardPreview() {
    com.wednowapp.wednow.ui.theme.WedNowTheme {
        MemoryCard(
            title = "Photo Gallery",
            description = "Every frame a forever memory",
            badgeText = "Memories",
            onClick = {})
    }
}
