package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    indices = [
        Index(value = ["studentId"], unique = true),
        Index(value = ["username"], unique = true),
        Index(value = ["fullName"]),
        Index(value = ["courseId"]),
        Index(value = ["batch"]),
        Index(value = ["status"])
    ]
)
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: String,
    val rollNo: String = "",
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val fatherName: String = "",
    val motherName: String = "",
    val gender: String = "Male",
    val photoUri: String? = null,
    val email: String,
    val phone: String,
    val dateOfBirth: String = "2003-05-15",
    val address: String = "Main Market Road, City Center",
    val guardianName: String = "Guardian Name",
    val courseId: String,
    val courseName: String,
    val batch: String = "Morning (09:00 AM - 11:00 AM)",
    val enrollmentDate: String = "2026-01-10",
    val feeDueDate: String = "2026-10-15", // for overdue calculation
    val totalCourseFee: Double = 15000.0,
    val feePaid: Double = 0.0,
    val status: String = "ACTIVE", // ACTIVE, BLOCKED, ARCHIVED
    val blockReason: String? = null,
    val isTemporaryPassword: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val code: String,
    val duration: String,
    val totalFees: Double,
    val description: String,
    val syllabus: String,
    val modulesCount: Int = 8
)

@Entity(
    tableName = "teachers",
    indices = [
        Index(value = ["teacherId"], unique = true),
        Index(value = ["username"], unique = true),
        Index(value = ["status"])
    ]
)
data class TeacherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val teacherId: String,
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val specialization: String,
    val assignedBatches: String = "Morning (09:00 AM - 11:00 AM)", // comma-separated or primary batch
    val isBatchAdmin: Boolean = false, // Designates teacher as Batch Administrator
    val status: String = "ACTIVE", // ACTIVE, DISABLED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "batches")
data class BatchEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val courseId: String,
    val courseName: String,
    val timing: String,
    val assignedTeacherId: String? = null,
    val assignedTeacherName: String? = null,
    val batchAdminTeacherId: String? = null,
    val batchAdminTeacherName: String? = null
)

@Entity(
    tableName = "attendance",
    indices = [
        Index(value = ["studentId", "date"], unique = true),
        Index(value = ["date"])
    ]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: String,
    val date: String, // YYYY-MM-DD
    val status: String, // PRESENT, ABSENT, LATE, LEAVE
    val remarks: String? = null,
    val markedByTeacherId: String? = null,
    val markedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "fee_records",
    indices = [
        Index(value = ["receiptNumber"], unique = true),
        Index(value = ["studentId"]),
        Index(value = ["paymentDate"])
    ]
)
data class FeeRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val receiptNumber: String,
    val studentId: String,
    val studentName: String = "",
    val courseName: String = "",
    val batch: String = "",
    val amount: Double,
    val paymentDate: String, // YYYY-MM-DD
    val paymentMethod: String, // Cash, UPI, Net Banking, Card
    val paymentType: String = "MANUAL_COUNTER", // MANUAL_COUNTER vs ONLINE_VERIFIED
    val transactionId: String,
    val remarks: String = "Tuition Fee Installment",
    val recordedBy: String = "Admin",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "fee_reminders",
    indices = [
        Index(value = ["studentId"]),
        Index(value = ["sentDate"])
    ]
)
data class FeeReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: String,
    val studentName: String,
    val courseName: String,
    val pendingAmount: Double,
    val dueDate: String,
    val sentDate: String,
    val status: String = "DELIVERED",
    val channel: String = "IN_APP_NOTIFICATION",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "exam_results",
    indices = [
        Index(value = ["studentId", "examName"]),
        Index(value = ["studentId"])
    ]
)
data class ExamResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: String,
    val examName: String,
    val examDate: String,
    val courseId: String,
    val totalMarks: Double = 100.0,
    val obtainedMarks: Double,
    val percentage: Double,
    val grade: String, // A+, A, B, C, F
    val rank: Int = 1,
    val isPublished: Boolean = true,
    val certificateNumber: String? = null,
    val certificateIssueDate: String? = null
)

