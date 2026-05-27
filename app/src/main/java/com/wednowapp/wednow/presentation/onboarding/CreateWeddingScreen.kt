package com.wednowapp.wednow.presentation.onboarding

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wednowapp.wednow.domain.model.TimelineEventData
import com.wednowapp.wednow.ui.theme.Blush
import com.wednowapp.wednow.ui.theme.BlushDeep
import com.wednowapp.wednow.ui.theme.BlushLight
import com.wednowapp.wednow.ui.theme.Champagne
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
import kotlinx.coroutines.launch

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun CreateWeddingScreen(
    onWeddingCreated: (weddingId: String) -> Unit,
    onJoinWeddingClick: () -> Unit,
    viewModel: CreateWeddingViewModel = hiltViewModel(),
) {
    val createState by viewModel.createState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    BackHandler(enabled = viewModel.step > 0) { viewModel.goBack() }

    LaunchedEffect(createState) {
        when (createState) {
            is CreateWeddingState.Success ->
                onWeddingCreated((createState as CreateWeddingState.Success).weddingId)
            is CreateWeddingState.Error ->
                snackbarHost.showSnackbar((createState as CreateWeddingState.Error).message)
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = Ivory,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            AnimatedContent(
                targetState = viewModel.step,
                transitionSpec = {
                    val fwd = targetState > initialState
                    slideInHorizontally(
                        tween(
                            380,
                            easing = FastOutSlowInEasing
                        )
                    ) { if (fwd) it else -it } +
                            fadeIn(tween(380)) togetherWith
                            slideOutHorizontally(tween(280)) { if (fwd) -it / 3 else it / 3 } +
                            fadeOut(tween(200))
                },
                label = "step",
            ) { step ->
                when (step) {
                    0 -> WelcomeStep(onBegin = viewModel::goNext, onJoin = onJoinWeddingClick)
                    8 -> InvitationStep(
                        vm = viewModel,
                        isLoading = createState is CreateWeddingState.Loading
                    )

                    else -> StepScaffold(vm = viewModel, step = step)
                }
            }
        }
    }
}

// ── Shared step scaffold (steps 1–7) ─────────────────────────────────────────

@Composable
private fun StepScaffold(vm: CreateWeddingViewModel, step: Int) {
    val buildingStep = step          // 1-indexed inside 1..7
    val progress by animateFloatAsState(
        targetValue = (buildingStep - 1) / 7f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "progress",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = vm::goBack) {
                Icon(
                    Icons.Default.ArrowBack, "Back",
                    tint = WarmGray500, modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "$buildingStep / 7",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = WarmGray400,
            )
        }

        // ── Progress bar ──────────────────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .padding(horizontal = Spacing.screenHorizontal),
        ) {
            drawLine(WarmGray100, Offset(0f, 0f), Offset(size.width, 0f), 4f, StrokeCap.Round)
            drawLine(
                Brush.horizontalGradient(listOf(Gold, Blush)),
                Offset(0f, 0f),
                Offset(size.width * progress, 0f),
                4f,
                StrokeCap.Round,
            )
        }

        // ── Step content ──────────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            when (step) {
                1 -> CoupleInfoStep(vm)
                2 -> DateTimeStep(vm)
                3 -> VenueStep(vm)
                4 -> CoverImageStep(vm)
                5 -> MenuStep(vm)
                6 -> DressCodeStep(vm)
                7 -> TimelineStep(vm)
            }
        }

        // ── Bottom action bar ─────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.lg)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (step > 1) {
                TextButton(onClick = vm::goBack) {
                    Text("Back", style = MaterialTheme.typography.labelLarge, color = WarmGray400)
                }
            }
            Spacer(Modifier.weight(1f))
            ElegantPrimaryButton(
                label = if (step == 7) "Review" else "Continue",
                enabled = vm.isNextEnabled(),
                onClick = vm::goNext,
            )
        }
    }
}

