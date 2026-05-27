package com.wednowapp.wednow.presentation.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
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
import com.wednowapp.wednow.domain.model.Wedding
import com.wednowapp.wednow.ui.components.WedNowErrorScreen
import com.wednowapp.wednow.ui.components.WedNowLoadingScreen
import com.wednowapp.wednow.ui.theme.BlushDeep
import com.wednowapp.wednow.ui.theme.BlushLight
import com.wednowapp.wednow.ui.theme.Champagne
import com.wednowapp.wednow.ui.theme.ChampagneLight
import com.wednowapp.wednow.ui.theme.Gold
import com.wednowapp.wednow.ui.theme.GoldDeep
import com.wednowapp.wednow.ui.theme.GoldLight
import com.wednowapp.wednow.ui.theme.Ivory
import com.wednowapp.wednow.ui.theme.Spacing
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
                onNavigateToRSVP = onNavigateToRSVP,
                onNavigateToGuestbook = onNavigateToGuestbook,
                onNavigateToPhotos = onNavigateToPhotos,
                onNavigateToWeddingInfo = onNavigateToWeddingInfo,
                onNavigateToGuests = onNavigateToGuests,
                onNavigateToChat = onNavigateToChat,
                onNavigateToTimeline = onNavigateToTimeline,
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
    onNavigateToRSVP: () -> Unit,
    onNavigateToGuestbook: () -> Unit,
    onNavigateToPhotos: () -> Unit,
    onNavigateToWeddingInfo: () -> Unit,
    onNavigateToGuests: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToTimeline: () -> Unit,
    onNavigateToShareInvitation: () -> Unit,
) {
    var showNavHub by remember { mutableStateOf(false) }
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
                    onNavigateToPhotos,
                    onNavigateToGuestbook,
                    Modifier.alpha(previewAlpha)
                )
            }
            item { Spacer(Modifier.height(Spacing.xxl)) }
            item { ElegantFooter(Modifier.alpha(previewAlpha)) }
            item { Spacer(Modifier.height(Spacing.xl)) }
        }
    }

    if (showNavHub) {
        NavHubBottomSheet(
            onDismiss = { showNavHub = false },
            onNavigateToRSVP = onNavigateToRSVP,
            onNavigateToPhotos = onNavigateToPhotos,
            onNavigateToGuestbook = onNavigateToGuestbook,
            onNavigateToGuests = onNavigateToGuests,
            onNavigateToChat = onNavigateToChat,
            onNavigateToWeddingInfo = onNavigateToWeddingInfo,
            onNavigateToTimeline = onNavigateToTimeline,
            isPrivileged = isPrivileged,
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
                color = BlushDeep,
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
                fontWeight = FontWeight.SemiBold,
                fontSize = 38.sp,
                color = BlushDeep,
            ),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
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

// ── Wedding details grid (5 tiles) ───────────────────────────────────────────

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
            InfoChip(
                Icons.Default.CalendarToday,
                BlushLight,
                BlushDeep,
                "Date",
                wedding.date.ifBlank { "TBA" }) { openCalendar(context, wedding) }
        }
        item {
            InfoChip(
                Icons.Default.LocationOn,
                ChampagneLight,
                Gold,
                "Venue",
                wedding.location.ifBlank { "TBA" }) {
                if (wedding.location.isNotBlank()) openMaps(
                    context,
                    wedding.location
                )
            }
        }
        item {
            InfoChip(
                Icons.Default.Restaurant,
                Color(0xFFD8EED8),
                Color(0xFF5A8A5A),
                "Menu",
                if (wedding.menu.isEmpty()) "TBA" else "${wedding.menu.size} Courses",
                onNavigateToWeddingInfo
            )
        }
        item {
            InfoChip(
                Icons.Default.Checkroom,
                Color(0xFFF5E8F2),
                Color(0xFFB885A8),
                "Dress Code",
                wedding.dressCode.style.ifBlank { "TBA" },
                onNavigateToWeddingInfo
            )
        }
        item {
            InfoChip(
                Icons.Default.Schedule,
                GoldLight,
                GoldDeep,
                "Schedule",
                if (wedding.timeline.isEmpty()) "TBA" else "${wedding.timeline.size} Events",
                onNavigateToTimeline
            )
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(50.dp),
        colors = CardDefaults.cardColors(containerColor = Ivory),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, Modifier.size(14.dp), iconTint)
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.5.sp,
                        fontSize = 8.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f),
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                    maxLines = 1,
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
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Enter",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

