package com.equationl.wxsteplog.step

import android.view.accessibility.AccessibilityNodeInfo
import com.equationl.wxsteplog.constants.Constants
import com.equationl.wxsteplog.db.DbUtil
import com.equationl.wxsteplog.model.LogModel
import com.equationl.wxsteplog.model.LogSettingMode
import com.equationl.wxsteplog.model.StepListIdModel
import com.equationl.wxsteplog.model.WxStepLogSetting
import com.equationl.wxsteplog.util.Utils
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
import kotlin.math.abs

class LogMultipleWxStepByHand : StepImpl() {
    companion object {
        private const val TAG = "LogMultipleWxStep"
        private const val SIMILARITY_THRESHOLD = 50
    }

    override fun onImpl(collector: StepCollector) {
        collector.next(StepTag.STEP_1) { step ->
            OverManager.log("当前为半自动模式，请确保启动时处于微信的 “微信运动” 页面")

            val setting = step.data as WxStepLogSetting
            val nodes = Assists.getAllNodes()
            for (node in nodes) {
                println("node.text = ${node?.text}")
                if (node?.text?.contains("步数排行榜") == true) {
                    val screen = node.getBoundsInScreen()
                    OverManager.log("当前【步数排行榜】的真实坐标为：${screen.top},${screen.right},${screen.bottom},${screen.left}")
                    OverManager.log("以1920x1080的基准坐标为：${Assists.getY(1920, screen.top)},${Assists.getX(1080, screen.right)},${Assists.getY(1920, screen.bottom)},${Assists.getX(1080, screen.left)}")
                    if (screen.top > Assists.getY(1920, Constants.stepOrderLimit.value.top.toInt())) {
                        OverManager.log("已进入微信主页，点击【步数排行榜】")
                        node.findFirstParentClickable()?.click()
                        OverManager.log("等待进入步数排行榜")
                        delay(1000)
                        return@next Step.get(StepTag.STEP_2, data = setting)
                    }
                }
            }

            if (Assists.getPackageName() == Constants.wxPkgName.value) {
                OverManager.log("没有查找到【步数排行榜】，但是当前已处于微信 APP 中")
                // Assists.back()
            }

            if (step.repeatCount == 5) {
                OverManager.log("已重复 5 次依旧没有找到【步数排行榜】，请确认当前是否处于微信运动主界面", isForceShow = true)
                return@next Step.none
            }

            OverManager.log("没有找到【步数排行榜】，重复查找")

            return@next Step.repeat
        }.next(StepTag.STEP_2) { step ->
            val setting = step.data as WxStepLogSetting
            val nodes = Assists.getAllNodes()
            for (node in nodes) {
                if (node?.text?.contains("正在加载") == true) {
                    OverManager.log("正在加载中，等待……")
                    delay(Constants.runStepIntervalTime.intValue.toLong())
                    return@next Step.get(StepTag.STEP_2, data = setting)
                }
            }

            var listView = getListView()

            if (listView == null) {
                OverManager.log("没有找到列表，重复查找", isForceShow = true)
                delay(1000)
                return@next Step.repeat
            }

            val idModel: StepListIdModel?
            var listChildren = listView.getChildren()
            if (listChildren.size >= 2) {
                idModel = getBaseIds(listChildren)
                OverManager.log("基准id数据为 $idModel")
                if (idModel == null) {
                    OverManager.log("没有找到可用基准数据，重复查找", isForceShow = true)
                    delay(1000)
                    return@next Step.repeat
                }
            }
            else {
                OverManager.log("当前运动数据列表数量不符合需求，需要 2，当前为 ${listChildren.size}，重复查找", isForceShow = true)
                delay(1000)
                return@next Step.repeat
            }

            val alreadyLogNameList = mutableListOf<String>()
            /** 是否需要记录所有的列表数据 */
            val isNeedLogAllList = setting.logUserMode == LogSettingMode.All
            /** 是否需要进入指定用户的详情中记录 */
            val isNeedEnterDetail = setting.logUserMode == LogSettingMode.Multiple || (setting.logUserMode == LogSettingMode.All && setting.isAllModelSpecialUser)

            var runningErrorCount = 0

            while (true) {
                if (runningErrorCount > 5) {
                    OverManager.log("累计错误达 $runningErrorCount 次，跳出循环读取", isForceShow = true)
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
                            if (!isNeedLogAllList && alreadyLogNameList.size == setting.userNameList.size) {
                                OverManager.log("已记录所有需要用户，返回", isForceShow = true)
                                Assists.back()
                                val delay = Utils.getIntervalTime(setting)
                                OverManager.log("间隔 $delay ms 后继续", isForceShow = true)
                                delay(delay)
                                OverManager.log("间隔时间到，继续记录", isForceShow = true)
                                return@next Step.get(StepTag.STEP_1, data = setting)
                            }

                            if (!isNeedLogAllList) {
                                if (!setting.userNameList.contains(nameText.toString())) {
                                    OverManager.log("当前用户 $nameText 不在需要记录的列表中，忽略本次记录")
                                    continue
                                }
                            }
                            if (alreadyLogNameList.contains(nameText.toString())) {
                                OverManager.log("当前用户 $nameText 已记录过，忽略本次记录")
                                continue
                            }

                            if (isNeedEnterDetail && setting.userNameList.contains(nameText.toString())) {
                                OverManager.log("当前用户 $nameText 需要记录详情，进入详情页")
                                val result = getFromDetail(nameNode, orderText.toString().toIntOrNull())
                                if (result) {
                                    alreadyLogNameList.add(nameText.toString())
                                    OverManager.log("已记录 $nameText 的详情数据，返回列表")
                                    Assists.back()
                                    delay(Constants.runStepIntervalTime.intValue.toLong())
                                }
                            }
                            else {
                                // save data
                                DbUtil.saveData(
                                    stepNum = stepText.toString().toIntOrNull(),
                                    likeNum = likeText.toString().toIntOrNull(),
                                    userName = nameText.toString(),
                                    userOrder = orderText.toString().toIntOrNull(),
                                    logUserMode = if (setting.logUserMode == LogSettingMode.Multiple) LogModel.Multiple else LogModel.ALL,
                                )
                                alreadyLogNameList.add(nameText.toString())
                            }
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
                    OverManager.log("已到达最后一页，返回", isForceShow = true)
                    Assists.back()
                    val delay = Utils.getIntervalTime(setting)
                    OverManager.log("间隔 $delay ms 后继续", isForceShow = true)
                    delay(delay)
                    OverManager.log("间隔时间到，继续记录", isForceShow = true)
                    return@next Step.get(StepTag.STEP_1, data = setting)
                }

                OverManager.log("本页已记录完成，滚动到下一页")
                listView?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                delay(Constants.runStepIntervalTime.intValue.toLong())

                // 这里需要重新拿一下 listview 对象，不然不知道为什么 listView.getChildren() 返回的不是完整数据
                listView = getListView()
                if (listView == null) {
                    runningErrorCount++
                    OverManager.log("没有查找到数据，忽略本页")
                    continue
                }
                listChildren = listView.getChildren()
            }

            OverManager.log("运行异常，返回上一步", isForceShow = true)
            return@next Step.get(StepTag.STEP_1, data = setting)
        }
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

        OverManager.log("遍历完毕当前数据后依旧没有找到基准数据", isForceShow = true)
        return null
    }

