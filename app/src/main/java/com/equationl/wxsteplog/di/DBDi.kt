package com.equationl.wxsteplog.di

import android.content.Context
import com.equationl.wxsteplog.db.WxStepDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {

    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext app: Context) = run {
        WxStepDB.create(app)
    }
}