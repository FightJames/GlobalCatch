package com.james.catcher.interceptor

import com.james.catcher.interceptor.CaptureData
import com.james.catcher.interceptor.Interceptor
import com.james.catcher.interceptor.InterceptorState

class SampleInterceptor : Interceptor() {

    override fun process(data: CaptureData): InterceptorState {
        return InterceptorState.YES
    }
}