package com.equationl.wxsteplog.model

data class StepListIdModel(
    val itemParentId: String,
    val itemOrderId: String,
    val itemNameId: String,
    val itemStepId: String,
    val itemLikeId: String,
    /** 旧版本的微信点赞数量是 TextView，但是新版本不再是 TEXT。因此需要区分*/
    val itemLikeIsText: Boolean
)
