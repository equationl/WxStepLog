package com.equationl.wxsteplog.util

import com.equationl.wxsteplog.db.WxStepTable
import com.equationl.wxsteplog.model.StaticsScreenModel

object ResolveDataUtil {
    // TODO
    fun rawDataToStaticsModel(
        isFoldData: Boolean,
        rawDataList: List<WxStepTable>,
    ): List<StaticsScreenModel> {
        val result = mutableListOf<StaticsScreenModel>()

        for (item in rawDataList) {

        }

        return result
    }
}