@Entity(tableName = "study_materials", indices = [Index(value = ["courseId"])])
data class StudyMaterialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseId: String,
    val title: String,
    val description: String,
    val type: String, // PDF_NOTES, VIDEO_CLASS, SYLLABUS
    val contentUrl: String,
    val fileSize: String? = null,
    val uploadedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "assignments", indices = [Index(value = ["courseId"])])
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseId: String,
    val title: String,
    val instructions: String,
    val dueDate: String,
    val maxMarks: Int = 50,
    val batchName: String? = null, // specific batch or all
    val createdById: String = "ADMIN", // TeacherId or ADMIN
    val createdByName: String = "Administrator",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "submissions",
    indices = [
        Index(value = ["assignmentId", "studentId"], unique = true),
        Index(value = ["studentId"])
    ]
)
data class SubmissionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assignmentId: Long,
    val studentId: String,
    val submissionContent: String,
    val submittedAt: Long = System.currentTimeMillis(),
    val marksObtained: Int? = null,
    val feedback: String? = null,
    val gradedByTeacherId: String? = null,
    val status: String = "SUBMITTED" // SUBMITTED, GRADED
)

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val priority: String = "GENERAL", // URGENT, IMPORTANT, GENERAL
    val category: String = "ACADEMIC", // EXAM, FEES, HOLIDAY, ACADEMIC
    val date: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_accounts")
data class AdminAccountEntity(
    @PrimaryKey
    val username: String,
    val passwordHash: String,
    val isInitialPassword: Boolean = true, // Force change if true!
    val lastLoginAt: Long? = null,
    val failedLoginAttempts: Int = 0,
    val lockedUntil: Long? = null
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val action: String,
    val performedBy: String,
    val targetId: String? = null,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "timetables",
    indices = [
        Index(value = ["batchName"]),
        Index(value = ["teacherId"]),
        Index(value = ["dayOfWeek"])
    ]
)
data class TimetableEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: String, // Monday, Tuesday, Wednesday, Thursday, Friday, Saturday
    val courseId: String,
    val courseName: String,
    val batchName: String,
    val subject: String,
    val teacherId: String,
    val teacherName: String,
    val roomOrLab: String, // e.g. "Lab 1 (Systems)", "Room 102"
    val startTime: String, // "09:00 AM"
    val endTime: String,   // "10:30 AM"
    val status: String = "SCHEDULED" // SCHEDULED, CANCELLED, RESCHEDULED
)

@Entity(
    tableName = "qr_attendance_sessions",
    indices = [
        Index(value = ["sessionCode"], unique = true),
        Index(value = ["batchName"])
    ]
)
data class QRAttendanceSessionEntity(
    @PrimaryKey
    val sessionId: String,
    val sessionCode: String, // Short code like "QR-9421"
    val batchName: String,
    val courseId: String,
    val teacherId: String,
    val teacherName: String,
    val date: String, // YYYY-MM-DD
    val subject: String,
    val createdTime: Long = System.currentTimeMillis(),
    val expiryTime: Long, // timestamp when expires
    val status: String = "ACTIVE" // ACTIVE, CLOSED, EXPIRED
)

