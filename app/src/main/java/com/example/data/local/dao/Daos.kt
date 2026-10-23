package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY id DESC LIMIT :limit OFFSET :offset")
    fun getStudentsPaged(limit: Int, offset: Int): Flow<List<StudentEntity>>

    @Query("""
        SELECT * FROM students 
        WHERE (studentId LIKE '%' || :query || '%' OR fullName LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%')
        ORDER BY id DESC LIMIT :limit OFFSET :offset
    """)
    fun searchStudents(query: String, limit: Int, offset: Int): Flow<List<StudentEntity>>

    @Query("""
        SELECT * FROM students 
        WHERE (:courseId IS NULL OR courseId = :courseId)
        AND (:status IS NULL OR status = :status)
        ORDER BY id DESC LIMIT :limit OFFSET :offset
    """)
    fun filterStudents(courseId: String?, status: String?, limit: Int, offset: Int): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE batch = :batch ORDER BY fullName ASC")
    fun getStudentsByBatch(batch: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE status = 'ACTIVE' ORDER BY fullName ASC")
    fun getAllActiveStudents(): Flow<List<StudentEntity>>

    @Query("SELECT COUNT(*) FROM students")
    fun getTotalStudentCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getTotalStudentCountDirect(): Int

    @Query("SELECT COUNT(*) FROM students WHERE status = 'ACTIVE'")
    fun getActiveStudentCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM students WHERE status = 'BLOCKED'")
    fun getBlockedStudentCount(): Flow<Int>

    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    suspend fun getByStudentId(studentId: String): StudentEntity?

    @Query("SELECT * FROM students WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(student: StudentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(students: List<StudentEntity>)

    @Update
    suspend fun update(student: StudentEntity)

    @Query("UPDATE students SET status = :status, blockReason = :reason WHERE studentId = :studentId")
    suspend fun updateStatus(studentId: String, status: String, reason: String?)

    @Query("UPDATE students SET passwordHash = :newPasswordHash, isTemporaryPassword = :isTemporary WHERE studentId = :studentId")
    suspend fun updatePassword(studentId: String, newPasswordHash: String, isTemporary: Boolean)

    @Query("DELETE FROM students WHERE studentId = :studentId")
    suspend fun delete(studentId: String)

    @Query("UPDATE students SET batch = :batch WHERE studentId = :studentId")
    suspend fun updateBatch(studentId: String, batch: String)

    @Query("SELECT * FROM students ORDER BY id ASC")
    suspend fun getAllForExport(): List<StudentEntity>
}

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers ORDER BY id DESC")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    @Query("SELECT * FROM teachers WHERE status = 'ACTIVE' ORDER BY fullName ASC")
    fun getActiveTeachers(): Flow<List<TeacherEntity>>

    @Query("SELECT * FROM teachers WHERE teacherId = :teacherId LIMIT 1")
    suspend fun getTeacherById(teacherId: String): TeacherEntity?

    @Query("SELECT * FROM teachers WHERE username = :username LIMIT 1")
    suspend fun getTeacherByUsername(username: String): TeacherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<TeacherEntity>)

    @Update
    suspend fun updateTeacher(teacher: TeacherEntity)

    @Query("UPDATE teachers SET status = :status WHERE teacherId = :teacherId")
    suspend fun updateTeacherStatus(teacherId: String, status: String)

    @Query("UPDATE teachers SET assignedBatches = :batches, isBatchAdmin = :isBatchAdmin WHERE teacherId = :teacherId")
    suspend fun assignTeacherBatch(teacherId: String, batches: String, isBatchAdmin: Boolean)
}

@Dao
interface BatchDao {
    @Query("SELECT * FROM batches ORDER BY name ASC")
    fun getAllBatches(): Flow<List<BatchEntity>>

    @Query("SELECT * FROM batches WHERE id = :id LIMIT 1")
    suspend fun getBatchById(id: String): BatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: BatchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatches(batches: List<BatchEntity>)

    @Update
    suspend fun updateBatch(batch: BatchEntity)

    @Query("DELETE FROM batches WHERE id = :id")
    suspend fun deleteBatch(id: String)

