package com.james.crashhunter.core

import android.os.Handler
import android.os.Looper
import com.james.crashhunter.activityLifecycle.ActivityManager
import com.james.crashhunter.activityLifecycle.ExceptionLifeCycleCallback
import com.james.crashhunter.ext.logd
import com.james.crashhunter.pagemanager.PageCrashCounter

object ExceptionCapture {

    fun init(config: ExceptionCaptureConfig) {
        config.application?.let { application ->
            ExceptionCaptureHelper.install(config)
            ExceptionCaptureHandler.init(config)
            ExceptionLifeCycleCallback.init(application)
            PageCrashCounter.init(application)
            ActivityManager.init(config)
            Handler(Looper.getMainLooper()).post {
                // when service crash, it will do while loop again to get message queue again.
                while (true) {
                    logd("Begin to catch exception from main thread.")
                    try {
                        Looper.loop()
                    } catch (t: Throwable) {
                        logd("crash happened : ${t.printStackTrace()}")
                        ExceptionCaptureHandler.uncaughtException(Thread.currentThread(), t)
                    }
                }
            }
        }
    }
}