package com.equationl.wxsteplog.step

import android.content.ComponentName
import android.content.Intent
import android.view.accessibility.AccessibilityNodeInfo
import com.equationl.wxsteplog.constants.Constants
import com.equationl.wxsteplog.db.DbUtil
import com.equationl.wxsteplog.model.LogModel
import com.equationl.wxsteplog.model.StepListIdModel
import com.equationl.wxsteplog.util.AccessibilityUtil
import com.equationl.wxsteplog.util.AccessibilityUtil.getLikeTextFromOrder
import com.equationl.wxsteplog.util.AccessibilityUtil.includeByContainsText
import com.equationl.wxsteplog.util.AccessibilityUtil.includeByMathText
import com.equationl.wxsteplog.util.DateTimeUtil
import com.equationl.wxsteplog.util.LogWrapper
import com.ven.assists.AssistsCore
import com.ven.assists.AssistsCore.click
import com.ven.assists.AssistsCore.findById
import com.ven.assists.AssistsCore.findFirstParentClickable
import com.ven.assists.AssistsCore.getBoundsInScreen
import com.ven.assists.AssistsCore.getChildren
import com.ven.assists.AssistsCore.getNodes
import com.ven.assists.stepper.Step
import com.ven.assists.stepper.StepCollector
import com.ven.assists.stepper.StepImpl
import kotlinx.coroutines.delay

class LogWxHistoryStep : StepImpl() {
    val alreadyLogDate = mutableListOf<Long>()

