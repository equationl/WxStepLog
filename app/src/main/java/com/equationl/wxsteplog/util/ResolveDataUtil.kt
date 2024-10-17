package com.equationl.wxsteplog.util

import com.equationl.wxsteplog.db.WxStepTable
import com.equationl.wxsteplog.model.StaticsScreenModel
import com.equationl.wxsteplog.util.DateTimeUtil.formatDateTime

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
}