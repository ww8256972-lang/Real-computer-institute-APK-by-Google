package com.example.ui.screens.verification

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.QrCodeWidget
import com.example.data.repository.CertificateVerificationResult
import com.example.data.repository.InstituteRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicVerificationScreen(
    instituteRepository: InstituteRepository,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    var selectedMode by remember { mutableIntStateOf(0) } // 0 = Certificate, 1 = Student ID
    var queryText by remember { mutableStateOf("VERIFY-ADCA-8941") }
    var idQueryText by remember { mutableStateOf("STU-2026-001") }

    val coroutineScope = rememberCoroutineScope()
    var certResult by remember { mutableStateOf<CertificateVerificationResult?>(null) }
    var idResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isChecking by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Initial check for demo certificate
        certResult = instituteRepository.verifyDigitalCertificate(queryText)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Official Verification", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("QR & Public Registry Lookup", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("verification_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tab Selector
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedMode == 0,
                    onClick = { selectedMode = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("tab_verify_certificate")
                ) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Certificate")
                }
                SegmentedButton(
                    selected = selectedMode == 1,
                    onClick = { selectedMode = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("tab_verify_id_card")
                ) {
                    Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Student ID Card")
                }
            }

            if (selectedMode == 0) {
                // Certificate Verification Form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Verify Course Certificate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enter the Certificate Number (e.g. CERT-2026-8941) or QR Token printed on the physical/digital certificate.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = queryText,
                            onValueChange = { queryText = it },
                            label = { Text("Certificate Reference / QR Token") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("verify_cert_input"),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) }
                        )

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isChecking = true
                                    certResult = instituteRepository.verifyDigitalCertificate(queryText)
                                    isChecking = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_verify_cert_action"),
                            enabled = queryText.isNotBlank() && !isChecking
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isChecking) "Checking Registry..." else "Verify Certificate Status")
                        }
                    }
                }

                // Verification Result Card
                certResult?.let { result ->
                    val isRevoked = result.status == "CANCELLED"
                    val isFound = result.status != "NOT_FOUND"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                !isFound -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                isRevoked -> Color(0xFFFEF2F2)
                                else -> Color(0xFFF0FDF4)
                            }
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when {
                                !isFound -> MaterialTheme.colorScheme.error
                                isRevoked -> Color(0xFFDC2626)
                                else -> Color(0xFF16A34A)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            !isFound -> Color(0xFFFEE2E2)
                                            isRevoked -> Color(0xFFFEE2E2)
                                            else -> Color(0xFFDCFCE7)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        !isFound -> Icons.Default.GppBad
                                        isRevoked -> Icons.Default.Cancel
                                        else -> Icons.Default.Verified
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        !isFound -> Color(0xFFDC2626)
                                        isRevoked -> Color(0xFFDC2626)
                                        else -> Color(0xFF16A34A)
                                    },
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Text(
                                text = when {
                                    !isFound -> "Certificate Not Found"
                                    isRevoked -> "Certificate Revoked / Cancelled"
                                    else -> "Official Verified Certificate"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    !isFound -> MaterialTheme.colorScheme.error
                                    isRevoked -> Color(0xFFDC2626)
                                    else -> Color(0xFF15803D)
                                }
                            )

                            if (isFound) {
                                HorizontalDivider()
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Certificate Number:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                        Text(result.certificateNumber, fontWeight = FontWeight.Bold)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Student Name:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                        Text(result.studentName, fontWeight = FontWeight.Bold)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Roll Number:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                        Text(result.rollNo, fontWeight = FontWeight.Medium)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Course Completed:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                        Text(result.courseName, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Grade Awarded:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                        Text(result.grade, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Issue Date:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                        Text(result.issueDate)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Issued By:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                        Text(result.issuedBy)
                                    }

                                    if (isRevoked && !result.cancellationReason.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text("Cancellation Audit Reason:", fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C), fontSize = 12.sp)
                                                Text(result.cancellationReason, color = Color(0xFF991B1B), fontSize = 13.sp)
                                                if (result.cancelledBy != null) {
                                                    Text("Revoked by: ${result.cancelledBy}", fontSize = 11.sp, color = Color.Gray)
                                                }
                                            }
                                        }
                                    }
                                }

                                if (!isRevoked) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    QrCodeWidget(
                                        data = "EDUSYNC-CERT:${result.certificateNumber}:${result.rollNo}",
                                        sizeDp = 130.dp
                                    )
                                    Text("Cryptographic Verification Seal", fontSize = 11.sp, color = Color.Gray)
                                }
                            } else {
                                Text(
                                    text = result.cancellationReason ?: "No certificate matched this record in the database.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            } else {
                // Student ID Verification Form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Verify Student ID Card",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Scan or type the Student ID (e.g. STU-2026-001) or Roll Number to verify active enrollment status.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = idQueryText,
                            onValueChange = { idQueryText = it },
                            label = { Text("Student ID or Roll No") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("verify_id_input"),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) }
                        )

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isChecking = true
                                    idResult = instituteRepository.verifyStudentIdCard(idQueryText)
                                    isChecking = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_verify_id_action"),
                            enabled = idQueryText.isNotBlank() && !isChecking
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isChecking) "Checking Registry..." else "Verify Student ID")
                        }
                    }
                }

                idResult?.let { (isValid, message) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isValid) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isValid) Color(0xFF16A34A) else Color(0xFFDC2626)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (isValid) Color(0xFF16A34A) else Color(0xFFDC2626),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = if (isValid) "Valid Institute ID Card" else "Invalid or Suspended ID",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isValid) Color(0xFF15803D) else Color(0xFFDC2626)
                                )
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
