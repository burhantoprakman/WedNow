package com.wednowapp.wednow.presentation.weddinginfo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wednowapp.wednow.domain.model.DressCodeData
import com.wednowapp.wednow.domain.model.MenuCourseData
import com.wednowapp.wednow.domain.model.TimelineEventData
import com.wednowapp.wednow.domain.model.Wedding
import com.wednowapp.wednow.ui.components.WedNowErrorScreen
import com.wednowapp.wednow.ui.components.WedNowLoadingScreen
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private data class ColorSwatch(val color: Color, val label: String, val hex: String)

private fun hexToColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) {
    Color.LightGray
}

private enum class ActiveEditor { DATE, VENUE, MENU, DRESS_CODE, TIMELINE }

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun WeddingInfoScreen(
    onBack: () -> Unit,
    viewModel: WeddingInfoViewModel = hiltViewModel(),
) {
    val state        by viewModel.state.collectAsStateWithLifecycle()
    val isPrivileged by viewModel.isPrivileged.collectAsStateWithLifecycle()
    val editMode by viewModel.editMode.collectAsStateWithLifecycle()
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    val scope        = rememberCoroutineScope()
    val context      = LocalContext.current

    LaunchedEffect(saveState) {
        when (val s = saveState) {
            is SaveState.Saved -> snackbarHost.showSnackbar("Changes saved ✓")
            is SaveState.Error -> snackbarHost.showSnackbar("Error: ${s.message}")
            else -> Unit
        }
    }

    when (val s = state) {
        is WeddingInfoState.Loading -> WedNowLoadingScreen()
        is WeddingInfoState.Error   -> WedNowErrorScreen(message = s.message, onRetry = viewModel::retry)
        is WeddingInfoState.Success -> WeddingInfoContent(
            wedding = draft ?: s.wedding,
            isPrivileged = isPrivileged,
            editMode = editMode,
            saveState = saveState,
            onBack = onBack,
            onCopyCode = {
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("Wedding Code", s.wedding.id))
                scope.launch { snackbarHost.showSnackbar("Code copied ✓") }
            },
            onEnterEditMode = viewModel::enterEditMode,
            onExitEditMode = viewModel::exitEditMode,
            onUpdateDraft = viewModel::updateDraft,
            onSave = viewModel::saveChanges,
            snackbarHost = snackbarHost,
        )
    }
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
private fun WeddingInfoContent(
    wedding: Wedding,
    isPrivileged: Boolean,
    editMode: Boolean,
    saveState: SaveState,
    onBack: () -> Unit,
    onCopyCode: () -> Unit,
    onEnterEditMode: () -> Unit,
    onExitEditMode: () -> Unit,
    onUpdateDraft: (Wedding) -> Unit,
    onSave: () -> Unit,
    snackbarHost: SnackbarHostState,
) {
    val context = LocalContext.current
    var activeEditor by remember { mutableStateOf<ActiveEditor?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            ChampagneLight.copy(alpha = 0.5f),
                            Ivory,
                            Ivory,
                            Ivory
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 140.dp),
        ) {
            item { InfoHeader(wedding = wedding, onBack = onBack) }

            item {
                Card(
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screenHorizontal),
                    shape     = RoundedCornerShape(24.dp),
                    colors    = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        EditSectionWrapper(
                            editMode = editMode,
                            onEdit = { activeEditor = ActiveEditor.DATE },
                        ) { DateSection(wedding = wedding, context = context) }

                        SectionDivider()

                        EditSectionWrapper(
                            editMode = editMode,
                            onEdit = { activeEditor = ActiveEditor.VENUE },
                        ) { VenueSection(wedding = wedding, context = context) }

                        SectionDivider()

                        EditSectionWrapper(
                            editMode = editMode,
                            onEdit = { activeEditor = ActiveEditor.MENU },
                        ) { MenuSection(wedding.menu) }

                        SectionDivider()

                        EditSectionWrapper(
                            editMode = editMode,
                            onEdit = { activeEditor = ActiveEditor.DRESS_CODE },
                        ) { DressCodeSection(wedding.dressCode) }

                        SectionDivider()

                        EditSectionWrapper(
                            editMode = editMode,
                            onEdit = { activeEditor = ActiveEditor.TIMELINE },
                        ) { ScheduleSection(wedding.timeline) }
                    }
                }
            }
        }

        // ── Edit mode floating action bar ─────────────────────────────────────
        AnimatedVisibility(
            visible = isPrivileged,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            EditModeBar(
                editMode = editMode,
                saveState = saveState,
                onEnter = onEnterEditMode,
                onDiscard = onExitEditMode,
                onSave = onSave,
            )
        }

        SnackbarHost(
            hostState = snackbarHost,
            modifier  = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = Spacing.sm),
        )
    }

    // ── Bottom sheet editors ──────────────────────────────────────────────────
    when (activeEditor) {
        ActiveEditor.DATE -> DateEditorSheet(
            current = wedding.date,
            onUpdate = { onUpdateDraft(wedding.copy(date = it)) },
            onDismiss = { activeEditor = null },
        )

        ActiveEditor.VENUE -> VenueEditorSheet(
            current = wedding.location,
            onUpdate = { onUpdateDraft(wedding.copy(location = it)) },
            onDismiss = { activeEditor = null },
        )

        ActiveEditor.MENU -> MenuEditorSheet(
            menu = wedding.menu,
            onUpdate = { onUpdateDraft(wedding.copy(menu = it)) },
            onDismiss = { activeEditor = null },
        )

        ActiveEditor.DRESS_CODE -> DressCodeEditorSheet(
            dressCode = wedding.dressCode,
            onUpdate = { onUpdateDraft(wedding.copy(dressCode = it)) },
            onDismiss = { activeEditor = null },
        )

        ActiveEditor.TIMELINE -> TimelineEditorSheet(
            timeline = wedding.timeline,
            onUpdate = { onUpdateDraft(wedding.copy(timeline = it)) },
            onDismiss = { activeEditor = null },
        )

        null -> Unit
    }
}

