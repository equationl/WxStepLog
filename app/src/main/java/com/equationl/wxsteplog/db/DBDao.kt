package com.equationl.wxsteplog.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WxStepDao{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertData(data: WxStepTable): Long

    @Query("SELECT * FROM wx_step_table")
    suspend fun queryAllData(): List<WxStepTable>
}