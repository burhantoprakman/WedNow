package com.wednowapp.wednow.presentation.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.wednowapp.wednow.ui.theme.BlushDeep
import com.wednowapp.wednow.ui.theme.Champagne
import com.wednowapp.wednow.ui.theme.ChampagneLight
import com.wednowapp.wednow.ui.theme.Gold
import com.wednowapp.wednow.ui.theme.GoldDeep
import com.wednowapp.wednow.ui.theme.Ivory
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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

        // Full-screen background image
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
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
            item { Spacer(Modifier.height(Spacing.xl)) }
            item { WeddingExperienceCard({ showNavHub = true }, Modifier.alpha(ctaAlpha)) }
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
            item { Spacer(Modifier.height(Spacing.xxl)) }
            item { ElegantFooter(Modifier.alpha(previewAlpha)) }
            item { Spacer(Modifier.height(Spacing.lg)) }
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
            onDismiss = { showAccountSheet = false },
            onSignIn = { showAccountSheet = false; showSignInSheet = true },
            onSignOut = { authViewModel.signOut(); showAccountSheet = false },
            onPrivacyPolicy = {
                context.startActivity(
                    android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://wednow.app/privacy"),
                    )
                )
            },
            onTermsAndConditions = {
                context.startActivity(
                    android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://wednow.app/terms"),
                    )
                )
            },
            onContactSupport = {
                context.startActivity(
                    android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("mailto:hello@wednow.app"),
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

    if (showNavHub) {
        NavHubBottomSheet(
            onDismiss = { showNavHub = false },
            onNavigateToRSVP = onNavigateToRSVP,
            onNavigateToPhotos = onNavigateToPhotos,
            onNavigateToGuestbook = onNavigateToGuestbook,
            onNavigateToGuests = onNavigateToGuests,
            onNavigateToChat = onNavigateToChat,
            onNavigateToNotifications = onNavigateToNotifications,
            isPrivileged = isPrivileged,
            unreadNotificationCount = unreadNotificationCount,
            onNavigateToShareInvitation = onNavigateToShareInvitation,
        )
    }
}

// ── Menu button ───────────────────────────────────────────────────────────────

@Composable
private fun MenuButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.size(44.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Ivory),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp),
            )
        }
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
                    error = painterResource(R.drawable.homeimage),
                    placeholder = painterResource(R.drawable.homeimage),
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.homeimage),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

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
private fun CountdownSection(dateStr: String, modifier: Modifier = Modifier) {
    var countdown by remember { mutableStateOf(timeUntilWedding(dateStr)) }

    LaunchedEffect(dateStr) {
        while (true) {
            countdown = timeUntilWedding(dateStr)
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
                value = wedding.date.ifBlank { "TBA" },
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

// ── Wedding experience card ───────────────────────────────────────────────────

@Composable
private fun WeddingExperienceCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
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

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal)
            .offset(y = floatY.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Ivory),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.cardMd, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            // Champagne gradient icon tile
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Champagne, ChampagneLight))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(22.dp),
                )
            }

            // Title + subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Enter Wedding Experience",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Explore all features",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f),
                )
            }

            // Gold arrow circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Gold),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Enter",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
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
                poeticLine = "Every frame, a forever memory",
                badgeText = "MEMORIES",
                backgroundRes = R.drawable.homeimage,
                accentTint = Color.Black.copy(alpha = 0.05f),
                gradientEnd = Color.Black.copy(alpha = 0.72f),
                onClick = onNavigateToPhotos,
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp),
            )
            MemoryCard(
                title = "Guestbook",
                poeticLine = "Love notes from those who matter most",
                badgeText = "WISHES",
                backgroundRes = R.drawable.homeimage,
                accentTint = Color.Black.copy(alpha = 0.05f),
                gradientEnd = Color.Black.copy(alpha = 0.72f),
                onClick = onNavigateToGuestbook,
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp),
            )
        }

        // ── Row 2: Wedding Info + Guests ──────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            MemoryCard(
                title = "Wedding Info",
                poeticLine = "Venue, menu & all the details",
                badgeText = "DETAILS",
                backgroundRes = R.drawable.homeimage,
                accentTint = Color.Black.copy(alpha = 0.05f),
                gradientEnd = Color.Black.copy(alpha = 0.72f),
                onClick = onNavigateToWeddingInfo,
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp),
            )
            MemoryCard(
                title = "Our Guests",
                poeticLine = "Those celebrating with us",
                badgeText = "GUESTS",
                backgroundRes = R.drawable.homeimage,
                accentTint = Color.Black.copy(alpha = 0.05f),
                gradientEnd = Color.Black.copy(alpha = 0.72f),
                onClick = onNavigateToGuests,
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp),
            )
        }
    }
}

