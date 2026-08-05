package com.equationl.wxsteplog.util

import android.util.Log
import com.blankj.utilcode.util.TimeUtils
import com.equationl.wxsteplog.constants.Constants
import com.equationl.wxsteplog.util.log.LogUtil
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

object LogWrapper {
    private const val TAG = "LogWrapper"

    private const val MAX_CACHE_LENGTH = 1000
    private const val TRIM_LENGTH = 500

    var logCache = StringBuilder("")
        private set

    // UI only needs to know that newer content exists. A conflated signal avoids creating
    // one suspended coroutine per log entry when the main thread is busy.
    val logAppendValue = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    fun log(msg: String, isForceShow: Boolean = false): String {
        return msg.addLog(isForceShow = isForceShow)
    }

    fun String.addLog(isForceShow: Boolean = false): String {
        return logAppend(this, isForceShow = isForceShow)
    }

    @Synchronized
    fun logAppend(msg: CharSequence, isForceShow: Boolean = false): String {
        val showDetailLog = Constants.showDetailLog.value
        if (!isForceShow && !showDetailLog) {
            Log.i(TAG, msg.toString())
            return ""
        }

        // 强制日志是需要长期保留的关键运行记录，即使关闭详细日志也写入文件。
        LogUtil.i(TAG, msg.toString())

        if (logCache.isNotEmpty()) {
            logCache.append("\n")
        }
        if (logCache.length > MAX_CACHE_LENGTH) {
            logCache.delete(0, TRIM_LENGTH)
        }
        logCache.append(TimeUtils.getNowString())
        logCache.append("\n")
        logCache.append(msg)
        logAppendValue.tryEmit(Unit)
        return msg.toString()
    }

    @Synchronized
    fun clearLog() {
        logCache = StringBuilder("")
        logAppendValue.tryEmit(Unit)
    }

}
