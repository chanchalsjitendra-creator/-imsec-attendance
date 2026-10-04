package com.example.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.biometrics.BiometricHelper
import com.example.data.model.AttendanceRecord
import com.example.data.model.ImsecCampusConfig
import com.example.data.model.StudentProfile
import com.example.data.model.Subject
import com.example.data.repository.AttendanceRepository
import com.example.location.LocationHelper
import com.example.location.LocationState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class BiometricScanState {
    object Idle : BiometricScanState()
    object Scanning : BiometricScanState()
    object VerifyingGeoAndBiometric : BiometricScanState()
    data class Success(val record: AttendanceRecord) : BiometricScanState()
    data class Error(val message: String) : BiometricScanState()
}

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    val repository = AttendanceRepository(application)
    val locationHelper = LocationHelper(application)

    val subjects: StateFlow<List<Subject>> = repository.subjects
    val attendanceHistory: StateFlow<List<AttendanceRecord>> = repository.attendanceHistory
    val studentProfile = repository.studentProfile
    val locationState: StateFlow<LocationState> = locationHelper.locationState
    val isIosThemePreview: StateFlow<Boolean> = repository.isIosThemePreview

    // Active subject selected for check-in
    private val _selectedSubject = MutableStateFlow<Subject?>(null)
    val selectedSubject: StateFlow<Subject?> = _selectedSubject.asStateFlow()

    // Biometric modal visible state
    private val _isBiometricSheetVisible = MutableStateFlow(false)
    val isBiometricSheetVisible: StateFlow<Boolean> = _isBiometricSheetVisible.asStateFlow()

    // Biometric scan state
    private val _biometricScanState = MutableStateFlow<BiometricScanState>(BiometricScanState.Idle)
    val biometricScanState: StateFlow<BiometricScanState> = _biometricScanState.asStateFlow()

    // Biometric type selected: "fingerprint" or "face"
    private val _biometricMode = MutableStateFlow("fingerprint") // "fingerprint" or "face"
    val biometricMode: StateFlow<String> = _biometricMode.asStateFlow()

    // Leave request dialog
    private val _isLeaveDialogVisible = MutableStateFlow(false)
    val isLeaveDialogVisible: StateFlow<Boolean> = _isLeaveDialogVisible.asStateFlow()

    // Edit profile dialog
    private val _isEditProfileDialogVisible = MutableStateFlow(false)
    val isEditProfileDialogVisible: StateFlow<Boolean> = _isEditProfileDialogVisible.asStateFlow()

    // Teacher classroom coordinates dialog
    private val _isTeacherCoordDialogVisible = MutableStateFlow(false)
    val isTeacherCoordDialogVisible: StateFlow<Boolean> = _isTeacherCoordDialogVisible.asStateFlow()

    private val _teacherSelectedSubject = MutableStateFlow<Subject?>(null)
    val teacherSelectedSubject: StateFlow<Subject?> = _teacherSelectedSubject.asStateFlow()

    // User-friendly banner message
    private val _snackBarMessage = MutableStateFlow<String?>(null)
    val snackBarMessage: StateFlow<String?> = _snackBarMessage.asStateFlow()

    init {
        // Automatically default selected subject to the next scheduled subject today
        _selectedSubject.value = repository.getNextClassToday()
    }

    fun selectSubject(subject: Subject) {
        _selectedSubject.value = subject
    }

    fun openCheckIn(subject: Subject? = null, mode: String = "fingerprint") {
        if (subject != null) {
            _selectedSubject.value = subject
        } else if (_selectedSubject.value == null) {
            _selectedSubject.value = subjects.value.firstOrNull()
        }
        _biometricMode.value = mode
        _biometricScanState.value = BiometricScanState.Idle
        _isBiometricSheetVisible.value = true
    }

    fun dismissBiometricSheet() {
        _isBiometricSheetVisible.value = false
        _biometricScanState.value = BiometricScanState.Idle
    }

    fun setBiometricMode(mode: String) {
        _biometricMode.value = mode
    }

    /**
     * Executes interactive biometric touch & verify sequence
     */
    fun startBiometricVerification() {
        val subject = _selectedSubject.value ?: subjects.value.first()
        val loc = locationState.value

        viewModelScope.launch {
            _biometricScanState.value = BiometricScanState.Scanning
            BiometricHelper.triggerHapticFeedback(getApplication(), isSuccess = true)

            // Simulate sensor read duration
            delay(1200)

            _biometricScanState.value = BiometricScanState.VerifyingGeoAndBiometric

            // Compute cryptographic verification hash
            val hash = BiometricHelper.generateVerificationHash(
                studentName = studentProfile.value.name,
                rollNo = studentProfile.value.rollNo,
                subjectCode = subject.code,
                timestamp = System.currentTimeMillis(),
                latitude = loc.latitude,
                longitude = loc.longitude
            )

            delay(900)

            val record = repository.recordAttendance(
                subjectCode = subject.code,
                latitude = loc.latitude,
                longitude = loc.longitude,
                distanceMeters = loc.distanceToCampusMeters,
                isInsideCampus = loc.isWithinCampus,
                zoneName = loc.campusZone,
                biometricType = if (_biometricMode.value == "face") "Biometric (Face ID)" else "Biometric (Fingerprint)",
                biometricHash = hash
            )

            BiometricHelper.triggerHapticFeedback(getApplication(), isSuccess = true)
            _biometricScanState.value = BiometricScanState.Success(record)
            _snackBarMessage.value = "Present marked for ${subject.code}! Have a great lecture, Somya! ✨"
        }
    }

    fun toggleLocationSimulation(enabled: Boolean) {
        locationHelper.setSimulationMode(enabled)
    }

    fun selectCampusPreset(zoneName: String, lat: Double, lon: Double) {
        locationHelper.setCustomSimulationLocation(zoneName, lat, lon)
    }

    fun refreshGps() {
        locationHelper.refreshLocation()
    }

    fun toggleIosThemePreview() {
        repository.toggleIosThemePreview()
    }

    fun showLeaveDialog(subject: Subject? = null) {
        if (subject != null) _selectedSubject.value = subject
        _isLeaveDialogVisible.value = true
    }

    fun dismissLeaveDialog() {
        _isLeaveDialogVisible.value = false
    }

    fun submitLeaveRequest(subjectCode: String, reason: String) {
        repository.recordLeave(subjectCode, reason)
        _isLeaveDialogVisible.value = false
        _snackBarMessage.value = "Leave note recorded & sent to faculty mentor for $subjectCode."
    }

    fun clearSnackBarMessage() {
        _snackBarMessage.value = null
    }

    fun openEditProfileDialog() {
        _isEditProfileDialogVisible.value = true
    }

    fun dismissEditProfileDialog() {
        _isEditProfileDialogVisible.value = false
    }

    fun updateStudentProfile(newProfile: StudentProfile) {
        repository.updateStudentProfile(newProfile)
        _isEditProfileDialogVisible.value = false
        _snackBarMessage.value = "Profile updated: ${newProfile.name} (${newProfile.rollNo}) ✨"
    }

    fun resetStudentProfile() {
        repository.resetStudentProfile()
        _isEditProfileDialogVisible.value = false
        _snackBarMessage.value = "Profile reset to Somya Sharma defaults."
    }

    fun openTeacherCoordinatesDialog(subject: Subject) {
        _teacherSelectedSubject.value = subject
        _isTeacherCoordDialogVisible.value = true
    }

    fun dismissTeacherCoordinatesDialog() {
        _isTeacherCoordDialogVisible.value = false
        _teacherSelectedSubject.value = null
    }

    fun saveClassCoordinates(
        subjectCode: String,
        latitude: Double,
        longitude: Double,
        radiusMeters: Float,
        roomName: String,
        zoneName: String
    ) {
        repository.updateClassCoordinates(
            subjectCode = subjectCode,
            latitude = latitude,
            longitude = longitude,
            radiusMeters = radiusMeters,
            roomName = roomName,
            zoneName = zoneName
        )
        _isTeacherCoordDialogVisible.value = false
        _teacherSelectedSubject.value = null
        _snackBarMessage.value = "Teacher coordinates set for $subjectCode: ${"%.4f".format(latitude)}° N, ${"%.4f".format(longitude)}° E (±${radiusMeters.toInt()}m) 📍"
    }

    fun shareAttendanceReport() {
        val report = repository.generateReportText()
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, report)
            putExtra(Intent.EXTRA_TITLE, "Somya Sharma - IMSEC Attendance Verification")
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Somya's Attendance Report").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(shareIntent)
    }
}
