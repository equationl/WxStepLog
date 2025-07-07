package com.equationl.wxsteplog.overlays

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ScreenUtils
import com.equationl.wxsteplog.databinding.OverlaysLogViewBinding
import com.equationl.wxsteplog.model.ShowType
import com.equationl.wxsteplog.util.LogWrapper
import com.ven.assists.service.AssistsService
import com.ven.assists.service.AssistsServiceListener
import com.ven.assists.stepper.StepManager
import com.ven.assists.utils.CoroutineWrapper
import com.ven.assists.window.AssistsWindowManager
import com.ven.assists.window.AssistsWindowWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@SuppressLint("StaticFieldLeak")
object LogOverlay: AssistsServiceListener {

    var runAutoScrollListJob: Job? = null
    private var logCollectJob: Job? = null

    private val onScrollTouchListener = object : OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    runAutoScrollListJob?.cancel()
                    runAutoScrollListJob = null
                }

                MotionEvent.ACTION_UP -> {
                    runAutoScrollList()
                }
            }
            return false
        }
    }
    private var viewBinding: OverlaysLogViewBinding? = null
        @SuppressLint("ClickableViewAccessibility")
        get() {
            if (field == null) {
                field = OverlaysLogViewBinding.inflate(LayoutInflater.from(AssistsService.instance)).apply {
                    scrollView.setOnTouchListener(onScrollTouchListener)
                    btnClean.setOnClickListener {
                        CoroutineWrapper.launch { LogWrapper.clearLog() }
                    }
                    btnStop.setOnClickListener { StepManager.isStop = true }
                    btnShowDataList.setOnClickListener {
                        DataListOverlay.show()
                    }
                }
            }
            return field
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
                        x = 50
                        y = ScreenUtils.getScreenHeight() / 2 - 200
                    }, onClose = { hide() }).apply {
                        minWidth = (ScreenUtils.getScreenWidth() * 0.6).toInt()
                        minHeight = (ScreenUtils.getScreenHeight() * 0.4).toInt()
                        initialCenter = false
                        viewBinding.tvTitle.text = "日志"
                    }
                }
            }
            return field
        }

    fun show(showType: ShowType) {
        if (!AssistsService.listeners.contains(this)) {
            AssistsService.listeners.add(this)
        }
        if (!AssistsWindowManager.contains(assistWindowWrapper?.getView())) {
            AssistsWindowManager.add(assistWindowWrapper)
            viewBinding?.btnShowDataList?.isVisible = showType == ShowType.LOG
            initLogCollect()
            runAutoScrollList(delay = 0)
        }
    }

    fun hide() {
        AssistsWindowManager.removeView(assistWindowWrapper?.getView())
        logCollectJob?.cancel()
        logCollectJob = null
        runAutoScrollListJob?.cancel()
        runAutoScrollListJob = null
    }

    override fun onUnbind() {
        viewBinding = null
        assistWindowWrapper = null
        logCollectJob?.cancel()
        logCollectJob = null
        runAutoScrollListJob?.cancel()
        runAutoScrollListJob = null
    }


    private fun runAutoScrollList(delay: Long = 5000) {
        runAutoScrollListJob?.cancel()
        runAutoScrollListJob = CoroutineWrapper.launch {
            delay(delay)
            while (true) {
                withContext(Dispatchers.Main) {
                    viewBinding?.scrollView?.smoothScrollBy(0, viewBinding?.scrollView?.getChildAt(0)?.height ?: 0)
                }
                delay(250)
            }
        }
    }

    private fun initLogCollect() {
        logCollectJob?.cancel()
        logCollectJob = CoroutineWrapper.launch {
            withContext(Dispatchers.Main) {
                viewBinding?.apply {
                    tvLog.text = LogWrapper.logCache
                    tvLength.text = "${tvLog.length()}"
                }
            }
            LogWrapper.logAppendValue.collect {
                withContext(Dispatchers.Main) {
                    viewBinding?.apply {
                        tvLog.text = LogWrapper.logCache
                        tvLength.text = "${tvLog.length()}"
                    }
                }
            }
        }
    }
}