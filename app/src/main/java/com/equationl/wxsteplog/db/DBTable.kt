package com.equationl.wxsteplog.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.equationl.wxsteplog.model.LogModel

@Entity(tableName = "wx_step_table", indices = [Index(value = ["user_name", "step_num", "like_Num", "log_time", "user_order"], unique = true)])
data class WxStepTable (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "user_name")
    var userName: String,
    @ColumnInfo(name = "step_num")
    var stepNum: Int?,
    @ColumnInfo(name = "like_Num")
    var likeNum: Int?,
    @ColumnInfo(name = "log_time")
    var logTime: Long,
    @ColumnInfo(name = "log_time_string")
    var logTimeString: String,
    @ColumnInfo(name = "user_order")
    var userOrder: Int?,
    /**
     * 该值为枚举 [LogModel]
     *
     * 可能有多个类型，不同类型用 , 分隔
     * */
    @ColumnInfo(name = "log_model")
    var logModel: String?,
)

@Entity(tableName = "wx_step_history_table")
data class WxStepHistoryTable (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "user_name")
    var userName: String,
    @ColumnInfo(name = "step_num")
    var stepNum: Int?,
    @ColumnInfo(name = "like_Num")
    var likeNum: Int?,
    @ColumnInfo(name = "user_order")
    var userOrder: Int?,
    /**
     * 程序开始运行时间（本轮读取的开始时间）
     * */
    @ColumnInfo(name = "log_start_time")
    var logStartTime: Long,
    /**
     * 程序结束运行时间（此条数据插入时的时间）
     * */
    @ColumnInfo(name = "log_end_time")
    var logEndTime: Long,
    /**
     * 数据时间（该数据实际是哪个时间的数据， 忽略时间信息，仅日期信息）
     * */
    @ColumnInfo(name = "data_time")
    var dataTime: Long?,
    @ColumnInfo(name = "data_time_string")
    var dataTimeString: String?
)