// ── Edit mode bar ─────────────────────────────────────────────────────────────

@Composable
private fun EditModeBar(
    editMode: Boolean,
    saveState: SaveState,
    onEnter: () -> Unit,
    onDiscard: () -> Unit,
    onSave: () -> Unit,
) {
    val isSaving = saveState is SaveState.Saving

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
    ) {
        AnimatedVisibility(!editMode) {
            // Entry pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                GoldDeep.copy(alpha = 0.92f),
                                Gold.copy(alpha = 0.92f)
                            )
                        )
                    )
                    .clickable(onClick = onEnter),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.Edit, null, Modifier.size(16.dp), Color.White)
                    Text(
                        "Update Wedding Details",
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.5.sp),
                        color = Color.White,
                    )
                }
            }
        }

        AnimatedVisibility(editMode) {
            // Active edit actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                // Discard
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.White.copy(alpha = 0.92f))
                        .border(0.5.dp, WarmGray200, RoundedCornerShape(26.dp))
                        .clickable(enabled = !isSaving, onClick = onDiscard),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Discard",
                        style = MaterialTheme.typography.labelLarge,
                        color = WarmGray600,
                    )
                }
                // Save
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Brush.linearGradient(listOf(Gold, GoldDeep)))
                        .clickable(enabled = !isSaving, onClick = onSave),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            Modifier.size(20.dp),
                            Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(Icons.Default.Check, null, Modifier.size(16.dp), Color.White)
                            Text(
                                "Save Changes",
                                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.5.sp),
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Edit section wrapper ──────────────────────────────────────────────────────

@Composable
private fun EditSectionWrapper(
    editMode: Boolean,
    onEdit: () -> Unit,
    content: @Composable () -> Unit,
) {
    val borderAlpha by animateFloatAsState(if (editMode) 1f else 0f, tween(300))

    Box(modifier = Modifier.fillMaxWidth()) {
        // Champagne glow border when in edit mode
        if (editMode) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                Gold.copy(alpha = 0.25f * borderAlpha),
                                Champagne.copy(alpha = 0.5f * borderAlpha),
                                Gold.copy(alpha = 0.25f * borderAlpha),
                            )
                        ),
                        shape = RoundedCornerShape(0.dp),
                    )
                    .background(Gold.copy(alpha = 0.015f * borderAlpha))
            )
        }

        content()

        // Floating pencil icon
        AnimatedVisibility(
            visible = editMode,
            enter = fadeIn(tween(200)) + expandVertically(),
            exit = fadeOut(tween(150)) + shrinkVertically(),
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp, top = 10.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Gold, GoldDeep)))
                    .clickable(onClick = onEdit),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}

// ── Elegant bottom sheet shell ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ElegantBottomSheetEditor(
    title: String,
    subtitle: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Ivory,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(36.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(WarmGray200),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
                .navigationBarsPadding()
                .padding(bottom = Spacing.xl),
        ) {
            // Sheet header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = WarmGray800,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmGray400,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(Gold, GoldDeep)))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                ) {
                    Text(
                        "Done",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.5.sp),
                        color = Color.White,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Gold.copy(alpha = 0.3f), Color.Transparent)
                        )
                    ),
            )
            Spacer(Modifier.height(Spacing.md))

            content()
        }
    }
}

// ── Editor text field ─────────────────────────────────────────────────────────