// ── Memory card (Photos / Guestbook premium cards) ────────────────────────────

@Composable
private fun MemoryCard(
    title: String,
    poeticLine: String,
    badgeText: String,
    backgroundRes: Int,
    accentTint: Color,
    gradientEnd: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // ── Press scale ───────────────────────────────────────────────────────────
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "cardScale",
    )

    // ── Gold sparkle glow ─────────────────────────────────────────────────────
    var glowTick by remember { mutableIntStateOf(0) }
    val glowAlpha = remember { Animatable(0f) }
    LaunchedEffect(glowTick) {
        if (glowTick == 0) return@LaunchedEffect
        glowAlpha.snapTo(0f)
        glowAlpha.animateTo(1f, tween(220, easing = FastOutSlowInEasing))
        kotlinx.coroutines.delay(100)
        glowAlpha.animateTo(0f, tween(500))
    }

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                glowTick++
                onClick()
            },
    ) {
        // ── Background photo ──────────────────────────────────────────────────
        Image(
            painter = painterResource(backgroundRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // ── Per-card colour tint (gold warm / blush romantic) ─────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(accentTint)
        )

        // ── Bottom vignette for text legibility ───────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.00f to Color.Transparent,
                        0.28f to Color.Transparent,
                        1.00f to gradientEnd,
                    )
                )
        )

        // ── Text content ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            // Frosted glass badge chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontSize = 8.sp,
                    ),
                    color = Gold,
                )
            }
            Spacer(Modifier.height(6.dp))

            // Script title (DancingScript — luxury wedding feel)
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = DancingScript,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 26.sp,
                    color = Color.White,
                    letterSpacing = (-0.3).sp,
                ),
            )
            Spacer(Modifier.height(3.dp))

            // Italic poetic subtitle
            Text(
                text = poeticLine,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                ),
                color = Color.White.copy(alpha = 0.80f),
            )
        }

        // ── Gold sparkle border — animates on tap, fades out ─────────────────
        val ga = glowAlpha.value
        if (ga > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = (2.5f * ga).dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Gold.copy(alpha = ga),
                                Color(0xFFF0DFA0).copy(alpha = ga * 0.65f),
                                Gold.copy(alpha = ga),
                            )
                        ),
                        shape = RoundedCornerShape(24.dp),
                    )
            )
        }
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

// ── Wedding Experience Hub — warm glass portal, wedding-themed canvas symbols ──
//
//  Style: app-native ivory/champagne/gold palette — light glassmorphism.
//  Symbols: hand-drawn via Canvas, each echoes a wedding metaphor:
//    RSVP         → Two interlinked wedding rings with a diamond sparkle
//    Guests        → Six hearts rotating in a floral wreath
//    Chat          → Champagne bubbles drifting upward
//    Notifications → Water-ripple rings expanding from a 4-petal blossom
//    Share         → Petal burst radiating from a gold center

// Bubble data — static, used by RisingBubblesSymbol
private data class BubbleSpec(val phase: Float, val xFrac: Float, val radius: Float)

private val WeddingBubbles = listOf(
    BubbleSpec(0.00f, 0.36f, 5.0f),
    BubbleSpec(0.28f, 0.55f, 3.4f),
    BubbleSpec(0.54f, 0.44f, 6.0f),
    BubbleSpec(0.13f, 0.28f, 3.9f),
    BubbleSpec(0.42f, 0.63f, 4.4f),
    BubbleSpec(0.70f, 0.50f, 2.9f),
)

