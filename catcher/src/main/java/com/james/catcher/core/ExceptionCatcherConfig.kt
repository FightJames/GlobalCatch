package com.james.catcher.core

import android.app.Application
import com.james.catcher.interceptor.InterceptorManager
import com.james.catcher.interceptor.Interceptor

class ExceptionCatcherConfig(builder: Builder) {

    var application: Application? = builder.application
        private set

    var exceptionStrategy: ExceptionCaptureStrategy = builder.exceptionStrategy

    // write exceptions which can't be handled info to the file
    var writeToFile = builder.writeToFile

    val interceptors: List<Interceptor> = InterceptorManager.interceptorList()

    override fun toString(): String {
        return "ExceptionCaptureConfig(application=$application, catchStrategy=$exceptionStrategy, writeToFile=$writeToFile, interceptors=$interceptors)"
    }


    class Builder {
        var application: Application? = null
        var exceptionStrategy: ExceptionCaptureStrategy = ExceptionCaptureStrategy.CATCH
        var writeToFile = true
        val interceptors: MutableList<Interceptor> = mutableListOf()

        fun application(application: Application) = apply {
            this.application = application
        }

        fun setExceptionStrategy(exceptionStrategy: ExceptionCaptureStrategy) = apply {
            this.exceptionStrategy = exceptionStrategy
        }

        fun writeToFile(writeToFile: Boolean) = apply {
            this.writeToFile = writeToFile
        }

        fun addInterceptor(interceptor: Interceptor) = apply {
            InterceptorManager.addInterceptor(interceptor)
        }

        fun build(): ExceptionCatcherConfig = ExceptionCatcherConfig(this)
    }
}

enum class ExceptionCaptureStrategy {
    CATCH, // The exception will be handled by interceptors and catched to prevent app crash.
    NOT_CATCH, // Disable interceptors and throw exception make app crash.
}