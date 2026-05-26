package com.wednowapp.wednow.presentation.guestbook

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wednowapp.wednow.R
import com.wednowapp.wednow.domain.model.GuestbookPost
import com.wednowapp.wednow.ui.theme.*
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

private val DancingScript = FontFamily(
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
    val posts       by viewModel.posts.collectAsStateWithLifecycle()
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    GuestbookContent(
        posts           = posts,
        submitState     = submitState,
        messageInput    = viewModel.messageInput,
        onMessageChange = viewModel::onMessageChange,
        onSubmit        = viewModel::submit,
        canSubmit       = viewModel.canSubmit,
        onResetSubmit   = viewModel::resetSubmitState,
        onBack          = onBack,
        snackbarHost    = snackbarHost,
    )
}

// ── Content ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuestbookContent(
    posts: List<GuestbookPost>,
    submitState: PostSubmitState,
    messageInput: String,
    onMessageChange: (String) -> Unit,
    onSubmit: () -> Unit,
    canSubmit: Boolean,
    onResetSubmit: () -> Unit,
    onBack: () -> Unit,
    snackbarHost: SnackbarHostState,
) {
    var showWriteSheet by remember { mutableStateOf(false) }
    val pageCount  = posts.size + 1
    val pagerState = rememberPagerState { pageCount }

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
                onBack       = onBack,
                onWrite      = { showWriteSheet = true },
                currentPage  = pagerState.currentPage,
                totalMemories = posts.size,
            )

            PageTurnContainer(
                pagerState = pagerState,
                modifier   = Modifier.weight(1f).fillMaxWidth(),
            ) { page, pageOffset ->
                if (page == 0) {
                    BookCoverPage(
                        isEmpty    = posts.isEmpty(),
                        pageOffset = pageOffset,
                        modifier   = Modifier.fillMaxSize(),
                    )
                } else {
                    GuestbookPage(
                        post       = posts[page - 1],
                        pageIndex  = page,
                        totalPages = posts.size,
                        pageOffset = pageOffset,
                        modifier   = Modifier.fillMaxSize(),
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
            messageInput    = messageInput,
            onMessageChange = onMessageChange,
            onSubmit        = onSubmit,
            isSubmitting    = submitState == PostSubmitState.Loading,
            canSubmit       = canSubmit,
            onDismiss       = { showWriteSheet = false },
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
                tint     = WarmGray600,
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
                tint     = Color.White,
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
        state                 = pagerState,
        modifier              = modifier,
        beyondViewportPageCount = 1,
        pageSpacing           = 0.dp,
    ) { page ->
        val pageOffset = (pagerState.currentPage - page).toFloat() + pagerState.currentPageOffsetFraction
        content(page, pageOffset)
    }
}

// ── Book cover page ───────────────────────────────────────────────────────────

@Composable
private fun BookCoverPage(
    isEmpty: Boolean,
    pageOffset: Float,
    modifier: Modifier = Modifier,
) {
    val serifFamily = MaterialTheme.typography.displayLarge.fontFamily

    Box(
        modifier = modifier.graphicsLayer {
            val fraction = pageOffset.absoluteValue.coerceIn(0f, 1f)
            scaleX     = 1f - 0.05f * fraction
            scaleY     = 1f - 0.05f * fraction
            alpha      = 1f - 0.28f * fraction
            rotationY  = -(pageOffset.coerceIn(-1f, 1f)) * 4f
            cameraDistance = 14f * density
        },
    ) {
        // Stack of pages behind cover
        PageStack(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
        )

        // Cover card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm)
                .shadow(
                    elevation = 16.dp,
                    shape     = RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 4.dp, bottomEnd = 14.dp),
                    ambientColor = WarmGray700.copy(alpha = 0.25f),
                    spotColor    = WarmGray700.copy(alpha = 0.35f),
                )
                .background(
                    Brush.verticalGradient(listOf(Champagne.copy(alpha = 0.55f), WarmWhite, WarmWhite, WarmWhite)),
                    RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 4.dp, bottomEnd = 14.dp),
                )
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 4.dp, bottomEnd = 14.dp)),
        ) {
            // Spine shadow (left edge binding)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(18.dp)
                    .align(Alignment.CenterStart)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Black.copy(alpha = 0.09f), Color.Transparent)
                        )
                    )
            )

            // Paper texture
            PaperTexture()

            // Decorative border
            CoverBorder()

            // Corner flourishes
            CornerFlourish()

            // Cover content
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
                    tint     = Gold.copy(alpha = 0.55f),
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.height(Spacing.lg))
                Text(
                    text  = "Our Wedding",
                    style = TextStyle(
                        fontFamily  = DancingScript,
                        fontWeight  = FontWeight.SemiBold,
                        fontSize    = 46.sp,
                        lineHeight  = 52.sp,
                        color       = WarmGray800,
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
                Text(
                    text  = if (isEmpty)
                        "Be the first to\nleave a memory"
                    else
                        "A treasured collection\nof heartfelt memories",
                    style = TextStyle(
                        fontFamily = serifFamily,
                        fontStyle  = FontStyle.Italic,
                        fontSize   = 15.sp,
                        lineHeight = 24.sp,
                        color      = WarmGray500,
                    ),
                    textAlign = TextAlign.Center,
                )
                if (!isEmpty) {
                    Spacer(Modifier.height(Spacing.xl))
                    Text(
                        text  = "swipe to read  →",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                        color = Gold.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

// ── Guestbook page (1 memory) ─────────────────────────────────────────────────

@Composable
fun GuestbookPage(
    post: GuestbookPost,
    pageIndex: Int,
    totalPages: Int,
    pageOffset: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.graphicsLayer {
            val fraction = pageOffset.absoluteValue.coerceIn(0f, 1f)
            scaleX     = 1f - 0.05f * fraction
            scaleY     = 1f - 0.05f * fraction
            alpha      = 1f - 0.28f * fraction
            rotationY  = -(pageOffset.coerceIn(-1f, 1f)) * 4f
            cameraDistance = 14f * density
        },
    ) {
        // Stack of pages behind
        PageStack(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
        )

        // Main memory card
        MemoryEntryCard(
            post      = post,
            pageIndex = pageIndex,
            totalPages = totalPages,
            modifier  = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
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
) {
    val serifFamily = MaterialTheme.typography.displayLarge.fontFamily

    Box(
        modifier = modifier
            .shadow(
                elevation    = 14.dp,
                shape        = RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 4.dp, bottomEnd = 14.dp),
                ambientColor = WarmGray700.copy(alpha = 0.2f),
                spotColor    = WarmGray700.copy(alpha = 0.3f),
            )
            .background(
                WarmWhite,
                RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 4.dp, bottomEnd = 14.dp),
            )
            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 4.dp, bottomEnd = 14.dp)),
    ) {
        // Spine shadow
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(18.dp)
                .align(Alignment.CenterStart)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Black.copy(alpha = 0.07f), Color.Transparent)
                    )
                )
        )

        // Paper texture
        PaperTexture()

        // Corner flourishes
        CornerFlourish()

        // Page content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Page header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text  = "Memory",
                    style = TextStyle(
                        fontFamily = serifFamily,
                        fontStyle  = FontStyle.Italic,
                        fontSize   = 12.sp,
                        color      = WarmGray400,
                        letterSpacing = 0.5.sp,
                    ),
                )
                Text(
                    text  = "$pageIndex  /  $totalPages",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = WarmGray300,
                )
            }

            Spacer(Modifier.height(Spacing.sm))
            GoldDivider(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Spacing.xl))

            // Wish text (main content)
            ElegantWishText(
                text     = post.message,
                modifier = Modifier.weight(1f),
            )

            Spacer(Modifier.height(Spacing.lg))

            // Heart divider
            HeartDivider()

            Spacer(Modifier.height(Spacing.lg))

            // Signature
            Text(
                text  = "— ${displayName(post.guestId)}",
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
                text  = formatPageDate(post.timestamp),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                color = WarmGray300,
                textAlign = TextAlign.End,
                modifier  = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Elegant wish text ─────────────────────────────────────────────────────────

@Composable
fun ElegantWishText(text: String, modifier: Modifier = Modifier) {
    val fontSize = when {
        text.length > 180 -> 19.sp
        text.length > 100 -> 23.sp
        text.length > 60  -> 27.sp
        else              -> 31.sp
    }
    val lineH = when {
        text.length > 180 -> 30.sp
        text.length > 100 -> 36.sp
        else              -> 42.sp
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

// ── Photo collage layout ──────────────────────────────────────────────────────

@Composable
fun PhotoCollageLayout(photoUrls: List<String>, modifier: Modifier = Modifier) {
    if (photoUrls.isEmpty()) return
    when (photoUrls.size) {
        1 -> AsyncImage(
            model              = photoUrls[0],
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = modifier
                .fillMaxWidth()
                .height(200.dp)
                .rotate(-1.5f)
                .shadow(8.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp)),
        )
        2 -> Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            AsyncImage(
                model = photoUrls[0], contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.weight(1f).aspectRatio(3f / 4f)
                    .rotate(-2f).shadow(6.dp, RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp)),
            )
            AsyncImage(
                model = photoUrls[1], contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.weight(1f).aspectRatio(3f / 4f)
                    .rotate(1.5f).shadow(6.dp, RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp)),
            )
        }
        else -> Row(modifier = modifier.height(200.dp), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            AsyncImage(
                model = photoUrls[0], contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.weight(0.55f).fillMaxHeight()
                    .rotate(-1.5f).shadow(6.dp, RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp)),
            )
            Column(modifier = Modifier.weight(0.45f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                AsyncImage(
                    model = photoUrls[1], contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                        .rotate(2f).shadow(6.dp, RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp)),
                )
                AsyncImage(
                    model = photoUrls[2], contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                        .rotate(-1f).shadow(6.dp, RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp)),
                )
            }
        }
    }
}

