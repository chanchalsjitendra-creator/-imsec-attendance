package com.example.biometrics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.security.MessageDigest

object BiometricHelper {

    fun generateVerificationHash(
        studentName: String,
        rollNo: String,
        subjectCode: String,
        timestamp: Long,
        latitude: Double,
        longitude: Double
    ): String {
        val payload = "$studentName|$rollNo|$subjectCode|$timestamp|$latitude|$longitude|IMSEC_SECURE_TOKEN"
        val bytes = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }.take(16).uppercase()
    }

    fun triggerHapticFeedback(context: Context, isSuccess: Boolean = true) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let { v ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (isSuccess) {
                        // Double pulse for success
                        val timings = longArrayOf(0, 70, 60, 100)
                        val amplitudes = intArrayOf(0, 180, 0, 255)
                        v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    } else {
                        // Long buzz for error/warning
                        v.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(if (isSuccess) 150L else 300L)
                }
            }
        } catch (_: Exception) {
            // Gracefully ignore if vibrator is unavailable or permission denied
        }
    }
}
