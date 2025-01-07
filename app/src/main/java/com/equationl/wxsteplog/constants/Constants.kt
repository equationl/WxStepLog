package com.equationl.wxsteplog.constants

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

object Constants {
    val allUserNameList = mutableStateListOf<String>()
    var isExportWithFilter = false
    var showDataFilterUserName: String = ""

    val wxPkgName = mutableStateOf("com.tencent.mm")
    val wxLauncherPkg = mutableStateOf("com.tencent.mm.ui.LauncherUI")

    val functionList = listOf("读取历史记录（单次读取）", "记录实时数据（连续运行）")

    var logWxHistoryStepStartTime: Long = 0
}