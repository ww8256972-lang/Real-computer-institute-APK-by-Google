package com.example.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import android.speech.tts.TextToSpeech
import java.util.Locale
import kotlinx.coroutines.delay
import com.example.core.QrCodeWidget
import com.example.data.local.ExamQuestionBank
import com.example.data.local.entity.*
import com.example.data.remote.GeminiService
import com.example.data.repository.InstituteRepository
import com.example.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentMainScreen(
    student: StudentEntity,
    instituteRepository: InstituteRepository,
    geminiService: GeminiService,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            EduSyncTopAppBar(
                title = "EduSync Student Portal",
                subtitle = "${student.fullName} • ${student.studentId}",
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                actions = {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("student_logout_button")
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
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Home") },
                    modifier = Modifier.testTag("tab_student_home")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Learning") },
                    label = { Text("Learning") },
                    modifier = Modifier.testTag("tab_student_learning")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Fees") },
                    label = { Text("Fees & Att.") },
                    modifier = Modifier.testTag("tab_student_fees")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.WorkspacePremium, contentDescription = "Exams") },
                    label = { Text("Exams") },
                    modifier = Modifier.testTag("tab_student_exams")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "EduSync AI") },
                    label = { Text("EduSync AI") },
                    modifier = Modifier.testTag("tab_student_ai")
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> StudentDashboardTab(student, instituteRepository, onNavigateToExams = { selectedTab = 3 })
                1 -> StudentLearningTab(student, instituteRepository)
                2 -> StudentFeesAttendanceTab(student, instituteRepository)
                3 -> StudentExamsTab(student, instituteRepository)
                4 -> StudentAiTutorTab(student, geminiService)
            }
        }
    }
}

