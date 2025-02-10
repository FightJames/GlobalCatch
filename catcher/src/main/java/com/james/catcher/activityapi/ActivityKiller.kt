package com.james.catcher.activityapi

import android.os.Message

interface ActivityKiller {
    fun finishLaunchActivity(message: Message)

    fun finishResumeActivity(message: Message)

    fun finishPauseActivity(message: Message)

    fun finishStopActivity(message: Message)

}