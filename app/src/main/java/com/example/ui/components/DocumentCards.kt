package com.example.ui.components

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ExamResultEntity
import com.example.data.local.entity.FeeRecordEntity
import com.example.data.local.entity.StudentEntity
import java.io.File
import java.io.FileOutputStream

/**
 * Professional Digital Student Identity Card with photo, course, batch, validity & QR verification code.
 */
@Composable
fun StudentIdCard(
    student: StudentEntity,
    modifier: Modifier = Modifier,
    onDownloadPdf: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("student_id_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            // Institute Card Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF0F172A), Color(0xFF1E3A8A), Color(0xFF2563EB))
                        )
                    )
                    .padding(vertical = 14.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = "Institute Logo",
                            tint = Color(0xFF1E3A8A),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "EDUSYNC COMPUTER ACADEMY",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "GOVT. REG. NO: ED-MH-2024-9982 • ISO 9001:2015",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF93C5FD)
                        )
                    }
                }
            }

            // Card Body
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.fullName.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "STUDENT ID: ${student.studentId}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF2563EB)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (student.rollNo.isNotBlank()) {
                        IdCardField(label = "Roll No", value = student.rollNo)
                    }
                    IdCardField(label = "Course", value = student.courseName)
                    IdCardField(label = "Batch Time", value = student.batch)
                    IdCardField(label = "Enrolled", value = student.enrollmentDate)
                    IdCardField(label = "Emergency Ph", value = student.phone)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Photo + QR Code column
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE2E8F0))
                            .border(1.5.dp, Color(0xFF2563EB), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Student Photo",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    QrCodeView(
                        dataString = "https://edusync.institute/verify?studentId=${student.studentId}",
                        size = 64.dp
                    )
                    Text(
                        text = "Scan to Verify",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B),
                        fontSize = 9.sp
                    )
                }
            }

            // Footer bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Valid Till: Academic Year 2026-27",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF475569)
                    )
                    TextButton(
                        onClick = onDownloadPdf,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF ID Card", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun IdCardField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF64748B),
            fontSize = 9.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = Color(0xFF1E293B)
        )
    }
}

/**
 * Examination Admit Card with exam details, roll number, center, reporting time, and instructions.
 */
@Composable
fun AdmitCard(
    student: StudentEntity,
    examName: String = "Quarterly Examination - 2026",
    examDate: String = "15-Nov-2026",
    examCenter: String = "Main Computer Lab 1, City Campus",
    reportingTime: String = "08:30 AM",
    modifier: Modifier = Modifier,
    onPrintPdf: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admit_card_view"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "OFFICIAL EXAMINATION ADMIT CARD",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "EDUSYNC COMPUTER EDUCATION BOARD",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2563EB)
                    )
                }
                QrCodeView(
                    dataString = "ADMIT-CARD:${student.studentId}:$examDate",
                    size = 54.dp
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Candidate Name: ${student.fullName}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Roll / Student ID: ${student.studentId}", fontSize = 13.sp, color = Color(0xFF2563EB))
                    Text("Course: ${student.courseName}", fontSize = 13.sp)
                    Text("Examination: $examName", fontSize = 13.sp)
                    Text("Date & Reporting: $examDate @ $reportingTime", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("Exam Venue: $examCenter", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = Color(0xFFFFFBEB),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Important Instructions:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF92400E))
                    Text("1. Carry this printed/digital Admit Card & Photo ID.", fontSize = 11.sp, color = Color(0xFF78350F))
                    Text("2. Mobile phones & smartwatches are strictly prohibited in the exam hall.", fontSize = 11.sp, color = Color(0xFF78350F))
                    Text("3. Late entry after 15 minutes of exam start is not permitted.", fontSize = 11.sp, color = Color(0xFF78350F))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onPrintPdf,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
            ) {
                Icon(Icons.Default.Print, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Print / Download Admit Card")
            }
        }
    }
}

/**
 * Official Certificate Layout with serial number, QR verification, and academic grade.
 */
@Composable
fun InstituteCertificateCard(
    student: StudentEntity,
    result: ExamResultEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("certificate_card_view"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF9F6)),
        border = BorderStroke(2.dp, Color(0xFFD4AF37)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = "Certificate Seal",
                tint = Color(0xFFD4AF37),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "EDUSYNC COMPUTER ACADEMY",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = Color(0xFF0F172A)
            )
            Text(
                text = "CERTIFICATE OF EXCELLENCE & COMPLETION",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFD4AF37)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "This is to certify that",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF475569)
            )
            Text(
                text = student.fullName,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                ),
                color = Color(0xFF1E3A8A)
            )
            Text(
                text = "Roll No: ${student.studentId}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "has successfully completed the prescribed course in\n${student.courseName}\nwith Grade '${result.grade}' (${result.percentage}%) and Rank #${result.rank}.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Certificate No: ${result.certificateNumber ?: "EDUSYNC-2026-CERT"}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Issued: ${result.certificateIssueDate ?: "2026-10-22"}", fontSize = 11.sp, color = Color(0xFF64748B))
                }
                QrCodeView(
                    dataString = "https://edusync.institute/verify?cert=${result.certificateNumber}",
                    size = 64.dp
                )
            }
        }
    }
}

/**
 * Official Fee Receipt with transaction ID, amount, date, payment method,
 * and clear distinction between Office Counter Manual Payment and Online Payment.
 */