@Composable
private fun EditorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    singleLine: Boolean = true,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        placeholder = {
            Text(
                placeholder,
                style = MaterialTheme.typography.bodySmall,
                color = WarmGray300
            )
        },
        singleLine = singleLine,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Gold,
            unfocusedBorderColor = WarmGray200,
            focusedLabelColor = Gold,
            unfocusedLabelColor = WarmGray400,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
        ),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = WarmGray800),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    )
}

// ── Date editor ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateEditorSheet(
    current: Long,
    onUpdate: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val dpState = rememberDatePickerState(
        initialSelectedDateMillis = if (current == 0L) null else current,
    )

    ElegantBottomSheetEditor(
        title = "Wedding Date",
        subtitle = "When will you celebrate your love?",
        onDismiss = {
            dpState.selectedDateMillis?.let { onUpdate(it) }
            onDismiss()
        },
    ) {
        DatePicker(
            state = dpState,
            modifier = Modifier.fillMaxWidth(),
            colors = DatePickerDefaults.colors(
                containerColor = Ivory,
                titleContentColor = WarmGray600,
                headlineContentColor = WarmGray800,
                navigationContentColor = WarmGray600,
                weekdayContentColor = WarmGray500,
                dayContentColor = WarmGray800,
                disabledDayContentColor = WarmGray300,
                selectedDayContainerColor = Gold,
                selectedDayContentColor = Color.White,
                disabledSelectedDayContainerColor = WarmGray200,
                disabledSelectedDayContentColor = WarmGray400,
                todayDateBorderColor = Gold,
                todayContentColor = GoldDeep,
                yearContentColor = WarmGray700,
                currentYearContentColor = GoldDeep,
                selectedYearContainerColor = Gold,
                selectedYearContentColor = Color.White,
            ),
        )
    }
}

// ── Venue editor ──────────────────────────────────────────────────────────────

@Composable
private fun VenueEditorSheet(
    current: String,
    onUpdate: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(current) }

    ElegantBottomSheetEditor(
        title = "Venue",
        subtitle = "Where will the magic happen?",
        onDismiss = {
            onUpdate(text)
            onDismiss()
        },
    ) {
        EditorTextField(
            value = text,
            onValueChange = { text = it; onUpdate(it) },
            label = "Location",
            placeholder = "e.g. The Grand Ballroom, Istanbul",
            singleLine = false,
        )
    }
}

// ── Menu editor ───────────────────────────────────────────────────────────────

@Composable
private fun MenuEditorSheet(
    menu: List<MenuCourseData>,
    onUpdate: (List<MenuCourseData>) -> Unit,
    onDismiss: () -> Unit,
) {
    var courses by remember { mutableStateOf(menu.toMutableList().map { it.copy() }) }

    fun push() = onUpdate(courses.toList())

    ElegantBottomSheetEditor(
        title = "Wedding Menu",
        subtitle = "Curate your dining experience",
        onDismiss = { push(); onDismiss() },
    ) {
        MenuAccordionEditor(
            courses = courses,
            onChange = { courses = it.toMutableList(); push() },
        )
    }
}

@Composable
private fun MenuAccordionEditor(
    courses: List<MenuCourseData>,
    onChange: (List<MenuCourseData>) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        courses.forEachIndexed { idx, course ->
            MenuCourseEditorCard(
                course = course,
                onUpdate = { updated ->
                    onChange(courses.toMutableList().also { it[idx] = updated })
                },
                onDelete = {
                    onChange(courses.toMutableList().also { it.removeAt(idx) })
                },
            )
        }

        // Add course button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(
                            Gold.copy(alpha = 0.4f),
                            Champagne,
                            Gold.copy(alpha = 0.4f)
                        )
                    ),
                    RoundedCornerShape(16.dp)
                )
                .clickable {
                    onChange(
                        courses + MenuCourseData(
                            courseName = "New Course",
                            emoji = "🍽️",
                            items = emptyList(),
                        )
                    )
                }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(16.dp), Gold)
                Text(
                    "Add Course",
                    style = MaterialTheme.typography.labelMedium,
                    color = Gold,
                )
            }
        }
    }
}

