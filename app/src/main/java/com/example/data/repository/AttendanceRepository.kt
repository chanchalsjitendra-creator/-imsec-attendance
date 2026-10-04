package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AttendanceRecord
import com.example.data.model.StudentProfile
import com.example.data.model.Subject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import java.util.UUID

class AttendanceRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("imsec_attendance_somya_prefs", Context.MODE_PRIVATE)

    private val initialSubjects = listOf(
        Subject(
            id = "wt_sub",
            code = "WT",
            title = "Web Technology",
            professor = "Prof. Neha Goel",
            room = "CS-101 (Block A)",
            credits = 4,
            totalClasses = 24,
            attendedClasses = 21,
            colorHex = 0xFF2563EB, // Blue
            scheduledTimeToday = "09:30 AM - 10:25 AM",
            classroomLatitude = 28.675713,
            classroomLongitude = 77.499824,
            geofenceRadiusMeters = 80f,
            classroomZone = "Academic Block A (CS-101)"
        ),
        Subject(
            id = "da_sub",
            code = "DA",
            title = "Data Analytics",
            professor = "Dr. Rajesh Kumar",
            room = "CS Lab 2 (Computing Wing)",
            credits = 4,
            totalClasses = 22,
            attendedClasses = 18,
            colorHex = 0xFF059669, // Emerald
            scheduledTimeToday = "10:30 AM - 11:25 AM",
            classroomLatitude = 28.675920,
            classroomLongitude = 77.499610,
            geofenceRadiusMeters = 65f,
            classroomZone = "Computing Lab Center (Lab 2)"
        ),
        Subject(
            id = "daa_sub",
            code = "DAA",
            title = "Design Analysis and Algorithm",
            professor = "Prof. Amit Verma",
            room = "LT-2 (Lecture Theatre 2)",
            credits = 4,
            totalClasses = 26,
            attendedClasses = 22,
            colorHex = 0xFF7C3AED, // Violet
            scheduledTimeToday = "11:30 AM - 12:25 PM",
            classroomLatitude = 28.675750,
            classroomLongitude = 77.499860,
            geofenceRadiusMeters = 75f,
            classroomZone = "Lecture Theatre LT-2"
        ),
        Subject(
            id = "mlt_sub",
            code = "MLT",
            title = "Machine Learning and Techniques",
            professor = "Dr. Shruti Bansal",
            room = "AI & Data Science Lab (Block B)",
            credits = 4,
            totalClasses = 20,
            attendedClasses = 17,
            colorHex = 0xFFD97706, // Amber
            scheduledTimeToday = "01:15 PM - 02:10 PM",
            classroomLatitude = 28.675430,
            classroomLongitude = 77.500120,
            geofenceRadiusMeters = 60f,
            classroomZone = "Block B - AI & DS Lab"
        ),
        Subject(
            id = "dbms_sub",
            code = "DBMS",
            title = "Database Management System",
            professor = "Prof. Pooja Sharma",
            room = "CS-104 (Block A)",
            credits = 4,
            totalClasses = 25,
            attendedClasses = 23,
            colorHex = 0xFF0284C7, // Sky
            scheduledTimeToday = "02:15 PM - 03:10 PM",
            classroomLatitude = 28.675680,
            classroomLongitude = 77.499800,
            geofenceRadiusMeters = 80f,
            classroomZone = "Academic Block A (CS-104)"
        ),
        Subject(
            id = "coi_sub",
            code = "COI",
            title = "Constitution of India",
            professor = "Dr. Manoj Tripathi",
            room = "Audi-1 (Central Block)",
            credits = 2,
            totalClasses = 16,
            attendedClasses = 11, // 68.8% - provides natural humanized motivation to reach 75%
            colorHex = 0xFFE11D48, // Crimson
            scheduledTimeToday = "03:15 PM - 04:10 PM",
            classroomLatitude = 28.675210,
            classroomLongitude = 77.499450,
            geofenceRadiusMeters = 100f,
            classroomZone = "Central Auditorium (Audi-1)"
        )
    )

    private val initialRecords = listOf(
        AttendanceRecord(
            id = "rec_01",
            subjectCode = "WT",
            subjectTitle = "Web Technology",
            timestamp = System.currentTimeMillis() - 86400000L * 2 + 3600000L * 2,
            latitude = 28.675850,
            longitude = 77.499710,
            distanceToCampusMeters = 16.5f,
            isInsideCampus = true,
            campusZone = "Academic Block A - CS-101",
            biometricType = "Biometric (Fingerprint)",
            biometricHash = "9F8A2B1C4D7E5F6A",
            humanizedNote = "Present for Web Tech with Prof. Neha. Active participation verified."
        ),
        AttendanceRecord(
            id = "rec_02",
            subjectCode = "DAA",
            subjectTitle = "Design Analysis and Algorithm",
            timestamp = System.currentTimeMillis() - 86400000L * 1 + 3600000L * 4,
            latitude = 28.675713,
            longitude = 77.499824,
            distanceToCampusMeters = 8.2f,
            isInsideCampus = true,
            campusZone = "Lecture Theatre LT-2",
            biometricType = "Biometric (Face ID)",
            biometricHash = "3E7D9A1C5B8F2044",
            humanizedNote = "Present for DAA lecture. Dynamic Programming algorithms covered."
        ),
        AttendanceRecord(
            id = "rec_03",
            subjectCode = "DBMS",
            subjectTitle = "Database Management System",
            timestamp = System.currentTimeMillis() - 86400000L * 1 + 3600000L * 6,
            latitude = 28.675620,
            longitude = 77.499980,
            distanceToCampusMeters = 22.4f,
            isInsideCampus = true,
            campusZone = "CS Lab 2 - DBMS Terminal",
            biometricType = "Biometric (Fingerprint)",
            biometricHash = "A4B7C9D2E1F83011",
            humanizedNote = "SQL Indexing and Query Optimization practical verified."
        )
    )

    private val _subjects = MutableStateFlow(loadSubjects())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private val _attendanceHistory = MutableStateFlow(loadHistory())
    val attendanceHistory: StateFlow<List<AttendanceRecord>> = _attendanceHistory.asStateFlow()

    private val _studentProfile = MutableStateFlow(loadProfile())
    val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

    private val _isIosThemePreview = MutableStateFlow(false)
    val isIosThemePreview: StateFlow<Boolean> = _isIosThemePreview.asStateFlow()

    fun updateStudentProfile(newProfile: StudentProfile) {
        _studentProfile.value = newProfile
        saveProfile(newProfile)
    }

    fun resetStudentProfile() {
        val defaultProfile = StudentProfile()
        _studentProfile.value = defaultProfile
        saveProfile(defaultProfile)
    }

    private fun loadProfile(): StudentProfile {
        val name = prefs.getString("student_name", "Somya Sharma") ?: "Somya Sharma"
        val rollNo = prefs.getString("student_roll", "2401430120179") ?: "2401430120179"
        val college = prefs.getString("student_college", "IMS Engineering College (IMSEC)") ?: "IMS Engineering College (IMSEC)"
        val university = prefs.getString("student_univ", "AKTU Lucknow") ?: "AKTU Lucknow"
        val branch = prefs.getString("student_branch", "Computer Science & Engineering (CSE)") ?: "Computer Science & Engineering (CSE)"
        val semester = prefs.getString("student_sem", "6th Semester, Section B") ?: "6th Semester, Section B"
        val batchYear = prefs.getString("student_batch_year", "2024-2028") ?: "2024-2028"
        val email = prefs.getString("student_email", "somya.sharma21@imsec.ac.in") ?: "somya.sharma21@imsec.ac.in"
        val phone = prefs.getString("student_phone", "+91 98712 34567") ?: "+91 98712 34567"
        val target = prefs.getFloat("student_target", 75.0f)
        val streak = prefs.getInt("student_streak", 7)
        val mentor = prefs.getString("student_mentor", "Dr. Amit Verma (Associate Professor, CSE)") ?: "Dr. Amit Verma (Associate Professor, CSE)"
        val blood = prefs.getString("student_blood", "B+") ?: "B+"

        return StudentProfile(
            name = name,
            rollNo = rollNo,
            college = college,
            university = university,
            branch = branch,
            semester = semester,
            batchYear = batchYear,
            email = email,
            phone = phone,
            targetPercentage = target,
            streakDays = streak,
            mentorName = mentor,
            bloodGroup = blood
        )
    }

    private fun saveProfile(profile: StudentProfile) {
        prefs.edit()
            .putString("student_name", profile.name)
            .putString("student_roll", profile.rollNo)
            .putString("student_college", profile.college)
            .putString("student_univ", profile.university)
            .putString("student_branch", profile.branch)
            .putString("student_sem", profile.semester)
            .putString("student_batch_year", profile.batchYear)
            .putString("student_email", profile.email)
            .putString("student_phone", profile.phone)
            .putFloat("student_target", profile.targetPercentage)
            .putInt("student_streak", profile.streakDays)
            .putString("student_mentor", profile.mentorName)
            .putString("student_blood", profile.bloodGroup)
            .apply()
    }

    fun toggleIosThemePreview() {
        _isIosThemePreview.update { !it }
    }

    /**
     * Marks student present with Biometric & GPS verification
     */
    fun recordAttendance(
        subjectCode: String,
        latitude: Double,
        longitude: Double,
        distanceMeters: Float,
        isInsideCampus: Boolean,
        zoneName: String,
        biometricType: String,
        biometricHash: String
    ): AttendanceRecord {
        val subject = _subjects.value.find { it.code.equals(subjectCode, ignoreCase = true) }
            ?: _subjects.value.first()

        val distanceToClass = subject.distanceToClassMeters(latitude, longitude)
        val isInsideClassroom = subject.isStudentInsideClass(latitude, longitude)

        val humanizedNote = when {
            isInsideClassroom -> "Verified at ${subject.room}! (${"%.0f".format(distanceToClass)}m from teacher coordinates set by ${subject.professor}). Have a wonderful lecture, Somya! ✨"
            isInsideCampus -> "Within IMSEC campus (${"%.0f".format(distanceMeters)}m), recorded for ${subject.title} in ${subject.room} (teacher geofence: ${subject.geofenceRadiusMeters.toInt()}m). Verified."
            else -> "Attendance logged with location flag (${"%.0f".format(distanceMeters)}m from campus, ${"%.0f".format(distanceToClass)}m from classroom). Flagged for mentor review."
        }

        val record = AttendanceRecord(
            id = UUID.randomUUID().toString(),
            studentName = _studentProfile.value.name,
            rollNo = _studentProfile.value.rollNo,
            subjectCode = subject.code,
            subjectTitle = subject.title,
            timestamp = System.currentTimeMillis(),
            latitude = latitude,
            longitude = longitude,
            distanceToCampusMeters = distanceMeters,
            isInsideCampus = isInsideCampus,
            campusZone = zoneName,
            biometricType = biometricType,
            biometricHash = biometricHash,
            status = if (isInsideCampus) "Present" else "Conditional",
            humanizedNote = humanizedNote
        )

        // Increment attended and total classes
        _subjects.update { currentList ->
            currentList.map { sub ->
                if (sub.code.equals(subject.code, ignoreCase = true)) {
                    sub.copy(
                        attendedClasses = sub.attendedClasses + 1,
                        totalClasses = sub.totalClasses + 1
                    )
                } else sub
            }
        }

        // Add to history
        _attendanceHistory.update { listOf(record) + it }
        saveSubjects(_subjects.value)
        return record
    }

    /**
     * Marks an excused leave / absence
     */
    fun recordLeave(subjectCode: String, reason: String) {
        val subject = _subjects.value.find { it.code.equals(subjectCode, ignoreCase = true) } ?: return
        val record = AttendanceRecord(
            id = UUID.randomUUID().toString(),
            studentName = _studentProfile.value.name,
            rollNo = _studentProfile.value.rollNo,
            subjectCode = subject.code,
            subjectTitle = subject.title,
            timestamp = System.currentTimeMillis(),
            latitude = 28.675713,
            longitude = 77.499824,
            distanceToCampusMeters = 0f,
            isInsideCampus = true,
            campusZone = "Official Leave Approval",
            biometricType = "Mentor Exemption Slip",
            biometricHash = "LEAVE-${System.currentTimeMillis().toString().takeLast(6)}",
            status = "Excused",
            humanizedNote = "Medical/Official Leave note recorded: $reason"
        )
        // For excused, total increases or stays adjusted per college rule
        _attendanceHistory.update { listOf(record) + it }
    }

    fun getOverallPercentage(): Float {
        val list = _subjects.value
        val totalAttended = list.sumOf { it.attendedClasses }
        val totalConducted = list.sumOf { it.totalClasses }
        return if (totalConducted == 0) 100f else (totalAttended.toFloat() / totalConducted.toFloat()) * 100f
    }

    fun getNextClassToday(): Subject? {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val list = _subjects.value
        return when {
            hour < 10 -> list.getOrNull(0) // WT
            hour < 11 -> list.getOrNull(1) // DA
            hour < 12 -> list.getOrNull(2) // DAA
            hour < 14 -> list.getOrNull(3) // MLT
            hour < 15 -> list.getOrNull(4) // DBMS
            hour < 17 -> list.getOrNull(5) // COI
            else -> list.firstOrNull()
        }
    }

    fun getHumanizedGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning, Somya! ☀️"
            in 12..16 -> "Good afternoon, Somya! 🌿"
            in 17..21 -> "Good evening, Somya! 🌙"
            else -> "Late night study session, Somya! ☕"
        }
    }

    fun generateReportText(): String {
        val overall = getOverallPercentage()
        val sb = StringBuilder()
        sb.appendLine("=========================================")
        sb.appendLine("IMS ENGINEERING COLLEGE (IMSEC) - GHAZIABAD")
        sb.appendLine("BIOMETRIC & GPS ATTENDANCE VERIFICATION REPORT")
        sb.appendLine("=========================================")
        sb.appendLine("Student Name: ${_studentProfile.value.name}")
        sb.appendLine("University Roll No: ${_studentProfile.value.rollNo}")
        sb.appendLine("Branch & Sem: ${_studentProfile.value.branch} (${_studentProfile.value.semester})")
        sb.appendLine("Academic Batch: ${_studentProfile.value.batchYear}")
        sb.appendLine("Overall Attendance: ${"%.1f".format(overall)}% (Target: 75%)")
        sb.appendLine("Eligibility Status: ${if (overall >= 75f) "ELIGIBLE FOR SEMESTER EXAMS ✅" else "CRITERIA SHORTAGE ⚠️"}")
        sb.appendLine("College Geolocation: 28.675713° N, 77.499824° E (NH-24, Ghaziabad)")
        sb.appendLine("-----------------------------------------")
        sb.appendLine("SUBJECT-WISE ATTENDANCE BREAKDOWN:")
        _subjects.value.forEach { sub ->
            val status = if (sub.isSafe) "SAFE" else "CRITICAL"
            sb.appendLine("• ${sub.code} - ${sub.title}: ${sub.attendedClasses}/${sub.totalClasses} (${"%.1f".format(sub.percentage)}%) [$status]")
            sb.appendLine("  Faculty: ${sub.professor} | ${sub.room}")
        }
        sb.appendLine("-----------------------------------------")
        sb.appendLine("RECENT VERIFICATION LOGS (${_attendanceHistory.value.size} records):")
        _attendanceHistory.value.take(5).forEach { rec ->
            sb.appendLine("[${rec.formattedDate} ${rec.formattedTime}] ${rec.subjectCode} - ${rec.status}")
            sb.appendLine("  GPS: ${rec.coordinatesDisplay} (${rec.campusZone})")
            sb.appendLine("  Biometric Auth Hash: ${rec.biometricHash} (${rec.biometricType})")
        }
        sb.appendLine("=========================================")
        return sb.toString()
    }

    fun updateClassCoordinates(
        subjectCode: String,
        latitude: Double,
        longitude: Double,
        radiusMeters: Float,
        roomName: String = "",
        zoneName: String = ""
    ) {
        _subjects.update { currentList ->
            currentList.map { sub ->
                if (sub.code.equals(subjectCode, ignoreCase = true)) {
                    val updated = sub.copy(
                        classroomLatitude = latitude,
                        classroomLongitude = longitude,
                        geofenceRadiusMeters = radiusMeters,
                        room = if (roomName.isNotBlank()) roomName else sub.room,
                        classroomZone = if (zoneName.isNotBlank()) zoneName else sub.classroomZone
                    )
                    saveSubjectCoordinates(updated)
                    updated
                } else sub
            }
        }
    }

    private fun loadSubjects(): List<Subject> {
        return initialSubjects.map { sub ->
            if (prefs.contains("sub_lat_${sub.code}")) {
                val lat = prefs.getFloat("sub_lat_${sub.code}", sub.classroomLatitude.toFloat()).toDouble()
                val lon = prefs.getFloat("sub_lon_${sub.code}", sub.classroomLongitude.toFloat()).toDouble()
                val rad = prefs.getFloat("sub_rad_${sub.code}", sub.geofenceRadiusMeters)
                val room = prefs.getString("sub_room_${sub.code}", sub.room) ?: sub.room
                val zone = prefs.getString("sub_zone_${sub.code}", sub.classroomZone) ?: sub.classroomZone
                sub.copy(
                    classroomLatitude = lat,
                    classroomLongitude = lon,
                    geofenceRadiusMeters = rad,
                    room = room,
                    classroomZone = zone
                )
            } else {
                sub
            }
        }
    }

    private fun saveSubjectCoordinates(sub: Subject) {
        prefs.edit()
            .putFloat("sub_lat_${sub.code}", sub.classroomLatitude.toFloat())
            .putFloat("sub_lon_${sub.code}", sub.classroomLongitude.toFloat())
            .putFloat("sub_rad_${sub.code}", sub.geofenceRadiusMeters)
            .putString("sub_room_${sub.code}", sub.room)
            .putString("sub_zone_${sub.code}", sub.classroomZone)
            .apply()
    }

    private fun saveSubjects(list: List<Subject>) {
        list.forEach { saveSubjectCoordinates(it) }
    }

    private fun loadHistory(): List<AttendanceRecord> {
        return initialRecords
    }
}
