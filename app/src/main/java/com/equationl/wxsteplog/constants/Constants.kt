package com.equationl.wxsteplog.constants

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Rect

object Constants {
    const val WX_HISTORY_LOG_DATA_CSV_HEADER = "id,userName,stepNum,likeNum,userOrder,logStartTime,logEndTime,dataTime,dataTimeString,logModel\n"
    const val WX_LOG_DATA_CSV_HEADER = "id, userName, stepNum, likeNum, logTimeString, logTime, userOrder, logModel\n"

    const val WX_HISTORY_LOG_DATA_ANALYZE_CSV_HEADER = "名称,步数,点赞数,好友排名,日期\n"
    const val WX_LOG_DATA_ANALYZE_CSV_HEADER = "名称,步数,点赞数,好友排名,日期时间\n"

    const val GITHUB_HOME_PAGE = "https://github.com/equationl/WxStepLog"

    val allUserNameList = mutableStateListOf<String>()
    var isExportWithFilter = false
    var showDataFilterUserName: String = ""

    val wxPkgName = mutableStateOf("com.tencent.mm")
    val wxLauncherPkg = mutableStateOf("com.tencent.mm.ui.LauncherUI")
    val runStepIntervalTime = mutableIntStateOf(1000)
    val showDetailLog = mutableStateOf(true)
    val csvDelimiter = mutableStateOf(",")
    /**
     * “微信” view 坐标限定，以 1920*1080 为基准
     * */
    val wxViewLimit = mutableStateOf(Rect(left = -1f, top = 1850f, right = 270f, bottom = -1f))
    /**
     * “步数排行榜” view 坐标限定，以 1920*1080 为基准
     * */
    val stepOrderLimit = mutableStateOf(Rect(left = -1f, top = 1800f, right = -1f, bottom = -1f))

    val functionList = listOf("读取历史记录（单次读取）", "记录实时数据（连续运行）", "AI分析数据")

    var logWxHistoryStepStartTime: Long = 0
}