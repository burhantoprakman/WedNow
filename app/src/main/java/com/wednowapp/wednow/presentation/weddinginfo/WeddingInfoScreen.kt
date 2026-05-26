package com.wednowapp.wednow.presentation.weddinginfo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wednowapp.wednow.domain.model.Wedding
import com.wednowapp.wednow.ui.components.WedNowErrorScreen
import com.wednowapp.wednow.ui.components.WedNowLoadingScreen
import com.wednowapp.wednow.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ── Sample data (wire from model when Wedding fields are extended) ─────────────

private data class ScheduleItem(val time: String, val title: String, val status: String)
// status: "completed" | "current" | "upcoming"

private val sampleSchedule = listOf(
    ScheduleItem("14:00", "Guest Arrival",   "completed"),
    ScheduleItem("14:30", "Welcome Drinks",  "completed"),
    ScheduleItem("15:00", "Ceremony Begins", "current"),
    ScheduleItem("16:00", "Cocktail Hour",   "upcoming"),
    ScheduleItem("18:00", "Dinner Service",  "upcoming"),
    ScheduleItem("19:30", "First Dance",     "upcoming"),
    ScheduleItem("20:30", "Cake Cutting",    "upcoming"),
    ScheduleItem("21:30", "After Party",     "upcoming"),
    ScheduleItem("23:00", "Farewell",        "upcoming"),
)

private data class MenuCategory(val name: String, val emoji: String, val items: List<String>)

private val sampleMenu = listOf(
    MenuCategory("Starter",     "🥗", listOf("Bruschetta", "Caesar Salad", "Tomato Bisque", "Shrimp Cocktail")),
    MenuCategory("Main Course", "🍽", listOf("Chicken Marsala", "Pan-Seared Salmon", "Vegan Risotto", "Beef Tenderloin")),
    MenuCategory("Dessert",     "🍰", listOf("Wedding Cake", "French Macarons", "Crème Brûlée")),
    MenuCategory("Drinks",      "🥂", listOf("Champagne Toast", "House Wine", "Signature Cocktail", "Mocktails")),
)

private data class ColorSwatch(val color: Color, val label: String)

private val dressCodeColors = listOf(
    ColorSwatch(Color(0xFFFDFAF5), "Ivory"),
    ColorSwatch(Color(0xFFF5E6C8), "Champagne"),
    ColorSwatch(Color(0xFFD4848A), "Dusty Rose"),
    ColorSwatch(Color(0xFF8C7B6E), "Taupe"),
)
private const val DRESS_CODE_STYLE  = "Semi-Formal"
private val dressCodeDo   = listOf("Cocktail dress", "Tailored suit", "Elegant midi dress")
private val dressCodeDont = listOf("Jeans", "White dress", "Sportswear")

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun WeddingInfoScreen(
    onBack: () -> Unit,
    viewModel: WeddingInfoViewModel = hiltViewModel(),
) {
    val state        by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    val scope        = rememberCoroutineScope()
    val context      = LocalContext.current

    when (val s = state) {
        is WeddingInfoState.Loading -> WedNowLoadingScreen()
        is WeddingInfoState.Error   -> WedNowErrorScreen(message = s.message, onRetry = viewModel::retry)
        is WeddingInfoState.Success -> WeddingInfoContent(
            wedding      = s.wedding,
            onBack       = onBack,
            onCopyCode   = {
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("Wedding Code", s.wedding.id))
                scope.launch { snackbarHost.showSnackbar("Code copied ✓") }
            },
            snackbarHost = snackbarHost,
        )
    }
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
private fun WeddingInfoContent(
    wedding: Wedding,
    onBack: () -> Unit,
    onCopyCode: () -> Unit,
    snackbarHost: SnackbarHostState,
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(ChampagneLight.copy(alpha = 0.5f), Ivory, Ivory, Ivory)))
        )
        WeddingInfoFlorals()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 56.dp),
        ) {
            item { InfoHeader(wedding = wedding, onBack = onBack) }

            item {
                // ── Main event info card ──────────────────────────────────────
                Card(
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screenHorizontal),
                    shape     = RoundedCornerShape(24.dp),
                    colors    = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        DateSection(wedding = wedding, context = context)
                        SectionDivider()
                        VenueSection(wedding = wedding, context = context)
                        SectionDivider()
                        MenuSection()
                        SectionDivider()
                        DressCodeSection()
                        SectionDivider()
                        ScheduleSection()
                    }
                }
            }

            item { Spacer(Modifier.height(Spacing.md)) }

            item {
                WeddingCodeCard(
                    wedding    = wedding,
                    onCopyCode = onCopyCode,
                    modifier   = Modifier.padding(horizontal = Spacing.screenHorizontal),
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHost,
            modifier  = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = Spacing.sm),
        )
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun InfoHeader(wedding: Wedding, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Back button row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp),
                    tint     = WarmGray600,
                )
            }
        }

        // Title area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(bottom = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint     = Gold.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text      = wedding.name,
                style     = MaterialTheme.typography.headlineLarge,
                color     = WarmGray800,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "Event Details",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = WarmGray400,
            )
        }
    }
}

