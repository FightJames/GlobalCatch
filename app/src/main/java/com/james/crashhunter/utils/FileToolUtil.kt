package com.james.crashhunter.utils

import android.content.Context
import android.os.Environment
import java.io.*
import java.nio.channels.FileChannel
import java.util.ArrayList

object FileToolUtil {
    fun getCrashLogPath(context: Context): String {
        return getCachePath(context) + File.separator + "crashLogs"
    }

    fun getCrashPicPath(context: Context): String {
        val path = getCachePath(context) + File.separator + "crashPics"
        val file = File(path)
        if (!file.exists()) {
            file.mkdirs()
        }
        return path
    }

    fun getCrashSharePath(): String {
        val path = Environment.getExternalStorageDirectory().toString() + ""
        val file = File(path)
        if (!file.exists()) {
            file.mkdirs()
        }
        return path
    }

    private fun getCachePath(context: Context): String {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
            if (context.externalCacheDir != null) {
                context.externalCacheDir!!.absolutePath
            } else {
                context.cacheDir.absolutePath
            }
        } else {
            context.cacheDir.absolutePath
        }
    }

    fun getCrashFileList(context: Context): List<File> {
        val file = File(getCrashLogPath(context))
        val mFileList: MutableList<File> = ArrayList()
        val fileArray = file.listFiles()
        if (fileArray == null || fileArray.size <= 0) {
            return mFileList
        }
        for (f in fileArray) {
            if (f.isFile) {
                mFileList.add(f)
            }
        }
        return mFileList
    }
    fun deleteFile(fileName: String): Boolean {
        val file = File(fileName)
        // if file is existed and not a folder, delete it.
        return if (file.exists() && file.isFile) {
            file.delete()
        } else {
            false
        }
    }


    fun deleteAllFiles(root: File) {
        val files = root.listFiles()
        if (files != null) for (f in files) {
            if (f.isDirectory) { // is a folder
                deleteAllFiles(f)
                try {
                    f.delete()
                } catch (e: Exception) {
                }
            } else {
                if (f.exists()) { // is existed
                    deleteAllFiles(f)
                    try {
                        f.delete()
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    fun readFile2String(fileName: String?): String {
        var res = ""
        try {
            val inputStream = FileInputStream(fileName)
            var inputStreamReader: InputStreamReader? = null
            try {
                inputStreamReader = InputStreamReader(inputStream, "utf-8")
            } catch (e1: UnsupportedEncodingException) {
                e1.printStackTrace()
            }
            val reader = BufferedReader(inputStreamReader)
            val sb = StringBuilder("")
            var line: String?
            try {
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                    sb.append("\n")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            res = sb.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return res
    }

    fun renameFile(oldPath: String, newPath: String) {
        val oleFile = File(oldPath)
        val newFile = File(newPath)
        //执行重命名
        oleFile.renameTo(newFile)
    }

    fun copyFile(src: File?, dest: File?): Boolean {
        var result = false
        if (src == null || dest == null) {
            return result
        }
        if (dest.exists()) {
            dest.delete() // delete file
        }
        if (!createOrExistsDir(dest.parentFile)) {
            return false
        }
        try {
            dest.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var srcChannel: FileChannel? = null
        var dstChannel: FileChannel? = null
        try {
            srcChannel = FileInputStream(src).channel
            dstChannel = FileOutputStream(dest).channel
            srcChannel.transferTo(0, srcChannel.size(), dstChannel)
            result = true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return result
        } catch (e: IOException) {
            e.printStackTrace()
            return result
        }
        try {
            srcChannel.close()
            dstChannel.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    fun deleteFile(file: File?): Boolean {
        return file != null && (!file.exists() || file.isFile && file.delete())
    }

    fun createOrExistsDir(file: File?): Boolean {
        return file != null && if (file.exists()) file.isDirectory else file.mkdirs()
    }
}