    override fun onImpl(collector: StepCollector) {
        collector.next(StepTag.STEP_1) { step ->
            val setting = step.data
            LogWrapper.log("开始运行", isForceShow = true)
            LogWrapper.log("当前参数：$setting")
            LogWrapper.log("启动微信")
            alreadyLogDate.clear()
            Intent().apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                component = ComponentName(Constants.wxPkgName.value, Constants.wxLauncherPkg.value)
                if (!AssistsCore.launchApp(this)) {
                    LogWrapper.log("无法启动【微信】，你安装微信了吗？", isForceShow = true)
                    return@next Step.none
                }
            }
            LogWrapper.log("等待打开微信")
            delay(1000)
            return@next Step.get(StepTag.STEP_2, data = setting)
        }.next(StepTag.STEP_2) { step ->
            val setting = step.data
            val nodes = AssistsCore.getAllNodes()
            for (node in nodes) {
                if (node?.text?.contains("微信") == true) {
                    val screen = node.getBoundsInScreen()
                    LogWrapper.log("当前【微信】的坐标为：${screen.top},${screen.right},${screen.bottom},${screen.left}")
                    if (screen.right < AssistsCore.getX(1080, Constants.wxViewLimit.value.right.toInt()) && screen.top > AssistsCore.getY(1920, Constants.wxViewLimit.value.top.toInt())) {
                        LogWrapper.log("已打开微信主页，点击【微信】")
                        node.findFirstParentClickable()?.click()
                        LogWrapper.log("等待返回顶部")
                        delay(1000)
                        return@next Step.get(StepTag.STEP_3, data = setting)
                    }
                }
            }

            if (AssistsCore.getPackageName() == Constants.wxPkgName.value) {
                LogWrapper.log("没有查找到【微信】，但是当前已处于微信 APP 中，返回")
                AssistsCore.back()
            }

            if (step.repeatCount == 5) {
                LogWrapper.log("已重复 5 次依旧没有找到【微信】，返回第一步", isForceShow = true)
                return@next Step.get(StepTag.STEP_1, data = setting)
            }

            LogWrapper.log("没有找到【微信】，重复查找")

            return@next Step.repeat
        }.next(StepTag.STEP_3) { step ->
            val setting = step.data
            val nodes = AssistsCore.getAllNodes()
            for (node in nodes) {
                if (node?.text?.contains("微信运动") == true) {
                    LogWrapper.log("已进入微信主页，点击【微信运动】")
                    node.findFirstParentClickable()?.click()
                    LogWrapper.log("等待打开微信运动")
                    delay(1000)
                    return@next Step.get(StepTag.STEP_4, data = setting)
                }
            }

            if (AssistsCore.getPackageName() == Constants.wxPkgName.value) {
                LogWrapper.log("没有查找到【微信运动】，但是当前已处于微信 APP 中，返回")
                AssistsCore.back()
            }

            if (step.repeatCount == 5) {
                LogWrapper.log("已重复 5 次依旧没有找到【微信运动】，返回第一步", isForceShow = true)
                return@next Step.get(StepTag.STEP_1, data = setting)
            }

            LogWrapper.log("没有找到【微信运动】，重复查找")

            return@next Step.repeat
        }.next(StepTag.STEP_4) { step ->
            val setting = step.data
            var listView = getRecyclerView()

            if (listView == null) {
                LogWrapper.log("没有找到列表，返回第一步", isForceShow = true)
                return@next Step.get(StepTag.STEP_1, data = setting)
            }

            var runningErrorCount = 0

            while (true) {
                if (runningErrorCount > 5) {
                    LogWrapper.log("累计错误达 $runningErrorCount 次，跳出循环读取", isForceShow = true)
                    break
                }

                val listViewChildren = listView?.getChildren()

                var isMsgExpired = false

                for (cardParent in listViewChildren ?: listOf()) {
                    val cardSubItemList = cardParent?.getChildren()
                    if (isLegalHistoryCard(cardSubItemList)) {
                        val dateTimeText = cardSubItemList!!.first()!!.text.toString()
                        val dateTime =  try {
                            DateTimeUtil.getTimeFromMsgListHeader(dateTimeText)
                        } catch (tr: Throwable) {
                            LogWrapper.log("$dateTimeText 无法解析，跳过本次循环")
                            continue
                        }
                        if (alreadyLogDate.contains(dateTime)) {
                            LogWrapper.log("$dateTimeText 已记录，跳过")
                        }
                        else {
                            val clickAbleItem = cardSubItemList[1]!!.getNodes().first { it.isClickable }
                            clickAbleItem.click()
                            delay(Constants.runStepIntervalTime.intValue.toLong())
                            LogWrapper.log("开始记录 $dateTimeText 的数据")
                            val result = getDetailData(dateTime)
                            if (!result) {
                                isMsgExpired = true
                            }
                            delay(Constants.runStepIntervalTime.intValue.toLong())
                        }
                    }
                    else {
                        LogWrapper.log("当前卡片内容不符合，跳过")
                    }
                }

                if (isMsgExpired) {
                    return@next Step.none
                }

                LogWrapper.log("本页已记录完成，滚动到上一页")
                // TODO 如果只有一页时会始终认为滚动成功，导致无法判断是否滚动完成
                val endFlag = listView?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) != true

                if (endFlag) {
                    LogWrapper.log("已到达最后一页，程序结束，请关闭本窗口后自行返回 APP", isForceShow = true)
                    return@next Step.none
                }

                delay(Constants.runStepIntervalTime.intValue.toLong())
                listView = getRecyclerView()
                if (listView == null) {
                    runningErrorCount++
                    LogWrapper.log("没有查找到数据，忽略本页")
                    continue
                }
            }

