package com.equationl.wxsteplog.model

import android.util.Log

enum class LogUserMode(val showName: String) {
    /**
     * 指定用户(1个或多个)
     * */
    Multiple("指定"),
    /**
     * 当前所有用户
     * */
    All("全部")
}

fun String.toLogUserMode(): LogUserMode? {
    return try {
        LogUserMode.valueOf(this)
    } catch (tr: Throwable) {
        Log.e("LogUserMode", "toLogUserMode: ", tr)
        null
    }
}