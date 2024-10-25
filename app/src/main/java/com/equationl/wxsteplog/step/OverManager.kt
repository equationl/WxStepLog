package com.equationl.wxsteplog.step

import android.annotation.SuppressLint
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.TimeUtils
import com.equationl.wxsteplog.App
import com.equationl.wxsteplog.R
import com.equationl.wxsteplog.databinding.ViewMainOverBinding
import com.equationl.wxsteplog.model.LogUserMode
import com.equationl.wxsteplog.model.WxStepLogSetting
import com.equationl.wxsteplog.util.log.LogUtil
import com.ven.assists.Assists
import com.ven.assists.AssistsWindowManager
import com.ven.assists.stepper.StepListener
import com.ven.assists.stepper.StepManager

object OverManager : StepListener {
    private const val TAG = "OverManager"
    @SuppressLint("StaticFieldLeak")
    private var viewMainOver: ViewMainOverBinding? = null

    private var setting: WxStepLogSetting? = null

    private fun createView(): ViewMainOverBinding? {
        return Assists.service?.let {
            StepManager.stepListeners.add(this)
            ViewMainOverBinding.inflate(LayoutInflater.from(it)).apply {
                initView(this)
            }

        }
    }

    private fun initView(viewMainOverBinding: ViewMainOverBinding) {
        with(viewMainOverBinding) {
            parent.assistsWindowLayoutWrapperBinding.tvTitle.text = App.instance.baseContext.getText(
                R.string.app_name
            )
            llOption.isVisible = true
            llLog.isVisible = false
            btnCloseLog.isVisible = false

            btnLogWxStep.setOnClickListener {
                if (setting == null) {
                    LogUtil.e(TAG, "setting is null!")
                    return@setOnClickListener
                }
                beginStart(this)

                if (setting!!.logUserMode == LogUserMode.Multiple && setting!!.userNameList.size == 1) {
                    StepManager.execute(LogWxStep::class.java, StepTag.STEP_1, begin = true, data = setting!!)
                }
                else {
                    StepManager.execute(LogMultipleWxStep::class.java, StepTag.STEP_1, begin = true, data = setting!!)
                }
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

            root.setOnCloseClickListener {
                clear()
                return@setOnCloseClickListener false
            }
        }
    }

    fun show(setting: WxStepLogSetting) {
        this.setting = setting

        viewMainOver ?: let {
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
}