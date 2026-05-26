package com.wednowapp.wednow.presentation.photos

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wednowapp.wednow.domain.model.WeddingPhoto
import com.wednowapp.wednow.ui.theme.BlushDeep
import com.wednowapp.wednow.ui.theme.BlushLight
import com.wednowapp.wednow.ui.theme.ChampagneLight
import com.wednowapp.wednow.ui.theme.Gold
import com.wednowapp.wednow.ui.theme.Ivory
import com.wednowapp.wednow.ui.theme.Spacing
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Layout model ──────────────────────────────────────────────────────────────

private sealed class PhotoDisplayItem {
    data class Hero(val photo: WeddingPhoto) : PhotoDisplayItem()
    data class Single(val photo: WeddingPhoto) : PhotoDisplayItem()
    data class Duo(val left: WeddingPhoto, val right: WeddingPhoto, val leftWider: Boolean) : PhotoDisplayItem()
}

private fun buildDisplayItems(photos: List<WeddingPhoto>): List<PhotoDisplayItem> {
    if (photos.isEmpty()) return emptyList()
    val result = mutableListOf<PhotoDisplayItem>()
    result += PhotoDisplayItem.Hero(photos[0])
    val rest = photos.drop(1)
    var i = 0
    var leftWider = true
    while (i < rest.size) {
        // single full-width
        result += PhotoDisplayItem.Single(rest[i++])
        // pair (if available)
        if (i < rest.size) {
            val a = rest[i++]
            if (i < rest.size) {
                result += PhotoDisplayItem.Duo(a, rest[i++], leftWider)
                leftWider = !leftWider
            } else {
                result += PhotoDisplayItem.Single(a)
            }
        }
    }
    return result
}

private fun keyFor(item: PhotoDisplayItem): String = when (item) {
    is PhotoDisplayItem.Hero   -> "hero_${item.photo.id}"
    is PhotoDisplayItem.Single -> "single_${item.photo.id}"
    is PhotoDisplayItem.Duo    -> "duo_${item.left.id}"
}

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun PhotosScreen(
    onBack: () -> Unit,
    viewModel: PhotosViewModel = hiltViewModel(),
) {
    val photos      by viewModel.photos.collectAsStateWithLifecycle()
    val uploadState by viewModel.uploadState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    val photoPicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        uri?.let { viewModel.upload(it) }
    }

    LaunchedEffect(uploadState) {
        when (val s = uploadState) {
            is UploadState.Success -> { snackbarHostState.showSnackbar("Moment shared ✦"); viewModel.resetUploadState() }
            is UploadState.Error   -> { snackbarHostState.showSnackbar(s.message);         viewModel.resetUploadState() }
            else                   -> Unit
        }
    }

    PhotosContent(
        photos       = photos,
        isUploading  = uploadState == UploadState.Loading,
        onBack       = onBack,
        onPickPhoto  = { photoPicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
        snackbarHost = snackbarHostState,
    )
}

// ── Main content ──────────────────────────────────────────────────────────────