// ── Write memory sheet ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WriteMemorySheet(
    messageInput: String,
    onMessageChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isSubmitting: Boolean,
    canSubmit: Boolean,
    onDismiss: () -> Unit,
) {
    val serifFamily = MaterialTheme.typography.displayLarge.fontFamily
    val sheetState  = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = WarmWhite,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle       = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp).height(3.dp)
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
                .padding(horizontal = Spacing.screenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(Spacing.sm))

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
                text  = "Share your heartfelt wishes for the couple",
                style = MaterialTheme.typography.bodySmall,
                color = WarmGray400,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Spacing.lg))
            GoldDivider(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Spacing.lg))

            // Paper-like text input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 180.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .background(Ivory, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp)),
            ) {
                // Subtle ruled lines
                Canvas(modifier = Modifier.matchParentSize()) {
                    val lineColor = WarmGray300.copy(alpha = 0.18f)
                    val spacing   = 30.dp.toPx()
                    var y = 54.dp.toPx()
                    while (y < size.height - 18.dp.toPx()) {
                        drawLine(lineColor, Offset(24.dp.toPx(), y), Offset(size.width - 24.dp.toPx(), y), strokeWidth = 0.7f)
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
                    modifier  = Modifier.fillMaxWidth().padding(Spacing.md),
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

            Spacer(Modifier.height(Spacing.xl))

            // Seal & Sign button (wax seal style)
            if (isSubmitting) {
                CircularProgressIndicator(
                    color       = Gold,
                    strokeWidth = 2.dp,
                    modifier    = Modifier.size(52.dp),
                )
            } else {
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
                        tint     = if (canSubmit) Color.White else WarmGray400,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text  = if (canSubmit) "Seal & Sign" else "Write something first",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = if (canSubmit) Gold else WarmGray300,
            )

            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

// ── Page stack (depth effect) ─────────────────────────────────────────────────

@Composable
private fun PageStack(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 7.dp, y = 4.dp)
                .background(
                    Champagne.copy(alpha = 0.55f),
                    RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 4.dp, bottomEnd = 14.dp),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 4.dp, y = 2.dp)
                .background(
                    ChampagneLight.copy(alpha = 0.75f),
                    RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 4.dp, bottomEnd = 14.dp),
                ),
        )
    }
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

        // Top-left
        val tlPath = Path().apply {
            moveTo(pad, pad + arm)
            quadraticTo(pad, pad, pad + arm, pad)
        }
        drawPath(tlPath, color, style = stroke)

        // Top-right
        val trPath = Path().apply {
            moveTo(size.width - pad - arm, pad)
            quadraticTo(size.width - pad, pad, size.width - pad, pad + arm)
        }
        drawPath(trPath, color, style = stroke)

        // Bottom-left
        val blPath = Path().apply {
            moveTo(pad, size.height - pad - arm)
            quadraticTo(pad, size.height - pad, pad + arm, size.height - pad)
        }
        drawPath(blPath, color, style = stroke)

        // Bottom-right
        val brPath = Path().apply {
            moveTo(size.width - pad - arm, size.height - pad)
            quadraticTo(size.width - pad, size.height - pad, size.width - pad, size.height - pad - arm)
        }
        drawPath(brPath, color, style = stroke)

        // Small tick marks at each corner arm end
        listOf(
            Offset(pad, pad + arm) to Offset(pad + 6.dp.toPx(), pad + arm),
            Offset(pad + arm, pad) to Offset(pad + arm, pad + 6.dp.toPx()),
            Offset(size.width - pad, pad + arm) to Offset(size.width - pad - 6.dp.toPx(), pad + arm),
            Offset(size.width - pad - arm, size.height - pad) to Offset(size.width - pad - arm, size.height - pad - 6.dp.toPx()),
        ).forEach { (a, b) ->
            drawLine(color.copy(alpha = 0.12f), a, b, strokeWidth = 1.dp.toPx(), cap = StrokeCap.Round)
        }
    }
}

