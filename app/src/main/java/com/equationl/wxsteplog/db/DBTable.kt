package com.equationl.wxsteplog.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wx_step_table")
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
    var logTimeString: String
)