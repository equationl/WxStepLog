package com.equationl.wxsteplog.util

import android.util.Log
import com.equationl.wxsteplog.db.WxStepDB
import com.equationl.wxsteplog.db.WxStepTable
import com.equationl.wxsteplog.model.LogModel
import com.equationl.wxsteplog.model.StaticsScreenModel
import com.equationl.wxsteplog.ui.view.statistics.state.StatisticsChartData
import com.equationl.wxsteplog.util.DateTimeUtil.formatDateTime
import com.equationl.wxsteplog.util.DateTimeUtil.toTimestamp
import com.equationl.wxsteplog.util.DateTimeUtil.toWeekday

object ResolveDataUtil {
    private const val TAG = "ResolveDataUtil"

    fun rawDataToStaticsModel(
        rawDataList: List<WxStepTable>,
        isFoldData: Boolean,
    ): List<StaticsScreenModel> {
        val result = mutableListOf<StaticsScreenModel>()
        /** {"user", Pair(stepNum, likeNum)} */
        val lastDataMap = mutableMapOf<String, Pair<Int?, Int?>>()

        for (item in rawDataList) {
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

        return result
    }

    fun resolveChartData(resolveResult: List<StaticsScreenModel>): Map<String, List<StatisticsChartData>> {
        val charList = mutableMapOf<String, MutableList<StatisticsChartData>>()
        for (item in resolveResult) {
            val currentData = charList[item.userName] ?: mutableListOf()
            var lineData = currentData.find { it.label.split(" ").first() == item.headerTitle }
            if (lineData == null) {
                lineData = StatisticsChartData(
                    MutableList(49) { index: Int -> index },
                    MutableList(49) { _ -> 0 },
                    "${item.headerTitle} ${item.logTime.toWeekday()}",
                    Utils.getRandomColor(item.headerTitle.toTimestamp("yyyy-MM-dd")),
                )
                currentData.add(lineData)
            }

            val onlyTimeStamp = item.logTime.formatDateTime("HHmm").toTimestamp("HHmm", isWithoutTimeZone = true)
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

            // 版本 1 导出数据只有 6 列
            // 版本 2 导出数据增加了两列可空数据：当前排名、记录模式
            if (itemList.size !=  6 && itemList.size != 8) {
                Log.w(TAG, "importDataFromCsv: line data size not right: $itemList")
                hasConflict = true
                continue
            }

            try {
                val wxStepTable = WxStepTable(
                    userName = itemList[1],
                    stepNum = itemList[2].toIntOrNull(),
                    likeNum = itemList[3].toIntOrNull(),
                    logTime = itemList[5].toLongOrNull() ?: 0,
                    logTimeString = itemList[4],
                    userOrder = itemList.getOrNull(6)?.toIntOrNull(),
                    logModel = "${itemList.getOrNull(7) ?: ""},${LogModel.Import}"
                )
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