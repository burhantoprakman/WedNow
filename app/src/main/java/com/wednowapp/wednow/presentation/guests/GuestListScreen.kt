package com.wednowapp.wednow.presentation.guests

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wednowapp.wednow.domain.model.Guest
import com.wednowapp.wednow.domain.model.GuestRole
import com.wednowapp.wednow.domain.model.RSVPStatus
import com.wednowapp.wednow.ui.components.AvatarCircle
import com.wednowapp.wednow.ui.theme.*

// ── Internal layout model ──────────────────────────────────────────────────────

private data class GuestGroup(
    val label: String,
    val accentColor: Color,
    val guests: List<Guest>,
)

private fun groupGuests(guests: List<Guest>): List<GuestGroup> {
    val going    = guests.filter { it.rsvpStatus == RSVPStatus.GOING }
    val maybe    = guests.filter { it.rsvpStatus == RSVPStatus.MAYBE }
    val notGoing = guests.filter { it.rsvpStatus == RSVPStatus.NOT_GOING }
    val pending  = guests.filter { it.rsvpStatus.isNullOrBlank() }
    return buildList {
        if (going.isNotEmpty())    add(GuestGroup("Attending",     BlushDeep,   going))
        if (maybe.isNotEmpty())    add(GuestGroup("Maybe",         Gold,         maybe))
        if (pending.isNotEmpty())  add(GuestGroup("Yet to Reply",  WarmGray400,  pending))
        if (notGoing.isNotEmpty()) add(GuestGroup("Not Attending", WarmGray400,  notGoing))
    }
}

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun GuestListScreen(
    onBack: () -> Unit,
    viewModel: GuestListViewModel = hiltViewModel(),
) {
    val guests       by viewModel.guests.collectAsStateWithLifecycle()
    val currentGuest by viewModel.currentGuest.collectAsStateWithLifecycle()

    GuestListContent(
        guests         = guests,
        currentGuestId = currentGuest?.id,
        onBack         = onBack,
    )
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
private fun GuestListContent(
    guests: List<Guest>,
    currentGuestId: String?,
    onBack: () -> Unit,
) {
    val groups   = remember(guests) { groupGuests(guests) }
    val going    = remember(guests) { guests.count { it.rsvpStatus == RSVPStatus.GOING } }
    val maybe    = remember(guests) { guests.count { it.rsvpStatus == RSVPStatus.MAYBE } }
    val notGoing = remember(guests) { guests.count { it.rsvpStatus == RSVPStatus.NOT_GOING } }
    val pending  = remember(guests) { guests.count { it.rsvpStatus.isNullOrBlank() } }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(ChampagneLight.copy(alpha = 0.45f), Ivory, Ivory, Ivory)
                    )
                )
        )
        GuestListFlorals()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            GuestTopBar(onBack = onBack)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 40.dp),
            ) {
                item {
                    GuestListHeader(guestCount = guests.size)
                }

                if (guests.isNotEmpty()) {
                    item {
                        RsvpSummaryRow(
                            going    = going,
                            maybe    = maybe,
                            notGoing = notGoing,
                            pending  = pending,
                        )
                    }
                }

                if (guests.isEmpty()) {
                    item { EmptyGuestState(Modifier.fillMaxWidth()) }
                } else {
                    groups.forEach { group ->
                        item(key = "hdr_${group.label}") {
                            GuestSectionHeader(
                                label       = group.label,
                                count       = group.guests.size,
                                accentColor = group.accentColor,
                            )
                        }
                        items(group.guests, key = { it.id }) { guest ->
                            GuestCard(
                                guest         = guest,
                                isCurrentUser = guest.id == currentGuestId,
                                modifier      = Modifier.padding(
                                    horizontal = Spacing.screenHorizontal,
                                    vertical   = 4.dp,
                                ),
                            )
                        }
                        item(key = "sp_${group.label}") {
                            Spacer(Modifier.height(Spacing.sm))
                        }
                    }
                }
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun GuestTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
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
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun GuestListHeader(guestCount: Int) {
    val serifFamily = MaterialTheme.typography.displayLarge.fontFamily
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal)
            .padding(bottom = Spacing.md),
    ) {
        Text(
            text  = "Wedding Guests",
            style = MaterialTheme.typography.headlineLarge,
            color = WarmGray800,
        )
        Spacer(Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text  = "Celebrating together",
                style = TextStyle(
                    fontFamily = serifFamily,
                    fontStyle  = FontStyle.Italic,
                    fontSize   = 15.sp,
                    color      = WarmGray500,
                ),
            )
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(WarmGray300),
            )
            Text(
                text  = "$guestCount invited",
                style = MaterialTheme.typography.bodySmall,
                color = WarmGray400,
            )
        }
    }
}

