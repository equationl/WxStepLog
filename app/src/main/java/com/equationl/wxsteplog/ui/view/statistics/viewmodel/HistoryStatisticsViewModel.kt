package com.equationl.wxsteplog.ui.view.statistics.viewmodel

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.icu.text.Collator
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.wxsteplog.constants.Constants
import com.equationl.wxsteplog.db.DbUtil.DATABASE_FILE_NAME
import com.equationl.wxsteplog.db.WxStepDB
import com.equationl.wxsteplog.db.WxStepHistoryTable
import com.equationl.wxsteplog.model.StaticsScreenModel
import com.equationl.wxsteplog.ui.view.statistics.state.HistoryDataShowType
import com.equationl.wxsteplog.ui.view.statistics.state.HistoryLogItemModel
import com.equationl.wxsteplog.ui.view.statistics.state.HistoryStatisticsFilter
import com.equationl.wxsteplog.ui.view.statistics.state.HistoryStatisticsState
import com.equationl.wxsteplog.ui.view.statistics.state.StatisticsHistoryChartData
import com.equationl.wxsteplog.ui.view.statistics.state.StatisticsShowRange
import com.equationl.wxsteplog.ui.view.statistics.state.StatisticsShowType
import com.equationl.wxsteplog.util.CsvUtil
import com.equationl.wxsteplog.util.DateTimeUtil
import com.equationl.wxsteplog.util.DateTimeUtil.formatDateTime
import com.equationl.wxsteplog.util.ResolveDataUtil
import com.equationl.wxsteplog.util.Utils
import com.equationl.wxsteplog.util.log.LogUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class HistoryStatisticsViewModel @Inject constructor(
    private val db: WxStepDB
): ViewModel() {


    private val _uiState = MutableStateFlow(
        HistoryStatisticsState()
    )

    val uiState = _uiState.asStateFlow()

    fun init() {
        viewModelScope.launch {
            loadData(isFirstLoading = true)
        }
    }

    fun onFilterShowRange(value: StatisticsShowRange) {
        _uiState.update {
            it.copy(
                filter = it.filter.copy(showRange = value),
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
            putExtra(Intent.EXTRA_TITLE, "wxStepHistoryLog_${System.currentTimeMillis().formatDateTime("yyyy_MM_dd_HH_mm_ss")}.csv")
        }
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        return intent
    }

    fun createNewDatabaseFileIntent(): Intent {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_TITLE, "wxStepLog_${System.currentTimeMillis().formatDateTime("yyyy_MM_dd_HH_mm_ss")}.db")
        }
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        return intent
    }

    fun createReadDocumentIntent(): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/comma-separated-values"

            // putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        return intent
    }

    fun exportData(
        result: ActivityResult,
        context: Context,
        filter: HistoryStatisticsFilter?,
        detailId: Long?,
        onFinish: () -> Unit,
        onProgress: (progress: Float) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = result.data
            val uri = data?.data
            if (uri == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "导出错误：uri is null", Toast.LENGTH_SHORT).show()
                    onFinish()
                }

                return@launch
            }

            val outputStream = context.contentResolver.openOutputStream(uri)
            if (outputStream == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "导出错误：outputStream is null", Toast.LENGTH_SHORT).show()
                    onFinish()
                }

                return@launch
            }

            LogUtil.i("el", "export with filter: $filter")
            val dataList = if (filter == null) {
                if (detailId == null) {
                    db.wxStepHistoryDB().queryAllData()
                }
                else {
                    db.wxStepHistoryDB().queryAllDataByLogStartTime(detailId)
                }
            } else {
                val userName = if (filter.isFilterUser && filter.user != null) filter.user else "%"
                if (detailId == null) {
                    db.wxStepHistoryDB().queryRangeDataList(_uiState.value.filter.showRange.start, _uiState.value.filter.showRange.end, userName)
                }
                else {
                    db.wxStepHistoryDB().queryRangeDataListByLogStartTime(_uiState.value.filter.showRange.start, _uiState.value.filter.showRange.end, detailId, userName)
                }
            }

            // 表头
            outputStream.write(Constants.WX_HISTORY_LOG_DATA_CSV_HEADER.toByteArray())

            dataList.forEachIndexed { index, row ->
                outputStream.write(CsvUtil.encodeCsvLineByte(row.id,row.userName,row.stepNum,row.likeNum,row.userOrder,row.logStartTime,row.logEndTime,row.dataTime,row.dataTimeString,row.logModel))
                onProgress((index + 1).toFloat() / dataList.size)
            }
            outputStream.flush()
            outputStream.close()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "导出完成！", Toast.LENGTH_SHORT).show()
                onFinish()
            }
        }
    }

    fun exportDataToDb(
        result: ActivityResult,
        context: Context,
        onFinish: () -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = result.data
            val uri = data?.data
            if (uri == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "导出错误：uri is null", Toast.LENGTH_SHORT).show()
                    onFinish()
                }

                return@launch
            }

            val outputStream = context.contentResolver.openOutputStream(uri)
            if (outputStream == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "导出错误：outputStream is null", Toast.LENGTH_SHORT).show()
                    onFinish()
                }

                return@launch
            }

            val dataBaseFile = context.getDatabasePath(DATABASE_FILE_NAME)
            outputStream.write(dataBaseFile.readBytes())

            outputStream.flush()
            outputStream.close()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "导出完成！", Toast.LENGTH_SHORT).show()
                onFinish()
            }
        }
    }

    fun onImport(
        result: ActivityResult,
        context: Context,
        onFinish: () -> Unit,
        onProgress: (readLines: Int) -> Unit
    ) {
        val data = result.data
        val uri = data?.data

        var importResult: Result<Boolean>

        if (uri == null) {
            Toast.makeText(context, "导入错误：uri is null", Toast.LENGTH_SHORT).show()
            onFinish()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val buffer = context.contentResolver.openInputStream(uri)?.bufferedReader()

            if (buffer == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "导入错误：buffer is null", Toast.LENGTH_SHORT).show()
                    onFinish()
                }
                return@launch
            }

            buffer.useLines {
                importResult = ResolveDataUtil.importHistoryDataFromCsv(it, db, onProgress)
            }

            withContext(Dispatchers.Main) {
                importResult.fold(
                    onSuccess = {
                        if (it) {
                            Toast.makeText(context, "导入完成，但是部分数据由于某些错误没有导入", Toast.LENGTH_LONG).show()
                            onFinish()
                        }
                        else {
                            Toast.makeText(context, "导入成功", Toast.LENGTH_SHORT).show()
                            onFinish()
                        }
                    },
                    onFailure = {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                        onFinish()
                    }
                )
            }

            loadData()
        }

    }

    fun onChangeFilter(newFilter: HistoryStatisticsFilter) {
        _uiState.update { it.copy(filter = newFilter) }

        viewModelScope.launch {
            loadData()
        }
    }

    fun onCloseShowDetail() {
        _uiState.update {
            it.copy(
                detailId = null,
                filter = it.filter.copy(showRange = DateTimeUtil.getCurrentDayRange())
            )
        }

        viewModelScope.launch {
            loadData()
        }
    }

    fun onClickHistoryLogItem(item: HistoryLogItemModel) {
        _uiState.update {
            it.copy(
                detailId = item.id,
                filter = it.filter.copy(
                    showRange = StatisticsShowRange(
                        item.rawData.startTime,
                        item.rawData.endTime
                    )
                )
            )
        }

        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData(isFirstLoading: Boolean = false) = withContext(Dispatchers.IO) {
        _uiState.update { it.copy(isLoading = true) }
        val dao = db.wxStepHistoryDB()

        val userList = dao.getCurrentUserList().sortedWith { o1, o2 -> Collator.getInstance(Locale.CHINESE).compare(o1, o2) }

        var filter = _uiState.value.filter
        if (isFirstLoading && (filter.user == null || !filter.isFilterUser)) { // 默认筛选第一个用户
            filter = filter.copy(user = userList.firstOrNull(), isFilterUser = true)
            _uiState.update { it.copy(filter = filter) }
        }

        if (_uiState.value.detailId != null || _uiState.value.filter.dataShowType == HistoryDataShowType.ByAllData) { // 获取全部数据
            // 需要按时区偏移一下
            val offset = 0 //TimeZone.getDefault().rawOffset
            val filterUserName = if (filter.isFilterUser && filter.user != null) filter.user!! else "%"
            val rawDataList = if (_uiState.value.detailId != null)
                dao.queryRangeDataListByLogStartTime(
                    startTime = _uiState.value.filter.showRange.start - offset,
                    endTime = _uiState.value.filter.showRange.end - offset,
                    logStartTime = _uiState.value.detailId!!,
                    userName = filterUserName
                )
            else
                dao.queryRangeDataList(
                    startTime = _uiState.value.filter.showRange.start - offset,
                    endTime = _uiState.value.filter.showRange.end - offset,
                    userName = filterUserName
                )

            val resolveResult = resolveData(rawDataList)

            var charList = mapOf<String, StatisticsHistoryChartData>()
            if (_uiState.value.showType == StatisticsShowType.Chart) {
                charList = ResolveDataUtil.resolveHistoryChartData(resolveResult)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    dataList = resolveResult,
                    chartData = charList,
                    userNameList = userList
                )
            }
        }
        else { // 获取按记录时间的数据
            val logStartTimeList = dao.getLogStartTimeList()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    logItemList = logStartTimeList.map { item ->
                        HistoryLogItemModel(
                            id = item.logStartTime,
                            title = item.logStartTime.formatDateTime(),
                            subTitle = "${item.startTime.formatDateTime("yyyy-MM-dd")} - ${item.endTime.formatDateTime("yyyy-MM-dd")}",
                            count = item.count,
                            rawData = item
                        )
                    }
                )
            }
        }
    }

    private fun resolveData(rawDataList: List<WxStepHistoryTable>): List<StaticsScreenModel> {
        return ResolveDataUtil.rawHistoryDataToStaticsModel(rawDataList, _uiState.value.detailId != null)
    }
}