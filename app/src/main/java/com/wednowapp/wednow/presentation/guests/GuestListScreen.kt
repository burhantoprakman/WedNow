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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.wednowapp.wednow.ui.theme.BlushDeep
import com.wednowapp.wednow.ui.theme.BlushLight
import com.wednowapp.wednow.ui.theme.ChampagneLight
import com.wednowapp.wednow.ui.theme.Gold
import com.wednowapp.wednow.ui.theme.GoldDeep
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
import com.wednowapp.wednow.ui.theme.WarmGray800

// ── Internal layout model ──────────────────────────────────────────────────────

private data class GuestGroup(
    val label: String,
    val accentColor: Color,
    val guests: List<Guest>,
)

private fun groupGuests(guests: List<Guest>): List<GuestGroup> {
    val going = guests.filter { it.rsvpStatus == RSVPStatus.GOING }.sortedBy { it.name.lowercase() }
    val maybe = guests.filter { it.rsvpStatus == RSVPStatus.MAYBE }.sortedBy { it.name.lowercase() }
    val notGoing =
        guests.filter { it.rsvpStatus == RSVPStatus.NOT_GOING }.sortedBy { it.name.lowercase() }
    val pending = guests.filter { it.rsvpStatus.isNullOrBlank() }.sortedBy { it.name.lowercase() }
    return buildList {
        if (going.isNotEmpty())    add(GuestGroup("Attending",     BlushDeep,   going))
        if (maybe.isNotEmpty()) add(GuestGroup("Maybe", Gold, maybe))
        if (pending.isNotEmpty()) add(GuestGroup("Yet to Reply", WarmGray400, pending))
        if (notGoing.isNotEmpty()) add(GuestGroup("Not Attending", WarmGray400, notGoing))
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
    guests: List<Guest>?,        // null = first Firestore snapshot not yet received
    currentGuestId: String?,
    onBack: () -> Unit,
) {
    val guestList = guests ?: emptyList()
    val groups = remember(guestList) { groupGuests(guestList) }
    val going = remember(guestList) { guestList.count { it.rsvpStatus == RSVPStatus.GOING } }
    val maybe = remember(guestList) { guestList.count { it.rsvpStatus == RSVPStatus.MAYBE } }
    val notGoing = remember(guestList) { guestList.count { it.rsvpStatus == RSVPStatus.NOT_GOING } }
    val pending = remember(guestList) { guestList.count { it.rsvpStatus.isNullOrBlank() } }

    Box(modifier = Modifier.fillMaxSize()) {

        // Background — very subtle champagne fade
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(ChampagneLight.copy(alpha = 0.28f), Ivory, Ivory, Ivory)
                    )
                )
        )

        // Background florals — barely perceptible, atmosphere only
        GuestListFlorals()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            GuestTopBar(onBack = onBack)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item {
                    GuestListHeader(guestCount = guests?.size ?: 0)
                }

                when {
                    // ── Loading ────────────────────────────────────────────────
                    guests == null -> item { LoadingGuestsState() }

                    // ── Empty ──────────────────────────────────────────────────
                    guests.isEmpty() -> item { EmptyGuestState(Modifier.fillMaxWidth()) }

                    // ── Populated ─────────────────────────────────────────────
                    else -> {
                        item {
                            RSVPStatusSummary(
                                going = going,
                                maybe = maybe,
                                notGoing = notGoing,
                                pending = pending,
                            )
                        }

                        groups.forEach { group ->
                            item(key = "hdr_${group.label}") {
                                GuestSectionHeader(
                                    label = group.label,
                                    count = group.guests.size,
                                    accentColor = group.accentColor,
                                )
                            }
                            items(group.guests, key = { it.id }) { guest ->
                                GuestCard(
                                    guest = guest,
                                    isCurrentUser = guest.id == currentGuestId,
                                    modifier = Modifier.padding(
                                        horizontal = Spacing.screenHorizontal,
                                        vertical = 3.dp,
                                    ),
                                )
                            }
                            item(key = "sp_${group.label}") {
                                Spacer(Modifier.height(Spacing.xs))
                            }
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
                .background(WarmGray50)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(20.dp),
                tint = WarmGray600,
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
            .padding(top = Spacing.sm, bottom = Spacing.sm),
    ) {
        Text(
            text  = "Wedding Guests",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Light),
            color = WarmGray800,
        )
        Spacer(Modifier.height(3.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                text  = "Celebrating together",
                style = TextStyle(
                    fontFamily = serifFamily,
                    fontStyle  = FontStyle.Italic,
                    fontSize = 14.sp,
                    color = WarmGray400,
                ),
            )
            Box(
                modifier = Modifier
                    .size(2.5.dp)
                    .clip(CircleShape)
                    .background(WarmGray200),
            )
            Text(
                text = if (guestCount > 0) "$guestCount invited" else "",
                style = MaterialTheme.typography.bodySmall,
                color = WarmGray400,
            )
        }
        Spacer(Modifier.height(Spacing.md))
    }
}

