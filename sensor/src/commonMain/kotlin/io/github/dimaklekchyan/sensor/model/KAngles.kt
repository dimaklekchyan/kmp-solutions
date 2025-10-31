package io.github.dimaklekchyan.sensor.model

data class KAngles(
    val pitch: Double,
    val roll: Double,
    val yaw: Double,
) {
    companion object Companion {
        val INIT = KAngles(0.0, 0.0, 0.0)
    }
}