@Composable
private fun MenuCourseEditorCard(
    course: MenuCourseData,
    onUpdate: (MenuCourseData) -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var newItem by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(0.5.dp, WarmGray100, RoundedCornerShape(16.dp)),
    ) {
        // Course header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = Spacing.md, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            // Emoji picker (tap to cycle)
            val emojiOptions = listOf("🥗", "🍽️", "🥩", "🐟", "🍰", "🍷", "🥂", "☕", "🎂", "🍹")
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(ChampagneLight)
                    .border(0.5.dp, Gold.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    .clickable {
                        val next = (emojiOptions.indexOf(course.emoji) + 1).coerceIn(
                            0,
                            emojiOptions.lastIndex
                        )
                        onUpdate(course.copy(emoji = emojiOptions[next]))
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(course.emoji.ifBlank { "🍽️" }, fontSize = 16.sp)
            }

            OutlinedTextField(
                value = course.courseName,
                onValueChange = { onUpdate(course.copy(courseName = it)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = WarmGray800,
                    fontWeight = FontWeight.Medium
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(8.dp),
            )

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.DeleteOutline, null, Modifier.size(16.dp), WarmGray300)
            }

            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null, Modifier.size(18.dp), WarmGray300,
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md)
                    .padding(bottom = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                course.items.forEachIndexed { i, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(Gold.copy(alpha = 0.6f), CircleShape),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            item,
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmGray700,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = {
                                onUpdate(
                                    course.copy(
                                        items = course.items.toMutableList()
                                            .also { it.removeAt(i) })
                                )
                            },
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(Icons.Default.Close, null, Modifier.size(12.dp), WarmGray300)
                        }
                    }
                }
                // Add item row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = newItem,
                        onValueChange = { newItem = it },
                        placeholder = {
                            Text(
                                "Add dish…",
                                style = MaterialTheme.typography.bodySmall,
                                color = WarmGray300
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = WarmGray100,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                        ),
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = WarmGray800),
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (newItem.isNotBlank()) Gold else WarmGray100)
                            .clickable(enabled = newItem.isNotBlank()) {
                                onUpdate(course.copy(items = course.items + newItem.trim()))
                                newItem = ""
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Add,
                            null,
                            Modifier.size(16.dp),
                            if (newItem.isNotBlank()) Color.White else WarmGray300
                        )
                    }
                }
            }
        }
    }
}

// ── Dress code editor ─────────────────────────────────────────────────────────

private val kDressStyles = listOf(
    "Black Tie", "Formal", "Cocktail", "Semi-Formal",
    "Smart Casual", "Garden Party", "Beach Formal", "Casual Chic",
)

private val kWeddingPalette = listOf(
    "#F5E6C8" to "Champagne",
    "#FADADD" to "Rose",
    "#E8D5B7" to "Ivory",
    "#B8975A" to "Gold",
    "#8C9BAD" to "Dusty Blue",
    "#C4A7C7" to "Lavender",
    "#7A9E7E" to "Sage",
    "#D4848A" to "Blush",
    "#F0EAD6" to "Cream",
    "#A0856C" to "Mocha",
)

@Composable
private fun DressCodeEditorSheet(
    dressCode: DressCodeData,
    onUpdate: (DressCodeData) -> Unit,
    onDismiss: () -> Unit,
) {
    var dc by remember { mutableStateOf(dressCode) }
    var newSuggested by remember { mutableStateOf("") }
    var newAvoid by remember { mutableStateOf("") }

    fun push() = onUpdate(dc)

    ElegantBottomSheetEditor(
        title = "Dress Code",
        subtitle = "Set the tone for your celebration",
        onDismiss = { push(); onDismiss() },
    ) {
        // Style selector
        Text(
            "Style",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = WarmGray400,
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            kDressStyles.forEach { style ->
                val selected = dc.style == style
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (selected) Brush.linearGradient(
                                listOf(
                                    Gold,
                                    GoldDeep
                                )
                            ) else Brush.linearGradient(listOf(Color.White, Color.White))
                        )
                        .border(
                            0.5.dp,
                            if (selected) Color.Transparent else WarmGray200,
                            RoundedCornerShape(20.dp),
                        )
                        .clickable { dc = dc.copy(style = style); push() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        style,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) Color.White else WarmGray600,
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.lg))

        // Color palette
        Text(
            "Color Palette",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = WarmGray400,
        )
        Spacer(Modifier.height(8.dp))

        // Current swatches
        if (dc.colorHexes.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                dc.colorHexes.zip(dc.colorLabels).forEachIndexed { i, (hex, label) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(hexToColor(hex))
                                .border(1.dp, WarmGray200, CircleShape)
                                .clickable {
                                    dc = dc.copy(
                                        colorHexes = dc.colorHexes.toMutableList()
                                            .also { it.removeAt(i) },
                                        colorLabels = dc.colorLabels.toMutableList()
                                            .also { it.removeAt(i) },
                                    )
                                    push()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Close,
                                null,
                                Modifier.size(10.dp),
                                Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = WarmGray400,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(Modifier.height(Spacing.sm))
        }

        // Palette picker
        Text(
            "Tap to add",
            style = MaterialTheme.typography.bodySmall,
            color = WarmGray300,
        )
        Spacer(Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            kWeddingPalette.forEach { (hex, label) ->
                val alreadyAdded = dc.colorHexes.contains(hex)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(hexToColor(hex))
                        .border(
                            if (alreadyAdded) 2.dp else 0.5.dp,
                            if (alreadyAdded) Gold else WarmGray200,
                            CircleShape,
                        )
                        .clickable(enabled = !alreadyAdded) {
                            dc = dc.copy(
                                colorHexes = dc.colorHexes + hex,
                                colorLabels = dc.colorLabels + label,
                            )
                            push()
                        },
                )
            }
        }

        Spacer(Modifier.height(Spacing.lg))

        // Suggested
        EditableChipGroup(
            label = "Suggested",
            items = dc.suggested,
            accentColor = Color(0xFF6A8C6A),
            bgColor = Color(0xFFD8EED8),
            newText = newSuggested,
            onNewText = { newSuggested = it },
            onAdd = {
                dc = dc.copy(suggested = dc.suggested + newSuggested.trim())
                newSuggested = ""; push()
            },
            onRemove = { i ->
                dc = dc.copy(suggested = dc.suggested.toMutableList().also { it.removeAt(i) })
                push()
            },
        )

        Spacer(Modifier.height(Spacing.md))

        // Avoid
        EditableChipGroup(
            label = "Please Avoid",
            items = dc.avoid,
            accentColor = WarmGray400,
            bgColor = WarmGray100,
            newText = newAvoid,
            onNewText = { newAvoid = it },
            onAdd = {
                dc = dc.copy(avoid = dc.avoid + newAvoid.trim())
                newAvoid = ""; push()
            },
            onRemove = { i ->
                dc = dc.copy(avoid = dc.avoid.toMutableList().also { it.removeAt(i) })
                push()
            },
        )
    }
}