// ── Cover decorative border ───────────────────────────────────────────────────

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
        Box(Modifier.width(44.dp).height(0.5.dp).background(WarmGray300.copy(alpha = 0.5f)))
        Spacer(Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint     = BlushDeep.copy(alpha = 0.4f),
            modifier = Modifier.size(10.dp),
        )
        Spacer(Modifier.width(6.dp))
        Box(Modifier.width(44.dp).height(0.5.dp).background(WarmGray300.copy(alpha = 0.5f)))
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
            if (i < visibleCount - 1) {
                Spacer(Modifier.width(5.dp))
            }
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
        drawCircle(Color(0xFFEDD9B8).copy(alpha = 0.35f), 90.dp.toPx(),  Offset(bx,             by - 40.dp.toPx()))
        drawCircle(Color(0xFFF5E6C8).copy(alpha = 0.40f), 65.dp.toPx(),  Offset(bx - 70.dp.toPx(), by - 90.dp.toPx()))
        drawCircle(Color(0xFFEAB8BC).copy(alpha = 0.15f), 55.dp.toPx(),  Offset(bx + 10.dp.toPx(), by - 130.dp.toPx()))
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun displayName(guestId: String): String = when {
    guestId.isBlank()         -> "A Guest"
    guestId.contains("@")     -> guestId.substringBefore("@")
        .replace(".", " ")
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
    guestId.length > 20       -> guestId.take(16) + "…"
    else                      -> guestId
}

private fun formatPageDate(millis: Long): String {
    if (millis == 0L) return ""
    return SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH).format(Date(millis))
}
