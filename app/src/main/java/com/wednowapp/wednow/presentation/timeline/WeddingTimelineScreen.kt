package com.wednowapp.wednow.presentation.timeline

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wednowapp.wednow.R
import com.wednowapp.wednow.domain.model.TimelineEventData
import com.wednowapp.wednow.domain.model.Wedding
import com.wednowapp.wednow.ui.components.WedNowErrorScreen
import com.wednowapp.wednow.ui.components.WedNowLoadingScreen
import com.wednowapp.wednow.ui.theme.BlushDeep
import com.wednowapp.wednow.ui.theme.BlushLight
import com.wednowapp.wednow.ui.theme.ChampagneLight
import com.wednowapp.wednow.ui.theme.Gold
import com.wednowapp.wednow.ui.theme.GoldLight
import com.wednowapp.wednow.ui.theme.Ivory
import com.wednowapp.wednow.ui.theme.Spacing
import com.wednowapp.wednow.ui.theme.WarmGray100
import com.wednowapp.wednow.ui.theme.WarmGray200
import com.wednowapp.wednow.ui.theme.WarmGray300
import com.wednowapp.wednow.ui.theme.WarmGray400
import com.wednowapp.wednow.ui.theme.WarmGray50
import com.wednowapp.wednow.ui.theme.WarmGray500
import com.wednowapp.wednow.ui.theme.WarmGray600
import com.wednowapp.wednow.ui.theme.WarmGray700
import com.wednowapp.wednow.ui.theme.WarmWhite

// ── Script font ───────────────────────────────────────────────────────────────

private val _tlProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs,
)

private val DancingScript = FontFamily(
    Font(GoogleFont("Dancing Script"), _tlProvider, FontWeight.Normal),
    Font(GoogleFont("Dancing Script"), _tlProvider, FontWeight.SemiBold),
)

// ── Data model ────────────────────────────────────────────────────────────────

enum class EventStatus { COMPLETED, CURRENT, UPCOMING }

data class TimelineEvent(
    val id: Int,
    val time: String,
    val title: String,
    val description: String? = null,
    val icon: ImageVector,
    val emoji: String = "",
    val status: EventStatus,
)

private val knownIconNames = setOf(
    "groups", "wine_bar", "favorite", "local_bar", "restaurant",
    "music_note", "cake", "celebration", "nights_stay", "calendar", "schedule",
)

private fun iconNameToVector(iconName: String): ImageVector = when (iconName.lowercase()) {
    "groups" -> Icons.Default.Groups
    "wine_bar" -> Icons.Default.WineBar
    "favorite" -> Icons.Default.Favorite
    "local_bar" -> Icons.Default.LocalBar
    "restaurant" -> Icons.Default.Restaurant
    "music_note" -> Icons.Default.MusicNote
    "cake" -> Icons.Default.Cake
    "celebration" -> Icons.Default.Celebration
    "nights_stay" -> Icons.Default.NightsStay
    "calendar" -> Icons.Default.CalendarToday
    "schedule" -> Icons.Default.Schedule
    else -> Icons.Default.Celebration
}

private fun TimelineEventData.toTimelineEvent(id: Int): TimelineEvent {
    val isLegacyIcon = iconName.lowercase() in knownIconNames
    return TimelineEvent(
        id = id,
        time = time,
        title = title,
        description = description.ifBlank { null },
        icon = iconNameToVector(iconName),
        emoji = if (isLegacyIcon || iconName.isBlank()) "" else iconName,
        status = when (status.lowercase()) {
            "completed" -> EventStatus.COMPLETED
            "current" -> EventStatus.CURRENT
            else -> EventStatus.UPCOMING
        },
    )
}

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun WeddingTimelineScreen(
    onBack: () -> Unit,
    viewModel: TimelineViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (state) {
        is TimelineState.Loading -> WedNowLoadingScreen()
        is TimelineState.Error   -> WedNowErrorScreen(
            message = (state as TimelineState.Error).message,
            onRetry = viewModel::retry,
        )
        is TimelineState.Success -> {
            val s = state as TimelineState.Success
            TimelineContent(
                wedding = s.wedding,
                events = s.wedding.timeline.mapIndexed { i, it -> it.toTimelineEvent(i) },
                onBack = onBack,
            )
        }
    }
}