// ── Date section ──────────────────────────────────────────────────────────────

@Composable
private fun DateSection(wedding: Wedding, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openCalendar(context, wedding) }
            .padding(horizontal = Spacing.cardLg, vertical = Spacing.cardMd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionIconBox(
            icon       = Icons.Default.CalendarMonth,
            accentColor = BlushDeep,
            bgColor    = BlushLight,
        )
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = "WEDDING DATE",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, fontSize = 9.sp),
                color = WarmGray400,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = wedding.date.ifBlank { "Date TBA" },
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = WarmGray800,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = "Ceremony · 14:00 – 22:00  ·  Add to calendar",
                style = MaterialTheme.typography.bodySmall,
                color = Gold.copy(alpha = 0.75f),
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint     = WarmGray200,
            modifier = Modifier.size(16.dp),
        )
    }
}

// ── Venue section ─────────────────────────────────────────────────────────────

@Composable
private fun VenueSection(wedding: Wedding, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openMaps(context, wedding.location) }
            .padding(horizontal = Spacing.cardLg, vertical = Spacing.cardMd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionIconBox(
            icon       = Icons.Default.LocationOn,
            accentColor = Gold,
            bgColor    = ChampagneLight,
        )
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = "VENUE",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, fontSize = 9.sp),
                color = WarmGray400,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text     = wedding.location.ifBlank { "Venue TBA" },
                style    = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color    = WarmGray800,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = "Tap for directions",
                style = MaterialTheme.typography.bodySmall,
                color = Gold.copy(alpha = 0.75f),
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint     = WarmGray200,
            modifier = Modifier.size(16.dp),
        )
    }
}

// ── Menu section ──────────────────────────────────────────────────────────────

@Composable
private fun MenuSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.cardLg, vertical = Spacing.cardMd)) {
        // Section header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            SectionIconBox(
                icon       = Icons.Default.Restaurant,
                accentColor = Color(0xFF7A9E7E),
                bgColor    = Color(0xFFD8EED8),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "WEDDING MENU",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, fontSize = 9.sp),
                    color = WarmGray400,
                )
                Text(
                    text  = "4 courses",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = WarmGray600,
                )
            }
        }

        Spacer(Modifier.height(Spacing.md))

        // Categories
        sampleMenu.forEachIndexed { i, category ->
            MenuCategoryRow(category = category)
            if (i < sampleMenu.lastIndex) Spacer(Modifier.height(Spacing.xs))
        }
    }
}

@Composable
private fun MenuCategoryRow(category: MenuCategory) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (expanded) WarmGray50 else Color.Transparent)
            .border(
                width = 0.5.dp,
                color = if (expanded) WarmGray200 else WarmGray100,
                shape = RoundedCornerShape(14.dp),
            ),
    ) {
        // Category header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Emoji pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(0.5.dp, WarmGray100, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(text = category.emoji, fontSize = 14.sp)
            }
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text  = category.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                color = WarmGray700,
                modifier = Modifier.weight(1f),
            )
            // Item count
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(WarmGray100)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text  = "${category.items.size}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = WarmGray500,
                )
            }
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint     = WarmGray300,
                modifier = Modifier.size(18.dp),
            )
        }

        // Items as chips
        AnimatedVisibility(
            visible = expanded,
            enter   = expandVertically(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit    = shrinkVertically() + fadeOut(),
        ) {
            FlowRow(
                modifier            = Modifier.fillMaxWidth().padding(start = Spacing.md, end = Spacing.md, bottom = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement   = Arrangement.spacedBy(6.dp),
            ) {
                category.items.forEach { item ->
                    MenuItemChip(text = item)
                }
            }
        }
    }
}

