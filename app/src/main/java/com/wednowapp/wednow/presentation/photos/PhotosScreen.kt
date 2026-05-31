package com.wednowapp.wednow.presentation.photos

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wednowapp.wednow.domain.model.WeddingPhoto
import com.wednowapp.wednow.presentation.auth.LocalAuthViewModel
import com.wednowapp.wednow.presentation.auth.SignInBottomSheet
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
import com.wednowapp.wednow.ui.theme.WarmGray700
import com.wednowapp.wednow.ui.theme.WarmGray800
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Entry point ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosScreen(
    onBack: () -> Unit,
    viewModel: PhotosViewModel = hiltViewModel(),
) {
    val authViewModel = LocalAuthViewModel.current
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val myPhotos by viewModel.myPhotos.collectAsStateWithLifecycle()
    val uploadState by viewModel.uploadState.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var showSignIn by remember { mutableStateOf(false) }

    val photoPicker =
        rememberLauncherForActivityResult(PickMultipleVisualMedia(maxItems = 10)) { uris ->
            if (uris.isNotEmpty()) viewModel.uploadMultiple(uris)
        }

    fun requestUpload() {
        if (authViewModel.isSignedIn) {
            photoPicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        } else {
            showSignIn = true
        }
    }

    LaunchedEffect(uploadState) {
        when (val s = uploadState) {
            is UploadState.Success -> {
                val msg = if (s.count == 1) "Memory shared ❆" else "${s.count} memories shared ❆"
                snackbar.showSnackbar(msg)
                viewModel.resetUploadState()
            }
            is UploadState.Error -> {
                snackbar.showSnackbar(s.message); viewModel.resetUploadState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(deleteState) {
        if (deleteState?.isSuccess == true) {
            snackbar.showSnackbar("Photo deleted")
            viewModel.clearDeleteState()
        } else if (deleteState?.isFailure == true) {
            snackbar.showSnackbar("Failed to delete photo")
            viewModel.clearDeleteState()
        }
    }

    var fullscreenPhoto by remember { mutableStateOf<WeddingPhoto?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ivory),
    ) {
        GalleryFeed(
            photos = photos,
            myPhotos = myPhotos,
            currentGuestId = viewModel.currentGuestId,
            onBack = onBack,
            onToggleLike = viewModel::toggleLike,
            onPhotoTap = { fullscreenPhoto = it },
            canEdit = viewModel::canEdit,
            canDelete = viewModel::canDelete,
            isOwned = viewModel::isOwned,
            onEdit = viewModel::openEditCaption,
            onDelete = viewModel::requestDelete,
        )

        AddMemoryButton(
            uploadState = uploadState,
            onClick = ::requestUpload,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = Spacing.xl),
        )

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 100.dp),
        )
    }

    fullscreenPhoto?.let { photo ->
        FullscreenViewer(
            photo = photo,
            canEdit = viewModel.canEdit(photo),
            canDelete = viewModel.canDelete(photo),
            isOwned = viewModel.isOwned(photo),
            onEdit = { viewModel.openEditCaption(photo); fullscreenPhoto = null },
            onDelete = { viewModel.requestDelete(photo); fullscreenPhoto = null },
            onDismiss = { fullscreenPhoto = null },
        )
    }

    // Delete confirmation dialog
    viewModel.pendingDeletePhoto?.let { photo ->
        DeletePhotoDialog(
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete,
        )
    }

    // Edit caption sheet
    viewModel.editCaptionTarget?.let { photo ->
        EditCaptionSheet(
            initialCaption = photo.caption,
            onSave = viewModel::saveCaption,
            onDismiss = viewModel::dismissEditCaption,
        )
    }

    if (showSignIn) {
        SignInBottomSheet(
            authViewModel = authViewModel,
            reason = "Sign in to share photos with everyone.",
            onDismiss = { showSignIn = false; authViewModel.clearError() },
            onSuccess = {
                showSignIn = false
                photoPicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            },
        )
    }
}

// ── Feed ──────────────────────────────────────────────────────────────────────

