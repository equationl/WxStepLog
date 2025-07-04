package com.equationl.wxsteplog.overlays

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import com.blankj.utilcode.util.ScreenUtils
import com.equationl.wxsteplog.constants.Constants
import com.equationl.wxsteplog.databinding.OverlaysMainViewBinding
import com.equationl.wxsteplog.model.ShowType
import com.equationl.wxsteplog.model.WxStepLogSetting
import com.equationl.wxsteplog.step.FindUserNameStep
import com.equationl.wxsteplog.step.LogMultipleWxStep
import com.equationl.wxsteplog.step.LogMultipleWxStepByHand
import com.equationl.wxsteplog.step.LogWxHistoryStep
import com.equationl.wxsteplog.step.StepTag
import com.equationl.wxsteplog.util.LogWrapper
import com.equationl.wxsteplog.util.log.LogUtil
import com.ven.assists.service.AssistsService
import com.ven.assists.service.AssistsServiceListener
import com.ven.assists.stepper.StepManager
import com.ven.assists.window.AssistsWindowManager
import com.ven.assists.window.AssistsWindowWrapper

@SuppressLint("StaticFieldLeak")
object MainOverlay: AssistsServiceListener {
    private const val TAG = "MainOverlay"

    private var setting: WxStepLogSetting? = null
    private var showType = ShowType.LOG


    var viewBinding: OverlaysMainViewBinding? = null
        private set
        get() {
            if (field == null) {
                field = OverlaysMainViewBinding.inflate(LayoutInflater.from(AssistsService.instance))
            }
            return field
        }

    var onClose: ((parent: View) -> Unit)? = {
        AssistsWindowManager.removeView(assistWindowWrapper?.getView())

        StepManager.stepListeners = null
        viewBinding = null
        assistWindowWrapper = null
    }

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
                    }, onClose = this.onClose).apply {
                        minWidth = (ScreenUtils.getScreenWidth() * 0.6).toInt()
                        minHeight = (ScreenUtils.getScreenHeight() * 0.4).toInt()
                        initialCenter = true
//                        viewBinding.tvTitle.text = when(showType) {
//                            ShowType.LOG -> "记录数据"
//                            ShowType.FIND_USER -> "查找用户名"
//                            ShowType.SINGLE_LOG -> "读取数据"
//                        }
                    }
                }
            }
            return field
        }

    fun showStartLog(setting: WxStepLogSetting) {
        showType = ShowType.LOG
        this.setting = setting

        initView()

        if (!AssistsService.listeners.contains(this)) {
            AssistsService.listeners.add(this)
        }

        assistWindowWrapper?.viewBinding?.tvTitle?.text = "记录数据"

        AssistsWindowManager.removeView(assistWindowWrapper?.getView())
        AssistsWindowManager.add(assistWindowWrapper)
    }

    fun showFindUser() {
        showType = ShowType.FIND_USER

        initView()

        if (!AssistsService.listeners.contains(this)) {
            AssistsService.listeners.add(this)
        }

        assistWindowWrapper?.viewBinding?.tvTitle?.text = "查找用户名"

        AssistsWindowManager.removeView(assistWindowWrapper?.getView())
        AssistsWindowManager.add(assistWindowWrapper)
    }

    fun showSingleLog() {
        showType = ShowType.SINGLE_LOG

        initView()

        Constants.logWxHistoryStepStartTime = System.currentTimeMillis()

        if (!AssistsService.listeners.contains(this)) {
            AssistsService.listeners.add(this)
        }

        assistWindowWrapper?.viewBinding?.tvTitle?.text = "读取数据"

        AssistsWindowManager.removeView(assistWindowWrapper?.getView())
        AssistsWindowManager.add(assistWindowWrapper)
    }



    fun hide() {
        AssistsWindowManager.removeView(assistWindowWrapper?.getView())
    }

    override fun onUnbind() {
        viewBinding = null
        assistWindowWrapper = null
    }

    private fun initView() {
        viewBinding?.apply {

            when (showType) {
                ShowType.LOG -> {
                    btnLogWxStep.visibility = View.VISIBLE
                    btnFindUserName.visibility = View.GONE
                    btnLogHistory.visibility = View.GONE
                }
                ShowType.FIND_USER -> {
                    btnLogWxStep.visibility = View.GONE
                    btnFindUserName.visibility = View.VISIBLE
                    btnLogHistory.visibility = View.GONE
                }
                ShowType.SINGLE_LOG -> {
                    btnLogHistory.visibility = View.VISIBLE
                    btnLogWxStep.visibility = View.GONE
                    btnFindUserName.visibility = View.GONE
                }
            }

            btnLogWxStep.setOnClickListener {
                if (setting == null) {
                    LogUtil.e(TAG, "setting is null!")
                    return@setOnClickListener
                }

                LogWrapper.clearLog()
                LogOverlay.show(showType)

                if (setting!!.isAutoRunning) {
                    StepManager.execute(LogMultipleWxStep::class.java, StepTag.STEP_1, begin = true, data = setting!!, delay = Constants.runStepIntervalTime.intValue.toLong())
                }
                else {
                    StepManager.execute(LogMultipleWxStepByHand::class.java, StepTag.STEP_1, begin = true, data = setting!!, delay = Constants.runStepIntervalTime.intValue.toLong())
                }
            }

            btnFindUserName.setOnClickListener {
                LogWrapper.clearLog()
                LogOverlay.show(showType)

                StepManager.execute(FindUserNameStep::class.java, StepTag.STEP_1, begin = true, delay = Constants.runStepIntervalTime.intValue.toLong())
            }

            btnLogHistory.setOnClickListener {
                LogWrapper.clearLog()
                LogOverlay.show(showType)

                StepManager.execute(LogWxHistoryStep::class.java, StepTag.STEP_1, begin = true, delay = Constants.runStepIntervalTime.intValue.toLong())
            }

            btnLog.setOnClickListener {
                LogOverlay.show(showType)
            }
        }
    }
}