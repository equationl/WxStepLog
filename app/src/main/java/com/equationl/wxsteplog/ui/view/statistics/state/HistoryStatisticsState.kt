package com.equationl.wxsteplog.ui.view.statistics.state

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.graphics.Color
import com.equationl.wxsteplog.model.StaticsScreenModel
import com.equationl.wxsteplog.model.StepHistoryLogStartTimeDbModel
import com.equationl.wxsteplog.util.DateTimeUtil

data class HistoryStatisticsState(
    val isLoading: Boolean = true,
    val logItemList: List<HistoryLogItemModel> = listOf(),
    val dataList: List<StaticsScreenModel> = listOf(),
    /** {"user": [StatisticsHistoryChartData]} */
    val chartData: Map<String, StatisticsHistoryChartData> = mapOf(),
    val showType: StatisticsShowType = StatisticsShowType.List,
    val listState: LazyListState = LazyListState(),
    val filter: HistoryStatisticsFilter = HistoryStatisticsFilter(),
    val userNameList: List<String> = listOf(),
    val detailId: Long? = null
)

data class HistoryStatisticsFilter(
     /** 数据展示方式 */
    val dataShowType: HistoryDataShowType = HistoryDataShowType.ByLog,
     /** 读取的数据日期范围 */
     val showRange: StatisticsShowRange = DateTimeUtil.getCurrentDayRange(), // 默认今天
     /** 筛选指定用户， null 表示不筛选*/
    val user: String? = null,
     /** 是否筛选指定用户 */
    val isFilterUser: Boolean = false,
)

data class HistoryLogItemModel (
    val id: Long,
    val title: String,
    val subTitle: String,
    val count: Int,
    val rawData: StepHistoryLogStartTimeDbModel
)

data class StatisticsHistoryChartData(
    val x: MutableList<Number>,
    val y: MutableList<Number>,
    /** X 轴标签（简短） */
    val xLabelShort: MutableList<String>,
    /** X 轴标签（完整） */
    val xLabelFull: MutableList<String>,
    /** 图例标题 */
    val label: String,
    /** 线条颜色 */
    val color: Color,
)

enum class HistoryDataShowType {
    /** 按记录时间分组 */
    ByLog,
    /** 不分组，直接展示完整数据 */
    ByAllData
}
