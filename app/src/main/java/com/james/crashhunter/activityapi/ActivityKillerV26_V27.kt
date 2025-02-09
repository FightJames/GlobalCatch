package com.james.crashhunter.activityapi

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.os.IBinder
import android.os.Message

class ActivityKillerV26_V27 : ActivityKiller {
    /**
     * handleDestroyActivity((IBinder)msg.obj, msg.arg1 != 0,msg.arg2, false);
     * ActivityManager.getService().finishActivity(mToken, resultCode, resultData, finishTask)
     */
    override fun finishLaunchActivity(message: Message) {
        try {
            val activityClientRecord: Any = message.obj
            val tokenField = activityClientRecord.javaClass.getDeclaredField("token")
            tokenField.isAccessible = true
            val binder = tokenField.get(activityClientRecord) as IBinder
            finish(binder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun finishResumeActivity(message: Message) {
        finishSomeArgs(message)
    }

    override fun finishPauseActivity(message: Message) {
        finishSomeArgs(message)
    }

    override fun finishStopActivity(message: Message) {
        finishSomeArgs(message)
    }

    private fun finishSomeArgs(message: Message) {
        try {
            val someArgs = message.obj
            val arg1Field = someArgs.javaClass.getDeclaredField("arg1")
            arg1Field.isAccessible = true
            val binder = arg1Field[someArgs] as IBinder
            finish(binder)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    @Throws(java.lang.Exception::class)
    private fun finish(binder: IBinder) {
        val getServiceMethod = ActivityManager::class.java.getDeclaredMethod("getService")
        val activityManager = getServiceMethod.invoke(null)
        val finishActivityMethod = activityManager.javaClass.getDeclaredMethod(
            "finishActivity",
            IBinder::class.java,
            Int::class.javaPrimitiveType,
            Intent::class.java,
            Int::class.javaPrimitiveType
        )
        finishActivityMethod.isAccessible = true
        val DONT_FINISH_TASK_WITH_ACTIVITY = 0
        finishActivityMethod.invoke(
            activityManager, binder,
            Activity.RESULT_CANCELED, null, DONT_FINISH_TASK_WITH_ACTIVITY
        )
    }
}