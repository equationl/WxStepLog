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

    @Query("SELECT * FROM wx_step_table WHERE (log_time BETWEEN :startTime AND :endTime) ORDER BY log_time DESC LIMIT (:pageSize + 1) OFFSET ((:page - 1) * :pageSize)")
    suspend fun queryRangeDataList(startTime: Long, endTime: Long, page: Int = 1, pageSize: Int = 50): List<WxStepTable>
}