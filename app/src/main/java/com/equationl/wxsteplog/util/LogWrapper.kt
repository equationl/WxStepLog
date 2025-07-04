package com.equationl.wxsteplog.util

import android.util.Log
import com.blankj.utilcode.util.TimeUtils
import com.equationl.wxsteplog.constants.Constants
import com.equationl.wxsteplog.util.log.LogUtil
import com.ven.assists.utils.CoroutineWrapper
import kotlinx.coroutines.flow.MutableSharedFlow

object LogWrapper {
    private const val TAG = "LogWrapper"

    var logCache = StringBuilder("")

    val logAppendValue = MutableSharedFlow<Pair<String, String>>()

    fun log(msg: String, isForceShow: Boolean = false): String {
        return msg.addLog(isForceShow = isForceShow)
    }

    fun String.addLog(isForceShow: Boolean = false): String {
        return logAppend(this, isForceShow = isForceShow)
    }

    fun logAppend(msg: CharSequence, isForceShow: Boolean = false): String {
        if (!isForceShow && !Constants.showDetailLog.value) {
            Log.i(TAG, msg.toString())
            return ""
        }

        LogUtil.i(TAG, msg.toString())

        if (logCache.isNotEmpty()) {
            logCache.append("\n")
        }
        if (logCache.length > 1000) {
            logCache.delete(0, 500)
        }
        logCache.append(TimeUtils.getNowString())
        logCache.append("\n")
        logCache.append(msg)
        CoroutineWrapper.launch {
            logAppendValue.emit(Pair("\n${TimeUtils.getNowString()}\n$msg", logCache.toString()))
        }
        return msg.toString()
    }

    fun clearLog() {
        logCache = StringBuilder("")
        CoroutineWrapper.launch { logAppendValue.emit(Pair("", "")) }
    }

}