// ── Loading state ─────────────────────────────────────────────────────────────

@Composable
private fun LoadingGuestsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = Gold,
                strokeWidth = 2.dp,
            )
            Text(
                text = "Loading guests…",
                style = MaterialTheme.typography.bodySmall,
                color = WarmGray400,
            )
        }
    }
}

// ── RSVP status summary ───────────────────────────────────────────────────────

@Composable
private fun RSVPStatusSummary(going: Int, maybe: Int, notGoing: Int, pending: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal)
            .padding(bottom = Spacing.md),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RSVPStatItem(count = going, label = "Attending", color = BlushDeep)

            VerticalDivider(
                modifier = Modifier.height(28.dp),
                color = WarmGray100,
                thickness = 1.dp,
            )

            RSVPStatItem(count = maybe, label = "Maybe", color = GoldDeep)

            VerticalDivider(
                modifier = Modifier.height(28.dp),
                color = WarmGray100,
                thickness = 1.dp,
            )

            RSVPStatItem(count = pending, label = "Pending", color = WarmGray400)

            VerticalDivider(
                modifier = Modifier.height(28.dp),
                color = WarmGray100,
                thickness = 1.dp,
            )

            RSVPStatItem(count = notGoing, label = "Declined", color = WarmGray400)
        }
    }
}

