package io.github.dimaklekchyan.sensor

import io.github.dimaklekchyan.core.KContext
import io.github.dimaklekchyan.sensor.model.KAngles
import kotlinx.coroutines.flow.StateFlow

/**
 * This sensor provides device orientation in three planes: pitch, roll, yaw.
 * Use [KSensorInterval] to set the frequency of data retrieval.
 *
 * To start listening use [registerListener] and [unregisterListener] to stop.
 * Always make sure to disable sensors you don't need.
 *
 * @param context a context wrapper. See [KContext]
 * */
expect class KOrientationSensor(context: KContext) {
    val eulerAnglesFlow: StateFlow<KAngles>
    fun registerListener(interval: KSensorInterval = KSensorInterval.Normal)
    fun unregisterListener()
}

sealed class KSensorInterval(val milliseconds: Long) {
    object Fastest: KSensorInterval(0)
    object Game: KSensorInterval(20)
    object Ui: KSensorInterval(60)
    object Normal: KSensorInterval(200)
    class Custom(milliseconds: Long): KSensorInterval(milliseconds)
}