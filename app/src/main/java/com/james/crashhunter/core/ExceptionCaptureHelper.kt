package com.james.crashhunter.core

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import com.james.crashhunter.activityLifecycle.ExceptionLifeCycleCallback
import com.james.crashhunter.activityLifecycle.ExceptionLifeCycleCallback.FOREGROUND
import com.james.crashhunter.activityLifecycle.ExceptionLifeCycleCallback.OnAppForegroundStateChangeListener
import com.james.crashhunter.ext.logd
import com.james.crashhunter.ext.logi
import com.james.crashhunter.activityapi.ActivityKiller
import com.james.crashhunter.activityapi.ActivityKillerV15_V20
import com.james.crashhunter.activityapi.ActivityKillerV21_V23
import com.james.crashhunter.activityapi.ActivityKillerV24_V25
import com.james.crashhunter.activityapi.ActivityKillerV26_V27
import com.james.crashhunter.activityapi.ActivityKillerV28_
import com.james.crashhunter.interceptor.CaptureData
import me.weishu.reflection.Reflection

object ExceptionCaptureHelper : OnAppForegroundStateChangeListener {

    private var sActivityKiller: ActivityKiller? = null
    private lateinit var config: ExceptionCaptureConfig
    var isFromBackground = false

    /**
     * Remove Android P reflection restrictions
     */
    internal fun install(config: ExceptionCaptureConfig) {
        this.config = config
        ExceptionLifeCycleCallback.mListeners.add(0, this)
        try {
            config.application?.let { Reflection.unseal(it.baseContext) }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            logd("Fail unsel $throwable")
        }
        initActivityKiller()
    }

    /**
     * Replace the ActivityThread.mH.mCallback, intercept the lifecycle of Activity, If you directly
     * ignore the exception in lifecycle of Activity, there will be black screen, so we need to call
     * ActivityManager::finishActivity to finish current activity when exception happens.
     */
    private fun initActivityKiller() {
        // The way of get the ActivityManager in every Android version is different, the finishActivity,
        // token is the same situation.
        if (Build.VERSION.SDK_INT >= 28) {
            sActivityKiller = ActivityKillerV28_()
        } else if (Build.VERSION.SDK_INT >= 26) {
            sActivityKiller = ActivityKillerV26_V27()
        } else if (Build.VERSION.SDK_INT == 25 || Build.VERSION.SDK_INT == 24) {
            sActivityKiller = ActivityKillerV24_V25()
        } else if (Build.VERSION.SDK_INT in 21..23) {
            sActivityKiller = ActivityKillerV21_V23()
        } else {
            sActivityKiller = ActivityKillerV15_V20()
        }
        try {
            hookmH()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }


    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    @Throws(Exception::class)
    private fun hookmH() {
        val LAUNCH_ACTIVITY = 100
        val PAUSE_ACTIVITY = 101
        val PAUSE_ACTIVITY_FINISHING = 102
        val STOP_ACTIVITY_HIDE = 104
        val RESUME_ACTIVITY = 107
        val DESTROY_ACTIVITY = 109
        val NEW_INTENT = 112
        val RELAUNCH_ACTIVITY = 126
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val activityThread =
            activityThreadClass.getDeclaredMethod("currentActivityThread").invoke(null)
        val mhField = activityThreadClass.getDeclaredField("mH")
        mhField.isAccessible = true
        val mhHandler = mhField[activityThread] as Handler
        val callbackField = Handler::class.java.getDeclaredField("mCallback")
        callbackField.isAccessible = true
        callbackField[mhHandler] = Handler.Callback { msg ->
            logd(" receiveEvent " + msg.what.toString())
            if (Build.VERSION.SDK_INT >= 28) {
                // whole android P lifecycle will be here.
                val EXECUTE_TRANSACTION = 159
                if (msg.what == EXECUTE_TRANSACTION) {
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        ExceptionManager.handleException(CaptureData(throwable, Thread.currentThread(), config)) {
                            sActivityKiller?.finishLaunchActivity(msg)
                        }
//                        notifyException(throwable)
                    }
                    return@Callback true
                }
                return@Callback false
            }
            when (msg.what) {
                LAUNCH_ACTIVITY -> {
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        ExceptionManager.handleException(CaptureData(throwable, Thread.currentThread(), config)) {
                            sActivityKiller?.finishLaunchActivity(msg)
                        }
//                        notifyException(throwable)
                    }
                    return@Callback true
                }

                RESUME_ACTIVITY -> {
                    // back to activity. onRestart onStart onResume
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        ExceptionManager.handleException(CaptureData(throwable, Thread.currentThread(), config)) {
                            sActivityKiller?.finishResumeActivity(msg)
                        }
//                        notifyException(throwable)
                    }
                    return@Callback true
                }

                PAUSE_ACTIVITY_FINISHING -> {
                    // when click back button (backpress), it will call onPause
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        ExceptionManager.handleException(CaptureData(throwable, Thread.currentThread(), config)) {
                            sActivityKiller?.finishPauseActivity(msg)
                        }
//                        notifyException(throwable)
                    }
                    return@Callback true
                }

                PAUSE_ACTIVITY -> {
                    // When launch the new activity, the old activity will call activity.onPause
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        ExceptionManager.handleException(CaptureData(throwable, Thread.currentThread(), config)) {
                            sActivityKiller?.finishPauseActivity(msg)
                        }
//                        notifyException(throwable)
                    }
                    return@Callback true
                }

                STOP_ACTIVITY_HIDE -> {
                    // When launch the new activity, the old activity will call activity.onStop
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        ExceptionManager.handleException(CaptureData(throwable, Thread.currentThread(), config)) {
                            sActivityKiller?.finishStopActivity(msg)
                        }
//                        notifyException(throwable)
                    }
                    return@Callback true
                }

                DESTROY_ACTIVITY -> {
                    // Close activity. onStop  onDestroy
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
//                        notifyException(throwable)
                    }
                    return@Callback true
                }
            }
            false
        }
    }

//    private fun notifyException(e: Throwable) {
//        config?.run {
//            if (writeToFile) {
//                FileUtil.recordException(e, application)
//            }
//            logi("CrashHunterHelper: $e isFromBackground: $isFromBackground")
//            if (isFromBackground) {
//                this.exceptionStrategy = ExceptionCaptureStrategy.ERROR
//                listener?.unhandledException(e, Thread.currentThread(), this)
//            } else {
//                listener?.handledException(e, Thread.currentThread())
//            }
//        }
//    }


    override fun onAppForegroundStateChange(newState: Int) {
        isFromBackground = (newState == FOREGROUND)
        logi("ExceptionHelper:  $isFromBackground, newState: $newState")
    }
}