@Composable
private fun EditableChipGroup(
    label: String,
    items: List<String>,
    accentColor: Color,
    bgColor: Color,
    newText: String,
    onNewText: (String) -> Unit,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
        color = WarmGray400,
    )
    Spacer(Modifier.height(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items.forEachIndexed { i, item ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    item,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = accentColor
                )
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .clickable { onRemove(i) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Close,
                        null,
                        Modifier.size(9.dp),
                        accentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = newText,
            onValueChange = onNewText,
            placeholder = {
                Text(
                    "Add item…",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmGray300
                )
            },
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold,
                unfocusedBorderColor = WarmGray100,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
            ),
            textStyle = MaterialTheme.typography.bodySmall.copy(color = WarmGray800),
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (newText.isNotBlank()) Gold else WarmGray100)
                .clickable(enabled = newText.isNotBlank(), onClick = onAdd),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Add,
                null,
                Modifier.size(16.dp),
                if (newText.isNotBlank()) Color.White else WarmGray300
            )
        }
    }
}

// ── Timeline editor ───────────────────────────────────────────────────────────

@Composable
private fun TimelineEditorSheet(
    timeline: List<TimelineEventData>,
    onUpdate: (List<TimelineEventData>) -> Unit,
    onDismiss: () -> Unit,
) {
    var events by remember { mutableStateOf(timeline.toList()) }
    fun push() = onUpdate(events)

    ElegantBottomSheetEditor(
        title = "Day Schedule",
        subtitle = "Design the flow of your celebration",
        onDismiss = { push(); onDismiss() },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            events.forEachIndexed { idx, event ->
                EditableTimelineCard(
                    event = event,
                    index = idx,
                    total = events.size,
                    onUpdate = { updated ->
                        events = events.toMutableList().also { it[idx] = updated }
                        push()
                    },
                    onDelete = {
                        events = events.toMutableList().also { it.removeAt(idx) }
                        push()
                    },
                    onMoveUp = {
                        if (idx > 0) {
                            events = events.toMutableList().also {
                                val tmp = it[idx - 1]; it[idx - 1] = it[idx]; it[idx] = tmp
                            }
                            push()
                        }
                    },
                    onMoveDown = {
                        if (idx < events.lastIndex) {
                            events = events.toMutableList().also {
                                val tmp = it[idx + 1]; it[idx + 1] = it[idx]; it[idx] = tmp
                            }
                            push()
                        }
                    },
                )
            }

            // Add event button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                Gold.copy(alpha = 0.4f),
                                Champagne,
                                Gold.copy(alpha = 0.4f)
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        events = events + TimelineEventData(
                            time = "00:00",
                            title = "New Event",
                            status = "upcoming"
                        )
                        push()
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp), Gold)
                    Text("Add Event", style = MaterialTheme.typography.labelMedium, color = Gold)
                }
            }
        }
    }
}