    @Query("""
        UPDATE batches 
        SET assignedTeacherId = :teacherId, assignedTeacherName = :teacherName,
            batchAdminTeacherId = :batchAdminId, batchAdminTeacherName = :batchAdminName
        WHERE id = :batchId
    """)
    suspend fun assignTeacherToBatch(
        batchId: String,
        teacherId: String?,
        teacherName: String?,
        batchAdminId: String?,
        batchAdminName: String?
    )
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY name ASC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    suspend fun getCourseById(id: String): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourse(id: String)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceByDate(date: String): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markAttendance(attendance: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markBatchAttendance(records: List<AttendanceEntity>)

    @Query("SELECT COUNT(*) FROM attendance WHERE studentId = :studentId AND status = 'PRESENT'")
    suspend fun getPresentCount(studentId: String): Int

    @Query("SELECT COUNT(*) FROM attendance WHERE studentId = :studentId")
    suspend fun getTotalDaysCount(studentId: String): Int
}

@Dao
interface FeeRecordDao {
    @Query("SELECT * FROM fee_records WHERE studentId = :studentId ORDER BY paymentDate DESC")
    fun getFeesForStudent(studentId: String): Flow<List<FeeRecordEntity>>

    @Query("SELECT * FROM fee_records ORDER BY createdAt DESC")
    fun getAllFeeRecords(): Flow<List<FeeRecordEntity>>

    @Query("SELECT * FROM fee_records WHERE receiptNumber = :receiptNumber LIMIT 1")
    suspend fun getReceiptByNumber(receiptNumber: String): FeeRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeRecord(record: FeeRecordEntity): Long

    @Query("SELECT SUM(amount) FROM fee_records")
    suspend fun getTotalCollectedFees(): Double?

    @Query("SELECT SUM(amount) FROM fee_records WHERE studentId = :studentId")
    suspend fun getFeesCollectedForStudent(studentId: String): Double?

    @Query("SELECT * FROM fee_records ORDER BY id ASC")
    suspend fun getAllFeeRecordsForExport(): List<FeeRecordEntity>
}

@Dao
interface FeeReminderDao {
    @Query("SELECT * FROM fee_reminders ORDER BY createdAt DESC")
    fun getAllReminders(): Flow<List<FeeReminderEntity>>

    @Query("SELECT * FROM fee_reminders WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getRemindersForStudent(studentId: String): Flow<List<FeeReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: FeeReminderEntity): Long
}

@Dao
interface ExamResultDao {
    @Query("SELECT * FROM exam_results WHERE studentId = :studentId ORDER BY examDate DESC")
    fun getResultsForStudent(studentId: String): Flow<List<ExamResultEntity>>

    @Query("SELECT * FROM exam_results ORDER BY id DESC")
    fun getAllResults(): Flow<List<ExamResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: ExamResultEntity): Long

    @Query("SELECT * FROM exam_results WHERE id = :id LIMIT 1")
    suspend fun getResultById(id: Long): ExamResultEntity?

    @Query("SELECT * FROM exam_results WHERE certificateNumber = :certNo LIMIT 1")
    suspend fun getByCertificateNumber(certNo: String): ExamResultEntity?
}

@Dao
interface AcademicDao {
    @Query("SELECT * FROM study_materials WHERE courseId = :courseId ORDER BY id DESC")
    fun getStudyMaterials(courseId: String): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials ORDER BY id DESC")
    fun getAllStudyMaterials(): Flow<List<StudyMaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyMaterial(material: StudyMaterialEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyMaterials(materials: List<StudyMaterialEntity>)

    @Query("DELETE FROM study_materials WHERE id = :id")
    suspend fun deleteStudyMaterial(id: Long)

    @Query("SELECT * FROM assignments WHERE courseId = :courseId ORDER BY id DESC")
    fun getAssignments(courseId: String): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE batchName IS NULL OR batchName = :batchName ORDER BY id DESC")
    fun getAssignmentsForBatch(batchName: String?): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments ORDER BY id DESC")
    fun getAllAssignments(): Flow<List<AssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<AssignmentEntity>)

    @Query("SELECT * FROM submissions WHERE assignmentId = :assignmentId")
    fun getSubmissionsForAssignment(assignmentId: Long): Flow<List<SubmissionEntity>>

    @Query("SELECT * FROM submissions WHERE studentId = :studentId")
    fun getSubmissionsForStudent(studentId: String): Flow<List<SubmissionEntity>>

    @Query("SELECT * FROM submissions ORDER BY submittedAt DESC")
    fun getAllSubmissions(): Flow<List<SubmissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: SubmissionEntity)

    @Query("UPDATE submissions SET marksObtained = :marks, feedback = :feedback, gradedByTeacherId = :teacherId, status = 'GRADED' WHERE id = :id")
    suspend fun gradeSubmission(id: Long, marks: Int, feedback: String, teacherId: String? = null)

    @Query("SELECT * FROM notices ORDER BY id DESC")
    fun getNotices(): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<NoticeEntity>)

    @Query("DELETE FROM notices WHERE id = :id")
    suspend fun deleteNotice(id: Long)
}

@Dao
interface AdminDao {
    @Query("SELECT * FROM admin_accounts WHERE username = :username LIMIT 1")
    suspend fun getAdmin(username: String): AdminAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmin(admin: AdminAccountEntity)