// ── Step 0 — Welcome ──────────────────────────────────────────────────────────

@Composable
private fun WelcomeStep(onBegin: () -> Unit, onJoin: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Soft background florals
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                Color(0xFFF5E6C8).copy(alpha = 0.45f),
                220.dp.toPx(),
                Offset(-60.dp.toPx(), -40.dp.toPx())
            )
            drawCircle(
                Color(0xFFEAB8BC).copy(alpha = 0.22f),
                160.dp.toPx(),
                Offset(size.width + 40.dp.toPx(), 120.dp.toPx())
            )
            drawCircle(
                Color(0xFFEDD9A8).copy(alpha = 0.30f),
                130.dp.toPx(),
                Offset(size.width - 30.dp.toPx(), size.height - 80.dp.toPx())
            )
            drawCircle(
                Color(0xFFF5E6C8).copy(alpha = 0.35f),
                180.dp.toPx(),
                Offset(50.dp.toPx(), size.height + 30.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.screenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))

            // Ornament
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    Modifier
                        .size(28.dp, 0.5.dp)
                        .background(Gold.copy(alpha = 0.4f))
                )
                Icon(Icons.Default.Favorite, null, Modifier.size(10.dp), Gold.copy(alpha = 0.7f))
                Box(
                    Modifier
                        .size(28.dp, 0.5.dp)
                        .background(Gold.copy(alpha = 0.4f))
                )
            }

            Spacer(Modifier.height(Spacing.xl))

            Text(
                text = "Let's create your\nwedding experience",
                style = MaterialTheme.typography.displayMedium.copy(lineHeight = 52.sp),
                color = WarmGray800,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Spacing.lg))

            Text(
                text = "Every love story deserves\na beautiful beginning.",
                style = TextStyle(
                    fontFamily = MaterialTheme.typography.headlineLarge.fontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 20.sp,
                    lineHeight = 28.sp,
                ),
                color = BlushDeep.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.weight(1.2f))

            ElegantPrimaryButton(
                label = "Begin",
                onClick = onBegin,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.md))

            TextButton(onClick = onJoin) {
                Text(
                    "Join an existing wedding",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmGray400,
                )
            }

            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

// ── Step 1 — Couple Info ──────────────────────────────────────────────────────

@Composable
private fun CoupleInfoStep(vm: CreateWeddingViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Spacing.screenHorizontal,
            vertical = Spacing.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        item { StepLabel("01", "Your Names") }
        item {
            Text(
                "How would you like to be introduced to your guests?",
                style = MaterialTheme.typography.bodyLarge,
                color = WarmGray500,
            )
        }
        item { Spacer(Modifier.height(Spacing.sm)) }

        // Live preview
        item {
            AnimatedVisibility(
                visible = vm.coupleName.isNotBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ChampagneLight.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.cardLg),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (vm.coupleSubtitle.isNotBlank()) {
                            Text(
                                text = vm.coupleSubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 1.sp),
                                color = WarmGray500,
                            )
                        }
                        Text(
                            text = vm.coupleName.ifBlank { "Your Names" },
                            style = MaterialTheme.typography.displaySmall,
                            color = WarmGray800,
                            textAlign = TextAlign.Center,
                        )
                        Icon(
                            Icons.Default.Favorite,
                            null,
                            Modifier.size(12.dp),
                            Gold.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        item {
            ElegantTextField(
                value = vm.coupleName,
                onValueChange = vm::onCoupleNameChange,
                label = "Couple Names",
                placeholder = "Sarah & James",
                imeAction = ImeAction.Next,
            )
        }
        item {
            ElegantTextField(
                value = vm.coupleSubtitle,
                onValueChange = vm::onCoupleSubtitleChange,
                label = "Subtitle (optional)",
                placeholder = "Together with their families",
                imeAction = ImeAction.Done,
            )
        }
    }
}

