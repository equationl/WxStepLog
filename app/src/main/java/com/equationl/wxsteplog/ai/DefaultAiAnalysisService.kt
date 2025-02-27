package com.equationl.wxsteplog.ai

import com.equationl.wxsteplog.db.WxStepHistoryTable
import com.equationl.wxsteplog.util.datastore.DataStoreUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultAiAnalysisService @Inject constructor() : AiAnalysisInterface {

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

    override suspend fun analyzeStepData(
        data: List<WxStepHistoryTable>,
        prompt: String?,
        modelName: String
    ): Flow<AiAnalysisResult> = flow {
        // 检查模型是否配置
        if (!isModelConfigured(modelName)) {
            emit(
                AiAnalysisResult(
                    status = AnalysisStatus.ERROR,
                    content = "",
                    modelName = modelName,
                    errorMessage = "模型未正确配置，请在设置中配置API密钥"
                )
            )
            return@flow
        }
        
        // 发出处理中的状态
        emit(
            AiAnalysisResult(
                status = AnalysisStatus.PROCESSING,
                content = "正在准备数据进行分析...",
                modelName = modelName
            )
        )
        
        // 模拟处理过程
        delay(1000)
        
        // 提取数据摘要
        val dataCount = data.size
        val userList = data.map { it.userName }.distinct()
        val startDate = data.minByOrNull { it.dataTime ?: 0 }?.dataTime?.let {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
        } ?: "未知"
        val endDate = data.maxByOrNull { it.dataTime ?: 0 }?.dataTime?.let {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
        } ?: "未知"
        
        emit(
            AiAnalysisResult(
                status = AnalysisStatus.PROCESSING,
                content = "分析中...\n已收集 $dataCount 条数据\n用户数: ${userList.size}\n时间范围: $startDate 至 $endDate",
                modelName = modelName
            )
        )
        
        // 模拟分析过程
        delay(2000)
        
        // 实际项目中，这里应该调用真实的AI模型API，发送数据并获取分析结果
        // 这里只是一个模拟实现，返回一些基于数据的简单统计
        
        // 计算每个用户的平均步数
        val userAverageSteps = data
            .filter { it.stepNum != null }
            .groupBy { it.userName }
            .mapValues { (_, records) ->
                records.mapNotNull { it.stepNum }.average().toInt()
            }
        
        // 找出最高步数记录
        val maxStepRecord = data.maxByOrNull { it.stepNum ?: 0 }
        val maxStepUser = maxStepRecord?.userName ?: "未知"
        val maxSteps = maxStepRecord?.stepNum ?: 0
        
        // 构建分析结果
        val analysisResult = buildString {
            appendLine("步数数据分析报告（${modelName}）")
            appendLine("===============================")
            appendLine("数据概览:")
            appendLine("- 总记录数: $dataCount")
            appendLine("- 分析用户数: ${userList.size}")
            appendLine("- 时间范围: $startDate 至 $endDate")
            appendLine()
            
            appendLine("用户步数情况:")
            userAverageSteps.forEach { (userName, avgSteps) ->
                appendLine("- $userName: 平均步数 $avgSteps")
            }
            appendLine()
            
            appendLine("最高记录:")
            appendLine("- 用户 $maxStepUser 达成最高步数: $maxSteps")
            appendLine("===============================")
            appendLine("当前分析功能为本地模拟生成示例，非 AI 分析数据")
            
            if (!prompt.isNullOrBlank()) {
                appendLine()
                appendLine("根据您的提示「$prompt」进行的额外分析:")
                appendLine("- 此功能需要连接真实AI模型，当前为示例实现")
            }
        }
        
        // 发送最终结果
        emit(
            AiAnalysisResult(
                status = AnalysisStatus.COMPLETED,
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
