package com.example.ui.screens.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import com.example.data.local.entity.*
import com.example.data.repository.InstituteRepository
import com.example.data.repository.StudentPerformanceInsight
import com.example.ui.components.AnimatedTabIconHalo
import com.example.ui.components.EduSyncTopAppBar
import com.example.ui.components.ServerTimeAuthority
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.pullDownTabInteraction
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Teacher Batch Dashboard (Requirements 3 & 4)
 * Allows teachers to access their assigned batches, mark attendance, create assignments,
 * review submissions, and manage students in their batch.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherMainScreen(
    teacher: TeacherEntity,
    instituteRepository: InstituteRepository,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            EduSyncTopAppBar(
                title = "Teacher Dashboard",
                subtitle = "${teacher.fullName} • ${teacher.assignedBatches}",
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                actions = {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("teacher_logout_button")
                    ) {
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
                    icon = {
                        AnimatedTabIconHalo(selected = selectedTab == 0) {
                            Icon(Icons.Default.People, contentDescription = "Students")
                        }
                    },
                    label = { Text("Students", fontSize = 10.sp) },
                    modifier = Modifier.pullDownTabInteraction(selected = selectedTab == 0).testTag("tab_teacher_students")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        AnimatedTabIconHalo(selected = selectedTab == 1) {
                            Icon(Icons.Default.EventAvailable, contentDescription = "Attendance")
                        }
                    },
                    label = { Text("Attendance", fontSize = 10.sp) },
                    modifier = Modifier.pullDownTabInteraction(selected = selectedTab == 1).testTag("tab_teacher_attendance")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        AnimatedTabIconHalo(selected = selectedTab == 2) {
                            Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Assignments")
                        }
                    },
                    label = { Text("Homework", fontSize = 10.sp) },
                    modifier = Modifier.pullDownTabInteraction(selected = selectedTab == 2).testTag("tab_teacher_assignments")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        AnimatedTabIconHalo(selected = selectedTab == 3) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Library & E-Books")
                        }
                    },
                    label = { Text("Library", fontSize = 10.sp) },
                    modifier = Modifier.pullDownTabInteraction(selected = selectedTab == 3).testTag("tab_teacher_library")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = {
                        AnimatedTabIconHalo(selected = selectedTab == 4) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Timetable")
                        }
                    },
                    label = { Text("Timetable", fontSize = 10.sp) },
                    modifier = Modifier.pullDownTabInteraction(selected = selectedTab == 4).testTag("tab_teacher_timetable")
                )
                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    icon = {
                        AnimatedTabIconHalo(selected = selectedTab == 5) {
                            Icon(Icons.Default.Psychology, contentDescription = "Insights")
                        }
                    },
                    label = { Text("Insights", fontSize = 10.sp) },
                    modifier = Modifier.pullDownTabInteraction(selected = selectedTab == 5).testTag("tab_teacher_insights")
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> TeacherBatchStudentsTab(teacher, instituteRepository)
                1 -> TeacherAttendanceTab(teacher, instituteRepository)
                2 -> TeacherAssignmentsTab(teacher, instituteRepository)
                3 -> TeacherLibraryAndEbooksTab(teacher, instituteRepository)
                4 -> TeacherTimetableTab(teacher, instituteRepository)
                5 -> TeacherInsightsTab(teacher, instituteRepository)
            }
        }
    }
}

