package com.equationl.wxsteplog.ui.view.aianalysis.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.wxsteplog.ai.AiAnalysisInterface
import com.equationl.wxsteplog.ai.AiAnalysisResult
import com.equationl.wxsteplog.ai.AnalysisStatus
import com.equationl.wxsteplog.db.WxStepDB
import com.equationl.wxsteplog.db.WxStepHistoryTable
import com.equationl.wxsteplog.model.StepHistoryLogStartTimeDbModel
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
    private val aiAnalysisService: AiAnalysisInterface
) : ViewModel() {

    private val wxStepHistoryDao = wxStepDB.wxStepHistoryDB()

    // 支持的AI模型列表
    val supportedModels = aiAnalysisService.getSupportedModels()
    
    // 选择的模型
    var selectedModel by mutableStateOf(supportedModels.firstOrNull() ?: "")
        private set
    
    // 用户输入的额外提示
    var userPrompt by mutableStateOf("")
        private set
    
    // 数据源列表
    val logHistoryList = mutableStateListOf<StepHistoryLogStartTimeDbModel>()
    var selectedHistoryLogTime by mutableStateOf(0L)
        private set
    
    // 用户列表
    val userList = mutableStateListOf<String>()
    var selectedUser by mutableStateOf("")
        private set
    
    // 数据选择配置
    var startDate by mutableStateOf(Date())
        private set
    var endDate by mutableStateOf(Date())
        private set
    
    // 分析状态
    private val _analysisState = MutableStateFlow<AiAnalysisResult?>(null)
    val analysisState: StateFlow<AiAnalysisResult?> = _analysisState
    
    // 选中的数据
    private var selectedData = listOf<WxStepHistoryTable>()
    
    // 是否正在分析
    var isAnalyzing by mutableStateOf(false)
        private set
    
    // 初始化数据
    init {
        loadLogHistoryList()
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
                loadUserList()
            }
        }
    }
    
    /**
     * 加载用户列表
     */
    fun loadUserList() {
        viewModelScope.launch {
            userList.clear()
            userList.add("全部用户") // 添加一个特殊选项表示所有用户
            userList.addAll(wxStepHistoryDao.getCurrentUserList())
            selectedUser = userList.first()
            
            // 初始化日期范围
            val historyModel = logHistoryList.find { it.logStartTime == selectedHistoryLogTime }
            historyModel?.let {
                startDate = Date(it.startTime ?: 0)
                endDate = Date(it.endTime ?: Date().time)
            }
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
        
        loadUserList()
    }
    
    /**
     * 更新选中的用户
     */
    fun onUserSelected(user: String) {
        selectedUser = user
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
     * 加载选中的数据
     */
    private suspend fun loadSelectedData(): List<WxStepHistoryTable> {
        val userName = if (selectedUser == "全部用户") "%" else selectedUser
        
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
     * 开始分析数据
     */
    fun startAnalysis() {
        if (isAnalyzing) return
        
        viewModelScope.launch {
            isAnalyzing = true
            
            try {
                // 加载选中的数据
                selectedData = loadSelectedData()
                
                if (selectedData.isEmpty()) {
                    _analysisState.value = AiAnalysisResult(
                        status = AnalysisStatus.ERROR,
                        content = "",
                        modelName = selectedModel,
                        errorMessage = "没有找到符合条件的数据"
                    )
                    return@launch
                }
                
                // 调用AI分析服务
                aiAnalysisService.analyzeStepData(
                    data = selectedData,
                    prompt = userPrompt.takeIf { it.isNotBlank() },
                    modelName = selectedModel
                ).collectLatest { result ->
                    _analysisState.value = result
                    
                    // 分析完成或出错时结束分析状态
                    if (result.status == AnalysisStatus.COMPLETED || result.status == AnalysisStatus.ERROR) {
                        isAnalyzing = false
                    }
                }
            } catch (e: Exception) {
                _analysisState.value = AiAnalysisResult(
                    status = AnalysisStatus.ERROR,
                    content = "",
                    modelName = selectedModel,
                    errorMessage = "分析过程中出错: ${e.message ?: "未知错误"}"
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
