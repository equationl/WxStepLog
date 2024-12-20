package com.equationl.wxsteplog.db

import com.equationl.wxsteplog.App
import com.equationl.wxsteplog.model.LogModel
import com.equationl.wxsteplog.util.DateTimeUtil.formatDateTime

object DbUtil {
    val db by lazy { WxStepDB.create(App.instance) }

    suspend fun saveData(stepNum: Int?, likeNum: Int?, userName: String, userOrder: Int?, logUserMode: LogModel?) {
        val currentTime = System.currentTimeMillis()

        db.manHoursDB().insertData(
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
}