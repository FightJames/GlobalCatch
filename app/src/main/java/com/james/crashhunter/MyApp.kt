package com.james.crashhunter

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.MainThread
import com.james.crashhunter.core.ExceptionCapture
import com.james.crashhunter.core.ExceptionCaptureConfig
import com.james.crashhunter.core.ExceptionCaptureStrategy
import com.james.crashhunter.interceptor.CaptureData
import com.james.crashhunter.interceptor.Interceptor
import com.james.crashhunter.interceptor.InterceptorState
import me.weishu.reflection.Reflection

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = ExceptionCaptureConfig.Builder()
            .application(this)
            .setExceptionStrategy(ExceptionCaptureStrategy.CATCH)
            .addInterceptor(object : Interceptor() {
                override fun process(data: CaptureData): InterceptorState {
                    Log.d("Exception ", data.e.toString())
                    return InterceptorState.YES
                }
            }).build()
        ExceptionCapture.init(config)
    }
}