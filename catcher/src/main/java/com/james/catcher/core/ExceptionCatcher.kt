package com.james.catcher.core

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import com.james.catcher.ext.logd

object ExceptionCatcher {

    @MainThread
    fun init(config: ExceptionCatcherConfig) {
        config.application?.let { application ->
            // In android default system's UncaughtExceptionHandler will handle app crash flow.
            val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
            ExceptionCatcherHelper.install(config, defaultUncaughtExceptionHandler)
            ExceptionCatcherHandler.init(config, defaultUncaughtExceptionHandler)
            Handler(Looper.getMainLooper()).post {
                // when service crash, it will do while loop again to get message queue again.
                while (true) {
                    logd("Begin to catch exception from main thread.")
                    try {
                        Looper.loop()
                    } catch (t: Throwable) {
                        logd("crash happened : ${t.printStackTrace()}")
                        ExceptionCatcherHandler.uncaughtException(Thread.currentThread(), t)
                    }
                }
            }
        }
    }
}