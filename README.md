# Exception Capture

This library will caught all exception from app main thread. It can prevent app crash and provide a better way to solve problems.
But some cases, you need to let app crash.

## Use
```kotlin
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
                val config = ExceptionCatcherConfig.Builder()
            .application(this)
            .setExceptionStrategy(ExceptionCaptureStrategy.CATCH)
            .addInterceptor(object : Interceptor() {
                override fun process(data: CaptureData): InterceptorState {
                    return InterceptorState.YES
                }
            }).build()
        ExceptionCatcher.init(config)
    }
}
```

### ExceptionCaptureStrategy
CATCH : Catch all exception before system handle it.
NOT_CATCH : Don't catch exceptions, just make app crash.

### Interceptor
```kotlin
class SampleInterceptor : Interceptor() {
    
    override fun process(data: CaptureData): InterceptorState {
        return InterceptorState.YES
    }
}
```
Before system handle the exception, the exception will pass to interceptor to handle exception.
When the interceptor's process method return InterceptorState.YES which mean the exception was handled, it will not be threw to system. vis versa.

Here is CaptureData format
```kotlin
data class CaptureData(
    val e: Throwable,
    val thread: Thread,
    var config: ExceptionCaptureConfig? = null
)
```

## Error data file
The error data will store at SDCard/Android/data/<application package>/cache, data/data/<application package>/cache.
You can find the data here.

## Leak case
If there is any crash from service, it won't catch exception. Just let app crash.

```kotlin
class CrashService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        throw Exception("Crash Service")
        return super.onStartCommand(intent, flags, startId)
    }
}

```
