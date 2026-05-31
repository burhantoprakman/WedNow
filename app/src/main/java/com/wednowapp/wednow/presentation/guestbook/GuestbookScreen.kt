package com.wednowapp.wednow.presentation.guestbook

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wednowapp.wednow.R
import com.wednowapp.wednow.domain.model.GuestbookPost
import com.wednowapp.wednow.presentation.auth.LocalAuthViewModel
import com.wednowapp.wednow.presentation.auth.SignInBottomSheet
import com.wednowapp.wednow.ui.theme.BlushDeep
import com.wednowapp.wednow.ui.theme.Champagne
import com.wednowapp.wednow.ui.theme.ChampagneLight
import com.wednowapp.wednow.ui.theme.ErrorRose
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
import com.wednowapp.wednow.ui.theme.WarmWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

// ── Script font ───────────────────────────────────────────────────────────────

private val _gbProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs,
)

private val DancingScript = androidx.compose.ui.text.font.FontFamily(
    Font(GoogleFont("Dancing Script"), _gbProvider, FontWeight.Normal),
    Font(GoogleFont("Dancing Script"), _gbProvider, FontWeight.SemiBold),
)

// ── Entry point ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestbookScreen(
    onBack: () -> Unit,
    viewModel: GuestbookViewModel = hiltViewModel(),
) {
    val authViewModel = LocalAuthViewModel.current
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    var showSignIn by remember { mutableStateOf(false) }

    GuestbookContent(
        posts = posts,
        submitState = submitState,
        messageInput = viewModel.messageInput,
        selectedPhotoUris = viewModel.selectedPhotoUris,
        senderName = viewModel.senderName,
        onMessageChange = viewModel::onMessageChange,
        onPhotosSelected = viewModel::onPhotosSelected,
        onPhotoRemoved = viewModel::onPhotoRemoved,
        onSubmit = viewModel::submit,
        canSubmit = viewModel.canSubmit,
        onResetSubmit = viewModel::resetSubmitState,
        onBack = onBack,
        snackbarHost = snackbarHost,
        onRequestWrite = {
            if (authViewModel.isSignedIn) Unit
            else showSignIn = true
        },
        isSignedIn = authViewModel.isSignedIn,
        canEdit = viewModel::canEdit,
        canDelete = viewModel::canDelete,
        isOwned = viewModel::isOwned,
        onEdit = viewModel::openEdit,
        onDelete = viewModel::requestDelete,
    )

    // Delete confirmation
    viewModel.pendingDeletePost?.let {
        DeletePostDialog(
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete,
        )
    }

    // Edit sheet
    viewModel.editingPost?.let {
        EditPostSheet(
            initialMessage = viewModel.editMessageInput,
            existingPhotoUrls = viewModel.editExistingPhotoUrls,
            newPhotoUris = viewModel.editNewPhotoUris,
            onSave = viewModel::saveEdit,
            onMessageChange = viewModel::onEditMessageChange,
            onRemoveExistingPhoto = viewModel::removeExistingEditPhoto,
            onAddPhotos = viewModel::addEditPhotos,
            onRemoveNewPhoto = viewModel::removeNewEditPhoto,
            isSubmitting = viewModel.editSaving,
            onDismiss = viewModel::dismissEdit,
        )
    }

    if (showSignIn) {
        SignInBottomSheet(
            authViewModel = authViewModel,
            reason = "Sign in to leave a message in the guestbook.",
            onDismiss = { showSignIn = false; authViewModel.clearError() },
            onSuccess = { showSignIn = false },
        )
    }
}

