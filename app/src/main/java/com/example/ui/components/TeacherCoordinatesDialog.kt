package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Room
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ImsecCampusConfig
import com.example.data.model.Subject
import com.example.location.LocationState
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ImsecBlue
import com.example.ui.theme.ImsecLightBlue
import com.example.ui.theme.ImsecNavy

@Composable
fun TeacherCoordinatesDialog(
    subject: Subject,
    currentLocationState: LocationState,
    onDismiss: () -> Unit,
    onSave: (latitude: Double, longitude: Double, radiusMeters: Float, room: String, zone: String) -> Unit
) {
    var latText by remember { mutableStateOf(subject.classroomLatitude.toString()) }
    var lonText by remember { mutableStateOf(subject.classroomLongitude.toString()) }
    var radiusMeters by remember { mutableFloatStateOf(subject.geofenceRadiusMeters) }
    var roomName by remember { mutableStateOf(subject.room) }
    var zoneName by remember { mutableStateOf(subject.classroomZone) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var gpsCapturedBanner by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("teacher_coordinates_dialog"),
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = ImsecNavy,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Set Class Coordinates",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "${subject.code}: ${subject.title}",
                            fontSize = 12.sp,
                            color = ImsecBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Faculty: ${subject.professor} • Configure precise classroom coordinates for student verification",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                if (gpsCapturedBanner) {
                    Surface(
                        color = EmeraldSoft,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Current live location captured successfully!",
                                color = EmeraldSuccess,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Room / Lecture Hall
                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = { Text("Classroom / Hall Name *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.MeetingRoom, contentDescription = null, tint = ImsecNavy)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Campus Zone
                OutlinedTextField(
                    value = zoneName,
                    onValueChange = { zoneName = it },
                    label = { Text("IMSEC Campus Zone / Wing") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Business, contentDescription = null, tint = ImsecNavy)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Latitude
                OutlinedTextField(
                    value = latText,
                    onValueChange = {
                        latText = it
                        errorMessage = null
                        gpsCapturedBanner = false
                    },
                    label = { Text("Class Latitude (North) *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, tint = ImsecNavy)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("class_latitude_input")
                )

                // Longitude
                OutlinedTextField(
                    value = lonText,
                    onValueChange = {
                        lonText = it
                        errorMessage = null
                        gpsCapturedBanner = false
                    },
                    label = { Text("Class Longitude (East) *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, tint = ImsecNavy)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("class_longitude_input")
                )

                // Quick One-Tap: Capture Teacher's Current Live Location
                Button(
                    onClick = {
                        latText = currentLocationState.latitude.toString()
                        lonText = currentLocationState.longitude.toString()
                        gpsCapturedBanner = true
                        errorMessage = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ImsecLightBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("capture_live_coords_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = ImsecNavy,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Capture My Current Location (${"%.4f".format(currentLocationState.latitude)}°, ${"%.4f".format(currentLocationState.longitude)}°)",
                        color = ImsecNavy,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // IMSEC Campus Presets
                Column {
                    Text(
                        text = "IMSEC CAMPUS PRESET COORDINATES:",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val presets = listOf(
                        Triple("Block A (CS)", 28.675713, 77.499824),
                        Triple("CS Lab 2", 28.675920, 77.499610),
                        Triple("Block B (AI)", 28.675430, 77.500120),
                        Triple("Library / Audi", 28.675210, 77.499450)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { (name, lat, lon) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        latText = lat.toString()
                                        lonText = lon.toString()
                                        errorMessage = null
                                    }
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ImsecNavy,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // Classroom Geofence Radius Slider
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = null,
                                tint = ImsecNavy,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Classroom Geofence Radius",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "±${radiusMeters.toInt()} meters",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImsecNavy
                        )
                    }

                    Slider(
                        value = radiusMeters,
                        onValueChange = { radiusMeters = it },
                        valueRange = 25f..200f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = ImsecNavy,
                            activeTrackColor = ImsecNavy
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Students checking in for ${subject.code} must be physically within ${radiusMeters.toInt()}m of this room.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latText.toDoubleOrNull()
                    val lon = lonText.toDoubleOrNull()
                    if (lat == null || lat !in -90.0..90.0) {
                        errorMessage = "Please enter a valid decimal latitude between -90 and +90"
                        return@Button
                    }
                    if (lon == null || lon !in -180.0..180.0) {
                        errorMessage = "Please enter a valid decimal longitude between -180 and +180"
                        return@Button
                    }
                    if (roomName.isBlank()) {
                        errorMessage = "Please specify a room or lecture hall name"
                        return@Button
                    }
                    onSave(lat, lon, radiusMeters, roomName.trim(), zoneName.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = ImsecNavy),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_class_coordinates_button")
            ) {
                Text("Save Coordinates", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
