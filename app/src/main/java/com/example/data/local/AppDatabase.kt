package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.core.ExamQuestionBank
import com.example.core.SecurityUtils
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StudentEntity::class,
        CourseEntity::class,
        TeacherEntity::class,
        BatchEntity::class,
        AttendanceEntity::class,
        FeeRecordEntity::class,
        FeeReminderEntity::class,
        ExamResultEntity::class,
        StudyMaterialEntity::class,
        AssignmentEntity::class,
        SubmissionEntity::class,
        NoticeEntity::class,
        AdminAccountEntity::class,
        AuditLogEntity::class,
        TimetableEntity::class,
        QRAttendanceSessionEntity::class,
        ParentGuardianEntity::class,
        DigitalCertificateEntity::class,
        LearningGoalEntity::class,
        AchievementBadgeEntity::class,
        DigitalBookEntity::class,
        ScheduledExamEntity::class,
        ExamSubmissionEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun teacherDao(): TeacherDao
    abstract fun batchDao(): BatchDao
    abstract fun courseDao(): CourseDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun feeRecordDao(): FeeRecordDao
    abstract fun feeReminderDao(): FeeReminderDao
    abstract fun examResultDao(): ExamResultDao
    abstract fun academicDao(): AcademicDao
    abstract fun noticeDao(): NoticeDao
    abstract fun adminAccountDao(): AdminAccountDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun qrAttendanceDao(): QRAttendanceDao
    abstract fun parentGuardianDao(): ParentGuardianDao
    abstract fun certificateDao(): DigitalCertificateDao
    abstract fun learningGoalDao(): LearningGoalDao
    abstract fun achievementBadgeDao(): AchievementBadgeDao
    abstract fun digitalBookDao(): DigitalBookDao
    abstract fun scheduledExamDao(): ScheduledExamDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE students ADD COLUMN rollNo TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE students ADD COLUMN fatherName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE students ADD COLUMN motherName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE students ADD COLUMN gender TEXT NOT NULL DEFAULT 'Male'")
                db.execSQL("ALTER TABLE students ADD COLUMN totalCourseFee REAL NOT NULL DEFAULT 15000.0")
                db.execSQL("ALTER TABLE students ADD COLUMN feePaid REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE students ADD COLUMN status TEXT NOT NULL DEFAULT 'ACTIVE'")
                db.execSQL("ALTER TABLE students ADD COLUMN blockReason TEXT")
            }
        }

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS parent_guardians (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        guardianId TEXT NOT NULL,
                        username TEXT NOT NULL,
                        passwordHash TEXT NOT NULL,
                        fullName TEXT NOT NULL,
                        phone TEXT NOT NULL,
                        email TEXT NOT NULL,
                        linkedStudentId TEXT NOT NULL,
                        relationship TEXT NOT NULL,
                        lastLogin INTEGER,
                        status TEXT NOT NULL DEFAULT 'ACTIVE'
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_parent_guardians_guardianId ON parent_guardians(guardianId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_parent_guardians_username ON parent_guardians(username)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS digital_certificates (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        certificateNumber TEXT NOT NULL,
                        studentId TEXT NOT NULL,
                        studentName TEXT NOT NULL,
                        rollNo TEXT NOT NULL,
                        courseId TEXT NOT NULL,
                        courseName TEXT NOT NULL,
                        grade TEXT NOT NULL,
                        issueDate TEXT NOT NULL,
                        issuedBy TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'VALID',
                        verificationToken TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_digital_certificates_certificateNumber ON digital_certificates(certificateNumber)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_digital_certificates_verificationToken ON digital_certificates(verificationToken)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS learning_goals (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        studentId TEXT NOT NULL,
                        courseId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        targetDate TEXT NOT NULL,
                        progressPercent INTEGER NOT NULL DEFAULT 0,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        assignedBy TEXT NOT NULL DEFAULT 'System'
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_learning_goals_studentId ON learning_goals(studentId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS achievement_badges (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        studentId TEXT NOT NULL,
                        badgeCode TEXT NOT NULL,
                        badgeName TEXT NOT NULL,
                        description TEXT NOT NULL,
                        category TEXT NOT NULL,
                        awardedDate TEXT NOT NULL,
                        iconName TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_achievement_badges_studentId ON achievement_badges(studentId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS digital_books (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        author TEXT NOT NULL,
                        courseId TEXT NOT NULL,
                        subject TEXT NOT NULL,
                        category TEXT NOT NULL,
                        fileSize TEXT NOT NULL,
                        downloadCount INTEGER NOT NULL DEFAULT 0,
                        description TEXT NOT NULL,
                        readTimeMinutes INTEGER NOT NULL DEFAULT 25,
                        isDownloaded INTEGER NOT NULL DEFAULT 0,
                        contentSummary TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_digital_books_courseId ON digital_books(courseId)")
            }
        }

        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS scheduled_exams (
                        examId TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        courseId TEXT NOT NULL,
                        courseName TEXT NOT NULL,
                        scheduledStartTime INTEGER NOT NULL,
                        durationMinutes INTEGER NOT NULL,
                        totalQuestions INTEGER NOT NULL DEFAULT 50,
                        passingMarks INTEGER NOT NULL DEFAULT 20,
                        questionsJson TEXT NOT NULL,
                        assignedStudentIds TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'SCHEDULED',
                        createdByName TEXT NOT NULL DEFAULT 'Administrator',
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_scheduled_exams_courseId ON scheduled_exams(courseId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_scheduled_exams_status ON scheduled_exams(status)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS exam_submissions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        examId TEXT NOT NULL,
                        studentId TEXT NOT NULL,
                        studentName TEXT NOT NULL,
                        rollNo TEXT NOT NULL,
                        startedAt INTEGER NOT NULL,
                        submittedAt INTEGER,
                        answersJson TEXT NOT NULL DEFAULT '{}',
                        score INTEGER,
                        totalMarks INTEGER NOT NULL DEFAULT 50,
                        percentage REAL NOT NULL DEFAULT 0.0,
                        grade TEXT NOT NULL DEFAULT 'F',
                        status TEXT NOT NULL DEFAULT 'IN_PROGRESS',
                        autoSubmitted INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exam_submissions_examId ON exam_submissions(examId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exam_submissions_studentId ON exam_submissions(studentId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_exam_submissions_examId_studentId ON exam_submissions(examId, studentId)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "edusync_institute.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val count = database.adminAccountDao().getTotalAdminCountDirect()
                        if (count == 0) {
                            populateInitialData(database)
                        }
                    }
                }
            }

            private suspend fun populateInitialData(db: AppDatabase) {
                // Initial Admin
                val adminHash = SecurityUtils.hashPassword("Admin123")
                db.adminAccountDao().insertAdmin(
                    AdminAccountEntity(
                        username = "admin",
                        passwordHash = adminHash,
                        fullName = "Waqar Administrator",
                        email = "admin@waqarinstitute.edu",
                        phone = "+91 99999 88888",
                        role = "SUPER_ADMIN",
                        isActive = true,
                        isInitialPassword = true
                    )
                )

                // Initial Courses
                val courses = listOf(
                    CourseEntity(
                        id = "CRS_ADCA",
                        name = "Advance Diploma in Computer Applications (ADCA)",
                        code = "ADCA-201",
                        duration = "1 Year",
                        totalFees = 16500.0,
                        description = "Comprehensive master diploma covering Desktop Publishing, Financial Accounting (Tally), Web Designing & Office Tools.",
                        syllabus = "Semester 1: DCA Core + Photoshop + CorelDraw\nSemester 2: Tally Prime with GST, HTML5/CSS3, Python Basics & Hardware Maintenance",
                        modulesCount = 8
                    ),
                    CourseEntity(
                        id = "CRS_TALLY",
                        name = "Certificate in Financial Accounting (Tally Prime)",
                        code = "TALLY-501",
                        duration = "3 Months",
                        totalFees = 4500.0,
                        description = "Professional accounting certification with GST compliance and payroll management.",
                        syllabus = "Fundamentals of Accounting, Inventory Vouchers, Cost Centres, TDS & GST Returns, e-Way Bills.",
                        modulesCount = 4
                    )
                )
                db.courseDao().insertBatch(courses)

                // Initial Batches
                val batches = listOf(
                    BatchEntity(
                        id = "BCH_ADCA_MORN",
                        courseId = "CRS_ADCA",
                        name = "ADCA Morning Zenith",
                        timing = "09:00 AM - 10:30 AM",
                        startDate = "2026-08-01",
                        teacherId = "TCH-101",
                        capacity = 30,
                        enrolledCount = 2,
                        status = "ACTIVE"
                    )
                )
                db.batchDao().insertBatch(batches)

                // Initial Teachers
                val teacherHash = SecurityUtils.hashPassword("Teacher@123")
                val teachers = listOf(
                    TeacherEntity(
                        teacherId = "TCH-101",
                        username = "prof_sharma",
                        passwordHash = teacherHash,
                        fullName = "Rajesh Sharma",
                        email = "rajesh.sharma@waqarinstitute.edu",
                        phone = "+91 98765 00001",
                        specialization = "Advanced Accounting & Tax (Tally)",
                        status = "ACTIVE",
                        isInitialPassword = true
                    ),
                    TeacherEntity(
                        teacherId = "TCH-102",
                        username = "anita_deshmukh",
                        passwordHash = teacherHash,
                        fullName = "Anita Deshmukh",
                        email = "anita.web@waqarinstitute.edu",
                        phone = "+91 98765 00002",
                        specialization = "Web & Graphic Design",
                        status = "ACTIVE",
                        isInitialPassword = true
                    )
                )
                db.teacherDao().insertBatch(teachers)

                // Initial Students
                val studentHash = SecurityUtils.hashPassword("Student@123")
                val students = listOf(
                    StudentEntity(
                        studentId = "EDU-2026-1001",
                        username = "EDU-2026-1001", // Match ID initially
                        passwordHash = studentHash,
                        fullName = "Rahul Sharma",
                        rollNo = "1001",
                        batch = "ADCA Morning Zenith",
                        courseId = "CRS_ADCA",
                        courseName = "Advance Diploma in Computer Applications (ADCA)",
                        email = "rahul.s@example.com",
                        phone = "+91 98765 12345",
                        gender = "Male",
                        fatherName = "Ramesh Sharma",
                        motherName = "Sita Sharma",
                        totalCourseFee = 16500.0,
                        feePaid = 5000.0,
                        status = "ACTIVE",
                        isTemporaryPassword = true
                    ),
                    StudentEntity(
                        studentId = "EDU-2026-1002",
                        username = "priya.verma",
                        passwordHash = studentHash,
                        fullName = "Priya Verma",
                        rollNo = "1002",
                        batch = "ADCA Morning Zenith",
                        courseId = "CRS_ADCA",
                        courseName = "Advance Diploma in Computer Applications (ADCA)",
                        email = "priya.v@example.com",
                        phone = "+91 98234 56780",
                        gender = "Female",
                        fatherName = "Sunil Verma",
                        motherName = "Kavita Verma",
                        totalCourseFee = 16500.0,
                        feePaid = 16500.0,
                        status = "ACTIVE",
                        isTemporaryPassword = true
                    )
                )
                db.studentDao().insertBatch(students)

                // Initial Timetables
                val timetables = listOf(
                    TimetableEntity(
                        batchId = "BCH_ADCA_MORN",
                        batchName = "ADCA Morning Zenith",
                        courseId = "CRS_ADCA",
                        dayOfWeek = "MONDAY",
                        startTime = "09:00 AM",
                        endTime = "10:30 AM",
                        subject = "Computer Fundamentals & OS",
                        teacherId = "TCH-102",
                        teacherName = "Anita Deshmukh",
                        roomOrLab = "Lab 1",
                        isPractical = true
                    ),
                    TimetableEntity(
                        batchId = "BCH_ADCA_MORN",
                        batchName = "ADCA Morning Zenith",
                        courseId = "CRS_ADCA",
                        dayOfWeek = "TUESDAY",
                        startTime = "09:00 AM",
                        endTime = "10:30 AM",
                        subject = "Desktop Publishing (Corel & Photoshop)",
                        teacherId = "TCH-102",
                        teacherName = "Anita Deshmukh",
                        roomOrLab = "Lab 2",
                        isPractical = true
                    ),
                    TimetableEntity(
                        batchId = "BCH_ADCA_MORN",
                        batchName = "ADCA Morning Zenith",
                        courseId = "CRS_ADCA",
                        dayOfWeek = "WEDNESDAY",
                        startTime = "09:00 AM",
                        endTime = "10:30 AM",
                        subject = "Financial Accounting Tally Prime",
                        teacherId = "TCH-101",
                        teacherName = "Rajesh Sharma",
                        roomOrLab = "Lab 1",
                        isPractical = true
                    ),
                    TimetableEntity(
                        batchId = "BCH_ADCA_MORN",
                        batchName = "ADCA Morning Zenith",
                        courseId = "CRS_ADCA",
                        dayOfWeek = "THURSDAY",
                        startTime = "09:00 AM",
                        endTime = "10:30 AM",
                        subject = "MS Office (Word, Excel, Ppt)",
                        teacherId = "TCH-102",
                        teacherName = "Anita Deshmukh",
                        roomOrLab = "Lab 2",
                        isPractical = true
                    )
                )
                db.academicDao().insertTimetables(timetables)

                // Initial Notices
                db.noticeDao().insertNotice(
                    NoticeEntity(
                        title = "DCA Batch Mid-Term Examination Schedule",
                        content = "All students of the DCA Evening batch are hereby informed that the mid-term practical examinations will be held starting from next Monday. Please ensure your fee dues are cleared.",
                        date = "2026-10-15",
                        category = "EXAM",
                        targetAudience = "STUDENT",
                        authorId = "ADMIN"
                    )
                )
                db.noticeDao().insertNotice(
                    NoticeEntity(
                        title = "Staff Meeting: Implementation of New Syllabus",
                        content = "A mandatory staff meeting is scheduled for Saturday at 2:00 PM to discuss the integration of Python programming modules into the Advanced ADCA curriculum.",
                        date = "2026-10-10",
                        category = "ACADEMIC",
                        targetAudience = "TEACHER",
                        authorId = "ADMIN"
                    )
                )

                // Initial Parent / Guardian (Username: guardian, Password: Parent123)
                val guardianHash = SecurityUtils.hashPassword("Parent123")
                db.parentGuardianDao().insertGuardian(
                    ParentGuardianEntity(
                        guardianId = "GRD-2026-001",
                        username = "guardian",
                        passwordHash = guardianHash,
                        fullName = "Ramesh Sharma",
                        phone = "+91 98765 43210",
                        email = "ramesh.sharma@example.com",
                        linkedStudentId = "EDU-2026-1001",
                        relationship = "Father"
                    )
                )
                db.parentGuardianDao().insertGuardian(
                    ParentGuardianEntity(
                        guardianId = "GRD-2026-002",
                        username = "parent_priya",
                        passwordHash = guardianHash,
                        fullName = "Sunil Verma",
                        phone = "+91 98234 56789",
                        email = "sunil.verma@example.com",
                        linkedStudentId = "EDU-2026-1002",
                        relationship = "Father"
                    )
                )

                // Initial Digital Certificates
                db.certificateDao().insertCertificate(
                    DigitalCertificateEntity(
                        certificateNumber = "CERT-2026-8941",
                        studentId = "EDU-2026-1001",
                        studentName = "Rahul Sharma",
                        rollNo = "1001",
                        courseId = "CRS_ADCA",
                        courseName = "Advance Diploma in Computer Applications (ADCA)",
                        grade = "A+",
                        issueDate = "2026-10-15",
                        issuedBy = "Director of Computer Examinations",
                        status = "VALID",
                        verificationToken = "VERIFY-ADCA-8941"
                    )
                )

                // Initial Learning Goals
                db.learningGoalDao().insertGoal(
                    LearningGoalEntity(
                        studentId = "EDU-2026-1001",
                        courseId = "CRS_ADCA",
                        title = "Master Advanced Excel Macros & VBA",
                        description = "Complete 5 automated ledger scripts and pivot analysis.",
                        targetDate = "2026-11-10",
                        progressPercent = 75,
                        isCompleted = false,
                        assignedBy = "Prof. Rajesh Sharma"
                    )
                )
                db.learningGoalDao().insertGoal(
                    LearningGoalEntity(
                        studentId = "EDU-2026-1001",
                        courseId = "CRS_ADCA",
                        title = "Tally Prime GST Voucher Entry",
                        description = "Practice sales, purchase, and debit note voucher transactions.",
                        targetDate = "2026-10-28",
                        progressPercent = 100,
                        isCompleted = true,
                        assignedBy = "Self"
                    )
                )

                // Initial Achievement Badges
                db.achievementBadgeDao().insertBadge(
                    AchievementBadgeEntity(
                        studentId = "EDU-2026-1001",
                        badgeCode = "PERFECT_ATTENDANCE",
                        badgeName = "Attendance Champion",
                        description = "Maintained greater than 90% attendance across the quarter.",
                        category = "ATTENDANCE",
                        awardedDate = "2026-10-01",
                        iconName = "Star"
                    )
                )
                db.achievementBadgeDao().insertBadge(
                    AchievementBadgeEntity(
                        studentId = "EDU-2026-1001",
                        badgeCode = "EXAM_EXCELLENCE",
                        badgeName = "Distinction Scholar",
                        description = "Scored 92% in Financial Accounting & Tally Examination.",
                        category = "EXAM_PERFECTION",
                        awardedDate = "2026-10-15",
                        iconName = "WorkspacePremium"
                    )
                )

                // Initial 50-MCQ Scheduled Online Exam
                val defaultQuestions = ExamQuestionBank.getDefault50Questions("CRS_ADCA")
                db.scheduledExamDao().insertExam(
                    ScheduledExamEntity(
                        examId = "EXM-2026-ADCA-01",
                        title = "ADCA Comprehensive Online Examination (50 MCQs)",
                        courseId = "CRS_ADCA",
                        courseName = "Advance Diploma in Computer Applications (ADCA)",
                        scheduledStartTime = System.currentTimeMillis() - 3600_000L,
                        durationMinutes = 45,
                        totalQuestions = 50,
                        passingMarks = 20,
                        questionsJson = ExamQuestionBank.serializeQuestions(defaultQuestions),
                        assignedStudentIds = "ALL",
                        status = "ACTIVE",
                        createdByName = "Prof. Rajesh Sharma"
                    )
                )

                // Initial Digital Library Books
                val books = listOf(
                    DigitalBookEntity(
                        title = "Mastering Tally Prime with GST 2026",
                        author = "Er. Sanjeev Kapoor, CA",
                        courseId = "CRS_ADCA",
                        subject = "Financial Accounting",
                        category = "E-BOOK",
                        fileSize = "14.2 MB",
                        downloadCount = 184,
                        description = "Comprehensive practical handbook covering company creation, inventory management, e-invoicing, TDS, and GST returns.",
                        readTimeMinutes = 60,
                        isDownloaded = true,
                        contentSummary = "Chapter 1: Accounting Principles\nChapter 2: Ledger Groups & Cost Centres\nChapter 3: Goods & Services Tax (GST) Compliance\nChapter 4: Payroll & Bank Reconciliation"
                    ),
                    DigitalBookEntity(
                        title = "Python Programming for Beginners to Pro",
                        author = "Dr. Alan V. Hastings",
                        courseId = "CRS_PYTHON",
                        subject = "Python Core",
                        category = "E-BOOK",
                        fileSize = "22.5 MB",
                        downloadCount = 312,
                        description = "Step-by-step programming textbook covering Python syntax, data structures, OOP, file handling, and NumPy/Pandas essentials.",
                        readTimeMinutes = 90,
                        isDownloaded = false,
                        contentSummary = "Module 1: Variables, Types & Control Structures\nModule 2: Functions, Lambda & Modules\nModule 3: Object-Oriented Design\nModule 4: Data Analysis with Pandas"
                    ),
                    DigitalBookEntity(
                        title = "Modern Web Development (HTML5, CSS3 & JavaScript)",
                        author = "Anita Deshmukh",
                        courseId = "CRS_WEB",
                        subject = "Web Design",
                        category = "NOTES",
                        fileSize = "9.4 MB",
                        downloadCount = 245,
                        description = "Visual quick-reference cards with full examples for Flexbox, CSS Grid, Responsive Design, and ES6+ JavaScript.",
                        readTimeMinutes = 35,
                        isDownloaded = true,
                        contentSummary = "Section 1: Semantic HTML5 Elements\nSection 2: CSS Layouts (Flex & Grid)\nSection 3: DOM Event Handling & Fetch API\nSection 4: Deployment Best Practices"
                    ),
                    DigitalBookEntity(
                        title = "Cybersecurity Fundamentals & Network Defence",
                        author = "Col. R. K. Nair",
                        courseId = "CRS_CYBER",
                        subject = "Cyber Security",
                        category = "REFERENCE",
                        fileSize = "18.1 MB",
                        downloadCount = 98,
                        description = "Security manual for IT technicians, networking protocols, firewalls, threat analysis, and digital forensics.",
                        readTimeMinutes = 50,
                        isDownloaded = false,
                        contentSummary = "Unit 1: Network Architecture & OSI Model\nUnit 2: Cryptography & SSL/TLS\nUnit 3: Ethical Hacking Tools\nUnit 4: Security Incident Response"
                    ),
                    DigitalBookEntity(
                        title = "DCA Complete Practical Lab Manual",
                        author = "EduSync Technical Committee",
                        courseId = "CRS_DCA",
                        subject = "Computer Basics",
                        category = "PRACTICAL_MANUAL",
                        fileSize = "7.8 MB",
                        downloadCount = 520,
                        description = "Detailed practical exercises with screenshots for Windows OS, MS Word formatting, Excel formulas, and PowerPoint slides.",
                        readTimeMinutes = 30,
                        isDownloaded = true,
                        contentSummary = "Exercise 1: File Systems & Keyboard Shortcuts\nExercise 2: Advanced Word Mail Merge\nExercise 3: Financial Functions in Excel\nExercise 4: Slide Transitions & Timings"
                    )
                )
                db.digitalBookDao().insertBooks(books)
            }
        }
    }
}