@Composable
private fun MenuItemChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(0.5.dp, WarmGray200, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text  = text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = WarmGray700,
        )
    }
}

// ── Dress code section ────────────────────────────────────────────────────────

@Composable
private fun DressCodeSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.cardLg, vertical = Spacing.cardMd)) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            SectionIconBox(
                icon       = Icons.Default.Style,
                accentColor = BlushDeep,
                bgColor    = BlushLight,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "DRESS CODE",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, fontSize = 9.sp),
                    color = WarmGray400,
                )
                Text(
                    text  = DRESS_CODE_STYLE,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = WarmGray800,
                )
            }
        }

        Spacer(Modifier.height(Spacing.md))

        // Color palette
        Text(
            text  = "Color Palette",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
            color = WarmGray400,
        )
        Spacer(Modifier.height(Spacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            dressCodeColors.forEach { swatch ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(swatch.color)
                            .border(1.dp, WarmGray200, CircleShape),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text      = swatch.label,
                        style     = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color     = WarmGray400,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.md))

        // Do / Don't hints
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            // Suggested column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "✓  Suggested",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF6A8C6A),
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement   = Arrangement.spacedBy(4.dp),
                ) {
                    dressCodeDo.forEach { hint ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFD8EED8))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text  = hint,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = Color(0xFF3A5A3A),
                            )
                        }
                    }
                }
            }
            // Please avoid column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "✗  Please avoid",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                    color = WarmGray400,
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement   = Arrangement.spacedBy(4.dp),
                ) {
                    dressCodeDont.forEach { hint ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(WarmGray100)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text  = hint,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = WarmGray500,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Schedule section ──────────────────────────────────────────────────────────

@Composable
private fun ScheduleSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.cardLg, vertical = Spacing.cardMd),
    ) {
        // Header
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            SectionIconBox(
                icon        = Icons.Default.Schedule,
                accentColor = Gold,
                bgColor     = ChampagneLight,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "EVENT SCHEDULE",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, fontSize = 9.sp),
                    color = WarmGray400,
                )
                Text(
                    text  = "${sampleSchedule.size} events today",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = WarmGray600,
                )
            }
        }

        Spacer(Modifier.height(Spacing.md))

        // Compact timeline rows
        sampleSchedule.forEachIndexed { index, item ->
            CompactScheduleRow(
                item    = item,
                isFirst = index == 0,
                isLast  = index == sampleSchedule.lastIndex,
            )
        }
    }
}

@Composable
private fun CompactScheduleRow(
    item: ScheduleItem,
    isFirst: Boolean,
    isLast: Boolean,
) {
    val isCurrent   = item.status == "current"
    val isCompleted = item.status == "completed"

    val dotSize  = if (isCurrent) 10.dp else 8.dp
    val dotColor = when {
        isCompleted -> Gold.copy(alpha = 0.7f)
        isCurrent   -> BlushDeep
        else        -> WarmGray200
    }
    val lineColor = when {
        isCompleted -> Gold.copy(alpha = 0.35f)
        else        -> WarmGray200
    }

    Row(
        modifier          = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top,
    ) {
        // Left: dot + connecting line
        Box(
            modifier         = Modifier.width(20.dp).fillMaxHeight(),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .fillMaxHeight(0.5f)
                        .background(lineColor)
                        .align(Alignment.TopCenter),
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .fillMaxHeight(0.5f)
                        .background(lineColor)
                        .align(Alignment.BottomCenter),
                )
            }
            Box(
                modifier         = Modifier
                    .size(dotSize)
                    .background(dotColor, CircleShape)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center,
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint     = Color.White,
                        modifier = Modifier.size(5.dp),
                    )
                }
            }
        }

        Spacer(Modifier.width(10.dp))

        // Right: time + title + badge
        Row(
            modifier              = Modifier
                .weight(1f)
                .padding(
                    top    = 1.dp,
                    bottom = if (isLast) 0.dp else 9.dp,
                ),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text     = item.time,
                style    = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                color    = if (isCurrent) Gold else WarmGray400,
                modifier = Modifier.width(40.dp),
            )
            Text(
                text     = item.title,
                style    = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize   = 12.sp,
                ),
                color    = when {
                    isCompleted -> WarmGray400
                    isCurrent   -> WarmGray800
                    else        -> WarmGray600
                },
                modifier = Modifier.weight(1f),
            )
            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(BlushLight)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text  = "NOW",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 0.5.sp),
                        color = BlushDeep,
                    )
                }
            }
        }
    }
}

