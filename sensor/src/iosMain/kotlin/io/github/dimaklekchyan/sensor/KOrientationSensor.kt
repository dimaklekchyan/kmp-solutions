package io.github.dimaklekchyan.sensor

import io.github.dimaklekchyan.core.KContext
import io.github.dimaklekchyan.sensor.model.KAngles
import io.github.dimaklekchyan.sensor.model.KQuaternion
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue

actual class KOrientationSensor actual constructor(
    context: KContext
) {

    private val motionManager = CMMotionManager()

    private val _eulerAnglesFlow = MutableStateFlow(KAngles.INIT)
    actual val eulerAnglesFlow: StateFlow<KAngles> = _eulerAnglesFlow

    @OptIn(ExperimentalForeignApi::class)
    actual fun registerListener(interval: KSensorInterval) {
        if (motionManager.isDeviceMotionAvailable()) {
            motionManager.deviceMotionUpdateInterval = interval.milliseconds / 1000.0
            motionManager.startDeviceMotionUpdatesToQueue(NSOperationQueue.mainQueue) { data, error ->
                data?.let {
                    it.attitude.quaternion.useContents {
                        val quaternion = KQuaternion(
                            x = this.x,
                            y = this.y,
                            z = this.z,
                            w = this.w
                        )
                        val angles = quaternion.toEulerAngles()
                        _eulerAnglesFlow.tryEmit(angles)
                    }
                }
            }
        }
    }

    actual fun unregisterListener() {
        motionManager.stopDeviceMotionUpdates()
    }
}