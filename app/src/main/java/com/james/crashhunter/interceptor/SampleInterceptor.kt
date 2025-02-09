package com.james.crashhunter.interceptor

import com.james.crashhunter.interceptor.CaptureData
import com.james.crashhunter.interceptor.Interceptor
import com.james.crashhunter.interceptor.InterceptorState

class SampleInterceptor : Interceptor() {

    override fun process(data: CaptureData): InterceptorState {
        return InterceptorState.YES
    }
}