// ── Root layout ───────────────────────────────────────────────────────────────

@Composable
private fun TimelineContent(
    wedding: Wedding,
    events: List<TimelineEvent>,
    onBack: () -> Unit,
) {
    val listState    = rememberLazyListState()
    val currentIndex = events.indexOfFirst { it.status == EventStatus.CURRENT }

    LaunchedEffect(events) {
        if (currentIndex >= 1) {
            listState.animateScrollToItem(
                index        = currentIndex + 1, // +1 for header item
                scrollOffset = -160,
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Warm ivory gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ChampagneLight.copy(alpha = 0.65f),
                            Ivory,
                            Ivory,
                            WarmWhite,
                        )
                    )
                )
        )

        TimelineFlorals()

        LazyColumn(
            state          = listState,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 72.dp),
        ) {
            item {
                TimelineHeader(wedding = wedding, onBack = onBack)
            }

            val currentEvent = events.firstOrNull { it.status == EventStatus.CURRENT }
            if (currentEvent != null) {
                item {
                    HappeningNowBanner(event = currentEvent)
                    Spacer(Modifier.height(Spacing.md))
                }
            }

            item { Spacer(Modifier.height(Spacing.sm)) }

            itemsIndexed(
                items = events,
                key   = { _, event -> event.id },
            ) { index, event ->
                TimelineItem(
                    event   = event,
                    isFirst = index == 0,
                    isLast  = index == events.lastIndex,
                    index   = index,
                )
            }

            item { TimelineFooter() }
        }
    }
}

// ── Screen header ─────────────────────────────────────────────────────────────

@Composable
private fun TimelineHeader(wedding: Wedding, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal)
            .padding(top = Spacing.md, bottom = Spacing.lg),
    ) {
        // Back button
        IconButton(
            onClick  = onBack,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.8f)),
        ) {
            Icon(
                imageVector      = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint             = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier         = Modifier.size(20.dp),
            )
        }

        Spacer(Modifier.height(Spacing.md))

        Text(
            text  = "THE BIG DAY",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 3.sp),
            color = Gold.copy(alpha = 0.85f),
        )

        Spacer(Modifier.height(Spacing.xs))

        Text(
            text  = "Wedding Timeline",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(Spacing.xs))

        Text(
            text  = "Follow the celebration throughout the day",
            style = TextStyle(
                fontFamily = DancingScript,
                fontSize   = 20.sp,
                color      = BlushDeep.copy(alpha = 0.85f),
            ),
        )
        Spacer(Modifier.height(Spacing.lg))
        OrnamentDivider()
    }
}

@Composable
private fun OrnamentDivider() {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(WarmGray200)
        )
        Spacer(Modifier.width(Spacing.sm))
        Text("✦", style = MaterialTheme.typography.labelSmall, color = Gold.copy(alpha = 0.65f))
        Spacer(Modifier.width(Spacing.xs))
        Text("✦", style = MaterialTheme.typography.labelSmall, color = Gold.copy(alpha = 0.4f))
        Spacer(Modifier.width(Spacing.xs))
        Text("✦", style = MaterialTheme.typography.labelSmall, color = Gold.copy(alpha = 0.65f))
        Spacer(Modifier.width(Spacing.sm))
        Box(
            Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(WarmGray200)
        )
    }
}

// ── Happening Now banner ──────────────────────────────────────────────────────

@Composable
private fun HappeningNowBanner(event: TimelineEvent) {
    val infiniteTransition = rememberInfiniteTransition(label = "banner_pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(750), RepeatMode.Reverse),
        label = "banner_dot",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(BlushLight, ChampagneLight.copy(alpha = 0.85f))
                )
            )
            .padding(horizontal = Spacing.cardMd, vertical = Spacing.sm),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(dotAlpha)
                .clip(CircleShape)
                .background(BlushDeep),
        )
        Text(
            text  = "Happening Now",
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.5.sp),
            color = BlushDeep,
        )
        Text(
            text  = "·",
            style = MaterialTheme.typography.labelMedium,
            color = BlushDeep.copy(alpha = 0.5f),
        )
        Text(
            text     = event.title,
            style    = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color    = WarmGray700,
            modifier = Modifier.weight(1f),
        )
        Icon(event.icon, null, Modifier.size(15.dp), BlushDeep.copy(alpha = 0.65f))
    }
}

