package com.equationl.wxsteplog.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log

object Utils {

    fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    fun changeScreenOrientation(context: Context, orientation: Int) {
        Log.i("el", "call changeScreenOrientation with $orientation")

        val activity = context.findActivity() ?: return
        //val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
    }
}