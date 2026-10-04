package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ImsecCampusConfig
import com.example.location.LocationState
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ImsecBlue
import com.example.ui.theme.ImsecNavy
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CampusRadarView(
    locationState: LocationState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepAngle"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A) // Sleek dark radar console
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Radar Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (locationState.isWithinCampus) EmeraldSuccess else Color(0xFFEF4444))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "IMSEC LIVE CAMPUS RADAR",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    color = if (locationState.isWithinCampus) EmeraldSoft else Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (locationState.isWithinCampus) "INSIDE GEOFENCE ✅" else "OUTSIDE CAMPUS ⚠️",
                        color = if (locationState.isWithinCampus) EmeraldSuccess else Color(0xFFDC2626),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Radar Display
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF020617)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = size.width / 2f - 16.dp.toPx()

                    // Range Rings (100m, 250m, 500m geofence)
                    val r1 = maxRadius * 0.33f
                    val r2 = maxRadius * 0.66f
                    val r3 = maxRadius

                    drawCircle(
                        color = Color(0xFF1E293B),
                        radius = r1,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFF1E293B),
                        radius = r2,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFF22C55E).copy(alpha = 0.4f), // 500m Geofence boundary
                        radius = r3,
                        center = center,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                        )
                    )

                    // Crosshairs
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(center.x, 16.dp.toPx()),
                        end = Offset(center.x, size.height - 16.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(16.dp.toPx(), center.y),
                        end = Offset(size.width - 16.dp.toPx(), center.y),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Rotating Radar Sweep Beam
                    val rad = Math.toRadians(sweepAngle.toDouble())
                    val sweepEnd = Offset(
                        (center.x + maxRadius * cos(rad)).toFloat(),
                        (center.y + maxRadius * sin(rad)).toFloat()
                    )
                    drawLine(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFF38BDF8).copy(alpha = 0.05f)),
                            center = center,
                            radius = maxRadius
                        ),
                        start = center,
                        end = sweepEnd,
                        strokeWidth = 2.5.dp.toPx()
                    )

                    // Draw IMSEC Campus Center Flag
                    drawCircle(
                        color = Color(0xFF38BDF8),
                        radius = 5.dp.toPx(),
                        center = center
                    )

                    // Draw Academic Blocks pins around center
                    val blockOffsets = listOf(
                        Offset(center.x - 28.dp.toPx(), center.y - 20.dp.toPx()), // Block A
                        Offset(center.x + 32.dp.toPx(), center.y - 10.dp.toPx()), // Computing Labs
                        Offset(center.x - 10.dp.toPx(), center.y + 36.dp.toPx()), // Library
                        Offset(center.x + 20.dp.toPx(), center.y + 24.dp.toPx())  // Canteen
                    )
                    blockOffsets.forEach { pos ->
                        drawCircle(
                            color = Color(0xFF64748B),
                            radius = 3.5.dp.toPx(),
                            center = pos
                        )
                    }

                    // Draw Somya's Position
                    // Scale position based on actual distance
                    val distFraction = (locationState.distanceToCampusMeters / 500f).coerceIn(0.05f, 0.95f)
                    val studentRadius = maxRadius * distFraction
                    // Offset slightly for visual representation
                    val studentOffset = Offset(
                        center.x + studentRadius * 0.45f,
                        center.y - studentRadius * 0.85f
                    )

                    // Student pulsating outer wave
                    drawCircle(
                        color = if (locationState.isWithinCampus) Color(0xFF22C55E).copy(alpha = pulseAlpha * 0.4f) else Color(0xFFEF4444).copy(alpha = pulseAlpha * 0.4f),
                        radius = 12.dp.toPx(),
                        center = studentOffset
                    )
                    // Student center blip
                    drawCircle(
                        color = if (locationState.isWithinCampus) Color(0xFF22C55E) else Color(0xFFEF4444),
                        radius = 6.dp.toPx(),
                        center = studentOffset
                    )
                }

                // Center label
                Text(
                    text = "IMSEC",
                    color = Color(0xFF38BDF8),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 18.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Coordinates readout bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CAMPUS CENTER",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "28.6757° N, 77.4998° E",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "DISTANCE",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${"%.1f".format(locationState.distanceToCampusMeters)}m",
                        color = if (locationState.isWithinCampus) Color(0xFF22C55E) else Color(0xFFF87171),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ACCURACY",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "±${"%.1f".format(locationState.accuracyMeters)}m",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Location: ${locationState.campusZone}",
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
