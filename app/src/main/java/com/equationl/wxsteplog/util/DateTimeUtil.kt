package com.equationl.wxsteplog.util

import com.equationl.wxsteplog.ui.view.statistics.state.StatisticsShowRange
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtil {
    const val DAY_MILL_SECOND_TIME = 86400_000L
    const val HOUR_MILL_SECOND_TIME = 3600_000L
    const val MINUTE_MILL_SECOND_TIME = 60_000L
    const val SECOND_MILL_SECOND_TIME = 1_000L

    fun Long.formatDateTime(format: String = "yyyy-MM-dd HH:mm:ss"): String {
        val sDateFormat = SimpleDateFormat(format, Locale.getDefault())
        return sDateFormat.format(Date(this))
    }

    fun String.toTimestamp(format: String = "yyyy-MM-dd HH:mm:ss"): Long {
        val date = SimpleDateFormat(format, Locale.getDefault()).parse(this)
        return (date?.time ?: 0L).coerceAtLeast(0)
    }

    fun getWeeOfToday(): Long {
        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.MILLISECOND] = 0
        return cal.timeInMillis
    }

    fun getCurrentDayRange(): StatisticsShowRange {
        val start = getWeeOfToday()
        val end = start + DAY_MILL_SECOND_TIME
        return StatisticsShowRange(start, end)
    }

}