@Composable
private fun EditableTimelineCard(
    event: TimelineEventData,
    index: Int,
    total: Int,
    onUpdate: (TimelineEventData) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (event.status) {
        "current" -> BlushDeep
        "completed" -> Gold.copy(alpha = 0.8f)
        else -> WarmGray300
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(0.5.dp, WarmGray100, RoundedCornerShape(16.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = Spacing.md, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(statusColor, CircleShape),
            )

            // Time badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ChampagneLight)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    event.time.ifBlank { "--:--" },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = GoldDeep,
                )
            }

            Text(
                event.title.ifBlank { "Untitled" },
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = WarmGray700,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // Reorder
            Column {
                IconButton(
                    onClick = onMoveUp,
                    modifier = Modifier.size(20.dp),
                    enabled = index > 0
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        null,
                        Modifier.size(14.dp),
                        if (index > 0) WarmGray400 else WarmGray100
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    modifier = Modifier.size(20.dp),
                    enabled = index < total - 1
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        null,
                        Modifier.size(14.dp),
                        if (index < total - 1) WarmGray400 else WarmGray100
                    )
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.DeleteOutline, null, Modifier.size(14.dp), WarmGray300)
            }

            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null,
                Modifier.size(16.dp),
                WarmGray300
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md)
                    .padding(bottom = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    OutlinedTextField(
                        value = event.time,
                        onValueChange = { onUpdate(event.copy(time = it)) },
                        label = { Text("Time", style = MaterialTheme.typography.bodySmall) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = WarmGray100,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                        ),
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = WarmGray800),
                    )

                    // Status picker
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Status",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmGray400
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("upcoming", "current", "completed").forEach { s ->
                                val sel = event.status == s
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (sel) Gold else WarmGray100)
                                        .clickable { onUpdate(event.copy(status = s)) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Text(
                                        s.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = if (sel) Color.White else WarmGray500,
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = event.title,
                    onValueChange = { onUpdate(event.copy(title = it)) },
                    label = { Text("Title", style = MaterialTheme.typography.bodySmall) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = WarmGray100,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    ),
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = WarmGray800),
                )

                OutlinedTextField(
                    value = event.description,
                    onValueChange = { onUpdate(event.copy(description = it)) },
                    label = { Text("Description", style = MaterialTheme.typography.bodySmall) },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = WarmGray100,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    ),
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = WarmGray800),
                )
            }
        }
    }
}

// ── Existing display sections (unchanged) ─────────────────────────────────────

@Composable
private fun InfoHeader(wedding: Wedding, onBack: () -> Unit) {
    if (wedding.coverImageUrl.isNotBlank()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
        ) {
            AsyncImage(
                model = wedding.coverImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.30f),
                            0.5f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.55f),
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .padding(Spacing.screenHorizontal, Spacing.sm)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", Modifier.size(20.dp), Color.White)
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = Spacing.lg, start = Spacing.md, end = Spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = wedding.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "EVENT DETAILS",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
        }
        Spacer(Modifier.height(Spacing.lg))
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                        Icons.AutoMirrored.Filled.ArrowBack,
                        "Back",
                        Modifier.size(20.dp),
                        WarmGray600
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screenHorizontal)
                    .padding(bottom = Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Default.Favorite, null, Modifier.size(16.dp), Gold.copy(alpha = 0.5f))
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = wedding.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = WarmGray800,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Event Details",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = WarmGray400
                )
            }
        }
    }
}

@Composable
private fun DateSection(wedding: Wedding, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openCalendar(context, wedding) }
            .padding(horizontal = Spacing.cardLg, vertical = Spacing.cardMd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionIconBox(Icons.Default.CalendarMonth, BlushDeep, BlushLight)
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "WEDDING DATE",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontSize = 9.sp
                ),
                color = WarmGray400
            )
            Spacer(Modifier.height(2.dp))
            Text(
                if (wedding.date == 0L) "Date TBA" else formatWeddingDate(wedding.date),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = WarmGray800
            )
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(16.dp), WarmGray200)
    }
}

@Composable
private fun VenueSection(wedding: Wedding, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openMaps(context, wedding.location) }
            .padding(horizontal = Spacing.cardLg, vertical = Spacing.cardMd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionIconBox(Icons.Default.LocationOn, Gold, ChampagneLight)
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "VENUE",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontSize = 9.sp
                ),
                color = WarmGray400
            )
            Spacer(Modifier.height(2.dp))
            Text(
                wedding.location.ifBlank { "Venue TBA" },
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = WarmGray800,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "Tap for directions",
                style = MaterialTheme.typography.bodySmall,
                color = Gold.copy(alpha = 0.75f)
            )
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(16.dp), WarmGray200)
    }
}

@Composable
private fun MenuSection(menu: List<MenuCourseData>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.cardLg, vertical = Spacing.cardMd)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            SectionIconBox(Icons.Default.Restaurant, Color(0xFF7A9E7E), Color(0xFFD8EED8))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "WEDDING MENU",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontSize = 9.sp
                    ),
                    color = WarmGray400
                )
                Text(
                    if (menu.isEmpty()) "TBA" else "${menu.size} courses",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = WarmGray600
                )
            }
        }
        if (menu.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.md))
            menu.forEachIndexed { i, course ->
                MenuCategoryRow(course = course)
                if (i < menu.lastIndex) Spacer(Modifier.height(Spacing.xs))
            }
        }
    }
}