// ── Step 2 — Date & Time ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimeStep(vm: CreateWeddingViewModel) {
    // Date picker dialog
    if (vm.showDatePicker) {
        val dpState = rememberDatePickerState()
        BasicAlertDialog(onDismissRequest = vm::closeDatePicker) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Ivory),
            ) {
                Column(Modifier.padding(Spacing.md)) {
                    DatePicker(
                        state = dpState,
                        colors = DatePickerDefaults.colors(
                            containerColor = Ivory,
                            titleContentColor = WarmGray600,
                            headlineContentColor = WarmGray800,
                            selectedDayContainerColor = Gold,
                            selectedDayContentColor = Color.White,
                            todayDateBorderColor = Gold,
                            todayContentColor = Gold,
                        ),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = Spacing.md, bottom = Spacing.sm),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = vm::closeDatePicker) {
                            Text("Cancel", color = WarmGray400)
                        }
                        Spacer(Modifier.width(Spacing.sm))
                        TextButton(
                            onClick = { dpState.selectedDateMillis?.let { vm.onDateSelected(it) } },
                            enabled = dpState.selectedDateMillis != null,
                        ) {
                            Text("Select", color = Gold, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    // Time picker dialog
    if (vm.showTimePicker) {
        val tpState = rememberTimePickerState(is24Hour = false)
        BasicAlertDialog(onDismissRequest = vm::closeTimePicker) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Ivory),
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Select Time",
                        style = MaterialTheme.typography.headlineSmall,
                        color = WarmGray800
                    )
                    Spacer(Modifier.height(Spacing.lg))
                    TimePicker(
                        state = tpState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = ChampagneLight,
                            clockDialSelectedContentColor = Color.White,
                            selectorColor = Gold,
                            containerColor = Ivory,
                            timeSelectorSelectedContainerColor = Gold,
                            timeSelectorSelectedContentColor = Color.White,
                            timeSelectorUnselectedContainerColor = WarmGray100,
                        ),
                    )
                    Spacer(Modifier.height(Spacing.lg))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = vm::closeTimePicker) {
                            Text(
                                "Cancel",
                                color = WarmGray400
                            )
                        }
                        Spacer(Modifier.width(Spacing.sm))
                        TextButton(onClick = { vm.onTimeSelected(tpState.hour, tpState.minute) }) {
                            Text("Confirm", color = Gold, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Spacing.screenHorizontal,
            vertical = Spacing.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        item { StepLabel("02", "Date & Time") }
        item {
            Text(
                "When will you be celebrating your love?",
                style = MaterialTheme.typography.bodyLarge,
                color = WarmGray500,
            )
        }
        item { Spacer(Modifier.height(Spacing.sm)) }

        // Date formatted preview
        if (vm.formattedDate.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ChampagneLight.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.cardLg),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = vm.formattedDate,
                            style = MaterialTheme.typography.headlineSmall,
                            color = WarmGray800,
                            textAlign = TextAlign.Center,
                        )
                        if (vm.formattedTime.isNotBlank()) {
                            Text(
                                text = vm.formattedTime,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gold,
                            )
                        }
                    }
                }
            }
        }

        item {
            TappableInfoCard(
                icon = Icons.Default.CalendarMonth,
                label = "Wedding Date",
                value = vm.formattedDate.ifBlank { "Select a date" },
                isSelected = vm.formattedDate.isNotBlank(),
                onClick = vm::openDatePicker,
            )
        }
        item {
            TappableInfoCard(
                icon = Icons.Default.Schedule,
                label = "Ceremony Time",
                value = vm.formattedTime.ifBlank { "Select a time (optional)" },
                isSelected = vm.formattedTime.isNotBlank(),
                onClick = vm::openTimePicker,
            )
        }
    }
}

// ── Step 3 — Venue ────────────────────────────────────────────────────────────

