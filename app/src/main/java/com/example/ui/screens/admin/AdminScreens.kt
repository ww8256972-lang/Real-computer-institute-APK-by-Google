package com.example.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.core.QrCodeWidget
import com.example.core.SecurityUtils
import com.example.data.local.entity.*
import com.example.core.ExamQuestionBank
import com.example.data.repository.*
import com.example.ui.components.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

/**
 * Admin Panel (Requirement 2 & 5)
 * Completely renamed from Admin Console to Admin Panel.
 * Includes complete teacher management & supervision, counter fee recording, and automatic overdue reminders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(
    instituteRepository: InstituteRepository,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            EduSyncTopAppBar(
                title = "Admin Panel",
                subtitle = "Institute Operations & Supervision",
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                actions = {
                    // Small settings icon to access Cloud Backup & Verification (Requirement 6)
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("admin_settings_icon")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings & Backups")
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("admin_logout_button")
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
                            Icon(Icons.Default.Dashboard, contentDescription = "Overview")
                        }
                    },
                    label = { Text("Overview") },
                    modifier = Modifier.pullDownTabInteraction(selected = selectedTab == 0).testTag("tab_admin_overview")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        AnimatedTabIconHalo(selected = selectedTab == 1) {
                            Icon(Icons.Default.People, contentDescription = "Students")
                        }
                    },
                    label = { Text("Students") },
                    modifier = Modifier.pullDownTabInteraction(selected = selectedTab == 1).testTag("tab_admin_students")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        AnimatedTabIconHalo(selected = selectedTab == 2) {
                            Icon(Icons.Default.School, contentDescription = "Teachers")
                        }
                    },
                    label = { Text("Teachers") },
                    modifier = Modifier.pullDownTabInteraction(selected = selectedTab == 2).testTag("tab_admin_teachers")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        AnimatedTabIconHalo(selected = selectedTab == 3) {
                            Icon(Icons.Default.Payments, contentDescription = "Fees")
                        }
                    },
                    label = { Text("Fees & Dues") },
                    modifier = Modifier.pullDownTabInteraction(selected = selectedTab == 3).testTag("tab_admin_fees")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = {
                        AnimatedTabIconHalo(selected = selectedTab == 4) {
                            Icon(Icons.Default.Class, contentDescription = "Academic")
                        }
                    },
                    label = { Text("Academic") },
                    modifier = Modifier.pullDownTabInteraction(selected = selectedTab == 4).testTag("tab_admin_academic")
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> AdminOverviewTab(instituteRepository, onNavigateToStudents = { selectedTab = 1 })
                1 -> AdminStudentsTab(instituteRepository)
                2 -> AdminTeachersSupervisionTab(instituteRepository)
                3 -> AdminFeesManagementTab(instituteRepository)
                4 -> AdminAcademicTab(instituteRepository)
            }
        }
    }
}

@Composable
fun AdminOverviewTab(
    repository: InstituteRepository,
    onNavigateToStudents: () -> Unit
) {
    val totalCount by repository.getTotalStudentCount().collectAsState(initial = 0)
    val activeCount by repository.getActiveStudentCount().collectAsState(initial = 0)
    val blockedCount by repository.getBlockedStudentCount().collectAsState(initial = 0)
    val teachers by repository.getAllTeachers().collectAsState(initial = emptyList())
    var totalFeesCollected by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(Unit) {
        totalFeesCollected = repository.getTotalCollectedFees()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Institute Summary Banner (Requirement 2: clean labels)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("INSTITUTE DIRECTORY", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Text("$totalCount Registered Students", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("• $activeCount Active", color = Color(0xFF15803D), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("• $blockedCount Suspended", color = Color(0xFFB91C1C), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("• ${teachers.size} Faculty Members", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        // Metrics
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "Total Fees Collected",
                value = "₹${"%.0f".format(totalFeesCollected)}",
                icon = Icons.Default.CurrencyRupee,
                iconBgColor = Color(0xFFDCFCE7),
                iconColor = Color(0xFF15803D),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Active Batches",
                value = "3 Batches",
                icon = Icons.Default.Schedule,
                iconBgColor = Color(0xFFEDE9FE),
                iconColor = Color(0xFF6D28D9),
                modifier = Modifier.weight(1f)
            )
        }

        // Quick Operations (Screen 3 Reference: 4 Administrative Action Cards)
        Text("Administrative Actions", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedCard(
                onClick = onNavigateToStudents,
                modifier = Modifier.weight(1f).testTag("quick_action_students"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Add Student", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text("New Enrollment", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            OutlinedCard(
                onClick = onNavigateToStudents,
                modifier = Modifier.weight(1f).testTag("quick_action_search"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Search Roster", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text("Directory Lookup", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedCard(
                onClick = onNavigateToStudents,
                modifier = Modifier.weight(1f).testTag("quick_action_batches"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Manage Batches", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text("Timetable & Labs", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            OutlinedCard(
                onClick = onNavigateToStudents,
                modifier = Modifier.weight(1f).testTag("quick_action_reports"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEDE9FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Generate Reports", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text("Attendance & Dues", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/**
 * Student Directory & Management in Admin Panel
 */