// ── RSVP summary row ──────────────────────────────────────────────────────────

@Composable
private fun RsvpSummaryRow(going: Int, maybe: Int, notGoing: Int, pending: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal)
            .padding(bottom = Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        StatPill(
            icon        = Icons.Default.CheckCircle,
            count       = going,
            label       = "Attending",
            accentColor = BlushDeep,
            bgColor     = BlushLight,
            modifier    = Modifier.weight(1f),
        )
        StatPill(
            icon        = Icons.Default.Help,
            count       = maybe,
            label       = "Maybe",
            accentColor = GoldDeep,
            bgColor     = ChampagneLight,
            modifier    = Modifier.weight(1f),
        )
        StatPill(
            icon        = Icons.Default.Schedule,
            count       = pending,
            label       = "Pending",
            accentColor = WarmGray400,
            bgColor     = WarmGray50,
            modifier    = Modifier.weight(1f),
        )
        StatPill(
            icon        = Icons.Default.Cancel,
            count       = notGoing,
            label       = "Declined",
            accentColor = WarmGray500,
            bgColor     = WarmGray100,
            modifier    = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatPill(
    icon: ImageVector,
    count: Int,
    label: String,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint     = accentColor.copy(alpha = 0.55f),
            modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = count.toString(),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 19.sp),
            color = accentColor,
        )
        Text(
            text      = label,
            style     = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.2.sp),
            color     = accentColor.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
            maxLines  = 1,
        )
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
private fun GuestSectionHeader(
    label: String,
    count: Int,
    accentColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.5f)),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text  = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
            color = accentColor.copy(alpha = 0.65f),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text  = "($count)",
            style = MaterialTheme.typography.labelSmall,
            color = WarmGray300,
        )
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(0.5.dp)
                .background(
                    Brush.horizontalGradient(listOf(WarmGray200, Color.Transparent))
                ),
        )
    }
}

// ── Guest card ────────────────────────────────────────────────────────────────

@Composable
fun GuestCard(
    guest: Guest,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier,
    // Future fields — wire these when the model supports them:
    plusOneCount: Int = 0,
    plusOneNames: List<String> = emptyList(),
    tableAssignment: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) ChampagneLight.copy(alpha = 0.5f) else Color.White,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentUser) 4.dp else 2.dp),
        border    = if (isCurrentUser)
            androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.35f))
        else null,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (plusOneCount > 0) Modifier.clickable { expanded = !expanded }
                        else Modifier
                    )
                    .padding(Spacing.cardMd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Avatar
                AvatarCircle(
                    name            = guest.name.ifBlank { "?" },
                    size            = 48.dp,
                    backgroundColor = if (isCurrentUser) ChampagneLight else BlushLight,
                    textColor       = if (isCurrentUser) GoldDeep else BlushDeep,
                )

                Spacer(Modifier.width(Spacing.md))

                // Name + badge
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text     = guest.name.ifBlank { "Anonymous" },
                            style    = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 16.sp,
                            ),
                            color    = WarmGray800,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (isCurrentUser) YouChip()
                        if (guest.role == GuestRole.ADMIN || guest.role == GuestRole.COADMIN) {
                            RoleIndicator(guest.role)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    RSVPBadge(rsvpStatus = guest.rsvpStatus)
                }

                Spacer(Modifier.width(Spacing.sm))

                // Right column: table placeholder + expand chevron
                Column(horizontalAlignment = Alignment.End) {
                    FutureTablePlaceholder(tableAssignment = tableAssignment)
                    if (plusOneCount > 0) {
                        Spacer(Modifier.height(4.dp))
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Show guests",
                            tint     = WarmGray300,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            // Plus-one section (expands/collapses)
            AnimatedVisibility(
                visible = expanded && plusOneCount > 0,
                enter   = expandVertically(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit    = shrinkVertically() + fadeOut(),
            ) {
                PlusOneSection(
                    count    = plusOneCount,
                    names    = plusOneNames,
                    modifier = Modifier
                        .padding(start = 72.dp, end = Spacing.cardMd, bottom = Spacing.md),
                )
            }
        }
    }
}

// ── RSVP badge ────────────────────────────────────────────────────────────────

