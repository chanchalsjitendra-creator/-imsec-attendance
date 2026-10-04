package com.example.data.model

data class Subject(
    val id: String,
    val code: String,
    val title: String,
    val professor: String,
    val room: String,
    val credits: Int,
    val totalClasses: Int,
    val attendedClasses: Int,
    val colorHex: Long,
    val scheduledTimeToday: String? = null,
    val isLectureToday: Boolean = true,
    val classroomLatitude: Double = ImsecCampusConfig.IMSEC_LATITUDE,
    val classroomLongitude: Double = ImsecCampusConfig.IMSEC_LONGITUDE,
    val geofenceRadiusMeters: Float = 100f,
    val classroomZone: String = "IMSEC Campus"
) {
    val percentage: Float
        get() = if (totalClasses == 0) 100f else (attendedClasses.toFloat() / totalClasses.toFloat()) * 100f

    val isSafe: Boolean
        get() = percentage >= 75f

    val isWarning: Boolean
        get() = percentage in 65f..74.9f

    val isCritical: Boolean
        get() = percentage < 65f

    val formattedCoordinates: String
        get() = "${"%.4f".format(classroomLatitude)}° N, ${"%.4f".format(classroomLongitude)}° E"

    /**
     * Calculates distance in meters from given student coordinates to this classroom
     */
    fun distanceToClassMeters(studentLat: Double, studentLon: Double): Float {
        return ImsecCampusConfig.calculateDistanceMeters(
            studentLat,
            studentLon,
            classroomLatitude,
            classroomLongitude
        )
    }

    /**
     * Checks if given student coordinates fall within teacher's defined classroom geofence
     */
    fun isStudentInsideClass(studentLat: Double, studentLon: Double): Boolean {
        return distanceToClassMeters(studentLat, studentLon) <= geofenceRadiusMeters
    }

    /**
     * Calculates humanized attendance advice for 75% target
     */
    val humanizedAdvice: String
        get() {
            val target = 0.75f
            val currentP = percentage
            return when {
                currentP >= 75f -> {
                    val canBunk = ((attendedClasses / target) - totalClasses).toInt().coerceAtLeast(0)
                    if (canBunk == 0) {
                        "On track at ${"%.1f".format(currentP)}%. Attend the next class to build a safety buffer!"
                    } else {
                        "Awesome buffer! You can safely take $canBunk ${if (canBunk == 1) "class" else "classes"} off without dropping below 75%."
                    }
                }
                else -> {
                    val needed = kotlin.math.ceil((target * totalClasses - attendedClasses) / (1 - target)).toInt().coerceAtLeast(1)
                    "Need to attend the next $needed ${if (needed == 1) "class" else "classes"} to get back into the 75% safe zone. You've got this, Somya!"
                }
            }
        }
}
