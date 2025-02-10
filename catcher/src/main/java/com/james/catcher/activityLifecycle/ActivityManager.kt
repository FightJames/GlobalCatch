package com.james.catcher.activityLifecycle

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.james.catcher.core.ExceptionCatcherConfig
import java.util.Stack

object ActivityManager : ActivityLifecycleCallbacks {

    private val stack: Stack<String> = Stack()

    fun init(config: ExceptionCatcherConfig) {
        config.application?.registerActivityLifecycleCallbacks(this)
    }

    fun getTopActivityComponentName(): String = stack.peek()

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        stack.push(p0.componentName.toString())
    }

    override fun onActivityStarted(p0: Activity) {
    }

    override fun onActivityResumed(p0: Activity) {
    }

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityStopped(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(p0: Activity) {
        stack.pop()
    }

}

