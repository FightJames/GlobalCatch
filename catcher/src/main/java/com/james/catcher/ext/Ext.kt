package com.james.catcher.ext

import android.util.Log
import com.james.catcher.BuildConfig

val isDebug: Boolean = BuildConfig.DEBUG
val TAG = "CrashHunter"

fun Any.logd(string: String) {
    if (isDebug) {
        val name = this.javaClass.name
        if (name.length >= 26) {
            Log.d(TAG, string)
            return
        }
        Log.d(name, string)
    }
}

fun Any.loge(string: String) {
    if (isDebug) {
        val name = this.javaClass.name
        if (name.length >= 26) {
            Log.e(TAG, string)
            return
        }
        Log.d(name, string)
    }
}
fun Any.logi(string: String) {
    if (isDebug) {
        val name = this.javaClass.name
        if (name.length >= 26) {
            Log.i(TAG, string)
            return
        }
        Log.i(name, string)
    }
}
