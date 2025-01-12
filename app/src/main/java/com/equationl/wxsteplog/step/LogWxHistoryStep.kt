package com.equationl.wxsteplog.step

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.equationl.wxsteplog.constants.Constants
import com.equationl.wxsteplog.db.DbUtil
import com.equationl.wxsteplog.model.LogModel
import com.equationl.wxsteplog.model.StepListIdModel
import com.equationl.wxsteplog.util.DateTimeUtil
import com.ven.assists.Assists
import com.ven.assists.Assists.click
import com.ven.assists.Assists.findById
import com.ven.assists.Assists.findByText
import com.ven.assists.Assists.findFirstParentClickable
import com.ven.assists.Assists.getBoundsInScreen
import com.ven.assists.Assists.getChildren
import com.ven.assists.Assists.getNodes
import com.ven.assists.stepper.Step
import com.ven.assists.stepper.StepCollector
import com.ven.assists.stepper.StepImpl
import kotlinx.coroutines.delay

class LogWxHistoryStep : StepImpl() {
    val alreadyLogDate = mutableListOf<Long>()

    override fun onImpl(collector: StepCollector) {
        collector.next(StepTag.STEP_1) { step ->
            val setting = step.data
            OverManager.log("当前参数：$setting")
            OverManager.log("启动微信")
            alreadyLogDate.clear()
            Intent().apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                component = ComponentName(Constants.wxPkgName.value, Constants.wxLauncherPkg.value)
                try {
                    Assists.service?.startActivity(this)
                } catch (e: ActivityNotFoundException) {
                    OverManager.log("无法启动【微信】，你安装微信了吗？")
                    return@next Step.none
                }
            }
            return@next Step.get(StepTag.STEP_2, data = setting)
        }.next(StepTag.STEP_2) { step ->
            val setting = step.data
            Assists.findByText("微信").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.right < Assists.getX(1080, 270) && screen.top > Assists.getY(1920, 1850)) {
                    OverManager.log("已打开微信主页，点击【微信】")
                    it.findFirstParentClickable()?.click()
                    return@next Step.get(StepTag.STEP_3, data = setting)
                }
            }

            if (Assists.getPackageName() == Constants.wxPkgName.value) {
                OverManager.log("没有查找到【微信】，但是当前已处于微信 APP 中，返回")
                Assists.back()
            }

            if (step.repeatCount == 5) {
                OverManager.log("已重复 5 次依旧没有找到【微信】，返回第一步")
                return@next Step.get(StepTag.STEP_1, data = setting)
            }

            OverManager.log("没有找到【微信】，重复查找")

            return@next Step.repeat
        }.next(StepTag.STEP_3) { step ->
            val setting = step.data
            val nodes = Assists.getAllNodes()
            for (node in nodes) {
                if (node?.text?.contains("微信运动") == true) {
                    OverManager.log("已进入微信主页，点击【微信运动】")
                    node.findFirstParentClickable()?.click()
                    return@next Step.get(StepTag.STEP_4, data = setting)
                }
            }

            if (Assists.getPackageName() == Constants.wxPkgName.value) {
                OverManager.log("没有查找到【微信运动】，但是当前已处于微信 APP 中，返回")
                Assists.back()
            }

            if (step.repeatCount == 5) {
                OverManager.log("已重复 5 次依旧没有找到【微信运动】，返回第一步")
                return@next Step.get(StepTag.STEP_1, data = setting)
            }

            OverManager.log("没有找到【微信运动】，重复查找")

            return@next Step.repeat
        }.next(StepTag.STEP_4) { step ->
            val setting = step.data
            var listView = getRecyclerView()

            if (listView == null) {
                OverManager.log("没有找到列表，返回第一步")
                return@next Step.get(StepTag.STEP_1, data = setting)
            }

            var runningErrorCount = 0

            while (true) {
                if (runningErrorCount > 5) {
                    OverManager.log("累计错误达 $runningErrorCount 次，跳出循环读取")
                    break
                }

                val listViewChildren = listView?.getChildren()
                listViewChildren?.forEach { cardParent ->
                    val cardSubItemList = cardParent?.getChildren()
                    // 这种判断方式不对，如果有点赞消息的话也会判断成卡片（不过其实从点赞消息点进去也没有问题）
                    Log.i("el", "onImpl: className = ${cardSubItemList?.firstOrNull()?.className }, size = ${cardSubItemList?.size}")
                    if (cardSubItemList?.firstOrNull()?.className == "android.widget.TextView" && cardSubItemList.size >= 2) {
                        val dateTimeText = cardSubItemList.first()!!.text.toString()
                        val dateTime =  try {
                            DateTimeUtil.getTimeFromMsgListHeader(dateTimeText)
                        } catch (tr: Throwable) {
                            OverManager.log("$dateTimeText 无法解析，跳过本次循环")
                            return@forEach
                        }
                        if (alreadyLogDate.contains(dateTime)) {
                            OverManager.log("$dateTimeText 已记录，跳过")
                        }
                        else {
                            val clickAbleItem = cardSubItemList[1]!!.getNodes().first { it.isClickable }
                            clickAbleItem.click()
                            delay(1000)
                            OverManager.log("开始记录 $dateTimeText 的数据")
                            val result = getDetailData(dateTime)
                            if (!result) {
                                return@next Step.none
                            }
                            delay(1000)
                        }
                    }
                    else {
                        OverManager.log("当前卡片内容不符合，跳过")
                    }
                }
                OverManager.log("本页已记录完成，滚动到上一页")
                val endFlag = !(listView?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) ?: false)

                if (endFlag) {
                    OverManager.log("已到达最后一页，程序结束，请关闭本窗口后自行返回 APP")
                    return@next Step.none
                }

                delay(1000)
                listView = getRecyclerView()
                if (listView == null) {
                    runningErrorCount++
                    OverManager.log("没有查找到数据，忽略本页")
                    continue
                }
            }

            OverManager.log("运行异常，返回上一步")
            return@next Step.get(StepTag.STEP_3, data = setting)
        }
    }

    /**
     * @return false 消息已过期，true 其他原因返回
     * */
    private suspend fun getDetailData(dateTime: Long): Boolean {
        val nodes = Assists.getAllNodes()
        for (node in nodes) {
            if (node?.text?.contains("正在加载") == true) {
                OverManager.log("正在加载中，等待……")
                delay(1000)
                return getDetailData(dateTime)
            }
        }

        for (node in nodes) {
            if (node?.text?.contains("消息已过期") == true) {
                OverManager.log("消息已过期，结束运行，请关闭本窗口后自行返回 APP")
                return false
            }
        }

        var listView = getListView()

        if (listView == null) {
            OverManager.log("没有找到列表，返回")
            Assists.back()
            return true
        }

        val idModel: StepListIdModel?
        var listChildren = listView.getChildren()
        if (listChildren.size >= 2) {
            idModel = getBaseIds(listChildren)
            OverManager.log("基准id数据为 $idModel")
            if (idModel == null) {
                OverManager.log("没有找到可用基准数据，返回")
                Assists.back()
                return true
            }
        }
        else {
            OverManager.log("当前运动数据列表数量不符合需求，需要 2，当前为 ${listChildren.size}，返回")
            Assists.back()
            return true
        }

        val alreadyLogNameList = mutableListOf<String>()
        var runningErrorCount = 0

        while (true) {
            if (runningErrorCount > 5) {
                OverManager.log("累计错误达 $runningErrorCount 次，跳出循环读取")
                break
            }

            for (item in listChildren) {
                if (item?.viewIdResourceName == idModel.itemParentId) {
                    val orderText = item.findById(idModel.itemOrderId).firstOrNull()?.text
                    val nameNode = item.findById(idModel.itemNameId).firstOrNull()
                    val nameText = nameNode?.text
                    val stepText = item.findById(idModel.itemStepId).firstOrNull()?.text
                    val likeText = item.findById(idModel.itemLikeId).firstOrNull()?.text

                    OverManager.log("查找到数据，排名：$orderText, 名称：$nameText, 步数：$stepText, 点赞: $likeText")

                    if (!orderText.isNullOrBlank() && !nameText.isNullOrBlank() && !stepText.isNullOrBlank() && !likeText.isNullOrBlank()) {

                        if (alreadyLogNameList.contains(nameText.toString())) {
                            OverManager.log("当前用户 $nameText 已记录过，忽略本次记录")
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
                        OverManager.log("数据不完整，忽略本次记录")
                    }
                }
                else {
                    runningErrorCount++
                    OverManager.log("未查找到列表项数据，忽略本次记录")
                }
            }

            val endFlag = listView.findByText("邀请朋友").isNotEmpty()
            if (endFlag) {
                alreadyLogDate.add(dateTime)
                OverManager.log("已到达最后一页，返回")
                Assists.back()
                return true
            }

            OverManager.log("本页已记录完成，滚动到下一页")
            listView?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            delay(1000)

            // 这里需要重新拿一下 listview 对象，不然不知道为什么 listView.getChildren() 返回的不是完整数据
            listView = getListView()
            if (listView == null) {
                runningErrorCount++
                OverManager.log("没有查找到数据，忽略本页")
                continue
            }
            listChildren = listView.getChildren()
        }

        OverManager.log("运行异常，返回")
        Assists.back()
        return true
    }

    private fun getBaseIds(list: List<AccessibilityNodeInfo?>): StepListIdModel? {
        // 基准 item 用于确定 view 的 id
        for (baseItem in list) {
            if (baseItem == null) {
                OverManager.log("baseItem is null")
                continue
            }
            val textNode = mutableListOf<AccessibilityNodeInfo>()
            for (node in baseItem.getNodes()) {
                if (!node.text.isNullOrBlank()) {
                    textNode.add(node)
                }
            }

            if (textNode.size != 4) {
                OverManager.log("基准数据查找失败，需要数量 4， 当前为 ${textNode.size}")
                continue
            }

            // 按照左到右顺序排序
            textNode.sortBy { it.getBoundsInScreen().left }
            return StepListIdModel(
                itemParentId = baseItem.viewIdResourceName,
                itemOrderId = textNode[0].viewIdResourceName,
                itemNameId = textNode[1].viewIdResourceName,
                itemStepId = textNode[2].viewIdResourceName,
                itemLikeId = textNode[3].viewIdResourceName,
            )
        }

        OverManager.log("遍历完毕当前数据后依旧没有找到基准数据")
        return null
    }

    private fun getRecyclerView(): AccessibilityNodeInfo? {
        val listViewList = Assists.findByTags("androidx.recyclerview.widget.RecyclerView")
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
        return Assists.findByTags("android.widget.ListView").firstOrNull()
    }

}