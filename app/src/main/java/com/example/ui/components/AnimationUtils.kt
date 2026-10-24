package com.example.ui.components

import android.os.SystemClock
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Authoritative time helper that tracks monotonic elapsed realtime offset
 * from initial server/session sync so manual device clock changes mid-session
 * cannot bypass exam start/end windows or 24h cooldowns.
 */
object ServerTimeAuthority {
    private val anchorWallTimeMs: Long = System.currentTimeMillis()
    private val anchorElapsedRealtimeMs: Long = try {
        SystemClock.elapsedRealtime()
    } catch (_: Throwable) {
        0L
    }
    @Volatile
    private var serverOffsetMs: Long = 0L

    fun currentTimeMillis(): Long {
        return try {
            val elapsed = SystemClock.elapsedRealtime() - anchorElapsedRealtimeMs
            if (elapsed > 0L && anchorElapsedRealtimeMs > 0L) {
                anchorWallTimeMs + elapsed + serverOffsetMs
            } else {
                System.currentTimeMillis() + serverOffsetMs
            }
        } catch (_: Throwable) {
            System.currentTimeMillis() + serverOffsetMs
        }
    }

    fun setServerOffsetForTesting(offsetMs: Long) {
        serverOffsetMs = offsetMs
    }
}

/**
 * Represents an active circular rotating/pulsing touch ripple triggered at exact touch coordinates.
 */
private data class CircularRippleEffect(
    val id: Long,
    val center: Offset,
    val startTimeMs: Long
)

/**
 * Checks whether system animations are enabled (respecting accessibility reduced-motion settings).
 */
@Composable
fun rememberAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
            scale > 0f
        } catch (_: Throwable) {
            true
        }
    }
}

/**
 * Global non-blocking touch overlay that reproduces the reference video's circular,
 * rotating and pulsing ring click animation at the exact touch position without consuming
 * pointer events or interfering with scrolling, typing, dialogs, or navigation.
 */
