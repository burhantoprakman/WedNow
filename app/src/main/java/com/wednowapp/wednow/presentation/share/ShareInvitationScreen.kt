package com.wednowapp.wednow.presentation.share

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.wednowapp.wednow.R
import com.wednowapp.wednow.domain.model.Wedding
import com.wednowapp.wednow.ui.components.WedNowErrorScreen
import com.wednowapp.wednow.ui.components.WedNowLoadingScreen
import com.wednowapp.wednow.ui.theme.ChampagneLight
import com.wednowapp.wednow.ui.theme.Gold
import com.wednowapp.wednow.ui.theme.GoldDeep
import com.wednowapp.wednow.ui.theme.Ivory
import com.wednowapp.wednow.ui.theme.Spacing
import com.wednowapp.wednow.ui.theme.WarmGray400
import com.wednowapp.wednow.ui.theme.WarmGray500
import com.wednowapp.wednow.ui.theme.WarmGray700
import com.wednowapp.wednow.ui.theme.WarmGray800
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun ShareInvitationScreen(
    onEnterWedding: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: ShareInvitationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = Color.Transparent,
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val s = state) {
                is ShareInvitationState.Loading ->
                    WedNowLoadingScreen()

                is ShareInvitationState.Error ->
                    WedNowErrorScreen(message = s.message, onRetry = {})

                is ShareInvitationState.Success -> {
                    val code = s.wedding.shortCode.ifBlank { viewModel.weddingId }
                    ShareInvitationContent(
                        wedding = s.wedding,
                        weddingId = viewModel.weddingId,
                        isPrivileged = s.isPrivileged,
                        onEnterWedding = onEnterWedding,
                        onBack = onBack,
                        snackbarHost = snackbarHost,
                        deepLinkUrl = "https://maritsa.app/join/$code",
                    )
                }
            }
        }
    }
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
private fun ShareInvitationContent(
    wedding: Wedding,
    weddingId: String,
    isPrivileged: Boolean,
    onEnterWedding: () -> Unit,
    onBack: (() -> Unit)?,
    snackbarHost: SnackbarHostState,
    deepLinkUrl: String,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()

    val qrBitmap = remember(weddingId) { generateQrBitmap(deepLinkUrl, 512) }

    Box(Modifier.fillMaxSize()) {
        // ── Page background ───────────────────────────────────────────────────
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // Soft overlay so the card reads clearly on top
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.18f),
                            Color.Black.copy(alpha = 0.08f),
                            Color.Black.copy(alpha = 0.18f),
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.30f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.ArrowBack, "Back", Modifier.size(18.dp), Color.White)
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.sm))

            // ── Invitation card (captured by GraphicsLayer) ───────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screenHorizontal)
                    .drawWithContent {
                        graphicsLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(graphicsLayer)
                    },
            ) {
                RealInvitationCard(
                    wedding = wedding,
                    weddingId = weddingId,
                    qrBitmap = qrBitmap,
                    deepLinkUrl = deepLinkUrl,
                )
            }

            Spacer(Modifier.height(Spacing.lg))

            // ── Action buttons (admin / co-admin only) ────────────────────────
            if (isPrivileged) {
                Column(
                    modifier = Modifier.padding(horizontal = Spacing.screenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    // Share invitation as PDF with clickable deep link
                    ActionButton(
                        icon = Icons.Default.Share,
                        label = "Share Invitation",
                        isPrimary = true,
                        onClick = {
                            scope.launch {
                                val bmp = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                // deepLinkUrl opens the app when installed; the hosted
                                // page should redirect to Play Store when it is not.
                                val uri = createInvitationPdf(context, bmp, weddingId, deepLinkUrl)
                                if (uri != null) {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(
                                            Intent.EXTRA_SUBJECT,
                                            "Wedding Invitation — ${wedding.name}"
                                        )
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(
                                            intent,
                                            "Share Invitation"
                                        )
                                    )
                                }
                            }
                        },
                    )

                    // Save invitation PDF to Downloads
                    ActionButton(
                        icon = Icons.Default.Download,
                        label = "Save PDF",
                        isPrimary = false,
                        onClick = {
                            scope.launch {
                                val bmp = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                val bytes = buildInvitationPdfBytes(bmp, deepLinkUrl)
                                val saved = if (bytes != null) savePdfToDocuments(
                                    context,
                                    bytes,
                                    weddingId
                                ) else false
                                snackbarHost.showSnackbar(
                                    if (saved) "Invitation PDF saved to Downloads ✓"
                                    else "Failed to save — please try again"
                                )
                            }
                        },
                    )
                }

                Spacer(Modifier.height(Spacing.lg))
            }

            // ── Enter wedding ─────────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = Spacing.screenHorizontal),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                // Primary CTA — always visible
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Gold, GoldDeep)))
                        .clickable(onClick = onEnterWedding),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White.copy(alpha = 0.85f),
                        )
                        Text(
                            text = "Enter Your Wedding",
                            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.5.sp),
                            color = Color.White,
                        )
                    }
                }

                Text(
                    "Code: ${wedding.shortCode.ifBlank { weddingId }}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Color.White.copy(alpha = 0.65f),
                )
            }

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

