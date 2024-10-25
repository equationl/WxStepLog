package com.equationl.wxsteplog.model

data class WxStepLogSetting(
    val userNameList: List<String>,
    val logUserMode: LogSettingMode,
    val intervalTime: Long,
    val isRandomInterval: Boolean,
    val randomIntervalValue: Long,
)
