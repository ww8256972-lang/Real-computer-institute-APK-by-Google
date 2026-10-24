package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduSyncTopAppBar(
    title: String,
    subtitle: String? = null,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    showThemeToggle: Boolean = false,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("nav_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            if (showThemeToggle) {
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme"
                    )
                }
            }
            actions()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconBgColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("stat_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bg, fg) = when (status.uppercase()) {
        "ACTIVE" -> Color(0xFFDCFCE7) to Color(0xFF15803D)
        "BLOCKED" -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
        "PRESENT" -> Color(0xFFDCFCE7) to Color(0xFF15803D)
        "ABSENT" -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
        "GRADED" -> Color(0xFFEDE9FE) to Color(0xFF6D28D9)
        else -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status,
            color = fg,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * High-definition QR Code matrix simulation drawn via Canvas.
 * Generates official finder patterns and data dots.
 */
@Composable
fun QrCodeView(
    dataString: String,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    foregroundColor: Color = Color.Black,
    backgroundColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val cells = 21
            val cellSize = canvasWidth / cells

            // Finder patterns in 3 corners
            fun drawFinder(topRow: Int, leftCol: Int) {
                drawRect(
                    color = foregroundColor,
                    topLeft = Offset(leftCol * cellSize, topRow * cellSize),
                    size = Size(7 * cellSize, 7 * cellSize)
                )
                drawRect(
                    color = backgroundColor,
                    topLeft = Offset((leftCol + 1) * cellSize, (topRow + 1) * cellSize),
                    size = Size(5 * cellSize, 5 * cellSize)
                )
                drawRect(
                    color = foregroundColor,
                    topLeft = Offset((leftCol + 2) * cellSize, (topRow + 2) * cellSize),
                    size = Size(3 * cellSize, 3 * cellSize)
                )
            }

            drawFinder(0, 0)
            drawFinder(0, cells - 7)
            drawFinder(cells - 7, 0)

            // Pseudo-random deterministic data dots based on string hash
            val hash = dataString.hashCode()
            val random = java.util.Random(hash.toLong())

            for (r in 0 until cells) {
                for (c in 0 until cells) {
                    val inFinderTopLeft = r < 7 && c < 7
                    val inFinderTopRight = r < 7 && c >= cells - 7
                    val inFinderBottomLeft = r >= cells - 7 && c < 7
                    if (!inFinderTopLeft && !inFinderTopRight && !inFinderBottomLeft) {
                        if (random.nextBoolean()) {
                            drawRect(
                                color = foregroundColor,
                                topLeft = Offset(c * cellSize, r * cellSize),
                                size = Size(cellSize * 0.9f, cellSize * 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}