@Composable
fun StudentDashboardTab(
    student: StudentEntity,
    repository: InstituteRepository,
    onNavigateToExams: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val notices by repository.getNotices().collectAsState(initial = emptyList())
    val timetables by repository.getTimetablesByBatch(student.batch).collectAsState(initial = emptyList())
    val scheduledExams by repository.getScheduledExamsForStudent(student.studentId).collectAsState(initial = emptyList())
    var presentCount by remember { mutableIntStateOf(0) }
    var totalDays by remember { mutableIntStateOf(0) }
    var downloadDialog by remember { mutableStateOf<String?>(null) }

    // Practice Quiz state
    var showPracticeQuizDialog by remember { mutableStateOf(false) }
    var practiceQuizIdx by remember { mutableIntStateOf(0) }
    var practiceQuizScore by remember { mutableIntStateOf(0) }
    var practiceSelectedOpt by remember { mutableStateOf<Int?>(null) }
    var practiceQuizResultDialog by remember { mutableStateOf<String?>(null) }

    val practiceQuestions = remember {
        listOf(
            Triple("Which shortcut is used in Tally Prime to record a Contra Voucher?", listOf("F4", "F5", "F6", "F7"), 0),
            Triple("In Python, which keyword is used to define a function?", listOf("func", "def", "define", "function"), 1),
            Triple("What is the full form of 'DCA'?", listOf("Diploma in Computer Applications", "Digital Computing & Accounts", "Data Communications Architecture", "Degree in Computer Accounting"), 0),
            Triple("Which HTML5 tag is used to create a hyperlink?", listOf("<link>", "<a>", "<href>", "<nav>"), 1),
            Triple("Which Excel formula is used to look up values vertically in a table?", listOf("INDEX", "MATCH", "VLOOKUP", "SUMIF"), 2)
        )
    }

    LaunchedEffect(student.studentId) {
        val stats = repository.getStudentAttendanceStats(student.studentId)
        presentCount = stats.first
        totalDays = stats.second
    }

    val attPercentage: Int = if (totalDays > 0) ((presentCount.toFloat() / totalDays.toFloat()) * 100f).toInt() else 85

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Hello, ${student.fullName}!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${student.courseName} • ${student.batch}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // 1. Upcoming Schedule (Requirement 5)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("upcoming_schedule_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upcoming Schedule", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Surface(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(8.dp)) {
                        Text(student.batch, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold)
                    }
                }

                if (timetables.isEmpty()) {
                    Text(
                        "No classes scheduled for today. Regular classes run as per the weekly timetable.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                } else {
                    timetables.take(3).forEach { slot ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(slot.subject, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${slot.dayOfWeek} • ${slot.startTime} - ${slot.endTime} • ${slot.roomOrLab}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(6.dp)) {
                                Text(slot.teacherName, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), fontSize = 10.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 2. Practice MCQ Quiz (Placed IMMEDIATELY BELOW Upcoming Schedule - Requirement 5)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("practice_quiz_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Practice MCQ Quiz", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    StatusBadge("INSTANT EVALUATION")
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Test and sharpen your computer fundamentals, Tally Prime, and Python skills with authentic quiz questions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        practiceQuizIdx = 0
                        practiceQuizScore = 0
                        practiceSelectedOpt = null
                        showPracticeQuizDialog = true
                    },
                    modifier = Modifier.fillMaxWidth().testTag("home_start_mcq_btn")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Practice MCQ Quiz (5 Questions)")
                }
            }
        }

        // Scheduled Exams Alert (if any active or scheduled exam)
        if (scheduledExams.isNotEmpty()) {
            val upcomingOrLive = scheduledExams.filter { it.isPublished }
            if (upcomingOrLive.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("home_scheduled_exams_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Certification Exams Scheduled",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                fontSize = 13.sp
                            )
                            Text(
                                "${upcomingOrLive.size} official scheduled exam(s) assigned to you.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB45309)
                            )
                        }
                        Button(
                            onClick = onNavigateToExams,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                        ) {
                            Text("Go to Exams", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Academic Progress & Attendance summary
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "Attendance",
                value = "$attPercentage%",
                icon = Icons.Default.EventAvailable,
                iconBgColor = if (attPercentage >= 75) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                iconColor = if (attPercentage >= 75) Color(0xFF15803D) else Color(0xFFB91C1C),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Course Progress",
                value = "65%",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                iconBgColor = Color(0xFFEDE9FE),
                iconColor = Color(0xFF6D28D9),
                modifier = Modifier.weight(1f)
            )
        }

        // Digital Student ID Card
        Text("Digital Student Identity Card", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        StudentIdCard(
            student = student,
            onDownloadPdf = { downloadDialog = "Student ID Card downloaded successfully to device storage." }
        )

        // Announcements & Notices
        Text("Institute Announcements", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        if (notices.isEmpty()) {
            Text("No new notices at this time.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        } else {
            notices.take(3).forEach { notice ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(notice.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            StatusBadge(notice.priority)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(notice.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Published: ${notice.date}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }
        }
    }

    // Practice MCQ Quiz Dialog
    if (showPracticeQuizDialog) {
        val q = practiceQuestions[practiceQuizIdx]
        AlertDialog(
            onDismissRequest = { showPracticeQuizDialog = false },
            title = { Text("Practice Question ${practiceQuizIdx + 1} of 5") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(q.first, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    q.second.forEachIndexed { idx, opt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (practiceSelectedOpt == idx) Color(0xFFDBEAFE) else Color.Transparent)
                                .border(1.dp, if (practiceSelectedOpt == idx) Color(0xFF2563EB) else Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            RadioButton(
                                selected = practiceSelectedOpt == idx,
                                onClick = { practiceSelectedOpt = idx }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(opt, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (practiceSelectedOpt == q.third) {
                            practiceQuizScore++
                        }
                        if (practiceQuizIdx < practiceQuestions.size - 1) {
                            practiceQuizIdx++
                            practiceSelectedOpt = null
                        } else {
                            val finalScore = practiceQuizScore
                            showPracticeQuizDialog = false
                            val percentage = (finalScore.toDouble() / practiceQuestions.size) * 100
                            scope.launch {
                                repository.saveExamResult(
                                    ExamResultEntity(
                                        studentId = student.studentId,
                                        examName = "Practice Computer Fundamentals Quiz",
                                        examDate = "2026-10-23",
                                        courseId = student.courseId,
                                        totalMarks = 100.0,
                                        obtainedMarks = percentage,
                                        percentage = percentage,
                                        grade = if (percentage >= 80) "A+" else if (percentage >= 60) "A" else "B",
                                        rank = 1,
                                        certificateNumber = "EDUSYNC-PRAC-${System.currentTimeMillis() % 10000}",
                                        certificateIssueDate = "2026-10-23"
                                    )
                                )
                                practiceQuizResultDialog = "Practice Quiz Completed! Score: $finalScore / 5 ($percentage%). Result saved to your academic record."
                            }
                        }
                    },
                    enabled = practiceSelectedOpt != null
                ) {
                    Text(if (practiceQuizIdx == practiceQuestions.size - 1) "Finish Quiz" else "Next")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPracticeQuizDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (practiceQuizResultDialog != null) {
        AlertDialog(
            onDismissRequest = { practiceQuizResultDialog = null },
            title = { Text("Quiz Evaluation") },
            text = { Text(practiceQuizResultDialog!!) },
            confirmButton = { TextButton(onClick = { practiceQuizResultDialog = null }) { Text("OK") } }
        )
    }

    if (downloadDialog != null) {
        AlertDialog(
            onDismissRequest = { downloadDialog = null },
            title = { Text("Download Successful") },
            text = { Text(downloadDialog!!) },
            confirmButton = {
                TextButton(onClick = { downloadDialog = null }) { Text("OK") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLearningTab(
    student: StudentEntity,
    repository: InstituteRepository
) {
    val scope = rememberCoroutineScope()
    var selectedLearningMode by remember { mutableIntStateOf(0) } // 0: Notes & PDFs, 1: Video Classes, 2: Assignments
    val materials by repository.getStudyMaterials(student.courseId).collectAsState(initial = emptyList())
    val assignments by repository.getAssignments(student.courseId).collectAsState(initial = emptyList())
    val submissions by repository.getSubmissionsForStudent(student.studentId).collectAsState(initial = emptyList())
    val digitalBooks by repository.getAllBooks().collectAsState(initial = emptyList())

    var submitAssignmentDialog by remember { mutableStateOf<AssignmentEntity?>(null) }
    var solutionText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        PrimaryTabRow(selectedTabIndex = selectedLearningMode) {
            Tab(selected = selectedLearningMode == 0, onClick = { selectedLearningMode = 0 }, text = { Text("PDF Notes") })
            Tab(selected = selectedLearningMode == 1, onClick = { selectedLearningMode = 1 }, text = { Text("Videos") })
            Tab(selected = selectedLearningMode == 2, onClick = { selectedLearningMode = 2 }, text = { Text("Homework") })
            Tab(selected = selectedLearningMode == 3, onClick = { selectedLearningMode = 3 }, text = { Text("E-Books") })
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedLearningMode) {
            0 -> {
                val pdfs = materials.filter { it.type == "PDF_NOTES" || it.type == "SYLLABUS" }
                if (pdfs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No PDF notes uploaded for your course yet.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(pdfs) { pdf ->
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("pdf_item_${pdf.id}"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(pdf.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                                        Text(pdf.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Size: ${pdf.fileSize ?: "2.5 MB"}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    IconButton(onClick = { statusMessage = "Opening '${pdf.title}' in PDF reader..." }) {
                                        Icon(Icons.Default.Download, contentDescription = "Download")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                val videos = materials.filter { it.type == "VIDEO_CLASS" }
                if (videos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No recorded video classes available yet.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(videos) { video ->
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("video_item_${video.id}"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(video.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                                        Text(video.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Duration: ${video.fileSize ?: "45 Mins"}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    IconButton(onClick = { statusMessage = "Launching Video Class player..." }) {
                                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Play")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                if (assignments.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No pending homework assignments.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(assignments) { assign ->
                            val sub = submissions.firstOrNull { it.assignmentId == assign.id }
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("assignment_item_${assign.id}"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(assign.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        if (sub != null) {
                                            StatusBadge(sub.status)
                                        } else {
                                            StatusBadge("PENDING")
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(assign.instructions, style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Due Date: ${assign.dueDate} • Max Marks: ${assign.maxMarks}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

                                    if (sub != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text("Your Submission: ${sub.submissionContent}", style = MaterialTheme.typography.bodySmall)
                                                if (sub.marksObtained != null) {
                                                    Text("Score: ${sub.marksObtained}/${assign.maxMarks} • Teacher Feedback: ${sub.feedback ?: "Good job"}", fontWeight = FontWeight.Bold, color = Color(0xFF15803D), style = MaterialTheme.typography.bodySmall)
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { submitAssignmentDialog = assign },
                                            modifier = Modifier.align(Alignment.End).testTag("submit_hw_btn_${assign.id}")
                                        ) {
                                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Submit Homework")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                if (digitalBooks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No digital books or manuals available.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(digitalBooks) { book ->
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("student_book_${book.id}"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(book.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Text("${book.author} • ${book.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${book.fileSize} • ~${book.readTimeMinutes} min read • ${book.category}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                repository.toggleBookDownloaded(book.id, !book.isDownloaded)
                                                statusMessage = if (!book.isDownloaded) "Downloaded '${book.title}' for offline reading!" else "Removed offline cache for '${book.title}'"
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (book.isDownloaded) Color(0xFF15803D) else MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            if (book.isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (book.isDownloaded) "Offline" else "Get")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (submitAssignmentDialog != null) {
        AlertDialog(
            onDismissRequest = { submitAssignmentDialog = null },
            title = { Text("Submit: ${submitAssignmentDialog!!.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Type your answers or paste your project GitHub/Drive link below:", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = solutionText,
                        onValueChange = { solutionText = it },
                        label = { Text("Solution or Answer text") },
                        modifier = Modifier.fillMaxWidth().height(140.dp).testTag("hw_solution_input"),
                        maxLines = 6
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (solutionText.isNotBlank()) {
                            scope.launch {
                                repository.submitAssignment(
                                    SubmissionEntity(
                                        assignmentId = submitAssignmentDialog!!.id,
                                        studentId = student.studentId,
                                        submissionContent = solutionText.trim()
                                    )
                                )
                                submitAssignmentDialog = null
                                solutionText = ""
                                statusMessage = "Assignment submitted successfully!"
                            }
                        }
                    }
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { submitAssignmentDialog = null }) { Text("Cancel") }
            }
        )
    }

    if (statusMessage != null) {
        AlertDialog(
            onDismissRequest = { statusMessage = null },
            title = { Text("Notice") },
            text = { Text(statusMessage!!) },
            confirmButton = { TextButton(onClick = { statusMessage = null }) { Text("OK") } }
        )
    }
}

@Composable
fun StudentFeesAttendanceTab(
    student: StudentEntity,
    repository: InstituteRepository
) {
    val scope = rememberCoroutineScope()
    val feeRecords by repository.getFeesForStudent(student.studentId).collectAsState(initial = emptyList())
    val attendanceList by repository.getAttendanceForStudent(student.studentId).collectAsState(initial = emptyList())

    var presentCount by remember { mutableIntStateOf(0) }
    var totalDays by remember { mutableIntStateOf(0) }
    var totalPaid by remember { mutableDoubleStateOf(0.0) }
    var totalCourseFee by remember { mutableDoubleStateOf(16500.0) }
    var showPayDialog by remember { mutableStateOf(false) }
    var payAmount by remember { mutableStateOf("3000") }
    var paymentNotice by remember { mutableStateOf<String?>(null) }
    var showQrCheckInDialog by remember { mutableStateOf(false) }
    var qrSessionPinInput by remember { mutableStateOf("") }
    var qrCheckInResult by remember { mutableStateOf<String?>(null) }
    val timetableList by repository.getTimetablesByBatch(student.batch).collectAsState(initial = emptyList())

    LaunchedEffect(student.studentId) {
        val stats = repository.getStudentAttendanceStats(student.studentId)
        presentCount = stats.first
        totalDays = stats.second
        val paid = repository.getFeesCollectedForStudent(student.studentId)
        totalPaid = paid
    }

    val pendingFees = (totalCourseFee - totalPaid).coerceAtLeast(0.0)
    val attPercentage: Int = if (totalDays > 0) ((presentCount.toFloat() / totalDays.toFloat()) * 100f).toInt() else 85

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Attendance Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (attPercentage < 75) Color(0xFFFEF2F2) else Color(0xFFF0FDF4)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ATTENDANCE PERCENTAGE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                        Text("$attPercentage%", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = if (attPercentage < 75) Color(0xFFDC2626) else Color(0xFF16A34A))
                    }
                    StatusBadge(if (attPercentage >= 75) "ELIGIBLE FOR EXAMS" else "LOW ATTENDANCE ALERT")
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { attPercentage / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = if (attPercentage >= 75) Color(0xFF16A34A) else Color(0xFFDC2626)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Total Days Logged: $totalDays • Present Days: $presentCount", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { showQrCheckInDialog = true },
                    modifier = Modifier.fillMaxWidth().testTag("student_qr_checkin_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Instant QR / PIN Attendance Check-In")
                }
            }
        }

        // Fees Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Course Fee Breakdown", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Package Fee:", color = Color.Gray)
                    Text("₹${"%.0f".format(totalCourseFee)}", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Amount Paid:", color = Color.Gray)
                    Text("₹${"%.0f".format(totalPaid)}", fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pending Balance Due:", fontWeight = FontWeight.Bold)
                    Text("₹${"%.0f".format(pendingFees)}", fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2626), fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                if (pendingFees > 0) {
                    Button(
                        onClick = { showPayDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("pay_fees_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pay Installment (Online Simulation)")
                    }
                } else {
                    Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15803D))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("All Fees Fully Paid. No outstanding dues!", color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Payment History Receipts
        Text("Payment History & Official Receipts", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        if (feeRecords.isEmpty()) {
            Text("No payments recorded yet.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        } else {
            feeRecords.forEach { record ->
                FeeReceiptCard(
                    record = record,
                    studentName = student.fullName,
                    onDownload = { paymentNotice = "Downloaded Receipt #${record.receiptNumber} as PDF." }
                )
            }
        }

        // Weekly Class Schedule
        Text("Weekly Class & Lab Schedule", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        if (timetableList.isEmpty()) {
            Text("No scheduled timetable published for this batch yet.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        } else {
            timetableList.forEach { slot ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("timetable_slot_${slot.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${slot.dayOfWeek} • ${slot.startTime} - ${slot.endTime}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${slot.subject} (${slot.roomOrLab})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            color = Color(0xFFE0E7FF),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(slot.teacherName, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3730A3))
                        }
                    }
                }
            }
        }
    }

    if (showPayDialog) {
        AlertDialog(
            onDismissRequest = { showPayDialog = false },
            title = { Text("Online UPI / Card Fee Payment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Simulate payment gateway confirmation with immediate receipt issuance:", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = payAmount,
                        onValueChange = { payAmount = it },
                        label = { Text("Amount (₹)") },
                        modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = payAmount.toDoubleOrNull() ?: 2000.0
                        scope.launch {
                            val res = repository.recordFeePayment(student.studentId, amt, "UPI (GooglePay)", "Online Student App Payment")
                            showPayDialog = false
                            if (res.isSuccess) {
                                totalPaid += amt
                                paymentNotice = "Payment of ₹$amt successful! Digital Receipt #${res.getOrThrow().receiptNumber} issued."
                            }
                        }
                    }
                ) {
                    Text("Confirm & Pay")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPayDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (paymentNotice != null) {
        AlertDialog(
            onDismissRequest = { paymentNotice = null },
            title = { Text("Payment Confirmation") },
            text = { Text(paymentNotice!!) },
            confirmButton = { TextButton(onClick = { paymentNotice = null }) { Text("Done") } }
        )
    }

    if (showQrCheckInDialog) {
        AlertDialog(
            onDismissRequest = { showQrCheckInDialog = false },
            title = { Text("Live Session QR / PIN Check-In") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter the 6-character live session code displayed by your teacher:", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = qrSessionPinInput,
                        onValueChange = { qrSessionPinInput = it.uppercase() },
                        label = { Text("Session PIN (e.g. ED8921)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("qr_session_pin_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (qrSessionPinInput.isNotBlank()) {
                            scope.launch {
                                val res = repository.submitQrAttendance(student.studentId, qrSessionPinInput.trim())
                                showQrCheckInDialog = false
                                qrSessionPinInput = ""
                                if (res.success) {
                                    qrCheckInResult = res.message
                                    val stats = repository.getStudentAttendanceStats(student.studentId)
                                    presentCount = stats.first
                                    totalDays = stats.second
                                } else {
                                    qrCheckInResult = res.message
                                }
                            }
                        }
                    }
                ) {
                    Text("Verify & Check-In")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrCheckInDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (qrCheckInResult != null) {
        AlertDialog(
            onDismissRequest = { qrCheckInResult = null },
            title = { Text("QR Attendance Status") },
            text = { Text(qrCheckInResult!!) },
            confirmButton = { TextButton(onClick = { qrCheckInResult = null }) { Text("OK") } }
        )
    }
}

@Composable
fun StudentExamsTab(
    student: StudentEntity,
    repository: InstituteRepository
) {
    val scope = rememberCoroutineScope()
    val results by repository.getResultsForStudent(student.studentId).collectAsState(initial = emptyList())
    val goals by repository.getLearningGoals(student.studentId).collectAsState(initial = emptyList())
    val badges by repository.getAchievementBadges(student.studentId).collectAsState(initial = emptyList())
    val certificates by repository.getCertificatesForStudent(student.studentId).collectAsState(initial = emptyList())
    val scheduledExams by repository.getScheduledExamsForStudent(student.studentId).collectAsState(initial = emptyList())
    val submissions by repository.getStudentExamSubmissions(student.studentId).collectAsState(initial = emptyList())

    // Active Secure Exam Mode state
    var activeExamToTake by remember { mutableStateOf<ScheduledExamEntity?>(null) }
    var activeSubmission by remember { mutableStateOf<ExamSubmissionEntity?>(null) }
    var examErrorMessage by remember { mutableStateOf<String?>(null) }
    var examSubmittedNotice by remember { mutableStateOf<String?>(null) }

    // Practice Quiz state
    var showQuizDialog by remember { mutableStateOf(false) }
    var quizScore by remember { mutableStateOf<Int?>(null) }
    var currentQuestionIdx by remember { mutableIntStateOf(0) }
    var selectedOptionIdx by remember { mutableStateOf<Int?>(null) }
    var testResultDialog by remember { mutableStateOf<String?>(null) }

    val sampleQuestions = remember {
        listOf(
            Triple("Which shortcut is used in Tally Prime to record a Contra Voucher?", listOf("F4", "F5", "F6", "F7"), 0),
            Triple("In Python, which keyword is used to define a function?", listOf("func", "def", "define", "function"), 1),
            Triple("What is the full form of 'DCA'?", listOf("Diploma in Computer Applications", "Digital Computing & Accounts", "Data Communications Architecture", "Degree in Computer Accounting"), 0),
            Triple("Which HTML5 tag is used to create a hyperlink?", listOf("<link>", "<a>", "<href>", "<nav>"), 1),
            Triple("Which Excel formula is used to look up values vertically in a table?", listOf("INDEX", "MATCH", "VLOOKUP", "SUMIF"), 2)
        )
    }

    val currentTime = System.currentTimeMillis()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Scheduled Certification Exams Dashboard (Requirement 8)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("scheduled_exams_dashboard_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scheduled Certification Exams", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    StatusBadge("SECURE EXAM MODE")
                }
                Text(
                    "Official timed examinations scheduled by your administrator. Requires exactly 50 MCQs, autosaves responses, and auto-submits upon timer expiration.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (scheduledExams.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Text(
                    "No scheduled certification exams assigned to you at this time. Upcoming exams scheduled by the admin within 24 hours will appear here.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        } else {
            scheduledExams.forEach { exam ->
                val submission = submissions.firstOrNull { it.examId == exam.examId }
                val isUpcoming = currentTime < exam.scheduledStartTime
                val isLive = currentTime in exam.scheduledStartTime..exam.scheduledEndTime
                val isExpired = currentTime > exam.scheduledEndTime
                val isCompleted = submission?.status == "SUBMITTED" || submission?.status == "EVALUATED"

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("student_scheduled_exam_${exam.examId}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLive && !isCompleted) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isLive && !isCompleted) CardDefaults.outlinedCardBorder().copy(width = 1.5.dp) else null
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(exam.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text("${exam.courseName} • ${exam.durationMinutes} Minutes • 50 MCQs", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            if (isCompleted) {
                                StatusBadge("EVALUATED")
                            } else if (isLive) {
                                Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(6.dp)) {
                                    Text("LIVE NOW", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                }
                            } else if (isUpcoming) {
                                val minsUntil = ((exam.scheduledStartTime - currentTime) / (1000 * 60)).coerceAtLeast(1)
                                Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(6.dp)) {
                                    Text("Starts in ${minsUntil}m", modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                }
                            } else {
                                StatusBadge("CLOSED")
                            }
                        }

                        if (isCompleted && submission != null) {
                            Surface(
                                color = if (submission.isPassed) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Score: ${submission.score.toInt()} / ${exam.totalMarks.toInt()} (${"%.1f".format(submission.percentage)}%)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (submission.isPassed) Color(0xFF15803D) else Color(0xFFDC2626)
                                        )
                                        Text(
                                            if (submission.isPassed) "PASSED • Official Certificate Eligible" else "NEEDS RETAKE (Passing: ${exam.passingMarks.toInt()}%)",
                                            fontSize = 11.sp,
                                            color = if (submission.isPassed) Color(0xFF16A34A) else Color(0xFFDC2626)
                                        )
                                    }
                                    if (submission.isAutoSubmitted) {
                                        Text("(Auto-Submitted on timeout)", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        } else if (isLive) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val attemptRes = repository.startExamAttempt(exam.examId, student.studentId, student.fullName)
                                        if (attemptRes.isSuccess) {
                                            activeSubmission = attemptRes.getOrNull()
                                            activeExamToTake = exam
                                        } else {
                                            examErrorMessage = attemptRes.exceptionOrNull()?.message ?: "Unable to enter exam."
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("enter_secure_exam_btn_${exam.examId}"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Enter Secure Exam Mode (50 MCQs)")
                            }
                        } else if (isUpcoming) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Exam window opens automatically at the scheduled time. Secure Exam Mode will unlock then.",
                                    modifier = Modifier.padding(10.dp),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Examination Admit Card Section
        Text("Examination Admit Card", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        AdmitCard(
            student = student,
            onPrintPdf = { testResultDialog = "Admit card PDF downloaded successfully." }
        )

        // Online Interactive MCQ Test
        Card(
            modifier = Modifier.fillMaxWidth().testTag("online_exam_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Practice Online MCQ Test", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Test your computer knowledge in DCA, Tally & Python with instant evaluation.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        currentQuestionIdx = 0
                        selectedOptionIdx = null
                        quizScore = 0
                        showQuizDialog = true
                    },
                    modifier = Modifier.fillMaxWidth().testTag("start_mcq_test_btn")
                ) {
                    Text("Start 5-Question MCQ Quiz")
                }
            }
        }

        // Official Marksheet & Completion Certificate
        Text("Official Marksheet & Completion Certificate", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        if (results.isEmpty()) {
            Text("No exam results published yet.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        } else {
            results.forEach { result ->
                InstituteCertificateCard(student = student, result = result)
            }
        }

        // Learning Goals & Progress Milestones
        Text("Learning Goals & Target Milestones", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        if (goals.isEmpty()) {
            Text("No active learning goals tracked.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        } else {
            goals.forEach { goal ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("student_goal_${goal.id}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(goal.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            StatusBadge(if (goal.isCompleted) "COMPLETED" else "${goal.progressPercent}%")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${goal.description} • Target Date: ${goal.targetDate}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { goal.progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = if (goal.isCompleted) Color(0xFF16A34A) else Color(0xFF2563EB)
                        )
                        if (!goal.isCompleted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            val nextProg = (goal.progressPercent + 20).coerceAtMost(100)
                                            repository.updateGoalProgress(goal.id, nextProg, nextProg >= 100)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Log +20% Progress")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Achievement Badges
        Text("Skill Badges & Achievements", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        if (badges.isEmpty()) {
            Text("No badges unlocked yet. Complete practical exams and keep attendance high to earn badges!", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                badges.forEach { badge ->
                    Card(
                        modifier = Modifier.weight(1f).testTag("badge_item_${badge.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(badge.badgeName, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                            Text(badge.description, style = MaterialTheme.typography.labelSmall, color = Color(0xFF92400E), textAlign = TextAlign.Center, maxLines = 2)
                        }
                    }
                }
            }
        }

        // Digital Certificates with Verification Token
        if (certificates.isNotEmpty()) {
            Text("Official Verified Digital Certificates", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            certificates.forEach { cert ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("cert_item_${cert.certificateNumber}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (cert.status == "CANCELLED") Color(0xFFFEF2F2) else Color(0xFFF8FAFC)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(cert.certificateNumber, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                Text(cert.courseName, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1E3A8A), fontWeight = FontWeight.SemiBold)
                            }
                            StatusBadge(cert.status)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            QrCodeWidget(
                                data = "https://edusync.institute/verify?certId=${cert.certificateNumber}&token=${cert.verificationToken}",
                                modifier = Modifier.size(76.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Grade: ${cert.grade} • Student: ${cert.studentName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                Text("Issued: ${cert.issueDate}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("Verification Token: ${cert.verificationToken.take(12)}...", style = MaterialTheme.typography.labelSmall, color = Color(0xFF475569))
                                if (cert.status == "CANCELLED" && !cert.cancellationReason.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Cancellation Reason: ${cert.cancellationReason}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Secure Scheduled Exam Full-Screen Dialog (Requirement 8)
    if (activeExamToTake != null && activeSubmission != null) {
        val exam = activeExamToTake!!
        val sub = activeSubmission!!
        val questions = remember(exam.examId) {
            ExamQuestionBank.deserializeQuestions(exam.questionsJson)
        }
        var currentQIndex by remember { mutableIntStateOf(0) }
        var userAnswers by remember {
            mutableStateOf(ExamQuestionBank.deserializeAnswers(sub.answersJson).toMutableMap())
        }
        var totalSecondsRemaining by remember {
            mutableIntStateOf(exam.durationMinutes * 60)
        }
        var showConfirmSubmitDialog by remember { mutableStateOf(false) }

        // Server-controlled countdown timer
        LaunchedEffect(sub.submissionId) {
            while (totalSecondsRemaining > 0) {
                delay(1000L)
                totalSecondsRemaining--
            }
            // Auto-submit on timeout (Requirement 8)
            val autoRes = repository.submitExam(sub.submissionId, isAutoSubmit = true)
            if (autoRes.isSuccess) {
                val evaluated = autoRes.getOrNull()
                examSubmittedNotice = "EXAM TIME EXPIRED: Your answers were automatically submitted and evaluated. Score: ${evaluated?.score?.toInt() ?: 0}/100 (${"%.1f".format(evaluated?.percentage ?: 0.0)}%)."
            }
            activeExamToTake = null
            activeSubmission = null
        }

        AlertDialog(
            onDismissRequest = { /* Prevent dismissal to enforce Secure Exam Mode */ },
            modifier = Modifier.fillMaxSize().padding(8.dp),
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            title = {
                Column {
                    Surface(
                        color = Color(0xFFDC2626),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "SECURE EXAM MODE ACTIVE • Outside AI & In-App Chat Restricted",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(exam.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Question ${currentQIndex + 1} of 50", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        val mins = totalSecondsRemaining / 60
                        val secs = totalSecondsRemaining % 60
                        Surface(
                            color = if (mins < 5) Color(0xFFFEE2E2) else Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "%02d:%02d".format(mins, secs),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = if (mins < 5) Color(0xFFDC2626) else Color(0xFF1D4ED8)
                            )
                        }
                    }
                }
            },
            text = {
                val q = questions.getOrNull(currentQIndex)
                if (q != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "${currentQIndex + 1}. ${q.questionText}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )

                        val selectedOpt = userAnswers[currentQIndex]

                        q.options.forEachIndexed { optIdx, optText ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedOpt == optIdx) Color(0xFFDBEAFE) else Color.Transparent)
                                    .border(1.dp, if (selectedOpt == optIdx) Color(0xFF2563EB) else Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                RadioButton(
                                    selected = selectedOpt == optIdx,
                                    onClick = {
                                        userAnswers[currentQIndex] = optIdx
                                        // Auto-save answer to repository (Requirement 8)
                                        scope.launch {
                                            repository.autosaveExamAnswer(sub.submissionId, currentQIndex, optIdx)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(optText, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Question selector row
                        Text("Question Palette (${userAnswers.size}/50 answered):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { if (currentQIndex > 0) currentQIndex-- },
                                enabled = currentQIndex > 0,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Previous")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { if (currentQIndex < 49) currentQIndex++ },
                                enabled = currentQIndex < 49,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Next")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showConfirmSubmitDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
                ) {
                    Text("Submit Exam (${userAnswers.size}/50)")
                }
            },
            dismissButton = {
                // Secure exam mode cannot be canceled without submission
            }
        )

        if (showConfirmSubmitDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmSubmitDialog = false },
                title = { Text("Confirm Final Submission") },
                text = {
                    Text("Are you sure you want to finalize your exam? You answered ${userAnswers.size} of 50 questions. Once submitted, answers cannot be edited.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmSubmitDialog = false
                            scope.launch {
                                val submitRes = repository.submitExam(sub.submissionId, isAutoSubmit = false)
                                if (submitRes.isSuccess) {
                                    val evaluated = submitRes.getOrNull()
                                    examSubmittedNotice = "Exam Submitted Successfully! Score: ${evaluated?.score?.toInt() ?: 0}/100 (${"%.1f".format(evaluated?.percentage ?: 0.0)}%). Status: ${if (evaluated?.isPassed == true) "PASSED" else "NEEDS RETAKE"}."
                                } else {
                                    examErrorMessage = submitRes.exceptionOrNull()?.message ?: "Submission error."
                                }
                                activeExamToTake = null
                                activeSubmission = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
                    ) {
                        Text("Confirm & Submit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmSubmitDialog = false }) { Text("Review Questions") }
                }
            )
        }
    }

    if (examSubmittedNotice != null) {
        AlertDialog(
            onDismissRequest = { examSubmittedNotice = null },
            title = { Text("Exam Result & Evaluation") },
            text = { Text(examSubmittedNotice!!) },
            confirmButton = { TextButton(onClick = { examSubmittedNotice = null }) { Text("Done") } }
        )
    }

    if (examErrorMessage != null) {
        AlertDialog(
            onDismissRequest = { examErrorMessage = null },
            title = { Text("Exam Notice") },
            text = { Text(examErrorMessage!!) },
            confirmButton = { TextButton(onClick = { examErrorMessage = null }) { Text("OK") } }
        )
    }

    // MCQ Quiz Dialog
    if (showQuizDialog) {
        val q = sampleQuestions[currentQuestionIdx]
        AlertDialog(
            onDismissRequest = { showQuizDialog = false },
            title = { Text("Question ${currentQuestionIdx + 1} of 5") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(q.first, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    q.second.forEachIndexed { idx, opt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedOptionIdx == idx) Color(0xFFDBEAFE) else Color.Transparent)
                                .border(1.dp, if (selectedOptionIdx == idx) Color(0xFF2563EB) else Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            RadioButton(
                                selected = selectedOptionIdx == idx,
                                onClick = { selectedOptionIdx = idx }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(opt, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedOptionIdx == q.third) {
                            quizScore = (quizScore ?: 0) + 1
                        }
                        if (currentQuestionIdx < sampleQuestions.size - 1) {
                            currentQuestionIdx++
                            selectedOptionIdx = null
                        } else {
                            val finalScore = quizScore ?: 0
                            showQuizDialog = false
                            val percentage = (finalScore.toDouble() / sampleQuestions.size) * 100
                            scope.launch {
                                repository.saveExamResult(
                                    ExamResultEntity(
                                        studentId = student.studentId,
                                        examName = "Online Computer Knowledge Test",
                                        examDate = "2026-10-23",
                                        courseId = student.courseId,
                                        totalMarks = 100.0,
                                        obtainedMarks = percentage,
                                        percentage = percentage,
                                        grade = if (percentage >= 80) "A+" else if (percentage >= 60) "A" else "B",
                                        rank = 1,
                                        certificateNumber = "EDUSYNC-ONLINE-${System.currentTimeMillis() % 10000}",
                                        certificateIssueDate = "2026-10-23"
                                    )
                                )
                                testResultDialog = "Test Completed! Score: $finalScore / 5 ($percentage%). Result & Certificate generated."
                            }
                        }
                    },
                    enabled = selectedOptionIdx != null
                ) {
                    Text(if (currentQuestionIdx == sampleQuestions.size - 1) "Finish Quiz" else "Next")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuizDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (testResultDialog != null) {
        AlertDialog(
            onDismissRequest = { testResultDialog = null },
            title = { Text("Result Published") },
            text = { Text(testResultDialog!!) },
            confirmButton = { TextButton(onClick = { testResultDialog = null }) { Text("OK") } }
        )
    }
}

@Composable
fun StudentAiTutorTab(
    student: StudentEntity,
    geminiService: GeminiService
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var userPrompt by remember { mutableStateOf("") }
    var chatMessages by remember {
        mutableStateOf(
            listOf(
                "EduSync AI" to "Hello ${student.fullName}! I am EduSync AI, your curriculum-aligned academic tutor. Ask me any question regarding your course (${student.courseName}), MS Office, Tally Prime shortcuts, Python programming, or Web Development!"
            )
        )
    }
    var isThinking by remember { mutableStateOf(false) }
    var securityWarning by remember { mutableStateOf<String?>(null) }
    var copyNotice by remember { mutableStateOf<String?>(null) }

    // TextToSpeech initialization (Requirement 3: Read Aloud / Stop Speech)
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var currentlySpeakingIndex by remember { mutableStateOf<Int?>(null) }

    DisposableEffect(context) {
        val ttsEngine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsEngine.language = Locale.US
            }
        }
        tts = ttsEngine
        onDispose {
            ttsEngine.stop()
            ttsEngine.shutdown()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top AI Info Card (Requirement 3: "EduSync AI" without displaying model name)
        Surface(
            color = Color(0xFFF3E8FF),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7E22CE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("EduSync AI", fontWeight = FontWeight.Bold, color = Color(0xFF7E22CE), fontSize = 15.sp)
                    Text("Curriculum-Aligned Academic & Technical Computer Science Assistant", fontSize = 11.sp, color = Color(0xFF6B21A8))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick suggested questions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AssistChip(
                onClick = { userPrompt = "Explain Tally Prime GST voucher types" },
                label = { Text("Tally GST", fontSize = 11.sp) }
            )
            AssistChip(
                onClick = { userPrompt = "How does Python for loop work?" },
                label = { Text("Python Loop", fontSize = 11.sp) }
            )
            AssistChip(
                onClick = { userPrompt = "What are DCA course subjects?" },
                label = { Text("DCA Syllabus", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat Log
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatMessages.size) { idx ->
                val (sender, text) = chatMessages[idx]
                val isAi = sender != "You"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                ) {
                    Surface(
                        color = if (isAi) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.widthIn(max = 320.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sender,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isAi) MaterialTheme.colorScheme.primary else Color.White
                                )
                                if (isAi) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        // Copy button (Requirement 3)
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(text))
                                                copyNotice = "Copied to clipboard!"
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy text", modifier = Modifier.size(14.dp))
                                        }

                                        // Read Aloud / Stop Speech button (Requirement 3)
                                        val isThisSpeaking = currentlySpeakingIndex == idx
                                        IconButton(
                                            onClick = {
                                                if (isThisSpeaking) {
                                                    tts?.stop()
                                                    currentlySpeakingIndex = null
                                                } else {
                                                    tts?.stop()
                                                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "EduSyncTtsMsg")
                                                    currentlySpeakingIndex = idx
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                if (isThisSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                                contentDescription = if (isThisSpeaking) "Stop Speech" else "Read Aloud",
                                                modifier = Modifier.size(15.dp),
                                                tint = if (isThisSpeaking) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isAi) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                            )
                        }
                    }
                }
            }
            if (isThinking) {
                item {
                    Text("EduSync AI is formulating answer...", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userPrompt,
                onValueChange = { userPrompt = it },
                placeholder = { Text("Ask your doubt to EduSync AI...") },
                modifier = Modifier.weight(1f).testTag("ai_tutor_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    val q = userPrompt.trim()
                    if (q.isNotBlank()) {
                        chatMessages = chatMessages + ("You" to q)
                        userPrompt = ""
                        isThinking = true

                        scope.launch {
                            // Check for suspicious malicious activity
                            val (isSuspicious, reason) = geminiService.analyzeSuspiciousActivity(q)
                            if (isSuspicious) {
                                isThinking = false
                                securityWarning = "Suspicious activity detected! $reason"
                                chatMessages = chatMessages + ("Security Shield" to "ALERT: Malicious or SQL injection attempt blocked. This security violation has been logged.")
                                return@launch
                            }

                            val answer = geminiService.askTutor(q, student.courseName)
                            isThinking = false
                            chatMessages = chatMessages + ("EduSync AI" to answer)
                        }
                    }
                },
                enabled = !isThinking,
                modifier = Modifier.testTag("ai_tutor_send_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }

    if (copyNotice != null) {
        LaunchedEffect(copyNotice) {
            delay(2000L)
            copyNotice = null
        }
    }

    if (securityWarning != null) {
        AlertDialog(
            onDismissRequest = { securityWarning = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Security Shield Alert")
                }
            },
            text = { Text(securityWarning!!) },
            confirmButton = { TextButton(onClick = { securityWarning = null }) { Text("Acknowledge") } }
        )
    }
}
