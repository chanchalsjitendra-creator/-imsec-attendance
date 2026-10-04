package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.data.model.ImsecCampusConfig
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LocationState(
    val latitude: Double = ImsecCampusConfig.IMSEC_LATITUDE,
    val longitude: Double = ImsecCampusConfig.IMSEC_LONGITUDE,
    val accuracyMeters: Float = 6.5f,
    val altitudeMeters: Double = 214.0,
    val distanceToCampusMeters: Float = 18.0f,
    val isWithinCampus: Boolean = true,
    val campusZone: String = "Academic Block A (LT-2)",
    val hasPermission: Boolean = false,
    val isGpsEnabled: Boolean = true,
    val isSimulationMode: Boolean = true, // Default to true so Somya can immediately verify without physical presence blocking
    val lastUpdateTime: Long = System.currentTimeMillis()
)

class LocationHelper(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    init {
        checkPermissions()
        refreshLocation()
    }

    fun checkPermissions(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val granted = fineGranted || coarseGranted
        val gpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

        _locationState.update { it.copy(hasPermission = granted, isGpsEnabled = gpsEnabled) }
        return granted
    }

    fun setSimulationMode(enabled: Boolean) {
        _locationState.update {
            if (enabled) {
                // Preset to IMSEC Block A
                val distance = ImsecCampusConfig.calculateDistanceMeters(
                    28.675850,
                    77.499710,
                    ImsecCampusConfig.IMSEC_LATITUDE,
                    ImsecCampusConfig.IMSEC_LONGITUDE
                )
                it.copy(
                    isSimulationMode = true,
                    latitude = 28.675850,
                    longitude = 77.499710,
                    accuracyMeters = 4.2f,
                    distanceToCampusMeters = distance,
                    isWithinCampus = distance <= ImsecCampusConfig.DEFAULT_GEOFENCE_RADIUS_METERS,
                    campusZone = "Academic Block A (CS Dept & LT-2)",
                    lastUpdateTime = System.currentTimeMillis()
                )
            } else {
                it.copy(isSimulationMode = false)
            }
        }
        if (!enabled) {
            refreshLocation()
        }
    }

    fun setCustomSimulationLocation(zoneName: String, lat: Double, lon: Double) {
        val distance = ImsecCampusConfig.calculateDistanceMeters(
            lat,
            lon,
            ImsecCampusConfig.IMSEC_LATITUDE,
            ImsecCampusConfig.IMSEC_LONGITUDE
        )
        _locationState.update {
            it.copy(
                isSimulationMode = true,
                latitude = lat,
                longitude = lon,
                accuracyMeters = 3.8f,
                distanceToCampusMeters = distance,
                isWithinCampus = distance <= ImsecCampusConfig.DEFAULT_GEOFENCE_RADIUS_METERS,
                campusZone = zoneName,
                lastUpdateTime = System.currentTimeMillis()
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshLocation() {
        val hasPerm = checkPermissions()
        if (_locationState.value.isSimulationMode) {
            // Keep simulation data
            return
        }

        if (!hasPerm) return

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        applyLocation(loc)
                    } else {
                        // Fallback to locationManager
                        val lastKnown = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (lastKnown != null) {
                            applyLocation(lastKnown)
                        }
                    }
                }
                .addOnFailureListener {
                    // Try standard locationManager
                    val lastKnown = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (lastKnown != null) {
                        applyLocation(lastKnown)
                    }
                }
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    private fun applyLocation(loc: Location) {
        val distance = ImsecCampusConfig.calculateDistanceMeters(
            loc.latitude,
            loc.longitude,
            ImsecCampusConfig.IMSEC_LATITUDE,
            ImsecCampusConfig.IMSEC_LONGITUDE
        )
        val isInside = distance <= ImsecCampusConfig.DEFAULT_GEOFENCE_RADIUS_METERS
        
        // Find closest campus zone or label
        val closestZone = ImsecCampusConfig.CAMPUS_ZONES.minByOrNull { zone ->
            ImsecCampusConfig.calculateDistanceMeters(loc.latitude, loc.longitude, zone.latitude, zone.longitude)
        }
        val zoneLabel = if (isInside) {
            closestZone?.name ?: "IMSEC Campus Core"
        } else {
            "Outside IMSEC Campus (${"%.0f".format(distance)}m away)"
        }

        _locationState.update {
            it.copy(
                latitude = loc.latitude,
                longitude = loc.longitude,
                accuracyMeters = if (loc.hasAccuracy()) loc.accuracy else 8f,
                altitudeMeters = if (loc.hasAltitude()) loc.altitude else 214.0,
                distanceToCampusMeters = distance,
                isWithinCampus = isInside,
                campusZone = zoneLabel,
                lastUpdateTime = System.currentTimeMillis()
            )
        }
    }
}