// ── Feature preview cards ─────────────────────────────────────────────────────

@Composable
private fun FeaturePreviewSection(
    onNavigateToPhotos: () -> Unit,
    onNavigateToGuestbook: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        FeaturePreviewCard(
            icon = Icons.Default.PhotoLibrary,
            iconBg = GoldDeep,
            iconTint = ChampagneLight,
            title = "Photos",
            subtitle = "Relive beautiful moments",
            onClick = onNavigateToPhotos,
            modifier = Modifier.weight(1f),
        )
        FeaturePreviewCard(
            icon = Icons.Default.MenuBook,
            iconBg = ChampagneLight,
            iconTint = GoldDeep,
            title = "Guestbook",
            subtitle = "Share your love and wishes",
            onClick = onNavigateToGuestbook,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun FeaturePreviewCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Ivory),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                ),
                color = MaterialTheme.colorScheme.primaryContainer,
                textAlign = TextAlign.Center,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.60f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "→",
                style = TextStyle(fontSize = 14.sp, color = Gold, fontWeight = FontWeight.Normal),
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
        // Thin blush accent line
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            BlushDeep.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                    )
                ),
        )
    }
}

// ── Navigation hub bottom sheet ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavHubBottomSheet(
    onDismiss: () -> Unit,
    onNavigateToRSVP: () -> Unit,
    onNavigateToPhotos: () -> Unit,
    onNavigateToGuestbook: () -> Unit,
    onNavigateToGuests: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToWeddingInfo: () -> Unit,
    onNavigateToTimeline: () -> Unit,
    isPrivileged: Boolean,
    onNavigateToShareInvitation: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Ivory,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screenHorizontal)
                .navigationBarsPadding(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Your Wedding",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Choose where you'd like to go",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
            Spacer(Modifier.height(Spacing.md))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(Spacing.xs))
            NavHubItem(
                Icons.Default.HowToReg,
                "RSVP",
                "Confirm your attendance",
                onClick = { onDismiss(); onNavigateToRSVP() })
            NavHubItem(
                Icons.Default.PhotoLibrary,
                "Photos",
                "View & share moments",
                onClick = { onDismiss(); onNavigateToPhotos() })
            NavHubItem(
                Icons.Default.MenuBook,
                "Guestbook",
                "Leave a memory",
                onClick = { onDismiss(); onNavigateToGuestbook() })
            NavHubItem(
                Icons.Default.PeopleAlt,
                "Guests",
                "See who's celebrating",
                onClick = { onDismiss(); onNavigateToGuests() })
            NavHubItem(
                Icons.Default.Forum,
                "Chat",
                "Talk to everyone",
                onClick = { onDismiss(); onNavigateToChat() })
            NavHubItem(
                Icons.Default.Info,
                "Wedding Info",
                "Venue & schedule details",
                onClick = { onDismiss(); onNavigateToWeddingInfo() })
            NavHubItem(
                Icons.Default.Schedule,
                "Day Timeline",
                "Follow the event schedule",
                showDivider = isPrivileged,
                onClick = { onDismiss(); onNavigateToTimeline() })
            if (isPrivileged) {
                NavHubItem(
                    Icons.Default.Share,
                    "Share Invitation",
                    "QR code · share link · save PDF",
                    showDivider = false,
                    onClick = { onDismiss(); onNavigateToShareInvitation() })
            }
            Spacer(Modifier.height(Spacing.lg))
        }
    }
}

@Composable
private fun NavHubItem(
    icon: ImageVector,
    label: String,
    subtitle: String,
    showDivider: Boolean = true,
    onClick: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(BlushLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BlushDeep,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp),
            )
        }
        if (showDivider) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
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
