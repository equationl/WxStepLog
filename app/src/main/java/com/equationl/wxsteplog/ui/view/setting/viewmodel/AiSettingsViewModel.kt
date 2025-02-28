package com.equationl.wxsteplog.ui.view.setting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.wxsteplog.ai.AiAnalysisServiceFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AI设置的UI状态
 */
data class AiSettingsUiState(
    val isPaidServiceAvailable: Boolean = false,
    val isPaidServiceEnabled: Boolean = false,
    val availableModels: List<String> = emptyList(),
    val selectedModel: String = "",
    val modelConfigs: Map<String, Map<String, String>> = emptyMap()
)

/**
 * AI设置页面的ViewModel
 */
@HiltViewModel
class AiSettingsViewModel @Inject constructor(
    private val aiAnalysisServiceFactory: AiAnalysisServiceFactory,
    private val aiAnalysisInterface: com.equationl.wxsteplog.aiapi.AiAnalysisInterface
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AiSettingsUiState())
    val uiState: StateFlow<AiSettingsUiState> = _uiState.asStateFlow()
    
    /**
     * 加载当前设置
     */
    fun loadCurrentSettings() {
        viewModelScope.launch {
            val isPaidAvailable = aiAnalysisServiceFactory.isPaidServiceAvailable()
            val isUsingPaid = aiAnalysisServiceFactory.isUsingPaidService()
            val models = aiAnalysisInterface.getSupportedModels()
            
            // 获取用于显示的模型配置信息
            val modelConfigs = models.associateWith { modelName ->
                // 对于演示目的，这里只显示是否已配置
                if (aiAnalysisInterface.isModelConfigured(modelName)) {
                    mapOf("apiKey" to "********") // 不显示真实的API密钥
                } else {
                    mapOf("apiKey" to "")
                }
            }
            
            _uiState.update { currentState ->
                currentState.copy(
                    isPaidServiceAvailable = isPaidAvailable,
                    isPaidServiceEnabled = isUsingPaid,
                    availableModels = models,
                    selectedModel = if (models.isNotEmpty()) models[0] else "",
                    modelConfigs = modelConfigs
                )
            }
        }
    }
    
    /**
     * 切换付费服务状态
     *
     * @param enable 是否启用付费服务
     */
    fun togglePaidService(enable: Boolean) {
        viewModelScope.launch {
            aiAnalysisServiceFactory.enablePaidService(enable)
            
            _uiState.update { currentState ->
                currentState.copy(
                    isPaidServiceEnabled = aiAnalysisServiceFactory.isUsingPaidService()
                )
            }
            
            // 刷新可用模型列表，因为不同服务支持的模型可能不同
            loadCurrentSettings()
        }
    }
    
    /**
     * 选择AI模型
     *
     * @param modelName 模型名称
     */
    fun selectModel(modelName: String) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedModel = modelName
            )
        }
    }
    
    /**
     * 保存模型配置
     *
     * @param modelName 模型名称
     * @param config 配置信息
     */
    fun saveModelConfig(modelName: String, config: Map<String, String>) {
        viewModelScope.launch {
            aiAnalysisInterface.saveModelConfig(modelName, config)
            
            // 更新UI状态中的配置信息
            val updatedConfigs = _uiState.value.modelConfigs.toMutableMap()
            updatedConfigs[modelName] = config.mapValues { entry ->
                if (entry.key == "apiKey" && entry.value.isNotBlank()) "********" else entry.value
            }
            
            _uiState.update { currentState ->
                currentState.copy(
                    modelConfigs = updatedConfigs
                )
            }
        }
    }
}