// ── Content ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuestbookContent(
    posts: List<GuestbookPost>?,          // null = loading
    submitState: PostSubmitState,
    messageInput: String,
    selectedPhotoUris: List<Uri>,
    senderName: String,
    onMessageChange: (String) -> Unit,
    onPhotosSelected: (List<Uri>) -> Unit,
    onPhotoRemoved: (Uri) -> Unit,
    onSubmit: () -> Unit,
    canSubmit: Boolean,
    onResetSubmit: () -> Unit,
    onBack: () -> Unit,
    snackbarHost: SnackbarHostState,
    onRequestWrite: () -> Unit = {},     // called when unauthenticated user taps write
    isSignedIn: Boolean = true,
    canEdit: (GuestbookPost) -> Boolean = { false },
    canDelete: (GuestbookPost) -> Boolean = { false },
    isOwned: (GuestbookPost) -> Boolean = { false },
    onEdit: (GuestbookPost) -> Unit = {},
    onDelete: (GuestbookPost) -> Unit = {},
) {
    var showWriteSheet by remember { mutableStateOf(false) }
    val postList = posts ?: emptyList()
    val pageCount = postList.size + 1
    val pagerState = rememberPagerState(pageCount = { pageCount })

    LaunchedEffect(submitState) {
        when (val s = submitState) {
            is PostSubmitState.Success -> {
                snackbarHost.showSnackbar("Memory sealed ✦")
                onResetSubmit()
                showWriteSheet = false
            }
            is PostSubmitState.Error -> {
                snackbarHost.showSnackbar(s.message)
                onResetSubmit()
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Warm ivory background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(ChampagneLight.copy(alpha = 0.6f), Ivory, Ivory, Ivory)
                    )
                )
        )
        BookroomFlorals()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            GuestbookTopBar(
                onBack = onBack,
                onWrite = {
                    if (isSignedIn) showWriteSheet = true
                    else onRequestWrite()
                },
                currentPage = pagerState.currentPage,
                totalMemories = postList.size,
            )

            PageTurnContainer(
                pagerState = pagerState,
                modifier   = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { page, pageOffset ->
                if (page == 0) {
                    BookCoverPage(
                        isEmpty = postList.isEmpty(),
                        isLoading = posts == null,
                        pageOffset = pageOffset,
                        modifier   = Modifier.fillMaxSize(),
                    )
                } else {
                    val post = postList[page - 1]
                    GuestbookPage(
                        post = post,
                        pageIndex  = page,
                        totalPages = postList.size,
                        pageOffset = pageOffset,
                        modifier   = Modifier.fillMaxSize(),
                        canEdit = canEdit(post),
                        canDelete = canDelete(post),
                        isOwned = isOwned(post),
                        onEdit = { onEdit(post) },
                        onDelete = { onDelete(post) },
                    )
                }
            }

            PageIndicator(
                currentPage = pagerState.currentPage,
                totalPages  = pageCount,
                modifier    = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = Spacing.md),
            )
        }

        SnackbarHost(
            hostState = snackbarHost,
            modifier  = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = Spacing.xxl),
        )
    }

    if (showWriteSheet) {
        WriteMemorySheet(
            messageInput = messageInput,
            selectedPhotoUris = selectedPhotoUris,
            senderName = senderName,
            onMessageChange = onMessageChange,
            onPhotosSelected = onPhotosSelected,
            onPhotoRemoved = onPhotoRemoved,
            onSubmit = onSubmit,
            isSubmitting = submitState == PostSubmitState.Loading,
            canSubmit = canSubmit,
            onDismiss = { showWriteSheet = false },
        )
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun GuestbookTopBar(
    onBack: () -> Unit,
    onWrite: () -> Unit,
    currentPage: Int,
    totalMemories: Int,
) {
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
                tint = WarmGray600,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text  = "Wedding Guestbook",
                style = MaterialTheme.typography.headlineSmall,
                color = WarmGray800,
            )
            if (totalMemories > 0) {
                Text(
                    text  = "$totalMemories memories",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = WarmGray400,
                )
            }
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Gold)
                .clickable(onClick = onWrite),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Write a memory",
                modifier = Modifier.size(18.dp),
                tint = Color.White,
            )
        }
    }
}

// ── Page turn container ───────────────────────────────────────────────────────

@Composable
fun PageTurnContainer(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    content: @Composable (page: Int, pageOffset: Float) -> Unit,
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        beyondViewportPageCount = 1,
        pageSpacing = 0.dp,
    ) { page ->
        val pageOffset = (pagerState.currentPage - page).toFloat() +
                pagerState.currentPageOffsetFraction
        content(page, pageOffset)
    }
}

// ── Book cover page ───────────────────────────────────────────────────────────

