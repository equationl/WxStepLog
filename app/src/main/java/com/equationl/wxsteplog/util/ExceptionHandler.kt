package com.equationl.wxsteplog.util

import android.util.Log
import android.widget.Toast
import com.equationl.wxsteplog.App
import com.equationl.wxsteplog.util.log.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DefaultCrashCatchHandler: Thread.UncaughtExceptionHandler {
    private val TAG = "CrashCatchHandler"

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            LogUtil.e(TAG, "uncaughtException: 捕获到未处理的异常： ${thread.name}", throwable)

            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(App.instance, throwable.message, Toast.LENGTH_LONG).show()
            }
        } catch (tr: Throwable) {
            Log.e(TAG, "uncaughtException: ", tr)
        }
    }

    fun init() {
        //把当前的crash捕获器设置成默认的crash捕获器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

}