@Composable
private fun GalleryFeed(
    photos: List<WeddingPhoto>?,
    myPhotos: List<WeddingPhoto>,
    currentGuestId: String,
    onBack: () -> Unit,
    onToggleLike: (photoId: String, isCurrentlyLiked: Boolean) -> Unit,
    onPhotoTap: (WeddingPhoto) -> Unit,
    canEdit: (WeddingPhoto) -> Boolean,
    canDelete: (WeddingPhoto) -> Boolean,
    isOwned: (WeddingPhoto) -> Boolean,
    onEdit: (WeddingPhoto) -> Unit,
    onDelete: (WeddingPhoto) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            bottom = 140.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            GalleryHeader(
                onBack = onBack,
                photoCount = photos?.size ?: 0,
            )
        }

        // ── My Photos strip (only when signed-in user has uploads) ───────────────
        if (myPhotos.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                MyPhotosStrip(
                    photos = myPhotos,
                    currentGuestId = currentGuestId,
                    onToggleLike = onToggleLike,
                    onPhotoTap = onPhotoTap,
                    canEdit = canEdit,
                    canDelete = canDelete,
                    onEdit = onEdit,
                    onDelete = onDelete,
                )
            }
        }

        when {
            photos == null -> item(span = { GridItemSpan(maxLineSpan) }) {
                LoadingShimmer()
            }

            photos.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyGalleryState()
            }

            else -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    val photo = photos.first()
                    val isLiked = photo.likedBy.contains(currentGuestId)
                    FeaturedMemoryCard(
                        photo = photo,
                        isLiked = isLiked,
                        onLike = { onToggleLike(photo.id, isLiked) },
                        onTap = { onPhotoTap(photo) },
                        canEdit = canEdit(photo),
                        canDelete = canDelete(photo),
                        isOwned = isOwned(photo),
                        onEdit = { onEdit(photo) },
                        onDelete = { onDelete(photo) },
                    )
                }

                itemsIndexed(
                    items = photos.drop(1),
                    key = { _, photo -> photo.id },
                ) { _, photo ->
                    val isLiked = photo.likedBy.contains(currentGuestId)
                    MemoryCard(
                        photo = photo,
                        isLiked = isLiked,
                        onLike = { onToggleLike(photo.id, isLiked) },
                        onTap = { onPhotoTap(photo) },
                        canEdit = canEdit(photo),
                        canDelete = canDelete(photo),
                        isOwned = isOwned(photo),
                        onEdit = { onEdit(photo) },
                        onDelete = { onDelete(photo) },
                    )
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun GalleryHeader(onBack: () -> Unit, photoCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(vertical = Spacing.md),
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
                tint = WarmGray700,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(Modifier.height(Spacing.lg))

        Text(
            text = "Captured Memories",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Light,
                letterSpacing = (-0.5).sp,
            ),
            color = WarmGray800,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Moments shared with love",
            style = MaterialTheme.typography.bodyMedium,
            color = WarmGray400,
        )

        if (photoCount > 0) {
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = "$photoCount ${if (photoCount == 1) "memory" else "memories"}",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                color = Gold,
            )
        }

        Spacer(Modifier.height(Spacing.md))
    }
}

// ── Featured card ─────────────────────────────────────────────────────────────

@Composable
private fun FeaturedMemoryCard(
    photo: WeddingPhoto,
    isLiked: Boolean,
    onLike: () -> Unit,
    onTap: () -> Unit,
    canEdit: Boolean,
    canDelete: Boolean,
    isOwned: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onTap),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photo.imageUrl)
                .crossfade(700)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.45f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.65f),
                        ),
                    )
                )
        )

        // Top-start: FEATURED MEMORY badge + optional "Your Photo" badge
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.28f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(10.dp),
                )
                Text(
                    text = "FEATURED MEMORY",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = Color.White,
                )
            }
            if (isOwned) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Gold.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        "Your Photo",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White,
                    )
                }
            }
        }

        // Top-end: ⋮ menu
        if (canEdit || canDelete) {
            var menuExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .clickable { menuExpanded = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.MoreVert, null, Modifier.size(14.dp), Color.White)
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
                                    "Edit Caption",
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)) {
                if (photo.senderName.isNotBlank()) {
                    Text(
                        text = photo.senderName,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (photo.timestamp > 0L) {
                    Text(
                        text = formatTimeAgo(photo.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f),
                    )
                }
            }
            ElegantLikeButton(
                isLiked = isLiked,
                likeCount = photo.likedBy.size,
                onToggle = onLike,
            )
        }
    }
}

