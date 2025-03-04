package com.equationl.wxsteplog.ai

import android.util.Log
import com.equationl.common.datastore.DataStoreUtils
import com.equationl.wxsteplog.aiapi.AiAnalysisInterface
import com.equationl.wxsteplog.aiapi.AiAnalysisResult
import com.equationl.wxsteplog.aiapi.DataSourceType
import com.equationl.wxsteplog.aiapi.ModelBean
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.reflect.Constructor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI分析服务工厂类
 * 用于动态决定使用哪种AI分析服务实现
 */
@Singleton
class AiAnalysisServiceFactory @Inject constructor(
    private val demoService: DemoAiAnalysisService
) : AiAnalysisInterface {

    companion object {
        private const val TAG = "AiAnalysisFactory"
        
        // 付费AI服务的完整类名
        private const val PAID_SERVICE_CLASS = "com.equationl.wxsteplog.aipro.ProAiAnalysisService"
        
        // 是否启用付费AI服务的配置键
        private const val KEY_ENABLE_PAID_SERVICE = "ENABLE_PAID_AI_SERVICE"
        
        // 默认为false，即使用演示服务
        private const val DEFAULT_ENABLE_PAID_SERVICE = false
    }
    
    // 当前使用的AI服务
    @Volatile
    private var currentService: AiAnalysisInterface = demoService
    
    init {
        // 初始化时尝试加载付费服务
        tryLoadPaidService()
    }
    
    /**
     * 尝试加载付费AI服务
     * 如果无法加载，将使用演示服务
     */
    private fun tryLoadPaidService() {
        // 判断是否启用付费服务
        val enablePaidService = DataStoreUtils.getSyncData(KEY_ENABLE_PAID_SERVICE, DEFAULT_ENABLE_PAID_SERVICE)
        if (!enablePaidService) {
            Log.d(TAG, "付费AI服务未启用，使用演示服务")
            currentService = demoService
            return
        }
        
        try {
            // 使用反射动态加载付费服务类
            val paidServiceClass = Class.forName(PAID_SERVICE_CLASS)
            val constructor: Constructor<*> = paidServiceClass.getDeclaredConstructor()
            val paidService = constructor.newInstance() as AiAnalysisInterface
            
            Log.d(TAG, "成功加载付费AI服务: ${paidService::class.java.simpleName}")
            currentService = paidService
        } catch (e: Exception) {
            Log.e(TAG, "加载付费AI服务失败: ${e.message}", e)
            // 加载失败时回退到演示服务
            currentService = demoService
        }
    }
    
    /**
     * 切换是否使用付费AI服务
     * @param enable 是否启用付费服务
     * @return 是否切换成功
     */
    suspend fun enablePaidService(enable: Boolean): Boolean {
        if (enable) {
            // 尝试加载付费服务
            try {
                val paidServiceClass = Class.forName(PAID_SERVICE_CLASS)
                val constructor: Constructor<*> = paidServiceClass.getDeclaredConstructor()
                val paidService = constructor.newInstance() as AiAnalysisInterface
                
                // 保存设置并切换服务
                DataStoreUtils.putData(KEY_ENABLE_PAID_SERVICE, true)
                currentService = paidService
                Log.d(TAG, "切换到付费AI服务")
                return true
            } catch (e: Exception) {
                Log.e(TAG, "加载付费AI服务失败: ${e.message}", e)
                // 加载失败时回退到演示服务
                currentService = demoService
                return false
            }
        } else {
            // 禁用付费服务，使用演示服务
            DataStoreUtils.putData(KEY_ENABLE_PAID_SERVICE, false)
            currentService = demoService
            Log.d(TAG, "切换到演示AI服务")
            return true
        }
    }
    
    /**
     * 检查付费服务是否可用
     * @return 付费服务是否可用
     */
    fun isPaidServiceAvailable(): Boolean {
        return try {
            Class.forName(PAID_SERVICE_CLASS)
            true
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "isPaidServiceAvailable: ", e)
            false
        }
    }
    
    /**
     * 是否当前正在使用付费服务
     * @return 是否使用付费服务
     */
    fun isUsingPaidService(): Boolean {
        return currentService::class.java.name == PAID_SERVICE_CLASS
    }

    // 以下是委托给当前服务实现的方法
    
    override fun getSupportedModels(): List<ModelBean> = currentService.getSupportedModels()

    override suspend fun analyzeStepDataWithCsv(
        csvData: String,
        prompt: String?,
        model: ModelBean,
        dataSourceType: DataSourceType
    ): Flow<AiAnalysisResult> {
        return if (isUsingPaidService()) {
            // 使用付费服务
            currentService.analyzeStepDataWithCsv(csvData, prompt, model, dataSourceType)
        } else {
            // 使用演示服务，但添加提示信息
            flow {
                // 先添加一个提示状态
                emit(
                    AiAnalysisResult(
                        status = com.equationl.wxsteplog.aiapi.AnalysisStatus.PROCESSING,
                        content = "正在使用演示服务分析...\n(提示: 可通过加载AI Pro模块获得完整AI分析功能)",
                        model = model
                    )
                )
                
                // 然后委托给演示服务
                val demoFlow = demoService.analyzeStepDataWithCsv(csvData, prompt, model, dataSourceType)
                demoFlow.collect { result ->
                    emit(result)
                }
            }
        }
    }

    override fun cancelAnalysis() {
        currentService.cancelAnalysis()
    }

    override fun isModelConfigured(model: ModelBean): Boolean =
        currentService.isModelConfigured(model)

    override suspend fun saveModelConfig(model: ModelBean, config: Map<String, Any>) =
        currentService.saveModelConfig(model, config)

    override fun getModelConfig(model: ModelBean): Map<String, Any>? = currentService.getModelConfig(model)
}
