package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Smooth, lightweight 3-second mobile splash screen animation for 'EduSync Academy'
 * Displays the text 'Welcome' transitioning into 'Trusted by 100,000+ Students'
 * alongside glowing Python and Java coding icons.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var phaseTwo by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_and_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    LaunchedEffect(Unit) {
        val totalMs = 3000L
        val intervalMs = 25L
        var elapsed = 0L

        while (elapsed < totalMs) {
            delay(intervalMs)
            elapsed += intervalMs
            progress = (elapsed.toFloat() / totalMs).coerceIn(0f, 1f)

            if (elapsed >= 1400L && !phaseTwo) {
                phaseTwo = true
            }
        }
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("splash_screen")
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070B14),
                        Color(0xFF0F172A),
                        Color(0xFF0A0F1D)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing Coding Icons Row (Python & Java)
            Row(
                modifier = Modifier
                    .scale(pulseScale)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glowing Python Icon
                GlowingPythonIcon(glowAlpha = glowAlpha)

                // Glowing Java Icon
                GlowingJavaIcon(glowAlpha = glowAlpha)
            }

            // Academy Brand Header
            Text(
                text = "EduSync Academy",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "COMPUTER INSTITUTE MANAGEMENT SYSTEM",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF38BDF8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Animated Transition Container: 'Welcome' -> 'Trusted by 100,000+ Students'
            Box(
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !phaseTwo,
                    enter = fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.9f),
                    exit = fadeOut(animationSpec = tween(350)) + slideOutVertically(targetOffsetY = { -it / 2 })
                ) {
                    Text(
                        text = "Welcome",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = Color(0xFFF8FAFC),
                        textAlign = TextAlign.Center
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = phaseTwo,
                    enter = fadeIn(animationSpec = tween(600)) + scaleIn(initialScale = 0.95f),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF0284C7).copy(alpha = 0.3f),
                                        Color(0xFF2563EB).copy(alpha = 0.35f),
                                        Color(0xFF7C3AED).copy(alpha = 0.3f)
                                    )
                                )
                            )
                            .border(
                                width = 1.5.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF38BDF8), Color(0xFF60A5FA), Color(0xFFA855F7))
                                ),
                                shape = RoundedCornerShape(30.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Badge",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Trusted by 100,000+ Students",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Smooth Progress Indicator
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .width(200.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF38BDF8),
                trackColor = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (phaseTwo) "Powering 100,000+ Students & Professionals" else "Loading Academy Services...",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
            )
        }

        // Bottom Certification Label
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "ISO 9001:2015 Certified Computer Education",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF475569)
            )
        }
    }
}

/**
 * Glowing Python Coding Icon
 */
