package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
    abstract fun adminDao(): AdminDao
    abstract fun timetableDao(): TimetableDao
    abstract fun qrAttendanceDao(): QRAttendanceDao
    abstract fun parentGuardianDao(): ParentGuardianDao
    abstract fun certificateDao(): CertificateDao
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
            }
        }

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS timetables (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        dayOfWeek TEXT NOT NULL,
                        courseId TEXT NOT NULL,
                        courseName TEXT NOT NULL,
                        batchName TEXT NOT NULL,
                        subject TEXT NOT NULL,
                        teacherId TEXT NOT NULL,
                        teacherName TEXT NOT NULL,
                        roomOrLab TEXT NOT NULL,
                        startTime TEXT NOT NULL,
                        endTime TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'SCHEDULED'
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_timetables_batchName ON timetables(batchName)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_timetables_teacherId ON timetables(teacherId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_timetables_dayOfWeek ON timetables(dayOfWeek)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS qr_attendance_sessions (
                        sessionId TEXT PRIMARY KEY NOT NULL,
                        sessionCode TEXT NOT NULL,
                        batchName TEXT NOT NULL,
                        courseId TEXT NOT NULL,
                        teacherId TEXT NOT NULL,
                        teacherName TEXT NOT NULL,
                        date TEXT NOT NULL,
                        subject TEXT NOT NULL,
                        createdTime INTEGER NOT NULL,
                        expiryTime INTEGER NOT NULL,
                        status TEXT NOT NULL DEFAULT 'ACTIVE'
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_qr_attendance_sessions_sessionCode ON qr_attendance_sessions(sessionCode)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_qr_attendance_sessions_batchName ON qr_attendance_sessions(batchName)")

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
                        relationship TEXT NOT NULL DEFAULT 'Parent',
                        status TEXT NOT NULL DEFAULT 'ACTIVE',
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_parent_guardians_guardianId ON parent_guardians(guardianId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_parent_guardians_username ON parent_guardians(username)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_parent_guardians_linkedStudentId ON parent_guardians(linkedStudentId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS digital_certificates (
                        certificateNumber TEXT PRIMARY KEY NOT NULL,
                        studentId TEXT NOT NULL,
                        studentName TEXT NOT NULL,
                        rollNo TEXT NOT NULL,
                        courseId TEXT NOT NULL,
                        courseName TEXT NOT NULL,
                        grade TEXT NOT NULL,
                        issueDate TEXT NOT NULL,
                        issuedBy TEXT NOT NULL DEFAULT 'Director of Computer Examinations',
                        status TEXT NOT NULL DEFAULT 'VALID',
                        cancellationReason TEXT,
                        cancelledBy TEXT,
                        cancelledAt INTEGER,
                        verificationToken TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_digital_certificates_certificateNumber ON digital_certificates(certificateNumber)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_digital_certificates_verificationToken ON digital_certificates(verificationToken)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_digital_certificates_studentId ON digital_certificates(studentId)")

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
                        assignedBy TEXT NOT NULL DEFAULT 'Self',
                        createdAt INTEGER NOT NULL
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
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_exam_submissions_examId_studentId ON exam_submissions(examId, studentId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exam_submissions_studentId ON exam_submissions(studentId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exam_submissions_status ON exam_submissions(status)")
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
                    .fallbackToDestructiveMigration(dropAllTables = false)
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
                        ensureAdminAccount(database)
                    }
                }
            }

            private suspend fun ensureAdminAccount(db: AppDatabase) {
                val adminHash = SecurityUtils.hashPassword("Admin123")
                if (db.adminDao().getAdmin("admin") == null) {
                    db.adminDao().insertAdmin(
                        AdminAccountEntity(
                            username = "admin",
                            passwordHash = adminHash,
                            isInitialPassword = false
                        )
                    )
                }
                // Ensure Guardian and Digital Books exist
                if (db.parentGuardianDao().getGuardianByUsername("guardian") == null) {
                    seedNewModules(db)
                }
            }

            private suspend fun populateInitialData(db: AppDatabase) {
                // 1. Initial Admin Accounts (Bootstrap: admin / Admin123)
                val adminHash = SecurityUtils.hashPassword("Admin123")
                db.adminDao().insertAdmin(
                    AdminAccountEntity(
                        username = "admin",
                        passwordHash = adminHash,
                        isInitialPassword = false
                    )
                )
                db.adminDao().insertAdmin(
                    AdminAccountEntity(
                        username = "Admin123",
                        passwordHash = adminHash,
                        isInitialPassword = false
                    )
                )

                // 2. Initial Courses
                val courses = listOf(
                    CourseEntity(
                        id = "CRS_DCA",
                        name = "Diploma in Computer Applications (DCA)",
                        code = "DCA-101",
                        duration = "6 Months",
                        totalFees = 9500.0,
                        description = "Fundamentals of Computers, MS Office Suite, Internet & Email, Operating Systems.",
                        syllabus = "Module 1: Computer Fundamentals\nModule 2: MS Windows & Linux Basics\nModule 3: MS Word, Excel, PowerPoint\nModule 4: Internet, Cybersecurity & Email Management",
                        modulesCount = 4
                    ),
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
                        name = "Tally Prime with Advanced GST & E-Way Bill",
                        code = "TAL-301",
                        duration = "3 Months",
                        totalFees = 6500.0,
                        description = "Professional accounting with Inventory, Payroll, GST Reconciliation, TDS, and Financial Statements.",
                        syllabus = "1. Accounting Fundamentals & Ledgers\n2. Inventory & Stock Groups\n3. GST Returns (GSTR-1, 3B)\n4. Payroll & TDS\n5. Balance Sheet & Audit Reports",
                        modulesCount = 5
                    ),
                    CourseEntity(
                        id = "CRS_WEB",
                        name = "Full Stack Web Development",
                        code = "WEB-401",
                        duration = "6 Months",
                        totalFees = 18000.0,
                        description = "Frontend to backend development with HTML, CSS, JavaScript, React, Node.js, and SQL/MongoDB.",
                        syllabus = "1. HTML5 & Modern CSS3 Flex/Grid\n2. Modern JavaScript (ES6+)\n3. React.js & State Management\n4. REST API & Database Integration\n5. Capstone Project Deployment",
                        modulesCount = 6
                    ),
                    CourseEntity(
                        id = "CRS_PYTHON",
                        name = "Python Programming & Data Analytics",
                        code = "PY-501",
                        duration = "4 Months",
                        totalFees = 12500.0,
                        description = "Python syntax, OOP concepts, Pandas, NumPy, Data Visualization, and Automation scripts.",
                        syllabus = "1. Python Basics & Control Structures\n2. OOP & Functions\n3. NumPy & Pandas\n4. Matplotlib & Data Cleaning\n5. Automation Project",
                        modulesCount = 5
                    ),
                    CourseEntity(
                        id = "CRS_CCC",
                        name = "Course on Computer Concepts (CCC)",
                        code = "CCC-001",
                        duration = "3 Months",
                        totalFees = 4000.0,
                        description = "Government certified computer literacy course covering digital literacy, LibreOffice, and e-governance.",
                        syllabus = "1. Introduction to Computer\n2. GUI Operating System\n3. LibreOffice Writer, Calc, Impress\n4. Digital Financial Services & Cyber Security",
                        modulesCount = 4
                    )
                )
                db.courseDao().insertCourses(courses)

                // 3. Initial Teachers
                val teacherPassHash = SecurityUtils.hashPassword("Teacher@123")
                val teachers = listOf(
                    TeacherEntity(
                        teacherId = "TCH-101",
                        username = "prof_sharma",
                        passwordHash = teacherPassHash,
                        fullName = "Prof. Rajesh Sharma",
                        email = "rajesh.sharma@edusync.org",
                        phone = "9823114455",
                        specialization = "Tally Prime & Accounting",
                        assignedBatches = "Morning (09:00 AM - 11:00 AM)",
                        isBatchAdmin = true,
                        status = "ACTIVE"
                    ),
                    TeacherEntity(
                        teacherId = "TCH-102",
                        username = "anita_deshmukh",
                        passwordHash = teacherPassHash,
                        fullName = "Anita Deshmukh",
                        email = "anita.deshmukh@edusync.org",
                        phone = "9733221100",
                        specialization = "Full Stack Web & Python",
                        assignedBatches = "Evening (05:00 PM - 07:00 PM)",
                        isBatchAdmin = true,
                        status = "ACTIVE"
                    ),
                    TeacherEntity(
                        teacherId = "TCH-103",
                        username = "vikram_singh",
                        passwordHash = teacherPassHash,
                        fullName = "Vikram Singh",
                        email = "vikram.singh@edusync.org",
                        phone = "9944556677",
                        specialization = "DCA & MS Office Suite",
                        assignedBatches = "Afternoon (01:00 PM - 03:00 PM)",
                        isBatchAdmin = false,
                        status = "ACTIVE"
                    )
                )
                db.teacherDao().insertTeachers(teachers)

                // 4. Initial Batches
                val batches = listOf(
                    BatchEntity(
                        id = "BATCH_MRN_1",
                        name = "Morning (09:00 AM - 11:00 AM)",
                        courseId = "CRS_ADCA",
                        courseName = "Advance Diploma in Computer Applications (ADCA)",
                        timing = "09:00 AM - 11:00 AM",
                        assignedTeacherId = "TCH-101",
                        assignedTeacherName = "Prof. Rajesh Sharma",
                        batchAdminTeacherId = "TCH-101",
                        batchAdminTeacherName = "Prof. Rajesh Sharma"
                    ),
                    BatchEntity(
                        id = "BATCH_AFT_1",
                        name = "Afternoon (01:00 PM - 03:00 PM)",
                        courseId = "CRS_TALLY",
                        courseName = "Tally Prime with Advanced GST & E-Way Bill",
                        timing = "01:00 PM - 03:00 PM",
                        assignedTeacherId = "TCH-103",
                        assignedTeacherName = "Vikram Singh",
                        batchAdminTeacherId = "TCH-101",
                        batchAdminTeacherName = "Prof. Rajesh Sharma"
                    ),
                    BatchEntity(
                        id = "BATCH_EVN_1",
                        name = "Evening (05:00 PM - 07:00 PM)",
                        courseId = "CRS_WEB",
                        courseName = "Full Stack Web Development",
                        timing = "05:00 PM - 07:00 PM",
                        assignedTeacherId = "TCH-102",
                        assignedTeacherName = "Anita Deshmukh",
                        batchAdminTeacherId = "TCH-102",
                        batchAdminTeacherName = "Anita Deshmukh"
                    )
                )
                db.batchDao().insertBatches(batches)

                // 5. Initial Demo Students
                val studentPassHash = SecurityUtils.hashPassword("Student@123")
                val students = listOf(
                    StudentEntity(
                        studentId = "EDU-2026-1001",
                        rollNo = "1001",
                        username = "rahul_sharma",
                        passwordHash = studentPassHash,
                        fullName = "Rahul Sharma",
                        fatherName = "Ramesh Sharma",
                        motherName = "Sunita Sharma",
                        gender = "Male",
                        email = "rahul.sharma@example.com",
                        phone = "9876543210",
                        dateOfBirth = "2003-08-14",
                        address = "Plot 42, Green Avenue, Sector 5",
                        guardianName = "Ramesh Sharma",
                        courseId = "CRS_ADCA",
                        courseName = "Advance Diploma in Computer Applications (ADCA)",
                        batch = "Morning (09:00 AM - 11:00 AM)",
                        enrollmentDate = "2026-01-05",
                        feeDueDate = "2026-10-15",
                        totalCourseFee = 16500.0,
                        feePaid = 10000.0,
                        status = "ACTIVE"
                    ),
                    StudentEntity(
                        studentId = "EDU-2026-1002",
                        rollNo = "1002",
                        username = "priya_verma",
                        passwordHash = studentPassHash,
                        fullName = "Priya Verma",
                        fatherName = "Sunil Verma",
                        motherName = "Rekha Verma",
                        gender = "Female",
                        email = "priya.verma@example.com",
                        phone = "9823456789",
                        dateOfBirth = "2004-02-21",
                        address = "Flat 102, Shanti Vihar",
                        guardianName = "Sunil Verma",
                        courseId = "CRS_TALLY",
                        courseName = "Tally Prime with Advanced GST & E-Way Bill",
                        batch = "Afternoon (01:00 PM - 03:00 PM)",
                        enrollmentDate = "2026-01-15",
                        feeDueDate = "2026-10-10",
                        totalCourseFee = 6500.0,
                        feePaid = 6500.0,
                        status = "ACTIVE"
                    ),
                    StudentEntity(
                        studentId = "EDU-2026-1003",
                        rollNo = "1003",
                        username = "amit_patel",
                        passwordHash = studentPassHash,
                        fullName = "Amit Patel",
                        fatherName = "Bhupendra Patel",
                        motherName = "Geeta Patel",
                        gender = "Male",
                        email = "amit.patel@example.com",
                        phone = "9712345678",
                        dateOfBirth = "2002-11-30",
                        address = "Near Railway Station, Ward 8",
                        guardianName = "Bhupendra Patel",
                        courseId = "CRS_WEB",
                        courseName = "Full Stack Web Development",
                        batch = "Evening (05:00 PM - 07:00 PM)",
                        enrollmentDate = "2026-02-01",
                        feeDueDate = "2026-11-01",
                        totalCourseFee = 18000.0,
                        feePaid = 12000.0,
                        status = "ACTIVE"
                    ),
                    StudentEntity(
                        studentId = "EDU-2026-1004",
                        rollNo = "1004",
                        username = "sneha_singh",
                        passwordHash = studentPassHash,
                        fullName = "Sneha Singh",
                        fatherName = "Rajendra Singh",
                        motherName = "Kavita Singh",
                        gender = "Female",
                        email = "sneha.singh@example.com",
                        phone = "9988776655",
                        dateOfBirth = "2003-04-12",
                        address = "Civil Lines, Near City Park",
                        guardianName = "Rajendra Singh",
                        courseId = "CRS_DCA",
                        courseName = "Diploma in Computer Applications (DCA)",
                        batch = "Morning (09:00 AM - 11:00 AM)",
                        enrollmentDate = "2026-02-10",
                        feeDueDate = "2026-10-18",
                        totalCourseFee = 9500.0,
                        feePaid = 9500.0,
                        status = "ACTIVE"
                    )
                )
                db.studentDao().insertBatch(students)

                // 6. Sample Fee Records (with Counter vs Online distinction)
                val feeRecords = listOf(
                    FeeRecordEntity(
                        receiptNumber = "REC-2026-0001",
                        studentId = "EDU-2026-1001",
                        studentName = "Rahul Sharma",
                        courseName = "Advance Diploma in Computer Applications (ADCA)",
                        batch = "Morning (09:00 AM - 11:00 AM)",
                        amount = 6000.0,
                        paymentDate = "2026-01-05",
                        paymentMethod = "Cash (Office Counter)",
                        paymentType = "MANUAL_COUNTER",
                        transactionId = "OFC-REC/260105/98214",
                        remarks = "First Admission Installment - Office Counter",
                        recordedBy = "Admin Console"
                    ),
                    FeeRecordEntity(
                        receiptNumber = "REC-2026-0002",
                        studentId = "EDU-2026-1001",
                        studentName = "Rahul Sharma",
                        courseName = "Advance Diploma in Computer Applications (ADCA)",
                        batch = "Morning (09:00 AM - 11:00 AM)",
                        amount = 4000.0,
                        paymentDate = "2026-02-15",
                        paymentMethod = "Bank NEFT Transfer",
                        paymentType = "MANUAL_COUNTER",
                        transactionId = "NEFT/260215/1124",
                        remarks = "Second Quarter Installment - Direct Bank Deposit",
                        recordedBy = "Admin Console"
                    ),
                    FeeRecordEntity(
                        receiptNumber = "REC-2026-0003",
                        studentId = "EDU-2026-1002",
                        studentName = "Priya Verma",
                        courseName = "Tally Prime with Advanced GST & E-Way Bill",
                        batch = "Afternoon (01:00 PM - 03:00 PM)",
                        amount = 6500.0,
                        paymentDate = "2026-01-15",
                        paymentMethod = "Cash (Office Counter)",
                        paymentType = "MANUAL_COUNTER",
                        transactionId = "OFC-REC/260115/004",
                        remarks = "Full Course Fee Paid with Discount",
                        recordedBy = "Admin Console"
                    )
                )
                feeRecords.forEach { db.feeRecordDao().insertFeeRecord(it) }

                // 7. Sample Attendance
                val attendance = listOf(
                    AttendanceEntity(studentId = "EDU-2026-1001", date = "2026-10-18", status = "PRESENT", markedByTeacherId = "TCH-101"),
                    AttendanceEntity(studentId = "EDU-2026-1001", date = "2026-10-19", status = "PRESENT", markedByTeacherId = "TCH-101"),
                    AttendanceEntity(studentId = "EDU-2026-1001", date = "2026-10-20", status = "PRESENT", markedByTeacherId = "TCH-101"),
                    AttendanceEntity(studentId = "EDU-2026-1001", date = "2026-10-21", status = "ABSENT", remarks = "Medical Leave", markedByTeacherId = "TCH-101"),
                    AttendanceEntity(studentId = "EDU-2026-1001", date = "2026-10-22", status = "PRESENT", markedByTeacherId = "TCH-101"),
                    AttendanceEntity(studentId = "EDU-2026-1002", date = "2026-10-21", status = "PRESENT", markedByTeacherId = "TCH-103"),
                    AttendanceEntity(studentId = "EDU-2026-1002", date = "2026-10-22", status = "PRESENT", markedByTeacherId = "TCH-103")
                )
                db.attendanceDao().markBatchAttendance(attendance)

                // 8. Sample Exam Results
                db.examResultDao().insertResult(
                    ExamResultEntity(
                        studentId = "EDU-2026-1001",
                        examName = "ADCA Semester 1 Final Examination",
                        examDate = "2026-09-25",
                        courseId = "CRS_ADCA",
                        totalMarks = 100.0,
                        obtainedMarks = 92.0,
                        percentage = 92.0,
                        grade = "A+",
                        rank = 1,
                        isPublished = true,
                        certificateNumber = "EDUSYNC-CERT-2026-0041",
                        certificateIssueDate = "2026-10-01"
                    )
                )

                // 9. Initial Study Materials
                val materials = listOf(
                    StudyMaterialEntity(
                        courseId = "CRS_ADCA",
                        title = "Chapter 1: MS Word & Office Productivity Notes",
                        description = "Complete guide with shortcut keys, formatting tricks, and mail merge tutorials.",
                        type = "PDF_NOTES",
                        contentUrl = "https://edusync.institute/notes/adca_ch1.pdf",
                        fileSize = "4.2 MB"
                    ),
                    StudyMaterialEntity(
                        courseId = "CRS_ADCA",
                        title = "Lecture 12: Tally Prime Voucher Entries & GST",
                        description = "Video recording explaining Sales, Purchase, Contra, and Payment vouchers in Tally Prime.",
                        type = "VIDEO_CLASS",
                        contentUrl = "https://www.youtube.com/watch?v=sample_video_adca",
                        fileSize = "48 Mins (HD)"
                    ),
                    StudyMaterialEntity(
                        courseId = "CRS_WEB",
                        title = "Complete JavaScript ES6 Handbook",
                        description = "Arrow functions, Promises, Async/Await, and DOM manipulation notes with code snippets.",
                        type = "PDF_NOTES",
                        contentUrl = "https://edusync.institute/notes/js_es6_handbook.pdf",
                        fileSize = "6.8 MB"
                    )
                )
                db.academicDao().insertStudyMaterials(materials)

                // 10. Initial Assignments (with batch targeting and creator tag)
                val assignments = listOf(
                    AssignmentEntity(
                        courseId = "CRS_ADCA",
                        title = "Create Monthly Balance Sheet in MS Excel",
                        instructions = "Design a professional ledger balance sheet using VLOOKUP, SUMIFS, and Pivot Tables.",
                        dueDate = "2026-10-30",
                        maxMarks = 50,
                        batchName = "Morning (09:00 AM - 11:00 AM)",
                        createdById = "TCH-101",
                        createdByName = "Prof. Rajesh Sharma"
                    ),
                    AssignmentEntity(
                        courseId = "CRS_WEB",
                        title = "Build Responsive Portfolio Landing Page",
                        instructions = "Create a mobile-first HTML/CSS portfolio with navbar, projects grid, and contact form.",
                        dueDate = "2026-11-05",
                        maxMarks = 100,
                        batchName = "Evening (05:00 PM - 07:00 PM)",
                        createdById = "TCH-102",
                        createdByName = "Anita Deshmukh"
                    )
                )
                db.academicDao().insertAssignments(assignments)

                // 11. Initial Notices
                val notices = listOf(
                    NoticeEntity(
                        title = "Upcoming Quarterly Exam Schedule",
                        content = "The Quarterly Offline & Online examinations will commence from 15th November 2026. Students must clear any pending fees to download their Admit Cards.",
                        priority = "IMPORTANT",
                        category = "EXAM",
                        date = "2026-10-20"
                    ),
                    NoticeEntity(
                        title = "Diwali & National Holiday Closure Notice",
                        content = "Institute premises will remain closed from 28th October to 2nd November for festive holidays. Online video classes and practice assignments remain accessible in the app.",
                        priority = "GENERAL",
                        category = "HOLIDAY",
                        date = "2026-10-21"
                    )
                )
                db.academicDao().insertNotices(notices)

                // 12. Initial Audit Log
                db.adminDao().insertAuditLog(
                    AuditLogEntity(
                        action = "SYSTEM_INITIALIZATION",
                        performedBy = "System Bootstrap",
                        details = "Database initialized with Teachers, Batches, Courses, and Role-Based Permissions."
                    )
                )

                // 13. Seed New Modules (Timetable, Books, Badges, Certificates, Guardian)
                seedNewModules(db)
            }

            private suspend fun seedNewModules(db: AppDatabase) {
                // Initial Timetables
                val timetables = listOf(
                    TimetableEntity(
                        dayOfWeek = "Monday",
                        courseId = "CRS_ADCA",
                        courseName = "Advance Diploma in Computer Applications (ADCA)",
                        batchName = "Morning (09:00 AM - 11:00 AM)",
                        subject = "Tally Prime with GST",
                        teacherId = "TCH-101",
                        teacherName = "Prof. Rajesh Sharma",
                        roomOrLab = "Lab 1 (Accounting Systems)",
                        startTime = "09:00 AM",
                        endTime = "10:30 AM"
                    ),
                    TimetableEntity(
                        dayOfWeek = "Tuesday",
                        courseId = "CRS_ADCA",
                        courseName = "Advance Diploma in Computer Applications (ADCA)",
                        batchName = "Morning (09:00 AM - 11:00 AM)",
                        subject = "Desktop Publishing (Corel & Photoshop)",
                        teacherId = "TCH-102",
                        teacherName = "Anita Deshmukh",
                        roomOrLab = "Lab 2 (Graphics Studio)",
                        startTime = "09:00 AM",
                        endTime = "10:30 AM"
                    ),
                    TimetableEntity(
                        dayOfWeek = "Wednesday",
                        courseId = "CRS_PYTHON",
                        courseName = "Python Programming & Data Science",
                        batchName = "Evening (05:00 PM - 07:00 PM)",
                        subject = "Python OOP & Pandas",
                        teacherId = "TCH-103",
                        teacherName = "Vikram Singh",
                        roomOrLab = "Lab 3 (Development)",
                        startTime = "05:00 PM",
                        endTime = "06:30 PM"
                    ),
                    TimetableEntity(
                        dayOfWeek = "Thursday",
                        courseId = "CRS_DCA",
                        courseName = "Diploma in Computer Applications (DCA)",
                        batchName = "Afternoon (01:00 PM - 03:00 PM)",
                        subject = "MS Excel Advanced & VLOOKUP",
                        teacherId = "TCH-101",
                        teacherName = "Prof. Rajesh Sharma",
                        roomOrLab = "Room 102",
                        startTime = "01:00 PM",
                        endTime = "02:30 PM"
                    ),
                    TimetableEntity(
                        dayOfWeek = "Friday",
                        courseId = "CRS_ADCA",
                        courseName = "Advance Diploma in Computer Applications (ADCA)",
                        batchName = "Morning (09:00 AM - 11:00 AM)",
                        subject = "Web Fundamentals (HTML5/CSS3)",
                        teacherId = "TCH-102",
                        teacherName = "Anita Deshmukh",
                        roomOrLab = "Lab 1 (Accounting Systems)",
                        startTime = "09:00 AM",
                        endTime = "10:30 AM"
                    ),
                    TimetableEntity(
                        dayOfWeek = "Saturday",
                        courseId = "CRS_PYTHON",
                        courseName = "Python Programming & Data Science",
                        batchName = "Evening (05:00 PM - 07:00 PM)",
                        subject = "Practical Project Review & Lab Work",
                        teacherId = "TCH-103",
                        teacherName = "Vikram Singh",
                        roomOrLab = "Lab 3 (Development)",
                        startTime = "05:00 PM",
                        endTime = "06:30 PM"
                    )
                )
                db.timetableDao().insertTimetables(timetables)

                // Initial Parent / Guardian (Username: guardian, Password: Parent123)
                val guardianHash = SecurityUtils.hashPassword("Parent123")
                db.parentGuardianDao().insertGuardian(
                    ParentGuardianEntity(
                        guardianId = "GRD-2026-001",
                        username = "guardian",
                        passwordHash = guardianHash,
                        fullName = "Ramesh Kumar Sharma",
                        phone = "+91 98765 43210",
                        email = "ramesh.sharma@example.com",
                        linkedStudentId = "STU-2026-001",
                        relationship = "Father"
                    )
                )

                // Initial Digital Certificates
                db.certificateDao().insertCertificate(
                    DigitalCertificateEntity(
                        certificateNumber = "CERT-2026-8941",
                        studentId = "STU-2026-001",
                        studentName = "Amit Kumar",
                        rollNo = "EDUSYNC-2026-101",
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
                        studentId = "STU-2026-001",
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
                        studentId = "STU-2026-001",
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
                        studentId = "STU-2026-001",
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
                        studentId = "STU-2026-001",
                        badgeCode = "EXAM_EXCELLENCE",
                        badgeName = "Distinction Scholar",
                        description = "Scored 92% in Financial Accounting & Tally Examination.",
                        category = "EXAM_PERFECTION",
                        awardedDate = "2026-10-15",
                        iconName = "WorkspacePremium"
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