@Entity(
    tableName = "parent_guardians",
    indices = [
        Index(value = ["guardianId"], unique = true),
        Index(value = ["username"], unique = true),
        Index(value = ["linkedStudentId"])
    ]
)
data class ParentGuardianEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val guardianId: String,
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val linkedStudentId: String, // Student ID they have permission to monitor
    val relationship: String = "Parent", // Father, Mother, Guardian
    val status: String = "ACTIVE", // ACTIVE, REVOKED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "digital_certificates",
    indices = [
        Index(value = ["certificateNumber"], unique = true),
        Index(value = ["verificationToken"], unique = true),
        Index(value = ["studentId"])
    ]
)
data class DigitalCertificateEntity(
    @PrimaryKey
    val certificateNumber: String,
    val studentId: String,
    val studentName: String,
    val rollNo: String,
    val courseId: String,
    val courseName: String,
    val grade: String, // A+, A, B, C
    val issueDate: String, // YYYY-MM-DD
    val issuedBy: String = "Director of Computer Examinations",
    val status: String = "VALID", // VALID, CANCELLED, EXPIRED, REPLACED
    val cancellationReason: String? = null,
    val cancelledBy: String? = null,
    val cancelledAt: Long? = null,
    val verificationToken: String
)

@Entity(
    tableName = "learning_goals",
    indices = [Index(value = ["studentId"])]
)
data class LearningGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: String,
    val courseId: String,
    val title: String,
    val description: String,
    val targetDate: String,
    val progressPercent: Int = 0, // 0 to 100
    val isCompleted: Boolean = false,
    val assignedBy: String = "Self", // "Self" or Teacher Name
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "achievement_badges",
    indices = [Index(value = ["studentId"])]
)
data class AchievementBadgeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: String,
    val badgeCode: String,
    val badgeName: String,
    val description: String,
    val category: String, // ATTENDANCE, EXAM_PERFECTION, CODING, ASSIGNMENTS
    val awardedDate: String,
    val iconName: String
)

@Entity(
    tableName = "digital_books",
    indices = [Index(value = ["courseId"])]
)
data class DigitalBookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String,
    val courseId: String,
    val subject: String,
    val category: String, // E-BOOK, PRACTICAL_MANUAL, NOTES, REFERENCE
    val fileSize: String,
    val downloadCount: Int = 0,
    val description: String,
    val readTimeMinutes: Int = 25,
    val isDownloaded: Boolean = false,
    val contentSummary: String
)

@Entity(
    tableName = "scheduled_exams",
    indices = [
        Index(value = ["courseId"]),
        Index(value = ["status"])
    ]
)
data class ScheduledExamEntity(
    @PrimaryKey
    val examId: String,
    val title: String,
    val courseId: String,
    val courseName: String,
    val scheduledStartTime: Long,
    val durationMinutes: Int = 45,
    val totalQuestions: Int = 50,
    val passingMarks: Int = 20,
    val questionsJson: String,
    val assignedStudentIds: String = "ALL",
    val status: String = "SCHEDULED",
    val createdByName: String = "Administrator",
    val createdAt: Long = System.currentTimeMillis()
) {
    val scheduledEndTime: Long
        get() = scheduledStartTime + (durationMinutes * 60 * 1000L)
    val isPublished: Boolean
        get() = status != "CLOSED"
    val allowedStudentIdsJson: String
        get() = assignedStudentIds
    val totalMarks: Int
        get() = totalQuestions
}

@Entity(
    tableName = "exam_submissions",
    indices = [
        Index(value = ["examId", "studentId"], unique = true),
        Index(value = ["studentId"]),
        Index(value = ["status"])
    ]
)
data class ExamSubmissionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val examId: String,
    val studentId: String,
    val studentName: String,
    val rollNo: String = "",
    val startedAt: Long = System.currentTimeMillis(),
    val submittedAt: Long? = null,
    val answersJson: String = "{}",
    val score: Int? = null,
    val totalMarks: Int = 50,
    val percentage: Double = 0.0,
    val grade: String = "F",
    val status: String = "IN_PROGRESS",
    val autoSubmitted: Boolean = false
) {
    val submissionId: Long
        get() = id
    val isAutoSubmitted: Boolean
        get() = autoSubmitted
    val isPassed: Boolean
        get() = (grade == "A+" || grade == "A" || grade == "B" || grade == "C") && (score ?: 0) >= 20
}

data class ExamQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int
) {
    val questionText: String
        get() = question
}

