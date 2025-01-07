package com.equationl.wxsteplog.step

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.TimeUtils
import com.equationl.wxsteplog.adapter.FloatViewStepDataAdapter
import com.equationl.wxsteplog.constants.Constants
import com.equationl.wxsteplog.databinding.EmptyItemBinding
import com.equationl.wxsteplog.databinding.ViewMainOverBinding
import com.equationl.wxsteplog.db.DbUtil
import com.equationl.wxsteplog.model.WxStepLogSetting
import com.equationl.wxsteplog.util.DateTimeUtil
import com.equationl.wxsteplog.util.ResolveDataUtil
import com.equationl.wxsteplog.util.log.LogUtil
import com.ven.assists.Assists
import com.ven.assists.AssistsWindowManager
import com.ven.assists.stepper.StepListener
import com.ven.assists.stepper.StepManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object OverManager : StepListener {
    private const val TAG = "OverManager"
    @SuppressLint("StaticFieldLeak")
    private var viewMainOver: ViewMainOverBinding? = null
    private var setting: WxStepLogSetting? = null
    private var dataListAdapter: FloatViewStepDataAdapter? = null
    private var dao = DbUtil.db.wxStepDB()

    // 统计数据过滤参数
    private var isFilterUser: Boolean = true
    private var isFoldData: Boolean = true

    private fun createView(showType: ShowType = ShowType.LOG): ViewMainOverBinding? {
        return Assists.service?.let {
            StepManager.stepListeners.add(this)
            ViewMainOverBinding.inflate(LayoutInflater.from(it)).apply {
                initView(this, showType)
            }

        }
    }

    private fun initView(viewMainOverBinding: ViewMainOverBinding, showType: ShowType = ShowType.LOG) {
        with(viewMainOverBinding) {
            parent.assistsWindowLayoutWrapperBinding.tvTitle.text = when(showType) {
                ShowType.LOG -> "记录数据"
                ShowType.FIND_USER -> "查找用户名"
                ShowType.SINGLE_LOG -> "读取数据"
            }
            llOption.isVisible = true
            llLog.isVisible = false
            btnCloseLog.isVisible = false

            when (showType) {
                ShowType.LOG -> {
                    btnLogWxStep.visibility = View.VISIBLE
                    btnFindUserName.visibility = View.GONE
                    btnShowDataList.visibility = View.VISIBLE
                    btnLogHistory.visibility = View.GONE
                }
                ShowType.FIND_USER -> {
                    btnLogWxStep.visibility = View.GONE
                    btnFindUserName.visibility = View.VISIBLE
                    btnShowDataList.visibility = View.GONE
                    btnLogHistory.visibility = View.GONE
                }
                ShowType.SINGLE_LOG -> {
                    btnLogHistory.visibility = View.VISIBLE
                    btnLogWxStep.visibility = View.GONE
                    btnFindUserName.visibility = View.GONE
                    btnShowDataList.visibility = View.GONE
                }
            }

            btnLogWxStep.setOnClickListener {
                if (setting == null) {
                    LogUtil.e(TAG, "setting is null!")
                    return@setOnClickListener
                }
                beginStart(this)

//                if (setting!!.logUserMode == LogSettingMode.Multiple && setting!!.userNameList.size == 1) {
//                    StepManager.execute(LogWxStep::class.java, StepTag.STEP_1, begin = true, data = setting!!)
//                }
//                else {
                    StepManager.execute(LogMultipleWxStep::class.java, StepTag.STEP_1, begin = true, data = setting!!)
//                }
            }

            btnFindUserName.setOnClickListener {
                beginStart(this)
                StepManager.execute(FindUserNameStep::class.java, StepTag.STEP_1, begin = true)
            }

            btnLogHistory.setOnClickListener {
                beginStart(this)
                StepManager.execute(LogWxHistoryStep::class.java, StepTag.STEP_1, begin = true)
            }

            btnStop.setOnClickListener {
                stop()
            }
            btnCloseLog.setOnClickListener { showOption() }
            btnStopScrollLog.setOnClickListener {
                isAutoScrollLog = !isAutoScrollLog
            }
            btnLog.setOnClickListener {
                showLog()
                btnCloseLog.isVisible = true
                btnStop.isVisible = false
            }

            btnShowDataList.setOnClickListener {
                // 显示数据
                viewMainOver?.llLog?.isVisible = false
                viewMainOver?.runningBtn?.isVisible = false
                stepDataLl.visibility = View.VISIBLE
                loadStepListData()
            }

            stepDataCloseBtn.setOnClickListener {
                // 关闭显示数据
                stepDataLl.visibility = View.GONE
                viewMainOver?.llLog?.isVisible = true
                viewMainOver?.runningBtn?.isVisible = true
            }

            stepDataFoldDataCheck.setOnCheckedChangeListener { buttonView, isChecked ->
                log("更改折叠数据状态为：$isChecked")
                isFoldData = isChecked
                loadStepListData()
            }

            stepDataFilterUserCheck.setOnCheckedChangeListener { buttonView, isChecked ->
                log("更改筛选用户状态为：$isChecked")
                isFilterUser = isChecked
                loadStepListData()
            }

//            stepDataUserEt.setOnEditorActionListener { v, actionId, event ->
//                // 这里无法输入数据哦，没法获得焦点，如果这里拿到焦点的话又会与无障碍获取后面的页面内容冲突
//                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                    log("更改筛选用户为： ${v.text}")
//                    filterUser = v.text.toString()
//                    loadStepListData()
//                    return@setOnEditorActionListener true
//                }
//                false
//            }

            dataListAdapter = FloatViewStepDataAdapter(mutableListOf())
            stepDataListRv.layoutManager = LinearLayoutManager(Assists.service)
            stepDataListRv.adapter = dataListAdapter
            val noDataView = EmptyItemBinding.inflate(LayoutInflater.from(Assists.service))
            dataListAdapter?.setEmptyView(noDataView.root)

            root.setOnCloseClickListener {
                clear()
                return@setOnCloseClickListener false
            }
        }
    }

    fun show(setting: WxStepLogSetting) {
        this.setting = setting
        AssistsWindowManager.removeView(viewMainOver?.root)

        viewMainOver = createView()
        val width = ScreenUtils.getScreenWidth() - 60
        val height = SizeUtils.dp2px(300f)
        viewMainOver?.root?.layoutParams?.width = width
        viewMainOver?.root?.layoutParams?.height = height
        viewMainOver?.root?.minWidth = (ScreenUtils.getScreenWidth() * 0.6).toInt()
        viewMainOver?.root?.minHeight = height
        viewMainOver?.root?.setCenter()
        AssistsWindowManager.addAssistsWindowLayout(viewMainOver?.root)

    }

    fun showFindUser() {
        AssistsWindowManager.removeView(viewMainOver?.root)
        viewMainOver = createView(showType = ShowType.FIND_USER)
        val width = ScreenUtils.getScreenWidth() - 60
        val height = SizeUtils.dp2px(300f)
        viewMainOver?.root?.layoutParams?.width = width
        viewMainOver?.root?.layoutParams?.height = height
        viewMainOver?.root?.minWidth = (ScreenUtils.getScreenWidth() * 0.6).toInt()
        viewMainOver?.root?.minHeight = height
        viewMainOver?.root?.setCenter()
        AssistsWindowManager.addAssistsWindowLayout(viewMainOver?.root)
    }

    fun showSingleLog() {
        Constants.logWxHistoryStepStartTime = System.currentTimeMillis()
        AssistsWindowManager.removeView(viewMainOver?.root)
        viewMainOver = createView(showType = ShowType.SINGLE_LOG)
        val width = ScreenUtils.getScreenWidth() - 60
        val height = SizeUtils.dp2px(300f)
        viewMainOver?.root?.layoutParams?.width = width
        viewMainOver?.root?.layoutParams?.height = height
        viewMainOver?.root?.minWidth = (ScreenUtils.getScreenWidth() * 0.6).toInt()
        viewMainOver?.root?.minHeight = height
        viewMainOver?.root?.setCenter()
        AssistsWindowManager.addAssistsWindowLayout(viewMainOver?.root)
    }

    fun hideOverlay() {
        AssistsWindowManager.removeView(viewMainOver?.root)
    }

    private fun beginStart(view: ViewMainOverBinding) {
        with(view) {
            clearLog()
            showLog()
            isAutoScrollLog = true
            btnCloseLog.isVisible = false
            btnStop.isVisible = true
        }
    }

    private fun loadStepListData() {
        CoroutineScope(Dispatchers.IO).launch {
            val showRange = DateTimeUtil.getCurrentDayRange()
            // val offset = TimeZone.getDefault().rawOffset
            val rawDataList = if (isFilterUser && Constants.showDataFilterUserName.isNotBlank())
                dao.queryRangeDataListByUserName(showRange.start, showRange.end, Constants.showDataFilterUserName, 1, Int.MAX_VALUE)
            else
                dao.queryRangeDataList(showRange.start, showRange.end, 1, Int.MAX_VALUE)

            val resolveData = ResolveDataUtil.rawDataToStaticsModel(rawDataList, isFoldData)
            withContext(Dispatchers.Main) {
                dataListAdapter?.setList(resolveData)
            }

        }
    }

    override fun onStepStop() {
        log("已停止")
    }

    private fun stop() {
        if (StepManager.isStop) {
            showOption()
            return
        }
        StepManager.isStop = true
        isAutoScrollLog = false
        viewMainOver?.btnStop?.isVisible = false
        viewMainOver?.btnShowDataList?.isVisible = false
        viewMainOver?.btnCloseLog?.isVisible = true
    }

    fun showLog() {
        viewMainOver?.llOption?.isVisible = false
        viewMainOver?.llLog?.isVisible = true
    }

    fun showOption() {
        viewMainOver?.llOption?.isVisible = true
        viewMainOver?.llLog?.isVisible = false
    }

    fun clear() {
        StepManager.stepListeners.remove(this)
        viewMainOver = null
    }

    private val logStr: StringBuilder = StringBuilder()
    fun log(value: Any) {
        LogUtil.i(TAG, value.toString())

        if (logStr.length > 1000) logStr.delete(0, 50)
        if (logStr.isNotEmpty()) logStr.append("\n")
        logStr.append(TimeUtils.getNowString())
        logStr.append("\n")
        logStr.append(value.toString())
        viewMainOver?.tvLog?.text = logStr
    }

    fun clearLog() {
        logStr.delete(0, logStr.length)
        viewMainOver?.tvLog?.text = ""
    }

    var isAutoScrollLog = true
        set(value) {
            if (value) onAutoScrollLog()
            viewMainOver?.btnStopScrollLog?.text = if (value) "停止滚动" else "继续滚动"
            field = value
        }

    private fun onAutoScrollLog() {
        viewMainOver?.scrollView?.fullScroll(NestedScrollView.FOCUS_DOWN)
        ThreadUtils.runOnUiThreadDelayed({
            if (!isAutoScrollLog) return@runOnUiThreadDelayed
            onAutoScrollLog()
        }, 250)
    }

    enum class ShowType {
        LOG,
        FIND_USER,
        SINGLE_LOG
    }
}