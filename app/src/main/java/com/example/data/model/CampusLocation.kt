package com.example.data.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class CampusZone(
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val isDefaultCheckInZone: Boolean = false
)

object ImsecCampusConfig {
    const val COLLEGE_NAME = "IMS Engineering College (IMSEC)"
    const val COLLEGE_SHORT = "IMSEC"
    const val COLLEGE_ADDRESS = "NH-24, Adhyatmik Nagar, Ghaziabad, UP 201015"
    
    // Official IMSEC coordinates
    const val IMSEC_LATITUDE = 28.675713
    const val IMSEC_LONGITUDE = 77.499824
    
    // Geofence radius in meters (500m encompasses entire campus and academic blocks)
    const val DEFAULT_GEOFENCE_RADIUS_METERS = 500.0f

    val CAMPUS_ZONES = listOf(
        CampusZone(
            name = "Academic Block A (CS Dept & LT-2)",
            description = "Main CS classrooms, Dept Office & Lecture Theatres",
            latitude = 28.675850,
            longitude = 77.499710,
            isDefaultCheckInZone = true
        ),
        CampusZone(
            name = "Computing Lab Center (CS Lab 2 & 3)",
            description = "High-performance programming & analytics labs",
            latitude = 28.675620,
            longitude = 77.499980
        ),
        CampusZone(
            name = "Academic Block B (IT & Analytics Wing)",
            description = "Faculty rooms, tutorial halls & seminar room",
            latitude = 28.675980,
            longitude = 77.499450
        ),
        CampusZone(
            name = "Central Library & Audi-1",
            description = "Digital library, Constitution cell & reading halls",
            latitude = 28.675420,
            longitude = 77.499620
        ),
        CampusZone(
            name = "Campus Main Gate & Green Courtyard",
            description = "IMSEC Welcome Fountain & Entry check post",
            latitude = 28.675210,
            longitude = 77.499890
        )
    )

    /**
     * Computes distance in meters between two lat/lng coordinates using Haversine formula
     */
    fun calculateDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double = IMSEC_LATITUDE,
        lon2: Double = IMSEC_LONGITUDE
    ): Float {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c).toFloat()
    }
}
