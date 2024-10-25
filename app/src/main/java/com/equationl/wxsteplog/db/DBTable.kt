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