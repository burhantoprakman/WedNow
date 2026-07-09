package com.maritsa.app.presentation.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maritsa.app.ui.theme.Blush
import com.maritsa.app.ui.theme.BlushDeep
import com.maritsa.app.ui.theme.BlushLight
import com.maritsa.app.ui.theme.Champagne
import com.maritsa.app.ui.theme.ChampagneLight
import com.maritsa.app.ui.theme.CormorantGaramond
import com.maritsa.app.ui.theme.DmSans
import com.maritsa.app.ui.theme.Gold
import com.maritsa.app.ui.theme.GoldDeep
import com.maritsa.app.ui.theme.GoldLight
import com.maritsa.app.ui.theme.Ivory
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// ── Design-reference colors (extracted from design.png) ───────────────────────
private val DesignCream = Color(0xFFF7EFE5)
private val DesignSkyBlue = Color(0xFFCDD8E8)
private val DesignBlushBg = Color(0xFFF5DDD8)
private val DesignSageBg = Color(0xFFD4E0CC)
private val DesignGoldBg = Color(0xFFF0E4C8)
private val DesignText = Color(0xFF2A1F14)
private val DesignSubtext = Color(0xFF8C6E5A)
private val DesignGoldAccent = Color(0xFFB8975A)

// ── Particle types ────────────────────────────────────────────────────────────
private enum class ParticleType { PETAL, LEAF, BLOSSOM }

private data class Particle(
    val phase: Float,
    val xNorm: Float,
    val xSway: Float,
    val sizeDp: Float,
    val rotSpeed: Float,
    val type: ParticleType,
    val color: Color,
    val maxAlpha: Float
)

// Deterministic pseudo-random so particles are stable across recompositions
private fun pseudoRandom(seed: Double): Float = abs(sin(seed)).toFloat()

private fun pageParticles(pageIndex: Int): List<Particle> {
    val palettes = listOf(
        // Page 0 — Welcome: cherry blossoms (blush/pink + cream)
        listOf(Blush, BlushLight, Color(0xFFF8E0E0), ChampagneLight, Color(0xFFFFF0F0)),
        // Page 1 — Capture: rose petals (deeper pink + blush)
        listOf(BlushDeep, Blush, Color(0xFFF5C8C8), Color(0xFFEDAAAA), Champagne),
        // Page 2 — Connected: spring leaves (sage + mint + champagne)
        listOf(
            Color(0xFFB8CEB0),
            Color(0xFF90B090),
            Color(0xFFA8C8A0),
            ChampagneLight,
            Color(0xFFD0E4C8)
        ),
        // Page 3 — Forever: golden petals (gold + champagne + soft blush)
        listOf(GoldLight, Champagne, Color(0xFFEDD8A0), Blush, Color(0xFFF5E8C0)),
    )
    val colors = palettes[pageIndex]
    val count = 20

    return (0 until count).map { i ->
        val s = (pageIndex * 1000 + i).toDouble()
        val r1 = pseudoRandom(s * 137.508)
        val r2 = pseudoRandom(s * 97.12 + 1.5)
        val r3 = pseudoRandom(s * 43.77 + 2.3)
        val r4 = pseudoRandom(s * 61.18 + 3.7)

        Particle(
            phase = i.toFloat() / count.toFloat(),
            xNorm = r1 * 0.82f + 0.09f,
            xSway = 18f + r2 * 38f,
            sizeDp = 8f + r1 * 14f,
            rotSpeed = 0.35f + r3 * 1.1f,
            type = when (i % 3) {
                0 -> ParticleType.PETAL
                1 -> ParticleType.LEAF
                else -> ParticleType.BLOSSOM
            },
            color = colors[(i * 7 + pageIndex) % colors.size],
            maxAlpha = 0.38f + r4 * 0.35f
        )
    }
}

// ── Page data ─────────────────────────────────────────────────────────────────
private data class OnboardingPage(
    val headline: String,
    val subline: String,
    val ctaLabel: String,
    val bgTop: Color,
    val bgMid: Color,
    val bgBot: Color
)

