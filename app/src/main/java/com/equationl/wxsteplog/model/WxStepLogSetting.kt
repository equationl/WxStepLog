package com.equationl.wxsteplog.model

data class WxStepLogSetting(
    val userNameList: List<String>,
    val logUserMode: LogSettingMode,
    val intervalTime: Long,
    val isRandomInterval: Boolean,
    val randomIntervalValue: Long,
    val isAllModelSpecialUser: Boolean,
    val isAutoRunning: Boolean,
    val isAutoReset: Boolean,
    /** <停止时间(min),恢复时间(min)> */
    val restTime: Pair<Int, Int?>
)
