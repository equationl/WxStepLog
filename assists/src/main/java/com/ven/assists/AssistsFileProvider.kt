package com.ven.assists

import android.app.Application
import androidx.core.content.FileProvider

class AssistsFileProvider : FileProvider() {
    override fun onCreate(): Boolean {
        val applicationContext = context?.applicationContext
        if (applicationContext is Application) {
            Assists.init(applicationContext)
        }
        return super.onCreate()
    }
}