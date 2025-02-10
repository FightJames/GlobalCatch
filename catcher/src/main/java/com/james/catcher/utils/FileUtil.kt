package com.james.catcher.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Process
import android.text.TextUtils
import android.util.Log
import com.james.catcher.ext.logd
import com.james.catcher.ext.loge
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object FileUtil {
    private val TAG = "FileUtil"
    private val CRASH_REPORTER_EXTENSION = ".txt"

    var headContent: String = ""

    private val dataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    private var crashTime: String = ""
    private var crashHead: String = ""
    private var crashMem: String = ""
    private var crashThread: String = ""
    private var versionName: String = ""
    private var versionCode: String = ""

    fun recordException(e: Throwable, context: Context?) {
        e.localizedMessage?.let { msg ->
            loge( "record crash : $msg")
            e.printStackTrace()
            context?.let { saveCrashInfoInFile(it, e) }
        }
    }

    private fun saveCrashInfoInFile(context: Context, ex: Throwable) {
        initCrashHead(context)
        initPhoneHead(context)
        initThreadHead(context, ex)
        dumpExceptionToFile(context, ex)
//        saveCrashInfoToFile(context,ex);
    }

    private fun initCrashHead(context: Context) {
        //crash time
        crashTime = dataFormat.format(Date(System.currentTimeMillis()))
        // version info
        try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName,
                PackageManager.GET_CONFIGURATIONS)
            if (pi != null) {
                versionName = pi.versionName.toString()
                versionCode = pi.versionCode.toString()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        //Get android info
        val sb = StringBuilder()
//        sb.append("\nIs Debug version :").append(BuildConfig.BUILD_TYPE)
        sb.append("\nCrash time :").append(crashTime)
        sb.append("\nDoes root :").append(DevicesUtil.isDeviceRooted())
        sb.append("\nHardware manufacturer :").append(DevicesUtil.getManufacturer())
        sb.append("\nDevice brand:").append(DevicesUtil.getBrand())
        sb.append("\nPhone model :").append(DevicesUtil.getModel())
        sb.append("\nDevice version code :").append(DevicesUtil.getId())
        sb.append("\nCPU type:").append(DevicesUtil.getCpuType())
        sb.append("\nSystem version :").append(DevicesUtil.getSDKVersionName())
        sb.append("\nSystem version code:").append(DevicesUtil.getSDKVersionCode())
        sb.append("\nSystem version info :").append(versionName).append("—").append(versionCode)
        sb.append("\n\n")
        crashHead = sb.toString()
    }

    private fun initPhoneHead(context: Context) {
        val sb = StringBuilder()
        sb.append("Memory analysis :")
        val pid: Int = MemoryUtil.getCurrentPid()
        val pssInfo: MemoryUtil.PssInfo = MemoryUtil.getAppPssInfo(context, pid)
        sb.append("\nVM heap size:").append(MemoryUtil.getFormatSize(pssInfo.dalvikPss))
        sb.append("\nPhone heap size:").append(MemoryUtil.getFormatSize(pssInfo.nativePss))
        sb.append("\nPSS RAM usage :").append(MemoryUtil.getFormatSize(pssInfo.totalPss))
        sb.append("\nOther ratio :").append(MemoryUtil.getFormatSize(pssInfo.otherPss))
        val dalvikHeapMem: MemoryUtil.DalvikHeapMem = MemoryUtil.getAppDalvikHeapMem()
        sb.append("\nUsage RAM :").append(MemoryUtil.getFormatSize(dalvikHeapMem.allocated))
        sb.append("\nMAX RAM:").append(MemoryUtil.getFormatSize(dalvikHeapMem.maxMem))
        sb.append("\nFREE RAM:").append(MemoryUtil.getFormatSize(dalvikHeapMem.freeMem))
        val appTotalDalvikHeapSize: Double = MemoryUtil.getAppTotalDalvikHeapSize(context)
        sb.append("\nUsage RAM of application :").append(
            MemoryUtil.getFormatSize(
                appTotalDalvikHeapSize
            )
        )
        sb.append("\n\n")
        crashMem = sb.toString()
    }

    private fun initThreadHead(context: Context, ex: Throwable) {
        val sb = StringBuilder()
        sb.append("APP info :")
        val currentProcessName: String? = ProcessUtil.getCurrentProcessName(context)
        if (currentProcessName != null) {
            sb.append("\nApp process name :").append(currentProcessName)
        }
        sb.append("\nPID").append(Process.myPid())
        sb.append("\nUID who launched the pid :").append(Process.myUid())
        sb.append("\nThread id :").append(Thread.currentThread().id)
        sb.append("\nThread name :").append(Thread.currentThread().name)
        sb.append("\nMain thread id :").append(context.mainLooper.thread.id)
        sb.append("\nMain thread name").append(context.mainLooper.thread.name)
        sb.append("\nMain thread priority :").append(context.mainLooper.thread.priority)
        sb.append("\n\n")
        crashThread = sb.toString()
    }
    private fun dumpExceptionToFile(context: Context, ex: Throwable) {
        var file: File? = null
        var pw: PrintWriter? = null
        try {
            // SDCard/Android/data/<application package>/cache
            // data/data/<application package>/cache
            val dir: File = File(FileToolUtil.getCrashLogPath(context))
            if (!dir.exists()) {
                val ok = dir.mkdirs()
                if (!ok) {
                    return
                }
            }
            val fileName = "V" + versionName + "_" + crashTime + CRASH_REPORTER_EXTENSION
            file = File(dir, fileName)
            if (!file.exists()) {
                val createNewFileOk = file.createNewFile()
                if (!createNewFileOk) {
                    return
                }
            }
            logd( "Exception log file name：$fileName")
            logd( "Exception log file：$file")
            //Start to log
            pw = PrintWriter(BufferedWriter(FileWriter(file)))
            // Is there extra data to write ?
            if (!TextUtils.isEmpty(headContent)) {
                pw.println(headContent)
            }
            // Write device's info.
            pw.println(crashHead)
            pw.println(crashMem)
            pw.println(crashThread)
            // Dump exception stack info.
            ex.printStackTrace(pw)
            // Exception cause info.
            var cause = ex.cause
            while (cause != null) {
                cause.printStackTrace(pw)
                cause = cause.cause
            }
            val string = ex.toString()
            val splitEx: String
            splitEx = if (string.contains(":")) {
                ex.toString().split(":".toRegex()).toTypedArray()[0]
            } else {
                "java.lang.Exception"
            }
            val newName = "V" + versionName + "_" + crashTime + "_" + splitEx + CRASH_REPORTER_EXTENSION
            val newFile = File(dir, newName)
            FileToolUtil.renameFile(file.path, newFile.path)
            //file       V1.0_2020-09-02_09:05:01.txt
            //newFile    V1.0_2020-09-02_09:05:01_java.lang.NullPointerException.txt
            logd( "Exceoption log file path ：" + file.path + "----New Path : ---" + newFile.path)
        } catch (e: Exception) {
            loge( "dump file fail：$e")
        } finally {
            pw?.close()
        }
    }

    fun print(thr: Throwable) {
        val stackTraces = thr.stackTrace
        for (stackTrace in stackTraces) {
            val clazzName = stackTrace.className
            val fileName = stackTrace.fileName
            val lineNumber = stackTrace.lineNumber
            val methodName = stackTrace.methodName
            logd( "printThrowable------" + clazzName + "----"
                    + fileName + "------" + lineNumber + "----" + methodName)
        }
    }

    private fun parseThrowable(ex: Throwable?, context: Context?): StackTraceElement? {
        if (ex == null || ex.stackTrace.isEmpty()) {
            return null
        }
        if (context == null) {
            return null
        }
        val stackTrace = ex.stackTrace
        val element: StackTraceElement
        val packageName = context.packageName
        for (ele in stackTrace) {
            if (ele.className.contains(packageName)) {
                element = ele
                val clazzName = element.className
                val fileName = element.fileName
                val lineNumber = element.lineNumber
                val methodName = element.methodName
                logd( "printThrowable----1--" + clazzName + "----"
                        + fileName + "------" + lineNumber + "----" + methodName)
                return element
            }
        }
        element = stackTrace[0]
        val clazzName = element.className
        val fileName = element.fileName
        val lineNumber = element.lineNumber
        val methodName = element.methodName
        logd( "printThrowable----2--" + clazzName + "----"
                + fileName + "------" + lineNumber + "----" + methodName)
        return element
    }

    /**
     * Get exception file path.
     *
     * @param ctx
     * @return
     */
    @Deprecated("")
    fun getCrashReportFiles(ctx: Context): Array<String?>? {
        val filesDir = File(getCrashFilePath(ctx))
        val fileNames = filesDir.list()
        val length = fileNames.size
        val filePaths = arrayOfNulls<String>(length)
        for (i in 0 until length) {
            filePaths[i] = getCrashFilePath(ctx) + fileNames[i]
        }
        return filePaths
    }


    /**
     * Save error message to the txt file.
     * @param ex
     * @return
     */
    @Deprecated("")
    fun saveCrashInfoToFile(context: Context, ex: Throwable) {
        val info: Writer = StringWriter()
        val printWriter = PrintWriter(info)
        ex.printStackTrace(printWriter)
        var cause = ex.cause
        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        val result = info.toString()
        printWriter.close()
        val sb = StringBuilder()
        @SuppressLint("SimpleDateFormat") val now = dataFormat.format(Date())
        sb.append("TIME:").append(now) // crash time
        // Sofeware info
//        sb.append("\nAPPLICATION_ID:").append(BuildConfig.APPLICATION_ID) //APPLICATION_ID
//        sb.append("\nVERSION_CODE:").append(BuildConfig.VERSION_CODE) //Software version code.
//        sb.append("\nVERSION_NAME:").append(BuildConfig.VERSION_NAME) //VERSION_NAME
//        sb.append("\nBUILD_TYPE:").append(BuildConfig.BUILD_TYPE) // Does debug version?
        //设备信息
        sb.append("\nMODEL:").append(Build.MODEL)
        sb.append("\nRELEASE:").append(Build.VERSION.RELEASE)
        sb.append("\nSDK:").append(Build.VERSION.SDK_INT)
        sb.append("\nEXCEPTION:").append(ex.localizedMessage)
        sb.append("\nSTACK_TRACE:").append(result)
        val crashFilePath = getCrashFilePath(context)
        if (crashFilePath != null && crashFilePath.isNotEmpty()) {
            try {
                Log.w(TAG, "handleException---File path-----$crashFilePath")
                val writer = FileWriter(crashFilePath + now + CRASH_REPORTER_EXTENSION)
                writer.write(sb.toString())
                writer.flush()
                writer.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Deprecated("")
    private fun getCrashFilePath(context: Context): String? {
        var path: String? = null
        try {

            path = (Environment.getExternalStorageDirectory().canonicalPath
                    + "/" + context.packageName + "/Crash/")
            val file = File(path)
            if (!file.exists()) {
                file.mkdirs()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return path
    }
}