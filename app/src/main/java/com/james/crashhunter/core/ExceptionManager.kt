package com.james.crashhunter.core

import com.james.crashhunter.activityLifecycle.ActivityManager
import com.james.crashhunter.ext.logd
import com.james.crashhunter.interceptor.CaptureData
import com.james.crashhunter.interceptor.InterceptorManager
import com.james.crashhunter.interceptor.InterceptorState
import com.james.crashhunter.utils.FileUtil

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
        finishActivity: () -> Unit
    ) {

        finishActivity()
    }
}