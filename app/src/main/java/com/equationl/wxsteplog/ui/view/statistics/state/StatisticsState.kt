package com.equationl.wxsteplog.ui.view.statistics.state

import androidx.compose.foundation.lazy.LazyListState
import com.equationl.wxsteplog.model.StaticsScreenModel
import com.equationl.wxsteplog.util.DateTimeUtil

data class StatisticsState(
    val isLoading: Boolean = true,
    val isFoldData: Boolean = false, // TODO 是否折叠未改变的数据
    val dataList: List<StaticsScreenModel> = listOf(),
    val chartData: List<Any> = listOf(),
    val showRange: StatisticsShowRange = DateTimeUtil.getCurrentDayRange(), // 默认今天
    val showType: StatisticsShowType = StatisticsShowType.List,
    val listState: LazyListState = LazyListState()
)

data class StatisticsShowRange(
    val start: Long = 0L,
    val end: Long = 0L
)

enum class StatisticsShowType {
    List,
    Chart
}