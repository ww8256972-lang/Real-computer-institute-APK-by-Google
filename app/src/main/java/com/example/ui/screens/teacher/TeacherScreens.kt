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
import com.example.ui.components.EduSyncTopAppBar
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
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
                    icon = { Icon(Icons.Default.People, contentDescription = "Students") },
                    label = { Text("Students") },
                    modifier = Modifier.testTag("tab_teacher_students")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.EventAvailable, contentDescription = "Attendance") },
                    label = { Text("Attendance") },
                    modifier = Modifier.testTag("tab_teacher_attendance")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Assignments") },
                    label = { Text("Assignments") },
                    modifier = Modifier.testTag("tab_teacher_assignments")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Timetable") },
                    label = { Text("Timetable") },
                    modifier = Modifier.testTag("tab_teacher_timetable")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "Insights") },
                    label = { Text("Insights") },
                    modifier = Modifier.testTag("tab_teacher_insights")
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> TeacherBatchStudentsTab(teacher, instituteRepository)
                1 -> TeacherAttendanceTab(teacher, instituteRepository)
                2 -> TeacherAssignmentsTab(teacher, instituteRepository)
                3 -> TeacherTimetableTab(teacher, instituteRepository)
                4 -> TeacherInsightsTab(teacher, instituteRepository)
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

    var showCreateAssignmentDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("2026-11-10") }
    var maxMarks by remember { mutableStateOf("50") }

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
            Column {
                Text("Batch Assignments", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
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
                                Text(assign.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Surface(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(8.dp)) {
                                    Text("Max: ${assign.maxMarks} Marks", color = Color(0xFF1D4ED8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(assign.instructions, style = MaterialTheme.typography.bodySmall)
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
                                        Column {
                                            Text("Student: ${sub.studentId}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            Text("Answer: ${sub.submissionContent.take(40)}...", fontSize = 11.sp, color = Color.Gray)
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Assignment Title *") }, modifier = Modifier.fillMaxWidth().testTag("assign_title_in"))
                    OutlinedTextField(value = instructions, onValueChange = { instructions = it }, label = { Text("Instructions *") }, modifier = Modifier.fillMaxWidth().height(100.dp))
                    OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Due Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = maxMarks, onValueChange = { maxMarks = it }, label = { Text("Maximum Marks") }, modifier = Modifier.fillMaxWidth())
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
                                        createdByName = teacher.fullName
                                    )
                                )
                                showCreateAssignmentDialog = false
                                title = ""
                                instructions = ""
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

    if (gradingSubmission != null) {
        val s = gradingSubmission!!
        AlertDialog(
            onDismissRequest = { gradingSubmission = null },
            title = { Text("Grade Student ${s.studentId}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Submission: ${s.submissionContent}", style = MaterialTheme.typography.bodySmall)
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
    var selectedStudentId by remember { mutableStateOf(batchStudents.firstOrNull()?.studentId ?: "STU-2026-001") }
    var insight by remember { mutableStateOf<StudentPerformanceInsight?>(null) }

    LaunchedEffect(batchStudents) {
        if (batchStudents.isNotEmpty()) {
            selectedStudentId = batchStudents.first().studentId
            insight = repository.getStudentPerformanceInsights(selectedStudentId)
        }
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
                    Text(if (current != null) "${current.fullName} (${current.studentId})" else "Select Student")
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(res.studentName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Health: ${res.academicHealth}", color = if (res.academicHealth == "EXCELLENT") Color(0xFF15803D) else Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                        }
                        Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(8.dp)) {
                            Text("${"%.1f".format(res.attendancePercentage)}% Att.", fontWeight = FontWeight.Bold, color = Color(0xFF15803D), modifier = Modifier.padding(6.dp))
                        }
                    }

                    HorizontalDivider()
                    Text("Observed Academic Strengths:", fontWeight = FontWeight.Bold, color = Color(0xFF15803D), fontSize = 12.sp)
                    res.subjectStrengths.forEach { s -> Text("✓ $s", fontSize = 12.sp) }

                    Text("Areas Needing Guidance:", fontWeight = FontWeight.Bold, color = Color(0xFFB45309), fontSize = 12.sp)
                    res.subjectWeaknesses.forEach { w -> Text("• $w", fontSize = 12.sp) }

                    HorizontalDivider()
                    Text("Faculty Recommendation & Plan:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    Text(res.teacherRecommendations, fontSize = 12.sp)
                }
            }
        }
    }
}