@Composable
private fun PhotosContent(
    photos: List<WeddingPhoto>,
    isUploading: Boolean,
    onBack: () -> Unit,
    onPickPhoto: () -> Unit,
    snackbarHost: SnackbarHostState,
) {
    val likedPhotos = remember { mutableStateMapOf<String, Boolean>() }
    var fullscreenPhoto by remember { mutableStateOf<WeddingPhoto?>(null) }

    val displayItems = remember(photos) { buildDisplayItems(photos) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colors = listOf(ChampagneLight.copy(alpha = 0.4f), Ivory, Ivory))
            ),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 120.dp),
        ) {
            // Top bar
            item {
                MemoriesTopBar(onBack = onBack, photoCount = photos.size)
            }

            if (photos.isEmpty()) {
                item { ElegantEmptyState() }
            } else {
                items(displayItems, key = { keyFor(it) }) { item ->
                    val cardPad = Modifier.padding(
                        horizontal = Spacing.screenHorizontal,
                        vertical = Spacing.xs,
                    )
                    when (item) {
                        is PhotoDisplayItem.Hero -> {
                            Spacer(Modifier.height(Spacing.sm))
                            HeroPhotoSection(
                                photo = item.photo,
                                isLiked = likedPhotos[item.photo.id] ?: false,
                                onLike = { likedPhotos[item.photo.id] = !(likedPhotos[item.photo.id] ?: false) },
                                onTap = { fullscreenPhoto = item.photo },
                            )
                        }

                        is PhotoDisplayItem.Single -> {
                            MemoryPhotoCard(
                                photo = item.photo,
                                modifier = cardPad.fillMaxWidth().aspectRatio(16f / 10f),
                                cornerRadius = 20.dp,
                                isLiked = likedPhotos[item.photo.id] ?: false,
                                onLike = { likedPhotos[item.photo.id] = !(likedPhotos[item.photo.id] ?: false) },
                                onTap = { fullscreenPhoto = item.photo },
                            )
                        }

                        is PhotoDisplayItem.Duo -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            ) {
                                val leftWeight  = if (item.leftWider) 0.58f else 0.42f
                                val rightWeight = if (item.leftWider) 0.42f else 0.58f
                                MemoryPhotoCard(
                                    photo = item.left,
                                    modifier = Modifier.weight(leftWeight).fillMaxHeight(),
                                    cornerRadius = 20.dp,
                                    isLiked = likedPhotos[item.left.id] ?: false,
                                    onLike = { likedPhotos[item.left.id] = !(likedPhotos[item.left.id] ?: false) },
                                    onTap = { fullscreenPhoto = item.left },
                                )
                                MemoryPhotoCard(
                                    photo = item.right,
                                    modifier = Modifier.weight(rightWeight).fillMaxHeight(),
                                    cornerRadius = 20.dp,
                                    isLiked = likedPhotos[item.right.id] ?: false,
                                    onLike = { likedPhotos[item.right.id] = !(likedPhotos[item.right.id] ?: false) },
                                    onTap = { fullscreenPhoto = item.right },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Upload button — fixed bottom-right
        ElegantUploadButton(
            isUploading = isUploading,
            onClick = onPickPhoto,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = Spacing.screenHorizontal, bottom = Spacing.lg),
        )

        // Snackbar
        SnackbarHost(
            hostState = snackbarHost,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 80.dp),
        )
    }

    // Fullscreen viewer
    fullscreenPhoto?.let { photo ->
        FullscreenPhotoDialog(
            photo = photo,
            onDismiss = { fullscreenPhoto = null },
        )
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun MemoriesTopBar(onBack: () -> Unit, photoCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Back button
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
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            )
        }
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Captured Memories",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (photoCount > 0) {
                Text(
                    text = "$photoCount beautiful moments",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                )
            }
        }
    }
}

// ── Hero photo section ────────────────────────────────────────────────────────

