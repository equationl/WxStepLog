package com.equationl.wxsteplog.ui.view.aianalysis.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.wxsteplog.constants.Constants
import com.equationl.wxsteplog.db.WxStepDB
import com.equationl.wxsteplog.db.WxStepDao
import com.equationl.wxsteplog.db.WxStepHistoryTable
import com.equationl.wxsteplog.db.WxStepTable
import com.equationl.wxsteplog.model.StepHistoryLogStartTimeDbModel
import com.equationl.wxsteplog.util.CsvUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AiAnalysisViewModel @Inject constructor(
    wxStepDB: WxStepDB,
    private val aiAnalysisService: com.equationl.wxsteplog.aiapi.AiAnalysisInterface
) : ViewModel() {
    private val wxStepHistoryDao = wxStepDB.wxStepHistoryDB()
    private val wxStepDao: WxStepDao = wxStepDB.wxStepDB()

    // 支持的AI模型列表
    val supportedModels = aiAnalysisService.getSupportedModels()
    
    // 选择的模型
    var selectedModel by mutableStateOf(supportedModels.firstOrNull() ?: "")
        private set
    
    // 用户输入的额外提示
    var userPrompt by mutableStateOf("")
        private set
    
    // 数据源类型：历史数据或实时数据
    enum class DataSourceType {
        HISTORY, REALTIME
    }
    
    // 当前选中的数据源类型
    var dataSourceType by mutableStateOf(DataSourceType.REALTIME)
        private set
    
    // 历史数据源列表
    val logHistoryList = mutableStateListOf<StepHistoryLogStartTimeDbModel>()
    var selectedHistoryLogTime by mutableStateOf(0L)
        private set
    
    // 用户列表（历史数据）
    val historyUserList = mutableStateListOf<String>()
    var selectedHistoryUser by mutableStateOf("")
        private set
    
    // 用户列表（实时数据）
    val realtimeUserList = mutableStateListOf<String>()
    var selectedRealtimeUser by mutableStateOf("")
        private set
    
    // 数据选择配置
    var startDate by mutableStateOf(Date())
        private set
    var endDate by mutableStateOf(Date())
        private set
    
    // 是否剔除重复数据（实时数据）
    var removeDuplicates by mutableStateOf(true)
        private set

    // 是否在分析时自动滚动到底部
    var autoScrollToBottom by mutableStateOf(true)
        private set
    
    // 分析状态
    private val _analysisState = MutableStateFlow<com.equationl.wxsteplog.aiapi.AiAnalysisResult?>(null)
    val analysisState: StateFlow<com.equationl.wxsteplog.aiapi.AiAnalysisResult?> = _analysisState
    
    // 选中的数据
    private var selectedHistoryData = listOf<WxStepHistoryTable>()
    private var selectedRealtimeData = listOf<WxStepTable>()
    
    // 是否正在分析
    var isAnalyzing by mutableStateOf(false)
        private set
    
    // 初始化数据
    init {
        loadLogHistoryList()
        loadRealtimeUserList()
    }
    
    /**
     * 切换数据源类型
     */
    fun switchDataSourceType(type: DataSourceType) {
        if (dataSourceType == type) return
        dataSourceType = type
        
        // 根据数据源类型重新加载相关数据
        if (type == DataSourceType.HISTORY) {
            loadLogHistoryList()
        } else {
            loadRealtimeUserList()
        }
    }
    
    /**
     * 加载历史记录列表
     */
    fun loadLogHistoryList() {
        viewModelScope.launch {
            logHistoryList.clear()
            logHistoryList.addAll(wxStepHistoryDao.getLogStartTimeList())
            
            if (logHistoryList.isNotEmpty()) {
                selectedHistoryLogTime = logHistoryList.first().logStartTime
                loadHistoryUserList()
            }
        }
    }
    
    /**
     * 加载历史数据用户列表
     */
    fun loadHistoryUserList() {
        viewModelScope.launch {
            historyUserList.clear()
            historyUserList.add("全部用户") // 添加一个特殊选项表示所有用户
            historyUserList.addAll(wxStepHistoryDao.getCurrentUserList())
            selectedHistoryUser = historyUserList.first()
            
            // 初始化日期范围
            val historyModel = logHistoryList.find { it.logStartTime == selectedHistoryLogTime }
            historyModel?.let {
                startDate = Date(it.startTime ?: 0)
                endDate = Date(it.endTime ?: Date().time)
            }
        }
    }
    
    /**
     * 加载实时数据用户列表
     */
    fun loadRealtimeUserList() {
        viewModelScope.launch {
            realtimeUserList.clear()
            realtimeUserList.add("全部用户") // 添加一个特殊选项表示所有用户
            realtimeUserList.addAll(wxStepDao.getCurrentUserList())
            selectedRealtimeUser = realtimeUserList.first()
            
            // 初始化日期范围为今天和昨天
            val calendar = java.util.Calendar.getInstance()
            endDate = calendar.time
            
            calendar.add(java.util.Calendar.DAY_OF_MONTH, -1)
            startDate = calendar.time
        }
    }
    
    /**
     * 更新选中的历史记录
     */
    fun onHistoryLogSelected(logStartTime: Long) {
        if (selectedHistoryLogTime == logStartTime) return
        
        selectedHistoryLogTime = logStartTime
        
        // 更新日期范围
        val historyModel = logHistoryList.find { it.logStartTime == selectedHistoryLogTime }
        historyModel?.let {
            startDate = Date(it.startTime ?: 0)
            endDate = Date(it.endTime ?: Date().time)
        }
        
        loadHistoryUserList()
    }
    
    /**
     * 更新选中的历史数据用户
     */
    fun onHistoryUserSelected(user: String) {
        selectedHistoryUser = user
    }
    
    /**
     * 更新选中的实时数据用户
     */
    fun onRealtimeUserSelected(user: String) {
        selectedRealtimeUser = user
    }
    
    /**
     * 更新开始日期
     */
    fun onStartDateChanged(date: Date) {
        startDate = date
    }
    
    /**
     * 更新结束日期
     */
    fun onEndDateChanged(date: Date) {
        endDate = date
    }
    
    /**
     * 更新是否剔除重复数据选项
     */
    fun onRemoveDuplicatesChanged(value: Boolean) {
        removeDuplicates = value
    }
    
    /**
     * 更新是否自动滚动到底部选项
     */
    fun onAutoScrollToBottomChanged(value: Boolean) {
        Log.d("AiAnalysis", "自动滚动状态变更: $autoScrollToBottom -> $value")
        autoScrollToBottom = value
    }
    
    /**
     * 更新选中的模型
     */
    fun onModelSelected(model: String) {
        selectedModel = model
    }
    
    /**
     * 更新用户输入的提示
     */
    fun onPromptChanged(prompt: String) {
        userPrompt = prompt
    }
    
    /**
     * 加载选中的历史数据
     */
    private suspend fun loadSelectedHistoryData(): List<WxStepHistoryTable> {
        val userName = if (selectedHistoryUser == "全部用户") "%" else selectedHistoryUser
        
        return if (selectedHistoryLogTime > 0) {
            wxStepHistoryDao.queryRangeDataListByLogStartTime(
                startTime = startDate.time,
                endTime = endDate.time,
                logStartTime = selectedHistoryLogTime,
                userName = userName
            )
        } else {
            wxStepHistoryDao.queryRangeDataList(
                startTime = startDate.time,
                endTime = endDate.time,
                userName = userName
            )
        }
    }
    
    /**
     * 加载选中的实时数据
     */
    private suspend fun loadSelectedRealtimeData(): List<WxStepTable> {
        val userName = if (selectedRealtimeUser == "全部用户") "%" else selectedRealtimeUser
        
        val data = wxStepDao.queryRangeDataListByUserName(
            startTime = startDate.time,
            endTime = endDate.time,
            userName = userName
        )
        
        // 剔除重复数据
        return if (removeDuplicates) {
            val result = mutableListOf<WxStepTable>()
            /** {"user", Pair(stepNum, likeNum)} */
            val lastDataMap = mutableMapOf<String, Pair<Int?, Int?>>()
            
            for (item in data) {
                val lastData = lastDataMap[item.userName]
                if (lastData != null && lastData.first == item.stepNum && lastData.second == item.likeNum) {
                    continue
                }
                
                result.add(item)
                lastDataMap[item.userName] = Pair(item.stepNum, item.likeNum)
            }
            
            result
        } else {
            data
        }
    }
    
    /**
     * 将历史数据转换为CSV格式
     * 使用项目中已有的CSV转换逻辑
     */
    private fun convertHistoryDataToCsv(dataList: List<WxStepHistoryTable>): String {
        val stringBuilder = StringBuilder()
        
        // 添加CSV表头
        stringBuilder.append(Constants.WX_HISTORY_LOG_DATA_CSV_HEADER)
        
        // 添加数据行
        dataList.forEach { data ->
            stringBuilder.append(
                CsvUtil.encodeCsvLineString(
                    data.id,
                    data.userName,
                    data.stepNum,
                    data.likeNum,
                    data.userOrder,
                    data.logStartTime,
                    data.logEndTime,
                    data.dataTime,
                    data.dataTimeString,
                    data.logModel
                )
            )
        }
        
        return stringBuilder.toString()
    }
    
    /**
     * 将实时数据直接转换为CSV格式
     * 使用项目中已有的CSV转换逻辑
     */
    private fun convertRealtimeDataToCsv(dataList: List<WxStepTable>): String {
        val stringBuilder = StringBuilder()
        
        // 添加CSV表头
        stringBuilder.append(Constants.WX_LOG_DATA_CSV_HEADER)
        
        // 添加数据行 - 将实时数据映射到CSV格式
        dataList.forEach { row ->
            stringBuilder.append(
                CsvUtil.encodeCsvLineString(
                    row.id,
                    row.userName,
                    row.stepNum,
                    row.likeNum,
                    row.logTimeString,
                    row.logTime,
                    row.userOrder,
                    row.logModel
                )
            )
        }
        
        return stringBuilder.toString()
    }
    
    /**
     * 开始分析数据
     */
    fun startAnalysis() {
        if (isAnalyzing) return
        
        isAnalyzing = true
        _analysisState.value = com.equationl.wxsteplog.aiapi.AiAnalysisResult(
            status = com.equationl.wxsteplog.aiapi.AnalysisStatus.PROCESSING,
            content = "",
            modelName = selectedModel
        )
        
        viewModelScope.launch {
            try {
                // 转换为CSV格式的数据
                val csvData = if (dataSourceType == DataSourceType.HISTORY) {
                    // 加载并转换历史数据
                    val historyData = loadSelectedHistoryData()
                    if (historyData.isEmpty()) {
                        _analysisState.value = com.equationl.wxsteplog.aiapi.AiAnalysisResult(
                            status = com.equationl.wxsteplog.aiapi.AnalysisStatus.ERROR,
                            content = "",
                            modelName = selectedModel,
                            errorMessage = "没有找到符合条件的历史数据"
                        )
                        isAnalyzing = false
                        return@launch
                    }
                    convertHistoryDataToCsv(historyData)
                } else {
                    // 加载并直接转换实时数据
                    val realtimeData = loadSelectedRealtimeData()
                    if (realtimeData.isEmpty()) {
                        _analysisState.value = com.equationl.wxsteplog.aiapi.AiAnalysisResult(
                            status = com.equationl.wxsteplog.aiapi.AnalysisStatus.ERROR,
                            content = "",
                            modelName = selectedModel,
                            errorMessage = "没有找到符合条件的实时数据"
                        )
                        isAnalyzing = false
                        return@launch
                    }
                    convertRealtimeDataToCsv(realtimeData)
                }

                
                // 调用AI分析服务（使用CSV格式数据）
                aiAnalysisService.analyzeStepDataWithCsv(
                    csvData = csvData,
                    prompt = userPrompt.takeIf { it.isNotBlank() },
                    modelName = selectedModel
                ).collectLatest { result ->
                    _analysisState.value = result
                    
                    // 分析完成或出错时，停止自动滚动
                    if (result.status != com.equationl.wxsteplog.aiapi.AnalysisStatus.PROCESSING) {
                        isAnalyzing = false
                    }
                }
            } catch (e: Exception) {
                Log.e("AiAnalysis", "Analysis error", e)
                _analysisState.value = com.equationl.wxsteplog.aiapi.AiAnalysisResult(
                    status = com.equationl.wxsteplog.aiapi.AnalysisStatus.ERROR,
                    content = "",
                    modelName = selectedModel,
                    errorMessage = "分析时发生错误: ${e.message}"
                )
                isAnalyzing = false
            }
        }
    }
    
    /**
     * 取消分析
     */
    fun cancelAnalysis() {
        isAnalyzing = false
        _analysisState.value = null
    }
    
    /**
     * 格式化日期
     */
    fun formatDate(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
    }
    
    /**
     * 检查模型是否已配置
     */
    fun isModelConfigured(modelName: String): Boolean {
        return aiAnalysisService.isModelConfigured(modelName)
    }
    
    /**
     * 保存模型配置
     */
    fun saveModelConfig(modelName: String, apiKey: String) {
        viewModelScope.launch {
            aiAnalysisService.saveModelConfig(modelName, mapOf("apiKey" to apiKey))
        }
    }
}
