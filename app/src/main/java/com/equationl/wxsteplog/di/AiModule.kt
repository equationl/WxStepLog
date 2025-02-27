package com.equationl.wxsteplog.di

import com.equationl.wxsteplog.ai.AiAnalysisInterface
import com.equationl.wxsteplog.ai.DefaultAiAnalysisService
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
        defaultAiAnalysisService: DefaultAiAnalysisService
    ): AiAnalysisInterface
}
