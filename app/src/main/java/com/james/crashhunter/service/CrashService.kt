package com.james.crashhunter.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.james.catcher.core.ExceptionCaptureStrategy

class CrashService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        throw Exception("Crash Service")
        return super.onStartCommand(intent, flags, startId)
    }
}