@Composable
private fun BookCoverPage(
    isEmpty: Boolean,
    isLoading: Boolean,
    pageOffset: Float,
    modifier: Modifier = Modifier,
) {
    val serifFamily = MaterialTheme.typography.displayLarge.fontFamily

    Box(
        modifier = modifier.graphicsLayer {
            val fraction = pageOffset.absoluteValue.coerceIn(0f, 1f)
            scaleX = 1f - 0.05f * fraction
            scaleY = 1f - 0.05f * fraction
            alpha = 1f - 0.28f * fraction
            rotationY = -(pageOffset.coerceIn(-1f, 1f)) * 4f
            cameraDistance = 14f * density
        },
    ) {
        PageStack(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm)
                .shadow(
                    elevation = 16.dp,
                    shape = bookShape,
                    ambientColor = WarmGray700.copy(alpha = 0.25f),
                    spotColor = WarmGray700.copy(alpha = 0.35f),
                )
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Champagne.copy(alpha = 0.55f),
                            WarmWhite,
                            WarmWhite,
                            WarmWhite
                        )
                    ),
                    bookShape,
                )
                .clip(bookShape),
        ) {
            SpineShadow()
            PaperTexture()
            CoverBorder()
            CornerFlourish()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Gold.copy(alpha = 0.55f),
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.height(Spacing.lg))
                Text(
                    text  = "Our Wedding",
                    style = TextStyle(
                        fontFamily = DancingScript,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 46.sp,
                        lineHeight  = 52.sp,
                        color = WarmGray800,
                    ),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text  = "G U E S T B O O K",
                    style = TextStyle(
                        fontFamily    = serifFamily,
                        fontWeight    = FontWeight.Bold,
                        fontSize      = 14.sp,
                        letterSpacing = 4.sp,
                        color         = WarmGray600,
                    ),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(Spacing.xl))
                GoldDivider(Modifier.width(80.dp))
                Spacer(Modifier.height(Spacing.xl))
                when {
                    isLoading -> CircularProgressIndicator(
                        color = Gold.copy(alpha = 0.5f),
                        strokeWidth = 1.5.dp,
                        modifier = Modifier.size(22.dp),
                    )

                    isEmpty -> Text(
                        text = "Be the first to\nleave a memory",
                        style = TextStyle(
                            fontFamily = serifFamily,
                            fontStyle = FontStyle.Italic,
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            color = WarmGray500,
                        ),
                        textAlign = TextAlign.Center,
                    )

                    else -> {
                        Text(
                            text = "A treasured collection\nof heartfelt memories",
                            style = TextStyle(
                                fontFamily = serifFamily,
                                fontStyle = FontStyle.Italic,
                                fontSize = 15.sp,
                                lineHeight = 24.sp,
                                color = WarmGray500,
                            ),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(Spacing.xl))
                        Text(
                            text = "swipe to read  →",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                            color = Gold.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }
    }
}

// ── Guestbook page (one memory) ───────────────────────────────────────────────

@Composable
fun GuestbookPage(
    post: GuestbookPost,
    pageIndex: Int,
    totalPages: Int,
    pageOffset: Float,
    modifier: Modifier = Modifier,
    canEdit: Boolean = false,
    canDelete: Boolean = false,
    isOwned: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    Box(
        modifier = modifier.graphicsLayer {
            val fraction = pageOffset.absoluteValue.coerceIn(0f, 1f)
            scaleX = 1f - 0.05f * fraction
            scaleY = 1f - 0.05f * fraction
            alpha = 1f - 0.28f * fraction
            rotationY = -(pageOffset.coerceIn(-1f, 1f)) * 4f
            cameraDistance = 14f * density
        },
    ) {
        PageStack(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
        )

        MemoryEntryCard(
            post = post,
            pageIndex = pageIndex,
            totalPages = totalPages,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
            canEdit = canEdit,
            canDelete = canDelete,
            isOwned = isOwned,
            onEdit = onEdit,
            onDelete = onDelete,
        )
    }
}

// ── Memory entry card ─────────────────────────────────────────────────────────

