package com.equationl.wxsteplog.step

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import com.equationl.wxsteplog.constants.Constants
import com.equationl.wxsteplog.db.DbUtil
import com.equationl.wxsteplog.model.LogModel
import com.equationl.wxsteplog.model.LogSettingMode
import com.equationl.wxsteplog.model.StepListIdModel
import com.equationl.wxsteplog.model.WxStepLogSetting
import com.equationl.wxsteplog.util.AccessibilityUtil
import com.equationl.wxsteplog.util.AccessibilityUtil.getLikeTextFromOrder
import com.equationl.wxsteplog.util.AccessibilityUtil.findFirstNodeById
import com.equationl.wxsteplog.util.AccessibilityUtil.findFirstTextById
import com.equationl.wxsteplog.util.AccessibilityUtil.includeByMathText
import com.equationl.wxsteplog.util.AccessibilityUtil.recycleCompat
import com.equationl.wxsteplog.util.DateTimeUtil
import com.equationl.wxsteplog.util.DateTimeUtil.formatDateTime
import com.equationl.wxsteplog.util.ContinuousReadMetrics
import com.equationl.wxsteplog.util.LogWrapper
import com.equationl.wxsteplog.util.Utils
import com.ven.assists.AssistsCore
import com.ven.assists.AssistsCore.click
import com.ven.assists.AssistsCore.findFirstParentClickable
import com.ven.assists.AssistsCore.getBoundsInScreen
import com.ven.assists.AssistsCore.getChildren
import com.ven.assists.service.AssistsService
import com.ven.assists.stepper.Step
import com.ven.assists.stepper.StepCollector
import com.ven.assists.stepper.StepImpl
import kotlinx.coroutines.delay
import kotlin.math.abs

class LogMultipleWxStepByHand : StepImpl() {
    companion object {
        private const val TAG = "LogMultipleWxStep"
        private const val SIMILARITY_THRESHOLD = 50
    }

    private var stopDaySet = mutableSetOf<String>()
    private val readMetrics = ContinuousReadMetrics()

