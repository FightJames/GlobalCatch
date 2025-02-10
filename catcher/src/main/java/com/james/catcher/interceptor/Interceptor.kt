package com.james.catcher.interceptor

import com.james.catcher.core.ExceptionCatcherConfig
import com.james.catcher.ext.logd

abstract class Interceptor (var nextInterceptor: Interceptor? = null) {

    fun intercept(data: CaptureData): InterceptorState {
        logd("Interceptor: ${this.javaClass.name}")
        if (process(data) == InterceptorState.YES) return InterceptorState.YES
        return nextInterceptor?.intercept(data) ?: InterceptorState.NO
    }

    abstract fun process(data: CaptureData): InterceptorState

}

data class CaptureData(
    val e: Throwable,
    val thread: Thread,
    var config: ExceptionCatcherConfig
)


enum class InterceptorState {
    YES,
    NO
}