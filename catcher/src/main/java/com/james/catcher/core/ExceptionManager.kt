package com.james.catcher.core

import com.james.catcher.interceptor.CaptureData
import com.james.catcher.interceptor.InterceptorManager
import com.james.catcher.interceptor.InterceptorState
import com.james.catcher.utils.FileUtil

object ExceptionManager {

    fun handleException(data: CaptureData): Boolean {
        if (data.config.exceptionStrategy == ExceptionCaptureStrategy.NOT_CATCH) {
            return false
        } else {
            // there no interceptor can handle exception, so rethrow it
            if (InterceptorManager.intercept(data) == InterceptorState.NO) {
                if (data.config.writeToFile) {
                    FileUtil.recordException(data.e, data.config.application)
                }
                return false
            }
        }
        return true
    }


    fun handleException(
        data: CaptureData,
        exceptionHandled: () -> Unit,
        cantHandleException: (CaptureData) -> Unit
    ) {
        if (handleException(data)) {
            exceptionHandled()
        } else {
            if (data.config.writeToFile) {
                FileUtil.recordException(data.e, data.config.application)
            }
            cantHandleException(data)
        }
    }
}