private val pages = listOf(
    OnboardingPage(
        headline = "Every Wedding\nHas a Story.",
        subline = "Yours deserves to be told beautifully.",
        ctaLabel = "Begin",
        bgTop = DesignSkyBlue,
        bgMid = DesignCream,
        bgBot = Color(0xFFF5EDE0)
    ),
    OnboardingPage(
        headline = "Capture Every\nFlash of Joy.",
        subline = "Guests share photos in real time — no requests needed.",
        ctaLabel = "See how",
        bgTop = Color(0xFFDDCCE0),
        bgMid = Color(0xFFF5E8E4),
        bgBot = Color(0xFFFAF0EC)
    ),
    OnboardingPage(
        headline = "Everyone\nStays Connected.",
        subline = "Chat, RSVPs, schedules — all in one place.",
        ctaLabel = "Explore",
        bgTop = Color(0xFFC4D8CC),
        bgMid = Color(0xFFE8F0E4),
        bgBot = Color(0xFFF4F8F0)
    ),
    OnboardingPage(
        headline = "Relive It\nForever.",
        subline = "Your memories, messages, and moments — always preserved.",
        ctaLabel = "Join the Celebration",
        bgTop = Color(0xFFD8C8A8),
        bgMid = Color(0xFFF0E8D4),
        bgBot = Color(0xFFFAF5E8)
    ),
)

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun OnboardingScreen(
    onCompleted: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    fun finish() {
        viewModel.complete()
        onCompleted()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { i ->
            PageScene(
                page = pages[i],
                particles = remember(i) { pageParticles(i) }
            )
        }

        // Skip
        if (!isLastPage) {
            TextButton(
                onClick = ::finish,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 50.dp, end = 16.dp)
            ) {
                Text(
                    text = "Skip",
                    fontFamily = DmSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = DesignSubtext.copy(alpha = 0.65f)
                )
            }
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LiquidIndicator(count = pages.size, current = pagerState.currentPage)
            Spacer(modifier = Modifier.height(32.dp))
            PremiumButton(
                label = pages[pagerState.currentPage].ctaLabel,
                isLast = isLastPage,
                onClick = {
                    if (isLastPage) finish()
                    else scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            )
        }
    }
}

// ── Page scene ────────────────────────────────────────────────────────────────
@Composable
private fun PageScene(page: OnboardingPage, particles: List<Particle>) {
    val transition = rememberInfiniteTransition(label = "fall")
    // Master clock: 0→1 in 10 s, loop
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fall_clock"
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // Background — diagonal warm-cream-to-blue gradient matching design.png
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to page.bgTop,
                            0.45f to page.bgMid,
                            1.00f to page.bgBot
                        )
                    )
                )
        )

        // Botanical falling layer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val buffer = 100.dp.toPx()
            val totalTravel = h + buffer * 2f

            particles.forEach { p ->
                val cycle = ((progress + p.phase) % 1f)
                val yPos = cycle * totalTravel - buffer
                val xDrift = sin(cycle * 2f * PI.toFloat() * 1.8f) * p.xSway.dp.toPx()
                val xPos = p.xNorm * w + xDrift
                val rot = cycle * 360f * p.rotSpeed
                val alpha = when {
                    cycle < 0.08f -> (cycle / 0.08f) * p.maxAlpha
                    cycle > 0.88f -> ((1f - cycle) / 0.12f) * p.maxAlpha
                    else -> p.maxAlpha
                }
                val sz = p.sizeDp.dp.toPx()

                when (p.type) {
                    ParticleType.PETAL -> drawPetal(xPos, yPos, sz, rot, p.color, alpha)
                    ParticleType.LEAF -> drawLeaf(xPos, yPos, sz, rot, p.color, alpha)
                    ParticleType.BLOSSOM -> drawBlossom(xPos, yPos, sz, rot, p.color, alpha)
                }
            }
        }

        // Gentle bottom vignette so text stays legible
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, page.bgBot.copy(alpha = 0.94f))
                    )
                )
        )

        // Text content
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 36.dp, end = 36.dp, bottom = 200.dp),
            horizontalAlignment = Alignment.Start
        ) {

            Text(
                text = page.headline,
                fontFamily = CormorantGaramond,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
                lineHeight = 52.sp,
                letterSpacing = (-0.5).sp,
                color = DesignText,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = page.subline,
                fontFamily = DmSans,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 23.sp,
                letterSpacing = 0.1.sp,
                color = DesignSubtext,
                textAlign = TextAlign.Start
            )
        }
    }
}

// ── Canvas botanical draw functions ───────────────────────────────────────────