// ── Bottom sheet wrapper ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavHubBottomSheet(
    onDismiss: () -> Unit,
    onNavigateToRSVP: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onNavigateToPhotos: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onNavigateToGuestbook: () -> Unit,
    onNavigateToGuests: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    isPrivileged: Boolean,
    unreadNotificationCount: Int,
    onNavigateToShareInvitation: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        scrimColor = Color.Black.copy(alpha = 0.42f),
        dragHandle = null,
    ) {
        GlassNavHubContent(
            onDismiss = onDismiss,
            onNavigateToRSVP = onNavigateToRSVP,
            onNavigateToGuests = onNavigateToGuests,
            onNavigateToChat = onNavigateToChat,
            onNavigateToNotifications = onNavigateToNotifications,
            isPrivileged = isPrivileged,
            unreadNotificationCount = unreadNotificationCount,
            onNavigateToShareInvitation = onNavigateToShareInvitation,
        )
    }
}

// ── Hub content ───────────────────────────────────────────────────────────────

@Composable
private fun GlassNavHubContent(
    onDismiss: () -> Unit,
    onNavigateToRSVP: () -> Unit,
    onNavigateToGuests: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    isPrivileged: Boolean,
    unreadNotificationCount: Int,
    onNavigateToShareInvitation: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val sheetAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "sheetAlpha",
    )
    val sheetSlide by animateFloatAsState(
        targetValue = if (visible) 0f else 32f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "sheetSlide",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFEF9F4), Color(0xFFFAF2E6), Color(0xFFFCF4EE))
                )
            ),
    ) {
        // ── Ambient glow orbs ──────────────────────────────────────────────────
        Box(
            Modifier
                .size(240.dp)
                .offset(x = 148.dp, y = (-92).dp)
                .background(
                    Brush.radialGradient(
                        listOf(Gold.copy(alpha = 0.11f), Color.Transparent), radius = 360f
                    )
                )
        )
        Box(
            Modifier
                .size(200.dp)
                .offset(x = (-52).dp, y = 84.dp)
                .background(
                    Brush.radialGradient(
                        listOf(BlushDeep.copy(alpha = 0.09f), Color.Transparent), radius = 300f
                    )
                )
        )
        Box(
            Modifier
                .size(170.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 36.dp, y = 10.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFA890CC).copy(alpha = 0.07f), Color.Transparent),
                        radius = 250f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .graphicsLayer { alpha = sheetAlpha; translationY = sheetSlide },
        ) {
            Spacer(Modifier.height(14.dp))

            // Drag handle
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Gold.copy(0.20f), Gold.copy(0.54f), Gold.copy(0.20f))
                        )
                    )
                    .align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(22.dp))

            // ── Header ─────────────────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Your Wedding World",
                    style = TextStyle(
                        fontFamily = DancingScript,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 32.sp,
                        color = Color(0xFF3D2A18),
                        letterSpacing = (-0.3).sp,
                    ),
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        Modifier
                            .width(38.dp)
                            .height(0.7.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, Gold.copy(alpha = 0.56f))
                                )
                            )
                    )
                    Icon(Icons.Default.Favorite, null, Modifier.size(7.dp), Gold.copy(0.62f))
                    Box(
                        Modifier
                            .width(38.dp)
                            .height(0.7.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Gold.copy(alpha = 0.56f), Color.Transparent)
                                )
                            )
                    )
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    text = "Everything your guests experience in one place",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp, letterSpacing = 0.3.sp,
                    ),
                    color = Color(0xFF8B7355).copy(alpha = 0.72f),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(26.dp))

            // ── Row 1: RSVP + Guests ───────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GlassNavCard(
                    label = "Confirm Presence",
                    subtitle = "RSVP",
                    glowColor = Color(0xFFE8918B),
                    entryDelay = 120L,
                    modifier = Modifier
                        .weight(1f)
                        .height(168.dp),
                    onClick = { onDismiss(); onNavigateToRSVP() },
                ) { WeddingRingsSymbol(Color(0xFFD08078)) }

                GlassNavCard(
                    label = "The Circle",
                    subtitle = "Guests",
                    glowColor = Color(0xFFD4A86A),
                    entryDelay = 190L,
                    modifier = Modifier
                        .weight(1f)
                        .height(168.dp),
                    onClick = { onDismiss(); onNavigateToGuests() },
                ) { FloralWreathSymbol(Color(0xFFCB9A52)) }
            }

            Spacer(Modifier.height(12.dp))

            // ── Row 2: Chat + Notifications ────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GlassNavCard(
                    label = "Live Moments",
                    subtitle = "Chat",
                    glowColor = Color(0xFFD4B98A),
                    entryDelay = 260L,
                    modifier = Modifier
                        .weight(1f)
                        .height(168.dp),
                    onClick = { onDismiss(); onNavigateToChat() },
                ) { RisingBubblesSymbol(Color(0xFFD4AF6A)) }

                GlassNavCard(
                    label = "Waves of Updates",
                    subtitle = if (unreadNotificationCount > 0)
                        "$unreadNotificationCount new"
                    else "Notifications",
                    glowColor = Color(0xFFA890CC),
                    entryDelay = 330L,
                    badgeCount = unreadNotificationCount,
                    modifier = Modifier
                        .weight(1f)
                        .height(168.dp),
                    onClick = { onDismiss(); onNavigateToNotifications() },
                ) { WaterRippleSymbol(Color(0xFFAA90C0)) }
            }

            // ── Admin: Spread the Joy ──────────────────────────────────────────
            if (isPrivileged) {
                Spacer(Modifier.height(12.dp))
                GlassNavPill(onClick = { onDismiss(); onNavigateToShareInvitation() })
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

// ── Wedding portal card — glass tile with Canvas symbol ───────────────────────

@Composable
private fun GlassNavCard(
    label: String,
    subtitle: String,
    glowColor: Color,
    entryDelay: Long = 0L,
    badgeCount: Int = 0,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    symbol: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
        label = "cScale",
    )
    val pressGlow by animateFloatAsState(
        targetValue = if (isPressed) 0.20f else 0.10f,
        animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium),
        label = "cGlow",
    )

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(entryDelay)
        entered = true
    }
    val entryAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(360, easing = FastOutSlowInEasing),
        label = "eAlpha",
    )
    val entrySlide by animateFloatAsState(
        targetValue = if (entered) 0f else 20f,
        animationSpec = tween(360, easing = FastOutSlowInEasing),
        label = "eSlide",
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale; scaleY = scale
                alpha = entryAlpha
                translationY = entrySlide
            }
            .shadow(
                elevation = if (isPressed) 3.dp else 10.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = glowColor.copy(alpha = pressGlow),
                spotColor = glowColor.copy(alpha = pressGlow + 0.06f),
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.74f))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Gold.copy(alpha = 0.44f),
                        Color.White.copy(alpha = 0.88f),
                        glowColor.copy(alpha = 0.22f),
                    )
                ),
                shape = RoundedCornerShape(22.dp),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Canvas symbol — vertically centred in remaining space
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                symbol()
            }

            // Label + badge row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 13.dp, end = 11.dp, bottom = 13.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = TextStyle(
                            fontFamily = DancingScript,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = Color(0xFF2E2017),
                            letterSpacing = 0.sp,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = subtitle.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            letterSpacing = 1.4.sp,
                        ),
                        color = glowColor.copy(alpha = 0.78f),
                    )
                }
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Gold.copy(alpha = 0.90f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (badgeCount > 9) "9+" else "$badgeCount",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Color(0xFF3D2A18),
                        )
                    }
                }
            }
        }

        // Soft inner glow on press
        if (isPressed) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(glowColor.copy(alpha = 0.06f)))
        }
    }
}

