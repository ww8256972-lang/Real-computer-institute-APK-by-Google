package com.example.ui.screens

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.components.rememberAnimationsEnabled
import kotlinx.coroutines.delay

/**
 * Polished 4-Second Welcome Introduction for EduSync Academy (Requirement 8)
 * Features a professional EduSync Academy logo reveal, smooth circular orbital motion,
 * integrated promotional background video, responsive skip control, and 4000ms duration.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val context = LocalContext.current
    val animationsEnabled = rememberAnimationsEnabled()
    var playbackProgress by remember { mutableFloatStateOf(0f) }
    var hasFinished by remember { mutableStateOf(false) }

    fun finishSafely() {
        if (!hasFinished) {
            hasFinished = true
            onSplashFinished()
        }
    }

    // Exact 4-second (4000ms) animated introduction
    LaunchedEffect(Unit) {
        val totalDurationMs = 4000L
        val stepMs = 40L
        var elapsed = 0L
        while (elapsed < totalDurationMs && !hasFinished) {
            delay(stepMs)
            elapsed += stepMs
            playbackProgress = (elapsed.toFloat() / totalDurationMs).coerceIn(0f, 1f)
        }
        finishSafely()
    }

    // Logo reveal scale & alpha
    val logoScale by animateFloatAsState(
        targetValue = if (playbackProgress > 0.04f) 1f else 0.72f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoRevealScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (playbackProgress > 0.05f) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "contentRevealAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "splashOrbitalRings")
    val outerRingAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRingAngle"
    )
    val innerRingAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRingAngle"
    )
    val pulseRadiusFactor by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadiusFactor"
    )

    val videoUri = remember {
        try {
            Uri.parse("android.resource://" + context.packageName + "/" + R.raw.intro_video)
        } catch (_: Exception) {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("splash_screen")
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050A18),
                        Color(0xFF0A142D),
                        Color(0xFF060B18)
                    )
                )
            )
    ) {
        // Optional subtle promo video backdrop
        if (videoUri != null) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoURI(videoUri)
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            mp.setVolume(0.6f, 0.6f)
                            start()
                        }
                        setOnErrorListener { _, _, _ -> true }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.32f)
            )
        }

        // Top Bar with Brand Badge & Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0x2600E5FF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x5500E5FF))
            ) {
                Text(
                    text = "EDUSYNC 3D PORTAL",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF38BDF8),
                    letterSpacing = 1.sp
                )
            }

            Surface(
                onClick = { finishSafely() },
                shape = RoundedCornerShape(20.dp),
                color = Color(0x33FFFFFF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x55FFFFFF)),
                modifier = Modifier.testTag("splash_skip_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Skip", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Skip Welcome Animation",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Center 3D Animated Logo Reveal & Circular Orbital Rings
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 28.dp)
                .scale(if (animationsEnabled) logoScale else 1f)
                .alpha(if (animationsEnabled) contentAlpha else 1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                if (animationsEnabled) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerOffset = Offset(size.width / 2f, size.height / 2f)
                        val outerR = (size.minDimension / 2f) * 0.92f * pulseRadiusFactor
                        val midR = (size.minDimension / 2f) * 0.74f

                        // Ambient radial glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00E5FF).copy(alpha = 0.25f),
                                    Color(0xFF7C4DFF).copy(alpha = 0.10f),
                                    Color.Transparent
                                ),
                                center = centerOffset,
                                radius = outerR * 1.15f
                            ),
                            radius = outerR * 1.15f,
                            center = centerOffset
                        )

                        // Outer rotating orbital arcs
                        rotate(degrees = outerRingAngle, pivot = centerOffset) {
                            val topLeft = Offset(centerOffset.x - outerR, centerOffset.y - outerR)
                            val arcSize = Size(outerR * 2f, outerR * 2f)
                            for (i in 0 until 3) {
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            Color(0xFF00E5FF),
                                            Color(0xFF38BDF8),
                                            Color(0xFF7C4DFF),
                                            Color(0xFF00E5FF)
                                        ),
                                        center = centerOffset
                                    ),
                                    startAngle = i * 120f,
                                    sweepAngle = 76f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }

                        // Inner counter-rotating orbital ring
                        rotate(degrees = innerRingAngle, pivot = centerOffset) {
                            val topLeft = Offset(centerOffset.x - midR, centerOffset.y - midR)
                            val arcSize = Size(midR * 2f, midR * 2f)
                            for (i in 0 until 2) {
                                drawArc(
                                    color = Color(0xFFA78BFA).copy(alpha = 0.85f),
                                    startAngle = i * 180f,
                                    sweepAngle = 105f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }
                    }
                }

                // Core 3D Emblem Medallion
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF00C6FF),
                                    Color(0xFF2563EB),
                                    Color(0xFF7C3AED)
                                )
                            )
                        )
                        .border(2.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "EduSync Academy Emblem",
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "EduSync Academy",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Computer Education & Training Institute",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF38BDF8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0x2800C6FF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6600C6FF))
            ) {
                Text(
                    text = "Learn • Grow • Succeed",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
                    color = Color(0xFF7DD3FC),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // Bottom 4-Second Progress Indicator
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Initializing Secure Academic Workspace...",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(playbackProgress * 100).toInt()}%",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            LinearProgressIndicator(
                progress = { playbackProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFF00E5FF),
                trackColor = Color(0x33FFFFFF)
            )
        }
    }
}