// ── Real invitation card

@Composable
private fun RealInvitationCard(
    wedding: Wedding,
    weddingId: String,
    qrBitmap: Bitmap,
    deepLinkUrl: String,
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            // Outer gold border — classic invitation double-rule effect
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Gold.copy(alpha = 0.8f),
                        GoldDeep.copy(alpha = 0.6f),
                        Gold.copy(alpha = 0.8f)
                    )
                ),
                shape = RoundedCornerShape(4.dp),
            )
            .background(Ivory),
    ) {
        // Inner decorative border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(
                    width = 0.5.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Gold.copy(alpha = 0.4f),
                            ChampagneLight,
                            Gold.copy(alpha = 0.4f)
                        )
                    ),
                    shape = RoundedCornerShape(2.dp),
                )
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {

                // ── Opening ornament ──────────────────────────────────────────
                OrnamentDivider()
                Spacer(Modifier.height(20.dp))

                // ── Subtitle / Together with ──────────────────────────────────
                if (wedding.name.isNotBlank()) {
                    Text(
                        text = "Together with their families",
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        color = WarmGray500,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // ── Couple names ──────────────────────────────────────────────
                val nameStyle = MaterialTheme.typography.displayMedium.copy(
                    lineHeight = 52.sp,
                    fontWeight = FontWeight.Normal,
                )
                val ampIdx = wedding.name.indexOf(" & ")
                if (ampIdx != -1) {
                    val first = wedding.name.substring(0, ampIdx)
                    val second = wedding.name.substring(ampIdx + 3)
                    Text(
                        first,
                        style = nameStyle,
                        color = WarmGray800,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "&",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontStyle = FontStyle.Italic,
                            color = Gold,
                        ),
                    )
                    Text(
                        second,
                        style = nameStyle,
                        color = WarmGray800,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        wedding.name,
                        style = nameStyle,
                        color = WarmGray800,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Invitation phrase ─────────────────────────────────────────
                Text(
                    text = "request the honour of your presence\nat the celebration of their marriage",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = WarmGray500,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )

                Spacer(Modifier.height(24.dp))
                ThinDividerWithDiamond()
                Spacer(Modifier.height(20.dp))

                // ── Date & venue
                if (wedding.date != 0L) {
                    InvitationLine(
                        label = "DATE & TIME",
                        value = formatWeddingDate(wedding.date),
                    )
                    Spacer(Modifier.height(10.dp))
                }
                if (wedding.location.isNotBlank()) {
                    InvitationLine(
                        label = "VENUE",
                        value = wedding.location,
                    )
                }

                if (wedding.date != 0L || wedding.location.isNotBlank()) {
                    Spacer(Modifier.height(20.dp))
                    ThinDividerWithDiamond()
                    Spacer(Modifier.height(20.dp))
                }

                // ── QR code
                //
                // Tapping the label or the QR image itself opens the deep link:
                //   • App installed   → WedNow opens and the guest joins.
                //   • App not installed → browser opens the hosted page which
                //     redirects the guest to the Play Store.
                Text(
                    text = "SCAN OR CLICK QR TO JOIN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline,
                    ),
                    color = WarmGray400,
                    modifier = Modifier.clickable { openDeepLink(context, deepLinkUrl) },
                )
                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .background(Color.White)
                        .padding(8.dp)
                        .clickable { openDeepLink(context, deepLinkUrl) },
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Tap to join this wedding",
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Code: ${wedding.shortCode.ifBlank { "—" }}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Gold.copy(alpha = 0.75f),
                )

                Spacer(Modifier.height(24.dp))
                OrnamentDivider()
            }
        }
    }
}