@Composable
fun MemoryEntryCard(
    post: GuestbookPost,
    pageIndex: Int,
    totalPages: Int,
    modifier: Modifier = Modifier,
    previewPhotoUris: List<Uri> = emptyList(),   // overrides post.photoUrls for preview
    canEdit: Boolean = false,
    canDelete: Boolean = false,
    isOwned: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    val serifFamily = MaterialTheme.typography.displayLarge.fontFamily
    // Use URI overrides for preview mode, otherwise use stored URLs
    val photos: List<Any> = if (previewPhotoUris.isNotEmpty()) previewPhotoUris
    else post.photoUrls
    val hasPhotos = photos.isNotEmpty()

    Box(
        modifier = modifier
            .shadow(
                elevation = 14.dp,
                shape = bookShape,
                ambientColor = WarmGray700.copy(alpha = 0.2f),
                spotColor = WarmGray700.copy(alpha = 0.3f),
            )
            .background(WarmWhite, bookShape)
            .clip(bookShape),
    ) {
        SpineShadow()
        PaperTexture()
        CornerFlourish()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Page header ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Memory",
                        style = TextStyle(
                            fontFamily = serifFamily,
                            fontStyle = FontStyle.Italic,
                            fontSize = 12.sp,
                            color = WarmGray400,
                            letterSpacing = 0.5.sp,
                        ),
                    )
                    // "Your Message" badge
                    if (isOwned) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Gold.copy(alpha = 0.85f))
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                        ) {
                            Text(
                                "Your Message",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = Color.White,
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "$pageIndex  /  $totalPages",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = WarmGray300,
                    )
                    // ⋮ menu for edit/delete
                    if (canEdit || canDelete) {
                        var menuExpanded by remember { mutableStateOf(false) }
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(WarmGray100)
                                    .clickable { menuExpanded = true },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    null,
                                    Modifier.size(13.dp),
                                    WarmGray600,
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.background(Color.White),
                            ) {
                                if (canEdit) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Edit Message",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = WarmGray800
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Edit,
                                                null,
                                                Modifier.size(16.dp),
                                                GoldDeep
                                            )
                                        },
                                        onClick = { menuExpanded = false; onEdit() },
                                    )
                                }
                                if (canDelete) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Delete",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ErrorRose
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Delete,
                                                null,
                                                Modifier.size(16.dp),
                                                ErrorRose
                                            )
                                        },
                                        onClick = { menuExpanded = false; onDelete() },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.sm))
            GoldDivider(Modifier.fillMaxWidth())

            // ── Content area ───────────────────────────────────────────────
            if (hasPhotos) {
                Spacer(Modifier.height(Spacing.md))
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    MemoryPhotoGrid(
                        photos = photos,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.md))
                    ElegantWishText(
                        text = post.message,
                        compact = true,
                    )
                }
            } else {
                Spacer(Modifier.height(Spacing.xl))
                ElegantWishText(
                    text = post.message,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(Spacing.lg))
            HeartDivider()
            Spacer(Modifier.height(Spacing.lg))

            // ── Signature ──────────────────────────────────────────────────
            val displayName = post.senderName.ifBlank { "A Guest" }
            Text(
                text = "— $displayName",
                style = TextStyle(
                    fontFamily = serifFamily,
                    fontStyle  = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 15.sp,
                    color      = WarmGray600,
                ),
                textAlign = TextAlign.End,
                modifier  = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatPageDate(post.timestamp),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                color = WarmGray300,
                textAlign = TextAlign.End,
                modifier  = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Memory photo grid (1-2-3 photo layouts) ───────────────────────────────────

@Composable
fun MemoryPhotoGrid(photos: List<Any>, modifier: Modifier = Modifier) {
    if (photos.isEmpty()) return
    when (photos.size) {
        1 -> AsyncImage(
            model = photos[0],
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .fillMaxWidth()
                .height(190.dp)
                .rotate(-1f)
                .shadow(8.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp)),
        )

        2 -> Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            AsyncImage(
                model = photos[0],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(3f / 4f)
                    .rotate(-2f)
                    .shadow(6.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp)),
            )
            AsyncImage(
                model = photos[1],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(3f / 4f)
                    .rotate(1.5f)
                    .shadow(6.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp)),
            )
        }

        else -> Row(
            modifier = modifier.height(190.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            // Large photo on the left
            AsyncImage(
                model = photos[0],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxHeight()
                    .rotate(-1.5f)
                    .shadow(6.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp)),
            )
            // Two smaller photos stacked on the right
            Column(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                AsyncImage(
                    model = photos[1],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .rotate(2f)
                        .shadow(6.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp)),
                )
                AsyncImage(
                    model = photos[2],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .rotate(-1f)
                        .shadow(6.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp)),
                )
            }
        }
    }
}

// ── Elegant wish text ─────────────────────────────────────────────────────────

