package com.james.crashhunter.activityapi

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.servertransaction.ClientTransaction
import android.content.Intent
import android.os.IBinder
import android.os.Message

class ActivityKillerV28_ : ActivityKiller {
    override fun finishLaunchActivity(message: Message) {
        try {
            tryFinish1(message)
            return
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
        try {
            tryFinish2(message)
            return
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
        try {
            tryFinish3(message)
            return
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
    }

    @Throws(Throwable::class)
    private fun tryFinish1(message: Message) {
        val clientTransaction = message.obj as ClientTransaction
        val binder: IBinder = clientTransaction.getActivityToken()
        finish(binder)
    }

    @Throws(Throwable::class)
    private fun tryFinish2(message: Message) {
        val clientTransaction = message.obj
        val getActivityTokenMethod =
            clientTransaction.javaClass.getDeclaredMethod("getActivityToken")
        val binder = getActivityTokenMethod.invoke(clientTransaction) as IBinder
        finish(binder)
    }

    @Throws(Throwable::class)
    private fun tryFinish3(message: Message) {
        val clientTransaction = message.obj
        val mActivityTokenField = clientTransaction.javaClass.getDeclaredField("mActivityToken")
        val binder = mActivityTokenField[clientTransaction] as IBinder
        finish(binder)
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
        val clazz: Class<*> = ActivityManager::class.java
        val getServiceMethod = clazz.getDeclaredMethod("getService")
        getServiceMethod.isAccessible = true
        val activityManager = getServiceMethod.invoke(null)
        val managerClass: Class<*> = activityManager.javaClass
        // Get ActivityManagerService class, short : AMS, for getting finishActivity method
        // The forth parameter： Should finish the task which is relative the activity.
        val finishActivityMethod = managerClass.getDeclaredMethod(
            "finishActivity",
            IBinder::class.java,
            Int::class.javaPrimitiveType,
            Intent::class.java,
            Int::class.javaPrimitiveType
        )
        finishActivityMethod.isAccessible = true
        // When the activity had finished, the task did not finished.
        val DONT_FINISH_TASK_WITH_ACTIVITY = 0
        // FINISH_TASK_WITH_ROOT_ACTIVITY = 1
        // If the activity is root of task, the task will be finished.
        // For keeping past behaviors, the task will be delete at TaskManager.
        //FINISH_TASK_WITH_ACTIVITY = 2
        // The task and activity will be finished together, but not remove from TaskManager.
        finishActivityMethod.invoke(
            activityManager, binder,
            Activity.RESULT_CANCELED, null, DONT_FINISH_TASK_WITH_ACTIVITY
        )
    }
}