            LogWrapper.log("运行异常，返回上一步")
            return@next Step.get(StepTag.STEP_3, data = setting)
        }
    }

    /**
     * @return false 消息已过期，true 其他原因返回
     * */
    private suspend fun getDetailData(dateTime: Long): Boolean {
        val nodes = AssistsCore.getAllNodes()
        for (node in nodes) {
            if (node?.text?.contains("正在加载") == true) {
                LogWrapper.log("正在加载中，等待……")
                delay(Constants.runStepIntervalTime.intValue.toLong())
                return getDetailData(dateTime)
            }
        }

        for (node in nodes) {
            if (node?.text?.contains("消息已过期") == true) {
                LogWrapper.log("消息已过期，结束运行，请关闭本窗口后自行返回 APP", isForceShow = true)
                return false
            }
        }

        var listView = getListView()

        if (listView == null) {
            LogWrapper.log("没有找到列表，返回")
            AssistsCore.back()
            return true
        }

        val idModel: StepListIdModel?
        var listChildren = listView.getChildren()
        if (listChildren.size >= 2) {
            idModel = getBaseIds(listChildren)
            LogWrapper.log("基准id数据为 $idModel")
            if (idModel == null) {
                LogWrapper.log("没有找到可用基准数据，返回")
                AssistsCore.back()
                return true
            }
        }
        else {
            LogWrapper.log("当前运动数据列表数量不符合需求，需要 2，当前为 ${listChildren.size}，返回")
            AssistsCore.back()
            return true
        }

        val alreadyLogNameList = mutableListOf<String>()
        var runningErrorCount = 0

        while (true) {
            if (runningErrorCount > 5) {
                LogWrapper.log("累计错误达 $runningErrorCount 次，跳出循环读取", isForceShow = true)
                break
            }

            for (item in listChildren) {
                if (item?.viewIdResourceName == idModel.itemParentId) {
                    val orderText = item.findById(idModel.itemOrderId).firstOrNull()?.text
                    val nameNode = item.findById(idModel.itemNameId).firstOrNull()
                    val nameText = nameNode?.text
                    val stepText = item.findById(idModel.itemStepId).firstOrNull()?.text
                    val likeText = item.getLikeTextFromOrder(idModel)

                    LogWrapper.log("查找到数据，排名：$orderText, 名称：$nameText, 步数：$stepText, 点赞: $likeText")

                    if (!orderText.isNullOrBlank() && !nameText.isNullOrBlank() && !stepText.isNullOrBlank() && !likeText.isNullOrBlank()) {

                        if (alreadyLogNameList.contains(nameText.toString())) {
                            LogWrapper.log("当前用户 $nameText 已记录过，忽略本次记录")
                            continue
                        }

                        // 保存数据
                        DbUtil.saveHistoryData(
                            stepNum = stepText.toString().toIntOrNull(),
                            likeNum = likeText.toString().toIntOrNull(),
                            userName = nameText.toString(),
                            userOrder = orderText.toString().toIntOrNull(),
                            logStartTime = Constants.logWxHistoryStepStartTime,
                            dataTime = dateTime,
                            logUserMode = LogModel.HistoryLog
                        )
                        alreadyLogNameList.add(nameText.toString())
                    }
                    else {
                        LogWrapper.log("数据不完整，忽略本次记录")
                    }
                }
                else {
                    runningErrorCount++
                    LogWrapper.log("未查找到列表项数据，忽略本次记录")
                }
            }

            val endFlag = listView.includeByMathText("邀请朋友")
            if (endFlag) {
                alreadyLogDate.add(dateTime)
                LogWrapper.log("已到达最后一页，返回")
                AssistsCore.back()
                return true
            }

            LogWrapper.log("本页已记录完成，滚动到下一页")
            listView?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            delay(Constants.runStepIntervalTime.intValue.toLong())

            // 这里需要重新拿一下 listview 对象，不然不知道为什么 listView.getChildren() 返回的不是完整数据
            listView = getListView()
            if (listView == null) {
                runningErrorCount++
                LogWrapper.log("没有查找到数据，忽略本页")
                continue
            }
            listChildren = listView.getChildren()
        }

        LogWrapper.log("运行异常，返回", isForceShow = true)
        AssistsCore.back()
        return true
    }

    private fun getBaseIds(list: List<AccessibilityNodeInfo?>): StepListIdModel? {
        return AccessibilityUtil.getSportOrderListBaseId(list)
    }

    private fun getRecyclerView(): AccessibilityNodeInfo? {
        val listViewList = AssistsCore.findByTags("androidx.recyclerview.widget.RecyclerView")
        var listView: AccessibilityNodeInfo? = null
        if (listViewList.size == 1) {
            listView = listViewList.firstOrNull()
        }
        else {
            listViewList.forEach {
                if (it.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD)) {
                    listView = it
                    return@forEach
                }
            }
        }

        if (listView == null) {
            listView = listViewList.lastOrNull()
        }

        return listView
    }

    private fun getListView(): AccessibilityNodeInfo? {
        return AccessibilityUtil.getListView()
    }

    // TODO 这里判断条件没有考虑其他消息内容，例如点赞消息（虽然从点赞消息点进去内容记录也可以）
    private fun isLegalHistoryCard(cardSubItemList: ArrayList<AccessibilityNodeInfo>?): Boolean {
        return cardSubItemList?.firstOrNull()?.className == "android.widget.TextView"
                && cardSubItemList.size >= 2
                // 忽略初始卡片
                && !cardSubItemList[1].includeByContainsText("这一次，与你分享运动的快乐") && !cardSubItemList[1].includeByContainsText("嘿，新朋友快进来")
    }

}