@Composable
fun RSVPBadge(rsvpStatus: String?, modifier: Modifier = Modifier) {
    val normalised = rsvpStatus?.takeIf { it.isNotBlank() }

    val label = when (normalised) {
        RSVPStatus.GOING     -> "Attending"
        RSVPStatus.MAYBE     -> "Maybe"
        RSVPStatus.NOT_GOING -> "Not Attending"
        else                 -> "Awaiting Reply"
    }
    val icon = when (normalised) {
        RSVPStatus.GOING     -> Icons.Default.CheckCircle
        RSVPStatus.MAYBE     -> Icons.Default.Help
        RSVPStatus.NOT_GOING -> Icons.Default.Cancel
        else                 -> Icons.Default.Schedule
    }
    val textColor = when (normalised) {
        RSVPStatus.GOING     -> BlushDeep
        RSVPStatus.MAYBE     -> GoldDeep
        RSVPStatus.NOT_GOING -> WarmGray500
        else                 -> WarmGray400
    }
    val bgColor = when (normalised) {
        RSVPStatus.GOING     -> BlushLight
        RSVPStatus.MAYBE     -> ChampagneLight
        RSVPStatus.NOT_GOING -> WarmGray100
        else                 -> WarmGray50
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint     = textColor.copy(alpha = 0.65f),
            modifier = Modifier.size(10.dp),
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = textColor,
        )
    }
}

// ── Plus-one section ──────────────────────────────────────────────────────────

@Composable
fun PlusOneSection(
    count: Int,
    names: List<String>,
    modifier: Modifier = Modifier,
) {
    if (count == 0) return
    Column(modifier = modifier) {
        repeat(count) { i ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                // Tree connector line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(WarmGray200),
                )

                // Small avatar
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(WarmGray50),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint     = WarmGray300,
                        modifier = Modifier.size(15.dp),
                    )
                }

                Text(
                    text  = names.getOrElse(i) { "Guest ${i + 1}" },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = WarmGray500,
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text  = "+ $count joining",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.5.sp),
            color = WarmGray300,
            modifier = Modifier.padding(start = 13.dp),
        )
    }
}

// ── Future table placeholder ──────────────────────────────────────────────────

@Composable
fun FutureTablePlaceholder(
    tableAssignment: String?,
    modifier: Modifier = Modifier,
) {
    if (tableAssignment != null) {
        Column(modifier = modifier, horizontalAlignment = Alignment.End) {
            Text(
                text  = tableAssignment,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Gold.copy(alpha = 0.8f),
            )
            Text(
                text  = "Table",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.5.sp),
                color = WarmGray300,
            )
        }
    } else {
        // Subtle placeholder: three dots indicating "seat to be assigned"
        Text(
            text     = "· · ·",
            style    = MaterialTheme.typography.labelSmall.copy(letterSpacing = 3.sp, fontSize = 10.sp),
            color    = WarmGray200,
            modifier = modifier,
        )
    }
}

// ── You chip ──────────────────────────────────────────────────────────────────

@Composable
private fun YouChip() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Gold.copy(alpha = 0.15f))
            .padding(horizontal = 7.dp, vertical = 2.dp),
    ) {
        Text(
            text  = "You",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
            color = GoldDeep,
        )
    }
}

// ── Role indicator ────────────────────────────────────────────────────────────

@Composable
private fun RoleIndicator(role: String) {
    val (label, color) = when (role) {
        GuestRole.ADMIN   -> "Host" to Gold
        GuestRole.COADMIN -> "Co-host" to Gold.copy(alpha = 0.7f)
        else              -> return
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ChampagneLight)
            .padding(horizontal = 7.dp, vertical = 2.dp),
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium, fontSize = 10.sp),
            color = color,
        )
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyGuestState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(Spacing.xxl))
        Icon(
            imageVector = Icons.Default.PeopleAlt,
            contentDescription = null,
            tint     = BlushDeep.copy(alpha = 0.22f),
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(Spacing.lg))
        Text(
            text      = "No guests yet",
            style     = MaterialTheme.typography.headlineSmall,
            color     = WarmGray600.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text      = "Guests will appear here\nonce they join the wedding",
            style     = MaterialTheme.typography.bodyMedium,
            color     = WarmGray400.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
        )
    }
}

// ── Background florals ────────────────────────────────────────────────────────

@Composable
private fun GuestListFlorals() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(Color(0xFFEDD9B8).copy(alpha = 0.35f), 90.dp.toPx(),  Offset(-10.dp.toPx(), 30.dp.toPx()))
        drawCircle(Color(0xFFEAB8BC).copy(alpha = 0.12f), 65.dp.toPx(),  Offset(75.dp.toPx(),  100.dp.toPx()))
        drawCircle(Color(0xFFF5E6C8).copy(alpha = 0.40f), 70.dp.toPx(),  Offset(-25.dp.toPx(), 80.dp.toPx()))
        val bx = size.width
        val by = size.height
        drawCircle(Color(0xFFEDD9B8).copy(alpha = 0.28f), 80.dp.toPx(),  Offset(bx,              by - 40.dp.toPx()))
        drawCircle(Color(0xFFF5E6C8).copy(alpha = 0.35f), 55.dp.toPx(),  Offset(bx - 65.dp.toPx(), by - 85.dp.toPx()))
    }
}
