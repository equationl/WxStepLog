package com.equationl.wxsteplog.util

import android.util.Log
import com.equationl.wxsteplog.db.WxStepDB
import com.equationl.wxsteplog.db.WxStepTable
import com.equationl.wxsteplog.model.StaticsScreenModel
import com.equationl.wxsteplog.ui.view.statistics.state.StatisticsChartData
import com.equationl.wxsteplog.util.DateTimeUtil.formatDateTime
import com.equationl.wxsteplog.util.DateTimeUtil.toTimestamp

object ResolveDataUtil {
    private const val TAG = "ResolveDataUtil"

    fun rawDataToStaticsModel(
        rawDataList: List<WxStepTable>,
        isFoldData: Boolean,
        user: String?
    ): List<StaticsScreenModel> {
        val result = mutableListOf<StaticsScreenModel>()
        /** {"user", Pair(stepNum, likeNum)} */
        val lastDataMap = mutableMapOf<String, Pair<Int?, Int?>>()

        for (item in rawDataList) {
            if (user == null || user == item.userName) {
                if (isFoldData) {
                    val lastData = lastDataMap[item.userName]
                    if (lastData != null && lastData.first == item.stepNum && lastData.second == item.likeNum) {
                        continue
                    }
                }

                result.add(
                    StaticsScreenModel(
                        id = item.id,
                        logTime = item.logTime,
                        logTimeString = item.logTimeString,
                        stepNum = item.stepNum ?: 0,
                        likeNum = item.likeNum ?: 0,
                        headerTitle = item.logTime.formatDateTime("yyyy-MM-dd"),
                        userName = item.userName
                    )
                )
                lastDataMap[item.userName] = Pair(item.stepNum, item.likeNum)
            }
        }

        return result
    }

    fun resolveChartData(resolveResult: List<StaticsScreenModel>): Map<String, List<StatisticsChartData>> {
        val charList = mutableMapOf<String, MutableList<StatisticsChartData>>()
        for (item in resolveResult) {
            val currentData = charList[item.userName] ?: mutableListOf()
            var lineData = currentData.find { it.label == item.headerTitle }
            if (lineData == null) {
                lineData = StatisticsChartData(
                    MutableList(49) { index: Int -> index },
                    MutableList(49) { _ -> 0 },
                    item.headerTitle,
                    Utils.getRandomColor(item.headerTitle.toTimestamp("yyyy-MM-dd")),
                )
                currentData.add(lineData)
            }

            //  TODO 这里返回的是时间戳，所以需要按时区偏移一下，比如 GMT + 8
            val onlyTimeStamp = item.logTime.formatDateTime("HHmm").toTimestamp("HHmm") + 8 * DateTimeUtil.HOUR_MILL_SECOND_TIME
            // Log.i("el", "resolveChartData: onlyTimeStamp = $onlyTimeStamp, logTime = ${item.logTime}, fromat = ${item.logTime.formatDateTime("HHmm")}, step = ${item.stepNum}")
            val index = getXValueIndex(onlyTimeStamp)
            lineData.x[index] = index
            var yValue = lineData.y.getOrNull(index)
            if (yValue != null) {
                if (yValue.toInt() < item.stepNum) {
                    yValue = item.stepNum
                }
            }
            else {
                yValue = item.stepNum
            }
            lineData.y[index] = yValue

            charList[item.userName] = currentData
        }

        return charList
    }

    suspend fun importDataFromCsv(
        csvLines: Sequence<String>,
        db: WxStepDB
    ): Boolean {
        var hasConflict = false

        for (line in csvLines) {
            if (line.isBlank()) {
                Log.w(TAG, "importDataFromCsv: line is blank!")
                continue
            }

            val itemList = line.split(",")

            if (itemList.size !=  6) {
                Log.w(TAG, "importDataFromCsv: line data size not right: $itemList")
                hasConflict = true
                continue
            }

            try {
                val wxStepTable = WxStepTable(userName = itemList[1], stepNum = itemList[2].toIntOrNull(), likeNum = itemList[3].toIntOrNull(), logTime = itemList[5].toLongOrNull() ?: 0, logTimeString = itemList[4])
                val insertResult = db.manHoursDB().insertData(wxStepTable)
                if (insertResult <= 0) {
                    hasConflict = true
                    Log.w(TAG, "importDataFromCsv: insert Data fail, return $insertResult, with data: $wxStepTable")
                }
            } catch (tr: Throwable) {
                Log.e(TAG, "importDataFromCsv: ", tr)
                hasConflict = true
            }
        }

        return hasConflict
    }

    private fun getXValueIndex(timeStamp: Long): Int {
        return (timeStamp / 1000L / 60L / 30L).toInt() + 1
    }
}