@Composable
private fun MenuCategoryRow(course: MenuCourseData) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (expanded) WarmGray50 else Color.Transparent)
            .border(0.5.dp, if (expanded) WarmGray200 else WarmGray100, RoundedCornerShape(14.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(0.5.dp, WarmGray100, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(course.emoji, fontSize = 14.sp)
            }
            Spacer(Modifier.width(Spacing.sm))
            Text(
                course.courseName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                color = WarmGray700,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(WarmGray100)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    "${course.items.size}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = WarmGray500
                )
            }
            Spacer(Modifier.width(6.dp))
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null,
                Modifier.size(18.dp),
                WarmGray300
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter   = expandVertically(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit    = shrinkVertically() + fadeOut(),
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Spacing.md, end = Spacing.md, bottom = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement   = Arrangement.spacedBy(6.dp),
            ) {
                course.items.forEach { item -> MenuItemChip(item) }
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
            text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = WarmGray700
        )
    }
}

@Composable
private fun DressCodeSection(dressCode: DressCodeData) {
    val swatches = dressCode.colorHexes.zip(dressCode.colorLabels).map { (hex, label) ->
        ColorSwatch(hexToColor(hex), label, hex)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.cardLg, vertical = Spacing.cardMd)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            SectionIconBox(Icons.Default.Style, BlushDeep, BlushLight)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "DRESS CODE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontSize = 9.sp
                    ),
                    color = WarmGray400
                )
                Text(
                    dressCode.style.ifBlank { "TBA" },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = WarmGray800
                )
            }
        }

        if (swatches.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.md))
            Text(
                "Color Palette",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                color = WarmGray400
            )
            Spacer(Modifier.height(Spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                swatches.forEach { swatch ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(swatch.color)
                                .border(1.dp, WarmGray200, CircleShape)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            swatch.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = WarmGray400,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        if (dressCode.suggested.isNotEmpty() || dressCode.avoid.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                if (dressCode.suggested.isNotEmpty()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "✓  Suggested",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF6A8C6A)
                        )
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            dressCode.suggested.forEach { hint ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFFD8EED8))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        hint,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = Color(0xFF3A5A3A)
                                    )
                                }
                            }
                        }
                    }
                }
                if (dressCode.avoid.isNotEmpty()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "✗  Please avoid",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = WarmGray400
                        )
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            dressCode.avoid.forEach { hint ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(WarmGray100)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        hint,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = WarmGray500
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleSection(timeline: List<TimelineEventData>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.cardLg, vertical = Spacing.cardMd)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            SectionIconBox(Icons.Default.Schedule, Gold, ChampagneLight)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "EVENT SCHEDULE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontSize = 9.sp
                    ),
                    color = WarmGray400
                )
                Text(
                    if (timeline.isEmpty()) "TBA" else "${timeline.size} events today",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = WarmGray600
                )
            }
        }

        if (timeline.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.md))
            timeline.forEachIndexed { index, item ->
                CompactScheduleRow(
                    item = item,
                    isFirst = index == 0,
                    isLast = index == timeline.lastIndex
                )
            }
        }
    }
}

@Composable
private fun CompactScheduleRow(item: TimelineEventData, isFirst: Boolean, isLast: Boolean) {
    val isCurrent   = item.status == "current"
    val isCompleted = item.status == "completed"
    val dotColor = when {
        isCompleted -> Gold.copy(alpha = 0.7f); isCurrent -> BlushDeep; else -> WarmGray200
    }
    val lineColor = if (isCompleted) Gold.copy(alpha = 0.35f) else WarmGray200

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(20.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (!isFirst) Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .fillMaxHeight(0.5f)
                    .background(lineColor)
                    .align(Alignment.TopCenter)
            )
            if (!isLast) Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .fillMaxHeight(0.5f)
                    .background(lineColor)
                    .align(Alignment.BottomCenter)
            )
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 10.dp else 8.dp)
                    .background(dotColor, CircleShape)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) Icon(Icons.Default.Check, null, Modifier.size(5.dp), Color.White)
            }
        }
        Spacer(Modifier.width(10.dp))
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(top = 1.dp, bottom = if (isLast) 0.dp else 9.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                item.time,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                color = if (isCurrent) Gold else WarmGray400,
                modifier = Modifier.width(40.dp)
            )
            Text(
                item.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 12.sp
                ),
                color = when {
                    isCompleted -> WarmGray400; isCurrent -> WarmGray800; else -> WarmGray600
                },
                modifier = Modifier.weight(1f),
            )
            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(BlushLight)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "NOW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = BlushDeep
                    )
                }
            }
        }
    }
}

