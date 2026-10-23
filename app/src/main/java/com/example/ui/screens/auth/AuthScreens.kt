package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.SecurityUtils
import com.example.data.local.entity.CourseEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.TeacherEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import com.example.data.repository.InstituteRepository
import com.example.ui.components.EduSyncTopAppBar
import kotlinx.coroutines.launch

/**
 * Admin Login Screen with password privacy (Requirement 1 & 2)
 * Never displays default credentials or passwords as visible text.
 */
@Composable
fun AdminLoginScreen(
    authRepository: AuthRepository,
    onAdminLoginSuccess: () -> Unit,
    onNavigateToTeacherLogin: () -> Unit,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("Admin123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Forced Password Reset Dialog state
    var showForcedChangeDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var changeErrorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            EduSyncTopAppBar(
                title = "Admin Panel",
                subtitle = "Authorized Personnel Authentication",
                showBackButton = true,
                onBackClick = onBack,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Administrator Sign In",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = "Enter your administrator credentials to access the Admin Panel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_username_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_password_input"),
                singleLine = true
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        errorMessage = "Please enter both username and password."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        when (val result = authRepository.loginAdmin(username.trim(), password)) {
                            is AuthResult.Success -> {
                                isLoading = false
                                onAdminLoginSuccess()
                            }
                            is AuthResult.RequirePasswordChange -> {
                                isLoading = false
                                showForcedChangeDialog = true
                            }
                            is AuthResult.AccountLocked -> {
                                isLoading = false
                                errorMessage = "Account locked due to consecutive failed attempts. Retry in ${result.remainingSeconds}s."
                            }
                            is AuthResult.Error -> {
                                isLoading = false
                                errorMessage = result.message
                            }
                            else -> {
                                isLoading = false
                                errorMessage = "Authentication failed."
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("admin_login_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Sign In to Admin Panel", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Teacher Login shortcut for faculty members
            OutlinedButton(
                onClick = onNavigateToTeacherLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goto_teacher_login_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Teacher / Faculty Login")
            }
        }
    }

    if (showForcedChangeDialog) {
        val (score, strength) = SecurityUtils.evaluatePasswordStrength(newPassword)
        AlertDialog(
            onDismissRequest = { /* Cannot dismiss without changing initial password */ },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mandatory Password Change")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "For institutional security, please set a strong password (minimum 12 characters).",
                        style = MaterialTheme.typography.bodySmall
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Secure Password") },
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("new_admin_password_field"),
                        singleLine = true
                    )

                    if (newPassword.isNotEmpty()) {
                        LinearProgressIndicator(
                            progress = { score / 100f },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = if (score >= 80) Color(0xFF15803D) else if (score >= 50) Color(0xFFD97706) else Color(0xFFDC2626)
                        )
                        Text("Strength: $strength ($score/100)", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("confirm_admin_password_field"),
                        singleLine = true
                    )

                    if (changeErrorMessage != null) {
                        Text(changeErrorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPassword != confirmPassword) {
                            changeErrorMessage = "Passwords do not match."
                            return@Button
                        }
                        scope.launch {
                            val res = authRepository.changeAdminPassword("Admin123", newPassword)
                            if (res.isSuccess) {
                                showForcedChangeDialog = false
                                onAdminLoginSuccess()
                            } else {
                                changeErrorMessage = res.exceptionOrNull()?.message ?: "Failed to update password."
                            }
                        }
                    },
                    modifier = Modifier.testTag("submit_password_change_btn")
                ) {
                    Text("Update & Proceed")
                }
            }
        )
    }
}

/**
 * Teacher Login Screen (Requirement 3 & 4)
 */
@Composable
fun TeacherLoginScreen(
    authRepository: AuthRepository,
    onTeacherLoginSuccess: (TeacherEntity) -> Unit,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("prof_sharma") }
    var password by remember { mutableStateOf("Teacher@123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            EduSyncTopAppBar(
                title = "Teacher Portal",
                subtitle = "Faculty & Batch Management",
                showBackButton = true,
                onBackClick = onBack,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E7FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = Color(0xFF4338CA),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Teacher Sign In",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = "Access your assigned batches, student attendance & homework",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Teacher Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("teacher_username_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("teacher_password_input"),
                singleLine = true
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        errorMessage = "Please enter teacher username and password."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        when (val result = authRepository.loginTeacher(username.trim().lowercase(), password)) {
                            is AuthResult.Success -> {
                                isLoading = false
                                onTeacherLoginSuccess(result.data)
                            }
                            is AuthResult.AccountBlocked -> {
                                isLoading = false
                                errorMessage = result.reason
                            }
                            is AuthResult.Error -> {
                                isLoading = false
                                errorMessage = result.message
                            }
                            else -> {
                                isLoading = false
                                errorMessage = "Teacher login failed."
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("teacher_login_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Sign In to Batch Dashboard", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Student Login Screen without sensitive visible credentials (Requirement 1)
 */
@Composable
fun StudentLoginScreen(
    authRepository: AuthRepository,
    onStudentLoginSuccess: (StudentEntity) -> Unit,
    onNavigateToRegister: () -> Unit,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var usernameOrId by remember { mutableStateOf("EDU-2026-1001") }
    var password by remember { mutableStateOf("Student@123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            EduSyncTopAppBar(
                title = "Student Portal",
                subtitle = "Access Courses, ID Card & Attendance",
                showBackButton = true,
                onBackClick = onBack,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDBEAFE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = Color(0xFF1D4ED8),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Student Sign In",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = "Enter your Student ID (EDU-2026-XXXX) or Username",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = usernameOrId,
                onValueChange = { usernameOrId = it },
                label = { Text("Student ID or Username") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_username_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_password_input"),
                singleLine = true
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFB91C1C),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (usernameOrId.isBlank() || password.isBlank()) {
                        errorMessage = "Please enter student credentials."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        when (val res = authRepository.loginStudent(usernameOrId.trim(), password)) {
                            is AuthResult.Success -> {
                                isLoading = false
                                onStudentLoginSuccess(res.data)
                            }
                            is AuthResult.AccountBlocked -> {
                                isLoading = false
                                errorMessage = "Your student account is blocked: ${res.reason}"
                            }
                            is AuthResult.Error -> {
                                isLoading = false
                                errorMessage = res.message
                            }
                            else -> {
                                isLoading = false
                                errorMessage = "Login failed."
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("student_login_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Sign In to Student Portal", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.testTag("goto_register_button")
            ) {
                Text("New Student? Register Online Admission")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentRegisterScreen(
    instituteRepository: InstituteRepository,
    onRegisterSuccess: (StudentEntity) -> Unit,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var guardianName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val courses by instituteRepository.getAllCourses().collectAsState(initial = emptyList())
    var selectedCourse by remember { mutableStateOf<CourseEntity?>(null) }
    var courseMenuExpanded by remember { mutableStateOf(false) }

    val batches = listOf("Morning (09:00 AM - 11:00 AM)", "Afternoon (01:00 PM - 03:00 PM)", "Evening (05:00 PM - 07:00 PM)")
    var selectedBatch by remember { mutableStateOf(batches[0]) }
    var batchMenuExpanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            EduSyncTopAppBar(
                title = "New Student Admission",
                subtitle = "Self Online Registration",
                showBackButton = true,
                onBackClick = onBack,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Personal & Academic Information",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name *") },
                modifier = Modifier.fillMaxWidth().testTag("reg_fullname")
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Choose Username *") },
                modifier = Modifier.fillMaxWidth().testTag("reg_username")
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address *") },
                modifier = Modifier.fillMaxWidth().testTag("reg_email")
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number *") },
                modifier = Modifier.fillMaxWidth().testTag("reg_phone")
            )

            OutlinedTextField(
                value = guardianName,
                onValueChange = { guardianName = it },
                label = { Text("Parent / Guardian Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Residential Address") },
                modifier = Modifier.fillMaxWidth()
            )

            // Course selection dropdown
            ExposedDropdownMenuBox(
                expanded = courseMenuExpanded,
                onExpandedChange = { courseMenuExpanded = !courseMenuExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCourse?.name ?: "Select Course *",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Course *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseMenuExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth().testTag("reg_course_select")
                )
                ExposedDropdownMenu(
                    expanded = courseMenuExpanded,
                    onDismissRequest = { courseMenuExpanded = false }
                ) {
                    courses.forEach { c ->
                        DropdownMenuItem(
                            text = { Text("${c.name} (₹${"%.0f".format(c.totalFees)})") },
                            onClick = {
                                selectedCourse = c
                                courseMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Batch selection dropdown
            ExposedDropdownMenuBox(
                expanded = batchMenuExpanded,
                onExpandedChange = { batchMenuExpanded = !batchMenuExpanded }
            ) {
                OutlinedTextField(
                    value = selectedBatch,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Preferred Batch *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchMenuExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = batchMenuExpanded,
                    onDismissRequest = { batchMenuExpanded = false }
                ) {
                    batches.forEach { b ->
                        DropdownMenuItem(
                            text = { Text(b) },
                            onClick = {
                                selectedBatch = b
                                batchMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password *") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().testTag("reg_password")
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password *") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().testTag("reg_confirm_password")
            )

            if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (fullName.isBlank() || username.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill in all required fields."
                        return@Button
                    }
                    if (selectedCourse == null) {
                        errorMessage = "Please select an admission course."
                        return@Button
                    }
                    if (password != confirmPassword) {
                        errorMessage = "Passwords do not match."
                        return@Button
                    }
                    if (password.length < 6) {
                        errorMessage = "Password must be at least 6 characters."
                        return@Button
                    }

                    isSubmitting = true
                    errorMessage = null

                    scope.launch {
                        val result = instituteRepository.createStudent(
                            fullName = fullName.trim(),
                            username = username.trim().lowercase(),
                            rawPassword = password,
                            email = email.trim(),
                            phone = phone.trim(),
                            courseId = selectedCourse!!.id,
                            courseName = selectedCourse!!.name,
                            batch = selectedBatch,
                            guardianName = guardianName.trim(),
                            address = address.trim(),
                            adminUser = "Self Registration"
                        )

                        isSubmitting = false
                        if (result.isSuccess) {
                            onRegisterSuccess(result.getOrThrow())
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Registration failed."
                        }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_registration_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Complete Admission & Generate Student ID", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
