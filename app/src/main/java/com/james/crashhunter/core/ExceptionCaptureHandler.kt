package com.james.crashhunter.core

import com.james.crashhunter.interceptor.CaptureData

// For cases which exception is from other view event.
object ExceptionCaptureHandler : Thread.UncaughtExceptionHandler {

    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    private lateinit var config: ExceptionCaptureConfig

    fun init(config: ExceptionCaptureConfig) {
        ExceptionCaptureHandler.config = config;
        // get the default UncaughtExceptionHandler of system
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (!ExceptionManager.handleException(CaptureData(e, t, config))) {
            mDefaultHandler?.uncaughtException(t, e)
        }
    }
}