// ── Regular memory card ───────────────────────────────────────────────────────

@Composable
private fun MemoryCard(
    photo: WeddingPhoto,
    isLiked: Boolean,
    onLike: () -> Unit,
    onTap: () -> Unit,
    canEdit: Boolean,
    canDelete: Boolean,
    isOwned: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 5f)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onTap),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photo.imageUrl)
                .crossfade(600)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.55f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.55f),
                        ),
                    )
                )
        )

        // "Yours" badge at top-start
        if (isOwned) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Gold.copy(alpha = 0.85f))
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            ) {
                Text(
                    "Yours",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = Color.White,
                )
            }
        }

        // ⋮ menu at top-end
        if (canEdit || canDelete) {
            var menuExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .clickable { menuExpanded = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.MoreVert, null, Modifier.size(13.dp), Color.White)
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
                                    "Edit Caption",
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 9.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (photo.senderName.isNotBlank()) {
                Text(
                    text = photo.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
            ElegantLikeButton(
                isLiked = isLiked,
                likeCount = photo.likedBy.size,
                onToggle = onLike,
                compact = true,
            )
        }
    }
}

// ── My Photos strip ───────────────────────────────────────────────────────────

@Composable
private fun MyPhotosStrip(
    photos: List<WeddingPhoto>,
    currentGuestId: String,
    onToggleLike: (photoId: String, isCurrentlyLiked: Boolean) -> Unit,
    onPhotoTap: (WeddingPhoto) -> Unit,
    canEdit: (WeddingPhoto) -> Boolean,
    canDelete: (WeddingPhoto) -> Boolean,
    onEdit: (WeddingPhoto) -> Unit,
    onDelete: (WeddingPhoto) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Gold),
                )
                Text(
                    text = "My Photos",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = WarmGray800,
                )
            }
            Text(
                text = "${photos.size} ${if (photos.size == 1) "photo" else "photos"}",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.5.sp,
                ),
                color = Gold,
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(photos, key = { it.id }) { photo ->
                val isLiked = photo.likedBy.contains(currentGuestId)
                MyPhotoTile(
                    photo = photo,
                    isLiked = isLiked,
                    onLike = { onToggleLike(photo.id, isLiked) },
                    onTap = { onPhotoTap(photo) },
                    canEdit = canEdit(photo),
                    canDelete = canDelete(photo),
                    onEdit = { onEdit(photo) },
                    onDelete = { onDelete(photo) },
                )
            }
        }

        Spacer(Modifier.height(Spacing.lg))
        // Subtle divider before "All Memories" grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, WarmGray200, Color.Transparent)
                    )
                ),
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = "ALL MEMORIES",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            color = WarmGray400,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Spacing.sm))
    }
}

