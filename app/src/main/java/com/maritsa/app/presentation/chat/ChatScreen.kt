package com.maritsa.app.presentation.chat

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maritsa.app.R
import com.maritsa.app.domain.model.ChatMessage
import com.maritsa.app.domain.model.Guest
import com.maritsa.app.domain.model.GuestRole
import com.maritsa.app.ui.theme.Champagne
import com.maritsa.app.ui.theme.ChampagneLight
import com.maritsa.app.ui.theme.Gold
import com.maritsa.app.ui.theme.GoldDeep
import com.maritsa.app.ui.theme.GoldLight
import com.maritsa.app.ui.theme.Ivory
import com.maritsa.app.ui.theme.WarmGray100
import com.maritsa.app.ui.theme.WarmGray200
import com.maritsa.app.ui.theme.WarmGray300
import com.maritsa.app.ui.theme.WarmGray400
import com.maritsa.app.ui.theme.WarmGray50
import com.maritsa.app.ui.theme.WarmGray500
import com.maritsa.app.ui.theme.WarmGray600
import com.maritsa.app.ui.theme.WarmGray700
import com.maritsa.app.ui.theme.WarmGray800
import com.maritsa.app.ui.theme.WarmWhite
import kotlinx.coroutines.launch
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

private val tabs = listOf("Group Chat", "Direct Messages")

@Composable
fun ChatScreen(
    onBack: () -> Unit,
    onNavigateToDm: (otherGuestId: String) -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ivory),
    ) {
        ChatTopBar(
            selectedTab = pagerState.currentPage,
            onTabSelected = { scope.launch { pagerState.animateScrollToPage(it) } },
            onBack = onBack,
        )

        HorizontalDivider(color = WarmGray100, thickness = 0.8.dp)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> GroupChatPage(viewModel = viewModel)
                1 -> DmGuestListPage(viewModel = viewModel, onNavigateToDm = onNavigateToDm)
            }
        }
    }
}

// ── Custom top bar ────────────────────────────────────────────────────────────

@Composable
private fun ChatTopBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ivory)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(WarmGray100)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = WarmGray600,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "Chat",
                style = TextStyle(
                    fontFamily = DancingScript,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp,
                    color = WarmGray800,
                ),
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(38.dp)) // balance back button
        }

        Spacer(Modifier.height(16.dp))

        // ── Pill tab switcher ─────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50.dp))
                .background(WarmGray100)
                .padding(4.dp),
        ) {
            tabs.forEachIndexed { i, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (selectedTab == i) Ivory else Color.Transparent)
                        .clickable { onTabSelected(i) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selectedTab == i) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.sp,
                        ),
                        color = if (selectedTab == i) Gold else WarmGray500,
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

// ── Group Chat page ───────────────────────────────────────────────────────────

@Composable
private fun GroupChatPage(viewModel: ChatViewModel) {
    val messages by viewModel.messages.collectAsState()
    val currentGuestId by viewModel.currentGuestId.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ivory)
            .imePadding(),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No messages yet — say hi! 👋",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmGray400,
                        )
                    }
                }
            } else {
                items(messages, key = { it.id }) { message ->
                    ChatBubble(message = message, isOwn = message.guestId == currentGuestId)
                }
            }
        }

        MessageInputBar(
            value = viewModel.inputText,
            onValueChange = viewModel::onInputChange,
            onSend = viewModel::sendMessage,
            enabled = viewModel.canSend,
        )
    }
}

// ── DM guest list page ────────────────────────────────────────────────────────

@Composable
private fun DmGuestListPage(
    viewModel: ChatViewModel,
    onNavigateToDm: (otherGuestId: String) -> Unit,
) {
    val guests by viewModel.otherGuests.collectAsState()

    if (guests.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Ivory),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No other guests yet",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmGray400,
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Ivory),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(guests, key = { it.id }) { guest ->
                GuestDmRow(guest = guest, onClick = { onNavigateToDm(guest.id) })
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = WarmGray100,
                    thickness = 0.6.dp,
                )
            }
        }
    }
}

@Composable
private fun GuestDmRow(guest: Guest, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(ChampagneLight, GoldLight))),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = guest.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = TextStyle(
                    fontFamily = DancingScript,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    color = GoldDeep,
                ),
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = guest.name.ifBlank { "Guest" },
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = WarmGray800,
            )
            Text(
                text = roleLabel(guest.role),
                style = MaterialTheme.typography.labelSmall,
                color = WarmGray400,
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = WarmGray300,
            modifier = Modifier.size(18.dp),
        )
    }
}

private fun roleLabel(role: String) = when (role) {
    GuestRole.ADMIN -> "Admin"
    GuestRole.COADMIN -> "Co-admin"
    else -> "Guest"
}

// ── Chat bubble ───────────────────────────────────────────────────────────────

@Composable
internal fun ChatBubble(message: ChatMessage, isOwn: Boolean) {
    val bubbleShape = if (isOwn)
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    else
        RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start,
    ) {
        if (!isOwn && message.guestName.isNotBlank()) {
            Text(
                text = message.guestName,
                style = MaterialTheme.typography.labelSmall,
                color = GoldDeep,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(
                    if (isOwn) Brush.linearGradient(listOf(Champagne, GoldLight))
                    else Brush.linearGradient(listOf(WarmGray100, WarmGray50))
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isOwn) WarmGray800 else WarmGray700,
            )
        }
        Text(
            text = formatTime(message.timestamp),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = WarmGray300,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        )
    }
}

// ── Message input bar ─────────────────────────────────────────────────────────

@Composable
internal fun MessageInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
) {
    Column(modifier = Modifier.background(Ivory)) {
        HorizontalDivider(color = WarmGray100, thickness = 0.8.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        "Write a message…",
                        color = WarmGray300,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                modifier = Modifier.weight(1f),
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold.copy(alpha = 0.55f),
                    unfocusedBorderColor = WarmGray200,
                    focusedContainerColor = WarmWhite,
                    unfocusedContainerColor = WarmWhite,
                    cursorColor = Gold,
                    focusedTextColor = WarmGray800,
                    unfocusedTextColor = WarmGray800,
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (enabled) Gold else WarmGray100)
                    .clickable(enabled = enabled, onClick = onSend),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (enabled) Color.White else WarmGray300,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

internal fun formatTime(millis: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))
