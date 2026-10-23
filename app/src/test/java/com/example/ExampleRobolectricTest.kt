package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.SecurityUtils
import com.example.data.local.AppDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import com.example.data.repository.InstituteRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var context: Context
  private lateinit var database: AppDatabase
  private lateinit var authRepository: AuthRepository
  private lateinit var instituteRepository: InstituteRepository

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext<Context>()
    database = AppDatabase.getInstance(context)
    authRepository = AuthRepository(database)
    instituteRepository = InstituteRepository(database)
  }

  @Test
  fun `read string from context`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("EduSync", appName)
  }

  @Test
  fun `verify password hashing and verification`() {
    val password = "AdminPassword@2026"
    val hash = SecurityUtils.hashPassword(password)
    assertTrue(SecurityUtils.verifyPassword(password, hash))
    assertFalse(SecurityUtils.verifyPassword("WrongPassword", hash))
  }

  @Test
  fun `verify formula injection protection`() {
    val maliciousInput = "=cmd|' /C calc'!A0"
    val sanitized = SecurityUtils.sanitizeForSpreadsheet(maliciousInput)
    assertTrue(sanitized.startsWith("\"'") || sanitized.startsWith("'"))
  }

  @Test
  fun `verify strong password generation`() {
    val generated = SecurityUtils.generateStrongPassword(14)
    assertEquals(14, generated.length)
    val (score, strength) = SecurityUtils.evaluatePasswordStrength(generated)
    assertTrue(score >= 70)
  }

  @Test
  fun `verify student creation with detailed fields and deletion`() = runBlocking {
    val res = instituteRepository.createStudent(
      fullName = "Vikram Aditya",
      username = "vikram_test_user",
      rawPassword = "Password@123",
      email = "vikram@example.com",
      phone = "9876543299",
      courseId = "CRS_ADCA",
      courseName = "ADCA",
      batch = "Morning",
      guardianName = "Mahesh Aditya",
      address = "Civil Lines",
      customStudentId = "EDU-2026-9999",
      rollNo = "999",
      fatherName = "Mahesh Aditya",
      motherName = "Shanti Aditya",
      gender = "Male"
    )

    assertTrue("Student creation should succeed", res.isSuccess)
    val student = res.getOrThrow()
    assertEquals("EDU-2026-9999", student.studentId)
    assertEquals("999", student.rollNo)
    assertEquals("Mahesh Aditya", student.fatherName)
    assertEquals("Shanti Aditya", student.motherName)

    // Verify student can be fetched
    val fetched = instituteRepository.getStudentById("EDU-2026-9999")
    assertNotNull(fetched)
    assertEquals("Vikram Aditya", fetched?.fullName)

    // Delete student
    instituteRepository.deleteStudent("EDU-2026-9999", "Admin Test")
    val afterDelete = instituteRepository.getStudentById("EDU-2026-9999")
    assertNull("Student should be deleted from active database", afterDelete)
  }

  @Test
  fun `verify admin password change flow`() = runBlocking {
    // Attempt change admin password
    val newPassword = "NewAdminPassword@2026"
    val changeRes = authRepository.changeAdminPassword("admin", newPassword)
    assertTrue("Admin password change should succeed", changeRes.isSuccess)

    // Verify login with new password
    val loginRes = authRepository.loginAdmin("admin", newPassword)
    assertTrue("Login with new admin password should succeed", loginRes is AuthResult.Success)
  }
}
