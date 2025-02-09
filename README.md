# Exception Capture

This library will caught all exception from app main thread. It can prevent app crash and provide a better way to solve problems.
But some cases, you need to let app crash.

## Use
```
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val config = ExceptionCaptureConfig.Builder()
            .application(this)
            .setExceptionStrategy(ExceptionCaptureStrategy.CATCH)
            .addInterceptor(object : Interceptor() {
                override fun process(data: CaptureData): InterceptorState {
                    Log.d("Exception ", data.e.toString())
                    return InterceptorState.NO
                }
            }).build()
        ExceptionCapture.init(config)
    }
}
```

### ExceptionCaptureStrategy
CATCH : Catch all exception before system handle it.
NOT_CATCH : Don't catch exceptions, just make app crash.

### Interceptor
```
class SampleInterceptor : Interceptor() {
    
    override fun process(data: CaptureData): InterceptorState {
        return InterceptorState.YES
    }
}
```
Before system handle the exception, the exception will pass to interceptor to handle exception.
When the interceptor's process method return InterceptorState.YES which mean the exception was handled, it will not be threw to system. vis versa.

Here is CaptureData format
```
data class CaptureData(
    val e: Throwable,
    val thread: Thread,
    var config: ExceptionCaptureConfig? = null
)
```

## ExceptionListener
Theoretically, handledException will be called after the exception handled by interceptors.
Here is edge case for ExceptionListener.
If the exception is threw at onStart(), the exception will be handled and call ExceptionListener's handledException. But the exception not pass to interceptors.
And close current activity.
```
interface ExceptionListener {

    // The throwable was handled by interceptor.
    fun handledException(throwable: Throwable, thread: Thread)

    // The throwable can't be handled by interceptors.
    fun unhandledException(throwable: Throwable, thread: Thread, config: ExceptionCaptureConfig)
}
```

## Error data file
The error data will store at SDCard/Android/data/<application package>/cache, data/data/<application package>/cache.
You can find the data here.