@Composable
fun ElegantWishText(
    text: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val scale = if (compact) 0.72f else 1f
    val fontSize = when {
        text.length > 180 -> 19.sp * scale
        text.length > 100 -> 23.sp * scale
        text.length > 60 -> 27.sp * scale
        else -> 31.sp * scale
    }
    val lineH = when {
        text.length > 180 -> 30.sp * scale
        text.length > 100 -> 36.sp * scale
        else -> 42.sp * scale
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text  = text,
            style = TextStyle(
                fontFamily = DancingScript,
                fontWeight = FontWeight.SemiBold,
                fontSize   = fontSize,
                lineHeight = lineH,
                color      = WarmGray800,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

// ── Write memory sheet ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WriteMemorySheet(
    messageInput: String,
    selectedPhotoUris: List<Uri>,
    senderName: String,
    onMessageChange: (String) -> Unit,
    onPhotosSelected: (List<Uri>) -> Unit,
    onPhotoRemoved: (Uri) -> Unit,
    onSubmit: () -> Unit,
    isSubmitting: Boolean,
    canSubmit: Boolean,
    onDismiss: () -> Unit,
) {
    val serifFamily = MaterialTheme.typography.displayLarge.fontFamily
    val sheetState  = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showPreview by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = WarmWhite,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(WarmGray200),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(Spacing.sm))

            // Title
            Text(
                text  = "Write a Memory",
                style = TextStyle(
                    fontFamily = DancingScript,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 34.sp,
                    color      = WarmGray800,
                ),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Share your heartfelt wishes for the couple",
                style = MaterialTheme.typography.bodySmall,
                color = WarmGray400,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Spacing.lg))
            GoldDivider(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Spacing.lg))

            // ── Photo picker row ───────────────────────────────────────────
            PhotoPickerRow(
                uris = selectedPhotoUris,
                onPhotosSelected = onPhotosSelected,
                onRemove = onPhotoRemoved,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(4.dp))
            Text(
                text = "Add up to 3 photos  (optional)",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
                color = WarmGray300,
            )

            Spacer(Modifier.height(Spacing.lg))

            // ── Paper text input ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 160.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .background(Ivory, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp)),
            ) {
                // Ruled lines
                Canvas(modifier = Modifier.matchParentSize()) {
                    val lineColor = WarmGray300.copy(alpha = 0.18f)
                    val spacing   = 30.dp.toPx()
                    var y = 54.dp.toPx()
                    while (y < size.height - 18.dp.toPx()) {
                        drawLine(
                            color = lineColor,
                            start = Offset(24.dp.toPx(), y),
                            end = Offset(size.width - 24.dp.toPx(), y),
                            strokeWidth = 0.7f,
                        )
                        y += spacing
                    }
                }
                TextField(
                    value         = messageInput,
                    onValueChange = onMessageChange,
                    placeholder = {
                        Text(
                            text  = "Write your heartfelt message here…",
                            style = TextStyle(
                                fontFamily = DancingScript,
                                fontWeight = FontWeight.Normal,
                                fontSize   = 22.sp,
                                color      = WarmGray300,
                            ),
                        )
                    },
                    textStyle = TextStyle(
                        fontFamily = DancingScript,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 22.sp,
                        lineHeight = 36.sp,
                        color      = WarmGray800,
                    ),
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    maxLines  = 10,
                    enabled   = !isSubmitting,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor  = Color.Transparent,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor  = Color.Transparent,
                    ),
                )
            }

            Spacer(Modifier.height(Spacing.lg))

            // ── Preview & seal row ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Preview button
                if (messageInput.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(WarmGray100)
                            .clickable { showPreview = true }
                            .padding(horizontal = 16.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Preview,
                                contentDescription = null,
                                tint = WarmGray500,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "Preview",
                                style = MaterialTheme.typography.labelMedium,
                                color = WarmGray500,
                            )
                        }
                    }
                    Spacer(Modifier.width(Spacing.md))
                }

                // Seal & Sign — wax seal circle
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Gold,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(52.dp),
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(if (canSubmit) Gold else WarmGray200)
                                .clickable(enabled = canSubmit, onClick = onSubmit),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = "Seal & Sign",
                                tint = if (canSubmit) Color.White else WarmGray400,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (canSubmit) "Seal & Sign" else "Write something first",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = if (canSubmit) Gold else WarmGray300,
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xl))
        }
    }

    // ── Memory preview dialog ──────────────────────────────────────────────────
    if (showPreview) {
        MemoryPreviewDialog(
            message = messageInput,
            senderName = senderName,
            previewPhotoUris = selectedPhotoUris,
            onDismiss = { showPreview = false },
            onPost = { showPreview = false; onSubmit() },
            canPost = canSubmit,
        )
    }
}

// ── Photo picker row ──────────────────────────────────────────────────────────