// ── Timeline item ─────────────────────────────────────────────────────────────

@Composable
private fun TimelineItem(
    event: TimelineEvent,
    isFirst: Boolean,
    isLast: Boolean,
    index: Int,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val alpha by animateFloatAsState(
        targetValue  = if (visible) 1f else 0f,
        animationSpec = tween(480, delayMillis = index * 55),
        label = "item_alpha",
    )
    val offsetY by animateFloatAsState(
        targetValue  = if (visible) 0f else 18f,
        animationSpec = tween(480, delayMillis = index * 55, easing = FastOutSlowInEasing),
        label = "item_offset",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .graphicsLayer {
                this.alpha = alpha
                translationY = offsetY * density
            },
        verticalAlignment = Alignment.Top,
    ) {
        IndicatorColumn(event = event, isFirst = isFirst, isLast = isLast)

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    end = Spacing.screenHorizontal,
                    top = 4.dp,
                    bottom = if (isLast) 0.dp else 8.dp,
                ),
        ) {
            EventCard(event = event)
        }
    }
}

// ── Indicator column (public reusable) ────────────────────────────────────────

@Composable
fun IndicatorColumn(
    event: TimelineEvent,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot_pulse")

    val pulse1 by infiniteTransition.animateFloat(
        initialValue  = 1f, targetValue = 2.4f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "p1_scale",
    )
    val pulseA1 by infiniteTransition.animateFloat(
        initialValue  = 0.38f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "p1_alpha",
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue  = 1f, targetValue = 1.75f,
        animationSpec = infiniteRepeatable(tween(1600, delayMillis = 600, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "p2_scale",
    )
    val pulseA2 by infiniteTransition.animateFloat(
        initialValue  = 0.28f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1600, delayMillis = 600, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "p2_alpha",
    )

    val isCurrent   = event.status == EventStatus.CURRENT
    val isCompleted = event.status == EventStatus.COMPLETED

    val dotSize  = if (isCurrent) 26.dp else 20.dp
    val iconSize = if (isCurrent) 13.dp else 10.dp

    val dotBg = when (event.status) {
        EventStatus.COMPLETED -> Gold
        EventStatus.CURRENT   -> BlushDeep
        EventStatus.UPCOMING  -> WarmGray300
    }
    val dotBorder = when (event.status) {
        EventStatus.CURRENT  -> Gold
        EventStatus.COMPLETED -> Gold.copy(alpha = 0.35f)
        EventStatus.UPCOMING -> Color.Transparent
    }
    val iconTint = when (event.status) {
        EventStatus.UPCOMING -> WarmGray500
        else                 -> Color.White
    }
    val topLineColor = when {
        isCompleted -> Gold.copy(alpha = 0.48f)
        isCurrent   -> Brush.verticalGradient(listOf(Gold.copy(alpha = 0.4f), BlushDeep.copy(alpha = 0.45f)))
        else        -> Brush.verticalGradient(listOf(WarmGray200, WarmGray200))
    }

    Box(
        modifier = modifier
            .padding(start = Spacing.screenHorizontal)
            .width(32.dp)
            .fillMaxHeight(),
        contentAlignment = Alignment.TopCenter,
    ) {
        // Top line segment (item top → dot center)
        if (!isFirst) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight(0.5f)
                    .background(
                        when {
                            isCompleted -> Gold.copy(alpha = 0.48f)
                            isCurrent -> BlushDeep.copy(alpha = 0.42f)
                            else -> WarmGray200
                        }
                    )
                    .align(Alignment.TopCenter),
            )
        }

        // Bottom line segment (dot center → item bottom)
        if (!isLast) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight(0.5f)
                    .background(
                        when {
                            isCompleted -> Gold.copy(alpha = 0.35f)
                            else -> WarmGray200
                        }
                    )
                    .align(Alignment.BottomCenter),
            )
        }

        // Outer pulse ring
        if (isCurrent) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer { scaleX = pulse1; scaleY = pulse1; alpha = pulseA1 }
                    .background(BlushDeep, CircleShape)
                    .align(Alignment.Center),
            )
            // Inner pulse ring
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer { scaleX = pulse2; scaleY = pulse2; alpha = pulseA2 }
                    .background(GoldLight, CircleShape)
                    .align(Alignment.Center),
            )
        }

        // Dot circle
        Box(
            modifier = Modifier
                .size(dotSize)
                .background(dotBg, CircleShape)
                .border(1.5.dp, dotBorder, CircleShape)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isCompleted ->
                    Icon(Icons.Default.Check, null, Modifier.size(iconSize), iconTint)

                event.emoji.isNotEmpty() ->
                    Text(
                        text = event.emoji,
                        fontSize = if (isCurrent) 11.sp else 9.sp,
                        lineHeight = 14.sp,
                    )

                else ->
                    Icon(event.icon, null, Modifier.size(iconSize), iconTint)
            }
        }
    }
}

