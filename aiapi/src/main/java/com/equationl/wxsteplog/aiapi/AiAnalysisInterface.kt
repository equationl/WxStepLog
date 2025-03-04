package com.equationl.wxsteplog.aiapi

import kotlinx.coroutines.flow.Flow

/**
 * AI分析接口
 * 提供通用的AI分析能力，支持不同模型实现
 */
interface AiAnalysisInterface {
    /**
     * 获取支持的AI模型名称列表
     */
    fun getSupportedModels(): List<ModelBean>

    /**
     * 使用CSV格式分析步数数据
     * @param csvData CSV格式的步数数据
     * @param prompt 额外的分析提示，可选
     * @param model 使用的AI模型，必须是 [getSupportedModels] 返回的列表中的一个
     * @return 分析结果流，包含分析过程和最终结果
     */
    suspend fun analyzeStepDataWithCsv(
        csvData: String,
        prompt: String? = null,
        model: ModelBean,
        dataSourceType: DataSourceType
    ): Flow<AiAnalysisResult>

    /**
     * 停止分析
     * */
    fun cancelAnalysis()

    /**
     * 检查AI模型是否已正确配置（如API密钥等）
     * @param model 模型名称
     * @return 如果配置有效则返回true
     */
    fun isModelConfigured(model: ModelBean): Boolean

    /**
     * 获取模型配置
     *
     * */
    fun getModelConfig(model: ModelBean): Map<String, Any>?

    /**
     * 保存AI模型配置
     * @param model 模型名称
     * @param config 配置数据，通常包括API密钥等
     */
    suspend fun saveModelConfig(model: ModelBean, config: Map<String, Any>)
}

/**
 * AI分析结果类
 */
data class AiAnalysisResult(
    val status: AnalysisStatus,
    val content: String,
    val model: ModelBean,
    val errorMessage: String? = null,
    val thinkingContent: String? = null
)

/**
 * 分析状态枚举
 */
enum class AnalysisStatus {
    PROCESSING,  // 分析中
    COMPLETED,   // 分析完成
    ERROR,       // 发生错误
    THINKING,    // 思考中
}

data class ModelBean(
    val modelName: String,
    val modeShowName: String,
    val id: String = ""
)