@Composable
private fun PhotoPickerRow(
    uris: List<Uri>,
    onPhotosSelected: (List<Uri>) -> Unit,
    onRemove: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val remaining = 3 - uris.size  // 3, 2, or 1

    // PickMultipleVisualMedia requires maxItems >= 2; use single picker when only 1 slot left
    val multiLauncher = rememberLauncherForActivityResult(
        PickMultipleVisualMedia(maxItems = if (remaining >= 2) remaining else 2)
    ) { picked -> if (picked.isNotEmpty()) onPhotosSelected(picked) }

    val singleLauncher = rememberLauncherForActivityResult(
        PickVisualMedia()
    ) { uri -> uri?.let { onPhotosSelected(listOf(it)) } }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(3) { index ->
            val uri = uris.getOrNull(index)
            when {
                uri != null -> FilledPhotoSlot(
                    uri = uri,
                    onRemove = { onRemove(uri) },
                    modifier = Modifier.weight(1f),
                )

                index == uris.size -> AddPhotoSlot(
                    onClick = {
                        val req = PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                        if (remaining >= 2) multiLauncher.launch(req)
                        else singleLauncher.launch(req)
                    },
                    modifier = Modifier.weight(1f),
                )

                else -> GhostPhotoSlot(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FilledPhotoSlot(
    uri: Uri,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .shadow(4.dp, RoundedCornerShape(12.dp)),
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // Remove button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(5.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove photo",
                tint = Color.White,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

@Composable
private fun AddPhotoSlot(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(WarmGray50)
            .border(
                width = 1.dp,
                color = WarmGray200,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = WarmGray400,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = "Add photo",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = WarmGray400,
            )
        }
    }
}

@Composable
private fun GhostPhotoSlot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(WarmGray50.copy(alpha = 0.5f))
            .border(
                width = 0.5.dp,
                color = WarmGray100,
                shape = RoundedCornerShape(12.dp),
            ),
    )
}

// ── Memory preview dialog ─────────────────────────────────────────────────────

@Composable
private fun MemoryPreviewDialog(
    message: String,
    senderName: String,
    previewPhotoUris: List<Uri>,
    onDismiss: () -> Unit,
    onPost: () -> Unit,
    canPost: Boolean,
) {
    val previewPost = GuestbookPost(
        message = message,
        senderName = senderName,
        timestamp = System.currentTimeMillis(),
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(onClick = onDismiss),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Preview label
                Text(
                    text = "PREVIEW",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 3.sp),
                    color = Color.White.copy(alpha = 0.55f),
                )
                Spacer(Modifier.height(Spacing.sm))

                // Actual card (same composable as displayed in the book)
                Box(modifier = Modifier.clickable { /* consume click so it doesn't dismiss */ }) {
                    MemoryEntryCard(
                        post = previewPost,
                        pageIndex = 1,
                        totalPages = 1,
                        previewPhotoUris = previewPhotoUris,
                        modifier = Modifier
                            .padding(horizontal = Spacing.screenHorizontal)
                            .fillMaxWidth()
                            .height(500.dp),
                    )
                }

                Spacer(Modifier.height(Spacing.lg))

                // Action row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Edit
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                        )
                    }
                    // Post
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (canPost) Gold else WarmGray400)
                            .clickable(enabled = canPost, onClick = onPost)
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = "Seal & Sign",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

// ── Page stack (depth illusion) ───────────────────────────────────────────────

@Composable
private fun PageStack(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 7.dp, y = 4.dp)
                .background(Champagne.copy(alpha = 0.55f), bookShape),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 4.dp, y = 2.dp)
                .background(ChampagneLight.copy(alpha = 0.75f), bookShape),
        )
    }
}

// ── Shared book shape ─────────────────────────────────────────────────────────

private val bookShape = RoundedCornerShape(
    topStart = 4.dp,
    topEnd = 14.dp,
    bottomStart = 4.dp,
    bottomEnd = 14.dp,
)

// ── Spine shadow ──────────────────────────────────────────────────────────────

@Composable
private fun SpineShadow() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(18.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Black.copy(alpha = 0.08f), Color.Transparent)
                )
            )
    )
}

// ── Paper texture ─────────────────────────────────────────────────────────────

@Composable
private fun PaperTexture(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val lineColor = WarmGray300.copy(alpha = 0.06f)
        val spacing   = 24.dp.toPx()
        var y = 88.dp.toPx()
        while (y < size.height - 56.dp.toPx()) {
            drawLine(lineColor, Offset(36.dp.toPx(), y), Offset(size.width - 36.dp.toPx(), y), strokeWidth = 0.7f)
            y += spacing
        }
    }
}

// ── Corner flourish ───────────────────────────────────────────────────────────