@Composable
fun CircularTouchRippleHost(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF00E5FF),
    secondaryColor: Color = Color(0xFF7C4DFF),
    content: @Composable () -> Unit
) {
    val animationsEnabled = rememberAnimationsEnabled()
    val activeRipples = remember { mutableStateListOf<CircularRippleEffect>() }
    var rippleIdCounter by remember { mutableLongStateOf(0L) }
    var frameTickMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(activeRipples.size, animationsEnabled) {
        if (!animationsEnabled) {
            activeRipples.clear()
            return@LaunchedEffect
        }
        while (activeRipples.isNotEmpty()) {
            withFrameMillis { frameTime ->
                frameTickMs = System.currentTimeMillis()
                activeRipples.removeAll { (frameTickMs - it.startTimeMs) > 650L }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(animationsEnabled) {
                if (!animationsEnabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(
                        requireUnconsumed = false,
                        pass = PointerEventPass.Initial
                    )
                    val now = System.currentTimeMillis()
                    rippleIdCounter += 1L
                    if (activeRipples.size >= 6) {
                        activeRipples.removeAt(0)
                    }
                    activeRipples.add(
                        CircularRippleEffect(
                            id = rippleIdCounter,
                            center = down.position,
                            startTimeMs = now
                        )
                    )
                }
            }
    ) {
        content()

        if (animationsEnabled && activeRipples.isNotEmpty()) {
            val currentMs = frameTickMs
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maxRadiusPx = 46.dp.toPx()
                val innerMaxRadiusPx = 30.dp.toPx()
                val ringStrokePx = 2.6.dp.toPx()

                activeRipples.forEach { ripple ->
                    val elapsed = (currentMs - ripple.startTimeMs).coerceIn(0L, 650L)
                    val progress = elapsed / 650f
                    val easedProgress = 1f - (1f - progress) * (1f - progress)
                    val alpha = (1f - progress).coerceIn(0f, 1f)

                    val outerRadius = 10.dp.toPx() + (maxRadiusPx - 10.dp.toPx()) * easedProgress
                    val innerRadius = 6.dp.toPx() + (innerMaxRadiusPx - 6.dp.toPx()) * progress
                    val rotationDeg = progress * 240f

                    // Soft radial pulse glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.24f * alpha),
                                secondaryColor.copy(alpha = 0.12f * alpha),
                                Color.Transparent
                            ),
                            center = ripple.center,
                            radius = outerRadius * 1.15f
                        ),
                        radius = outerRadius * 1.15f,
                        center = ripple.center
                    )

                    // Outer expanding circle
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.65f * alpha),
                        radius = outerRadius,
                        center = ripple.center,
                        style = Stroke(width = ringStrokePx * (1f - 0.3f * progress))
                    )

                    // Rotating segmented orbital arcs (matching reference video rotating ring)
                    rotate(degrees = rotationDeg, pivot = ripple.center) {
                        val arcRectTopLeft = Offset(
                            ripple.center.x - innerRadius,
                            ripple.center.y - innerRadius
                        )
                        val arcSize = Size(innerRadius * 2f, innerRadius * 2f)
                        for (i in 0 until 3) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.9f * alpha),
                                        secondaryColor.copy(alpha = 0.85f * alpha),
                                        primaryColor.copy(alpha = 0.9f * alpha)
                                    ),
                                    center = ripple.center
                                ),
                                startAngle = i * 120f,
                                sweepAngle = 68f,
                                useCenter = false,
                                topLeft = arcRectTopLeft,
                                size = arcSize,
                                style = Stroke(width = ringStrokePx, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Center core dot pulse
                    if (progress < 0.55f) {
                        val dotProgress = progress / 0.55f
                        drawCircle(
                            color = Color.White.copy(alpha = (1f - dotProgress) * 0.85f),
                            radius = 4.dp.toPx() * (1f - 0.4f * dotProgress),
                            center = ripple.center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modifier that adds a smooth pull-down and spring-release interaction to navigation items,
 * tabs, and interactive cards, inspired by the reference video's circular feedback.
 */
@Composable
fun Modifier.pullDownTabInteraction(
    selected: Boolean = false
): Modifier {
    val animationsEnabled = rememberAnimationsEnabled()
    var isPressed by remember { mutableStateOf(false) }
    var selectionBounce by remember { mutableStateOf(false) }

    LaunchedEffect(selected) {
        if (selected && animationsEnabled) {
            selectionBounce = true
            delay(220)
            selectionBounce = false
        }
    }

    val translateY by animateFloatAsState(
        targetValue = when {
            !animationsEnabled -> 0f
            isPressed -> 10f
            selectionBounce -> 6f
            else -> 0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pullDownTranslateY"
    )

    val scale by animateFloatAsState(
        targetValue = when {
            !animationsEnabled -> 1f
            isPressed -> 0.92f
            selectionBounce -> 1.06f
            selected -> 1.02f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pullDownScale"
    )

    return this
        .pointerInput(animationsEnabled) {
            if (!animationsEnabled) return@pointerInput
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                isPressed = true
                waitForUpOrCancellation(pass = PointerEventPass.Initial)
                isPressed = false
            }
        }
        .graphicsLayer {
            translationY = translateY
            scaleX = scale
            scaleY = scale
        }
}

/**
 * Animated circular rotating halo icon container for active bottom navigation tabs.
 */
@Composable
fun AnimatedTabIconHalo(
    selected: Boolean,
    accentColor: Color = Color(0xFF00E5FF),
    content: @Composable () -> Unit
) {
    val animationsEnabled = rememberAnimationsEnabled()
    val infiniteTransition = rememberInfiniteTransition(label = "tabHaloRotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "haloAngle"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.pullDownTabInteraction(selected = selected)
    ) {
        if (selected && animationsEnabled) {
            Canvas(modifier = Modifier.size(34.dp)) {
                val r = size.minDimension / 2f
                drawCircle(
                    color = accentColor.copy(alpha = 0.14f),
                    radius = r
                )
                rotate(degrees = angle) {
                    drawArc(
                        color = accentColor.copy(alpha = 0.65f),
                        startAngle = 0f,
                        sweepAngle = 110f,
                        useCenter = false,
                        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = accentColor.copy(alpha = 0.45f),
                        startAngle = 180f,
                        sweepAngle = 110f,
                        useCenter = false,
                        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
        content()
    }
}