@Composable
private fun VenueStep(vm: CreateWeddingViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Spacing.screenHorizontal,
            vertical = Spacing.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        item { StepLabel("03", "Your Venue") }
        item {
            Text(
                "Where will your celebration take place?",
                style = MaterialTheme.typography.bodyLarge,
                color = WarmGray500,
            )
        }
        item { Spacer(Modifier.height(Spacing.sm)) }

        if (vm.venue.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ChampagneLight.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.cardLg),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        Box(
                            Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Gold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.LocationOn, null, Modifier.size(22.dp), Gold)
                        }
                        Column {
                            Text(
                                "VENUE",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                color = WarmGray400
                            )
                            Text(
                                vm.venue,
                                style = MaterialTheme.typography.titleMedium,
                                color = WarmGray800
                            )
                        }
                    }
                }
            }
        }

        item {
            ElegantTextField(
                value = vm.venue,
                onValueChange = vm::onVenueChange,
                label = "Venue Name & Address",
                placeholder = "Grand Ballroom, New York",
                leadingIcon = Icons.Default.LocationOn,
                imeAction = ImeAction.Done,
            )
        }
    }
}

// ── Step 4 — Cover Image ──────────────────────────────────────────────────────

@Composable
private fun CoverImageStep(vm: CreateWeddingViewModel) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { vm.onCoverImageSelected(it) }
        }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Spacing.screenHorizontal,
            vertical = Spacing.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        item { StepLabel("04", "Your Photo") }
        item {
            Text(
                "Choose a beautiful photo that represents your love story.",
                style = MaterialTheme.typography.bodyLarge,
                color = WarmGray500,
            )
        }

        item {
            if (vm.coverImageUri != null) {
                // Immersive preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { launcher.launch("image/*") },
                ) {
                    AsyncImage(
                        model = vm.coverImageUri,
                        contentDescription = "Cover photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    0.7f to Color.Transparent,
                                    1f to Color.Black.copy(alpha = 0.5f),
                                )
                            ),
                    )
                    Text(
                        text = "Tap to change",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = Spacing.md),
                    )
                }
            } else {
                // Upload area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(ChampagneLight.copy(alpha = 0.5f))
                        .border(
                            1.5.dp,
                            Brush.linearGradient(
                                listOf(
                                    Gold.copy(alpha = 0.4f),
                                    Blush.copy(alpha = 0.3f)
                                )
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        Box(
                            Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Gold.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.CameraAlt, null, Modifier.size(28.dp), Gold)
                        }
                        Text(
                            "Upload your photo",
                            style = MaterialTheme.typography.titleSmall,
                            color = WarmGray700
                        )
                        Text(
                            "Tap to browse gallery",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmGray400
                        )
                    }
                }
            }
        }

        item {
            Text(
                "This photo will appear on your home screen and digital invitation.",
                style = MaterialTheme.typography.bodySmall,
                color = WarmGray400,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Step 5 — Menu ────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MenuStep(vm: CreateWeddingViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Spacing.screenHorizontal,
            vertical = Spacing.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        item { StepLabel("05", "Wedding Menu") }
        item {
            Text(
                "Build your menu — tap a course to add dishes.",
                style = MaterialTheme.typography.bodyLarge,
                color = WarmGray500,
            )
        }
        item { Spacer(Modifier.height(Spacing.xs)) }

        itemsIndexed(vm.menuCourses) { courseIdx, course ->
            MenuCourseEditor(
                course = course,
                courseIdx = courseIdx,
                onAddItem = { item -> vm.onMenuItemAdd(courseIdx, item) },
                onRemoveItem = { itemIdx -> vm.onMenuItemRemove(courseIdx, itemIdx) },
            )
        }

        item { Spacer(Modifier.height(Spacing.lg)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MenuCourseEditor(
    course: com.wednowapp.wednow.domain.model.MenuCourseData,
    courseIdx: Int,
    onAddItem: (String) -> Unit,
    onRemoveItem: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var newItem by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (expanded) WarmGray50 else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 0.dp else 2.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = Spacing.md, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ChampagneLight),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(course.emoji, fontSize = 18.sp)
                }
                Spacer(Modifier.width(Spacing.md))
                Column(Modifier.weight(1f)) {
                    Text(
                        course.courseName,
                        style = MaterialTheme.typography.titleSmall,
                        color = WarmGray800
                    )
                    Text(
                        text = if (course.items.isEmpty()) "No dishes added" else "${course.items.size} dish${if (course.items.size == 1) "" else "es"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmGray400,
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null,
                    tint = if (expanded) WarmGray400 else Gold,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Expanded content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md)
                        .padding(bottom = Spacing.md)
                ) {
                    HorizontalDivider(color = WarmGray100, thickness = 0.5.dp)
                    Spacer(Modifier.height(Spacing.sm))

                    // Existing items
                    if (course.items.isNotEmpty()) {
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            course.items.forEachIndexed { idx, item ->
                                DishChip(text = item, onRemove = { onRemoveItem(idx) })
                            }
                        }
                        Spacer(Modifier.height(Spacing.sm))
                    }

                    // Add new item row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        OutlinedTextField(
                            value = newItem,
                            onValueChange = { newItem = it },
                            placeholder = {
                                Text(
                                    "Add a dish...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WarmGray300
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = WarmGray200,
                                focusedTextColor = WarmGray800,
                                unfocusedTextColor = WarmGray700,
                                cursorColor = Gold,
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                if (newItem.isNotBlank()) {
                                    onAddItem(newItem); newItem = ""
                                }
                            }),
                        )
                        Box(
                            Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (newItem.isNotBlank()) Gold else WarmGray100)
                                .clickable(enabled = newItem.isNotBlank()) {
                                    onAddItem(newItem); newItem = ""
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Add, null,
                                Modifier.size(20.dp),
                                if (newItem.isNotBlank()) Color.White else WarmGray300,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DishChip(text: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ChampagneLight)
            .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = WarmGray700
        )
        Box(
            Modifier
                .size(18.dp)
                .clip(CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Close, null, Modifier.size(10.dp), WarmGray400)
        }
    }
}

// ── Step 6 — Dress Code ───────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DressCodeStep(vm: CreateWeddingViewModel) {
    val dressCodeEmojis = mapOf(
        "Black Tie" to "🎩",
        "Formal" to "👔",
        "Cocktail" to "👗",
        "Semi-Formal" to "👠",
        "Smart Casual" to "✨",
        "Garden Party" to "🌸",
        "Beach Casual" to "🌊",
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Spacing.screenHorizontal,
            vertical = Spacing.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        item { StepLabel("06", "Dress Code") }
        item {
            Text(
                "How would you like your guests to dress?",
                style = MaterialTheme.typography.bodyLarge,
                color = WarmGray500,
            )
        }
        item { Spacer(Modifier.height(Spacing.xs)) }

        item {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                CreateWeddingViewModel.DRESS_CODE_OPTIONS.forEach { style ->
                    val selected = vm.dressCodeStyle == style
                    Card(
                        onClick = { vm.onDressCodeSelected(style) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) Gold else Color.White,
                        ),
                        elevation = CardDefaults.cardElevation(if (selected) 0.dp else 2.dp),
                        border = if (selected) null else
                            androidx.compose.foundation.BorderStroke(0.5.dp, WarmGray200),
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                horizontal = Spacing.md,
                                vertical = Spacing.sm
                            ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(dressCodeEmojis[style] ?: "✨", fontSize = 20.sp)
                            Text(
                                text = style,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) Color.White else WarmGray700,
                            )
                        }
                    }
                }
            }
        }

        if (vm.dressCodeStyle.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ChampagneLight.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.cardLg),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            "DRESS CODE",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                            color = WarmGray400
                        )
                        Text(
                            vm.dressCodeStyle,
                            style = MaterialTheme.typography.headlineSmall,
                            color = WarmGray800
                        )
                    }
                }
            }
        }

        item {
            Text(
                "You can skip this step — dress code details can be customised later.",
                style = MaterialTheme.typography.bodySmall,
                color = WarmGray400,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Step 7 — Timeline ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineStep(vm: CreateWeddingViewModel) {
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    if (showAddSheet) {
        AddEventSheet(
            sheetState = sheetState,
            onDismiss = { showAddSheet = false },
            onAdd = { event ->
                vm.onTimelineEventAdd(event)
                scope.launch { sheetState.hide() }.invokeOnCompletion { showAddSheet = false }
            },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Spacing.screenHorizontal,
            vertical = Spacing.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        item { StepLabel("07", "Day Timeline") }
        item {
            Text(
                "Map out the flow of your wedding day.",
                style = MaterialTheme.typography.bodyLarge,
                color = WarmGray500,
            )
        }
        item { Spacer(Modifier.height(Spacing.xs)) }

        if (vm.timelineEvents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(WarmGray50)
                        .border(1.dp, WarmGray100, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("✦", fontSize = 20.sp, color = Gold.copy(alpha = 0.5f))
                        Text(
                            "No events yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmGray400
                        )
                        Text(
                            "Add your first moment below",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmGray300
                        )
                    }
                }
            }
        }

        itemsIndexed(vm.timelineEvents) { idx, event ->
            TimelineEventRow(event = event, onRemove = { vm.onTimelineEventRemove(idx) })
        }

        item {
            Card(
                onClick = { showAddSheet = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Gold.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp), Gold)
                    }
                    Spacer(Modifier.width(Spacing.sm))
                    Text("Add an event", style = MaterialTheme.typography.titleSmall, color = Gold)
                }
            }
        }

        item {
            Text(
                "You can also skip this and add your schedule later.",
                style = MaterialTheme.typography.bodySmall,
                color = WarmGray400,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TimelineEventRow(event: TimelineEventData, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        // Time column
        Text(
            text = event.time,
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.5.sp),
            color = Gold,
            modifier = Modifier.width(48.dp),
        )

        // Timeline dot + line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Gold)
            )
        }

        // Content card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = WarmGray800,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, null, Modifier.size(14.dp), WarmGray300)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEventSheet(
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onAdd: (TimelineEventData) -> Unit,
) {
    var time by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("celebration") }

    val iconEmojis = mapOf(
        "groups" to "👥", "wine_bar" to "🍷", "favorite" to "💍",
        "local_bar" to "🍸", "restaurant" to "🍽️", "music_note" to "🎵",
        "cake" to "🎂", "celebration" to "🎉", "nights_stay" to "🌙",
        "schedule" to "⏰", "camera" to "📷", "star" to "⭐",
    )

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
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text("Add Event", style = MaterialTheme.typography.headlineSmall, color = WarmGray800)
            Spacer(Modifier.height(Spacing.xs))

            ElegantTextField(
                value = time,
                onValueChange = { time = it },
                label = "Time",
                placeholder = "14:00  or  2:00 PM",
                imeAction = ImeAction.Next,
            )

            ElegantTextField(
                value = title,
                onValueChange = { title = it },
                label = "Event Title",
                placeholder = "Ceremony Begins",
                imeAction = ImeAction.Done,
            )

            Text("Icon", style = MaterialTheme.typography.labelMedium, color = WarmGray500)

            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CreateWeddingViewModel.TIMELINE_ICONS.forEach { iconName ->
                    val selected = selectedIcon == iconName
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (selected) Gold else WarmGray100)
                            .border(
                                width = if (selected) 0.dp else 0.5.dp,
                                color = WarmGray200,
                                shape = CircleShape,
                            )
                            .clickable { selectedIcon = iconName },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(iconEmojis[iconName] ?: "✨", fontSize = 20.sp)
                    }
                }
            }

            Spacer(Modifier.height(Spacing.sm))

            ElegantPrimaryButton(
                label = "Add Event",
                enabled = time.isNotBlank() && title.isNotBlank(),
                onClick = {
                    onAdd(
                        TimelineEventData(
                            time = time.trim(),
                            title = title.trim(),
                            iconName = selectedIcon,
                            status = "upcoming",
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.md))
        }
    }
}

