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

  @Test
  fun `verify authorized course switching and session invalidation`() = runBlocking {
    val existing = instituteRepository.getStudentById("EDU-2026-8801")
    if (existing == null) {
      instituteRepository.createStudent(
        fullName = "Rohan Sharma",
        username = "rohan_course_test",
        rawPassword = "Password@123",
        email = "rohan@example.com",
        phone = "9876543288",
        courseId = "CRS_ADCA",
        courseName = "ADCA",
        batch = "Morning",
        guardianName = "Rajesh Sharma",
        address = "Civil Lines",
        customStudentId = "EDU-2026-8801",
        rollNo = "8801"
      )
    }
    val authorizedCourses = instituteRepository.getAuthorizedCoursesForStudent("EDU-2026-8801")
    assertTrue("Student EDU-2026-8801 should have multiple authorized courses", authorizedCourses.size >= 2)

    val switchSuccess = instituteRepository.switchStudentCourse("EDU-2026-8801", "CRS_TALLY")
    assertTrue("Switching to authorized course CRS_TALLY should succeed", switchSuccess.isSuccess)
    assertEquals("CRS_TALLY", switchSuccess.getOrNull()?.courseId)

    // Switch back to CRS_ADCA
    val switchBack = instituteRepository.switchStudentCourse("EDU-2026-8801", "CRS_ADCA")
    assertTrue("Switching back to CRS_ADCA should succeed", switchBack.isSuccess)
    assertEquals("CRS_ADCA", switchBack.getOrNull()?.courseId)

    // Attempt switching to an unauthorized course
    val unauthorizedSwitch = instituteRepository.switchStudentCourse("EDU-2026-8801", "CRS_UNAUTHORIZED_999")
    assertTrue("Switching to unauthorized course should fail", unauthorizedSwitch.isFailure)

    // Verify Log Out All Devices session invalidation
    val sessionInvalidated = instituteRepository.invalidateAllStudentSessions("EDU-2026-8801")
    assertTrue("Log Out All Devices should invalidate sessions cleanly", sessionInvalidated.isSuccess)
  }

  @Test
  fun `verify scheduled exam lifecycle upcoming active ended and evaluation`() {
    runBlocking {
      var student = instituteRepository.getStudentById("EDU-2026-8802")
      if (student == null) {
        student = instituteRepository.createStudent(
          fullName = "Priya Verma",
          username = "priya_exam_test",
          rawPassword = "Password@123",
          email = "priya@example.com",
          phone = "9876543289",
          courseId = "CRS_ADCA",
          courseName = "ADCA",
          batch = "Morning",
          guardianName = "Anil Verma",
          address = "Model Town",
          customStudentId = "EDU-2026-8802",
          rollNo = "8802"
        ).getOrThrow()
      }
      assertNotNull("Student EDU-2026-8802 should exist", student)
      com.example.ui.components.ServerTimeAuthority.setServerOffsetForTesting(0L)
      val baseNow = com.example.ui.components.ServerTimeAuthority.currentTimeMillis()
      val startMs = baseNow + 30 * 60_000L // Starts in 30 mins
      val durationMins = 60

      val exam = com.example.data.local.entity.ScheduledExamEntity(
        examId = "EXM-TEST-LIFECYCLE",
        title = "ADCA Lifecycle Verification Exam",
        courseId = student!!.courseId,
        courseName = student.courseName,
        scheduledStartTime = startMs,
        durationMinutes = durationMins,
        totalQuestions = 50,
        passingMarks = 20,
        questionsJson = com.example.core.ExamQuestionBank.serializeQuestions(
          com.example.core.ExamQuestionBank.getDefault50Questions()
        ),
        assignedStudentIds = com.example.core.ExamQuestionBank.serializeAllowedStudentIds(listOf(student.studentId)),
        status = "SCHEDULED"
      )
      instituteRepository.saveScheduledExam(exam)

      // 1. Before start time: Upcoming -> should block attempt
      val earlyAttempt = instituteRepository.startExamAttempt("EXM-TEST-LIFECYCLE", student)
      assertTrue("Attempt before scheduledStartTime should be blocked", earlyAttempt.isFailure)

      // 2. After end time without starting: Expired -> should block attempt
      com.example.ui.components.ServerTimeAuthority.setServerOffsetForTesting(120 * 60_000L) // +120 mins (past end time)
      val expiredAttempt = instituteRepository.startExamAttempt("EXM-TEST-LIFECYCLE", student)
      assertTrue("Attempt after scheduledEndTime should be blocked", expiredAttempt.isFailure)

      // 3. During active window: Active -> should allow attempt & submission
      instituteRepository.saveScheduledExam(exam.copy(status = "SCHEDULED"))
      com.example.ui.components.ServerTimeAuthority.setServerOffsetForTesting(40 * 60_000L)
      val activeAttempt = instituteRepository.startExamAttempt("EXM-TEST-LIFECYCLE", student)
      assertTrue("Attempt during active window should succeed", activeAttempt.isSuccess)

      val questions = com.example.core.ExamQuestionBank.getDefault50Questions()
      for (i in 0 until 43) {
        instituteRepository.autosaveExamAnswer(
          examId = "EXM-TEST-LIFECYCLE",
          studentId = student.studentId,
          questionIndex = i,
          optionIndex = questions[i].correctOptionIndex
        )
      }
      val submitted = instituteRepository.submitExam(
        examId = "EXM-TEST-LIFECYCLE",
        studentId = student.studentId,
        autoSubmitted = false
      )
      assertTrue("Submission should succeed", submitted.isSuccess)
      val subEntity = submitted.getOrThrow()
      assertEquals("EVALUATED", subEntity.status)
      assertEquals(43, subEntity.score)
      assertTrue("86% score should pass", subEntity.isPassed)

      // Reset clock offset
      com.example.ui.components.ServerTimeAuthority.setServerOffsetForTesting(0L)
    }
  }
}

