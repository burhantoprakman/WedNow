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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.wednowapp.wednow.ui.theme.ChampagneLight
import com.wednowapp.wednow.ui.theme.Gold
import com.wednowapp.wednow.ui.theme.Ivory
import com.wednowapp.wednow.ui.theme.Spacing
import com.wednowapp.wednow.ui.theme.WarmGray300
import com.wednowapp.wednow.ui.theme.WarmGray400
import com.wednowapp.wednow.ui.theme.WarmGray50
import com.wednowapp.wednow.ui.theme.WarmGray700
import com.wednowapp.wednow.ui.theme.WarmGray800
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun PhotosScreen(
    onBack: () -> Unit,
    viewModel: PhotosViewModel = hiltViewModel(),
) {
    val photos      by viewModel.photos.collectAsStateWithLifecycle()
    val uploadState by viewModel.uploadState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    val photoPicker =
        rememberLauncherForActivityResult(PickMultipleVisualMedia(maxItems = 10)) { uris ->
            if (uris.isNotEmpty()) viewModel.uploadMultiple(uris)
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

    var fullscreenPhoto by remember { mutableStateOf<WeddingPhoto?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ivory),
    ) {
        GalleryFeed(
            photos = photos,
            currentGuestId = viewModel.currentGuestId,
            onBack = onBack,
            onToggleLike = viewModel::toggleLike,
            onPhotoTap = { fullscreenPhoto = it },
        )

        AddMemoryButton(
            uploadState = uploadState,
            onClick = { photoPicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
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
        FullscreenViewer(photo = photo, onDismiss = { fullscreenPhoto = null })
    }
}

// ── Feed ──────────────────────────────────────────────────────────────────────

@Composable
private fun GalleryFeed(
    photos: List<WeddingPhoto>?,
    currentGuestId: String,
    onBack: () -> Unit,
    onToggleLike: (photoId: String, isCurrentlyLiked: Boolean) -> Unit,
    onPhotoTap: (WeddingPhoto) -> Unit,
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

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
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
private fun FullscreenViewer(photo: WeddingPhoto, onDismiss: () -> Unit) {
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
