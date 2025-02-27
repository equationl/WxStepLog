package com.equationl.wxsteplog.ai

import com.equationl.wxsteplog.db.WxStepHistoryTable
import kotlinx.coroutines.flow.Flow

/**
 * AI分析接口
 * 提供通用的AI分析能力，支持不同模型实现
 */
interface AiAnalysisInterface {
    /**
     * 获取支持的AI模型名称列表
     */
    fun getSupportedModels(): List<String>

    /**
     * 分析步数数据
     * @param data 要分析的步数历史数据
     * @param prompt 额外的分析提示，可选
     * @param modelName 使用的AI模型名称，必须是 [getSupportedModels] 返回的列表中的一个
     * @return 分析结果流，包含分析过程和最终结果
     */
    suspend fun analyzeStepData(
        data: List<WxStepHistoryTable>,
        prompt: String? = null,
        modelName: String
    ): Flow<AiAnalysisResult>

    /**
     * 检查AI模型是否已正确配置（如API密钥等）
     * @param modelName 模型名称
     * @return 如果配置有效则返回true
     */
    fun isModelConfigured(modelName: String): Boolean

    /**
     * 保存AI模型配置
     * @param modelName 模型名称
     * @param config 配置数据，通常包括API密钥等
     */
    suspend fun saveModelConfig(modelName: String, config: Map<String, String>)
}

/**
 * AI分析结果类
 */
data class AiAnalysisResult(
    val status: AnalysisStatus,
    val content: String,
    val modelName: String,
    val errorMessage: String? = null
)

/**
 * 分析状态枚举
 */
enum class AnalysisStatus {
    PROCESSING,  // 分析中
    COMPLETED,   // 分析完成
    ERROR        // 发生错误
}
