package com.james.catcher.activityLifecycle

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.james.catcher.ext.logd
import java.util.concurrent.CopyOnWriteArrayList

object ExceptionLifeCycleCallback : ActivityLifecycleCallbacks {
    val FOREGROUND = 1
    val BACKGROUND = 0

    private var mForegroundCount = 0
    private var mAppForegroundState: Int = FOREGROUND

    /**
     * Determine the activity is closed normally.
     * For example, the activity crash at onCreate isn't a normal case.
     * So it can't be counted at onDestroy callback with mForegroundCount field.
     **/
    private var isNormalCloseCurrentActivity = true
    var isInForeground = true
        private set
    // For thread safe
    val mListeners: CopyOnWriteArrayList<OnAppForegroundStateChangeListener> =
        CopyOnWriteArrayList()

    interface OnAppForegroundStateChangeListener {
        // Judge app is on background or foreground.
        fun onAppForegroundStateChange(newState: Int)
    }

    private fun determineAppForegroundState() {
        val oldState: Int = mAppForegroundState
        logd(" mForegroundCount: $mForegroundCount")
        mAppForegroundState = if (mForegroundCount >= 1) FOREGROUND else BACKGROUND
        isInForeground = mAppForegroundState == FOREGROUND
        if (mAppForegroundState != oldState) {
            mListeners.forEach {
                it.onAppForegroundStateChange(mAppForegroundState)
            }
        }
    }

    // Must call this at application onCreate() method.
    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        mForegroundCount++
        isNormalCloseCurrentActivity = false
        determineAppForegroundState()
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        mForegroundCount--
        isNormalCloseCurrentActivity = true
        determineAppForegroundState()
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (!isNormalCloseCurrentActivity) {
            mForegroundCount--
            determineAppForegroundState()
        }
        logd(" Crash Life cycle : $isNormalCloseCurrentActivity, mForegroundCount: $mForegroundCount")
    }
}