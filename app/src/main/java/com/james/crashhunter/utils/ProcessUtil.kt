package com.james.crashhunter.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import androidx.annotation.NonNull

object ProcessUtil {
    private var currentProcessName: String? = null

    fun getCurrentProcessName(context: Context): String? {
        if (!currentProcessName.isNullOrEmpty()) {
            return currentProcessName
        }
        // Get process name thru application API.
        currentProcessName = getCurrentProcessNameByApplication()
        if (!currentProcessName.isNullOrEmpty()) {
            return currentProcessName
        }
        // Get ActivityThread thru reflection and gain process name.
        currentProcessName = getCurrentProcessNameByActivityThread()
        if (!currentProcessName.isNullOrEmpty()) {
            return currentProcessName
        }
        // Get process name thru ActivityManager.
        currentProcessName = getCurrentProcessNameByActivityManager(context)
        return currentProcessName
    }

    fun getCurrentProcessNameByApplication(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Application.getProcessName()
        } else null
    }

    @SuppressLint("DiscouragedPrivateApi")
    fun getCurrentProcessNameByActivityThread(): String? {
        var processName: String? = null
        try {
            @SuppressLint("PrivateApi") val declaredMethod = Class.forName("android.app.ActivityThread",
                false, Application::class.java.classLoader)
                .getDeclaredMethod("currentProcessName", *arrayOfNulls<Class<*>?>(0))
            declaredMethod.isAccessible = true
            val invoke = declaredMethod.invoke(null, *arrayOfNulls(0))
            if (invoke is String) {
                processName = invoke
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return processName
    }

    fun getCurrentProcessNameByActivityManager(@NonNull context: Context?): String? {
        if (context == null) {
            return null
        }
        val pid = Process.myPid()
        val am = context.applicationContext
            .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        //Get current processes info.
        val runningAppList = am.runningAppProcesses
        if (runningAppList != null) {
            // Get application process info by current pid at current running processes info.
            for (processInfo in runningAppList) {
                if (processInfo.pid == pid) {
                    return processInfo.processName
                }
            }
        }
        return null
    }
}