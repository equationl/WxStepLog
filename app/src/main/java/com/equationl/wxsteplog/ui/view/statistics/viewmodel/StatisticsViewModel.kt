package com.equationl.wxsteplog.ui.view.statistics.viewmodel

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.wxsteplog.db.WxStepDB
import com.equationl.wxsteplog.db.WxStepTable
import com.equationl.wxsteplog.model.StaticsScreenModel
import com.equationl.wxsteplog.ui.view.statistics.state.StatisticsChartData
import com.equationl.wxsteplog.ui.view.statistics.state.StatisticsFilter
import com.equationl.wxsteplog.ui.view.statistics.state.StatisticsShowRange
import com.equationl.wxsteplog.ui.view.statistics.state.StatisticsShowType
import com.equationl.wxsteplog.ui.view.statistics.state.StatisticsState
import com.equationl.wxsteplog.util.DateTimeUtil.formatDateTime
import com.equationl.wxsteplog.util.ResolveDataUtil
import com.equationl.wxsteplog.util.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val db: WxStepDB
): ViewModel() {


    private val _uiState = MutableStateFlow(
        StatisticsState()
    )

    val uiState = _uiState.asStateFlow()

    fun init() {
        viewModelScope.launch {
            loadData()
        }
    }

    fun onFilterShowRange(value: StatisticsShowRange) {
        _uiState.update {
            it.copy(
                showRange = value
            )
        }
        viewModelScope.launch {
            loadData()
        }
    }

    fun onChangeShowType(context: Context) {
        _uiState.update {
            it.copy(
                showType = if (it.showType == StatisticsShowType.Chart) StatisticsShowType.List else StatisticsShowType.Chart,
            )
        }

        if (_uiState.value.showType == StatisticsShowType.Chart) {
            Utils.changeScreenOrientation(context, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        }
        else {
            Utils.changeScreenOrientation(context, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        }

        viewModelScope.launch {
            loadData()
        }
    }

    fun createNewDocumentIntent(): Intent {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/comma-separated-values"
            putExtra(Intent.EXTRA_TITLE, "wxStepLog_${System.currentTimeMillis().formatDateTime("yyyy_MM_dd_HH_mm_ss")}.csv")
        }
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        return intent
    }

    suspend fun exportData(result: ActivityResult, context: Context) {
        val data = result.data
        val uri = data?.data
        uri?.let {
            context.contentResolver.openOutputStream(it)?.let { outputStream ->
                val dataList = db.manHoursDB().queryAllData()
                for (row in dataList) {
                    outputStream.write("${row.id},${row.userName},${row.stepNum},${row.likeNum},${row.logTimeString},${row.logTime}\n".toByteArray())
                }
                outputStream.flush()
                outputStream.close()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "导出完成！", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun onChangeFilter(newFilter: StatisticsFilter) {
        _uiState.update { it.copy(filter = newFilter) }

        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() = withContext(Dispatchers.IO) {
        _uiState.update { it.copy(isLoading = true) }

        val rawDataList = db.manHoursDB().queryRangeDataList(_uiState.value.showRange.start, _uiState.value.showRange.end, 1, Int.MAX_VALUE)
        var filter = _uiState.value.filter

        // 统计图需要平滑数据，所以不能折叠
        if (_uiState.value.showType == StatisticsShowType.Chart) {
            filter = filter.copy(isFoldData = false)
        }

        val resolveResult = resolveData(rawDataList, filter)

        var charList = mapOf<String, List<StatisticsChartData>>()
        if (_uiState.value.showType == StatisticsShowType.Chart) {
            charList = ResolveDataUtil.resolveChartData(resolveResult)
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                dataList = resolveResult,
                chartData = charList,
            )
        }
    }

    private fun resolveData(rawDataList: List<WxStepTable>, filter: StatisticsFilter): List<StaticsScreenModel> {
        return ResolveDataUtil.rawDataToStaticsModel(rawDataList, filter.isFoldData, filter.user)
    }
}