@Composable
fun FeeReceiptCard(
    record: FeeRecordEntity,
    studentName: String = "",
    modifier: Modifier = Modifier,
    onDownload: () -> Unit = {}
) {
    val context = LocalContext.current
    val displayName = if (record.studentName.isNotBlank()) record.studentName else studentName
    val isManualCounter = record.paymentType == "MANUAL_COUNTER"
    var showReceiptModal by remember { mutableStateOf(false) }
    var generatedPdfPath by remember { mutableStateOf<String?>(null) }
    var pdfError by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("fee_receipt_item_${record.receiptNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, if (isManualCounter) Color(0xFFFBBF24).copy(alpha = 0.6f) else Color(0xFF60A5FA).copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Receipt # & Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RECEIPT #${record.receiptNumber}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Issued on ${record.paymentDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "PAID: ₹${"%.0f".format(record.amount)}",
                        color = Color(0xFF15803D),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Payment Source Distinction Badge (Requirement 7)
            Surface(
                color = if (isManualCounter) Color(0xFFFEF3C7) else Color(0xFFDBEAFE),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isManualCounter) Icons.Default.PointOfSale else Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = if (isManualCounter) Color(0xFFB45309) else Color(0xFF1D4ED8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isManualCounter) "OFFICE COUNTER MANUAL PAYMENT" else "ONLINE GATEWAY PAYMENT (VERIFIED)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isManualCounter) Color(0xFF92400E) else Color(0xFF1E40AF),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (displayName.isNotBlank()) {
                Text(
                    text = "Student: $displayName (${record.studentId})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            } else {
                Text(
                    text = "Student ID: ${record.studentId}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            if (record.courseName.isNotBlank() || record.batch.isNotBlank()) {
                Text(
                    text = "${record.courseName} • ${record.batch}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Mode: ${record.paymentMethod} • Txn ID: ${record.transactionId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (record.remarks.isNotBlank()) {
                Text(
                    text = "Remarks: ${record.remarks}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    val result = generateFeeReceiptPdfFile(context, record, displayName)
                    result.onSuccess { file ->
                        generatedPdfPath = file.absolutePath
                        pdfError = null
                    }.onFailure { err ->
                        pdfError = err.localizedMessage ?: "Failed to write PDF receipt"
                    }
                    showReceiptModal = true
                    onDownload()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("download_receipt_btn_${record.receiptNumber}"),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "PDF Receipt",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("View & Download PDF Receipt", style = MaterialTheme.typography.labelMedium)
            }
        }
    }

    if (showReceiptModal) {
        AlertDialog(
            onDismissRequest = { showReceiptModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = Color(0xFF15803D)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Official Fee Receipt PDF", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "Receipt #${record.receiptNumber} • Verified Record",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "EDUSYNC COMPUTER ACADEMY",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "ISO 9001:2015 Certified • Official Tuition Fee Receipt",
                                color = Color(0xFF2563EB),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            ReceiptDetailRow("Receipt No", record.receiptNumber)
                            ReceiptDetailRow("Payment Date", record.paymentDate)
                            ReceiptDetailRow("Student Name", displayName.ifBlank { record.studentId })
                            ReceiptDetailRow("Student ID", record.studentId)
                            if (record.courseName.isNotBlank()) {
                                ReceiptDetailRow("Course", record.courseName)
                            }
                            if (record.batch.isNotBlank()) {
                                ReceiptDetailRow("Batch", record.batch)
                            }
                            ReceiptDetailRow("Payment Mode", record.paymentMethod)
                            ReceiptDetailRow("Transaction ID", record.transactionId)
                            ReceiptDetailRow("Amount Paid", "₹${"%.2f".format(record.amount)}")
                            ReceiptDetailRow("Status", "PAID & VERIFIED")
                            if (record.remarks.isNotBlank()) {
                                ReceiptDetailRow("Remarks", record.remarks)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Authorized Signatory",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF334155)
                                    )
                                    Text(
                                        text = "Recorded by: ${record.recordedBy}",
                                        fontSize = 9.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                QrCodeView(
                                    dataString = "RECEIPT:${record.receiptNumber}:${record.studentId}:${record.amount}",
                                    size = 52.dp
                                )
                            }
                        }
                    }

                    if (generatedPdfPath != null) {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PDF downloaded & saved: ${record.receiptNumber}.pdf",
                                    fontSize = 11.sp,
                                    color = Color(0xFF14532D),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else if (pdfError != null) {
                        Text(
                            text = pdfError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showReceiptModal = false }) {
                    Icon(Icons.Default.DownloadDone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun ReceiptDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label:", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 11.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
    }
}

fun generateFeeReceiptPdfFile(
    context: Context,
    record: FeeRecordEntity,
    studentName: String
): Result<File> {
    return try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply { isAntiAlias = true }

        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("EDUSYNC COMPUTER ACADEMY", 40f, 50f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Official Tuition Fee Payment Receipt (ISO 9001:2015)", 40f, 72f, paint)
        canvas.drawLine(40f, 85f, 555f, 85f, paint)

        var y = 115f
        val lines = listOf(
            "Receipt Number: ${record.receiptNumber}",
            "Payment Date: ${record.paymentDate}",
            "Student Name: ${studentName.ifBlank { record.studentName.ifBlank { record.studentId } }}",
            "Student ID: ${record.studentId}",
            "Course: ${record.courseName}",
            "Batch: ${record.batch}",
            "Amount Paid: INR ${"%.2f".format(record.amount)}",
            "Payment Mode: ${record.paymentMethod} (${record.paymentType})",
            "Transaction ID: ${record.transactionId}",
            "Remarks: ${record.remarks}",
            "Recorded By: ${record.recordedBy}",
            "Verification Status: OFFICIAL VERIFIED RECEIPT"
        )
        for (line in lines) {
            canvas.drawText(line, 40f, y, paint)
            y += 26f
        }

        pdfDocument.finishPage(page)
        val dir = File(context.filesDir, "receipts").apply { mkdirs() }
        val safeName = record.receiptNumber.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val file = File(dir, "$safeName.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        Result.success(file)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

