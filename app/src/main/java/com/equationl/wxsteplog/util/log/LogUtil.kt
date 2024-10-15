package com.equationl.wxsteplog.util.log

import android.content.Context
import com.blankj.utilcode.BuildConfig

object LogUtil {
    private var isLog = BuildConfig.DEBUG

    /**
     * 初始化日志
     *
     * @param isLog 是否启用日志，默认为仅在 Debug 模式启用
     * */
    fun init(context: Context, isLog: Boolean = BuildConfig.DEBUG) {
        LogUtil.isLog = isLog
        Logger.instance.init(context)
    }

    /**
     *
     * @param msg 日志内容
     * @param tag 自定义 tag
     * @param tag2 页面堆栈
     * @param printLevel 输出级别
     * */
    fun d(tag: String?, msg: String?, printLevel: Logger.PrintLevel = Logger.PrintLevel.PRINT) {
        if (!isLog) return

        Logger.instance.log(tag = tag ?: " ", msg = msg ?: " ", printLevel = printLevel, level = Logger.LogLevel.DEBUG)
    }

    /**
     *
     * @param msg 日志内容
     * @param tag 自定义 tag
     * @param tag2 页面堆栈
     * @param printLevel 输出级别
     * */
    fun i(tag: String, msg: String, printLevel: Logger.PrintLevel = Logger.PrintLevel.WRITE) {
        if (!isLog) return

        Logger.instance.log(tag = tag, msg = msg, printLevel = printLevel, level = Logger.LogLevel.INFO)
    }

    /**
     *
     * @param msg 日志内容
     * @param tag 自定义 tag
     * @param tag2 页面堆栈
     * @param printLevel 输出级别
     * @param tr 错误堆栈
     * */
    fun w(tag: String, msg: String, tr: Throwable? = null, printLevel: Logger.PrintLevel = Logger.PrintLevel.POST) {
        if (!isLog) return

        Logger.instance.log(tag = tag, msg = msg, printLevel = printLevel, level = Logger.LogLevel.WARN, tr = tr)
    }

    /**
     *
     * @param msg 日志内容
     * @param tag 自定义 tag
     * @param tag2 页面堆栈
     * @param printLevel 输出级别
     * @param tr 错误堆栈
     * */
    fun e(tag: String, msg: String, tr: Throwable? = null, printLevel: Logger.PrintLevel = Logger.PrintLevel.POST) {
        if (!isLog) return

        Logger.instance.log(tag = tag, msg = msg, printLevel = printLevel, level = Logger.LogLevel.ERROR, tr = tr)
    }
}