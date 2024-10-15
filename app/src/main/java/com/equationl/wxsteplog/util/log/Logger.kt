package com.equationl.wxsteplog.util.log

import android.content.Context
import android.util.Log
import com.blankj.utilcode.util.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Logger {
    private var isInit = false
    private lateinit var context: Context
    private var lastCheckDay: String = ""

    fun init(context: Context) {
        this.context = context
        isInit = true
    }

    /**
     * @param tag TAG
     * @param msg msg
     * @param isWrite 是否写入本地文件，设置为 false 则强制不写入本地文件
     * @param printLevel 打印级别，默认为写入本地储存
     * @param level 日志级别
     * @param tr 错误堆栈
     * */
    fun log(tag: String, msg: String, isWrite: Boolean = true, printLevel: PrintLevel = PrintLevel.WRITE, level: LogLevel = LogLevel.DEBUG, tr: Throwable? = null) {
        if (!isInit) {
            Log.e(tag, "log: 未初始化！")
            return
        }

        if (printLevel.ordinal >= PrintLevel.POST.ordinal) {
            post2Server(getMsg(tag, msg, PrintLevel.POST, level, tr))
        }
        if (printLevel.ordinal >= PrintLevel.WRITE.ordinal) {
            if (isWrite) writeLogFile(getMsg(tag, msg, PrintLevel.WRITE, level, tr))
        }
        if (printLevel.ordinal >= PrintLevel.PRINT.ordinal) {
            val msgText = getMsg(tag, msg, PrintLevel.PRINT, level, tr)
            when (level) {
                LogLevel.DEBUG -> {
                    Log.d(tag, msgText)
                }
                LogLevel.INFO -> {
                    Log.i(tag, msgText)
                }
                LogLevel.WARN -> {
                    Log.w(tag, msgText, tr)
                }
                LogLevel.ERROR -> {
                    Log.e(tag, msgText, tr)
                }
            }
        }
    }

    /**
     * 检测并删除过期日志
     *
     * 优先使用 [keepDays] 清理，如果清理完毕依然不满足 [maxSize] 则使用 [maxSize] 清理
     *
     * **注意：为了保证日志能够写入成功，对于 [keepDays] 最小值为 1，如果小于 1 则会拒绝清理；对于 [maxSize] 即使当前日志大于该值，依旧会保留当前最新一份日志文件**
     *
     * *日志保存时以小时为单位，每小时生成一个日志文件*
     *
     * @param keepDays 日志保留天数，超过该天数的日志都将被删除
     * @param maxSize 日志占用最大空间（单位 byte），如果当前日志大小超过该值则会从旧到新依次删除日志直至占用空间小于该值
     * 如果设置为 -1 则表示不检测大小
     *
     * */
    suspend fun clearLogFile(keepDays: Int = 15, maxSize: Long = -1) = withContext(Dispatchers.IO) {
        if (lastCheckDay == TimeUtils.getNowString(SimpleDateFormat("yyyy-MM-dd"))) { // 一天只清理一次
            Log.w(TAG, "今日已运行过日志清理，不再运行")
            return@withContext
        }

        lastCheckDay = TimeUtils.getNowString(SimpleDateFormat("yyyy-MM-dd"))

        if (keepDays < 1) {  // 至少保留一天，不然当前日志会无法写入
            Log.w(TAG, "clearLogFile: keepDays must >= 1 !")
            return@withContext
        }

        val nowDayString = timestamp2DateStr(System.currentTimeMillis())
        val lastDate = timeString2DateLong(nowDayString, "yyyy-MM-dd") - keepDays * 86_400_000 // 一天 86,400,000 ms

        val rootPath = context.getExternalFilesDir(null)
        if (rootPath == null) {
            Log.e(TAG, "clearLogFile: 无法获取储存目录")
            post2Server("【致命错误】无法获取储存目录")
            return@withContext
        }
        val logRootDirFile = File(rootPath.absolutePath+"/Log/Core/")

        val fileTree = logRootDirFile.walk()

        // 第一次遍历，清理指定日期之外的日志
        fileTree.maxDepth(1)
            .filter { it.isDirectory }
            .forEach {
                try {
                    val timeDate = timeString2DateLong(it.name, "yyyy_MM_dd")

                    Log.d(TAG, "clearLogFile: 读取到文件：${it.name}, timeDate=$timeDate, lastDate=$lastDate")

                    if (timeDate in 1..lastDate) {
                        Log.d(TAG, "clearLogFile: 删除该日期所有日志：${it.name}")
                        it.deleteRecursively()
                    }
                } catch (tr: Throwable) {
                    Log.e(TAG, "clearLogFile: 日志内部错误", tr)
                }
            }

        // 第二次遍历，按照总占用空间从旧到新依次清理
        if (maxSize == -1L) {
            Log.d(TAG, "clearLogFile: 不限制日志占用空间")
            return@withContext
        }
        val allLogFiles = arrayListOf<File>()
        var usedStorage = 0L
        fileTree.maxDepth(Int.MAX_VALUE)
            .sortedBy { it.parentFile?.name }
            .sortedWith {f1: File, f2: File ->
                if (f1 > f2) 1
                else -1
            }
            .filter { it.extension == "log" }
            .forEach {
                try {
                    Log.d(TAG, "clearLogFile: 查找到文件：${it.absolutePath}, 大小：${it.length()}")
                    allLogFiles.add(it)
                    usedStorage += it.length()
                }
                catch (tr: Throwable) {
                    Log.e(TAG, "clearLogFile: 日志内部错误2", tr)
                }
            }

        Log.d(TAG, "clearLogFile: 当前已用空间：$usedStorage")
        if (usedStorage > maxSize) {
            try {
                Log.d(TAG, "clearLogFile: 当前日志大小超出限额，需要删除")
                allLogFiles.removeAt(allLogFiles.size - 1)  // 移除最后一个文件，确保不会删除最新的日志文件
                for (file in allLogFiles) {
                    Log.d(TAG, "clearLogFile: 删除文件：${file.absolutePath}")
                    usedStorage -= file.length()
                    val parentFile = file.parentFile
                    file.delete()
                    if (parentFile?.listFiles()?.isEmpty() == true) {
                        Log.d(TAG, "clearLogFile: 当前文件夹已空，删除： $parentFile")
                        parentFile.delete()
                    }
                    if (usedStorage <= maxSize) {
                        Log.d(TAG, "clearLogFile: 日志大小已符合，不再继续删除")
                        break
                    }
                }
            } catch (tr: Throwable) {
                Log.e(TAG, "clearLogFile: 日志内部错误3", tr)
            }
        }
    }

    private fun getMsg(tag: String, msg: String, printLevel: PrintLevel, logLevel: LogLevel, tr: Throwable?): String {
        return when (printLevel) {
            PrintLevel.PRINT -> {
                "$msg\n${tr?.stackTraceToString() ?: ""}"
            }
            PrintLevel.WRITE -> {
                val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA).format(Date())
                "${logLevel.levelTag} $time--$tag：$msg\n${tr?.stackTraceToString() ?: ""}"
            }
            PrintLevel.POST -> {
                "【$tag】$msg"
            }
        }
    }

    private fun post2Server(msg: String, tr: Throwable? = null) {
        return
    }

    private fun writeLogFile(msg: String) {
        val dirName = SimpleDateFormat("yyyy_MM_dd", Locale.CHINA).format(Date())
        val fileName = SimpleDateFormat("HH", Locale.CHINA).format(Date())+".log"
        val rootPath = context.getExternalFilesDir(null)
        if (rootPath == null) {
            Log.e(TAG, "writeLogFile: 无法获取储存目录")
            post2Server("【致命错误】无法获取储存目录")
            return
        }
        val fileDir = File(rootPath.absolutePath+"/Log/Core/$dirName/")
        val file = File(fileDir, fileName)
        if (!file.exists()) {
            try {
                Log.i(TAG, "writeLogFile: 尝试创建文件：$file")
                fileDir.mkdirs()
                file.createNewFile()
            } catch (tr: Throwable) {
                Log.e(TAG, "writeLogFile: 创建文件出错：", tr)

                post2Server("【致命错误】创建日志文件失败: ${tr.stackTraceToString()}")
            }
        }

        try {
            if (file.canWrite()) {
                file.appendText(msg)
            }
            else {
                post2Server("【致命错误】writeLogFile: 无法写入日志文件！")
                Log.w(TAG, "writeLogFile: 无法写入日志文件！")
            }
        } catch (tr: Throwable) {
            post2Server("【致命错误】写入日志文件失败: ${tr.stackTraceToString()}")
        }
    }

    /**
     * 将字符串转为时间戳
     *
     * @param pattern 时间样式 yyyy-MM-dd HH:mm:ss
     * @return 时间戳，如果出错则返回 0
     */
    private fun timeString2DateLong(time:String, pattern: String = "yyyy-MM-dd HH:mm:ss"): Long {
        val dateFormat = SimpleDateFormat(pattern, Locale.CHINA)
        var date: Date? = null
        try {
            date = dateFormat.parse(time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return date?.time ?: 0
    }

    /**
     * 时间戳转换成字符窜
     *
     * @param pattern 时间样式 yyyy-MM-dd HH:mm:ss
     * @return [String] 时间字符串
     */
    private fun timestamp2DateStr(time: Long, pattern: String = "yyyy-MM-dd"): String {
        val date = Date(time)
        val format = SimpleDateFormat(pattern, Locale.CHINA)
        return format.format(date)
    }

    companion object {
        private const val TAG = "Logger"

        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            Logger()
        }
    }

    enum class LogLevel(val levelTag: String) {
        DEBUG("DEBUG"),
        INFO("INFO"),
        WARN("WARN"),
        ERROR("ERROR")
    }

    /**
     * 日志打印级别
     * */
    enum class PrintLevel {
        /**
         * 仅打印
         * */
        PRINT,
        /**
         * 打印后写入本地日志文件
         * */
        WRITE,
        /**
         * 打印并写入本地日志文件最后上传至服务器
         * */
        POST
    }
}