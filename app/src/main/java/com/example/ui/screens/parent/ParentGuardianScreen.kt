package com.example.ui.screens.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.example.data.local.entity.FeeRecordEntity
import com.example.data.local.entity.ParentGuardianEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.TimetableEntity
import com.example.data.repository.InstituteRepository
import com.example.ui.components.EduSyncTopAppBar
import com.example.ui.components.StatCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentGuardianScreen(
    guardian: ParentGuardianEntity,
    instituteRepository: InstituteRepository,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    var student by remember { mutableStateOf<StudentEntity?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var feeRecords by remember { mutableStateOf<List<FeeRecordEntity>>(emptyList()) }
    var timetables by remember { mutableStateOf<List<TimetableEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(guardian.linkedStudentId, retryTrigger) {
        isLoading = true
        errorMessage = null
        try {
            val record = instituteRepository.getStudentById(guardian.linkedStudentId)
            if (record != null) {
                student = record
            } else {
                errorMessage = "Ward student profile (${guardian.linkedStudentId}) could not be found in active student records."
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load student data: ${e.localizedMessage ?: "Network or database timeout."}"
        } finally {
            isLoading = false
        }
    }

    val notices by instituteRepository.getNotices().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            EduSyncTopAppBar(
                title = "Parent Portal",
                subtitle = "${guardian.fullName} • Ward: ${student?.fullName ?: guardian.linkedStudentId}",
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                actions = {
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("parent_logout_btn")) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Ward Overview") },
                    modifier = Modifier.testTag("tab_parent_overview")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Payments, contentDescription = null) },
                    label = { Text("Fee Ledger") },
                    modifier = Modifier.testTag("tab_parent_fees")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text("Timetable") },
                    modifier = Modifier.testTag("tab_parent_timetable")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
                    label = { Text("Notices") },
                    modifier = Modifier.testTag("tab_parent_notices")
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(44.dp))
                        Text(
                            text = "Loading ward profile & records...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Unable to Load Profile",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = errorMessage ?: "Unknown error occurred.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { retryTrigger++ },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retry")
                            }
                        }
                    }
                }
            } else {
                val currentStudent = student
                if (currentStudent == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.PersonOff, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                                Text("No Student Record Linked", fontWeight = FontWeight.Bold)
                                Text("Linked ID: ${guardian.linkedStudentId}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Button(onClick = { retryTrigger++ }) {
                                    Text("Refresh")
                                }
                            }
                        }
                    }
                } else {
                    when (selectedTab) {
                        0 -> ParentWardOverviewTab(guardian, currentStudent, instituteRepository)
                        1 -> ParentFeeLedgerTab(currentStudent, instituteRepository)
                        2 -> ParentTimetableTab(currentStudent, instituteRepository)
                        3 -> ParentNoticesTab(notices)
                    }
                }
            }
        }
    }
}

@Composable
fun ParentWardOverviewTab(
    guardian: ParentGuardianEntity,
    student: StudentEntity,
    repository: InstituteRepository
) {
    val attendanceList by repository.getAttendanceForStudent(student.studentId).collectAsState(initial = emptyList())
    val results by repository.getResultsForStudent(student.studentId).collectAsState(initial = emptyList())

    val totalDays = attendanceList.size
    val presentCount = attendanceList.count { it.status == "PRESENT" }
    val attendancePct = if (totalDays > 0) (presentCount.toDouble() / totalDays) * 100.0 else 92.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Ward Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.fullName.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Roll: ${student.rollNo} • ID: ${student.studentId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = student.courseName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Batch: ${student.batch}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Attendance Quick Metric
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Attendance Record", fontWeight = FontWeight.Bold)
                    Text(
                        "${"%.1f".format(attendancePct)}%",
                        fontWeight = FontWeight.Bold,
                        color = if (attendancePct >= 75) Color(0xFF16A34A) else Color(0xFFDC2626)
                    )
                }
                LinearProgressIndicator(
                    progress = { (attendancePct / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (attendancePct >= 75) Color(0xFF16A34A) else Color(0xFFDC2626)
                )
                Text(
                    text = "Present in $presentCount of $totalDays recorded class sessions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Fee Summary Card
        val pendingFee = (student.totalCourseFee - student.feePaid).coerceAtLeast(0.0)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Fee Account Summary", fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Paid Amount",
                        value = "₹${"%.0f".format(student.feePaid)}",
                        icon = Icons.Default.CheckCircle,
                        iconBgColor = Color(0xFFDCFCE7),
                        iconColor = Color(0xFF15803D),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Pending Due",
                        value = "₹${"%.0f".format(pendingFee)}",
                        icon = Icons.Default.Warning,
                        iconBgColor = if (pendingFee > 0) Color(0xFFFEE2E2) else Color(0xFFF1F5F9),
                        iconColor = if (pendingFee > 0) Color(0xFFB91C1C) else Color(0xFF475569),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pendingFee > 0) {
                    Text(
                        text = "Next installment due date: ${student.feeDueDate}. Please visit counter for payment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB91C1C)
                    )
                }
            }
        }

        // Academic Results
        Text("Academic Results & Grades", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
        if (results.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = "Term exams underway. Results will be published here upon faculty evaluation.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            for (res in results) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(res.examName, fontWeight = FontWeight.Bold)
                            Text("Exam Date: ${res.examDate} • Rank: #${res.rank}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Grade ${res.grade}",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D),
                                fontSize = 16.sp
                            )
                            Text("${res.percentage}% (${res.obtainedMarks.toInt()}/${res.totalMarks.toInt()})", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParentFeeLedgerTab(
    student: StudentEntity,
    repository: InstituteRepository
) {
    val feeRecords by repository.getFeesForStudent(student.studentId).collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Official Fee Payment Receipts", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        if (feeRecords.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No fee payment transactions recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(feeRecords) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(record.receiptNumber, fontWeight = FontWeight.Bold)
                                Text("₹${"%.0f".format(record.amount)}", fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            }
                            Text("Date: ${record.paymentDate} • Mode: ${record.paymentMethod}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Txn Ref: ${record.transactionId} • ${record.remarks}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParentTimetableTab(
    student: StudentEntity,
    repository: InstituteRepository
) {
    val timetables by repository.getTimetablesByBatch(student.batch).collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Weekly Class Timetable (${student.batch})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        if (timetables.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Timetable schedule will appear once assigned by faculty.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(timetables) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    item.dayOfWeek.take(3).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.subject, fontWeight = FontWeight.Bold)
                                Text("${item.startTime} - ${item.endTime} • ${item.roomOrLab}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Instructor: ${item.teacherName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParentNoticesTab(
    notices: List<com.example.data.local.entity.NoticeEntity>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Institute Notices & Circulars", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(notices) { notice ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(notice.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Badge(containerColor = if (notice.priority == "URGENT") Color(0xFFDC2626) else MaterialTheme.colorScheme.primary) {
                                Text(notice.priority, fontSize = 10.sp)
                            }
                        }
                        Text(notice.content, style = MaterialTheme.typography.bodyMedium)
                        Text("Published: ${notice.date} • ${notice.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
