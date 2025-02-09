package com.james.crashhunter.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicLong

object MemoryUtil {
    fun getCurrentPid(): Int {
        return Process.myPid()
    }

    fun getMemoryInfo(context: Context, onGetMemoryInfoCallback: OnGetMemoryInfoCallback) {
        Thread {
            val pkgName = context.packageName
            val pid = Process.myPid()
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mi = ActivityManager.MemoryInfo()
            am.getMemoryInfo(mi)
            //1. ram
            val ramMemoryInfo = RamMemoryInfo()
            ramMemoryInfo.availMem = mi.availMem
            ramMemoryInfo.isLowMemory = mi.lowMemory
            ramMemoryInfo.lowMemThreshold = mi.threshold
            ramMemoryInfo.totalMem = getRamTotalMemSync(context)
            //2. pss
            val pssInfo = getAppPssInfo(context, pid)
            //3. dalvik heap
            val dalvikHeapMem = getAppDalvikHeapMem()
            Handler(Looper.getMainLooper()).post { onGetMemoryInfoCallback.onGetMemoryInfo(pkgName, pid, ramMemoryInfo, pssInfo, dalvikHeapMem) }
        }.start()
    }

    fun getSystemRam(context: Context, onGetRamMemoryInfoCallback: OnGetRamMemoryInfoCallback) {
        getRamTotalMem(context, object : OnGetRamTotalMemCallback {
            override fun onGetRamTotalMem(totalMem: Long) {
                val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val mi = ActivityManager.MemoryInfo()
                am.getMemoryInfo(mi)
                val ramMemoryInfo = RamMemoryInfo()
                ramMemoryInfo.availMem = mi.availMem
                ramMemoryInfo.isLowMemory = mi.lowMemory
                ramMemoryInfo.lowMemThreshold = mi.threshold
                ramMemoryInfo.totalMem = totalMem
                onGetRamMemoryInfoCallback.onGetRamMemoryInfo(ramMemoryInfo)
            }
        })
    }

    fun getAppPssInfo(context: Context, pid: Int): PssInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = am.getProcessMemoryInfo(intArrayOf(pid))[0]
        val pssInfo = PssInfo()
        //返回总的PSS内存使用量(以kB为单位)
        pssInfo.totalPss = memoryInfo.totalPss.toDouble()
        //dalvik堆的比例设置大小
        pssInfo.dalvikPss = memoryInfo.dalvikPss.toDouble()
        //本机堆的比例设置大小
        pssInfo.nativePss = memoryInfo.nativePss.toDouble()
        //比例设置大小为其他所有
        pssInfo.otherPss = memoryInfo.otherPss.toDouble()
        return pssInfo
    }

    fun getAppDalvikHeapMem(): DalvikHeapMem {
        val runtime = Runtime.getRuntime()
        val dalvikHeapMem = DalvikHeapMem()
        dalvikHeapMem.freeMem = runtime.freeMemory().toDouble()
        dalvikHeapMem.maxMem = Runtime.getRuntime().maxMemory().toDouble()
        // used memory
        dalvikHeapMem.allocated = Runtime.getRuntime().totalMemory().toDouble() - runtime.freeMemory().toDouble()
        return dalvikHeapMem
    }

    fun getAppTotalDalvikHeapSize(context: Context): Double {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.memoryClass.toDouble()
    }

    class DalvikHeapMem {
        var freeMem: Double = 0.0
        var maxMem: Double = 0.0
        var allocated: Double = 0.0
    }

    class PssInfo {
        var totalPss: Double = 0.0
        var dalvikPss: Double = 0.0
        var nativePss: Double = 0.0
        var otherPss: Double = 0.0
    }

    class RamMemoryInfo {
        // avaiable ram
        var availMem: Long = 0

        //total ram
        var totalMem: Long = 0

        // if memory usage over than lowMemThreshold, the process may be killed.
        var lowMemThreshold: Long = 0

        // Does run on low ram ?
        var isLowMemory = false
    }

    interface OnGetMemoryInfoCallback {
        fun onGetMemoryInfo(pkgName: String?, pid: Int, ramMemoryInfo: RamMemoryInfo?, pssInfo: PssInfo?, dalvikHeapMem: DalvikHeapMem?)
    }

    interface OnGetRamMemoryInfoCallback {
        fun onGetRamMemoryInfo(ramMemoryInfo: RamMemoryInfo?)
    }

    private interface OnGetRamTotalMemCallback {
        // Total RAM (Unit : KB)
        fun onGetRamTotalMem(totalMem: Long)
    }

    private fun getRamTotalMem(context: Context, onGetRamTotalMemCallback: OnGetRamTotalMemCallback) {
        Thread {
            val totalRam = getRamTotalMemSync(context)
            Handler(Looper.getMainLooper()).post { onGetRamTotalMemCallback.onGetRamTotalMem(totalRam) }
        }.start()
    }

    private fun getRamTotalMemSync(context: Context): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mi = ActivityManager.MemoryInfo()
            am.getMemoryInfo(mi)
            mi.totalMem
        } else if (sTotalMem.get() > 0L) { // The value is got.
            sTotalMem.get()
        } else {
            val tm = getRamTotalMemByFile()
            sTotalMem.set(tm)
            tm
        }
    }

    private val sTotalMem = AtomicLong(0L)

    private fun getRamTotalMemByFile(): Long {
        val dir = "/proc/meminfo"
        try {
            val fr = FileReader(dir)
            val br = BufferedReader(fr, 2048)
            val memoryLine = br.readLine()
            val subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"))
            br.close()
            return subMemoryLine.replace("\\D+".toRegex(), "").toInt().toLong()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return 0L
    }


    fun getFormatSize(size: Double): String? {
        val kiloByte = size / 1024
        if (kiloByte < 1) {
            return size.toString() + "Byte"
        }
        val megaByte = kiloByte / 1024
        if (megaByte < 1) {
            val result1 = BigDecimal(kiloByte.toString())
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB"
        }
        val gigaByte = megaByte / 1024
        if (gigaByte < 1) {
            val result2 = BigDecimal(megaByte.toString())
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB"
        }
        val teraBytes = gigaByte / 1024
        if (teraBytes < 1) {
            val result3 = BigDecimal(gigaByte.toString())
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB"
        }
        val result4 = BigDecimal(teraBytes)
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB"
    }

}