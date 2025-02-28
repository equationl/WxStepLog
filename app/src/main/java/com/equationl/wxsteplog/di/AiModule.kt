package com.equationl.wxsteplog.di

import com.equationl.wxsteplog.ai.AiAnalysisServiceFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {
    
    @Binds
    @Singleton
    abstract fun bindAiAnalysisService(
        aiAnalysisServiceFactory: AiAnalysisServiceFactory
    ): com.equationl.wxsteplog.aiapi.AiAnalysisInterface
}