// ── Event card (public reusable component) ────────────────────────────────────

@Composable
fun EventCard(
    event: TimelineEvent,
    modifier: Modifier = Modifier,
) {
    val isCurrent   = event.status == EventStatus.CURRENT
    val isCompleted = event.status == EventStatus.COMPLETED

    val cardColor = when (event.status) {
        EventStatus.COMPLETED -> WarmGray50
        EventStatus.CURRENT   -> Color.White
        EventStatus.UPCOMING  -> Color.White.copy(alpha = 0.92f)
    }
    val elevation = when (event.status) {
        EventStatus.COMPLETED -> 0.dp
        EventStatus.CURRENT   -> 8.dp
        EventStatus.UPCOMING  -> 2.dp
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border    = if (isCurrent) BorderStroke(1.dp, Gold.copy(alpha = 0.28f)) else null,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            // Gold gradient left accent bar for the active event
            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.verticalGradient(listOf(Gold, BlushDeep))
                        ),
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (isCompleted) 0.58f else 1f)
                    .padding(
                        start = if (isCurrent) 14.dp else Spacing.cardMd,
                        end = Spacing.cardMd,
                        top = Spacing.cardMd,
                        bottom = Spacing.cardMd,
                    ),
            ) {
                // Time + status badge
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text  = event.time,
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.5.sp),
                        color = when (event.status) {
                            EventStatus.CURRENT  -> Gold
                            else                 -> WarmGray400
                        },
                    )
                    when (event.status) {
                        EventStatus.CURRENT  -> LiveBadge()
                        EventStatus.COMPLETED -> CompletedBadge()
                        EventStatus.UPCOMING  -> Unit
                    }
                }

                Spacer(Modifier.height(5.dp))

                // Emoji accent (non-legacy events)
                if (event.emoji.isNotEmpty() && !isCompleted) {
                    Text(
                        text = event.emoji,
                        fontSize = if (isCurrent) 22.sp else 18.sp,
                        lineHeight = if (isCurrent) 28.sp else 24.sp,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }

                // Event title
                Text(
                    text  = event.title,
                    style = if (isCurrent) {
                        MaterialTheme.typography.titleMedium.copy(
                            fontSize   = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    } else {
                        MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp)
                    },
                    color = if (isCompleted) WarmGray600 else MaterialTheme.colorScheme.onSurface,
                )

                // Description
                if (event.description != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text     = event.description,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = if (isCompleted)
                            WarmGray400
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f),
                        maxLines = if (isCurrent) 3 else 2,
                    )
                }
            }
        }
    }
}

// ── Badges ────────────────────────────────────────────────────────────────────

@Composable
private fun LiveBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "live")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue  = 1f, targetValue = 0.12f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "live_dot",
    )
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(BlushLight)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .alpha(dotAlpha)
                .clip(CircleShape)
                .background(BlushDeep),
        )
        Text(
            text  = "LIVE",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = BlushDeep,
        )
    }
}