// Single petal: tapered oval pointing "up" from base, rotated in place
private fun DrawScope.drawPetal(
    cx: Float, cy: Float, size: Float,
    rotation: Float, color: Color, alpha: Float
) {
    val path = Path().apply {
        moveTo(cx, cy)
        cubicTo(
            cx - size * 0.42f, cy - size * 0.22f,
            cx - size * 0.36f, cy - size * 0.82f,
            cx, cy - size
        )
        cubicTo(
            cx + size * 0.36f, cy - size * 0.82f,
            cx + size * 0.42f, cy - size * 0.22f,
            cx, cy
        )
    }
    withTransform({ rotate(rotation, Offset(cx, cy)) }) {
        drawPath(path, color.copy(alpha = alpha))
    }
}

// Leaf: slightly asymmetric pointed shape with a stem vein
private fun DrawScope.drawLeaf(
    cx: Float, cy: Float, size: Float,
    rotation: Float, color: Color, alpha: Float
) {
    val path = Path().apply {
        moveTo(cx, cy)
        // Left curve — slightly wider belly
        cubicTo(
            cx - size * 0.40f, cy - size * 0.28f,
            cx - size * 0.30f, cy - size * 0.78f,
            cx, cy - size
        )
        // Right curve — slightly thinner
        cubicTo(
            cx + size * 0.26f, cy - size * 0.78f,
            cx + size * 0.34f, cy - size * 0.28f,
            cx, cy
        )
    }
    withTransform({ rotate(rotation, Offset(cx, cy)) }) {
        drawPath(path, color.copy(alpha = alpha))
        // Center vein
        drawLine(
            color = color.copy(alpha = alpha * 0.30f),
            start = Offset(cx, cy - size * 0.08f),
            end = Offset(cx, cy - size * 0.92f),
            strokeWidth = 0.8.dp.toPx()
        )
    }
}

// Blossom: 5-petal flower that rotates as a unit
private fun DrawScope.drawBlossom(
    cx: Float, cy: Float, size: Float,
    rotation: Float, color: Color, alpha: Float
) {
    val petalOffset = size * 0.38f
    repeat(5) { i ->
        // Angle of this petal's position (in radians) from blossom center
        val posAngleRad = ((rotation + i * 72.0) * PI / 180.0).toFloat()
        // Each petal base is offset from center, rotated outward
        val pcx = cx + cos(posAngleRad) * petalOffset
        val pcy = cy + sin(posAngleRad) * petalOffset
        // Petal rotation: petal tip must point outward from center
        // Our petal path points upward (-Y); outward direction = posAngle deg from +X axis
        // Rotation to align "up" to "outward" = posAngle + 90°
        val petalRot = rotation + i * 72f + 90f

        val path = Path().apply {
            moveTo(pcx, pcy)
            cubicTo(
                pcx - size * 0.22f, pcy - size * 0.08f,
                pcx - size * 0.22f, pcy - size * 0.46f,
                pcx, pcy - size * 0.56f
            )
            cubicTo(
                pcx + size * 0.22f, pcy - size * 0.46f,
                pcx + size * 0.22f, pcy - size * 0.08f,
                pcx, pcy
            )
        }
        withTransform({ rotate(petalRot, Offset(pcx, pcy)) }) {
            drawPath(path, color.copy(alpha = alpha))
        }
    }
    // Yellow-cream center dot
    drawCircle(
        color = Color(0xFFFFF4D6).copy(alpha = (alpha * 0.90f).coerceAtMost(1f)),
        radius = size * 0.13f,
        center = Offset(cx, cy)
    )
}

// ── Page indicator ────────────────────────────────────────────────────────────
@Composable
private fun LiquidIndicator(count: Int, current: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { i ->
            val isActive = i == current
            val width: Dp by animateDpAsState(
                targetValue = if (isActive) 28.dp else 6.dp,
                animationSpec = tween(360, easing = FastOutSlowInEasing),
                label = "dot_w_$i"
            )
            val dotAlpha by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.30f,
                animationSpec = tween(360),
                label = "dot_a_$i"
            )
            Box(
                modifier = Modifier
                    .height(5.dp)
                    .width(width)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (isActive)
                            Brush.horizontalGradient(listOf(DesignGoldAccent, Blush))
                        else
                            Brush.horizontalGradient(
                                listOf(
                                    DesignSubtext.copy(alpha = dotAlpha),
                                    DesignSubtext.copy(alpha = dotAlpha)
                                )
                            )
                    )
            )
        }
    }
}

// ── CTA button ────────────────────────────────────────────────────────────────
@Composable
private fun PremiumButton(label: String, isLast: Boolean, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(listOf(GoldDeep, Gold, GoldLight))
            )
            .clickable(interactionSource = source, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = DmSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                letterSpacing = 0.3.sp,
                color = Ivory
            )
            if (!isLast) {
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Ivory.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
