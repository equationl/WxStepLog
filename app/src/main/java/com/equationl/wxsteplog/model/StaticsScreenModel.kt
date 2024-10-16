package com.equationl.wxsteplog.model

data class StaticsScreenModel(
    val id: Int,
    val logTime: Long,
    val logTimeString: String,
    val stepNum: Int,
    val likeNum: Int,
    val headerTitle: String
)
