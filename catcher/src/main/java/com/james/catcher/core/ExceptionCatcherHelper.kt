package com.james.catcher.core

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import com.james.catcher.ext.logd
import com.james.catcher.activityapi.ActivityKiller
import com.james.catcher.activityapi.ActivityKillerV15_V20
import com.james.catcher.activityapi.ActivityKillerV21_V23
import com.james.catcher.activityapi.ActivityKillerV24_V25
import com.james.catcher.activityapi.ActivityKillerV26_V27
import com.james.catcher.activityapi.ActivityKillerV28_
import com.james.catcher.interceptor.CaptureData
import me.weishu.reflection.Reflection

object ExceptionCatcherHelper {

    private var sActivityKiller: ActivityKiller? = null
    private lateinit var config: ExceptionCatcherConfig
    private var defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null
    const val LAUNCH_ACTIVITY = 100
    const val PAUSE_ACTIVITY = 101
    const val PAUSE_ACTIVITY_FINISHING = 102
    const val STOP_ACTIVITY_HIDE = 104
    const val RESUME_ACTIVITY = 107
    const val DESTROY_ACTIVITY = 109
    const val NEW_INTENT = 112
    const val RELAUNCH_ACTIVITY = 126
    const val CREATE_SERVICE = 114
    const val SERVICE_ARGS = 115
    const val STOP_SERVICE = 116
    const val BIND_SERVICE = 121
    const val UNBIND_SERVICE = 122

    /**
     * Remove Android P reflection restrictions
     */
    internal fun install(
        config: ExceptionCatcherConfig,
        defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler?
    ) {
        ExceptionCatcherHelper.defaultUncaughtExceptionHandler = defaultUncaughtExceptionHandler
        ExceptionCatcherHelper.config = config
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
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val activityThread =
            activityThreadClass.getDeclaredMethod("currentActivityThread").invoke(null)
        val mhField = activityThreadClass.getDeclaredField("mH")
        mhField.isAccessible = true
        val mhHandler = mhField[activityThread] as Handler
        val callbackField = Handler::class.java.getDeclaredField("mCallback")
        callbackField.isAccessible = true
        callbackField[mhHandler] = Handler.Callback { msg ->

            // Service part
            when (msg.what) {
                CREATE_SERVICE,
                SERVICE_ARGS,
                STOP_SERVICE,
                BIND_SERVICE,
                UNBIND_SERVICE -> {
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (t: Throwable) {
                        defaultUncaughtExceptionHandler?.uncaughtException(
                            Thread.currentThread(),
                            t
                        )
                    }
                }
            }


            if (Build.VERSION.SDK_INT >= 28) {
                // whole android P lifecycle will be here.
                val EXECUTE_TRANSACTION = 159
                if (msg.what == EXECUTE_TRANSACTION) {
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        ExceptionManager.handleException(
                            CaptureData(throwable, Thread.currentThread(), config), {
                                sActivityKiller?.finishLaunchActivity(msg)
                            }) {
                            defaultUncaughtExceptionHandler?.uncaughtException(
                                Thread.currentThread(),
                                throwable
                            )
                        }
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
                        ExceptionManager.handleException(
                            CaptureData(throwable, Thread.currentThread(), config), {
                                sActivityKiller?.finishLaunchActivity(msg)
                            }) {
                            defaultUncaughtExceptionHandler?.uncaughtException(
                                Thread.currentThread(),
                                throwable
                            )
                        }
                    }
                    return@Callback true
                }

                RESUME_ACTIVITY -> {
                    // back to activity. onRestart onStart onResume
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        ExceptionManager.handleException(
                            CaptureData(throwable, Thread.currentThread(), config), {
                                sActivityKiller?.finishResumeActivity(msg)
                            }) {
                            defaultUncaughtExceptionHandler?.uncaughtException(
                                Thread.currentThread(),
                                throwable
                            )
                        }
                    }
                    return@Callback true
                }

                PAUSE_ACTIVITY_FINISHING -> {
                    // when click back button (backpress), it will call onPause
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        ExceptionManager.handleException(
                            CaptureData(throwable, Thread.currentThread(), config), {
                                sActivityKiller?.finishPauseActivity(msg)
                            }) {
                            defaultUncaughtExceptionHandler?.uncaughtException(
                                Thread.currentThread(),
                                throwable
                            )
                        }
                    }
                    return@Callback true
                }

                PAUSE_ACTIVITY -> {
                    // When launch the new activity, the old activity will call activity.onPause
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        ExceptionManager.handleException(
                            CaptureData(throwable, Thread.currentThread(), config), {
                                sActivityKiller?.finishPauseActivity(msg)
                            }) {
                            defaultUncaughtExceptionHandler?.uncaughtException(
                                Thread.currentThread(),
                                throwable
                            )
                        }
                    }
                    return@Callback true
                }

                STOP_ACTIVITY_HIDE -> {
                    // When launch the new activity, the old activity will call activity.onStop
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        ExceptionManager.handleException(
                            CaptureData(throwable, Thread.currentThread(), config), {
                                sActivityKiller?.finishStopActivity(msg)
                            }) {
                            defaultUncaughtExceptionHandler?.uncaughtException(
                                Thread.currentThread(),
                                throwable
                            )
                        }
                    }
                    return@Callback true
                }

                DESTROY_ACTIVITY -> {
                    // Close activity. onStop onDestroy
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (_: Throwable) {
                    }
                    return@Callback true
                }
            }
            false
        }
    }
}