// ── Step 8 — Invitation Preview ───────────────────────────────────────────────

@Composable
private fun InvitationStep(vm: CreateWeddingViewModel, isLoading: Boolean) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                Color(0xFFF5E6C8).copy(alpha = 0.40f),
                200.dp.toPx(),
                Offset(size.width + 40.dp.toPx(), -20.dp.toPx())
            )
            drawCircle(
                Color(0xFFEAB8BC).copy(alpha = 0.20f),
                140.dp.toPx(),
                Offset(-30.dp.toPx(), size.height * 0.4f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Back
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm)
            ) {
                IconButton(onClick = vm::goBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        "Back",
                        tint = WarmGray400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(Spacing.sm))

            Column(
                Modifier.padding(horizontal = Spacing.screenHorizontal),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Your Invitation",
                    style = MaterialTheme.typography.displaySmall,
                    color = WarmGray800,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Preview your digital wedding experience",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmGray500,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(Spacing.xl))

            // ── Invitation Card ───────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screenHorizontal),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    // Header — gradient with names (no image; QR will appear post-creation)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Champagne,
                                        ChampagneLight,
                                        BlushLight
                                    )
                                )
                            ),
                    ) {
                        // Soft decorative circles
                        Canvas(Modifier.fillMaxSize()) {
                            drawCircle(
                                Color(0xFFEDD9A8).copy(alpha = 0.35f),
                                100.dp.toPx(),
                                Offset(size.width - 40.dp.toPx(), 30.dp.toPx())
                            )
                            drawCircle(
                                Color(0xFFEAB8BC).copy(alpha = 0.25f),
                                80.dp.toPx(),
                                Offset(30.dp.toPx(), size.height - 20.dp.toPx())
                            )
                        }
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(Spacing.md),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                null,
                                Modifier.size(14.dp),
                                Gold.copy(alpha = 0.6f)
                            )
                            Text(
                                text = vm.coupleName.ifBlank { "Your Names" },
                                style = MaterialTheme.typography.headlineLarge,
                                color = WarmGray800,
                                textAlign = TextAlign.Center,
                            )
                            if (vm.coupleSubtitle.isNotBlank()) {
                                Text(
                                    text = vm.coupleSubtitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                    color = WarmGray500,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }

                    // Details
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.cardLg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        HorizontalDivider(color = WarmGray100, thickness = 0.5.dp)

                        if (vm.formattedDate.isNotBlank()) {
                            InvitationDetailRow(
                                emoji = "📅",
                                label = "DATE",
                                value = listOf(
                                    vm.formattedDate,
                                    vm.formattedTime
                                ).filter { it.isNotBlank() }.joinToString(" • "),
                            )
                        }
                        if (vm.venue.isNotBlank()) {
                            InvitationDetailRow(emoji = "📍", label = "VENUE", value = vm.venue)
                        }
                        if (vm.menuCourses.any { it.items.isNotEmpty() }) {
                            InvitationDetailRow(
                                emoji = "🍽️",
                                label = "MENU",
                                value = "${vm.menuCourses.count { it.items.isNotEmpty() }} courses",
                            )
                        }
                        if (vm.dressCodeStyle.isNotBlank()) {
                            InvitationDetailRow(
                                emoji = "👗",
                                label = "DRESS CODE",
                                value = vm.dressCodeStyle
                            )
                        }

                        HorizontalDivider(color = WarmGray100, thickness = 0.5.dp)

                        // QR placeholder
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                "SCAN TO JOIN",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                                color = WarmGray400,
                            )
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(WarmGray50)
                                    .border(1.dp, WarmGray100, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text("✦", fontSize = 18.sp, color = Gold.copy(alpha = 0.5f))
                                    Text(
                                        "Generated\nafter creation",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WarmGray300,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = WarmGray100, thickness = 0.5.dp)

                        // Decorative footer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier
                                    .size(24.dp, 0.5.dp)
                                    .background(Gold.copy(alpha = 0.3f))
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.Default.Favorite,
                                null,
                                Modifier.size(8.dp),
                                Gold.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                Modifier
                                    .size(24.dp, 0.5.dp)
                                    .background(Gold.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xl))

            // ── Share preview before creation ─────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = Spacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                // Share text preview
                val shareText = buildString {
                    appendLine("💍 You're invited to our wedding!")
                    appendLine()
                    if (vm.coupleName.isNotBlank()) appendLine(vm.coupleName)
                    if (vm.formattedDate.isNotBlank()) appendLine("📅 ${vm.formattedDate}")
                    if (vm.venue.isNotBlank()) appendLine("📍 ${vm.venue}")
                    appendLine()
                    append("More details coming soon — we can't wait to celebrate with you!")
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(ChampagneLight, Champagne)))
                        .clickable {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    "Wedding Invitation — ${vm.coupleName}"
                                )
                            }
                            context.startActivity(Intent.createChooser(intent, "Share via"))
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        Icon(Icons.Default.Share, null, Modifier.size(18.dp), WarmGray700)
                        Text(
                            "Share Invitation Preview",
                            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.5.sp),
                            color = WarmGray700,
                        )
                    }
                }

                // ── Create Wedding CTA ────────────────────────────────────────
                if (isLoading) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = Gold,
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    ElegantPrimaryButton(
                        label = "Create Wedding",
                        onClick = vm::submit,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextButton(onClick = vm::goBack, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Go back & edit",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmGray400
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun InvitationDetailRow(emoji: String, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(emoji, fontSize = 14.sp)
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = WarmGray400
            )
            Text(value, style = MaterialTheme.typography.bodyMedium, color = WarmGray700)
        }
    }
}

