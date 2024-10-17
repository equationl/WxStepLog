package com.equationl.wxsteplog.util

import android.util.Log
import com.equationl.wxsteplog.db.WxStepTable
import com.equationl.wxsteplog.model.StaticsScreenModel
import com.equationl.wxsteplog.ui.view.statistics.state.StatisticsChartData
import com.equationl.wxsteplog.util.DateTimeUtil.formatDateTime
import com.equationl.wxsteplog.util.DateTimeUtil.toTimestamp

object ResolveDataUtil {
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

    // TODO
    fun resolveChartData(resolveResult: List<StaticsScreenModel>): Map<String, List<StatisticsChartData>> {
        val charList = mutableMapOf<String, MutableList<StatisticsChartData>>()
        for (item in resolveResult) {
            val currentData = charList[item.userName] ?: mutableListOf()
            var lineData = currentData.find { it.label == item.headerTitle }
            if (lineData == null) {
                lineData = StatisticsChartData(
                    MutableList(48, { index: Int -> index }),
                    MutableList(48, { _ -> 0 }),
                    item.headerTitle,
                    Utils.getRandomColor(item.headerTitle.toTimestamp("yyyy-MM-dd")),
                )
                currentData.add(lineData)
            }

            val onlyTimeStamp = item.logTime.formatDateTime("HHmm").toTimestamp("HHmm")
            Log.i("el", "resolveChartData: onlyTimeStamp = $onlyTimeStamp, logTime = ${item.logTime}, fromat = ${item.logTime.formatDateTime("HHmm")}")
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

        // TODO 如果折叠了数据，需要平滑 Y 轴曲线（或者加载数据时不要折叠数据就行）

        return charList
    }

    fun importDataFromCsv(csvData: String): List<WxStepTable> {
        val result = mutableListOf<WxStepTable>()

        for (line in csvData.lines()) {
            if (line.isBlank()) continue
            val itemList = line.split(",")
            if (itemList.size !=  6) continue
            result.add(
                WxStepTable(userName = itemList[1], stepNum = itemList[2].toIntOrNull(), likeNum = itemList[3].toIntOrNull(), logTime = itemList[5].toLongOrNull() ?: 0, logTimeString = itemList[4])
            )
        }

        return result
    }

    private fun getXValueIndex(timeStamp: Long): Int {
        return (timeStamp / 1000L / 60L / 30L).toInt()
    }
}