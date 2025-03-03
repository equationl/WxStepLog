package com.equationl.wxsteplog.ui.view.setting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.wxsteplog.ai.AiAnalysisServiceFactory
import com.equationl.wxsteplog.aiapi.ModelBean
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
    val availableModels: List<ModelBean> = emptyList(),
    val selectedModel: ModelBean? = null,
    /**
     * {"$modelName" to Map}
     * */
    val modelConfigs: Map<String, Map<String, Any>> = emptyMap()
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

            val newModelConfig = mutableMapOf<String, Map<String, Any>>()
            for (model in models) {
                val modelConfig = aiAnalysisServiceFactory.getModelConfig(model) ?: emptyMap()
                newModelConfig[model.modelName] = modelConfig
            }
            
            _uiState.update { currentState ->
                currentState.copy(
                    isPaidServiceAvailable = isPaidAvailable,
                    isPaidServiceEnabled = isUsingPaid,
                    availableModels = models,
                    selectedModel = if (models.isNotEmpty()) models[0] else null,
                    modelConfigs = newModelConfig
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
    fun selectModel(modelName: ModelBean) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedModel = modelName
            )
        }
    }
    
    /**
     * 保存模型配置
     *
     * @param model 模型名称
     * @param config 配置信息
     */
    fun saveModelConfig(model: ModelBean, config: Map<String, Any>) {
        viewModelScope.launch {
            aiAnalysisInterface.saveModelConfig(model, config)
            
            // 更新UI状态中的配置信息
            val updatedConfigs = _uiState.value.modelConfigs.toMutableMap()
            updatedConfigs[model.modelName] = config.mapValues { entry ->
                entry.value
            }
            
            _uiState.update { currentState ->
                currentState.copy(
                    modelConfigs = updatedConfigs
                )
            }
        }
    }
}