@Composable
private fun HeroPhotoSection(
    photo: WeddingPhoto,
    isLiked: Boolean,
    onLike: () -> Unit,
    onTap: () -> Unit,
) {
    val likeScale by animateFloatAsState(
        targetValue = if (isLiked) 1.3f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "hero_like_scale",
    )
    var showHeart by remember { mutableStateOf(false) }
    val heartAlpha = remember { Animatable(0f) }
    val heartOffset = remember { Animatable(0f) }

    LaunchedEffect(showHeart) {
        if (showHeart) {
            heartAlpha.snapTo(0.9f)
            heartOffset.snapTo(0f)
            launch { heartAlpha.animateTo(0f, tween(900, easing = FastOutLinearInEasing)) }
            heartOffset.animateTo(-80f, tween(900))
            showHeart = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal)
            .aspectRatio(3f / 4f)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onTap),
    ) {
        AsyncImage(
            model = photo.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                        startY = 400f,
                    )
                )
        )

        // Top badge
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.25f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = "FEATURED MEMORY",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = Color.White,
            )
        }

        // Floating heart animation
        if (heartAlpha.value > 0f) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.White.copy(alpha = heartAlpha.value),
                modifier = Modifier
                    .size(52.dp)
                    .align(Alignment.Center)
                    .offset(y = heartOffset.value.dp),
            )
        }

        // Bottom overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    text = "Captured with love",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color = Color.White.copy(alpha = 0.7f),
                )
                if (photo.uploadedBy.isNotBlank()) {
                    Text(
                        text = photo.uploadedBy,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                    )
                }
            }
            // Like button (consumes own touch via pointerInput)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
                    .pointerInput(isLiked) {
                        detectTapGestures {
                            onLike()
                            if (!isLiked) showHeart = true
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color(0xFFE88888) else Color.White,
                        modifier = Modifier.size(18.dp).scale(likeScale),
                    )
                    Text(
                        text = if (isLiked) "Loved" else "Love",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

// ── Memory photo card ─────────────────────────────────────────────────────────

@Composable
private fun MemoryPhotoCard(
    photo: WeddingPhoto,
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 20.dp,
    isLiked: Boolean,
    onLike: () -> Unit,
    onTap: () -> Unit,
) {
    val likeScale by animateFloatAsState(
        targetValue = if (isLiked) 1.3f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "like_scale",
    )
    var showHeart by remember { mutableStateOf(false) }
    val heartAlpha = remember { Animatable(0f) }
    val heartOffset = remember { Animatable(0f) }

    LaunchedEffect(showHeart) {
        if (showHeart) {
            heartAlpha.snapTo(0.9f)
            heartOffset.snapTo(0f)
            launch { heartAlpha.animateTo(0f, tween(750, easing = FastOutLinearInEasing)) }
            heartOffset.animateTo(-60f, tween(750))
            showHeart = false
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(onClick = onTap),
    ) {
        AsyncImage(
            model = photo.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                        startY = 200f,
                    )
                )
        )

        // Floating heart
        if (heartAlpha.value > 0f) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.White.copy(alpha = heartAlpha.value),
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.Center)
                    .offset(y = heartOffset.value.dp),
            )
        }

        // Bottom row: uploader + like
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            // Uploader info
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                if (photo.uploadedBy.isNotBlank()) {
                    Text(
                        text = photo.uploadedBy,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        maxLines = 1,
                    )
                }
                if (photo.timestamp > 0L) {
                    Text(
                        text = formatTimeAgo(photo.timestamp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White.copy(alpha = 0.65f),
                    )
                }
            }

            // Like button (pointerInput prevents bubbling)
            Box(
                modifier = Modifier
                    .pointerInput(isLiked) {
                        detectTapGestures {
                            onLike()
                            if (!isLiked) showHeart = true
                        }
                    }
                    .padding(4.dp),
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color(0xFFE88888) else Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp).scale(likeScale),
                )
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun ElegantEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(Spacing.xxl))
        Icon(
            imageVector = Icons.Default.PhotoLibrary,
            contentDescription = null,
            tint = BlushDeep.copy(alpha = 0.3f),
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(Spacing.lg))
        Text(
            text = "No memories yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = "Be the first to capture\na beautiful moment",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
        )
    }
}

// ── Upload button ─────────────────────────────────────────────────────────────

@Composable
private fun ElegantUploadButton(
    isUploading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = { if (!isUploading) onClick() },
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Gold),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
                Text(
                    text = "Uploading...",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Add Memory",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
            }
        }
    }
}

// ── Fullscreen viewer ─────────────────────────────────────────────────────────

@Composable
private fun FullscreenPhotoDialog(
    photo: WeddingPhoto,
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
                model = photo.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDismiss),
            )

            // Close button
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(Spacing.md)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
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

            // Bottom info
            if (photo.uploadedBy.isNotBlank() || photo.timestamp > 0L) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                            )
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                ) {
                    if (photo.uploadedBy.isNotBlank()) {
                        Text(
                            text = photo.uploadedBy,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                        )
                    }
                    if (photo.timestamp > 0L) {
                        Text(
                            text = formatTimeAgo(photo.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
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
        diff < 60_000L       -> "Just now"
        diff < 3_600_000L    -> "${diff / 60_000}m ago"
        diff < 86_400_000L   -> "${diff / 3_600_000}h ago"
        diff < 604_800_000L  -> "${diff / 86_400_000}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
