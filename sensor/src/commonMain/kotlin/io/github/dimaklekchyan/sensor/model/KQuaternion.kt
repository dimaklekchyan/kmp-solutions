package io.github.dimaklekchyan.sensor.model

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt

data class KQuaternion(
    val x: Double,
    val y: Double,
    val z: Double,
    val w: Double
) {
    internal fun toEulerAngles(): KAngles {
        val (x, y, z, w) = this

        // pitch (lateral-axis rotation)
        val sinrCosp = 2 * (w * x + y * z);
        val cosrCosp = 1 - 2 * (x * x + y * y);
        val pitch = atan2(sinrCosp, cosrCosp);

        // roll (longitudinal-axis rotation)
        val sinr = sqrt(1 + 2 * (w * y - x * z))
        val cosr = sqrt(1 - 2 * (w * y - x * z))
        val roll = 2 * atan2(sinr, cosr) - PI / 2

        // yaw (vertical-axis rotation)
        val sinyCosp = 2 * (w * z + x * y)
        val cosyCosp = 1 - 2 * (y * y + z * z)
        val yaw = atan2(sinyCosp, cosyCosp)

        val pitchDegrees = pitch * 180 / PI
        val rollDegrees = roll * 180 / PI
        val yawDegrees = -yaw * 180 / PI

        return KAngles(pitchDegrees, rollDegrees, yawDegrees)
    }
}