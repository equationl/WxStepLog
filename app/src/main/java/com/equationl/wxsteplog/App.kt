package com.equationl.wxsteplog

import android.app.Application
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
        LogUtil.init(this, isLog = true)
    }
}