// ── Shared reusable components ────────────────────────────────────────────────

@Composable
private fun StepLabel(number: String, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = Gold,
        )
        Box(
            Modifier
                .size(20.dp, 0.5.dp)
                .background(WarmGray200)
        )
    }
    Spacer(Modifier.height(4.dp))
    Text(title, style = MaterialTheme.typography.displaySmall, color = WarmGray800)
}

@Composable
private fun ElegantTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    imeAction: ImeAction = ImeAction.Next,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
            color = WarmGray500
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = WarmGray300)
            },
            leadingIcon = if (leadingIcon != null) {
                { Icon(leadingIcon, null, Modifier.size(20.dp), WarmGray400) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = WarmGray800),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold,
                unfocusedBorderColor = WarmGray200,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = WarmGray50,
                cursorColor = Gold,
                focusedTextColor = WarmGray800,
                unfocusedTextColor = WarmGray700,
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = imeAction,
            ),
        )
    }
}

@Composable
private fun TappableInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ChampagneLight.copy(alpha = 0.7f) else Color.White,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 0.dp else 2.dp),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
        else null,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Gold.copy(alpha = 0.15f) else WarmGray100
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, Modifier.size(22.dp), if (isSelected) Gold else WarmGray400)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = WarmGray400
                )
                Text(value, style = MaterialTheme.typography.bodyMedium, color = WarmGray800)
            }
            if (isSelected) {
                Icon(Icons.Default.Check, null, Modifier.size(16.dp), Gold)
            }
        }
    }
}

@Composable
private fun ElegantPrimaryButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (enabled)
                    Brush.linearGradient(listOf(Gold, GoldDeep))
                else
                    Brush.linearGradient(listOf(WarmGray200, WarmGray200)),
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.5.sp),
            color = if (enabled) Color.White else WarmGray400,
        )
    }
}
