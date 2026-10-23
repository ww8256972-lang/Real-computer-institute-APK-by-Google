package com.example.data.repository

import com.example.core.SecurityUtils
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AdminAccountEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.ParentGuardianEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.TeacherEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class RequirePasswordChange(val username: String) : AuthResult<Nothing>()
    data class AccountLocked(val remainingSeconds: Long) : AuthResult<Nothing>()
    data class AccountBlocked(val reason: String) : AuthResult<Nothing>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

class AuthRepository(private val db: AppDatabase) {

    private val adminDao = db.adminDao()
    private val studentDao = db.studentDao()
    private val teacherDao = db.teacherDao()
    private val parentGuardianDao = db.parentGuardianDao()

    suspend fun loginAdmin(username: String, rawPassword: String): AuthResult<AdminAccountEntity> =
        withContext(Dispatchers.IO) {
            val admin = adminDao.getAdmin(username) 
                ?: adminDao.getAdmin(username.lowercase())
                ?: (if (username.equals("admin", ignoreCase = true)) adminDao.getAdmin("Admin123") else null)
                ?: (if (username.equals("Admin123", ignoreCase = true)) adminDao.getAdmin("admin") else null)
                ?: return@withContext AuthResult.Error("Invalid administrator credentials.")

            val now = System.currentTimeMillis()
            if (admin.lockedUntil != null && admin.lockedUntil > now) {
                val remainingSec = (admin.lockedUntil - now) / 1000
                return@withContext AuthResult.AccountLocked(remainingSec)
            }

            val isValid = SecurityUtils.verifyPassword(rawPassword, admin.passwordHash)
            if (!isValid) {
                val failed = admin.failedLoginAttempts + 1
                val lockTime = if (failed >= 5) now + (15 * 60 * 1000) else null
                adminDao.updateLoginAttempt(admin.username, failed, lockTime)

                adminDao.insertAuditLog(
                    AuditLogEntity(
                        action = "ADMIN_LOGIN_FAILED",
                        performedBy = admin.username,
                        details = "Failed attempt #$failed" + if (lockTime != null) " (Account locked for 15 mins)" else ""
                    )
                )

                return@withContext if (lockTime != null) {
                    AuthResult.AccountLocked(15 * 60)
                } else {
                    AuthResult.Error("Invalid password. Attempt $failed of 5.")
                }
            }

            adminDao.recordSuccessfulLogin(admin.username)
            adminDao.insertAuditLog(
                AuditLogEntity(
                    action = "ADMIN_LOGIN_SUCCESS",
                    performedBy = admin.username,
                    details = "Administrator logged into Admin Panel."
                )
            )

            if (admin.isInitialPassword) {
                return@withContext AuthResult.RequirePasswordChange(admin.username)
            }

            AuthResult.Success(admin)
        }

    suspend fun changeAdminPassword(username: String = "admin", newPassword: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            if (newPassword.length < 8) {
                return@withContext Result.failure(Exception("Password must be at least 8 characters long."))
            }

            val newHash = SecurityUtils.hashPassword(newPassword)
            adminDao.updateAdminPassword(username, newHash)
            adminDao.updateAdminPassword("admin", newHash)
            adminDao.updateAdminPassword("Admin123", newHash)
            adminDao.insertAuditLog(
                AuditLogEntity(
                    action = "ADMIN_PASSWORD_CHANGED",
                    performedBy = username,
                    details = "Admin password updated successfully."
                )
            )
            Result.success(Unit)
        }

    suspend fun loginTeacher(username: String, rawPassword: String): AuthResult<TeacherEntity> =
        withContext(Dispatchers.IO) {
            val normalized = username.trim().lowercase()
            val teacher = teacherDao.getTeacherByUsername(normalized)
                ?: teacherDao.getTeacherByUsername(normalized.replace(".", "_"))
                ?: teacherDao.getTeacherByUsername(normalized.replace("_", "."))
                ?: return@withContext AuthResult.Error("Teacher account not found.")

            if (teacher.status != "ACTIVE") {
                return@withContext AuthResult.AccountBlocked("Teacher account has been deactivated by the Administrator.")
            }

            val isValid = SecurityUtils.verifyPassword(rawPassword, teacher.passwordHash)
            if (!isValid) {
                return@withContext AuthResult.Error("Incorrect teacher password.")
            }

            adminDao.insertAuditLog(
                AuditLogEntity(
                    action = "TEACHER_LOGIN_SUCCESS",
                    performedBy = teacher.teacherId,
                    details = "Teacher '${teacher.fullName}' logged into Batch Dashboard."
                )
            )

            AuthResult.Success(teacher)
        }

    suspend fun loginStudent(usernameOrId: String, rawPassword: String): AuthResult<StudentEntity> =
        withContext(Dispatchers.IO) {
            val student = studentDao.getByUsername(usernameOrId)
                ?: studentDao.getByStudentId(usernameOrId)
                ?: return@withContext AuthResult.Error("Student ID or Username not found.")

            if (student.status == "BLOCKED") {
                val reason = student.blockReason ?: "Account suspended by institute administrator. Please contact office."
                return@withContext AuthResult.AccountBlocked(reason)
            }

            val isValid = SecurityUtils.verifyPassword(rawPassword, student.passwordHash)
            if (!isValid) {
                return@withContext AuthResult.Error("Incorrect password. Please verify credentials or reset password.")
            }

            adminDao.insertAuditLog(
                AuditLogEntity(
                    action = "STUDENT_LOGIN_SUCCESS",
                    performedBy = student.studentId,
                    details = "Student logged into mobile portal."
                )
            )

            AuthResult.Success(student)
        }

    suspend fun loginGuardian(username: String, rawPassword: String): AuthResult<ParentGuardianEntity> =
        withContext(Dispatchers.IO) {
            val normalized = username.trim().lowercase()
            val guardian = parentGuardianDao.getGuardianByUsername(normalized)
                ?: return@withContext AuthResult.Error("Parent / Guardian account not found.")

            if (guardian.status != "ACTIVE") {
                return@withContext AuthResult.AccountBlocked("Guardian access has been revoked by institute administration.")
            }

            val isValid = SecurityUtils.verifyPassword(rawPassword, guardian.passwordHash)
            if (!isValid) {
                return@withContext AuthResult.Error("Incorrect password. Please verify credentials.")
            }

            adminDao.insertAuditLog(
                AuditLogEntity(
                    action = "GUARDIAN_LOGIN_SUCCESS",
                    performedBy = guardian.guardianId,
                    details = "Guardian '${guardian.fullName}' logged in to monitor student '${guardian.linkedStudentId}'."
                )
            )

            AuthResult.Success(guardian)
        }
}
