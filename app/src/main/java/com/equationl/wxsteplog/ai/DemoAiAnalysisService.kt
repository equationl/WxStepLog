package com.equationl.wxsteplog.ai

import com.equationl.wxsteplog.constants.Constants
import com.equationl.wxsteplog.util.datastore.DataStoreUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI分析服务的示例实现
 * 仅用于演示功能，不连接实际的AI服务
 * 生成模拟的分析结果
 */
@Singleton
class DemoAiAnalysisService @Inject constructor() :
    com.equationl.wxsteplog.aiapi.AiAnalysisInterface {

    companion object {
        // 支持的AI模型列表
        private val SUPPORTED_MODELS = listOf(
            "OpenAI GPT-3.5",
            "OpenAI GPT-4",
            "Azure OpenAI",
            "本地模型"
        )
        
        // 配置项的Key前缀
        private const val CONFIG_PREFIX = "AI_MODEL_CONFIG_"
    }

    override fun getSupportedModels(): List<String> = SUPPORTED_MODELS

    override suspend fun analyzeStepDataWithCsv(
        csvData: String,
        prompt: String?,
        modelName: String
    ): Flow<com.equationl.wxsteplog.aiapi.AiAnalysisResult> = flow {
        // 检查模型是否配置
        if (!isModelConfigured(modelName)) {
            emit(
                com.equationl.wxsteplog.aiapi.AiAnalysisResult(
                    status = com.equationl.wxsteplog.aiapi.AnalysisStatus.ERROR,
                    content = "",
                    modelName = modelName,
                    errorMessage = "模型未正确配置，请在设置中配置API密钥"
                )
            )
            return@flow
        }
        
        // 发出处理中的状态
        emit(
            com.equationl.wxsteplog.aiapi.AiAnalysisResult(
                status = com.equationl.wxsteplog.aiapi.AnalysisStatus.PROCESSING,
                content = "正在准备CSV格式数据进行分析...",
                modelName = modelName
            )
        )
        
        // 解析CSV数据以获取基本统计信息
        val delimiter = Constants.csvDelimiter.value.first()
        val lines = csvData.lines().filter { it.isNotBlank() }
        val dataCount = if (lines.isNotEmpty()) lines.size - 1 else 0 // 减去表头
        
        if (dataCount <= 0) {
            emit(
                com.equationl.wxsteplog.aiapi.AiAnalysisResult(
                    status = com.equationl.wxsteplog.aiapi.AnalysisStatus.ERROR,
                    content = "",
                    modelName = modelName,
                    errorMessage = "CSV数据为空或格式不正确"
                )
            )
            return@flow
        }
        
        emit(
            com.equationl.wxsteplog.aiapi.AiAnalysisResult(
                status = com.equationl.wxsteplog.aiapi.AnalysisStatus.PROCESSING,
                content = "分析中...\n已收集 $dataCount 条CSV格式数据，准备分析",
                modelName = modelName
            )
        )
        
        // 模拟分析过程
        delay(2000)
        
        // 在实际的AI模型实现中，这里将直接把CSV数据发送给AI模型
        // 这样AI模型可以直接处理表格格式的数据
        
        // 构建示例分析结果
        val analysisResult = buildString {
            appendLine("CSV格式步数数据分析报告（${modelName}）")
            appendLine("===============================")
            appendLine("数据概览:")
            appendLine("- 总记录数: $dataCount")
            appendLine("- 数据格式: CSV")
            appendLine()
            
            appendLine("CSV数据示例:")
            val sampleLines = lines.take(3).joinToString("\n")
            appendLine(sampleLines)
            appendLine("...")
            appendLine("===============================")
            appendLine("当前分析功能为本地模拟生成示例，仅用于演示功能逻辑和界面布局，非 AI 分析数据")
            
            if (!prompt.isNullOrBlank()) {
                appendLine()
                appendLine("根据您的提示「$prompt」进行的额外分析:")
                appendLine("- 此功能需要连接真实AI模型，当前为示例实现")
            }
        }
        
        // 发送最终结果
        emit(
            com.equationl.wxsteplog.aiapi.AiAnalysisResult(
                status = com.equationl.wxsteplog.aiapi.AnalysisStatus.COMPLETED,
                content = analysisResult,
                modelName = modelName
            )
        )
    }

    override fun isModelConfigured(modelName: String): Boolean {
        val configKey = "$CONFIG_PREFIX$modelName"
        return when (modelName) {
            "本地模型" -> true  // 本地模型不需要API密钥
            else -> DataStoreUtils.getSyncData(configKey, "") != ""
        }
    }

    override suspend fun saveModelConfig(modelName: String, config: Map<String, String>) {
        val configKey = "$CONFIG_PREFIX$modelName"
        // 简单实现：将配置序列化为字符串存储
        // 实际项目中可能需要更复杂的加密存储
        val configString = config.entries.joinToString(";") { "${it.key}=${it.value}" }
        DataStoreUtils.putData(configKey, configString)
    }
}
