package com.james.catcher.core

import com.james.catcher.interceptor.CaptureData

// For cases which exception is from other view event.
object ExceptionCatcherHandler : Thread.UncaughtExceptionHandler {

    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    private lateinit var config: ExceptionCatcherConfig

    fun init(config: ExceptionCatcherConfig, defaultHandler: Thread.UncaughtExceptionHandler? = null) {
        ExceptionCatcherHandler.config = config;
        mDefaultHandler = defaultHandler ?: Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (!ExceptionManager.handleException(CaptureData(e, t, config))) {
            mDefaultHandler?.uncaughtException(t, e)
        }
    }
}