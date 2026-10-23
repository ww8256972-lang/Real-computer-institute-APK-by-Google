package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ParentGuardianEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.TeacherEntity
import com.example.data.remote.GeminiService
import com.example.data.repository.AuthRepository
import com.example.data.repository.InstituteRepository
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.admin.AdminMainScreen
import com.example.ui.screens.admin.AdminSettingsBackupScreen
import com.example.ui.screens.auth.AdminLoginScreen
import com.example.ui.screens.auth.ParentLoginScreen
import com.example.ui.screens.auth.StudentLoginScreen
import com.example.ui.screens.auth.StudentRegisterScreen
import com.example.ui.screens.auth.TeacherLoginScreen
import com.example.ui.screens.parent.ParentGuardianScreen
import com.example.ui.screens.student.StudentMainScreen
import com.example.ui.screens.teacher.TeacherMainScreen
import com.example.ui.screens.verification.PublicVerificationScreen
import com.example.ui.theme.EduSyncTheme

enum class AppScreen {
    SPLASH,
    ROLE_SELECTION,
    STUDENT_LOGIN,
    STUDENT_REGISTER,
    STUDENT_MAIN,
    ADMIN_LOGIN,
    ADMIN_MAIN,
    ADMIN_SETTINGS_BACKUP,
    TEACHER_LOGIN,
    TEACHER_MAIN,
    PARENT_LOGIN,
    PARENT_MAIN,
    PUBLIC_VERIFICATION
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val instituteRepository = InstituteRepository(database)
        val authRepository = AuthRepository(database)
        val geminiService = GeminiService()

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }
            var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
            var activeStudent by remember { mutableStateOf<StudentEntity?>(null) }
            var activeTeacher by remember { mutableStateOf<TeacherEntity?>(null) }
            var activeGuardian by remember { mutableStateOf<ParentGuardianEntity?>(null) }

            EduSyncTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        AppScreen.SPLASH -> {
                            SplashScreen(
                                onSplashFinished = {
                                    currentScreen = AppScreen.ROLE_SELECTION
                                }
                            )
                        }

                        AppScreen.ROLE_SELECTION -> {
                            RoleSelectionScreen(
                                onSelectStudent = { currentScreen = AppScreen.STUDENT_LOGIN },
                                onSelectTeacher = { currentScreen = AppScreen.TEACHER_LOGIN },
                                onSelectParent = { currentScreen = AppScreen.PARENT_LOGIN },
                                onSelectAdmin = { currentScreen = AppScreen.ADMIN_LOGIN },
                                onOpenVerification = { currentScreen = AppScreen.PUBLIC_VERIFICATION },
                                onOpenAdminSettings = { currentScreen = AppScreen.ADMIN_SETTINGS_BACKUP },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { isDarkTheme = !isDarkTheme }
                            )
                        }

                        AppScreen.STUDENT_LOGIN -> {
                            BackHandler { currentScreen = AppScreen.ROLE_SELECTION }
                            StudentLoginScreen(
                                authRepository = authRepository,
                                onStudentLoginSuccess = { student ->
                                    activeStudent = student
                                    currentScreen = AppScreen.STUDENT_MAIN
                                },
                                onNavigateToRegister = { currentScreen = AppScreen.STUDENT_REGISTER },
                                onBack = { currentScreen = AppScreen.ROLE_SELECTION },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { isDarkTheme = !isDarkTheme }
                            )
                        }

                        AppScreen.STUDENT_REGISTER -> {
                            BackHandler { currentScreen = AppScreen.STUDENT_LOGIN }
                            StudentRegisterScreen(
                                instituteRepository = instituteRepository,
                                onRegisterSuccess = { student ->
                                    activeStudent = student
                                    currentScreen = AppScreen.STUDENT_MAIN
                                },
                                onBack = { currentScreen = AppScreen.STUDENT_LOGIN },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { isDarkTheme = !isDarkTheme }
                            )
                        }

                        AppScreen.STUDENT_MAIN -> {
                            BackHandler {
                                currentScreen = AppScreen.ROLE_SELECTION
                                activeStudent = null
                            }
                            if (activeStudent != null) {
                                StudentMainScreen(
                                    student = activeStudent!!,
                                    instituteRepository = instituteRepository,
                                    geminiService = geminiService,
                                    onLogout = {
                                        activeStudent = null
                                        currentScreen = AppScreen.ROLE_SELECTION
                                    },
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                                )
                            } else {
                                currentScreen = AppScreen.ROLE_SELECTION
                            }
                        }

                        AppScreen.ADMIN_LOGIN -> {
                            BackHandler { currentScreen = AppScreen.ROLE_SELECTION }
                            AdminLoginScreen(
                                authRepository = authRepository,
                                onAdminLoginSuccess = { currentScreen = AppScreen.ADMIN_MAIN },
                                onNavigateToTeacherLogin = { currentScreen = AppScreen.TEACHER_LOGIN },
                                onBack = { currentScreen = AppScreen.ROLE_SELECTION },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { isDarkTheme = !isDarkTheme }
                            )
                        }

                        AppScreen.TEACHER_LOGIN -> {
                            BackHandler { currentScreen = AppScreen.ADMIN_LOGIN }
                            TeacherLoginScreen(
                                authRepository = authRepository,
                                onTeacherLoginSuccess = { teacher ->
                                    activeTeacher = teacher
                                    currentScreen = AppScreen.TEACHER_MAIN
                                },
                                onBack = { currentScreen = AppScreen.ADMIN_LOGIN },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { isDarkTheme = !isDarkTheme }
                            )
                        }

                        AppScreen.TEACHER_MAIN -> {
                            BackHandler {
                                activeTeacher = null
                                currentScreen = AppScreen.ROLE_SELECTION
                            }
                            if (activeTeacher != null) {
                                TeacherMainScreen(
                                    teacher = activeTeacher!!,
                                    instituteRepository = instituteRepository,
                                    onLogout = {
                                        activeTeacher = null
                                        currentScreen = AppScreen.ROLE_SELECTION
                                    },
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                                )
                            } else {
                                currentScreen = AppScreen.ROLE_SELECTION
                            }
                        }

                        AppScreen.ADMIN_MAIN -> {
                            BackHandler { currentScreen = AppScreen.ROLE_SELECTION }
                            AdminMainScreen(
                                instituteRepository = instituteRepository,
                                onOpenSettings = { currentScreen = AppScreen.ADMIN_SETTINGS_BACKUP },
                                onLogout = { currentScreen = AppScreen.ROLE_SELECTION },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { isDarkTheme = !isDarkTheme }
                            )
                        }

                        AppScreen.ADMIN_SETTINGS_BACKUP -> {
                            BackHandler { currentScreen = AppScreen.ROLE_SELECTION }
                            AdminSettingsBackupScreen(
                                instituteRepository = instituteRepository,
                                authRepository = authRepository,
                                onBack = { currentScreen = AppScreen.ROLE_SELECTION },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { isDarkTheme = !isDarkTheme }
                            )
                        }

                        AppScreen.PARENT_LOGIN -> {
                            BackHandler { currentScreen = AppScreen.ROLE_SELECTION }
                            ParentLoginScreen(
                                authRepository = authRepository,
                                onLoginSuccess = { guardian ->
                                    activeGuardian = guardian
                                    currentScreen = AppScreen.PARENT_MAIN
                                },
                                onBack = { currentScreen = AppScreen.ROLE_SELECTION },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { isDarkTheme = !isDarkTheme }
                            )
                        }

                        AppScreen.PARENT_MAIN -> {
                            BackHandler {
                                activeGuardian = null
                                currentScreen = AppScreen.ROLE_SELECTION
                            }
                            if (activeGuardian != null) {
                                ParentGuardianScreen(
                                    guardian = activeGuardian!!,
                                    instituteRepository = instituteRepository,
                                    onLogout = {
                                        activeGuardian = null
                                        currentScreen = AppScreen.ROLE_SELECTION
                                    },
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                                )
                            } else {
                                currentScreen = AppScreen.ROLE_SELECTION
                            }
                        }

                        AppScreen.PUBLIC_VERIFICATION -> {
                            BackHandler { currentScreen = AppScreen.ROLE_SELECTION }
                            PublicVerificationScreen(
                                instituteRepository = instituteRepository,
                                onBack = { currentScreen = AppScreen.ROLE_SELECTION },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { isDarkTheme = !isDarkTheme }
                            )
                        }
                    }
                }
            }
        }
    }
}
