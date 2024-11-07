package com.equationl.wxsteplog.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WxStepDao{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertData(data: WxStepTable): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllData(data: List<WxStepTable>): List<Long>

    @Query("SELECT * FROM wx_step_table")
    suspend fun queryAllData(): List<WxStepTable>

    @Query("SELECT * FROM wx_step_table WHERE (log_time BETWEEN :startTime AND :endTime) ORDER BY log_time ASC LIMIT (:pageSize + 1) OFFSET ((:page - 1) * :pageSize)")
    suspend fun queryRangeDataList(startTime: Long, endTime: Long, page: Int = 1, pageSize: Int = 50): List<WxStepTable>

    @Query("SELECT * FROM wx_step_table WHERE (log_time BETWEEN :startTime AND :endTime) AND user_name=:userName ORDER BY log_time ASC LIMIT (:pageSize + 1) OFFSET ((:page - 1) * :pageSize)")
    suspend fun queryRangeDataListByUserName(startTime: Long, endTime: Long, userName: String, page: Int = 1, pageSize: Int = 50): List<WxStepTable>

    @Query("SELECT DISTINCT user_name FROM wx_step_table")
    suspend fun getCurrentUserList(): List<String>
}