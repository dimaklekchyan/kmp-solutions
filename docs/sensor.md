Sensor is a library for obtaining information from device sensors.

## Installation
project **build.gradle.kts**
```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.dimaklekchyan:sensor:0.1.0")
        }
    }
}
```

## [Orientation sensor](../sensor/src/commonMain/kotlin/io/github/dimaklekchyan/sensor/KOrientationSensor.kt)
A sensor which provides device orientation in three planes: pitch, roll, yaw. 
Please note that angles are calculated in the coordinate system used in aviation.

<img src="../../docs/media/sensor.png" width="480" height="480">

### Planes. Values are positive when rotating clockwise.
* pitch - a rotation around a lateral axis. Range of values [-180, 180]
* roll - a rotation around a longitudinal axis. Range of values [-90, 90]
* yaw - a rotation around a vertical axis. Range of values [-180, 180]

### Usage
To start listening of sensor use `sensor.registerListener` and `sensor.unregisterListener` to stop.
Always make sure to disable sensors you don't need.

Common code:
```kotlin
@Composable
fun Sample() {
    val kContext = rememberKContext()

    val orientationSensor = remember(kContext) {
        KOrientationSensor(kContext)
    }

    val orientation by orientationSensor.eulerAnglesFlow.collectAsState()

    DisposableEffect(Unit) {
        orientationSensor.registerListener()
        onDispose { orientationSensor.unregisterListener() }
    }

    LifecycleEventEffect(
      event = Lifecycle.Event.ON_RESUME,
      onEvent = { orientationSensor.registerListener() }
    )
    LifecycleEventEffect(
      event = Lifecycle.Event.ON_PAUSE,
      onEvent = { orientationSensor.unregisterListener() }
    )
}
```

Use [KSensorInterval](../sensor/src/commonMain/kotlin/io/github/dimaklekchyan/sensor/KOrientationSensor.kt) to set the frequency of data retrieval.
You can choose from standard options or set your own using `KSensorInterval.Custom(...)`
```kotlin
orientationSensor.registerListener(
  interval = KSensorInterval.Fastest //Game, Ui, Normal or Custom
)
```

## Samples
More examples can be found in the [sample directory](../sample/shared/src/commonMain/kotlin/io/github/dimaklekchyan/sample/shared/Sample.kt).

