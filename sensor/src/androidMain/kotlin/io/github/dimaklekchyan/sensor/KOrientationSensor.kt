package io.github.dimaklekchyan.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import io.github.dimaklekchyan.core.KContext
import io.github.dimaklekchyan.sensor.model.KAngles
import io.github.dimaklekchyan.sensor.model.KQuaternion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual class KOrientationSensor actual constructor(
    context: KContext
) : SensorEventListener {

    private var sensorManager: SensorManager = context.context
        .getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var rotationVectorSensor: Sensor? = sensorManager
        .getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _eulerAnglesFlow = MutableStateFlow(KAngles.INIT)
    actual val eulerAnglesFlow: StateFlow<KAngles> = _eulerAnglesFlow

    actual fun registerListener(interval: KSensorInterval) {
        rotationVectorSensor?.let {
            val microseconds = (interval.milliseconds * 1000).toInt()
            sensorManager.registerListener(this, it, microseconds)
        }
    }

    actual fun unregisterListener() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

            val q = FloatArray(4)
            SensorManager.getQuaternionFromVector(q, event.values)

            val quaternion = KQuaternion(
                x = q[1].toDouble(), // x
                y = q[2].toDouble(), // y
                z = q[3].toDouble(), // z
                w = q[0].toDouble()  // w
            )
            _eulerAnglesFlow.tryEmit(quaternion.toEulerAngles())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}