@Composable
private fun CornerFlourish(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val color  = Gold.copy(alpha = 0.22f)
        val stroke = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
        val pad    = 22.dp.toPx()
        val arm    = 30.dp.toPx()

        val tlPath = Path().apply { moveTo(pad, pad + arm); quadraticTo(pad, pad, pad + arm, pad) }
        drawPath(tlPath, color, style = stroke)

        val trPath = Path().apply {
            moveTo(size.width - pad - arm, pad)
            quadraticTo(size.width - pad, pad, size.width - pad, pad + arm)
        }
        drawPath(trPath, color, style = stroke)

        val blPath = Path().apply {
            moveTo(pad, size.height - pad - arm)
            quadraticTo(pad, size.height - pad, pad + arm, size.height - pad)
        }
        drawPath(blPath, color, style = stroke)

        val brPath = Path().apply {
            moveTo(size.width - pad - arm, size.height - pad)
            quadraticTo(size.width - pad, size.height - pad, size.width - pad, size.height - pad - arm)
        }
        drawPath(brPath, color, style = stroke)
    }
}

// ── Cover border ──────────────────────────────────────────────────────────────

@Composable
private fun CoverBorder(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val m1     = 22.dp.toPx()
        val color  = Gold.copy(alpha = 0.18f)
        val stroke = Stroke(width = 0.8.dp.toPx())
        drawRect(color, topLeft = Offset(m1, m1), size = Size(size.width - m1 * 2, size.height - m1 * 2), style = stroke)
        val m2 = 27.dp.toPx()
        drawRect(color.copy(alpha = 0.09f), topLeft = Offset(m2, m2), size = Size(size.width - m2 * 2, size.height - m2 * 2), style = stroke)
    }
}

// ── Gold divider ──────────────────────────────────────────────────────────────

@Composable
fun GoldDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, Gold.copy(alpha = 0.45f), Color.Transparent)
                )
            ),
    )
}

// ── Heart divider ─────────────────────────────────────────────────────────────

@Composable
private fun HeartDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(Modifier
            .width(44.dp)
            .height(0.5.dp)
            .background(WarmGray300.copy(alpha = 0.5f)))
        Spacer(Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = BlushDeep.copy(alpha = 0.4f),
            modifier = Modifier.size(10.dp),
        )
        Spacer(Modifier.width(6.dp))
        Box(Modifier
            .width(44.dp)
            .height(0.5.dp)
            .background(WarmGray300.copy(alpha = 0.5f)))
    }
}

// ── Page indicator ────────────────────────────────────────────────────────────

@Composable
private fun PageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val visibleCount = totalPages.coerceAtMost(9)
        repeat(visibleCount) { i ->
            val isSelected = i == currentPage
            val dotSize by animateFloatAsState(
                targetValue = if (isSelected) 8f else 4.5f,
                animationSpec = tween(200),
                label = "dot_size",
            )
            val dotAlpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.3f,
                animationSpec = tween(200),
                label = "dot_alpha",
            )
            Box(
                modifier = Modifier
                    .size(dotSize.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = dotAlpha)),
            )
            if (i < visibleCount - 1) Spacer(Modifier.width(5.dp))
        }
        if (totalPages > 9) {
            Spacer(Modifier.width(6.dp))
            Text(
                text  = "${currentPage + 1} / $totalPages",
                style = MaterialTheme.typography.labelSmall,
                color = WarmGray300,
            )
        }
    }
}

// ── Screen floral blobs ───────────────────────────────────────────────────────

@Composable
private fun BookroomFlorals() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(Color(0xFFEDD9B8).copy(alpha = 0.40f), 100.dp.toPx(), Offset(0.dp.toPx(),   20.dp.toPx()))
        drawCircle(Color(0xFFEAB8BC).copy(alpha = 0.15f), 70.dp.toPx(),  Offset(80.dp.toPx(),  90.dp.toPx()))
        drawCircle(Color(0xFFF5E6C8).copy(alpha = 0.45f), 75.dp.toPx(),  Offset(-20.dp.toPx(), 75.dp.toPx()))
        val bx = size.width
        val by = size.height
        drawCircle(
            Color(0xFFEDD9B8).copy(alpha = 0.35f),
            90.dp.toPx(),
            Offset(bx, by - 40.dp.toPx())
        )
        drawCircle(Color(0xFFF5E6C8).copy(alpha = 0.40f), 65.dp.toPx(),  Offset(bx - 70.dp.toPx(), by - 90.dp.toPx()))
        drawCircle(Color(0xFFEAB8BC).copy(alpha = 0.15f), 55.dp.toPx(),  Offset(bx + 10.dp.toPx(), by - 130.dp.toPx()))
    }
}

