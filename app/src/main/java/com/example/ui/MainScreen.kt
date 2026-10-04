package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BiometricVerificationSheet
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.LeaveApplicationDialog
import com.example.ui.components.TeacherCoordinatesDialog
import com.example.ui.screens.CampusScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ImsecBlue
import com.example.ui.theme.ImsecLightBlue
import com.example.ui.theme.ImsecNavy
import com.example.ui.theme.SoftAmber
import com.example.ui.theme.WarmAmber

enum class NavTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    TODAY("Today", Icons.Default.Today),
    SUBJECTS("Subjects", Icons.Default.MenuBook),
    CAMPUS("Campus Radar", Icons.Default.Explore),
    PROFILE("Somya's ID", Icons.Default.Badge)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: AttendanceViewModel,
    onRequestLocationPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(NavTab.TODAY) }

    val subjects by viewModel.subjects.collectAsState()
    val attendanceHistory by viewModel.attendanceHistory.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()
    val locationState by viewModel.locationState.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val isBiometricSheetVisible by viewModel.isBiometricSheetVisible.collectAsState()
    val biometricScanState by viewModel.biometricScanState.collectAsState()
    val biometricMode by viewModel.biometricMode.collectAsState()
    val isLeaveDialogVisible by viewModel.isLeaveDialogVisible.collectAsState()
    val isEditProfileDialogVisible by viewModel.isEditProfileDialogVisible.collectAsState()
    val isTeacherCoordDialogVisible by viewModel.isTeacherCoordDialogVisible.collectAsState()
    val teacherSelectedSubject by viewModel.teacherSelectedSubject.collectAsState()
    val isIosTheme by viewModel.isIosThemePreview.collectAsState()
    val snackBarMsg by viewModel.snackBarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackBarMsg) {
        snackBarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackBarMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "IMSEC Attendance",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = SoftAmber,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = WarmAmber,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${studentProfile.streakDays}d",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = WarmAmber
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${studentProfile.name} • ${studentProfile.semester}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                modifier = Modifier.clickable { viewModel.openEditProfileDialog() }
                            )
                        }

                        // Live Campus Status indicator
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (locationState.isWithinCampus) EmeraldSoft else Color(0xFFFEE2E2),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { currentTab = NavTab.CAMPUS }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (locationState.isWithinCampus) EmeraldSuccess else Color(0xFFDC2626))
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (locationState.isWithinCampus) "IMSEC Core" else "Outside",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (locationState.isWithinCampus) EmeraldSuccess else Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = if (isIosTheme) MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) else MaterialTheme.colorScheme.surface,
                tonalElevation = if (isIosTheme) 0.dp else 4.dp
            ) {
                NavTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ImsecNavy,
                            selectedTextColor = ImsecNavy,
                            indicatorColor = ImsecLightBlue
                        ),
                        modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentTab == NavTab.TODAY || currentTab == NavTab.SUBJECTS) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openCheckIn(selectedSubject) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Check In"
                        )
                    },
                    text = {
                        Text(
                            text = "Smart Check-In",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    },
                    containerColor = ImsecNavy,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("main_checkin_fab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavTab.TODAY -> {
                    TodayScreen(
                        subjects = subjects,
                        locationState = locationState,
                        studentName = studentProfile.name,
                        greeting = viewModel.repository.getHumanizedGreeting(),
                        streakDays = studentProfile.streakDays,
                        overallPercentage = viewModel.repository.getOverallPercentage(),
                        nextClass = viewModel.repository.getNextClassToday(),
                        onCheckInSubject = { subject ->
                            viewModel.openCheckIn(subject)
                        },
                        onNavigateToCampus = {
                            currentTab = NavTab.CAMPUS
                        },
                        onRefreshLocation = {
                            onRequestLocationPermissions()
                            viewModel.refreshGps()
                        }
                    )
                }

                NavTab.SUBJECTS -> {
                    SubjectsScreen(
                        subjects = subjects,
                        overallPercentage = viewModel.repository.getOverallPercentage(),
                        onCheckIn = { subject ->
                            viewModel.openCheckIn(subject)
                        },
                        onLeave = { subject ->
                            viewModel.showLeaveDialog(subject)
                        },
                        onOpenTeacherCoordinates = { subject ->
                            viewModel.openTeacherCoordinatesDialog(subject)
                        }
                    )
                }

                NavTab.CAMPUS -> {
                    CampusScreen(
                        locationState = locationState,
                        onRefreshLocation = {
                            onRequestLocationPermissions()
                            viewModel.refreshGps()
                        },
                        onToggleSimulation = { enabled ->
                            viewModel.toggleLocationSimulation(enabled)
                        },
                        onSelectZonePreset = { zoneName, lat, lon ->
                            viewModel.selectCampusPreset(zoneName, lat, lon)
                        }
                    )
                }

                NavTab.PROFILE -> {
                    ProfileScreen(
                        student = studentProfile,
                        overallPercentage = viewModel.repository.getOverallPercentage(),
                        history = attendanceHistory,
                        isIosTheme = isIosTheme,
                        onToggleIosTheme = { viewModel.toggleIosThemePreview() },
                        onShareReport = { viewModel.shareAttendanceReport() },
                        onOpenLeaveDialog = { viewModel.showLeaveDialog() },
                        onEditProfile = { viewModel.openEditProfileDialog() }
                    )
                }
            }
        }

        // Edit Profile Dialog
        if (isEditProfileDialogVisible) {
            EditProfileDialog(
                initialProfile = studentProfile,
                onDismiss = { viewModel.dismissEditProfileDialog() },
                onSave = { updated -> viewModel.updateStudentProfile(updated) },
                onReset = { viewModel.resetStudentProfile() }
            )
        }

        // Teacher Class Coordinates Dialog
        if (isTeacherCoordDialogVisible && teacherSelectedSubject != null) {
            TeacherCoordinatesDialog(
                subject = teacherSelectedSubject!!,
                currentLocationState = locationState,
                onDismiss = { viewModel.dismissTeacherCoordinatesDialog() },
                onSave = { lat, lon, radius, room, zone ->
                    viewModel.saveClassCoordinates(
                        subjectCode = teacherSelectedSubject!!.code,
                        latitude = lat,
                        longitude = lon,
                        radiusMeters = radius,
                        roomName = room,
                        zoneName = zone
                    )
                }
            )
        }

        // Biometric Verification Bottom Sheet
        if (isBiometricSheetVisible && selectedSubject != null) {
            BiometricVerificationSheet(
                subject = selectedSubject!!,
                locationState = locationState,
                studentName = studentProfile.name,
                rollNo = studentProfile.rollNo,
                scanState = biometricScanState,
                biometricMode = biometricMode,
                onModeChange = { viewModel.setBiometricMode(it) },
                onStartScan = { viewModel.startBiometricVerification() },
                onDismiss = { viewModel.dismissBiometricSheet() }
            )
        }

        // Leave Application Dialog
        if (isLeaveDialogVisible) {
            LeaveApplicationDialog(
                selectedSubject = selectedSubject,
                studentName = studentProfile.name,
                rollNo = studentProfile.rollNo,
                onDismiss = { viewModel.dismissLeaveDialog() },
                onSubmit = { subCode, reason ->
                    viewModel.submitLeaveRequest(subCode, reason)
                }
            )
        }
    }
}