    private fun getListView(): AccessibilityNodeInfo? {
        var listView = Assists.findByTags("android.widget.ListView").firstOrNull()
        if (listView == null) {
            listView = Assists.findByTags("androidx.recyclerview.widget.RecyclerView").firstOrNull()
        }

        return listView
    }

    private suspend fun getFromDetail(nameNode: AccessibilityNodeInfo, userOrder: Int?, isClick: Boolean = true): Boolean {
        if (isClick) {
            OverManager.log("点击【${nameNode.text}】")
            nameNode.findFirstParentClickable()?.click()
        }

        delay(Constants.runStepIntervalTime.intValue.toLong())
        val nodes = Assists.getAllNodes()

        if (nodes.find { it.text == "正在加载"} != null) {
            OverManager.log("正在加载中，等待……")
            getFromDetail(nameNode, userOrder, false)
        }

        val findKeyNode = nodes.find { it.text == "步数" }
        if (findKeyNode == null) {
            OverManager.log("没有找到步数关键数据，忽略本次记录")
            return false
        }

        val dataList = mutableListOf<AccessibilityNodeInfo>()
        val keyNodeBound = findKeyNode.getBoundsInScreen()
        for (node in nodes) {
            val nodeBound = node.getBoundsInScreen()
            if (abs(nodeBound.top - keyNodeBound.top) < SIMILARITY_THRESHOLD) {
                dataList.add(node)
            }
        }

        dataList.removeIf { it.className !=  "android.widget.TextView" || it.text.toString().toIntOrNull() == null}
        dataList.sortBy { it.getBoundsInScreen().left }

        if (dataList.size != 2) {
            OverManager.log("查找的数据不完整，忽略本次记录")
            Assists.back()
            return false
        }

        OverManager.log("查找到数据，步数：${dataList[0].text}, 点赞: ${dataList[1].text}")
        // save data
        DbUtil.saveData(
            stepNum = dataList[0].text.toString().toIntOrNull(),
            likeNum = dataList[1].text.toString().toIntOrNull(),
            userName = nameNode.text.toString(),
            userOrder = userOrder,
            logUserMode = LogModel.Single
        )

        return true
    }

}