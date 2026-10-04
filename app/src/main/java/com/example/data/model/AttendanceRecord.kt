package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AttendanceRecord(
    val id: String,
    val studentName: String = "Somya Sharma",
    val rollNo: String = "2101430100089",
    val subjectCode: String,
    val subjectTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val distanceToCampusMeters: Float,
    val isInsideCampus: Boolean,
    val campusZone: String = "IMSEC Campus Core",
    val biometricType: String = "Biometric (Fingerprint)",
    val biometricHash: String,
    val status: String = "Present",
    val humanizedNote: String = "Marked present with biometric & GPS verification"
) {
    val formattedTime: String
        get() = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))

    val formattedDate: String
        get() = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(timestamp))

    val coordinatesDisplay: String
        get() = "${"%.5f".format(latitude)}° N, ${"%.5f".format(longitude)}° E"
}
