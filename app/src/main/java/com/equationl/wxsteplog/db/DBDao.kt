package com.equationl.wxsteplog.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface WxStepDao{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertData(data: WxStepTable): Long
}