// ── Delete post dialog ────────────────────────────────────────────────────────

@Composable
private fun DeletePostDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                "Delete message?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = WarmGray800,
            )
        },
        text = {
            Text(
                "This memory will be permanently removed.",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmGray400,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = ErrorRose, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = WarmGray400)
            }
        },
    )
}

// ── Edit post sheet ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPostSheet(
    initialMessage: String,
    existingPhotoUrls: List<String>,
    newPhotoUris: List<Uri>,
    onSave: () -> Unit,
    onMessageChange: (String) -> Unit,
    onRemoveExistingPhoto: (String) -> Unit,
    onAddPhotos: (List<Uri>) -> Unit,
    onRemoveNewPhoto: (Uri) -> Unit,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Ivory,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(WarmGray200),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(bottom = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = "Edit Memory",
                style = TextStyle(
                    fontFamily = DancingScript,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 30.sp,
                    color = WarmGray800,
                ),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Spacing.lg))
            GoldDivider(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Spacing.lg))

            // ── Photo row (existing URLs + new URIs) ──────────────────────────
            EditPhotoRow(
                existingUrls = existingPhotoUrls,
                newUris = newPhotoUris,
                onRemoveExisting = onRemoveExistingPhoto,
                onAddPhotos = onAddPhotos,
                onRemoveNew = onRemoveNewPhoto,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Add up to 3 photos  (optional)",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
                color = WarmGray300,
            )

            Spacer(Modifier.height(Spacing.lg))

            // ── Message field ─────────────────────────────────────────────────
            OutlinedTextField(
                value = initialMessage,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 120.dp),
                placeholder = { Text("Your message…", color = WarmGray300) },
                shape = RoundedCornerShape(14.dp),
                enabled = !isSubmitting,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = WarmGray200,
                    cursorColor = Gold,
                ),
            )

            Spacer(Modifier.height(Spacing.lg))

            // ── Save / loading ────────────────────────────────────────────────
            if (isSubmitting) {
                CircularProgressIndicator(
                    color = Gold,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(44.dp),
                )
            } else {
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        contentColor = Color.White
                    ),
                ) {
                    Text("Save Changes", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

// ── Edit photo row (existing URLs + new URIs) ─────────────────────────────────

@Composable
private fun EditPhotoRow(
    existingUrls: List<String>,
    newUris: List<Uri>,
    onRemoveExisting: (String) -> Unit,
    onAddPhotos: (List<Uri>) -> Unit,
    onRemoveNew: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalCount = existingUrls.size + newUris.size
    val remaining = 3 - totalCount

    val multiLauncher = rememberLauncherForActivityResult(
        PickMultipleVisualMedia(maxItems = if (remaining >= 2) remaining else 2)
    ) { picked -> if (picked.isNotEmpty()) onAddPhotos(picked) }

    val singleLauncher = rememberLauncherForActivityResult(
        PickVisualMedia()
    ) { uri -> uri?.let { onAddPhotos(listOf(it)) } }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(3) { index ->
            when {
                // Slot occupied by an existing (URL) photo
                index < existingUrls.size -> FilledUrlPhotoSlot(
                    url = existingUrls[index],
                    onRemove = { onRemoveExisting(existingUrls[index]) },
                    modifier = Modifier.weight(1f),
                )
                // Slot occupied by a newly added (URI) photo
                index < totalCount -> {
                    val uriIndex = index - existingUrls.size
                    FilledPhotoSlot(
                        uri = newUris[uriIndex],
                        onRemove = { onRemoveNew(newUris[uriIndex]) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Next empty slot → "Add photo" button
                index == totalCount && totalCount < 3 -> AddPhotoSlot(
                    onClick = {
                        val req = PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                        if (remaining >= 2) multiLauncher.launch(req) else singleLauncher.launch(req)
                    },
                    modifier = Modifier.weight(1f),
                )
                // Remaining empty slots → ghost placeholders
                else -> GhostPhotoSlot(modifier = Modifier.weight(1f))
            }
        }
    }
}

// ── Filled slot for a remote URL photo ───────────────────────────────────────

@Composable
private fun FilledUrlPhotoSlot(
    url: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .shadow(4.dp, RoundedCornerShape(12.dp)),
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(5.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove photo",
                tint = Color.White,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatPageDate(millis: Long): String {
    if (millis == 0L) return ""
    return SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH).format(Date(millis))
}