@Composable
private fun CompletedBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(WarmGray100)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Default.Check, null, Modifier.size(9.dp), WarmGray400)
        Text(
            text  = "DONE",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = WarmGray400,
        )
    }
}

// ── Footer ────────────────────────────────────────────────────────────────────

@Composable
private fun TimelineFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xl, vertical = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector      = Icons.Default.Favorite,
            contentDescription = null,
            tint             = Gold.copy(alpha = 0.5f),
            modifier         = Modifier.size(14.dp),
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text  = "Wishing you a lifetime of love and happiness",
            style = TextStyle(
                fontFamily = DancingScript,
                fontSize   = 18.sp,
                color      = BlushDeep.copy(alpha = 0.7f),
            ),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.md))
        Box(
            modifier = Modifier
                .width(52.dp)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Gold.copy(alpha = 0.5f), Color.Transparent)
                    )
                ),
        )
    }
}

// ── Background florals ────────────────────────────────────────────────────────

@Composable
private fun TimelineFlorals() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val rx = size.width
        val by = size.height

        // Top-right blobs
        drawCircle(Color(0xFFFAE8EA).copy(alpha = 0.42f), 100.dp.toPx(), Offset(rx + 8.dp.toPx(),   55.dp.toPx()))
        drawCircle(Color(0xFFEDD9B8).copy(alpha = 0.32f), 62.dp.toPx(),  Offset(rx - 55.dp.toPx(), 140.dp.toPx()))
        drawCircle(Color(0xFFF5E6C8).copy(alpha = 0.38f), 44.dp.toPx(),  Offset(rx - 20.dp.toPx(), 190.dp.toPx()))

        // Bottom-left blobs
        drawCircle(Color(0xFFEDD9B8).copy(alpha = 0.38f), 88.dp.toPx(),  Offset(-10.dp.toPx(), by - 55.dp.toPx()))
        drawCircle(Color(0xFFFAE8EA).copy(alpha = 0.28f), 58.dp.toPx(),  Offset(55.dp.toPx(),  by - 115.dp.toPx()))
        drawCircle(Color(0xFFEAB8BC).copy(alpha = 0.16f), 36.dp.toPx(),  Offset(20.dp.toPx(),  by - 155.dp.toPx()))
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Timeline – Active"
)
@androidx.compose.runtime.Composable
private fun TimelinePreview() {
    com.wednowapp.wednow.ui.theme.WedNowTheme {
        val wedding = com.wednowapp.wednow.domain.model.Wedding(
            id = "w1", name = "Sophie & James",
            date = 1781481600000L, location = "Grand Ballroom, New York",
        )
        val events = listOf(
            TimelineEvent(
                0,
                "10:00 AM",
                "Guest Arrival",
                icon = Icons.Default.Groups,
                status = EventStatus.COMPLETED
            ),
            TimelineEvent(
                1,
                "11:00 AM",
                "Ceremony Begins",
                icon = Icons.Default.Favorite,
                status = EventStatus.CURRENT
            ),
            TimelineEvent(
                2,
                "12:30 PM",
                "Cocktail Hour",
                icon = Icons.Default.WineBar,
                status = EventStatus.UPCOMING
            ),
            TimelineEvent(
                3,
                "2:00 PM",
                "Wedding Dinner",
                icon = Icons.Default.Restaurant,
                status = EventStatus.UPCOMING
            ),
            TimelineEvent(
                4,
                "4:00 PM",
                "First Dance",
                icon = Icons.Default.MusicNote,
                status = EventStatus.UPCOMING
            ),
            TimelineEvent(
                5,
                "6:00 PM",
                "Cake Cutting",
                icon = Icons.Default.Cake,
                status = EventStatus.UPCOMING
            ),
            TimelineEvent(
                6,
                "8:00 PM",
                "Evening Celebration",
                icon = Icons.Default.Celebration,
                status = EventStatus.UPCOMING
            ),
        )
        TimelineContent(wedding = wedding, events = events, onBack = {})
    }
}
