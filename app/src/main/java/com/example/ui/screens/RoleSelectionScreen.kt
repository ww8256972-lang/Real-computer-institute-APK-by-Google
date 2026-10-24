package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Screen 1: Futuristic 3D Portal Selection Screen matching the reference design.
 * Features dark cyber background, glowing 3D academy emblem, "Learn • Grow • Succeed"
 * pill badge, 4 glossy 3D portal cards, and bottom verification pill button.
 */
@Composable
fun RoleSelectionScreen(
    onSelectStudent: () -> Unit,
    onSelectTeacher: () -> Unit,
    onSelectParent: () -> Unit,
    onSelectAdmin: () -> Unit,
    onOpenVerification: () -> Unit,
    onOpenAdminSettings: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("role_selection_screen")
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF060B18),
                        Color(0xFF0A1226),
                        Color(0xFF080D1D)
                    )
                )
            )
    ) {
        // Subtle cyber glow canvas in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x3300D2FF), Color(0x0000D2FF)),
                    center = Offset(size.width * 0.5f, size.height * 0.16f),
                    radius = size.width * 0.45f
                ),
                radius = size.width * 0.45f,
                center = Offset(size.width * 0.5f, size.height * 0.16f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x228A2387), Color(0x008A2387)),
                    center = Offset(size.width * 0.85f, size.height * 0.65f),
                    radius = size.width * 0.35f
                ),
                radius = size.width * 0.35f,
                center = Offset(size.width * 0.85f, size.height * 0.65f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // 3D Glowing Emblem
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .shadow(16.dp, CircleShape, spotColor = Color(0xFF00D2FF), ambientColor = Color(0xFF00D2FF))
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF00D2FF), Color(0xFF0055FF), Color(0xFF6B21A8))
                        )
                    )
                    .border(2.5.dp, Color(0x88FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "EduSync Logo",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            // Institute Title & Subtitle
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "EduSync Academy",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Computer Education & Training Institute",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8)
                )
            }

            // "Learn • Grow • Succeed" 3D Pill Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0x2600D2FF),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0x6600D2FF))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Learn • Grow • Succeed",
                        color = Color(0xFF7DD3FC),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 4 Glossy 3D Portal Cards
            PortalCard3D(
                title = "Student Portal",
                subtitle = "Course notes, assignments, attendance & fee receipts",
                icon = Icons.Default.School,
                gradient = listOf(Color(0xFF00C6FF), Color(0xFF0072FF)),
                glowBorder = Color(0x5500C6FF),
                testTag = "portal_student_card",
                onClick = onSelectStudent
            )

            PortalCard3D(
                title = "Teacher / Faculty Portal",
                subtitle = "Assigned batches, QR attendance & homework",
                icon = Icons.Default.Person,
                gradient = listOf(Color(0xFF00F260), Color(0xFF0575E6)),
                glowBorder = Color(0x5500F260),
                testTag = "portal_teacher_card",
                onClick = onSelectTeacher
            )

            PortalCard3D(
                title = "Parent & Guardian Portal",
                subtitle = "Ward attendance, fee ledger, results & notices",
                icon = Icons.Default.FamilyRestroom,
                gradient = listOf(Color(0xFFF7971E), Color(0xFFFFD200)),
                glowBorder = Color(0x55F7971E),
                testTag = "portal_parent_card",
                onClick = onSelectParent
            )

            PortalCard3D(
                title = "Admin Panel",
                subtitle = "Institute control, teachers, fees, timetables & certs",
                icon = Icons.Default.AdminPanelSettings,
                gradient = listOf(Color(0xFFB92B27), Color(0xFF1565C0)),
                glowBorder = Color(0x558A2387),
                testTag = "portal_admin_card",
                onClick = onSelectAdmin
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Bottom Verification Pill Button
            Surface(
                onClick = onOpenVerification,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("portal_verify_card"),
                shape = RoundedCornerShape(26.dp),
                color = Color(0x1F00D2FF),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0x8000D2FF))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Verify Student ID & Certificate QR",
                        color = Color(0xFFE0F2FE),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PortalCard3D(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: List<Color>,
    glowBorder: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xCC111827),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, glowBorder),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3D Gradient Icon Box
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(gradient))
                    .border(1.5.dp, Color(0x55FFFFFF), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Pill Indicator
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0x22FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Enter",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