@Composable
private fun WeddingCodeCard(
    wedding: Wedding,
    onCopyCode: () -> Unit,
    modifier: Modifier = Modifier
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
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Key, null, Modifier.size(20.dp), GoldDeep)
        }
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "WEDDING CODE",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontSize = 9.sp
                ),
                color = WarmGray400
            )
            Text(
                wedding.id,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = WarmGray700,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onCopyCode) {
            Icon(
                Icons.Default.ContentCopy,
                "Copy code",
                Modifier.size(18.dp),
                GoldDeep.copy(alpha = 0.7f)
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
        Icon(icon, null, Modifier.size(22.dp), accentColor)
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = Spacing.cardLg),
        color = WarmGray100,
        thickness = 0.5.dp
    )
}

@Composable
private fun WeddingInfoFlorals() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            Color(0xFFEDD9B8).copy(alpha = 0.35f),
            90.dp.toPx(),
            Offset(-10.dp.toPx(), 20.dp.toPx())
        )
        drawCircle(
            Color(0xFFEAB8BC).copy(alpha = 0.14f),
            65.dp.toPx(),
            Offset(70.dp.toPx(), 90.dp.toPx())
        )
        drawCircle(
            Color(0xFFF5E6C8).copy(alpha = 0.38f),
            70.dp.toPx(),
            Offset(-20.dp.toPx(), 70.dp.toPx())
        )
        val bx = size.width; val by = size.height
        drawCircle(
            Color(0xFFEDD9B8).copy(alpha = 0.28f),
            80.dp.toPx(),
            Offset(bx, by - 40.dp.toPx())
        )
        drawCircle(
            Color(0xFFF5E6C8).copy(alpha = 0.32f),
            55.dp.toPx(),
            Offset(bx - 60.dp.toPx(), by - 80.dp.toPx())
        )
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
    try { context.startActivity(intent) } catch (_: Exception) { }
}

private fun openMaps(context: Context, location: String) {
    if (location.isBlank()) return
    val encoded = Uri.encode(location)
    try {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=$encoded")
            ).setPackage("com.google.android.apps.maps")
        )
        return
    } catch (_: Exception) { }
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encoded")))
    } catch (_: Exception) {
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val _previewWedding = com.wednowapp.wednow.domain.model.Wedding(
    id = "w1", shortCode = "WED123",
    name = "Sophie & James",
    date = 1781481600000L,
    location = "Grand Ballroom, New York",
    adminGuestId = "g1",
    createdAt = 1_700_000_000_000L,
    menu = listOf(
        com.wednowapp.wednow.domain.model.MenuCourseData(
            "Starter",
            "🥗",
            listOf("Caesar Salad", "Bruschetta")
        ),
        com.wednowapp.wednow.domain.model.MenuCourseData(
            "Main",
            "🍽️",
            listOf("Chicken Supreme", "Vegan Risotto")
        ),
        com.wednowapp.wednow.domain.model.MenuCourseData("Dessert", "🍰", listOf("Wedding Cake")),
    ),
    dressCode = com.wednowapp.wednow.domain.model.DressCodeData(style = "Black Tie"),
)

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Wedding Info – View"
)
@Composable
private fun WeddingInfoViewPreview() {
    com.wednowapp.wednow.ui.theme.WedNowTheme {
        WeddingInfoContent(
            wedding = _previewWedding,
            isPrivileged = false,
            editMode = false,
            saveState = com.wednowapp.wednow.presentation.weddinginfo.SaveState.Idle,
            onBack = {},
            onCopyCode = {},
            onEnterEditMode = {},
            onExitEditMode = {},
            onUpdateDraft = {},
            onSave = {},
            snackbarHost = androidx.compose.material3.SnackbarHostState(),
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Wedding Info – Admin Edit"
)
@Composable
private fun WeddingInfoEditPreview() {
    com.wednowapp.wednow.ui.theme.WedNowTheme {
        WeddingInfoContent(
            wedding = _previewWedding,
            isPrivileged = true,
            editMode = true,
            saveState = com.wednowapp.wednow.presentation.weddinginfo.SaveState.Idle,
            onBack = {},
            onCopyCode = {},
            onEnterEditMode = {},
            onExitEditMode = {},
            onUpdateDraft = {},
            onSave = {},
            snackbarHost = androidx.compose.material3.SnackbarHostState(),
        )
    }
}

private fun formatWeddingDate(ms: Long): String {
    if (ms == 0L) return ""
    val utc = TimeZone.getTimeZone("UTC")
    val date = Date(ms)
    val dateFmt = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).apply { timeZone = utc }
    val cal = Calendar.getInstance(utc).apply { time = date }
    if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0) return dateFmt.format(
        date
    )
    val timeFmt = SimpleDateFormat("h:mm a", Locale.ENGLISH).apply { timeZone = utc }
    return "${dateFmt.format(date)} • ${timeFmt.format(date)}"
}
