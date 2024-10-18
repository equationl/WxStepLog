package com.equationl.wxsteplog.ui.view.statistics.state

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.graphics.Color
import com.equationl.wxsteplog.model.StaticsScreenModel
import com.equationl.wxsteplog.util.DateTimeUtil

data class StatisticsState(
    val isLoading: Boolean = true,
    val dataList: List<StaticsScreenModel> = listOf(),
    /** {"user": list} */
    val chartData: Map<String, List<StatisticsChartData>> = mapOf(),
    val showRange: StatisticsShowRange = DateTimeUtil.getCurrentDayRange(), // 默认今天
    val showType: StatisticsShowType = StatisticsShowType.List,
    val listState: LazyListState = LazyListState(),
    val filter: StatisticsFilter = StatisticsFilter(),
    val userNameList: List<String> = listOf()
)

data class StatisticsShowRange(
    val start: Long = 0L,
    val end: Long = 0L
)

data class StatisticsFilter(
     /** 是否折叠未改变的数据 */
    val isFoldData: Boolean = true,
     /** 筛选指定用户， null 表示不筛选*/
    val user: String? = null,
)

data class StatisticsChartData(
    val x: MutableList<Number>,
    val y: MutableList<Number>,
    /** 图例标题 */
    val label: String,
    /** 线条颜色 */
    val color: Color,
)

enum class StatisticsShowType {
    List,
    Chart
}