// ── Canvas wedding symbols ────────────────────────────────────────────────────
//   All colours stay within the app's warm ivory / champagne / blush palette.

// A) Wedding Rings — RSVP
//    Two interlocked arcs (wedding bands) with a 4-point diamond sparkle above.
@Composable
private fun WeddingRingsSymbol(color: Color) {
    val tr = rememberInfiniteTransition(label = "rings")
    val rotation by tr.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Restart),
        label = "rRot",
    )
    val pulse by tr.animateFloat(
        initialValue = 0.88f, targetValue = 1.00f,
        animationSpec = infiniteRepeatable(
            tween(2600, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "rPulse",
    )
    Canvas(modifier = Modifier.size(82.dp, 66.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.height * 0.28f * pulse
        val gap = r * 0.44f

        // Left band
        drawArc(
            color = color.copy(alpha = 0.88f),
            startAngle = rotation + 20f, sweepAngle = 310f, useCenter = false,
            topLeft = Offset(cx - gap - r, cy - r), size = Size(r * 2f, r * 2f),
            style = Stroke(2.4f, cap = StrokeCap.Round),
        )
        // Right band (layered, slightly transparent)
        drawArc(
            color = color.copy(alpha = 0.55f),
            startAngle = rotation + 200f, sweepAngle = 310f, useCenter = false,
            topLeft = Offset(cx + gap - r, cy - r), size = Size(r * 2f, r * 2f),
            style = Stroke(2.4f, cap = StrokeCap.Round),
        )

        // Diamond sparkle — 4-pointed star above the rings
        val scx = cx
        val scy = cy - r * 1.10f
        val sr = r * 0.13f * pulse
        for (k in 0..3) {
            val a = (k * 90.0) * PI / 180.0
            drawLine(
                color = color.copy(alpha = 0.76f),
                start = Offset(scx, scy),
                end = Offset((scx + sr * cos(a)).toFloat(), (scy + sr * sin(a)).toFloat()),
                strokeWidth = 1.8f,
                cap = StrokeCap.Round,
            )
        }
        drawCircle(color.copy(alpha = 0.50f), r * 0.085f, Offset(scx, scy))
    }
}

// B) Floral Wreath — Guests
//    Six hearts arranged in a slowly rotating ring, with dot accents between them.
@Composable
private fun FloralWreathSymbol(color: Color) {
    val tw = rememberInfiniteTransition(label = "wreath")
    val rotation by tw.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "wRot",
    )
    val pulse by tw.animateFloat(
        initialValue = 0.90f, targetValue = 1.00f,
        animationSpec = infiniteRepeatable(
            tween(3000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "wPulse",
    )
    Canvas(modifier = Modifier.size(82.dp, 66.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val ringR = size.height * 0.30f * pulse
        val hs = ringR * 0.22f   // heart half-size

        for (i in 0 until 6) {
            val angle = (i * 60.0 + rotation) * PI / 180.0
            val hx = (cx + ringR * cos(angle)).toFloat()
            val hy = (cy + ringR * sin(angle)).toFloat()
            val alphaF = 0.58f + 0.32f * sin(pulse * PI * 2.0 + i * PI / 3.0).toFloat()

            // Heart bezier path
            val heart = Path().apply {
                moveTo(hx, hy + hs * 0.36f)
                cubicTo(hx - hs, hy - hs * 0.10f, hx - hs, hy - hs * 0.66f, hx, hy - hs * 0.23f)
                cubicTo(hx + hs, hy - hs * 0.66f, hx + hs, hy - hs * 0.10f, hx, hy + hs * 0.36f)
                close()
            }
            drawPath(heart, color.copy(alpha = alphaF))

            // Small dot between hearts
            val mid = ((i + 0.5) * 60.0 + rotation) * PI / 180.0
            val dr = ringR * 0.82f
            drawCircle(
                color.copy(alpha = 0.36f), ringR * 0.055f,
                Offset((cx + dr * cos(mid)).toFloat(), (cy + dr * sin(mid)).toFloat()),
            )
        }
        // Centre blossom
        drawCircle(color.copy(alpha = 0.70f), ringR * 0.20f, Offset(cx, cy))
        drawCircle(Color.White.copy(alpha = 0.55f), ringR * 0.08f, Offset(cx, cy))
    }
}

// C) Rising Champagne Bubbles — Chat
//    Six bubbles at staggered phases drift upward with gentle lateral sway.
@Composable
private fun RisingBubblesSymbol(color: Color) {
    val tb = rememberInfiniteTransition(label = "bubbles")
    val progress by tb.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3800, easing = LinearEasing), RepeatMode.Restart),
        label = "bProg",
    )
    Canvas(modifier = Modifier.size(82.dp, 72.dp)) {
        val w = size.width
        val h = size.height
        WeddingBubbles.forEach { b ->
            val p = (progress + b.phase) % 1f
            val y = h - p * h * 1.12f
            val alpha = when {
                p < 0.12f -> p / 0.12f
                p > 0.78f -> (1f - p) / 0.22f
                else -> 1f
            } * 0.70f
            val drift = (sin(p * PI * 3.0 + b.phase * 7.0) * 4.5).toFloat()
            val x = w * b.xFrac + drift
            if (y < -b.radius || y > h + b.radius) return@forEach

            // Bubble body
            drawCircle(color.copy(alpha = alpha), b.radius, Offset(x, y))
            // Highlight dot
            drawCircle(
                Color.White.copy(alpha = alpha * 0.48f),
                b.radius * 0.33f,
                Offset(x - b.radius * 0.26f, y - b.radius * 0.26f),
            )
            // Thin outline
            drawCircle(
                color.copy(alpha = alpha * 0.22f), b.radius, Offset(x, y),
                style = Stroke(0.9f),
            )
        }
    }
}

// D) Water Ripple + Blossom — Notifications
//    Three expanding ripple rings around a 4-petal flower at the center.
@Composable
private fun WaterRippleSymbol(color: Color) {
    val tp = rememberInfiniteTransition(label = "ripple")
    val progress by tp.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing), RepeatMode.Restart),
        label = "rProg",
    )
    val petalPulse by tp.animateFloat(
        initialValue = 0.88f, targetValue = 1.00f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pPulse",
    )
    Canvas(modifier = Modifier.size(82.dp, 66.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxR = size.height * 0.43f

        // Ripple rings
        for (i in 0..2) {
            val ph = (progress + i * 0.334f) % 1f
            val r = maxR * ph
            val a = (1f - ph) * 0.55f
            drawCircle(color.copy(alpha = a), r, Offset(cx, cy), style = Stroke(1.6f))
        }

        // 4-petal blossom — each petal is an oval rotated outward
        val pr = maxR * 0.24f * petalPulse
        for (i in 0..3) {
            withTransform({
                rotate(i * 90f, Offset(cx, cy))
            }) {
                drawOval(
                    color = color.copy(alpha = 0.65f),
                    topLeft = Offset(cx - pr * 0.24f, cy - pr * 0.92f),
                    size = Size(pr * 0.48f, pr * 0.75f),
                )
            }
        }
        // Blossom centre
        drawCircle(Color(0xFFF5EDD0).copy(alpha = 0.92f), pr * 0.30f, Offset(cx, cy))
    }
}