@Composable
fun TeacherBatchStudentsTab(
    teacher: TeacherEntity,
    repository: InstituteRepository
) {
    // Teachers access only their assigned batches
    val batchStudents by repository.getStudentsByBatch(teacher.assignedBatches).collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Teacher Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(teacher.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Specialization: ${teacher.specialization}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (teacher.isBatchAdmin) {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "BATCH ADMIN",
                                color = Color(0xFF15803D),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Assigned Batch: ${teacher.assignedBatches}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                Text("Total Enrolled Students: ${batchStudents.size}", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Students in Assigned Batch (${batchStudents.size})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))

        if (batchStudents.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No students currently assigned to this batch.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(batchStudents) { student ->
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("teacher_student_row_${student.studentId}"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0E7FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(student.fullName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFF4338CA))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(student.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("ID: ${student.studentId} • Ph: ${student.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(student.courseName, fontSize = 11.sp, color = Color.Gray)
                            }
                            StatusBadge(student.status)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherAttendanceTab(
    teacher: TeacherEntity,
    repository: InstituteRepository
) {
    val scope = rememberCoroutineScope()
    val batchStudents by repository.getStudentsByBatch(teacher.assignedBatches).collectAsState(initial = emptyList())
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var attendanceState by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var activeQrSession by remember { mutableStateOf<QRAttendanceSessionEntity?>(null) }
    var showQrModal by remember { mutableStateOf(false) }
    var alertMsg by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(batchStudents) {
        if (attendanceState.isEmpty() && batchStudents.isNotEmpty()) {
            attendanceState = batchStudents.associate { it.studentId to true }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Daily Batch Attendance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Date: $today • Batch: ${teacher.assignedBatches}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = {
                        scope.launch {
                            val session = repository.createQrAttendanceSession(
                                batchName = teacher.assignedBatches,
                                courseId = "CRS_ADCA",
                                teacherId = teacher.teacherId,
                                teacherName = teacher.fullName,
                                subject = "Class Session",
                                validityMinutes = 15
                            )
                            activeQrSession = session
                            showQrModal = true
                        }
                    },
                    modifier = Modifier.testTag("teacher_qr_attendance_btn")
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Live QR")
                }

                Button(
                    onClick = {
                        isSaving = true
                        scope.launch {
                            val records = batchStudents.map { s ->
                                val isPresent = attendanceState[s.studentId] ?: true
                                AttendanceEntity(
                                    studentId = s.studentId,
                                    date = today,
                                    status = if (isPresent) "PRESENT" else "ABSENT",
                                    markedByTeacherId = teacher.teacherId
                                )
                            }
                        repository.markBatchAttendance(records)
                        isSaving = false
                        alertMsg = "Attendance successfully saved for ${records.size} students in ${teacher.assignedBatches}."
                    }
                },
                enabled = !isSaving && batchStudents.isNotEmpty(),
                modifier = Modifier.testTag("teacher_save_attendance_btn")
            ) {
                Text("Save Attendance")
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

        if (batchStudents.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No students in this batch to mark attendance.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(batchStudents) { student ->
                    val isPresent = attendanceState[student.studentId] ?: true
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("att_toggle_${student.studentId}"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(student.fullName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text("ID: ${student.studentId}", fontSize = 12.sp, color = Color.Gray)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilterChip(
                                    selected = isPresent,
                                    onClick = {
                                        attendanceState = attendanceState + (student.studentId to true)
                                    },
                                    label = { Text("Present") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFDCFCE7),
                                        selectedLabelColor = Color(0xFF15803D)
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                FilterChip(
                                    selected = !isPresent,
                                    onClick = {
                                        attendanceState = attendanceState + (student.studentId to false)
                                    },
                                    label = { Text("Absent") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFEE2E2),
                                        selectedLabelColor = Color(0xFFB91C1C)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showQrModal && activeQrSession != null) {
        val session = activeQrSession!!
        AlertDialog(
            onDismissRequest = { showQrModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCode2, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Live QR Attendance")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Batch: ${session.batchName}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    QrCodeWidget(
                        data = session.sessionCode,
                        sizeDp = 180.dp
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "SESSION CODE: ${session.sessionCode}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    Text(
                        text = "Students scan this QR code or enter the 6-character code in their Student Portal to mark attendance. Token expires in 15 minutes.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showQrModal = false }) { Text("Close Scanner") }
            }
        )
    }

    if (alertMsg != null) {
        AlertDialog(
            onDismissRequest = { alertMsg = null },
            title = { Text("Attendance Status") },
            text = { Text(alertMsg!!) },
            confirmButton = { TextButton(onClick = { alertMsg = null }) { Text("OK") } }
        )
    }
}

@Composable
fun TeacherAssignmentsTab(
    teacher: TeacherEntity,
    repository: InstituteRepository
) {
    val scope = rememberCoroutineScope()
    val batchAssignments by repository.getAssignmentsForBatch(teacher.assignedBatches).collectAsState(initial = emptyList())
    val allSubmissions by repository.getAllSubmissions().collectAsState(initial = emptyList())
    val batchStudents by repository.getStudentsByBatch(teacher.assignedBatches).collectAsState(initial = emptyList())

    var showCreateAssignmentDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("2026-11-10") }
    var maxMarks by remember { mutableStateOf("50") }
    var attachmentUri by remember { mutableStateOf("") }

    var showUploadStudyMaterialDialog by remember { mutableStateOf(false) }
    var matTitle by remember { mutableStateOf("") }
    var matModule by remember { mutableStateOf("Module 1: Core Concepts") }
    var matType by remember { mutableStateOf("PDF_NOTE") }
    var matContent by remember { mutableStateOf("") }

    var showScheduleExamDialog by remember { mutableStateOf(false) }
    var examTitle by remember { mutableStateOf("") }
    var examDuration by remember { mutableStateOf("60") }

    var gradingSubmission by remember { mutableStateOf<SubmissionEntity?>(null) }
    var gradeMarks by remember { mutableStateOf("45") }
    var feedback by remember { mutableStateOf("Well done!") }
    var alertMsg by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Assignments, Study PDFs & Exams", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Assigned to: ${teacher.assignedBatches}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Button(
                onClick = { showCreateAssignmentDialog = true },
                modifier = Modifier.testTag("teacher_create_assignment_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Homework")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { showUploadStudyMaterialDialog = true },
                modifier = Modifier.weight(1f).testTag("teacher_upload_pdf_btn")
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Study PDF", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = { showScheduleExamDialog = true },
                modifier = Modifier.weight(1f).testTag("teacher_schedule_exam_btn")
            ) {
                Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Schedule 50-MCQ Exam", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (batchAssignments.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No assignments sent to this batch yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(batchAssignments) { assign ->
                    val subs = allSubmissions.filter { it.assignmentId == assign.id }
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("teacher_assign_card_${assign.id}"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(assign.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                Surface(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(8.dp)) {
                                    Text("Max: ${assign.maxMarks} Marks", color = Color(0xFF1D4ED8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(assign.instructions, style = MaterialTheme.typography.bodySmall)
                            if (assign.attachmentUri.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(color = Color(0xFFF0FDF4), shape = RoundedCornerShape(6.dp)) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Attached Resource: ${assign.attachmentUri}", fontSize = 11.sp, color = Color(0xFF15803D), fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Due Date: ${assign.dueDate} • Submissions Received: ${subs.size}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

                            if (subs.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Student Submissions:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                subs.forEach { sub ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Student: ${sub.studentId}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            Text("Answer: ${sub.submissionContent.take(40)}...", fontSize = 11.sp, color = Color.Gray)
                                            if (sub.fileUri.isNotBlank()) {
                                                Text("Photo/File: ${sub.fileUri.substringAfterLast("/")}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        if (sub.status == "GRADED") {
                                            StatusBadge("Score: ${sub.marksObtained}/${assign.maxMarks}")
                                        } else {
                                            Button(
                                                onClick = { gradingSubmission = sub },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("Grade", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateAssignmentDialog) {
        AlertDialog(
            onDismissRequest = { showCreateAssignmentDialog = false },
            title = { Text("Create Batch Assignment") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Assignment Title *") }, modifier = Modifier.fillMaxWidth().testTag("assign_title_in"))
                    OutlinedTextField(value = instructions, onValueChange = { instructions = it }, label = { Text("Instructions *") }, modifier = Modifier.fillMaxWidth().height(90.dp))
                    OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Due Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = maxMarks, onValueChange = { maxMarks = it }, label = { Text("Maximum Marks") }, modifier = Modifier.fillMaxWidth())

                    Text("Attach Reference File (PDF / Image / Doc):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AssistChip(
                            onClick = { attachmentUri = "Lab_Worksheet_${System.currentTimeMillis() % 1000}.pdf" },
                            label = { Text("+ PDF Worksheet", fontSize = 11.sp) }
                        )
                        AssistChip(
                            onClick = { attachmentUri = "Diagram_Reference_${System.currentTimeMillis() % 1000}.png" },
                            label = { Text("+ Image Diagram", fontSize = 11.sp) }
                        )
                    }
                    if (attachmentUri.isNotBlank()) {
                        Text("Attached: $attachmentUri", fontSize = 11.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            scope.launch {
                                repository.addAssignment(
                                    AssignmentEntity(
                                        courseId = "CRS_ADCA",
                                        title = title.trim(),
                                        instructions = instructions.trim(),
                                        dueDate = dueDate.trim(),
                                        maxMarks = maxMarks.toIntOrNull() ?: 50,
                                        batchName = teacher.assignedBatches,
                                        createdById = teacher.teacherId,
                                        createdByName = teacher.fullName,
                                        attachmentUri = attachmentUri
                                    )
                                )
                                showCreateAssignmentDialog = false
                                title = ""
                                instructions = ""
                                attachmentUri = ""
                                alertMsg = "Assignment sent to all students in ${teacher.assignedBatches}."
                            }
                        }
                    }
                ) {
                    Text("Send Assignment")
                }
            },
            dismissButton = { TextButton(onClick = { showCreateAssignmentDialog = false }) { Text("Cancel") } }
        )
    }

    if (showUploadStudyMaterialDialog) {
        AlertDialog(
            onDismissRequest = { showUploadStudyMaterialDialog = false },
            title = { Text("Share Study PDF / E-Book with Students", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = matTitle,
                        onValueChange = { matTitle = it },
                        label = { Text("Document / PDF Title *") },
                        placeholder = { Text("e.g. Tally Prime GST Voucher Handbook.pdf") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = matModule,
                        onValueChange = { matModule = it },
                        label = { Text("Module Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = matType == "PDF_NOTE",
                            onClick = { matType = "PDF_NOTE" },
                            label = { Text("PDF Note") }
                        )
                        FilterChip(
                            selected = matType == "VIDEO",
                            onClick = { matType = "VIDEO" },
                            label = { Text("Video Lecture") }
                        )
                    }
                    OutlinedTextField(
                        value = matContent,
                        onValueChange = { matContent = it },
                        label = { Text("Summary & Study Notes *") },
                        modifier = Modifier.fillMaxWidth().height(90.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (matTitle.isNotBlank()) {
                            scope.launch {
                                repository.addStudyMaterial(
                                    StudyMaterialEntity(
                                        courseId = "CRS_ADCA",
                                        title = matTitle.trim(),
                                        description = "${matModule.trim().ifBlank { "Module 1" }} • ${matContent.trim().ifBlank { "Shared by ${teacher.fullName} for ${teacher.assignedBatches}." }}",
                                        type = if (matType == "PDF_NOTE") "PDF_NOTES" else "VIDEO_CLASS",
                                        contentUrl = "https://edusync.academy/materials/${matTitle.trim().replace(" ", "_")}",
                                        fileSize = if (matType == "PDF_NOTE") "18 Pages • PDF" else "35 mins • HD Video"
                                    )
                                )
                                showUploadStudyMaterialDialog = false
                                matTitle = ""
                                matContent = ""
                                alertMsg = "Study material published to student Learning portals."
                            }
                        }
                    }
                ) { Text("Upload & Share") }
            },
            dismissButton = { TextButton(onClick = { showUploadStudyMaterialDialog = false }) { Text("Cancel") } }
        )
    }

    if (showScheduleExamDialog) {
        var examStartDelayMins by remember { mutableStateOf("0") }
        var examCourseId by remember { mutableStateOf("CRS_ADCA") }
        AlertDialog(
            onDismissRequest = { showScheduleExamDialog = false },
            title = { Text("Schedule 50-MCQ Batch Exam", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Schedules a 50-MCQ timed online exam for students in ${teacher.assignedBatches}. Opens at the scheduled time, closes automatically when duration ends, and publishes results 5 minutes after completion.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = examTitle,
                        onValueChange = { examTitle = it },
                        label = { Text("Exam Title *") },
                        placeholder = { Text("e.g. ADCA Mid-Term Practical Assessment") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("CRS_ADCA" to "ADCA", "CRS_DCA" to "DCA", "CRS_TALLY" to "Tally", "CRS_PYTHON" to "Python").forEach { (cid, cname) ->
                            FilterChip(
                                selected = examCourseId == cid,
                                onClick = { examCourseId = cid },
                                label = { Text(cname, fontSize = 11.sp) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = examDuration,
                            onValueChange = { examDuration = it },
                            label = { Text("Duration (Mins)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = examStartDelayMins,
                            onValueChange = { examStartDelayMins = it },
                            label = { Text("Starts in (Mins, 0=Live)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (examTitle.isNotBlank()) {
                            scope.launch {
                                val targetIds = if (batchStudents.isNotEmpty()) {
                                    batchStudents.map { it.studentId }
                                } else {
                                    listOf("EDU-2026-1001", "EDU-2026-1002", "EDU-2026-1003")
                                }
                                val now = ServerTimeAuthority.currentTimeMillis()
                                val dur = (examDuration.toIntOrNull() ?: 60).coerceAtLeast(5)
                                val delayMins = (examStartDelayMins.toLongOrNull() ?: 0L).coerceAtLeast(0L)
                                val startMs = if (delayMins == 0L) now - 10_000L else now + delayMins * 60_000L
                                val courseLabel = when (examCourseId) {
                                    "CRS_DCA" -> "Diploma in Computer Applications (DCA)"
                                    "CRS_TALLY" -> "Tally Prime with GST & Taxation"
                                    "CRS_PYTHON" -> "Python & Data Science Foundation"
                                    else -> "Advance Diploma in Computer Applications (ADCA)"
                                }
                                repository.saveScheduledExam(
                                    ScheduledExamEntity(
                                        examId = "EXM-TCH-${now % 100000}",
                                        title = examTitle.trim(),
                                        courseId = examCourseId,
                                        courseName = courseLabel,
                                        scheduledStartTime = startMs,
                                        durationMinutes = dur,
                                        totalQuestions = 50,
                                        passingMarks = 20,
                                        questionsJson = com.example.core.ExamQuestionBank.serializeQuestions(
                                            com.example.core.ExamQuestionBank.getDefault50Questions()
                                        ),
                                        assignedStudentIds = com.example.core.ExamQuestionBank.serializeAllowedStudentIds(targetIds),
                                        status = if (delayMins == 0L) "LIVE" else "SCHEDULED"
                                    )
                                )
                                showScheduleExamDialog = false
                                examTitle = ""
                                alertMsg = "50-MCQ Exam scheduled and published for ${targetIds.size} students."
                            }
                        }
                    }
                ) { Text("Publish Exam") }
            },
            dismissButton = { TextButton(onClick = { showScheduleExamDialog = false }) { Text("Cancel") } }
        )
    }

    if (gradingSubmission != null) {
        val s = gradingSubmission!!
        AlertDialog(
            onDismissRequest = { gradingSubmission = null },
            title = { Text("Grade Student ${s.studentId}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Submission: ${s.submissionContent}", style = MaterialTheme.typography.bodySmall)
                    if (s.fileUri.isNotBlank()) {
                        Text("Attached File/Photo: ${s.fileUri}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    OutlinedTextField(value = gradeMarks, onValueChange = { gradeMarks = it }, label = { Text("Marks Obtained") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = feedback, onValueChange = { feedback = it }, label = { Text("Teacher Feedback") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.gradeSubmission(
                                id = s.id,
                                marks = gradeMarks.toIntOrNull() ?: 45,
                                feedback = feedback.trim(),
                                teacherId = teacher.teacherId
                            )
                            gradingSubmission = null
                            alertMsg = "Graded student ${s.studentId} successfully."
                        }
                    }
                ) { Text("Submit Grade") }
            },
            dismissButton = { TextButton(onClick = { gradingSubmission = null }) { Text("Cancel") } }
        )
    }

    if (alertMsg != null) {
        AlertDialog(
            onDismissRequest = { alertMsg = null },
            title = { Text("Notice") },
            text = { Text(alertMsg!!) },
            confirmButton = { TextButton(onClick = { alertMsg = null }) { Text("OK") } }
        )
    }
}

@Composable
fun TeacherTimetableTab(
    teacher: TeacherEntity,
    repository: InstituteRepository
) {
    val timetables by repository.getTimetablesByTeacher(teacher.teacherId).collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Your Teaching Schedule (${timetables.size} classes)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        if (timetables.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No specific classes assigned to your ID yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(timetables) { item ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.dayOfWeek.take(3).uppercase(), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.subject, fontWeight = FontWeight.Bold)
                                Text("${item.startTime} - ${item.endTime} • ${item.roomOrLab}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Batch: ${item.batchName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherInsightsTab(
    teacher: TeacherEntity,
    repository: InstituteRepository
) {
    val scope = rememberCoroutineScope()
    val batchStudents by repository.getStudentsByBatch(teacher.assignedBatches).collectAsState(initial = emptyList())
    var selectedStudentId by remember { mutableStateOf(batchStudents.firstOrNull()?.studentId ?: "EDU-2026-1001") }
    var insight by remember { mutableStateOf<StudentPerformanceInsight?>(null) }

    LaunchedEffect(batchStudents) {
        if (batchStudents.isNotEmpty()) {
            selectedStudentId = batchStudents.first().studentId
        }
        insight = repository.getStudentPerformanceInsights(selectedStudentId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Batch Performance Insights", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Student from ${teacher.assignedBatches}:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

                var dropdownExpanded by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { dropdownExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val current = batchStudents.firstOrNull { it.studentId == selectedStudentId }
                    Text(if (current != null) "${current.fullName} (${current.studentId})" else "Student ($selectedStudentId)")
                }
                DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                    batchStudents.forEach { s ->
                        DropdownMenuItem(
                            text = { Text("${s.fullName} (${s.studentId})") },
                            onClick = {
                                selectedStudentId = s.studentId
                                dropdownExpanded = false
                                scope.launch {
                                    insight = repository.getStudentPerformanceInsights(s.studentId)
                                }
                            }
                        )
                    }
                }
            }
        }

        insight?.let { res ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(
                                res.studentName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Academic Health: ${res.academicHealth}",
                                color = if (res.academicHealth == "EXCELLENT") Color(0xFF16A34A) else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(8.dp)) {
                            Text(
                                "${"%.1f".format(res.attendancePercentage)}% Att.",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    HorizontalDivider()
                    Text("Observed Academic Strengths:", fontWeight = FontWeight.Bold, color = Color(0xFF16A34A), fontSize = 13.sp)
                    res.subjectStrengths.forEach { s ->
                        Text("✓ $s", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Text("Areas Needing Guidance:", fontWeight = FontWeight.Bold, color = Color(0xFFD97706), fontSize = 13.sp)
                    res.subjectWeaknesses.forEach { w ->
                        Text("• $w", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    HorizontalDivider()
                    Text("Faculty Recommendation & Plan:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    Text(res.teacherRecommendations, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

/**
 * Requirement 4 & 10: Dedicated Teacher Portal — Library, E-Books & Video Lectures Section
 * Allows authorized teachers to upload, organize, manage, and share PDFs, E-Books, and Video Lectures
 * with assigned courses or batches while preserving existing learning materials and downloads.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherLibraryAndEbooksTab(
    teacher: TeacherEntity,
    repository: InstituteRepository
) {
    val scope = rememberCoroutineScope()
    val allBooks by repository.getAllBooks().collectAsState(initial = emptyList())
    val allMaterials by repository.getAllStudyMaterials().collectAsState(initial = emptyList())
    val courses by repository.getAllCourses().collectAsState(initial = emptyList())

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: E-Books & Manuals, 1: PDF Notes, 2: Video Lectures
    var showUploadModal by remember { mutableStateOf(false) }
    var uploadCategory by remember { mutableStateOf("E-BOOK") } // E-BOOK, PDF_NOTES, VIDEO_CLASS
    var uploadTitle by remember { mutableStateOf("") }
    var uploadCourseId by remember { mutableStateOf("CRS_ADCA") }
    var uploadSubject by remember { mutableStateOf(teacher.specialization.ifBlank { "Computer Applications" }) }
    var uploadSizeOrDuration by remember { mutableStateOf("4.2 MB") }
    var uploadSummary by remember { mutableStateOf("") }
    var editingBook by remember { mutableStateOf<DigitalBookEntity?>(null) }
    var viewingBook by remember { mutableStateOf<DigitalBookEntity?>(null) }
    var noticeMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().testTag("teacher_library_header_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Faculty Library, E-Books & Videos",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Upload, organize & share E-Books, PDFs & Video Lectures for ${teacher.assignedBatches}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Button(
                    onClick = {
                        uploadTitle = ""
                        uploadSummary = ""
                        uploadCategory = when (selectedSubTab) {
                            1 -> "PDF_NOTES"
                            2 -> "VIDEO_CLASS"
                            else -> "E-BOOK"
                        }
                        uploadSizeOrDuration = if (selectedSubTab == 2) "40 Mins • HD" else "3.8 MB"
                        showUploadModal = true
                    },
                    modifier = Modifier.testTag("teacher_library_upload_btn")
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload", fontSize = 12.sp)
                }
            }
        }

        PrimaryTabRow(selectedTabIndex = selectedSubTab) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("E-Books (${allBooks.size})", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("PDF Notes (${allMaterials.count { it.type != "VIDEO_CLASS" }})", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text("Video Lectures (${allMaterials.count { it.type == "VIDEO_CLASS" }})", fontSize = 12.sp) }
            )
        }

        when (selectedSubTab) {
            0 -> {
                if (allBooks.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No E-Books uploaded yet. Tap 'Upload' to publish an E-Book.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(allBooks) { book ->
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("teacher_ebook_card_${book.id}"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(28.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(book.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                                Text("${book.author} • Course: ${book.courseId.ifBlank { "All" }} • ${book.fileSize}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        Surface(color = Color(0xFFEDE9FE), shape = RoundedCornerShape(6.dp)) {
                                            Text(book.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6D28D9), modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                        }
                                    }
                                    Text(book.description, style = MaterialTheme.typography.bodySmall)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Downloads: ${book.downloadCount} • Shared with authorized students", fontSize = 11.sp, color = Color.Gray)
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            OutlinedButton(
                                                onClick = { viewingBook = book },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text("View", fontSize = 11.sp)
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    editingBook = book
                                                    uploadTitle = book.title
                                                    uploadSummary = book.description
                                                    uploadCategory = book.category
                                                    uploadCourseId = book.courseId.ifBlank { "CRS_ADCA" }
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text("Edit", fontSize = 11.sp)
                                            }
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        repository.deleteBook(book.id)
                                                        noticeMsg = "Removed '${book.title}' from Library."
                                                    }
                                                },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete E-Book", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                val pdfs = allMaterials.filter { it.type != "VIDEO_CLASS" }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(pdfs) { pdf ->
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("teacher_pdf_card_${pdf.id}"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(34.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pdf.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text("Course: ${pdf.courseId} • ${pdf.fileSize ?: "2.4 MB"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    Text(pdf.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusBadge("PUBLISHED")
                            }
                        }
                    }
                }
            }
            2 -> {
                val videos = allMaterials.filter { it.type == "VIDEO_CLASS" }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(videos) { video ->
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("teacher_video_card_${video.id}"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.OndemandVideo, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(video.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text("Course: ${video.courseId} • Duration: ${video.fileSize ?: "40 Mins • HD"}", fontSize = 11.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold)
                                    Text(video.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusBadge("IN-APP VIDEO")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUploadModal) {
        val courseOptions = if (courses.isNotEmpty()) {
            courses.map { it.id to it.name }
        } else {
            listOf(
                "CRS_ADCA" to "ADCA (1 Year Diploma)",
                "CRS_DCA" to "DCA (6 Months)",
                "CRS_TALLY" to "Tally Prime & GST",
                "CRS_PYTHON" to "Python & Data Science",
                "CRS_WEB" to "Full-Stack Web Dev"
            )
        }
        AlertDialog(
            onDismissRequest = { showUploadModal = false },
            title = { Text("Upload E-Book, PDF or Video Lecture", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "E-BOOK" to "E-Book",
                            "PDF_NOTES" to "PDF Note",
                            "VIDEO_CLASS" to "Video Lecture"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = uploadCategory == key,
                                onClick = {
                                    uploadCategory = key
                                    uploadSizeOrDuration = if (key == "VIDEO_CLASS") "45 Mins • HD" else "4.2 MB • PDF"
                                },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uploadTitle,
                        onValueChange = { uploadTitle = it },
                        label = { Text("Title *") },
                        placeholder = { Text("e.g. Complete Tally Prime & GST E-Book") },
                        modifier = Modifier.fillMaxWidth().testTag("teacher_upload_title_input"),
                        singleLine = true
                    )

                    Text("Authorized Target Course:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        courseOptions.take(4).forEach { (cid, cname) ->
                            FilterChip(
                                selected = uploadCourseId == cid,
                                onClick = { uploadCourseId = cid },
                                label = { Text(cid.removePrefix("CRS_"), fontSize = 10.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uploadSubject,
                        onValueChange = { uploadSubject = it },
                        label = { Text("Subject / Module") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uploadSizeOrDuration,
                        onValueChange = { uploadSizeOrDuration = it },
                        label = { Text(if (uploadCategory == "VIDEO_CLASS") "Video Duration (e.g. 45 Mins • HD)" else "File Size (e.g. 4.2 MB)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uploadSummary,
                        onValueChange = { uploadSummary = it },
                        label = { Text("Summary / Chapters / Lecture Notes *") },
                        modifier = Modifier.fillMaxWidth().height(90.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (uploadTitle.isNotBlank()) {
                            scope.launch {
                                val desc = uploadSummary.trim().ifBlank {
                                    "Published by ${teacher.fullName} for ${teacher.assignedBatches} (${uploadSubject.trim()})."
                                }
                                if (uploadCategory == "E-BOOK") {
                                    repository.addBook(
                                        DigitalBookEntity(
                                            title = uploadTitle.trim(),
                                            author = teacher.fullName,
                                            courseId = uploadCourseId,
                                            subject = uploadSubject.trim(),
                                            category = "E-BOOK",
                                            fileSize = uploadSizeOrDuration.trim().ifBlank { "4.2 MB" },
                                            description = desc,
                                            readTimeMinutes = 40,
                                            isDownloaded = false,
                                            downloadCount = 0,
                                            contentSummary = desc
                                        )
                                    )
                                } else {
                                    repository.addStudyMaterial(
                                        StudyMaterialEntity(
                                            courseId = uploadCourseId,
                                            title = uploadTitle.trim(),
                                            description = desc,
                                            type = if (uploadCategory == "VIDEO_CLASS") "VIDEO_CLASS" else "PDF_NOTES",
                                            contentUrl = "https://edusync.academy/materials/${uploadTitle.trim().replace(" ", "_")}",
                                            fileSize = uploadSizeOrDuration.trim().ifBlank {
                                                if (uploadCategory == "VIDEO_CLASS") "45 Mins • HD" else "3.5 MB • PDF"
                                            }
                                        )
                                    )
                                }
                                showUploadModal = false
                                noticeMsg = "'${uploadTitle.trim()}' published to authorized students in $uploadCourseId."
                            }
                        }
                    },
                    modifier = Modifier.testTag("teacher_confirm_library_upload_btn")
                ) {
                    Text("Upload & Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadModal = false }) { Text("Cancel") }
            }
        )
    }

    editingBook?.let { b ->
        AlertDialog(
            onDismissRequest = { editingBook = null },
            title = { Text("Edit E-Book Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uploadTitle,
                        onValueChange = { uploadTitle = it },
                        label = { Text("E-Book Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uploadSummary,
                        onValueChange = { uploadSummary = it },
                        label = { Text("Description & Summary") },
                        modifier = Modifier.fillMaxWidth().height(90.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (uploadTitle.isNotBlank()) {
                            scope.launch {
                                repository.updateBook(
                                    b.copy(
                                        title = uploadTitle.trim(),
                                        description = uploadSummary.trim(),
                                        contentSummary = uploadSummary.trim()
                                    )
                                )
                                editingBook = null
                                noticeMsg = "E-Book updated."
                            }
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingBook = null }) { Text("Cancel") }
            }
        )
    }

    viewingBook?.let { b ->
        AlertDialog(
            onDismissRequest = { viewingBook = null },
            title = { Text(b.title, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Author: ${b.author} • Course: ${b.courseId} • Size: ${b.fileSize}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    HorizontalDivider()
                    Text(b.description, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { viewingBook = null }) { Text("Close") }
            }
        )
    }

    if (noticeMsg != null) {
        AlertDialog(
            onDismissRequest = { noticeMsg = null },
            title = { Text("Faculty Library") },
            text = { Text(noticeMsg!!) },
            confirmButton = {
                TextButton(onClick = { noticeMsg = null }) { Text("OK") }
            }
        )
    }
}
