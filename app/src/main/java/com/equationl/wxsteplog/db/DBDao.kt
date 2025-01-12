package com.equationl.wxsteplog.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.equationl.wxsteplog.model.StepHistoryLogStartTimeDbModel

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


@Dao
interface WxStepHistoryDao{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertData(data: WxStepHistoryTable): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllData(data: List<WxStepHistoryTable>): List<Long>

    @Query("SELECT * FROM wx_step_history_table")
    suspend fun queryAllData(): List<WxStepHistoryTable>

    @Query("SELECT * FROM wx_step_history_table WHERE log_start_time=:logStartTime")
    suspend fun queryAllDataByLogStartTime(logStartTime: Long): List<WxStepHistoryTable>

    @Query("SELECT log_start_time AS 'logStartTime', count(*) AS 'count', min(data_time) AS 'startTime', max(data_time) AS 'endTime' FROM wx_step_history_table GROUP BY log_start_time")
    suspend fun getLogStartTimeList(): List<StepHistoryLogStartTimeDbModel>

    @Query("SELECT * FROM wx_step_history_table WHERE (data_time BETWEEN :startTime AND :endTime) AND user_name LIKE :userName ORDER BY data_time ASC")
    suspend fun queryRangeDataList(startTime: Long, endTime: Long, userName: String = "%"): List<WxStepHistoryTable>

    @Query("SELECT * FROM wx_step_history_table WHERE (data_time BETWEEN :startTime AND :endTime) AND user_name LIKE :userName AND log_start_time=:logStartTime ORDER BY data_time ASC")
    suspend fun queryRangeDataListByLogStartTime(startTime: Long, endTime: Long, logStartTime: Long, userName: String = "%"): List<WxStepHistoryTable>

    @Query("SELECT DISTINCT user_name FROM wx_step_history_table")
    suspend fun getCurrentUserList(): List<String>
}