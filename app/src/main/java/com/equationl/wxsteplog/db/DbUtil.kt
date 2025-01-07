package com.equationl.wxsteplog.db

import com.equationl.wxsteplog.App
import com.equationl.wxsteplog.model.LogModel
import com.equationl.wxsteplog.util.DateTimeUtil.formatDateTime

object DbUtil {
    const val DATABASE_FILE_NAME = "wx_step_data.db"

    val db by lazy { WxStepDB.create(App.instance) }

    suspend fun saveData(stepNum: Int?, likeNum: Int?, userName: String, userOrder: Int?, logUserMode: LogModel?) {
        val currentTime = System.currentTimeMillis()

        db.wxStepDB().insertData(
            WxStepTable(
                userName = userName,
                stepNum = stepNum,
                likeNum = likeNum,
                logTime = currentTime,
                logTimeString = currentTime.formatDateTime(),
                userOrder = userOrder,
                logModel = logUserMode?.name
            )
        )
    }

    suspend fun saveHistoryData(stepNum: Int?, likeNum: Int?, userName: String, userOrder: Int?, logStartTime: Long, dataTime: Long) {
        val currentTime = System.currentTimeMillis()

        db.wxStepHistoryDB().insertData(
            WxStepHistoryTable(
                userName = userName,
                stepNum = stepNum,
                likeNum = likeNum,
                userOrder = userOrder,
                logStartTime = logStartTime,
                logEndTime = currentTime,
                dataTime = dataTime,
                dataTimeString = dataTime.formatDateTime(),
            )
        )
    }
}