// ── Wedding code card ─────────────────────────────────────────────────────────

@Composable
private fun WeddingCodeCard(
    wedding: Wedding,
    onCopyCode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ChampagneLight.copy(alpha = 0.8f))
            .border(1.dp, Gold.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
            .padding(horizontal = Spacing.cardLg, vertical = Spacing.cardMd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Gold.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = null,
                tint     = GoldDeep,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = "WEDDING CODE",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, fontSize = 9.sp),
                color = WarmGray400,
            )
            Text(
                text     = wedding.id,
                style    = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color    = WarmGray700,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onCopyCode) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy code",
                tint     = GoldDeep.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// ── Shared sub-components ─────────────────────────────────────────────────────

@Composable
private fun SectionIconBox(icon: ImageVector, accentColor: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(horizontal = Spacing.cardLg),
        color     = WarmGray100,
        thickness = 0.5.dp,
    )
}

// ── Background florals ────────────────────────────────────────────────────────

@Composable
private fun WeddingInfoFlorals() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(Color(0xFFEDD9B8).copy(alpha = 0.35f), 90.dp.toPx(),  Offset(-10.dp.toPx(), 20.dp.toPx()))
        drawCircle(Color(0xFFEAB8BC).copy(alpha = 0.14f), 65.dp.toPx(),  Offset(70.dp.toPx(),  90.dp.toPx()))
        drawCircle(Color(0xFFF5E6C8).copy(alpha = 0.38f), 70.dp.toPx(),  Offset(-20.dp.toPx(), 70.dp.toPx()))
        val bx = size.width; val by = size.height
        drawCircle(Color(0xFFEDD9B8).copy(alpha = 0.28f), 80.dp.toPx(),  Offset(bx,              by - 40.dp.toPx()))
        drawCircle(Color(0xFFF5E6C8).copy(alpha = 0.32f), 55.dp.toPx(),  Offset(bx - 60.dp.toPx(), by - 80.dp.toPx()))
    }
}

// ── Intent helpers ────────────────────────────────────────────────────────────

private fun openCalendar(context: Context, wedding: Wedding) {
    val dateMs = parseWeddingDate(wedding.date) ?: System.currentTimeMillis()
    val cal = Calendar.getInstance().apply { timeInMillis = dateMs }
    cal.set(Calendar.HOUR_OF_DAY, 14); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
    val startMs = cal.timeInMillis
    cal.set(Calendar.HOUR_OF_DAY, 22)
    val endMs = cal.timeInMillis

    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, "Wedding Day — ${wedding.name}")
        putExtra(CalendarContract.Events.EVENT_LOCATION, wedding.location)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMs)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMs)
        putExtra(CalendarContract.Events.DESCRIPTION, "Celebrating the wedding of ${wedding.name}")
    }
    try { context.startActivity(intent) } catch (_: Exception) { }
}

private fun openMaps(context: Context, location: String) {
    if (location.isBlank()) return
    val encoded = Uri.encode(location)
    // Try Google Maps navigation first
    try {
        val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$encoded"))
            .setPackage("com.google.android.apps.maps")
        context.startActivity(mapsIntent)
        return
    } catch (_: Exception) { }
    // Fallback: generic geo intent (any maps app)
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encoded")))
    } catch (_: Exception) { }
}

private fun parseWeddingDate(dateStr: String): Long? {
    val formats = listOf(
        "MMMM d, yyyy", "MMM d, yyyy", "MMMM dd, yyyy",
        "MM/dd/yyyy", "dd/MM/yyyy", "yyyy-MM-dd",
        "d MMMM yyyy",  "dd MMMM yyyy",
    )
    for (pattern in formats) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
            sdf.isLenient = false
            return sdf.parse(dateStr)?.time
        } catch (_: Exception) { }
    }
    return null
}