// E) Petal Burst — Share Invitation (used inside GlassNavPill)
//    Eight alternating-length petals radiating from a gold centre, rotating slowly.
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
    val startMs = parseWeddingDate(wedding.date) ?: return
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

private fun parseWeddingDate(dateStr: String): Long? {
    val formats = listOf(
        "MMMM d, yyyy", "MMM d, yyyy", "MMMM dd, yyyy",
        "MM/dd/yyyy", "dd/MM/yyyy", "yyyy-MM-dd",
        "d MMMM yyyy", "dd MMMM yyyy",
    )
    for (pattern in formats) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
            sdf.isLenient = false
            val date = sdf.parse(dateStr) ?: continue
            val cal = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 14)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            return cal.timeInMillis
        } catch (_: Exception) {
        }
    }
    return null
}

// ── Helper ────────────────────────────────────────────────────────────────────

private fun timeUntilWedding(dateStr: String): CountdownTime? {
    val formats = listOf(
        "MMMM d, yyyy", "MMM d, yyyy", "MMMM dd, yyyy",
        "MM/dd/yyyy", "dd/MM/yyyy", "yyyy-MM-dd",
        "d MMMM yyyy", "dd MMMM yyyy",
    )
    for (pattern in formats) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
            sdf.isLenient = false
            val date: Date = sdf.parse(dateStr) ?: continue
            val diff = date.time - System.currentTimeMillis()
            if (diff <= 0L) return CountdownTime(0, 0, 0, 0)
            return CountdownTime(
                days = TimeUnit.MILLISECONDS.toDays(diff),
                hours = TimeUnit.MILLISECONDS.toHours(diff) % 24L,
                minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60L,
                seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60L,
            )
        } catch (_: Exception) {
        }
    }
    return null
}