    @Query("UPDATE admin_accounts SET passwordHash = :newPasswordHash, isInitialPassword = 0 WHERE username = :username")
    suspend fun updateAdminPassword(username: String, newPasswordHash: String)

    @Query("UPDATE admin_accounts SET failedLoginAttempts = :failedAttempts, lockedUntil = :lockedUntil WHERE username = :username")
    suspend fun updateLoginAttempt(username: String, failedAttempts: Int, lockedUntil: Long?)

    @Query("UPDATE admin_accounts SET failedLoginAttempts = 0, lockedUntil = NULL, lastLoginAt = :loginTime WHERE username = :username")
    suspend fun recordSuccessfulLogin(username: String, loginTime: Long = System.currentTimeMillis())

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 500")
    fun getAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)
}

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetables ORDER BY id ASC")
    fun getAllTimetables(): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetables")
    suspend fun getAllTimetablesDirect(): List<TimetableEntity>

    @Query("SELECT * FROM timetables WHERE batchName = :batchName ORDER BY id ASC")
    fun getTimetablesByBatch(batchName: String): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetables WHERE teacherId = :teacherId ORDER BY id ASC")
    fun getTimetablesByTeacher(teacherId: String): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetables WHERE dayOfWeek = :day ORDER BY id ASC")
    fun getTimetablesByDay(day: String): Flow<List<TimetableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetable(entry: TimetableEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetables(entries: List<TimetableEntity>)

    @Query("DELETE FROM timetables WHERE id = :id")
    suspend fun deleteTimetable(id: Long)

    @Query("DELETE FROM timetables WHERE batchName = :batchName")
    suspend fun clearBatchTimetable(batchName: String)

    @Query("UPDATE timetables SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}

@Dao
interface QRAttendanceDao {
    @Query("SELECT * FROM qr_attendance_sessions WHERE status = 'ACTIVE' ORDER BY createdTime DESC")
    fun getActiveSessions(): Flow<List<QRAttendanceSessionEntity>>

    @Query("SELECT * FROM qr_attendance_sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): QRAttendanceSessionEntity?

    @Query("SELECT * FROM qr_attendance_sessions WHERE sessionCode = :code LIMIT 1")
    suspend fun getSessionByCode(code: String): QRAttendanceSessionEntity?

    @Query("SELECT * FROM qr_attendance_sessions WHERE batchName = :batchName ORDER BY createdTime DESC")
    fun getSessionsForBatch(batchName: String): Flow<List<QRAttendanceSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: QRAttendanceSessionEntity)

    @Query("UPDATE qr_attendance_sessions SET status = :status WHERE sessionId = :sessionId")
    suspend fun updateSessionStatus(sessionId: String, status: String)
}

@Dao
interface ParentGuardianDao {
    @Query("SELECT * FROM parent_guardians WHERE username = :username LIMIT 1")
    suspend fun getGuardianByUsername(username: String): ParentGuardianEntity?

    @Query("SELECT * FROM parent_guardians WHERE guardianId = :guardianId LIMIT 1")
    suspend fun getGuardianById(guardianId: String): ParentGuardianEntity?

    @Query("SELECT * FROM parent_guardians WHERE linkedStudentId = :studentId")
    fun getGuardiansForStudent(studentId: String): Flow<List<ParentGuardianEntity>>

    @Query("SELECT * FROM parent_guardians ORDER BY id DESC")
    fun getAllGuardians(): Flow<List<ParentGuardianEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuardian(guardian: ParentGuardianEntity): Long

    @Update
    suspend fun updateGuardian(guardian: ParentGuardianEntity)

    @Query("UPDATE parent_guardians SET status = :status WHERE guardianId = :guardianId")
    suspend fun updateStatus(guardianId: String, status: String)
}

@Dao
interface CertificateDao {
    @Query("SELECT * FROM digital_certificates ORDER BY issueDate DESC")
    fun getAllCertificates(): Flow<List<DigitalCertificateEntity>>

    @Query("SELECT * FROM digital_certificates WHERE studentId = :studentId ORDER BY issueDate DESC")
    fun getCertificatesForStudent(studentId: String): Flow<List<DigitalCertificateEntity>>

    @Query("SELECT * FROM digital_certificates WHERE certificateNumber = :certNo LIMIT 1")
    suspend fun getCertificateByNumber(certNo: String): DigitalCertificateEntity?

    @Query("SELECT * FROM digital_certificates WHERE verificationToken = :token LIMIT 1")
    suspend fun getCertificateByToken(token: String): DigitalCertificateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(certificate: DigitalCertificateEntity)

    @Query("""
        UPDATE digital_certificates 
        SET status = 'CANCELLED', cancellationReason = :reason, cancelledBy = :cancelledBy, cancelledAt = :cancelledAt 
        WHERE certificateNumber = :certNo
    """)
    suspend fun cancelCertificate(certNo: String, reason: String, cancelledBy: String, cancelledAt: Long = System.currentTimeMillis())
}

@Dao
interface LearningGoalDao {
    @Query("SELECT * FROM learning_goals WHERE studentId = :studentId ORDER BY id DESC")
    fun getGoalsForStudent(studentId: String): Flow<List<LearningGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: LearningGoalEntity): Long

    @Query("UPDATE learning_goals SET progressPercent = :progress, isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateProgress(id: Long, progress: Int, isCompleted: Boolean)

    @Query("DELETE FROM learning_goals WHERE id = :id")
    suspend fun deleteGoal(id: Long)
}

@Dao
interface AchievementBadgeDao {
    @Query("SELECT * FROM achievement_badges WHERE studentId = :studentId ORDER BY id DESC")
    fun getBadgesForStudent(studentId: String): Flow<List<AchievementBadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: AchievementBadgeEntity): Long
}

@Dao
interface DigitalBookDao {
    @Query("SELECT * FROM digital_books ORDER BY id ASC")
    fun getAllBooks(): Flow<List<DigitalBookEntity>>

    @Query("SELECT * FROM digital_books WHERE courseId = :courseId ORDER BY id ASC")
    fun getBooksByCourse(courseId: String): Flow<List<DigitalBookEntity>>

    @Query("SELECT * FROM digital_books WHERE title LIKE '%' || :query || '%' OR subject LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%'")
    fun searchBooks(query: String): Flow<List<DigitalBookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: DigitalBookEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<DigitalBookEntity>)

    @Query("UPDATE digital_books SET downloadCount = downloadCount + 1, isDownloaded = 1 WHERE id = :id")
    suspend fun recordDownload(id: Long)

    @Query("UPDATE digital_books SET isDownloaded = :isDownloaded WHERE id = :id")
    suspend fun toggleDownloaded(id: Long, isDownloaded: Boolean)

    @Query("DELETE FROM digital_books WHERE id = :id")
    suspend fun deleteBook(id: Long)
}

