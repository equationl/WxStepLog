package com.equationl.wxsteplog

import android.app.Application
import com.equationl.wxsteplog.util.DefaultCrashCatchHandler
import com.equationl.wxsteplog.util.datastore.DataStoreUtils
import com.equationl.wxsteplog.util.log.LogUtil
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {

    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        DataStoreUtils.init(this)
        LogUtil.init(this, isLog = true)
        // 初始化错误捕捉
        DefaultCrashCatchHandler().init()
    }
}