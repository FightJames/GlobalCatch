package com.james.crashhunter.pagemanager

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle

object PageCrashCounter : ActivityLifecycleCallbacks {

    private var pageName: String = ""
        set(value) {
            if (value != field) {
                count = 0
            }
            field = value
        }

    private var count: Int = 0

    val currentPageCrashCount: Int
        get() = count


    // Must call this at application onCreate() method.
    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }

    fun crash() {
        count++
    }

    fun resetCrashCount() {
        count = 0
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        pageName = p0.componentName.className
    }

    override fun onActivityStarted(p0: Activity) {
        pageName = p0.componentName.className
    }

    override fun onActivityResumed(p0: Activity) {
        pageName = p0.componentName.className
    }

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityStopped(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }
}