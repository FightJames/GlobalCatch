package com.james.crashhunter.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import java.io.File

object DevicesUtil {
    /**
     * detect the device is rooted.
     * @return true : devices is rooted, vis versa
     */
    fun isDeviceRooted(): Boolean {
        try {
            val su = "su"
            val locations = arrayOf("/system/bin/", "/system/xbin/", "/sbin/", "/system/sd/xbin/",
                "/system/bin/failsafe/", "/data/local/xbin/", "/data/local/bin/", "/data/local/")
            locations.forEach {
                if (File(it + su).exists()) {
                    return true
                }
            }
        } catch (e: Exception) {
        }
        return false
    }

    /**
     * eg. 1.2.1-beta
     */

    fun getSDKVersionName(): String? {
        return Build.VERSION.RELEASE
    }

    /**
     * This is the sdk version. Like 19, 21 or others
     */

    fun getSDKVersionCode(): Int {
        return Build.VERSION.SDK_INT
    }

    /**
     * Get android id.
     */
    @SuppressLint("HardwareIds")
    fun getAndroidID(context: Context): String? {
        return Settings.Secure.getString(
            context.applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    /**
     * Get Manufacturer. eg. Xiaomi
     */
    fun getManufacturer(): String? {
        return Build.MANUFACTURER

    }
    /**
     * Get brand. eg. Xiaomi
     */
    fun getBrand(): String? {
        return Build.BRAND
    }

    /**
     * Get device version code.
     */
    fun getId(): String? {
        return Build.ID
    }

    /**
     * Get CPU type.
     */
    fun getCpuType(): String? {
        return Build.CPU_ABI
    }

    /**
     * Get device's mode. eg. MI2SC
     */
    fun getModel(): String? {
        var model = Build.MODEL
        model = model?.trim { it <= ' ' }?.replace("\\s*".toRegex(), "") ?: ""
        return model
    }
}