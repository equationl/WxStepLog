package com.equationl.wxsteplog.util

import com.equationl.wxsteplog.ui.view.statistics.state.StatisticsShowRange
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeUtil {
    const val DAY_MILL_SECOND_TIME = 86400_000L
    const val HOUR_MILL_SECOND_TIME = 3600_000L
    const val MINUTE_MILL_SECOND_TIME = 60_000L
    const val SECOND_MILL_SECOND_TIME = 1_000L

    private val timeWithHalfHours = listOf(
        "00:00", "00:30", "01:00", "01:30", "02:00", "02:30", "03:00",
        "03:30", "04:00", "04:30", "05:00", "05:30", "06:00", "06:30",
        "07:00", "07:30", "08:00", "08:30", "09:00", "09:30", "10:00",
        "10:30", "11:00", "11:30", "12:00", "12:30", "13:00", "13:30",
        "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00",
        "17:30", "18:00", "18:30", "19:00", "19:30", "20:00", "20:30",
        "21:00", "21:30", "22:00", "22:30", "23:00", "23:30", "24:00",
    )

    fun Long.formatDateTime(format: String = "yyyy-MM-dd HH:mm:ss", local: Locale = Locale.getDefault()): String {
        val sDateFormat = SimpleDateFormat(format, local)
        return sDateFormat.format(Date(this))
    }

    fun Long.toWeekday(): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this

        val weekdayNumber = calendar.get(Calendar.DAY_OF_WEEK)
        return when (weekdayNumber) {
            Calendar.SUNDAY -> "星期日"
            Calendar.MONDAY -> "星期一"
            Calendar.TUESDAY -> "星期二"
            Calendar.WEDNESDAY -> "星期三"
            Calendar.THURSDAY -> "星期四"
            Calendar.FRIDAY -> "星期五"
            Calendar.SATURDAY -> "星期六"
            else -> "未知"
        }
    }

    fun String.toTimestamp(format: String = "yyyy-MM-dd HH:mm:ss", local: Locale = Locale.getDefault(), isWithoutTimeZone: Boolean = false): Long {
        val sdf = SimpleDateFormat(format, local)
        if (isWithoutTimeZone) {
            sdf.timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = sdf.parse(this)
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

    fun getWeeOfYesterday(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -1)
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

    fun getTimeFromSecond(millisecond: Int): String {
        val minutes = millisecond / 60
        val hoursValue = minutes / 60
        val minutesValue = minutes % 60

        return "${hoursValue.toString().padStart(2, '0')}:${minutesValue.toString().padStart(2, '0')}"
    }

    fun getTimeFromHalfHourIndex(index: Int): String {
        return timeWithHalfHours[index]
    }

    /**
     *
     * 返回从聊天消息列表中解析出来的时间
     *
     * 仅解析到日期，时间信息将被舍去。
     *
     * 输入的 [dateTimeText] 可能具有以下几种形式：
     *
     * 0 -> "14:27"
     * 1 -> "昨天 22:40"
     * 2 -> "1月5日 晚上22:40"
     * 3 -> "2024年12月30日 下午17:21"
     * */
    fun getTimeFromMsgListHeader(dateTimeText: String): Long {
        val textList = dateTimeText.split(" ")
        if (textList.isEmpty()) return 0
        if (textList.size == 1) { // 情况 0
            return getWeeOfToday()
        }
        else {
            val firstItem = textList.first()

            return if (firstItem.contains("昨天")) { // 情况 1
                getWeeOfYesterday()
            } else {
                if (firstItem.contains("年")) { // 情况 3
                    firstItem.toTimestamp("yyyy年M月d日")
                } else { // 情况 2
                    "${System.currentTimeMillis().formatDateTime("yyyy年")}$firstItem".toTimestamp("yyyy年M月d日")
                }
            }
        }
    }

}