    override fun onImpl(collector: StepCollector) {
        collector.next(StepTag.STEP_1) { step ->
            LogWrapper.log("当前为半自动模式，请确保启动时处于微信的 “微信运动” 页面")

            val setting = step.data as WxStepLogSetting
            val nodes = AssistsCore.getAllNodes()
            try {
                for (node in nodes) {
                    if (node.text?.contains("步数排行榜") != true) continue
                    val screen = node.getBoundsInScreen()
                    LogWrapper.log("当前【步数排行榜】的真实坐标为：${screen.top},${screen.right},${screen.bottom},${screen.left}")
                    LogWrapper.log("以1920x1080的基准坐标为：${AssistsCore.getY(1920, screen.top)},${AssistsCore.getX(1080, screen.right)},${AssistsCore.getY(1920, screen.bottom)},${AssistsCore.getX(1080, screen.left)}")
                    if (screen.top > AssistsCore.getY(1920, Constants.stepOrderLimit.value.top.toInt())) {
                        LogWrapper.log("已进入微信主页，点击【步数排行榜】")
                        node.findFirstParentClickable()?.click()
                        LogWrapper.log("等待进入步数排行榜")
                        delay(1000)
                        return@next Step.get(StepTag.STEP_2, data = setting)
                    }
                }
            } finally {
                nodes.recycleCompat()
            }

            if (AssistsCore.getPackageName() == Constants.wxPkgName.value) {
                LogWrapper.log("没有查找到【步数排行榜】，但是当前已处于微信 APP 中")
                // AssistsCore.back()
            }

            if (step.repeatCount == 5) {
                LogWrapper.log("已重复 5 次依旧没有找到【步数排行榜】，请确认当前是否处于微信运动主界面", isForceShow = true)
                return@next Step.none
            }

            LogWrapper.log("没有找到【步数排行榜】，重复查找")

            return@next Step.repeat
        }.next(StepTag.STEP_2) { step ->
            val setting = step.data as WxStepLogSetting
            readMetrics.beginIfNeeded()
            val nodes = AssistsCore.getAllNodes()
            try {
                for (node in nodes) {
                    if (node.text?.contains("正在加载") != true) continue
                    LogWrapper.log("正在加载中，等待……")
                    delay(Constants.runStepIntervalTime.intValue.toLong())
                    return@next Step.get(StepTag.STEP_2, data = setting)
                }
            } finally {
                nodes.recycleCompat()
            }

            var startedAt = SystemClock.elapsedRealtime()
            var listView = getListView()
            readMetrics.addListLookupTime(SystemClock.elapsedRealtime() - startedAt)

            if (listView == null) {
                LogWrapper.log("没有找到列表，重复查找", isForceShow = true)
                delay(1000)
                return@next Step.repeat
            }

            val idModel: StepListIdModel?
            var listChildren = listView.getChildren()
            if (listChildren.size >= 2) {
                idModel = getBaseIds(listChildren)
                LogWrapper.log("基准id数据为 $idModel")
                if (idModel == null) {
                    LogWrapper.log("没有找到可用基准数据，重复查找", isForceShow = true)
                    delay(1000)
                    return@next Step.repeat
                }
            }
            else {
                LogWrapper.log("当前运动数据列表数量不符合需求，需要 2，当前为 ${listChildren.size}，重复查找", isForceShow = true)
                delay(1000)
                return@next Step.repeat
            }

            val alreadyLogNameList = mutableSetOf<String>()
            /** 是否需要记录所有的列表数据 */
            val isNeedLogAllList = setting.logUserMode == LogSettingMode.All
            /** 是否需要进入指定用户的详情中记录 */
            val isNeedEnterDetail = setting.logUserMode == LogSettingMode.Multiple || (setting.logUserMode == LogSettingMode.All && setting.isAllModelSpecialUser)

            var runningErrorCount = 0

            while (true) {
                if (runningErrorCount > 5) {
                    LogWrapper.log("累计错误达 $runningErrorCount 次，跳出循环读取", isForceShow = true)
                    break
                }

                var foundListItem = false
                for (item in listChildren) {
                    if (item?.viewIdResourceName == idModel.itemParentId) {
                        foundListItem = true
                        val orderText = item.findFirstTextById(idModel.itemOrderId)
                        val nameText = item.findFirstTextById(idModel.itemNameId)
                        val stepText = item.findFirstTextById(idModel.itemStepId)
                        val likeText = item.getLikeTextFromOrder(idModel)

                        LogWrapper.log("查找到数据，排名：$orderText, 名称：$nameText, 步数：$stepText, 点赞: $likeText")

                        if (!orderText.isNullOrBlank() && !nameText.isNullOrBlank() && !stepText.isNullOrBlank() && !likeText.isNullOrBlank()) {
                            if (!isNeedLogAllList && alreadyLogNameList.size == setting.userNameList.size) {
                                LogWrapper.log("已记录所有需要用户，返回", isForceShow = true)
                                readMetrics.finish("指定用户读取完成")
                                AssistsCore.back()

                                if (setting.isAutoReset) {
                                    val currentTime = DateTimeUtil.getCurrentMinute()
                                    if (currentTime >= setting.restTime.first && System.currentTimeMillis().formatDateTime("yyyy-MM-dd") !in stopDaySet) {
                                        return@next checkStopTime(setting)
                                    }
                                }

                                val delay = Utils.getIntervalTime(setting)
                                LogWrapper.log("间隔 $delay ms 后继续", isForceShow = true)
                                delay(delay)
                                LogWrapper.log("间隔时间到，继续记录", isForceShow = true)
                                return@next Step.get(StepTag.STEP_1, data = setting)
                            }

                            if (!isNeedLogAllList) {
                                if (!setting.userNameList.contains(nameText)) {
                                    LogWrapper.log("当前用户 $nameText 不在需要记录的列表中，忽略本次记录")
                                    continue
                                }
                            }
                            if (alreadyLogNameList.contains(nameText)) {
                                LogWrapper.log("当前用户 $nameText 已记录过，忽略本次记录")
                                continue
                            }

                            if (isNeedEnterDetail && setting.userNameList.contains(nameText)) {
                                LogWrapper.log("当前用户 $nameText 需要记录详情，进入详情页")
                                val nameNode = item.findFirstNodeById(idModel.itemNameId)
                                startedAt = SystemClock.elapsedRealtime()
                                val result = try {
                                    nameNode != null && getFromDetail(nameNode, orderText.toIntOrNull())
                                } finally {
                                    listOf(nameNode).recycleCompat()
                                    readMetrics.addDetailWaitTime(SystemClock.elapsedRealtime() - startedAt)
                                }
                                if (result) {
                                    alreadyLogNameList.add(nameText)
                                }
                                LogWrapper.log("详情读取结束，返回列表")
                                AssistsCore.back()
                                delay(Constants.runStepIntervalTime.intValue.toLong())
                            }
                            else {
                                startedAt = SystemClock.elapsedRealtime()
                                DbUtil.saveData(
                                    stepNum = stepText.toIntOrNull(),
                                    likeNum = likeText.toIntOrNull(),
                                    userName = nameText,
                                    userOrder = orderText.toIntOrNull(),
                                    logUserMode = if (setting.logUserMode == LogSettingMode.Multiple) LogModel.Multiple else LogModel.ALL,
                                )
                                readMetrics.addDbWriteTime(SystemClock.elapsedRealtime() - startedAt)
                                alreadyLogNameList.add(nameText)
                            }
                        }
                        else {
                            LogWrapper.log("数据不完整，忽略本次记录")
                        }
                    }
                    else {
                        LogWrapper.log("未查找到列表项数据，忽略本次记录")
                    }
                }

                runningErrorCount = if (foundListItem) 0 else runningErrorCount + 1
                if (foundListItem) readMetrics.onPageRead()

                val endFlag = listView.includeByMathText("邀请朋友")
                if (endFlag) {
                    LogWrapper.log("已到达最后一页，返回", isForceShow = true)
                    readMetrics.finish("全部页面读取完成")
                    AssistsCore.back()

                    if (setting.isAutoReset) {
                        val currentTime = DateTimeUtil.getCurrentMinute()
                        if (currentTime >= setting.restTime.first && System.currentTimeMillis().formatDateTime("yyyy-MM-dd") !in stopDaySet) {
                            return@next checkStopTime(setting)
                        }
                    }

                    val delay = Utils.getIntervalTime(setting)
                    LogWrapper.log("间隔 $delay ms 后继续", isForceShow = true)
                    delay(delay)
                    LogWrapper.log("间隔时间到，继续记录", isForceShow = true)
                    return@next Step.get(StepTag.STEP_1, data = setting)
                }

                LogWrapper.log("本页已记录完成，滚动到下一页")
                listView?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                (listChildren + listOfNotNull(listView)).recycleCompat()
                delay(Constants.runStepIntervalTime.intValue.toLong())

                // 这里需要重新拿一下 listview 对象，不然不知道为什么 listView.getChildren() 返回的不是完整数据
                startedAt = SystemClock.elapsedRealtime()
                val nextListView = getListView()
                readMetrics.addListLookupTime(SystemClock.elapsedRealtime() - startedAt)
                if (nextListView == null) {
                    runningErrorCount++
                    listView = null
                    listChildren = arrayListOf()
                    LogWrapper.log("没有查找到数据，忽略本页")
                    continue
                }
                listView = nextListView
                listChildren = listView.getChildren()
            }

            LogWrapper.log("运行异常，返回上一步", isForceShow = true)
            readMetrics.finish("连续空页面过多")
            return@next Step.get(StepTag.STEP_1, data = setting)
        }
    }

