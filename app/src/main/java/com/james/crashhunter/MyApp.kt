package com.james.crashhunter

import android.app.Application
import android.util.Log
import com.james.catcher.core.ExceptionCatcher
import com.james.catcher.core.ExceptionCatcherConfig
import com.james.catcher.core.ExceptionCaptureStrategy
import com.james.catcher.interceptor.CaptureData
import com.james.catcher.interceptor.Interceptor
import com.james.catcher.interceptor.InterceptorState

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = ExceptionCatcherConfig.Builder()
            .application(this)
            .setExceptionStrategy(ExceptionCaptureStrategy.CATCH)
            .addInterceptor(object : Interceptor() {
                override fun process(data: CaptureData): InterceptorState {
                    Log.d("Exception", data.e.toString())
                    return InterceptorState.YES
                }
            }).build()
        ExceptionCatcher.init(config)
    }
}