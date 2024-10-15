package com.equationl.wxsteplog.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtil {

    fun Long.formatDateTime(format: String = "yyyy-MM-dd HH:mm:ss"): String {
        val sDateFormat = SimpleDateFormat(format, Locale.getDefault())
        return sDateFormat.format(Date(this))
    }

    fun String.toTimestamp(format: String = "yyyy-MM-dd HH:mm:ss"): Long {
        val date = SimpleDateFormat(format, Locale.getDefault()).parse(this)
        return date?.time ?: 0L
    }

}