    private fun getBaseIds(list: List<AccessibilityNodeInfo?>): StepListIdModel? {
        return AccessibilityUtil.getSportOrderListBaseId(list)
    }

    private fun getListView(): AccessibilityNodeInfo? {
        return AccessibilityUtil.getListView()
    }

    private suspend fun getFromDetail(nameNode: AccessibilityNodeInfo, userOrder: Int?): Boolean {
        val userName = nameNode.text?.toString() ?: return false
        LogWrapper.log("点击【$userName】")
        nameNode.findFirstParentClickable()?.click()

        while (true) {
            delay(Constants.runStepIntervalTime.intValue.toLong())
            val nodes = AssistsCore.getAllNodes()
            try {
                if (nodes.any { it.text == "正在加载" }) {
                    LogWrapper.log("正在加载中，等待……")
                    continue
                }

                val findKeyNode = nodes.find { it.text == "步数" }
                if (findKeyNode == null) {
                    // 加载提示可能先于实际内容消失，继续等待，不因设备或网络较慢丢数据。
                    LogWrapper.log("详情数据尚未出现，继续等待……")
                    continue
                }

                val keyNodeBound = findKeyNode.getBoundsInScreen()
                val dataList = nodes
                    .filter {
                        it.className == "android.widget.TextView" &&
                            it.text?.toString()?.toIntOrNull() != null &&
                            abs(it.getBoundsInScreen().top - keyNodeBound.top) < SIMILARITY_THRESHOLD
                    }
                    .sortedBy { it.getBoundsInScreen().left }

                if (dataList.size != 2) {
                    LogWrapper.log("查找的数据不完整，忽略本次记录")
                    return false
                }

                val stepNum = dataList[0].text.toString().toIntOrNull()
                val likeNum = dataList[1].text.toString().toIntOrNull()
                LogWrapper.log("查找到数据，步数：$stepNum, 点赞: $likeNum")
                DbUtil.saveData(stepNum, likeNum, userName, userOrder, LogModel.Single)
                return true
            } finally {
                nodes.recycleCompat()
            }
        }
    }

    private suspend fun checkStopTime(setting: WxStepLogSetting): Step {
        if (setting.restTime.second == null) {
            LogWrapper.log("已到达设置的停止时间，且未设置恢复时间，停止运行", isForceShow = true)
            AssistsCore.home()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                AssistsService.instance?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
            }
            return Step.none
        }
        else {
            var currentTime = DateTimeUtil.getCurrentMinute()
            var timeLeft = DateTimeUtil.calculateDuration(currentTime, setting.restTime.second!!)
            LogWrapper.log("已到达设置的停止时间，将在 $timeLeft 分钟后恢复运行", isForceShow = true)
            delay(timeLeft * 60_000L)
            LogWrapper.log("恢复时间到，继续记录", isForceShow = true)
            stopDaySet.add(System.currentTimeMillis().formatDateTime("yyyy-MM-dd"))
            return Step.get(StepTag.STEP_4, data = setting)
        }
    }

}