@Composable
fun GlowingPythonIcon(
    glowAlpha: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(76.dp)
            .shadow(
                elevation = (12 * glowAlpha).dp,
                shape = CircleShape,
                ambientColor = Color(0xFF38BDF8),
                spotColor = Color(0xFFFFD43B)
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.sweepGradient(
                    listOf(
                        Color(0xFF38BDF8).copy(alpha = glowAlpha),
                        Color(0xFFFFD43B).copy(alpha = glowAlpha),
                        Color(0xFF38BDF8).copy(alpha = glowAlpha)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(46.dp)) {
            val w = size.width
            val h = size.height

            // Upper Python Snake (Blue)
            val bluePath = Path().apply {
                moveTo(w * 0.2f, h * 0.5f)
                lineTo(w * 0.2f, h * 0.32f)
                quadraticTo(w * 0.2f, h * 0.15f, w * 0.5f, h * 0.15f)
                lineTo(w * 0.65f, h * 0.15f)
                quadraticTo(w * 0.8f, h * 0.15f, w * 0.8f, h * 0.32f)
                lineTo(w * 0.8f, h * 0.45f)
                lineTo(w * 0.48f, h * 0.45f)
                quadraticTo(w * 0.35f, h * 0.45f, w * 0.35f, h * 0.58f)
                lineTo(w * 0.35f, h * 0.7f)
                lineTo(w * 0.2f, h * 0.7f)
                close()
            }
            drawPath(
                path = bluePath,
                color = Color(0xFF38BDF8)
            )
            // Python Eye (Blue snake)
            drawCircle(
                color = Color.White,
                radius = w * 0.045f,
                center = Offset(w * 0.38f, h * 0.27f)
            )

            // Lower Python Snake (Yellow)
            val yellowPath = Path().apply {
                moveTo(w * 0.8f, h * 0.5f)
                lineTo(w * 0.8f, h * 0.68f)
                quadraticTo(w * 0.8f, h * 0.85f, w * 0.5f, h * 0.85f)
                lineTo(w * 0.35f, h * 0.85f)
                quadraticTo(w * 0.2f, h * 0.85f, w * 0.2f, h * 0.68f)
                lineTo(w * 0.2f, h * 0.55f)
                lineTo(w * 0.52f, h * 0.55f)
                quadraticTo(w * 0.65f, h * 0.55f, w * 0.65f, h * 0.42f)
                lineTo(w * 0.65f, h * 0.3f)
                lineTo(w * 0.8f, h * 0.3f)
                close()
            }
            drawPath(
                path = yellowPath,
                color = Color(0xFFFFD43B)
            )
            // Python Eye (Yellow snake)
            drawCircle(
                color = Color(0xFF0F172A),
                radius = w * 0.045f,
                center = Offset(w * 0.62f, h * 0.73f)
            )
        }
    }
}

/**
 * Glowing Java Coding Icon
 */
@Composable
fun GlowingJavaIcon(
    glowAlpha: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(76.dp)
            .shadow(
                elevation = (12 * glowAlpha).dp,
                shape = CircleShape,
                ambientColor = Color(0xFFF97316),
                spotColor = Color(0xFFEF4444)
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.sweepGradient(
                    listOf(
                        Color(0xFFF97316).copy(alpha = glowAlpha),
                        Color(0xFFEF4444).copy(alpha = glowAlpha),
                        Color(0xFFF97316).copy(alpha = glowAlpha)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(46.dp)) {
            val w = size.width
            val h = size.height

            // Steaming Java Waves (Cyan/Orange heat waves)
            val wave1 = Path().apply {
                moveTo(w * 0.45f, h * 0.15f)
                cubicTo(w * 0.55f, h * 0.22f, w * 0.35f, h * 0.32f, w * 0.5f, h * 0.4f)
            }
            drawPath(
                path = wave1,
                color = Color(0xFF38BDF8),
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )

            val wave2 = Path().apply {
                moveTo(w * 0.6f, h * 0.18f)
                cubicTo(w * 0.7f, h * 0.25f, w * 0.5f, h * 0.34f, w * 0.65f, h * 0.42f)
            }
            drawPath(
                path = wave2,
                color = Color(0xFFF97316),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // Java Coffee Cup Body
            val cupPath = Path().apply {
                moveTo(w * 0.25f, h * 0.5f)
                lineTo(w * 0.72f, h * 0.5f)
                cubicTo(w * 0.72f, h * 0.75f, w * 0.65f, h * 0.82f, w * 0.5f, h * 0.82f)
                cubicTo(w * 0.35f, h * 0.82f, w * 0.25f, h * 0.75f, w * 0.25f, h * 0.5f)
                close()
            }
            drawPath(
                path = cupPath,
                color = Color(0xFFEA580C)
            )

            // Java Cup Handle
            val handlePath = Path().apply {
                moveTo(w * 0.72f, h * 0.54f)
                cubicTo(w * 0.88f, h * 0.54f, w * 0.88f, h * 0.72f, w * 0.7f, h * 0.72f)
            }
            drawPath(
                path = handlePath,
                color = Color(0xFFFDBA74),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // Java Saucer / Base Curve
            val saucerPath = Path().apply {
                moveTo(w * 0.2f, h * 0.88f)
                quadraticTo(w * 0.5f, h * 0.94f, w * 0.8f, h * 0.88f)
            }
            drawPath(
                path = saucerPath,
                color = Color(0xFF38BDF8),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )
        }
    }
}