// ── Card sub-components ───────────────────────────────────────────────────────

@Composable
private fun InvitationLine(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = Gold,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = WarmGray700,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun OrnamentDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier
                .size(32.dp, 0.5.dp)
                .background(Gold.copy(alpha = 0.5f))
        )
        Text("✦", fontSize = 9.sp, color = Gold.copy(alpha = 0.7f))
        Icon(Icons.Default.Favorite, null, Modifier.size(10.dp), Gold.copy(alpha = 0.8f))
        Text("✦", fontSize = 9.sp, color = Gold.copy(alpha = 0.7f))
        Box(
            Modifier
                .size(32.dp, 0.5.dp)
                .background(Gold.copy(alpha = 0.5f))
        )
    }
}

@Composable
private fun ThinDividerWithDiamond() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(Gold.copy(alpha = 0.25f))
        )
        Text("◆", fontSize = 7.sp, color = Gold.copy(alpha = 0.5f))
        Box(
            Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(Gold.copy(alpha = 0.25f))
        )
    }
}

// ── Action button ─────────────────────────────────────────────────────────────

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isPrimary)
                    Brush.linearGradient(listOf(Gold, GoldDeep))
                else
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.85f),
                            ChampagneLight.copy(alpha = 0.9f)
                        )
                    )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isPrimary) Color.White else WarmGray700,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.5.sp),
                color = if (isPrimary) Color.White else WarmGray700,
            )
        }
    }
}

// ── Deep-link / App-store launcher ───────────────────────────────────────────

/**
 * Opens [url] via the standard ACTION_VIEW intent.
 *
 * Behaviour on Android:
 *  • If [url] is the Maritsa App Link (https://maritsa.app/join/…) and the app is
 *    installed, Android routes it directly to WedNow (no browser needed).
 *  • If the app is **not** installed, the browser opens the page.  Host a simple
 *    redirect page at https://maritsa.app/join/{id} that sends guests to the
 *    Play Store, e.g.:
 *      <meta http-equiv="refresh" content="0; url=https://play.google.com/store/apps/details?id=com.wednowapp.wednow">
 */
private fun openDeepLink(context: Context, url: String) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}

// ── PDF generation ────────────────────────────────────────────────────────────

private fun createInvitationPdf(
    context: Context,
    bitmap: Bitmap,
    weddingId: String,
    joinUrl: String,
): Uri? = runCatching {
    val pdfBytes = buildInvitationPdfBytes(bitmap, joinUrl) ?: return@runCatching null
    val dir = File(context.cacheDir, "invitation").also { it.mkdirs() }
    val file = File(dir, "invitation_$weddingId.pdf")
    file.writeBytes(pdfBytes)
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}.getOrNull()