@Composable
private fun RSVPStatItem(count: Int, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text  = count.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (count > 0) color else WarmGray200,
        )
        Text(
            text      = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 0.2.sp,
            ),
            color = WarmGray400,
            textAlign = TextAlign.Center,
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
            .padding(horizontal = Spacing.screenHorizontal)
            .padding(top = Spacing.md, bottom = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Accent dot
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.40f)),
        )
        Spacer(Modifier.width(8.dp))

        // Section label — tracked small caps
        Text(
            text  = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = WarmGray500,
        )
        Spacer(Modifier.width(7.dp))

        // Count — subtle
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = WarmGray300,
        )
        Spacer(Modifier.width(Spacing.sm))

        // Hairline rule — extends to the right edge
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(WarmGray200, WarmGray100, Color.Transparent)
                    )
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
    plusOneCount: Int = 0,
    plusOneNames: List<String> = emptyList(),
    tableAssignment: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) ChampagneLight.copy(alpha = 0.45f) else Color.White,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentUser) 3.dp else 1.dp,
        ),
        border = if (isCurrentUser)
            androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.28f))
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
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // ── Avatar ─────────────────────────────────────────────────
                AvatarCircle(
                    name            = guest.name.ifBlank { "?" },
                    size = 40.dp,
                    backgroundColor = if (isCurrentUser) ChampagneLight else BlushLight.copy(alpha = 0.7f),
                    textColor       = if (isCurrentUser) GoldDeep else BlushDeep,
                )

                Spacer(Modifier.width(12.dp))

                // ── Name + chips + RSVP badge ──────────────────────────────
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(
                            text     = guest.name.ifBlank { "Anonymous" },
                            style    = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                            ),
                            color    = WarmGray800,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (isCurrentUser) YouChip()
                        if (guest.role == GuestRole.ADMIN || guest.role == GuestRole.COADMIN) {
                            RoleIndicator(guest.role)
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    RSVPBadge(rsvpStatus = guest.rsvpStatus)
                }

                Spacer(Modifier.width(Spacing.sm))

                // ── Right column: seating slot + expand chevron ────────────
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    FutureTablePlaceholder(tableAssignment = tableAssignment)
                    if (plusOneCount > 0) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess
                            else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Show guests",
                            tint = WarmGray300,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            // ── Plus-one section (accordion) ───────────────────────────────
            AnimatedVisibility(
                visible = expanded && plusOneCount > 0,
                enter   = expandVertically(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit    = shrinkVertically() + fadeOut(),
            ) {
                PlusOneSection(
                    count    = plusOneCount,
                    names    = plusOneNames,
                    modifier = Modifier.padding(
                        start = 66.dp,
                        end = 14.dp,
                        bottom = 12.dp,
                    ),
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
    val textColor = when (normalised) {
        RSVPStatus.GOING     -> BlushDeep
        RSVPStatus.MAYBE     -> GoldDeep
        RSVPStatus.NOT_GOING -> WarmGray500
        else -> WarmGray300
    }
    val bgColor = when (normalised) {
        RSVPStatus.GOING -> BlushLight.copy(alpha = 0.55f)
        RSVPStatus.MAYBE -> ChampagneLight.copy(alpha = 0.70f)
        RSVPStatus.NOT_GOING -> WarmGray100
        else                 -> WarmGray50
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 0.1.sp,
            ),
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
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(18.dp)
                        .background(WarmGray100),
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(WarmGray50),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = WarmGray300,
                        modifier = Modifier.size(13.dp),
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
            text = "+ $count joining",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 0.4.sp
            ),
            color = WarmGray300,
            modifier = Modifier.padding(start = 13.dp),
        )
    }
}

// ── Future seating placeholder ────────────────────────────────────────────────

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
                color = Gold.copy(alpha = 0.80f),
            )
            Text(
                text  = "Table",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    letterSpacing = 0.4.sp,
                ),
                color = WarmGray300,
            )
        }
    } else {
        // Reserved space — invisible to users but preserves card layout alignment
        Spacer(modifier = modifier.size(width = 20.dp, height = 14.dp))
    }
}

// ── You chip ──────────────────────────────────────────────────────────────────

@Composable
private fun YouChip() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Gold.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text  = "You",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
            ),
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
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
            ),
            color = color,
        )
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyGuestState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = Spacing.xxl, horizontal = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Spacer(Modifier.height(Spacing.xl))
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(ChampagneLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.PeopleAlt,
                contentDescription = null,
                tint = Gold.copy(alpha = 0.45f),
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(Modifier.height(Spacing.md))
        Text(
            text      = "No guests yet",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Light),
            color = WarmGray700,
            textAlign = TextAlign.Center,
        )
        Text(
            text      = "Guests will appear here\nonce they join the wedding",
            style     = MaterialTheme.typography.bodyMedium,
            color = WarmGray400,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Background florals ────────────────────────────────────────────────────────

@Composable
private fun GuestListFlorals() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color(0xFFEDD9B8).copy(alpha = 0.15f),
            radius = 80.dp.toPx(),
            center = Offset(-15.dp.toPx(), 20.dp.toPx()),
        )
        drawCircle(
            color = Color(0xFFEDD9B8).copy(alpha = 0.12f),
            radius = 65.dp.toPx(),
            center = Offset(size.width, size.height - 50.dp.toPx()),
        )
    }
}
