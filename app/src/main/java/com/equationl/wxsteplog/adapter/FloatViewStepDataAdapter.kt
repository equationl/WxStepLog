package com.equationl.wxsteplog.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.equationl.wxsteplog.R
import com.equationl.wxsteplog.model.StaticsScreenModel

class FloatViewStepDataAdapter(data: MutableList<StaticsScreenModel>) : BaseQuickAdapter<StaticsScreenModel, BaseViewHolder>(R.layout.float_view_step_date_item, data) {

    override fun convert(holder: BaseViewHolder, item: StaticsScreenModel) {
        holder.getView<TextView>(R.id.stepDataItem_date_text).text = item.logTimeString
        holder.getView<TextView>(R.id.stepDataItem_userName_text).text = item.userName
        holder.getView<TextView>(R.id.stepDataItem_step_text).text = item.stepNum.toString()
        holder.getView<TextView>(R.id.stepDataItem_like_text).text = item.likeNum.toString()
    }
}