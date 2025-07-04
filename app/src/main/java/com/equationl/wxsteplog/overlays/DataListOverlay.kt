package com.equationl.wxsteplog.overlays

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ScreenUtils
import com.equationl.wxsteplog.adapter.FloatViewStepDataAdapter
import com.equationl.wxsteplog.constants.Constants
import com.equationl.wxsteplog.databinding.EmptyItemBinding
import com.equationl.wxsteplog.databinding.OverlaysDataListViewBinding
import com.equationl.wxsteplog.db.DbUtil
import com.equationl.wxsteplog.util.DateTimeUtil
import com.equationl.wxsteplog.util.LogWrapper
import com.equationl.wxsteplog.util.ResolveDataUtil
import com.ven.assists.service.AssistsService
import com.ven.assists.service.AssistsServiceListener
import com.ven.assists.window.AssistsWindowManager
import com.ven.assists.window.AssistsWindowWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("StaticFieldLeak")
object DataListOverlay: AssistsServiceListener {
    private var dataListAdapter: FloatViewStepDataAdapter? = null
    private var dao = DbUtil.db.wxStepDB()

    private var isFilterUser: Boolean = true
    private var isFoldData: Boolean = true

    private var viewBinding: OverlaysDataListViewBinding? = null
        @SuppressLint("ClickableViewAccessibility")
        get() {
            if (field == null) {
                field = OverlaysDataListViewBinding.inflate(LayoutInflater.from(AssistsService.instance)).apply {
                    stepDataFoldDataCheck.setOnCheckedChangeListener { buttonView, isChecked ->
                        LogWrapper.log("更改折叠数据状态为：$isChecked")
                        isFoldData = isChecked
                        loadStepListData()
                    }

                    stepDataFilterUserCheck.setOnCheckedChangeListener { buttonView, isChecked ->
                        LogWrapper.log("更改筛选用户状态为：$isChecked")
                        isFilterUser = isChecked
                        loadStepListData()
                    }
                }
            }
            return field
        }


    var onClose: ((parent: View) -> Unit)? = null

    var showed = false
        private set
        get() {
            assistWindowWrapper?.let {
                return AssistsWindowManager.isVisible(it.getView())
            } ?: return false
        }

    var assistWindowWrapper: AssistsWindowWrapper? = null
        private set
        get() {
            viewBinding?.let {
                if (field == null) {
                    field = AssistsWindowWrapper(it.root, wmLayoutParams = AssistsWindowManager.createLayoutParams().apply {
                        width = (ScreenUtils.getScreenWidth() * 0.8).toInt()
                        height = (ScreenUtils.getScreenHeight() * 0.5).toInt()
                    }, onClose = { hide() }).apply {
                        minWidth = (ScreenUtils.getScreenWidth() * 0.6).toInt()
                        minHeight = (ScreenUtils.getScreenHeight() * 0.4).toInt()
                        initialCenter = true
                        viewBinding.tvTitle.text = "查看数据"
                    }
                }
            }
            return field
        }

    fun show() {
        if (!AssistsService.listeners.contains(this)) {
            AssistsService.listeners.add(this)
        }
        if (!AssistsWindowManager.contains(assistWindowWrapper?.getView())) {
            AssistsWindowManager.add(assistWindowWrapper)

            dataListAdapter = FloatViewStepDataAdapter(mutableListOf())
            viewBinding?.stepDataListRv?.layoutManager = LinearLayoutManager(AssistsService.instance)
            viewBinding?.stepDataListRv?.adapter = dataListAdapter
            val noDataView = EmptyItemBinding.inflate(LayoutInflater.from(AssistsService.instance))
            dataListAdapter?.setEmptyView(noDataView.root)

            loadStepListData()
        }
    }

    fun hide() {
        AssistsWindowManager.removeView(assistWindowWrapper?.getView())

    }

    override fun onUnbind() {
        viewBinding = null
        assistWindowWrapper = null
    }

    private fun loadStepListData() {
        CoroutineScope(Dispatchers.IO).launch {
            val showRange = DateTimeUtil.getCurrentDayRange()
            // val offset = TimeZone.getDefault().rawOffset
            val rawDataList = if (isFilterUser && Constants.showDataFilterUserName.isNotBlank())
                dao.queryRangeDataListByUserName(showRange.start, showRange.end, Constants.showDataFilterUserName, 1, Int.MAX_VALUE)
            else
                dao.queryRangeDataList(showRange.start, showRange.end, 1, Int.MAX_VALUE)

            val resolveData = ResolveDataUtil.rawDataToStaticsModel(rawDataList,
                isFoldData
            )
            withContext(Dispatchers.Main) {
                dataListAdapter?.setList(resolveData)
            }
        }
    }

}