fun buildInvitationPdfBytes(bitmap: Bitmap, joinUrl: String): ByteArray? = runCatching {
    // Compress bitmap as JPEG for embedding
    val jpegOut = java.io.ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, jpegOut)
    val jpegBytes = jpegOut.toByteArray()
    val imgW = bitmap.width
    val imgH = bitmap.height

    // PDF page: A4 width (595 pt), height proportional to bitmap
    val pageW = 595
    val pageH = (595.0 * imgH / imgW).toInt()

    // Link annotation rect: bottom ~28% of page, centered (covers QR code area)
    // PDF y-origin is at the bottom of the page
    val lx1 = (pageW * 0.12).toInt()
    val ly1 = 0
    val lx2 = (pageW * 0.88).toInt()
    val ly2 = (pageH * 0.28).toInt()

    val out = java.io.ByteArrayOutputStream()
    val offsets = IntArray(8)

    fun w(s: String) = out.write(s.toByteArray(Charsets.ISO_8859_1))
    fun wb(b: ByteArray) = out.write(b)

    // Header (binary hint bytes mark this as a binary file)
    w("%PDF-1.4\n%âãÏÓ\n")

    // Object 1: Catalog
    offsets[1] = out.size()
    w("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n")

    // Object 2: Page tree
    offsets[2] = out.size()
    w("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n")

    // Object 3: Page
    offsets[3] = out.size()
    w("3 0 obj\n")
    w("<< /Type /Page /MediaBox [0 0 $pageW $pageH]\n")
    w("   /Contents 4 0 R\n")
    w("   /Resources << /XObject << /Im1 5 0 R >> >>\n")
    w("   /Annots [6 0 R] /Parent 2 0 R >>\n")
    w("endobj\n")

    // Object 4: Content stream — scale image to fill page
    val cs = "q $pageW 0 0 $pageH 0 0 cm /Im1 Do Q"
    offsets[4] = out.size()
    w("4 0 obj\n<< /Length ${cs.length} >>\nstream\n$cs\nendstream\nendobj\n")

    // Object 5: JPEG image XObject
    offsets[5] = out.size()
    w("5 0 obj\n")
    w("<< /Type /XObject /Subtype /Image\n")
    w("   /Width $imgW /Height $imgH /ColorSpace /DeviceRGB\n")
    w("   /BitsPerComponent 8 /Filter /DCTDecode /Length ${jpegBytes.size} >>\n")
    w("stream\n")
    wb(jpegBytes)
    w("\nendstream\nendobj\n")

    // Object 6: URI link annotation over the QR code area
    offsets[6] = out.size()
    w("6 0 obj\n")
    w("<< /Type /Annot /Subtype /Link\n")
    w("   /Rect [$lx1 $ly1 $lx2 $ly2]\n")
    w("   /A << /Type /Action /S /URI /URI ($joinUrl) >>\n")
    w("   /Border [0 0 0] >>\n")
    w("endobj\n")

    // Cross-reference table (each entry is exactly 20 bytes)
    val xrefPos = out.size()
    w("xref\n0 7\n")
    w("0000000000 65535 f \n")
    for (i in 1..6) {
        w(offsets[i].toString().padStart(10, '0') + " 00000 n \n")
    }
    w("trailer\n<< /Size 7 /Root 1 0 R >>\n")
    w("startxref\n$xrefPos\n%%EOF\n")

    out.toByteArray()
}.getOrNull()

// ── QR generation ─────────────────────────────────────────────────────────────

private fun generateQrBitmap(content: String, sizePx: Int): Bitmap {
    val hints = mapOf(EncodeHintType.MARGIN to 1)
    val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
    val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    for (x in 0 until sizePx) for (y in 0 until sizePx) {
        bmp.setPixel(
            x,
            y,
            if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        )
    }
    return bmp
}

// ── Date formatting ───────────────────────────────────────────────────────────

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

// ── Save PDF to Downloads ─────────────────────────────────────────────────────

private fun savePdfToDocuments(context: Context, pdfBytes: ByteArray, weddingId: String): Boolean =
    runCatching {
        val filename = "Maritsa_Invitation_$weddingId.pdf"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, filename)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            }
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
            )!!
            context.contentResolver.openOutputStream(uri)!!.use { it.write(pdfBytes) }
        } else {
            // App-specific external storage — no permission required
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: context.filesDir
            dir.mkdirs()
            File(dir, filename).writeBytes(pdfBytes)
        }
        true
    }.getOrDefault(false)
