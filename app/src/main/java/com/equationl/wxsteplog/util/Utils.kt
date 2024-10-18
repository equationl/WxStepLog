package com.equationl.wxsteplog.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

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

    fun getRandomColor(seed: Long): Color {
        val random = Random(seed)
        return Color(
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        )
    }

    fun SpannableStringBuilder.appendCompat(
        text: CharSequence,
        what: Any,
        flags: Int,
    ): SpannableStringBuilder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            append(text, what, flags)
        } else {
            append(text, 0, text.length)
            setSpan(what, length - text.length, length, flags)
            this
        }
}