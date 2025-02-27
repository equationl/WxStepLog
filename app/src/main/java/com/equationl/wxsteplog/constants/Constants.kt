package com.equationl.wxsteplog.constants

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

object Constants {
    const val WX_HISTORY_LOG_DATA_CSV_HEADER = "id,userName,stepNum,likeNum,userOrder,logStartTime,logEndTime,dataTime,dataTimeString,logModel\n"
    const val GITHUB_HOME_PAGE = "https://github.com/equationl/WxStepLog"

    val allUserNameList = mutableStateListOf<String>()
    var isExportWithFilter = false
    var showDataFilterUserName: String = ""

    val wxPkgName = mutableStateOf("com.tencent.mm")
    val wxLauncherPkg = mutableStateOf("com.tencent.mm.ui.LauncherUI")
    val runStepIntervalTime = mutableIntStateOf(1000)
    val showDetailLog = mutableStateOf(true)
    val csvDelimiter = mutableStateOf(",")

    val functionList = listOf("读取历史记录（单次读取）", "记录实时数据（连续运行）", "AI分析历史数据")

    var logWxHistoryStepStartTime: Long = 0
}