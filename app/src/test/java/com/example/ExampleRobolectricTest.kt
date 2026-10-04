package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ImsecCampusConfig
import com.example.data.repository.AttendanceRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("IMSEC Attendance", appName)
    }

    @Test
    fun `verify student profile and subjects`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = AttendanceRepository(context)

        val profile = repository.studentProfile.value
        assertEquals("Somya Sharma", profile.name)
        assertEquals("2401430120179", profile.rollNo)
        assertEquals("2024-2028", profile.batchYear)

        // Test editing academic year from 2021-2025 to 2024-2028
        val updatedProfile = profile.copy(batchYear = "2024-2028")
        repository.updateStudentProfile(updatedProfile)
        assertEquals("2024-2028", repository.studentProfile.value.batchYear)

        val subjects = repository.subjects.value
        val codes = subjects.map { it.code }
        assertTrue(codes.contains("WT"))
        assertTrue(codes.contains("DA"))
        assertTrue(codes.contains("DAA"))
        assertTrue(codes.contains("MLT"))
        assertTrue(codes.contains("DBMS"))
        assertTrue(codes.contains("COI"))
    }

    @Test
    fun `verify imsec coordinates and geofence`() {
        val distance = ImsecCampusConfig.calculateDistanceMeters(
            28.675850,
            77.499710,
            ImsecCampusConfig.IMSEC_LATITUDE,
            ImsecCampusConfig.IMSEC_LONGITUDE
        )
        assertTrue(distance < ImsecCampusConfig.DEFAULT_GEOFENCE_RADIUS_METERS)
    }

    @Test
    fun `verify teacher can add class coordinates`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = AttendanceRepository(context)

        // Teacher updates coordinates for DAA
        repository.updateClassCoordinates(
            subjectCode = "DAA",
            latitude = 28.675750,
            longitude = 77.499860,
            radiusMeters = 50f,
            roomName = "LT-2 (Lecture Theatre 2)",
            zoneName = "Academic Block A"
        )

        val updatedSub = repository.subjects.value.first { it.code == "DAA" }
        assertEquals(28.675750, updatedSub.classroomLatitude, 0.0001)
        assertEquals(77.499860, updatedSub.classroomLongitude, 0.0001)
        assertEquals(50f, updatedSub.geofenceRadiusMeters, 0.01f)
        assertEquals("LT-2 (Lecture Theatre 2)", updatedSub.room)

        // Verify distance calculation to teacher's class
        val distance = updatedSub.distanceToClassMeters(28.675750, 77.499860)
        assertTrue(distance < 5f)
        assertTrue(updatedSub.isStudentInsideClass(28.675750, 77.499860))
    }
}