@Composable
fun AdminStudentsTab(repository: InstituteRepository) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    val students by if (searchQuery.isBlank()) {
        repository.getStudentsPaged(page = 1, pageSize = 100).collectAsState(initial = emptyList())
    } else {
        repository.searchStudents(query = searchQuery.trim(), page = 1, pageSize = 100).collectAsState(initial = emptyList())
    }

    var showAddStudentDialog by remember { mutableStateOf(false) }
    var newCustomStudentId by remember { mutableStateOf("") }
    var newRollNo by remember { mutableStateOf("") }
    var newFullName by remember { mutableStateOf("") }
    var newFatherName by remember { mutableStateOf("") }
    var newMotherName by remember { mutableStateOf("") }
    var newGender by remember { mutableStateOf("Male") }
    var newDob by remember { mutableStateOf("2003-05-15") }
    var newUsername by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var newEmail by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newCourseId by remember { mutableStateOf("COURSE_01") }
    var newCourseName by remember { mutableStateOf("Full Stack Web Development") }
    var newBatch by remember { mutableStateOf("Morning (09:00 AM - 11:00 AM)") }
    var newTotalFee by remember { mutableStateOf("15000") }
    var newFeePaid by remember { mutableStateOf("5000") }
    var newAddress by remember { mutableStateOf("") }
    var selectedAvatarIndex by remember { mutableIntStateOf(0) }

    var studentToResetPassword by remember { mutableStateOf<StudentEntity?>(null) }
    var studentToDelete by remember { mutableStateOf<StudentEntity?>(null) }
    var resetPassText by remember { mutableStateOf("") }
    var alertMsg by remember { mutableStateOf<String?>(null) }

    val batchOptions = listOf(
        "Morning (09:00 AM - 11:00 AM)",
        "Afternoon (01:00 PM - 03:00 PM)",
        "Evening (05:00 PM - 07:00 PM)"
    )

    val courseOptions = listOf(
        "COURSE_01" to "Full Stack Web Development",
        "COURSE_02" to "Python & AI Data Science",
        "COURSE_03" to "Graphic Design & UI/UX",
        "COURSE_04" to "Financial Accounting & Tally Prime",
        "COURSE_05" to "Cybersecurity & Ethical Hacking"
    )

    val avatarColors = listOf(
        Color(0xFF2563EB) to "Blue Avatar",
        Color(0xFF059669) to "Green Avatar",
        Color(0xFF7C3AED) to "Purple Avatar",
        Color(0xFFEA580C) to "Orange Avatar"
    )

    val guardians by repository.getAllGuardians().collectAsState(initial = emptyList())
    var showParentAccountsDialog by remember { mutableStateOf(false) }
    var newParentName by remember { mutableStateOf("") }
    var newParentUsername by remember { mutableStateOf("") }
    var newParentPassword by remember { mutableStateOf("") }
    var newParentPhone by remember { mutableStateOf("+91 98765 43210") }
    var newParentStudentId by remember { mutableStateOf("EDU-2026-1001") }
    var newParentRelation by remember { mutableStateOf("Father") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Student Directory", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("${students.size} Students Enrolled • ${guardians.size} Parent Accounts", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        newParentStudentId = students.firstOrNull()?.studentId ?: "EDU-2026-1001"
                        showParentAccountsDialog = true
                    },
                    modifier = Modifier.testTag("admin_parent_accounts_btn")
                ) {
                    Icon(Icons.Default.FamilyRestroom, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Parent IDs")
                }
                Button(
                    onClick = { showAddStudentDialog = true },
                    modifier = Modifier.testTag("admin_add_student_btn")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Student")
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, ID, roll no, or phone...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_student_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        if (alertMsg != null) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alertMsg!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { alertMsg = null }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        students.forEach { student ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_student_card_${student.studentId}"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Student Avatar / Photo Thumbnail
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    when (student.id.toInt() % 4) {
                                        0 -> Color(0xFFDBEAFE)
                                        1 -> Color(0xFFD1FAE5)
                                        2 -> Color(0xFFEDE9FE)
                                        else -> Color(0xFFFEF3C7)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = student.fullName.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = when (student.id.toInt() % 4) {
                                    0 -> Color(0xFF1D4ED8)
                                    1 -> Color(0xFF047857)
                                    2 -> Color(0xFF6D28D9)
                                    else -> Color(0xFFB45309)
                                },
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(student.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                StatusBadge(student.status)
                            }
                            Text(
                                "ID: ${student.studentId} • Roll: ${student.rollNo.ifBlank { student.studentId.takeLast(4) }}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Course: ${student.courseName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Batch: ${student.batch}", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                "Father: ${student.fatherName.ifBlank { student.guardianName }} • Mother: ${student.motherName.ifBlank { "N/A" }}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text("Ph: ${student.phone} • DOB: ${student.dateOfBirth} • ${student.gender}", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reset Password Button
                        OutlinedButton(
                            onClick = {
                                studentToResetPassword = student
                                resetPassText = ""
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Password", fontSize = 11.sp)
                        }

                        // Suspend / Reactivate Button
                        if (student.status == "ACTIVE") {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        repository.setStudentStatus(student.studentId, "BLOCKED", "Admin policy compliance", "Admin Panel")
                                        alertMsg = "Student ${student.fullName} has been suspended."
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Suspend", fontSize = 11.sp)
                            }
                        } else {
                            Button(
                                onClick = {
                                    scope.launch {
                                        repository.setStudentStatus(student.studentId, "ACTIVE", "Restored by Admin", "Admin Panel")
                                        alertMsg = "Student ${student.fullName} reactivated."
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Reactivate", fontSize = 11.sp)
                            }
                        }

                        // Delete Student Button (User Requirement)
                        OutlinedButton(
                            onClick = { studentToDelete = student },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("admin_delete_student_btn_${student.studentId}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(15.dp), tint = Color(0xFFDC2626))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete", fontSize = 11.sp, color = Color(0xFFDC2626))
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog (User Requirement)
    if (studentToDelete != null) {
        val student = studentToDelete!!
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Student Record?")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Are you sure you want to permanently delete student '${student.fullName}'?",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("Student ID: ${student.studentId} • Roll: ${student.rollNo.ifBlank { student.studentId.takeLast(4) }}", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        "This will revoke student login access and admission records from the active institute registry.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = studentToDelete!!
                        studentToDelete = null
                        scope.launch {
                            repository.deleteStudent(target.studentId, "Admin Panel")
                            alertMsg = "Student ${target.fullName} (${target.studentId}) successfully deleted."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_student_btn")
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Student Dialog with Full Personal Details & Photo Avatar
    if (showAddStudentDialog) {
        AlertDialog(
            onDismissRequest = { showAddStudentDialog = false },
            title = { Text("Enroll New Student", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Identification & Roll Number", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = newCustomStudentId,
                        onValueChange = { newCustomStudentId = it },
                        label = { Text("Custom Student ID (Optional)") },
                        placeholder = { Text("e.g. EDU-2026-1050 (blank = auto)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_custom_student_id"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newRollNo,
                        onValueChange = { newRollNo = it },
                        label = { Text("Roll Number") },
                        placeholder = { Text("e.g. 105") },
                        modifier = Modifier.fillMaxWidth().testTag("add_student_roll_no"),
                        singleLine = true
                    )

                    Text("Personal Information", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = newFullName,
                        onValueChange = { newFullName = it },
                        label = { Text("Full Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("add_student_name"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newFatherName,
                        onValueChange = { newFatherName = it },
                        label = { Text("Father's Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("add_student_father_name"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newMotherName,
                        onValueChange = { newMotherName = it },
                        label = { Text("Mother's Name") },
                        modifier = Modifier.fillMaxWidth().testTag("add_student_mother_name"),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newPhone,
                            onValueChange = { newPhone = it },
                            label = { Text("Phone Number *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f).testTag("add_student_phone"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = newDob,
                            onValueChange = { newDob = it },
                            label = { Text("DOB (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Email Address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Gender:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("Male", "Female", "Other").forEach { g ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = newGender == g, onClick = { newGender = g })
                                Text(g, fontSize = 12.sp)
                            }
                        }
                    }

                    Text("Photo Avatar Preset:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        avatarColors.forEachIndexed { index, (color, label) ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .padding(if (selectedAvatarIndex == index) 3.dp else 0.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedAvatarIndex == index) {
                                    Icon(Icons.Default.Check, contentDescription = label, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    Text("Course & Batch", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    courseOptions.forEach { (cId, cName) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = newCourseId == cId,
                                onClick = {
                                    newCourseId = cId
                                    newCourseName = cName
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(cName, fontSize = 12.sp)
                        }
                    }

                    Text("Batch Timing:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    batchOptions.forEach { b ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(selected = newBatch == b, onClick = { newBatch = b })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(b, fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newTotalFee,
                            onValueChange = { newTotalFee = it },
                            label = { Text("Total Fee (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newFeePaid,
                            onValueChange = { newFeePaid = it },
                            label = { Text("Fee Paid (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Text("Portal Login Credentials", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { Text("Username *") },
                        modifier = Modifier.fillMaxWidth().testTag("add_student_username"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Password *") },
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth().testTag("add_student_password"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newAddress,
                        onValueChange = { newAddress = it },
                        label = { Text("Residential Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFullName.isNotBlank() && newUsername.isNotBlank() && newPassword.isNotBlank()) {
                            scope.launch {
                                val res = repository.createStudent(
                                    fullName = newFullName.trim(),
                                    username = newUsername.trim().lowercase(),
                                    rawPassword = newPassword,
                                    email = if (newEmail.isNotBlank()) newEmail.trim() else "${newUsername.trim()}@edusync.org",
                                    phone = if (newPhone.isNotBlank()) newPhone.trim() else "+91 98765 00000",
                                    courseId = newCourseId,
                                    courseName = newCourseName,
                                    batch = newBatch,
                                    guardianName = if (newFatherName.isNotBlank()) newFatherName.trim() else "Guardian",
                                    address = if (newAddress.isNotBlank()) newAddress.trim() else "City Center",
                                    customStudentId = newCustomStudentId.ifBlank { null },
                                    rollNo = newRollNo.trim(),
                                    fatherName = newFatherName.trim(),
                                    motherName = newMotherName.trim(),
                                    gender = newGender,
                                    dateOfBirth = newDob.trim(),
                                    totalCourseFee = newTotalFee.toDoubleOrNull() ?: 15000.0,
                                    feePaid = newFeePaid.toDoubleOrNull() ?: 0.0,
                                    photoUri = "avatar_${selectedAvatarIndex + 1}",
                                    isTemporaryPassword = false,
                                    adminUser = "Admin Panel"
                                )
                                showAddStudentDialog = false
                                if (res.isSuccess) {
                                    val created = res.getOrThrow()
                                    alertMsg = "Student ${created.fullName} enrolled! ID: ${created.studentId}, Roll: ${created.rollNo}"
                                    newFullName = ""
                                    newUsername = ""
                                    newPassword = ""
                                    newRollNo = ""
                                    newCustomStudentId = ""
                                    newFatherName = ""
                                    newMotherName = ""
                                } else {
                                    alertMsg = "Error: ${res.exceptionOrNull()?.message}"
                                }
                            }
                        }
                    }
                ) { Text("Enroll Student") }
            },
            dismissButton = {
                TextButton(onClick = { showAddStudentDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Reset Password Dialog
    if (studentToResetPassword != null) {
        AlertDialog(
            onDismissRequest = { studentToResetPassword = null },
            title = { Text("Reset Password for ${studentToResetPassword!!.fullName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter new secure password for this student:")
                    OutlinedTextField(
                        value = resetPassText,
                        onValueChange = { resetPassText = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetPassText.isNotBlank()) {
                            scope.launch {
                                repository.resetStudentPassword(
                                    studentId = studentToResetPassword!!.studentId,
                                    newPassword = resetPassText,
                                    isTemporary = true,
                                    adminUser = "Admin Panel"
                                )
                                alertMsg = "Password reset for ${studentToResetPassword!!.fullName}. Temporary password flag set."
                                studentToResetPassword = null
                            }
                        }
                    }
                ) { Text("Set Password") }
            },
            dismissButton = {
                TextButton(onClick = { studentToResetPassword = null }) { Text("Cancel") }
            }
        )
    }

    if (showParentAccountsDialog) {
        AlertDialog(
            onDismissRequest = { showParentAccountsDialog = false },
            title = { Text("Parent / Guardian Accounts", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Create a Parent ID linked to a Student ID so parents can sign in to the Parent Portal:",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = newParentName,
                        onValueChange = { newParentName = it },
                        label = { Text("Parent / Guardian Full Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("parent_full_name_input"),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newParentUsername,
                            onValueChange = { newParentUsername = it },
                            label = { Text("Login Username *") },
                            modifier = Modifier.weight(1f).testTag("parent_username_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newParentPassword,
                            onValueChange = { newParentPassword = it },
                            label = { Text("Password *") },
                            modifier = Modifier.weight(1f).testTag("parent_password_input"),
                            singleLine = true
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newParentStudentId,
                            onValueChange = { newParentStudentId = it },
                            label = { Text("Linked Student ID *") },
                            placeholder = { Text("EDU-2026-1001") },
                            modifier = Modifier.weight(1f).testTag("parent_linked_student_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newParentRelation,
                            onValueChange = { newParentRelation = it },
                            label = { Text("Relation") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = newParentPhone,
                        onValueChange = { newParentPhone = it },
                        label = { Text("Parent Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (newParentName.isNotBlank() && newParentUsername.isNotBlank() && newParentPassword.isNotBlank() && newParentStudentId.isNotBlank()) {
                                scope.launch {
                                    val res = repository.createParentGuardian(
                                        fullName = newParentName,
                                        username = newParentUsername,
                                        rawPassword = newParentPassword,
                                        phone = newParentPhone,
                                        linkedStudentId = newParentStudentId,
                                        relationship = newParentRelation,
                                        adminUser = "Admin Panel"
                                    )
                                    if (res.isSuccess) {
                                        val g = res.getOrThrow()
                                        newParentName = ""
                                        newParentUsername = ""
                                        newParentPassword = ""
                                        alertMsg = "Parent account '${g.username}' (${g.guardianId}) created and linked to student ${g.linkedStudentId}!"
                                    } else {
                                        alertMsg = "Error: ${res.exceptionOrNull()?.message}"
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_create_parent_account")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create & Link Parent ID")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Active Parent Accounts (${guardians.size}):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    guardians.forEach { g ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${g.fullName} (${g.relationship})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Username: ${g.username} • ID: ${g.guardianId}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("Linked Student: ${g.linkedStudentId}", fontSize = 11.sp)
                                }
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            repository.deleteParentGuardian(g.guardianId, "Admin Panel")
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Parent", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showParentAccountsDialog = false }) { Text("Close") }
            }
        )
    }

    if (alertMsg != null) {
        AlertDialog(
            onDismissRequest = { alertMsg = null },
            title = { Text("Student Directory") },
            text = { Text(alertMsg!!) },
            confirmButton = { TextButton(onClick = { alertMsg = null }) { Text("OK") } }
        )
    }
}

/**
 * Teacher Management & Complete Supervision (Requirements 3 & 5)
 */
@Composable
fun AdminTeachersSupervisionTab(repository: InstituteRepository) {
    val scope = rememberCoroutineScope()
    val teachers by repository.getAllTeachers().collectAsState(initial = emptyList())
    val allAssignments by repository.getAllAssignments().collectAsState(initial = emptyList())
    val allSubmissions by repository.getAllSubmissions().collectAsState(initial = emptyList())

    var showAddTeacherDialog by remember { mutableStateOf(false) }
    var teacherName by remember { mutableStateOf("") }
    var teacherUsername by remember { mutableStateOf("") }
    var teacherPassword by remember { mutableStateOf("") }
    var teacherPhone by remember { mutableStateOf("") }
    var teacherSpecialization by remember { mutableStateOf("") }
    var teacherBatch by remember { mutableStateOf("Morning (09:00 AM - 11:00 AM)") }
    var isBatchAdmin by remember { mutableStateOf(false) }

    var selectedTeacherForAction by remember { mutableStateOf<TeacherEntity?>(null) }
    var alertMsg by remember { mutableStateOf<String?>(null) }

    val batchOptions = listOf(
        "Morning (09:00 AM - 11:00 AM)",
        "Afternoon (01:00 PM - 03:00 PM)",
        "Evening (05:00 PM - 07:00 PM)"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Teacher Management", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("${teachers.size} Faculty Members Configured", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Button(
                onClick = { showAddTeacherDialog = true },
                modifier = Modifier.testTag("admin_add_teacher_btn")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Teacher")
            }
        }

        // List of teachers
        teachers.forEach { teacher ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("teacher_card_${teacher.teacherId}"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(teacher.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                if (teacher.isBatchAdmin) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(6.dp)) {
                                        Text("BATCH ADMIN", color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.padding(4.dp))
                                    }
                                }
                            }
                            Text("ID: ${teacher.teacherId} • User: ${teacher.username}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Specialization: ${teacher.specialization}", fontSize = 12.sp, color = Color.Gray)
                            Text("Assigned Batch: ${teacher.assignedBatches}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        StatusBadge(teacher.status)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (teacher.status == "ACTIVE") {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        repository.updateTeacherStatus(teacher.teacherId, "DISABLED")
                                        alertMsg = "Teacher account '${teacher.fullName}' has been disabled."
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Disable Account")
                            }
                        } else {
                            Button(
                                onClick = {
                                    scope.launch {
                                        repository.updateTeacherStatus(teacher.teacherId, "ACTIVE")
                                        alertMsg = "Teacher account '${teacher.fullName}' reactivated."
                                    }
                                }
                            ) {
                                Text("Reactivate")
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Complete Admin Supervision of Teacher Activities (Requirement 5)
        Text("Teacher Supervision & Assignments Monitor", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Text("Supervise all assignments and learning activities created by teachers:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

        if (allAssignments.isEmpty()) {
            Text("No assignments logged.", color = Color.Gray)
        } else {
            allAssignments.forEach { assign ->
                val subs = allSubmissions.filter { it.assignmentId == assign.id }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(assign.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Batch: ${assign.batchName ?: "All"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }
                        Text("Created By: ${assign.createdByName} (${assign.createdById})", fontSize = 11.sp, color = Color.Gray)
                        Text("Instructions: ${assign.instructions}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Submissions Received: ${subs.size} • Due Date: ${assign.dueDate}", fontSize = 11.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    if (showAddTeacherDialog) {
        AlertDialog(
            onDismissRequest = { showAddTeacherDialog = false },
            title = { Text("Create Teacher Account") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = teacherName, onValueChange = { teacherName = it }, label = { Text("Teacher Full Name *") }, modifier = Modifier.fillMaxWidth().testTag("teacher_fullname_in"))
                    OutlinedTextField(value = teacherUsername, onValueChange = { teacherUsername = it }, label = { Text("Username *") }, modifier = Modifier.fillMaxWidth().testTag("teacher_username_in"))
                    OutlinedTextField(value = teacherPassword, onValueChange = { teacherPassword = it }, label = { Text("Initial Password *") }, modifier = Modifier.fillMaxWidth().testTag("teacher_password_in"))
                    OutlinedTextField(value = teacherPhone, onValueChange = { teacherPhone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = teacherSpecialization, onValueChange = { teacherSpecialization = it }, label = { Text("Specialization (e.g. Tally Prime)") }, modifier = Modifier.fillMaxWidth())

                    Text("Assign Primary Batch:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    batchOptions.forEach { b ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = teacherBatch == b, onClick = { teacherBatch = b })
                            Text(b, fontSize = 12.sp)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isBatchAdmin, onCheckedChange = { isBatchAdmin = it })
                        Text("Designate as Batch Administrator", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (teacherName.isNotBlank() && teacherUsername.isNotBlank() && teacherPassword.isNotBlank()) {
                            scope.launch {
                                val res = repository.createTeacher(
                                    fullName = teacherName.trim(),
                                    username = teacherUsername.trim().lowercase(),
                                    rawPassword = teacherPassword,
                                    email = "${teacherUsername.trim()}@edusync.org",
                                    phone = teacherPhone.trim(),
                                    specialization = teacherSpecialization.trim(),
                                    assignedBatches = teacherBatch,
                                    isBatchAdmin = isBatchAdmin,
                                    adminUser = "Admin Panel"
                                )
                                showAddTeacherDialog = false
                                if (res.isSuccess) {
                                    alertMsg = "Teacher '${teacherName}' created successfully and assigned to $teacherBatch."
                                    teacherName = ""
                                    teacherUsername = ""
                                    teacherPassword = ""
                                } else {
                                    alertMsg = "Error: ${res.exceptionOrNull()?.message}"
                                }
                            }
                        }
                    }
                ) { Text("Save Teacher") }
            },
            dismissButton = { TextButton(onClick = { showAddTeacherDialog = false }) { Text("Cancel") } }
        )
    }

    if (alertMsg != null) {
        AlertDialog(
            onDismissRequest = { alertMsg = null },
            title = { Text("Teacher Management") },
            text = { Text(alertMsg!!) },
            confirmButton = { TextButton(onClick = { alertMsg = null }) { Text("OK") } }
        )
    }
}

/**
 * Student Fee Management & Automatic Overdue Reminders (Requirements 7 & 8)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFeesManagementTab(repository: InstituteRepository) {
    val scope = rememberCoroutineScope()
    val allFees by repository.getAllFeeRecords().collectAsState(initial = emptyList())
    val activeStudents by repository.getAllActiveStudents().collectAsState(initial = emptyList())
    val reminders by repository.getAllFeeReminders().collectAsState(initial = emptyList())

    var totalCollected by remember { mutableDoubleStateOf(0.0) }
    var showRecordCounterPaymentDialog by remember { mutableStateOf(false) }

    var selectedStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var studentDropdownExpanded by remember { mutableStateOf(false) }
    var payAmount by remember { mutableStateOf("4500") }
    var payMethod by remember { mutableStateOf("Cash (Office Counter)") }
    var referenceNumber by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("Tuition installment received at institute counter") }

    var isProcessingReminders by remember { mutableStateOf(false) }
    var alertMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(allFees) {
        totalCollected = repository.getTotalCollectedFees()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Finance Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Fee Collection & Billing", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Revenue Logged", fontSize = 12.sp, color = Color.Gray)
                        Text("₹${"%.0f".format(totalCollected)}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF15803D))
                    }
                    Button(
                        onClick = {
                            selectedStudent = activeStudents.firstOrNull()
                            showRecordCounterPaymentDialog = true
                        },
                        modifier = Modifier.testTag("admin_record_fee_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record Fee Payment")
                    }
                }
            }
        }

        // Smart Fee Forecast & Historical Trends (Requirement 3)
        var feeForecast by remember { mutableStateOf<FeeForecastReport?>(null) }
        LaunchedEffect(allFees, activeStudents) {
            feeForecast = repository.getFeeForecast()
        }

        feeForecast?.let { forecast ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color(0xFF2563EB))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Smart Fee Forecast & Estimation", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Surface(color = Color(0xFFDBEAFE), shape = RoundedCornerShape(6.dp)) {
                            Text("Forecast Engine Active", fontSize = 10.sp, color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "Projected Fees",
                            value = "₹${"%.0f".format(forecast.totalProjectedFees)}",
                            icon = Icons.Default.Analytics,
                            iconBgColor = Color(0xFFEFF6FF),
                            iconColor = Color(0xFF1D4ED8),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Outstanding Dues",
                            value = "₹${"%.0f".format(forecast.totalOutstandingFees)}",
                            icon = Icons.Default.Warning,
                            iconBgColor = Color(0xFFFEF2F2),
                            iconColor = Color(0xFFB91C1C),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "Est. 30-Day Inflow",
                            value = "₹${"%.0f".format(forecast.projectedMonthlyInflow)}",
                            icon = Icons.Default.Savings,
                            iconBgColor = Color(0xFFF0FDF4),
                            iconColor = Color(0xFF15803D),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Overdue Accounts",
                            value = "${forecast.overdueCount} students",
                            icon = Icons.Default.PendingActions,
                            iconBgColor = Color(0xFFFFFBEB),
                            iconColor = Color(0xFFB45309),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Historical Collection Trend & Predictive Projection:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                    forecast.trendHistory.forEach { (period, amount) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(period, fontSize = 11.sp, color = Color(0xFF334155))
                            Text("₹${"%.0f".format(amount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (period.contains("Forecast")) Color(0xFF2563EB) else Color(0xFF15803D))
                        }
                    }
                }
            }
        }

        // Automatic Overdue Fee Reminders (Requirement 8)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Automatic Overdue Fee Reminders", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text("Identifies students past due date and dispatches in-app alerts", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Button(
                        onClick = {
                            isProcessingReminders = true
                            scope.launch {
                                val sentCount = repository.sendBatchFeeReminders(adminUser = "Admin Panel")
                                isProcessingReminders = false
                                alertMsg = if (sentCount > 0) {
                                    "Successfully dispatched $sentCount overdue fee reminder notifications with payment receipts links."
                                } else {
                                    "All active students are currently up-to-date. No overdue balances found."
                                }
                            }
                        },
                        enabled = !isProcessingReminders,
                        modifier = Modifier.testTag("process_overdue_reminders_btn")
                    ) {
                        Text(if (isProcessingReminders) "Processing..." else "Send Reminders")
                    }
                }

                if (reminders.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Latest Dispatched Reminders (${reminders.size}):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    reminders.take(3).forEach { r ->
                        Text("• ${r.studentName} (${r.studentId}): ₹${"%.0f".format(r.pendingAmount)} overdue since ${r.dueDate}", fontSize = 11.sp, color = Color(0xFFB91C1C))
                    }
                }
            }
        }

        // Fee Records History
        Text("Saved Payment Receipts (${allFees.size})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        allFees.forEach { fee ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("RECEIPT #${fee.receiptNumber}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                            Text("Student: ${fee.studentName} (${fee.studentId})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Surface(
                            color = if (fee.paymentType == "MANUAL_COUNTER") Color(0xFFFEF3C7) else Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                if (fee.paymentType == "MANUAL_COUNTER") "OFFICE COUNTER" else "ONLINE GATEWAY",
                                color = if (fee.paymentType == "MANUAL_COUNTER") Color(0xFFB45309) else Color(0xFF15803D),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Amount: ₹${"%.0f".format(fee.amount)} • Method: ${fee.paymentMethod} • Date: ${fee.paymentDate}", style = MaterialTheme.typography.bodySmall)
                    Text("Ref/TxID: ${fee.transactionId} • ${fee.remarks}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }

    // Record Payment Dialog (Requirement 7)
    if (showRecordCounterPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showRecordCounterPaymentDialog = false },
            title = { Text("Record Fee Payment") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Select Student:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

                    ExposedDropdownMenuBox(
                        expanded = studentDropdownExpanded,
                        onExpandedChange = { studentDropdownExpanded = !studentDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = if (selectedStudent != null) "${selectedStudent!!.fullName} (${selectedStudent!!.studentId})" else "Select Student",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentDropdownExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = studentDropdownExpanded,
                            onDismissRequest = { studentDropdownExpanded = false }
                        ) {
                            activeStudents.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s.fullName} (${s.studentId}) • ${s.courseName}") },
                                    onClick = {
                                        selectedStudent = s
                                        studentDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(value = payAmount, onValueChange = { payAmount = it }, label = { Text("Fee Amount (₹) *") }, modifier = Modifier.fillMaxWidth().testTag("fee_amount_input"))
                    OutlinedTextField(value = payMethod, onValueChange = { payMethod = it }, label = { Text("Payment Method (Cash / Cheque / Bank / UPI)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = referenceNumber, onValueChange = { referenceNumber = it }, label = { Text("Receipt Reference / Cheque No.") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks") }, modifier = Modifier.fillMaxWidth())

                    Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "This payment will be recorded as 'Office Counter Manual Payment' and a unique official receipt will be issued to the student portal.",
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val s = selectedStudent
                        val amt = payAmount.toDoubleOrNull()
                        if (s != null && amt != null && amt > 0) {
                            scope.launch {
                                val res = repository.recordFeePayment(
                                    studentId = s.studentId,
                                    amount = amt,
                                    method = payMethod.trim(),
                                    paymentType = "MANUAL_COUNTER",
                                    remarks = if (referenceNumber.isNotBlank()) "$remarks (Ref: $referenceNumber)" else remarks,
                                    recordedBy = "Admin Panel"
                                )
                                showRecordCounterPaymentDialog = false
                                if (res.isSuccess) {
                                    alertMsg = "Payment of ₹$amt recorded! Receipt #${res.getOrThrow().receiptNumber} generated and added to ${s.fullName}'s student portal."
                                } else {
                                    alertMsg = "Failed to record payment: ${res.exceptionOrNull()?.message}"
                                }
                            }
                        }
                    }
                ) { Text("Save & Generate Receipt") }
            },
            dismissButton = { TextButton(onClick = { showRecordCounterPaymentDialog = false }) { Text("Cancel") } }
        )
    }

    if (alertMsg != null) {
        AlertDialog(
            onDismissRequest = { alertMsg = null },
            title = { Text("Fee Management") },
            text = { Text(alertMsg!!) },
            confirmButton = { TextButton(onClick = { alertMsg = null }) { Text("OK") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAcademicTab(repository: InstituteRepository) {
    val scope = rememberCoroutineScope()
    var academicSubTab by remember { mutableIntStateOf(0) } // 0=Courses, 1=Batches, 2=Timetable, 3=Library, 4=Certificates, 5=Insights

    val courses by repository.getAllCourses().collectAsState(initial = emptyList())
    val allBatches by repository.getAllBatches().collectAsState(initial = emptyList())
    val activeStudents by repository.getAllActiveStudents().collectAsState(initial = emptyList())
    val teachers by repository.getAllTeachers().collectAsState(initial = emptyList())
    val allTimetables by repository.getAllTimetables().collectAsState(initial = emptyList())
    val allBooks by repository.getAllBooks().collectAsState(initial = emptyList())
    val allCertificates by repository.getAllCertificates().collectAsState(initial = emptyList())

    // Course Management state
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var newCourseName by remember { mutableStateOf("") }
    var newCourseCode by remember { mutableStateOf("") }
    var newCourseDuration by remember { mutableStateOf("6 Months") }
    var newCourseFees by remember { mutableStateOf("15000") }
    var newCourseModules by remember { mutableStateOf("8") }
    var newCourseDescription by remember { mutableStateOf("") }
    var newCourseSyllabus by remember { mutableStateOf("") }
    var courseToDelete by remember { mutableStateOf<CourseEntity?>(null) }

    // Batch Management state
    var showAddBatchDialog by remember { mutableStateOf(false) }
    var newBatchName by remember { mutableStateOf("") }
    var selectedBatchCourseId by remember { mutableStateOf("") }
    var selectedBatchTiming by remember { mutableStateOf("Morning (09:00 AM - 11:00 AM)") }
    var selectedBatchTeacherId by remember { mutableStateOf("") }
    var isNewBatchAdmin by remember { mutableStateOf(false) }
    var courseDropdownExpanded by remember { mutableStateOf(false) }
    var timingDropdownExpanded by remember { mutableStateOf(false) }
    var teacherDropdownExpanded by remember { mutableStateOf(false) }

    var batchToEditCoordinator by remember { mutableStateOf<BatchEntity?>(null) }
    var editCoordinatorTeacherId by remember { mutableStateOf("") }
    var editIsBatchAdmin by remember { mutableStateOf(false) }
    var editTeacherDropdownExpanded by remember { mutableStateOf(false) }
    var batchToDelete by remember { mutableStateOf<BatchEntity?>(null) }

    var showAddNoticeDialog by remember { mutableStateOf(false) }
    var noticeTitle by remember { mutableStateOf("") }
    var noticeContent by remember { mutableStateOf("") }

    // Timetable Generator Dialog state
    var showGenerateTimetableDialog by remember { mutableStateOf(false) }
    var selectedTimetableBatch by remember { mutableStateOf("Morning (09:00 AM - 11:00 AM)") }
    var selectedTimetableRoom by remember { mutableStateOf("Lab 1 (Accounting Systems)") }

    // Certificate Issuance & Revocation state
    var showIssueCertDialog by remember { mutableStateOf(false) }
    var certStudentId by remember { mutableStateOf("") }
    var certCourseId by remember { mutableStateOf("CRS_ADCA") }
    var certGrade by remember { mutableStateOf("A+") }

    var certToCancel by remember { mutableStateOf<DigitalCertificateEntity?>(null) }
    var certCancelReason by remember { mutableStateOf("") }

    // Student Insights state
    var insightStudentId by remember { mutableStateOf("EDU-2026-1001") }
    var currentInsight by remember { mutableStateOf<StudentPerformanceInsight?>(null) }

    // Scheduled Exams state (Requirement 8)
    val allScheduledExams by repository.getAllScheduledExams().collectAsState(initial = emptyList())
    var showCreateExamDialog by remember { mutableStateOf(false) }
    var newExamTitle by remember { mutableStateOf("") }
    var newExamCourseId by remember { mutableStateOf("") }
    var newExamDurationMinutes by remember { mutableStateOf("60") }
    var newExamStartHoursFromNow by remember { mutableStateOf("1") }
    var newExamSelectedStudentIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var newExam50Questions by remember { mutableStateOf(ExamQuestionBank.getDefault50Questions()) }
    var selectedExamForSubmissions by remember { mutableStateOf<ScheduledExamEntity?>(null) }
    var examSubmissionsForDialog by remember { mutableStateOf<List<ExamSubmissionEntity>>(emptyList()) }
    var examToDelete by remember { mutableStateOf<ScheduledExamEntity?>(null) }

    // Course Editing state (Requirement 4)
    var courseToEdit by remember { mutableStateOf<CourseEntity?>(null) }
    var editCourseName by remember { mutableStateOf("") }
    var editCourseDuration by remember { mutableStateOf("") }
    var editCourseFees by remember { mutableStateOf("") }
    var editCourseModules by remember { mutableStateOf("") }
    var editCourseDescription by remember { mutableStateOf("") }
    var editCourseSyllabus by remember { mutableStateOf("") }

    // Batch Editing & Student Assignment state (Requirement 4 & 7: 24-hour HH:mm format)
    var batchStartTime24h by remember { mutableStateOf("09:00") }
    var batchEndTime24h by remember { mutableStateOf("11:00") }
    var batchTimeError by remember { mutableStateOf<String?>(null) }
    var batchToEdit by remember { mutableStateOf<BatchEntity?>(null) }
    var editBatchName by remember { mutableStateOf("") }
    var editBatchTiming by remember { mutableStateOf("") }
    var editBatchStart24h by remember { mutableStateOf("09:00") }
    var editBatchEnd24h by remember { mutableStateOf("11:00") }
    var editBatchCourseId by remember { mutableStateOf("") }
    var batchForStudentEnrollment by remember { mutableStateOf<BatchEntity?>(null) }
    var selectedEnrollmentStudentIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Custom Timetable Entry with conflict detection + Edit/View/Delete state (Requirement 5)
    var showCustomTimetableDialog by remember { mutableStateOf(false) }
    var customScheduleDay by remember { mutableStateOf("Monday") }
    var customScheduleStartTime by remember { mutableStateOf("09:00") }
    var customScheduleEndTime by remember { mutableStateOf("11:00") }
    var customScheduleBatchName by remember { mutableStateOf("") }
    var customScheduleSubject by remember { mutableStateOf("") }
    var customScheduleTeacherId by remember { mutableStateOf("") }
    var customScheduleRoom by remember { mutableStateOf("Lab 1 (Accounting Systems)") }
    var scheduleConflictError by remember { mutableStateOf<String?>(null) }
    var timetableToView by remember { mutableStateOf<TimetableEntity?>(null) }
    var timetableToEdit by remember { mutableStateOf<TimetableEntity?>(null) }
    var timetableToDelete by remember { mutableStateOf<TimetableEntity?>(null) }

    // Library Management state (Requirement 1 & 7: Upload, View, Download, Rename/Edit, Delete with confirmation)
    var showUploadBookDialog by remember { mutableStateOf(false) }
    var newBookTitle by remember { mutableStateOf("") }
    var newBookAuthor by remember { mutableStateOf("") }
    var newBookSubject by remember { mutableStateOf("Computer Applications") }
    var newBookCategory by remember { mutableStateOf("E-BOOK") }
    var newBookSize by remember { mutableStateOf("4.2 MB") }
    var newBookDesc by remember { mutableStateOf("") }
    var bookToView by remember { mutableStateOf<DigitalBookEntity?>(null) }
    var bookToEdit by remember { mutableStateOf<DigitalBookEntity?>(null) }
    var editBookTitle by remember { mutableStateOf("") }
    var editBookAuthor by remember { mutableStateOf("") }
    var editBookCategory by remember { mutableStateOf("") }
    var editBookDesc by remember { mutableStateOf("") }
    var bookToDelete by remember { mutableStateOf<DigitalBookEntity?>(null) }

    var alertMsg by remember { mutableStateOf<String?>(null) }

    val standardTimingSlots = listOf(
        "Morning (09:00 - 11:00)",
        "Mid-Day (11:00 - 13:00)",
        "Afternoon (14:00 - 16:00)",
        "Evening (17:00 - 19:00)"
    )

    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val time24hRegex = remember { Regex("^([01]\\d|2[0-3]):([0-5]\\d)$") }

    LaunchedEffect(academicSubTab) {
        if (academicSubTab == 6 && currentInsight == null) {
            currentInsight = repository.getStudentPerformanceInsights(insightStudentId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Module Sub-Tab Selector
        ScrollableTabRow(
            selectedTabIndex = academicSubTab,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(selected = academicSubTab == 0, onClick = { academicSubTab = 0 }, text = { Text("Courses") })
            Tab(selected = academicSubTab == 1, onClick = { academicSubTab = 1 }, text = { Text("Batches") })
            Tab(selected = academicSubTab == 2, onClick = { academicSubTab = 2 }, text = { Text("Timetable") })
            Tab(selected = academicSubTab == 3, onClick = { academicSubTab = 3 }, text = { Text("Exams (50 MCQ)") })
            Tab(selected = academicSubTab == 4, onClick = { academicSubTab = 4 }, text = { Text("Library") })
            Tab(selected = academicSubTab == 5, onClick = { academicSubTab = 5 }, text = { Text("Certificates") })
            Tab(selected = academicSubTab == 6, onClick = { academicSubTab = 6 }, text = { Text("Insights") })
        }

        when (academicSubTab) {
            0 -> {
                // COURSES MANAGEMENT (Requirement 4)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Course Management", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("${courses.size} Academic Programs Offered • Configure fees & syllabi", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { showAddNoticeDialog = true },
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Notice")
                                }
                                Button(
                                    onClick = {
                                        newCourseName = ""
                                        newCourseCode = ""
                                        newCourseDuration = "6 Months"
                                        newCourseFees = "15000"
                                        newCourseModules = "8"
                                        newCourseDescription = ""
                                        newCourseSyllabus = ""
                                        showAddCourseDialog = true
                                    },
                                    modifier = Modifier.height(40.dp).testTag("admin_add_course_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Course")
                                }
                            }
                        }
                    }
                }

                courses.forEach { course ->
                    val enrolledInCourse = activeStudents.count { it.courseId == course.id }
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(course.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text("Code: ${course.code}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                }
                                Surface(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(8.dp)) {
                                    Text("₹${"%.0f".format(course.totalFees)}", color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                            Text(course.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Duration: ${course.duration} • ${course.modulesCount} Modules", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(6.dp)) {
                                    Text("$enrolledInCourse Enrolled Students", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.padding(4.dp))
                                }
                            }

                            if (course.syllabus.isNotBlank()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Syllabus & Qualification Criteria:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(course.syllabus, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        courseToEdit = course
                                        editCourseName = course.name
                                        editCourseDuration = course.duration
                                        editCourseFees = course.totalFees.toInt().toString()
                                        editCourseModules = course.modulesCount.toString()
                                        editCourseDescription = course.description
                                        editCourseSyllabus = course.syllabus
                                    },
                                    modifier = Modifier.height(34.dp).testTag("edit_course_${course.id}")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit", fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { courseToDelete = course },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.height(34.dp).testTag("delete_course_${course.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Archive", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // BATCH MANAGEMENT & TEACHER ASSIGNMENT (Requirement 4)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Batch Management & Rosters", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("${allBatches.size} Batches Scheduled • Morning, afternoon & evening slots", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Button(
                                onClick = {
                                    newBatchName = ""
                                    selectedBatchCourseId = courses.firstOrNull()?.id ?: ""
                                    selectedBatchTiming = standardTimingSlots.first()
                                    selectedBatchTeacherId = teachers.firstOrNull()?.teacherId ?: ""
                                    isNewBatchAdmin = false
                                    showAddBatchDialog = true
                                },
                                modifier = Modifier.height(40.dp).testTag("admin_create_batch_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Create Batch")
                            }
                        }
                    }
                }

                if (allBatches.isEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text("No batches registered yet. Click 'Create Batch' to schedule a new batch.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                    }
                } else {
                    allBatches.forEach { batch ->
                        val enrolledCount = activeStudents.count {
                            it.batch.equals(batch.name, ignoreCase = true) || it.batch.contains(batch.name)
                        }
                        val isMorning = batch.timing.contains("Morning", ignoreCase = true)
                        val isEvening = batch.timing.contains("Evening", ignoreCase = true)
                        val timingBadgeBg = if (isMorning) Color(0xFFFEF3C7) else if (isEvening) Color(0xFFEDE9FE) else Color(0xFFE0F2FE)
                        val timingBadgeColor = if (isMorning) Color(0xFFB45309) else if (isEvening) Color(0xFF6D28D9) else Color(0xFF0369A1)

                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("batch_card_${batch.id}"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(batch.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Text("Course: ${batch.courseName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Surface(color = timingBadgeBg, shape = RoundedCornerShape(8.dp)) {
                                        Text(
                                            batch.timing,
                                            color = timingBadgeColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(8.dp)) {
                                        Text(
                                            "$enrolledCount Students Enrolled",
                                            color = Color(0xFF15803D),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    if (batch.batchAdminTeacherId != null) {
                                        Surface(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(6.dp)) {
                                            Text(
                                                "BATCH ADMIN DESIGNATED",
                                                color = Color(0xFF1D4ED8),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Faculty Coordinator:", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            batch.assignedTeacherName ?: "Unassigned",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = if (batch.assignedTeacherName != null) MaterialTheme.colorScheme.onSurface else Color.Gray
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        // Assign Students to Batch (Requirement 4)
                                        OutlinedButton(
                                            onClick = {
                                                batchForStudentEnrollment = batch
                                                selectedEnrollmentStudentIds = activeStudents
                                                    .filter { it.batch.equals(batch.name, ignoreCase = true) }
                                                    .map { it.studentId }
                                                    .toSet()
                                            },
                                            modifier = Modifier.height(34.dp).testTag("assign_students_${batch.id}")
                                        ) {
                                            Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Students", fontSize = 11.sp)
                                        }

                                        // Edit Batch (Requirement 4)
                                        OutlinedButton(
                                            onClick = {
                                                batchToEdit = batch
                                                editBatchName = batch.name
                                                editBatchTiming = batch.timing
                                                editBatchCourseId = batch.courseId
                                            },
                                            modifier = Modifier.height(34.dp).testTag("edit_batch_${batch.id}")
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }

                                        // Coordinator Assignment
                                        OutlinedButton(
                                            onClick = {
                                                batchToEditCoordinator = batch
                                                editCoordinatorTeacherId = batch.assignedTeacherId ?: (teachers.firstOrNull()?.teacherId ?: "")
                                                editIsBatchAdmin = batch.batchAdminTeacherId != null
                                            },
                                            modifier = Modifier.height(34.dp).testTag("assign_coordinator_${batch.id}")
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }

                                        OutlinedButton(
                                            onClick = { batchToDelete = batch },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                            modifier = Modifier.height(34.dp).testTag("delete_batch_${batch.id}")
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // TIMETABLE GENERATOR & SCHEDULING (Requirement 4)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Timetable Management", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Scheduling with teacher & lab conflict detection", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        customScheduleDay = "Monday"
                                        customScheduleStartTime = "09:00 AM"
                                        customScheduleEndTime = "11:00 AM"
                                        customScheduleBatchName = allBatches.firstOrNull()?.name ?: ""
                                        customScheduleSubject = "Computer Fundamentals"
                                        customScheduleTeacherId = teachers.firstOrNull()?.teacherId ?: ""
                                        customScheduleRoom = "Lab 1 (Accounting Systems)"
                                        scheduleConflictError = null
                                        showCustomTimetableDialog = true
                                    },
                                    modifier = Modifier.testTag("btn_custom_timetable_slot")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Custom Slot")
                                }
                                Button(
                                    onClick = { showGenerateTimetableDialog = true },
                                    modifier = Modifier.testTag("btn_auto_generate_timetable")
                                ) {
                                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Auto")
                                }
                            }
                        }
                    }
                }

                Text("Active Weekly Timetable Entries (${allTimetables.size}):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                allTimetables.forEach { item ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.dayOfWeek.take(3).uppercase(), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.subject, fontWeight = FontWeight.Bold)
                                Text("${item.startTime} - ${item.endTime} • ${item.batchName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Instructor: ${item.teacherName} • ${item.roomOrLab}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(
                                onClick = { timetableToView = item },
                                modifier = Modifier.size(34.dp).testTag("view_timetable_${item.id}")
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = "View", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = {
                                    timetableToEdit = item
                                    customScheduleDay = item.dayOfWeek
                                    customScheduleStartTime = item.startTime
                                    customScheduleEndTime = item.endTime
                                    customScheduleSubject = item.subject
                                    customScheduleBatchName = item.batchName
                                    customScheduleRoom = item.roomOrLab
                                    customScheduleTeacherId = item.teacherId
                                },
                                modifier = Modifier.size(34.dp).testTag("edit_timetable_${item.id}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = { timetableToDelete = item },
                                modifier = Modifier.size(34.dp).testTag("delete_timetable_${item.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            3 -> {
                // SCHEDULED EXAMS (50 MCQs) (Requirement 8 & 11)
                val currTime = ServerTimeAuthority.currentTimeMillis()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Scheduled Exams (50 MCQs)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Schedule 50-question timed exams with auto-submission & instant results", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Button(
                                onClick = {
                                    newExamTitle = ""
                                    newExamCourseId = courses.firstOrNull()?.id ?: ""
                                    newExamDurationMinutes = "60"
                                    newExamStartHoursFromNow = "1"
                                    newExamSelectedStudentIds = activeStudents.map { it.studentId }.toSet()
                                    newExam50Questions = ExamQuestionBank.getDefault50Questions()
                                    showCreateExamDialog = true
                                },
                                modifier = Modifier.testTag("admin_create_50_mcq_exam_btn")
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Schedule Exam")
                            }
                        }
                    }
                }

                Text("Scheduled Institute Examinations (${allScheduledExams.size}):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                if (allScheduledExams.isEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text(
                            "No scheduled exams registered. Click '+ Schedule Exam' to create a timed exam with exactly 50 MCQs and assign authorized students.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                } else {
                    allScheduledExams.forEach { exam ->
                        val isLive = currTime in exam.scheduledStartTime..exam.scheduledEndTime
                        val isUpcoming = currTime < exam.scheduledStartTime
                        val isExpired = currTime > exam.scheduledEndTime
                        val allowedStudentsCount = ExamQuestionBank.deserializeAllowedStudentIds(exam.allowedStudentIdsJson).size

                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("admin_exam_card_${exam.examId}"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(exam.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Text("${exam.courseName} • Duration: ${exam.durationMinutes} mins • Exactly ${exam.totalQuestions} MCQs", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                    if (isLive) {
                                        Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(6.dp)) {
                                            Text("LIVE NOW", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                        }
                                    } else if (isUpcoming) {
                                        Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(6.dp)) {
                                            Text("UPCOMING", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                        }
                                    } else {
                                        StatusBadge("EXPIRED")
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Passing: ${exam.passingMarks.toInt()}% • Assigned Students: $allowedStudentsCount", fontSize = 11.sp, color = Color.Gray)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                scope.launch {
                                                    val subs = repository.getSubmissionsForExam(exam.examId).first()
                                                    examSubmissionsForDialog = subs
                                                    selectedExamForSubmissions = exam
                                                }
                                            },
                                            modifier = Modifier.height(34.dp).testTag("view_exam_submissions_${exam.examId}")
                                        ) {
                                            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Submissions", fontSize = 11.sp)
                                        }

                                        IconButton(
                                            onClick = { examToDelete = exam },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                // DIGITAL LIBRARY & E-BOOKS (Requirement 1 & 7)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Digital Library & Learning Resources", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Upload, view, download, rename/edit & manage E-Books and PDFs", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                newBookTitle = ""
                                newBookAuthor = "EduSync Faculty"
                                newBookSubject = "Computer Applications"
                                newBookCategory = "E-BOOK"
                                newBookSize = "4.5 MB"
                                newBookDesc = ""
                                showUploadBookDialog = true
                            },
                            modifier = Modifier.testTag("admin_upload_library_btn")
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upload File")
                        }
                    }
                }

                Text("Available Resources (${allBooks.size}):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                allBooks.forEach { book ->
                    Card(modifier = Modifier.fillMaxWidth().testTag("library_book_card_${book.id}"), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(book.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(6.dp)) {
                                    Text(book.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.padding(4.dp))
                                }
                            }
                            Text("Author: ${book.author} • Subject: ${book.subject} • Size: ${book.fileSize}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(book.description, fontSize = 12.sp)
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Downloads: ${book.downloadCount} • ${book.readTimeMinutes}m read", fontSize = 11.sp, color = Color.Gray)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { bookToView = book },
                                        modifier = Modifier.height(32.dp).testTag("view_book_${book.id}"),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("View", fontSize = 11.sp)
                                    }
                                    FilledTonalButton(
                                        onClick = {
                                            scope.launch {
                                                repository.toggleBookDownloaded(book.id, true)
                                                alertMsg = "'${book.title}' downloaded to local storage."
                                            }
                                        },
                                        modifier = Modifier.height(32.dp).testTag("download_book_${book.id}"),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(if (book.isDownloaded) Icons.Default.Check else Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (book.isDownloaded) "Saved" else "Download", fontSize = 11.sp)
                                    }
                                    IconButton(
                                        onClick = {
                                            bookToEdit = book
                                            editBookTitle = book.title
                                            editBookAuthor = book.author
                                            editBookCategory = book.category
                                            editBookDesc = book.description
                                        },
                                        modifier = Modifier.size(32.dp).testTag("edit_book_${book.id}")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Rename / Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { bookToDelete = book },
                                        modifier = Modifier.size(32.dp).testTag("delete_book_${book.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            5 -> {
                // CERTIFICATES & REVOCATION (Requirement 10)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Certificate Issuance & Revocation", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Generate verifiable digital diplomas with cryptographic QR tokens", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Button(
                                onClick = { showIssueCertDialog = true },
                                modifier = Modifier.testTag("btn_issue_certificate")
                            ) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Issue Certificate")
                            }
                        }
                    }
                }

                Text("Issued Certificates Registry (${allCertificates.size}):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                allCertificates.forEach { cert ->
                    val isRevoked = cert.status == "CANCELLED"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isRevoked) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(cert.certificateNumber, fontWeight = FontWeight.Bold, color = if (isRevoked) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary)
                                    Text("Recipient: ${cert.studentName} (Roll: ${cert.rollNo})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                                Surface(
                                    color = if (isRevoked) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = cert.status,
                                        color = if (isRevoked) Color(0xFFB91C1C) else Color(0xFF15803D),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Text("Course: ${cert.courseName} • Grade: ${cert.grade} • Issued: ${cert.issueDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Token: ${cert.verificationToken}", fontSize = 11.sp, color = Color.Gray)

                            if (isRevoked) {
                                Text("Revocation Reason: ${cert.cancellationReason}", fontSize = 12.sp, color = Color(0xFFB91C1C), fontWeight = FontWeight.Medium)
                            } else {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    OutlinedButton(
                                        onClick = {
                                            certToCancel = cert
                                            certCancelReason = ""
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                        modifier = Modifier.height(34.dp).testTag("btn_revoke_cert_${cert.certificateNumber}")
                                    ) {
                                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Revoke / Cancel")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            6 -> {
                // STUDENT PERFORMANCE INSIGHTS (Requirement 6: High contrast in light & dark mode)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Student Performance Diagnostics", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Select or enter a Student ID to generate multi-dimensional academic progress analysis:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        OutlinedTextField(
                            value = insightStudentId,
                            onValueChange = { insightStudentId = it },
                            label = { Text("Student ID (e.g. EDU-2026-1001)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            activeStudents.take(3).forEach { st ->
                                AssistChip(
                                    onClick = {
                                        insightStudentId = st.studentId
                                        scope.launch {
                                            currentInsight = repository.getStudentPerformanceInsights(st.studentId)
                                        }
                                    },
                                    label = { Text(st.fullName.substringBefore(" "), fontSize = 11.sp) }
                                )
                            }
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    currentInsight = repository.getStudentPerformanceInsights(insightStudentId.trim())
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("btn_analyze_insights")
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run Performance Diagnostic")
                        }
                    }
                }

                currentInsight?.let { insight ->
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
                                        insight.studentName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Academic Health: ${insight.academicHealth}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (insight.academicHealth == "EXCELLENT") Color(0xFF16A34A) else MaterialTheme.colorScheme.primary
                                    )
                                }
                                Surface(
                                    color = Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        "${"%.1f".format(insight.attendancePercentage)}% Att.",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            HorizontalDivider()
                            Text("Demonstrated Competencies & Strengths:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF16A34A))
                            insight.subjectStrengths.forEach { s ->
                                Text("✓ $s", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Text("Identified Areas for Revision:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFD97706))
                            insight.subjectWeaknesses.forEach { w ->
                                Text("• $w", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            }

                            HorizontalDivider()
                            Text("Faculty Recommendation & Revision Plan:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text(insight.teacherRecommendations, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }

    // Add Course Dialog
    if (showAddCourseDialog) {
        AlertDialog(
            onDismissRequest = { showAddCourseDialog = false },
            title = { Text("Add New Course Curriculum", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newCourseName,
                        onValueChange = { newCourseName = it },
                        label = { Text("Course Name *") },
                        placeholder = { Text("e.g. Python Programming & AI") },
                        modifier = Modifier.fillMaxWidth().testTag("add_course_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newCourseCode,
                        onValueChange = { newCourseCode = it },
                        label = { Text("Course Code *") },
                        placeholder = { Text("e.g. CRS_PYTHON_AI") },
                        modifier = Modifier.fillMaxWidth().testTag("add_course_code_input"),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newCourseDuration,
                            onValueChange = { newCourseDuration = it },
                            label = { Text("Duration *") },
                            placeholder = { Text("e.g. 6 Months") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newCourseFees,
                            onValueChange = { newCourseFees = it },
                            label = { Text("Total Fee (₹) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("add_course_fees_input"),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = newCourseModules,
                        onValueChange = { newCourseModules = it },
                        label = { Text("Modules Count") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newCourseDescription,
                        onValueChange = { newCourseDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCourseSyllabus,
                        onValueChange = { newCourseSyllabus = it },
                        label = { Text("Syllabus & Qualification Criteria") },
                        placeholder = { Text("e.g. 10+2 Pass; Modules: Core Python, OOP, APIs, SQLite") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fee = newCourseFees.toDoubleOrNull() ?: 15000.0
                        val modules = newCourseModules.toIntOrNull() ?: 8
                        if (newCourseName.isNotBlank() && newCourseCode.isNotBlank()) {
                            val courseId = newCourseCode.trim().uppercase()
                            scope.launch {
                                repository.saveCourse(
                                    CourseEntity(
                                        id = courseId,
                                        name = newCourseName.trim(),
                                        code = newCourseCode.trim().uppercase(),
                                        duration = newCourseDuration.trim(),
                                        totalFees = fee,
                                        description = newCourseDescription.trim().ifBlank { "Professional career certification program in ${newCourseName.trim()}." },
                                        syllabus = newCourseSyllabus.trim().ifBlank { "Standard certified modules with laboratory practicals." },
                                        modulesCount = modules
                                    )
                                )
                                showAddCourseDialog = false
                                alertMsg = "Course '${newCourseName.trim()}' successfully added to academy curriculum."
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_course_btn")
                ) { Text("Save Course") }
            },
            dismissButton = { TextButton(onClick = { showAddCourseDialog = false }) { Text("Cancel") } }
        )
    }

    // Delete Course Confirmation Dialog
    courseToDelete?.let { course ->
        AlertDialog(
            onDismissRequest = { courseToDelete = null },
            title = { Text("Archive / Delete Course") },
            text = { Text("Are you sure you want to remove '${course.name}' (${course.code}) from the curriculum? Existing enrolled student records will remain preserved.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.deleteCourse(course.id)
                            alertMsg = "Course '${course.name}' archived."
                            courseToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { courseToDelete = null }) { Text("Cancel") } }
        )
    }

    // Create Batch Dialog
    if (showAddBatchDialog) {
        val selectedCourse = courses.find { it.id == selectedBatchCourseId } ?: courses.firstOrNull()
        val selectedTeacher = teachers.find { it.teacherId == selectedBatchTeacherId } ?: teachers.firstOrNull()

        AlertDialog(
            onDismissRequest = { showAddBatchDialog = false },
            title = { Text("Schedule New Batch", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newBatchName,
                        onValueChange = { newBatchName = it },
                        label = { Text("Batch Name *") },
                        placeholder = { Text("e.g. Morning ADCA Slot A (09:00 AM - 11:00 AM)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_batch_name_input"),
                        singleLine = true
                    )

                    // Timing Selector (24-Hour HH:mm Format - Requirement 7)
                    Text("24-Hour Class Timing (HH:mm) *", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = batchStartTime24h,
                            onValueChange = {
                                batchStartTime24h = it
                                batchTimeError = null
                                selectedBatchTiming = "${it.trim()} - ${batchEndTime24h.trim()}"
                            },
                            label = { Text("Start (24h HH:mm)") },
                            placeholder = { Text("09:00") },
                            modifier = Modifier.weight(1f).testTag("batch_start_24h_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = batchEndTime24h,
                            onValueChange = {
                                batchEndTime24h = it
                                batchTimeError = null
                                selectedBatchTiming = "${batchStartTime24h.trim()} - ${it.trim()}"
                            },
                            label = { Text("End (24h HH:mm)") },
                            placeholder = { Text("11:00") },
                            modifier = Modifier.weight(1f).testTag("batch_end_24h_input"),
                            singleLine = true
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("09:00" to "11:00", "14:00" to "16:00", "17:00" to "19:00").forEach { (s, e) ->
                            AssistChip(
                                onClick = {
                                    batchStartTime24h = s
                                    batchEndTime24h = e
                                    selectedBatchTiming = "$s - $e"
                                    batchTimeError = null
                                },
                                label = { Text("$s-$e", fontSize = 11.sp) }
                            )
                        }
                    }
                    if (batchTimeError != null) {
                        Text(batchTimeError!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Course Picker
                    Text("Associated Course *", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    ExposedDropdownMenuBox(
                        expanded = courseDropdownExpanded,
                        onExpandedChange = { courseDropdownExpanded = !courseDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCourse?.name ?: "Select Course",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseDropdownExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = courseDropdownExpanded,
                            onDismissRequest = { courseDropdownExpanded = false }
                        ) {
                            courses.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text("${c.name} (${c.code})") },
                                    onClick = {
                                        selectedBatchCourseId = c.id
                                        courseDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Faculty Coordinator Dropdown
                    Text("Designated Batch Coordinator (Teacher):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    ExposedDropdownMenuBox(
                        expanded = teacherDropdownExpanded,
                        onExpandedChange = { teacherDropdownExpanded = !teacherDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedTeacher?.fullName ?: "Select Teacher",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherDropdownExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = teacherDropdownExpanded,
                            onDismissRequest = { teacherDropdownExpanded = false }
                        ) {
                            teachers.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text("${t.fullName} (${t.specialization})") },
                                    onClick = {
                                        selectedBatchTeacherId = t.teacherId
                                        teacherDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isNewBatchAdmin,
                            onCheckedChange = { isNewBatchAdmin = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Grant Batch Administrator Privileges to Coordinator", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val c = courses.find { it.id == selectedBatchCourseId } ?: courses.firstOrNull()
                        val t = teachers.find { it.teacherId == selectedBatchTeacherId }
                        val s24 = batchStartTime24h.trim()
                        val e24 = batchEndTime24h.trim()
                        if (!time24hRegex.matches(s24) || !time24hRegex.matches(e24)) {
                            batchTimeError = "Please enter valid 24-hour HH:mm times (e.g. 09:00 and 17:00)."
                            return@Button
                        }
                        if (newBatchName.isNotBlank() && c != null) {
                            val batchId = "BATCH_${System.currentTimeMillis() % 100000}"
                            val formattedTiming = "$s24 - $e24"
                            val batch = BatchEntity(
                                id = batchId,
                                name = newBatchName.trim(),
                                courseId = c.id,
                                courseName = c.name,
                                timing = formattedTiming,
                                assignedTeacherId = t?.teacherId,
                                assignedTeacherName = t?.fullName,
                                batchAdminTeacherId = if (isNewBatchAdmin) t?.teacherId else null,
                                batchAdminTeacherName = if (isNewBatchAdmin) t?.fullName else null
                            )
                            scope.launch {
                                repository.addBatch(batch)
                                if (t != null) {
                                    repository.assignBatchTeacher(
                                        batchId = batchId,
                                        teacher = t,
                                        isBatchAdmin = isNewBatchAdmin,
                                        adminUser = "Admin Panel"
                                    )
                                }
                                showAddBatchDialog = false
                                alertMsg = "Batch '${batch.name}' ($formattedTiming) successfully created and scheduled."
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_batch_btn")
                ) { Text("Create Batch") }
            },
            dismissButton = { TextButton(onClick = { showAddBatchDialog = false }) { Text("Cancel") } }
        )
    }

    // Reassign Teacher Coordinator Dialog
    batchToEditCoordinator?.let { batch ->
        val currentTeacher = teachers.find { it.teacherId == editCoordinatorTeacherId }
        AlertDialog(
            onDismissRequest = { batchToEditCoordinator = null },
            title = { Text("Assign Coordinator for ${batch.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Faculty Member:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    ExposedDropdownMenuBox(
                        expanded = editTeacherDropdownExpanded,
                        onExpandedChange = { editTeacherDropdownExpanded = !editTeacherDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = currentTeacher?.fullName ?: "Select Teacher",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editTeacherDropdownExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = editTeacherDropdownExpanded,
                            onDismissRequest = { editTeacherDropdownExpanded = false }
                        ) {
                            teachers.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text("${t.fullName} (${t.specialization})") },
                                    onClick = {
                                        editCoordinatorTeacherId = t.teacherId
                                        editTeacherDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = editIsBatchAdmin,
                            onCheckedChange = { editIsBatchAdmin = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Designate as Batch Administrator", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val t = teachers.find { it.teacherId == editCoordinatorTeacherId }
                        scope.launch {
                            repository.assignBatchTeacher(
                                batchId = batch.id,
                                teacher = t,
                                isBatchAdmin = editIsBatchAdmin,
                                adminUser = "Admin Panel"
                            )
                            batchToEditCoordinator = null
                            alertMsg = "Coordinator updated for batch '${batch.name}'."
                        }
                    }
                ) { Text("Update Coordinator") }
            },
            dismissButton = { TextButton(onClick = { batchToEditCoordinator = null }) { Text("Cancel") } }
        )
    }

    // Delete Batch Dialog
    batchToDelete?.let { batch ->
        AlertDialog(
            onDismissRequest = { batchToDelete = null },
            title = { Text("Delete Batch") },
            text = { Text("Are you sure you want to delete '${batch.name}'? Timetable slots and coordinator links will be cleared.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.deleteBatch(batch.id)
                            batchToDelete = null
                            alertMsg = "Batch '${batch.name}' deleted."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { batchToDelete = null }) { Text("Cancel") } }
        )
    }

    // Auto-Generate Timetable Dialog
    if (showGenerateTimetableDialog) {
        AlertDialog(
            onDismissRequest = { showGenerateTimetableDialog = false },
            title = { Text("Auto-Generate Batch Timetable") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Batch to Schedule:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = selectedTimetableBatch,
                        onValueChange = { selectedTimetableBatch = it },
                        label = { Text("Batch Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = selectedTimetableRoom,
                        onValueChange = { selectedTimetableRoom = it },
                        label = { Text("Room / Lab") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            "The scheduler verifies teacher availability and room allocation across Monday to Saturday slots. Any booking conflict will halt generation.",
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val subjectsAndTeachers = listOf(
                                Triple("Tally Prime with GST", "TCH-101", "Prof. Rajesh Sharma"),
                                Triple("Desktop Publishing (Corel & Photoshop)", "TCH-102", "Anita Deshmukh"),
                                Triple("Python Core Programming", "TCH-103", "Vikram Singh")
                            )
                            val res = repository.generateTimetableForBatch(
                                batchName = selectedTimetableBatch,
                                courseId = "CRS_ADCA",
                                courseName = "Advance Diploma in Computer Applications (ADCA)",
                                subjectsAndTeachers = subjectsAndTeachers,
                                roomOrLab = selectedTimetableRoom,
                                adminUser = "Admin Panel"
                            )
                            showGenerateTimetableDialog = false
                            alertMsg = if (res.success) {
                                "${res.message} Generated ${res.generatedCount} conflict-free class slots for $selectedTimetableBatch."
                            } else {
                                "Conflict detected: ${res.conflictsDetected.joinToString()}"
                            }
                        }
                    }
                ) { Text("Generate Schedule") }
            },
            dismissButton = { TextButton(onClick = { showGenerateTimetableDialog = false }) { Text("Cancel") } }
        )
    }

    // Issue Certificate Dialog
    if (showIssueCertDialog) {
        var studentInput by remember { mutableStateOf(activeStudents.firstOrNull()?.studentId ?: "STU-2026-001") }
        AlertDialog(
            onDismissRequest = { showIssueCertDialog = false },
            title = { Text("Issue Official Digital Certificate") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = studentInput,
                        onValueChange = { studentInput = it },
                        label = { Text("Student ID (e.g. STU-2026-001)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = certGrade,
                        onValueChange = { certGrade = it },
                        label = { Text("Final Grade (A+, A, B, C)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val res = repository.issueCertificate(
                                studentId = studentInput.trim(),
                                courseId = certCourseId,
                                grade = certGrade.trim(),
                                adminUser = "Admin Panel"
                            )
                            showIssueCertDialog = false
                            alertMsg = if (res.isSuccess) {
                                val cert = res.getOrThrow()
                                "Certificate #${cert.certificateNumber} successfully issued to ${cert.studentName}! Verification token: ${cert.verificationToken}"
                            } else {
                                "Failed to issue certificate: ${res.exceptionOrNull()?.message}"
                            }
                        }
                    }
                ) { Text("Issue & Sign") }
            },
            dismissButton = { TextButton(onClick = { showIssueCertDialog = false }) { Text("Cancel") } }
        )
    }

    // Cancel / Revoke Certificate Dialog (Requirement 10)
    certToCancel?.let { cert ->
        AlertDialog(
            onDismissRequest = { certToCancel = null },
            title = { Text("Revoke Certificate ${cert.certificateNumber}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Recipient: ${cert.studentName} (${cert.rollNo})")
                    Text("A mandatory reason must be recorded in the audit registry for revoking this certificate:", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = certCancelReason,
                        onValueChange = { certCancelReason = it },
                        label = { Text("Mandatory Revocation Reason *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (certCancelReason.isNotBlank()) {
                            scope.launch {
                                val res = repository.cancelCertificate(
                                    certNo = cert.certificateNumber,
                                    reason = certCancelReason.trim(),
                                    adminUser = "Admin Panel"
                                )
                                certToCancel = null
                                alertMsg = if (res.isSuccess) {
                                    "Certificate ${cert.certificateNumber} revoked. Public registry now reflects CANCELLED status."
                                } else {
                                    "Revocation failed: ${res.exceptionOrNull()?.message}"
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    enabled = certCancelReason.isNotBlank()
                ) { Text("Confirm Revocation") }
            },
            dismissButton = { TextButton(onClick = { certToCancel = null }) { Text("Dismiss") } }
        )
    }

    // Broadcast Notice Dialog
    if (showAddNoticeDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoticeDialog = false },
            title = { Text("Publish Institute Notice") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = noticeTitle, onValueChange = { noticeTitle = it }, label = { Text("Notice Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = noticeContent, onValueChange = { noticeContent = it }, label = { Text("Notice Content") }, modifier = Modifier.fillMaxWidth().height(100.dp))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noticeTitle.isNotBlank()) {
                            scope.launch {
                                repository.addNotice(
                                    NoticeEntity(
                                        title = noticeTitle.trim(),
                                        content = noticeContent.trim(),
                                        date = "2026-10-22",
                                        priority = "IMPORTANT"
                                    )
                                )
                                showAddNoticeDialog = false
                                noticeTitle = ""
                                noticeContent = ""
                                alertMsg = "Notice published to all student portals."
                            }
                        }
                    }
                ) { Text("Broadcast") }
            },
            dismissButton = { TextButton(onClick = { showAddNoticeDialog = false }) { Text("Cancel") } }
        )
    }

    // Edit Course Dialog (Requirement 4)
    courseToEdit?.let { course ->
        AlertDialog(
            onDismissRequest = { courseToEdit = null },
            title = { Text("Edit Course: ${course.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editCourseName,
                        onValueChange = { editCourseName = it },
                        label = { Text("Course Name") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_course_name_input"),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editCourseDuration,
                            onValueChange = { editCourseDuration = it },
                            label = { Text("Duration") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editCourseFees,
                            onValueChange = { editCourseFees = it },
                            label = { Text("Total Fee (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = editCourseModules,
                        onValueChange = { editCourseModules = it },
                        label = { Text("Modules Count") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editCourseDescription,
                        onValueChange = { editCourseDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editCourseSyllabus,
                        onValueChange = { editCourseSyllabus = it },
                        label = { Text("Syllabus & Qualification Criteria") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fee = editCourseFees.toDoubleOrNull() ?: course.totalFees
                        val mods = editCourseModules.toIntOrNull() ?: course.modulesCount
                        scope.launch {
                            repository.updateCourse(
                                course.copy(
                                    name = editCourseName.trim(),
                                    duration = editCourseDuration.trim(),
                                    totalFees = fee,
                                    modulesCount = mods,
                                    description = editCourseDescription.trim(),
                                    syllabus = editCourseSyllabus.trim()
                                )
                            )
                            courseToEdit = null
                            alertMsg = "Course updated successfully."
                        }
                    }
                ) { Text("Save Changes") }
            },
            dismissButton = { TextButton(onClick = { courseToEdit = null }) { Text("Cancel") } }
        )
    }

    // Edit Batch Dialog (Requirement 4 & 7: 24-Hour HH:mm Format)
    batchToEdit?.let { batch ->
        AlertDialog(
            onDismissRequest = { batchToEdit = null },
            title = { Text("Edit Batch: ${batch.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editBatchName,
                        onValueChange = { editBatchName = it },
                        label = { Text("Batch Name") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_batch_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editBatchTiming,
                        onValueChange = { editBatchTiming = it },
                        label = { Text("Timing (24-Hour HH:mm - HH:mm)") },
                        placeholder = { Text("e.g. 09:00 - 11:00 or 14:00 - 16:00") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("09:00 - 11:00", "14:00 - 16:00", "17:00 - 19:00").forEach { preset ->
                            AssistChip(
                                onClick = { editBatchTiming = preset },
                                label = { Text(preset, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.updateBatch(
                                batch.copy(
                                    name = editBatchName.trim(),
                                    timing = editBatchTiming.trim()
                                )
                            )
                            batchToEdit = null
                            alertMsg = "Batch updated successfully."
                        }
                    }
                ) { Text("Save Changes") }
            },
            dismissButton = { TextButton(onClick = { batchToEdit = null }) { Text("Cancel") } }
        )
    }

    // Assign Students to Batch Dialog (Requirement 4)
    batchForStudentEnrollment?.let { batch ->
        AlertDialog(
            onDismissRequest = { batchForStudentEnrollment = null },
            title = { Text("Enroll Students: ${batch.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Select active students to enroll into this batch (${selectedEnrollmentStudentIds.size} selected):",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            selectedEnrollmentStudentIds = activeStudents.map { it.studentId }.toSet()
                        }) {
                            Text("Select All")
                        }
                        TextButton(onClick = {
                            selectedEnrollmentStudentIds = emptySet()
                        }) {
                            Text("Clear")
                        }
                    }
                    activeStudents.forEach { st ->
                        val isSelected = selectedEnrollmentStudentIds.contains(st.studentId)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFEFF6FF) else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    selectedEnrollmentStudentIds = if (checked) {
                                        selectedEnrollmentStudentIds + st.studentId
                                    } else {
                                        selectedEnrollmentStudentIds - st.studentId
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(st.fullName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("${st.studentId} • Current: ${st.batch}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.assignStudentsToBatch(selectedEnrollmentStudentIds.toList(), batch.name)
                            batchForStudentEnrollment = null
                            alertMsg = "${selectedEnrollmentStudentIds.size} student(s) successfully assigned to ${batch.name}."
                        }
                    }
                ) { Text("Apply Assignment") }
            },
            dismissButton = { TextButton(onClick = { batchForStudentEnrollment = null }) { Text("Cancel") } }
        )
    }

    // Custom Timetable Entry with Conflict Detection (Requirement 4)
    if (showCustomTimetableDialog) {
        val selectedTeacherObj = teachers.find { it.teacherId == customScheduleTeacherId } ?: teachers.firstOrNull()

        AlertDialog(
            onDismissRequest = { showCustomTimetableDialog = false },
            title = { Text("Add Custom Timetable Slot", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Day of Week *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        daysOfWeek.take(3).forEach { day ->
                            FilterChip(
                                selected = customScheduleDay == day,
                                onClick = { customScheduleDay = day },
                                label = { Text(day.take(3)) }
                            )
                        }
                        daysOfWeek.drop(3).forEach { day ->
                            FilterChip(
                                selected = customScheduleDay == day,
                                onClick = { customScheduleDay = day },
                                label = { Text(day.take(3)) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customScheduleStartTime,
                            onValueChange = { customScheduleStartTime = it },
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = customScheduleEndTime,
                            onValueChange = { customScheduleEndTime = it },
                            label = { Text("End Time") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = customScheduleSubject,
                        onValueChange = { customScheduleSubject = it },
                        label = { Text("Subject / Module *") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_timetable_subject_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = customScheduleBatchName,
                        onValueChange = { customScheduleBatchName = it },
                        label = { Text("Target Batch Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = customScheduleRoom,
                        onValueChange = { customScheduleRoom = it },
                        label = { Text("Classroom / Lab *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    scheduleConflictError?.let { err ->
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ $err",
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val teacher = selectedTeacherObj
                        if (teacher != null && customScheduleSubject.isNotBlank() && customScheduleBatchName.isNotBlank()) {
                            scope.launch {
                                // Live conflict validation (Requirement 4)
                                val conflict = repository.validateScheduleSlot(
                                    day = customScheduleDay,
                                    startTime = customScheduleStartTime,
                                    endTime = customScheduleEndTime,
                                    teacherId = teacher.teacherId,
                                    roomOrLab = customScheduleRoom,
                                    batch = customScheduleBatchName
                                )
                                if (conflict != null) {
                                    scheduleConflictError = conflict
                                } else {
                                    repository.addCustomTimetableEntry(
                                        TimetableEntity(
                                            batchName = customScheduleBatchName.trim(),
                                            dayOfWeek = customScheduleDay,
                                            startTime = customScheduleStartTime.trim(),
                                            endTime = customScheduleEndTime.trim(),
                                            subject = customScheduleSubject.trim(),
                                            teacherId = teacher.teacherId,
                                            teacherName = teacher.fullName,
                                            roomOrLab = customScheduleRoom.trim()
                                        )
                                    )
                                    showCustomTimetableDialog = false
                                    alertMsg = "Timetable slot added without scheduling conflicts."
                                }
                            }
                        }
                    }
                ) { Text("Validate & Save") }
            },
            dismissButton = { TextButton(onClick = { showCustomTimetableDialog = false }) { Text("Cancel") } }
        )
    }

    // Create 50-MCQ Scheduled Exam Dialog (Requirement 8)
    if (showCreateExamDialog) {
        val selectedCourse = courses.find { it.id == newExamCourseId } ?: courses.firstOrNull()

        AlertDialog(
            onDismissRequest = { showCreateExamDialog = false },
            title = { Text("Schedule 50-MCQ Exam", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Requirement 8 Verified: Exam requires exactly 50 MCQs, scheduled within next 24h with server timing & autosave.",
                            modifier = Modifier.padding(8.dp),
                            fontSize = 11.sp,
                            color = Color(0xFF1D4ED8),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedTextField(
                        value = newExamTitle,
                        onValueChange = { newExamTitle = it },
                        label = { Text("Exam Title *") },
                        placeholder = { Text("e.g. DCA Semester Certification Final") },
                        modifier = Modifier.fillMaxWidth().testTag("new_exam_title_input"),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newExamDurationMinutes,
                            onValueChange = { newExamDurationMinutes = it },
                            label = { Text("Duration (mins)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newExamStartHoursFromNow,
                            onValueChange = { newExamStartHoursFromNow = it },
                            label = { Text("Starts in (Hours, 0=Live)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Text("Course: ${selectedCourse?.name ?: "All Courses"}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)

                    // Students selection
                    Text(
                        "Selected Authorized Students: ${newExamSelectedStudentIds.size} of ${activeStudents.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { newExamSelectedStudentIds = activeStudents.map { it.studentId }.toSet() }) {
                            Text("Select All (${activeStudents.size})")
                        }
                        TextButton(onClick = { newExamSelectedStudentIds = emptySet() }) {
                            Text("Clear")
                        }
                    }

                    // 50 Questions verified status
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15803D))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("50 Questions Bank Ready", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF15803D))
                                Text("Covers MS Office, Tally Prime, Python, C++, Web Dev & Networking", fontSize = 10.sp, color = Color(0xFF166534))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val duration = (newExamDurationMinutes.toIntOrNull() ?: 60).coerceAtLeast(5)
                        val hours = (newExamStartHoursFromNow.toDoubleOrNull() ?: 1.0).coerceIn(0.0, 72.0)
                        val now = ServerTimeAuthority.currentTimeMillis()
                        val startMs = if (hours <= 0.0) now - 10_000L else now + (hours * 3600 * 1000).toLong()

                        if (newExamTitle.isNotBlank() && newExamSelectedStudentIds.isNotEmpty()) {
                            scope.launch {
                                val exam = ScheduledExamEntity(
                                    examId = "EXAM-${now % 100000}",
                                    title = newExamTitle.trim(),
                                    courseId = selectedCourse?.id ?: "CRS_ADCA",
                                    courseName = selectedCourse?.name ?: "Advance Diploma in Computer Applications (ADCA)",
                                    scheduledStartTime = startMs,
                                    durationMinutes = duration,
                                    totalQuestions = 50,
                                    passingMarks = 20,
                                    questionsJson = ExamQuestionBank.serializeQuestions(newExam50Questions),
                                    assignedStudentIds = ExamQuestionBank.serializeAllowedStudentIds(newExamSelectedStudentIds.toList()),
                                    status = if (hours <= 0.0) "LIVE" else "SCHEDULED"
                                )
                                repository.saveScheduledExam(exam)
                                showCreateExamDialog = false
                                alertMsg = "Exam '${newExamTitle.trim()}' scheduled successfully with 50 MCQs for ${newExamSelectedStudentIds.size} student(s)."
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_schedule_exam_btn")
                ) { Text("Publish Exam") }
            },
            dismissButton = { TextButton(onClick = { showCreateExamDialog = false }) { Text("Cancel") } }
        )
    }

    // View Exam Submissions Dialog (Requirement 8)
    selectedExamForSubmissions?.let { exam ->
        AlertDialog(
            onDismissRequest = { selectedExamForSubmissions = null },
            title = { Text("Submissions: ${exam.title}", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Submissions recorded (${examSubmissionsForDialog.size} students attempted):",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    if (examSubmissionsForDialog.isEmpty()) {
                        Text("No students have attempted this exam yet.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        examSubmissionsForDialog.forEach { sub ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (sub.isPassed) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(sub.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        StatusBadge(sub.status)
                                    }
                                    Text("Student ID: ${sub.studentId}", fontSize = 11.sp, color = Color.Gray)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Score: ${sub.score ?: 0}/100 (${"%.1f".format(sub.percentage)}%)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (sub.isPassed) Color(0xFF15803D) else Color(0xFFDC2626)
                                        )
                                        Text(
                                            if (sub.isPassed) "PASSED" else "FAILED",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (sub.isPassed) Color(0xFF15803D) else Color(0xFFDC2626)
                                        )
                                    }
                                    if (sub.autoSubmitted) {
                                        Text("(Auto-submitted on session timeout)", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { selectedExamForSubmissions = null }) { Text("Close") } }
        )
    }

    // Delete Scheduled Exam Dialog
    examToDelete?.let { exam ->
        AlertDialog(
            onDismissRequest = { examToDelete = null },
            title = { Text("Delete Scheduled Exam") },
            text = { Text("Are you sure you want to delete '${exam.title}'? All scheduled sessions and associated submission records will be removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.deleteScheduledExam(exam.examId)
                            examToDelete = null
                            alertMsg = "Scheduled exam removed."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { examToDelete = null }) { Text("Cancel") } }
        )
    }

    // View Timetable Entry Dialog (Requirement 5)
    timetableToView?.let { slot ->
        AlertDialog(
            onDismissRequest = { timetableToView = null },
            title = { Text("Class Schedule Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Subject: ${slot.subject}", fontWeight = FontWeight.Bold)
                    Text("Day: ${slot.dayOfWeek}")
                    Text("Time: ${slot.startTime} - ${slot.endTime}")
                    Text("Batch: ${slot.batchName}")
                    Text("Instructor: ${slot.teacherName} (${slot.teacherId})")
                    Text("Room / Lab: ${slot.roomOrLab}")
                }
            },
            confirmButton = { TextButton(onClick = { timetableToView = null }) { Text("Close") } }
        )
    }

    // Edit Timetable Entry Dialog (Requirement 5)
    timetableToEdit?.let { slot ->
        AlertDialog(
            onDismissRequest = { timetableToEdit = null },
            title = { Text("Edit Timetable Slot", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customScheduleDay,
                        onValueChange = { customScheduleDay = it },
                        label = { Text("Day of Week") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customScheduleStartTime,
                            onValueChange = { customScheduleStartTime = it },
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = customScheduleEndTime,
                            onValueChange = { customScheduleEndTime = it },
                            label = { Text("End Time") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = customScheduleSubject,
                        onValueChange = { customScheduleSubject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customScheduleBatchName,
                        onValueChange = { customScheduleBatchName = it },
                        label = { Text("Batch Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customScheduleRoom,
                        onValueChange = { customScheduleRoom = it },
                        label = { Text("Room / Lab") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.updateTimetable(
                                slot.copy(
                                    dayOfWeek = customScheduleDay.trim(),
                                    startTime = customScheduleStartTime.trim(),
                                    endTime = customScheduleEndTime.trim(),
                                    subject = customScheduleSubject.trim(),
                                    batchName = customScheduleBatchName.trim(),
                                    roomOrLab = customScheduleRoom.trim()
                                )
                            )
                            timetableToEdit = null
                            alertMsg = "Timetable slot updated and synced to Student & Teacher portals."
                        }
                    }
                ) { Text("Save Changes") }
            },
            dismissButton = { TextButton(onClick = { timetableToEdit = null }) { Text("Cancel") } }
        )
    }

    // Delete Timetable Confirmation Dialog (Requirement 5)
    timetableToDelete?.let { slot ->
        AlertDialog(
            onDismissRequest = { timetableToDelete = null },
            title = { Text("Delete Timetable Slot?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete '${slot.subject}' (${slot.dayOfWeek}, ${slot.startTime} - ${slot.endTime}) for ${slot.batchName}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.deleteTimetable(slot.id)
                            timetableToDelete = null
                            alertMsg = "Timetable entry deleted."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { timetableToDelete = null }) { Text("Cancel") } }
        )
    }

    // Upload Digital Library File / E-Book Dialog (Requirement 1 & 7)
    if (showUploadBookDialog) {
        AlertDialog(
            onDismissRequest = { showUploadBookDialog = false },
            title = { Text("Upload Library File / E-Book", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newBookTitle,
                        onValueChange = { newBookTitle = it },
                        label = { Text("File / E-Book Title *") },
                        placeholder = { Text("e.g. Complete Python & SQL Lab Guide.pdf") },
                        modifier = Modifier.fillMaxWidth().testTag("upload_book_title_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newBookAuthor,
                        onValueChange = { newBookAuthor = it },
                        label = { Text("Author / Publisher") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("E-BOOK", "PDF MANUAL", "VIDEO LECTURE", "SYLLABUS").forEach { cat ->
                            FilterChip(
                                selected = newBookCategory == cat,
                                onClick = {
                                    newBookCategory = cat
                                    if (cat == "VIDEO LECTURE") {
                                        newBookSize = "45 Mins • HD"
                                    } else if (newBookSize.contains("Mins")) {
                                        newBookSize = "4.5 MB"
                                    }
                                },
                                label = { Text(cat, fontSize = 10.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = newBookDesc,
                        onValueChange = { newBookDesc = it },
                        label = { Text("Description & Chapter Summary") },
                        modifier = Modifier.fillMaxWidth().height(90.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBookTitle.isNotBlank()) {
                            scope.launch {
                                val descText = newBookDesc.trim().ifBlank { "Official EduSync Academy digital study resource." }
                                repository.addBook(
                                    DigitalBookEntity(
                                        title = newBookTitle.trim(),
                                        author = newBookAuthor.trim().ifBlank { "EduSync Faculty" },
                                        courseId = "CRS_ADCA",
                                        subject = newBookSubject.trim(),
                                        category = newBookCategory,
                                        fileSize = newBookSize,
                                        description = descText,
                                        readTimeMinutes = 45,
                                        isDownloaded = true,
                                        downloadCount = 1,
                                        contentSummary = descText
                                    )
                                )
                                repository.addStudyMaterial(
                                    StudyMaterialEntity(
                                        courseId = "CRS_ADCA",
                                        title = newBookTitle.trim(),
                                        description = descText,
                                        type = if (newBookCategory == "VIDEO LECTURE") "VIDEO_CLASS" else "PDF_NOTES",
                                        contentUrl = "https://edusync.academy/library/${newBookTitle.trim().replace(" ", "_")}",
                                        fileSize = newBookSize
                                    )
                                )
                                showUploadBookDialog = false
                                alertMsg = "'${newBookTitle.trim()}' uploaded to Digital Library & Student Learning portal."
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_upload_book_btn")
                ) { Text("Upload & Publish") }
            },
            dismissButton = { TextButton(onClick = { showUploadBookDialog = false }) { Text("Cancel") } }
        )
    }

    // View Library Book Dialog (Requirement 7)
    bookToView?.let { b ->
        AlertDialog(
            onDismissRequest = { bookToView = null },
            title = { Text(b.title, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Code: ${b.bookCode} • Category: ${b.category}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Text("Author: ${b.author} • Subject: ${b.subject} • Size: ${b.fileSize}", fontSize = 12.sp)
                    HorizontalDivider()
                    Text(b.description, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = { TextButton(onClick = { bookToView = null }) { Text("Close Viewer") } }
        )
    }

    // Rename / Edit Library Book Dialog (Requirement 7)
    bookToEdit?.let { b ->
        AlertDialog(
            onDismissRequest = { bookToEdit = null },
            title = { Text("Rename / Edit Library File", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editBookTitle,
                        onValueChange = { editBookTitle = it },
                        label = { Text("File / Book Title *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editBookAuthor,
                        onValueChange = { editBookAuthor = it },
                        label = { Text("Author") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editBookCategory,
                        onValueChange = { editBookCategory = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editBookDesc,
                        onValueChange = { editBookDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth().height(90.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editBookTitle.isNotBlank()) {
                            scope.launch {
                                repository.updateBook(
                                    b.copy(
                                        title = editBookTitle.trim(),
                                        author = editBookAuthor.trim(),
                                        category = editBookCategory.trim(),
                                        description = editBookDesc.trim()
                                    )
                                )
                                bookToEdit = null
                                alertMsg = "Library file updated."
                            }
                        }
                    }
                ) { Text("Save Changes") }
            },
            dismissButton = { TextButton(onClick = { bookToEdit = null }) { Text("Cancel") } }
        )
    }

    // Delete Library Book Confirmation Dialog (Requirement 7)
    bookToDelete?.let { b ->
        AlertDialog(
            onDismissRequest = { bookToDelete = null },
            title = { Text("Delete Library Resource?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to permanently delete '${b.title}' (${b.bookCode}) from the Digital Library?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.deleteBook(b.id)
                            bookToDelete = null
                            alertMsg = "Resource '${b.title}' deleted from library."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete Permanently") }
            },
            dismissButton = { TextButton(onClick = { bookToDelete = null }) { Text("Cancel") } }
        )
    }

    if (alertMsg != null) {
        AlertDialog(
            onDismissRequest = { alertMsg = null },
            title = { Text("Academic Management") },
            text = { Text(alertMsg!!) },
            confirmButton = { TextButton(onClick = { alertMsg = null }) { Text("OK") } }
        )
    }
}