@Composable
private fun MyPhotoTile(
    photo: WeddingPhoto,
    isLiked: Boolean,
    onLike: () -> Unit,
    onTap: () -> Unit,
    canEdit: Boolean,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(112.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, Gold.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .clickable(onClick = onTap),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photo.imageUrl)
                .crossfade(500)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // Gradient vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.5f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.5f),
                        ),
                    )
                ),
        )
        // Like count at bottom-start
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 6.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = if (isLiked) Color(0xFFEA8A8A) else Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(11.dp)
                    .clickable(onClick = onLike),
            )
            if (photo.likedBy.isNotEmpty()) {
                Text(
                    text = photo.likedBy.size.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }
        // ⋮ menu at top-end
        if (canEdit || canDelete) {
            var menuExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { menuExpanded = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.MoreVert, null, Modifier.size(11.dp), Color.White)
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
                                    "Edit Caption",
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

// ── Like button ───────────────────────────────────────────────────────────────

@Composable
private fun ElegantLikeButton(
    isLiked: Boolean,
    likeCount: Int,
    onToggle: () -> Unit,
    compact: Boolean = false,
) {
    val scale by animateFloatAsState(
        targetValue = if (isLiked) 1.35f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "like_scale",
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .pointerInput(isLiked) { detectTapGestures { onToggle() } }
            .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isLiked) "Unlike" else "Like",
            tint = if (isLiked) Color(0xFFEA8A8A) else Color.White.copy(alpha = 0.85f),
            modifier = Modifier
                .size(if (compact) 14.dp else 17.dp)
                .scale(scale),
        )
        if (likeCount > 0) {
            Text(
                text = likeCount.toString(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = if (compact) 10.sp else 12.sp,
                ),
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

// ── Add Memory floating button ────────────────────────────────────────────────

@Composable
private fun AddMemoryButton(
    uploadState: UploadState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isUploading = uploadState is UploadState.Loading
    val label = when {
        uploadState is UploadState.Loading && uploadState.total > 1 ->
            "Uploading ${uploadState.current}/${uploadState.total}…"

        isUploading -> "Uploading…"
        else -> "Add Memory"
    }

    Surface(
        onClick = { if (!isUploading) onClick() },
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        color = if (isUploading) WarmGray300 else Gold,
        shadowElevation = 14.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.5.sp),
                color = Color.White,
            )
        }
    }
}

// ── Loading shimmer ───────────────────────────────────────────────────────────

@Composable
private fun LoadingShimmer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
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
                text = "Loading memories…",
                style = MaterialTheme.typography.bodySmall,
                color = WarmGray400,
            )
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyGalleryState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xxl, horizontal = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(ChampagneLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                tint = Gold.copy(alpha = 0.6f),
                modifier = Modifier.size(36.dp),
            )
        }

        Spacer(Modifier.height(Spacing.sm))

        Text(
            text = "No memories yet",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Light),
            color = WarmGray700,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "Be the first to capture\na beautiful moment",
            style = MaterialTheme.typography.bodyMedium,
            color = WarmGray400,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Fullscreen viewer ─────────────────────────────────────────────────────────

@Composable
private fun FullscreenViewer(
    photo: WeddingPhoto,
    canEdit: Boolean,
    canDelete: Boolean,
    isOwned: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photo.imageUrl)
                    .crossfade(400)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDismiss),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(Spacing.md)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            // ⋮ menu at top-end
            if (canEdit || canDelete) {
                var menuExpanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(Spacing.md)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .clickable { menuExpanded = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.MoreVert, null, Modifier.size(20.dp), Color.White)
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
                                        "Edit Caption",
                                        style = MaterialTheme.typography.bodySmall
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

            if (photo.senderName.isNotBlank() || photo.timestamp > 0L) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                            )
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                ) {
                    if (photo.senderName.isNotBlank()) {
                        Text(
                            text = photo.senderName,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                        )
                    }
                    if (photo.timestamp > 0L) {
                        Text(
                            text = formatTimeAgo(photo.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.65f),
                        )
                    }
                }
            }
        }
    }
}

// ── Delete confirmation dialog ────────────────────────────────────────────────

@Composable
private fun DeletePhotoDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                "Delete photo?",
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

// ── Edit caption sheet ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditCaptionSheet(
    initialCaption: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var caption by remember { mutableStateOf(initialCaption) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Ivory,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(bottom = Spacing.xl),
        ) {
            Text(
                "Edit Caption",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = WarmGray800,
            )
            Spacer(Modifier.height(Spacing.md))
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add a caption…", color = WarmGray300) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = WarmGray200,
                    cursorColor = Gold,
                ),
                maxLines = 3,
            )
            Spacer(Modifier.height(Spacing.lg))
            Button(
                onClick = { onSave(caption) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = Color.White
                ),
            ) {
                Text("Save Caption", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ── Helper ────────────────────────────────────────────────────────────────────

private fun formatTimeAgo(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000L -> "Just now"
        diff < 3_600_000L -> "${diff / 60_000}m ago"
        diff < 86_400_000L -> "${diff / 3_600_000}h ago"
        diff < 604_800_000L -> "${diff / 86_400_000}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