@Dao
interface ScheduledExamDao {
    @Query("SELECT * FROM scheduled_exams ORDER BY scheduledStartTime DESC")
    fun getAllScheduledExams(): Flow<List<ScheduledExamEntity>>

    @Query("SELECT * FROM scheduled_exams WHERE examId = :examId LIMIT 1")
    suspend fun getExamById(examId: String): ScheduledExamEntity?

    @Query("SELECT * FROM scheduled_exams WHERE courseId = :courseId ORDER BY scheduledStartTime DESC")
    fun getExamsByCourse(courseId: String): Flow<List<ScheduledExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ScheduledExamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<ScheduledExamEntity>)

    @Update
    suspend fun updateExam(exam: ScheduledExamEntity)

    @Query("DELETE FROM scheduled_exams WHERE examId = :examId")
    suspend fun deleteExam(examId: String)

    @Query("UPDATE scheduled_exams SET status = :status WHERE examId = :examId")
    suspend fun updateExamStatus(examId: String, status: String)

    // Submissions
    @Query("SELECT * FROM exam_submissions WHERE examId = :examId ORDER BY startedAt DESC")
    fun getSubmissionsForExam(examId: String): Flow<List<ExamSubmissionEntity>>

    @Query("SELECT * FROM exam_submissions WHERE studentId = :studentId ORDER BY startedAt DESC")
    fun getSubmissionsForStudent(studentId: String): Flow<List<ExamSubmissionEntity>>

    @Query("SELECT * FROM exam_submissions WHERE examId = :examId AND studentId = :studentId LIMIT 1")
    suspend fun getSubmission(examId: String, studentId: String): ExamSubmissionEntity?

    @Query("SELECT * FROM exam_submissions WHERE id = :id LIMIT 1")
    suspend fun getSubmissionById(id: Long): ExamSubmissionEntity?

    @Query("SELECT * FROM exam_submissions WHERE examId = :examId AND studentId = :studentId LIMIT 1")
    fun observeSubmission(examId: String, studentId: String): Flow<ExamSubmissionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: ExamSubmissionEntity): Long

    @Update
    suspend fun updateSubmission(submission: ExamSubmissionEntity)

    @Query("SELECT COUNT(*) FROM exam_submissions WHERE examId = :examId AND status IN ('SUBMITTED', 'EVALUATED')")
    fun getCompletedSubmissionsCount(examId: String): Flow<Int>
}


