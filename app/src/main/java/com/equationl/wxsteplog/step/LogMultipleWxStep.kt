package com.equationl.wxsteplog.step

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
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

class LogMultipleWxStep : StepImpl() {
    companion object {
        private const val TAG = "LogMultipleWxStep"
        private const val SIMILARITY_THRESHOLD = 50
    }

    override fun onImpl(collector: StepCollector) {
        collector.next(StepTag.STEP_1) { step ->
            val setting = step.data as WxStepLogSetting
            OverManager.log("开始运行", isForceShow = true)
            OverManager.log("当前参数：$setting")
            OverManager.log("启动微信")
            Intent().apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                component = ComponentName(Constants.wxPkgName.value, Constants.wxLauncherPkg.value)
                try {
                    Assists.service?.startActivity(this)
                } catch (e: ActivityNotFoundException) {
                    OverManager.log("无法启动【微信】，你安装微信了吗？", isForceShow = true)
                    return@next Step.none
                }
            }
            return@next Step.get(StepTag.STEP_2, data = setting)
        }.next(StepTag.STEP_2) { step ->
            val setting = step.data as WxStepLogSetting
            
            // 查找底部导航栏的"微信"标签
            val wxNodes = Assists.findByText("微信")
            val bottomNavItems = wxNodes.filter { node ->
                // 检查是否在屏幕底部区域
                val bounds = node.getBoundsInScreen()
                val isBottomArea = bounds.top > (Assists.getAppHeightInScreen() * 0.85)
                
                // 检查是否有父级元素可点击
                val hasClickableParent = node.findFirstParentClickable() != null
                
                isBottomArea && hasClickableParent
            }
            
            if (bottomNavItems.isNotEmpty()) {
                OverManager.log("已打开微信主页，点击底部导航栏【微信】")
                bottomNavItems.first().findFirstParentClickable()?.click()
                return@next Step.get(StepTag.STEP_3, data = setting)
            }

            if (Assists.getPackageName() == Constants.wxPkgName.value) {
                OverManager.log("没有查找到【微信】，但是当前已处于微信 APP 中，返回")
                Assists.back()
            }

            if (step.repeatCount == 5) {
                OverManager.log("已重复 5 次依旧没有找到【微信】，返回第一步", isForceShow = true)
                return@next Step.get(StepTag.STEP_1, data = setting)
            }

            OverManager.log("没有找到【微信】，重复查找")

            return@next Step.repeat
        }.next(StepTag.STEP_3) { step ->
            val setting = step.data as WxStepLogSetting
            
            // 搜索具有"微信运动"文本的所有节点
            val sportNodes = Assists.findByText("微信运动")
            
            // 检查是否在会话列表中找到微信运动
            val validNodes = sportNodes.filter { node ->
                // 过滤条件：确保文本完全匹配"微信运动"而不是包含
                node.text?.toString() == "微信运动" && 
                // 确保节点可以通过父元素点击
                node.findFirstParentClickable() != null
            }
            
            if (validNodes.isNotEmpty()) {
                OverManager.log("已进入微信主页，点击【微信运动】")
                validNodes.first().findFirstParentClickable()?.click()
                return@next Step.get(StepTag.STEP_4, data = setting)
            }

            if (Assists.getPackageName() == Constants.wxPkgName.value) {
                OverManager.log("没有查找到【微信运动】，但是当前已处于微信 APP 中，返回")
                Assists.back()
            }

            if (step.repeatCount == 5) {
                OverManager.log("已重复 5 次依旧没有找到【微信运动】，返回第一步", isForceShow = true)
                return@next Step.get(StepTag.STEP_1, data = setting)
            }

            OverManager.log("没有找到【微信运动】，重复查找")

            return@next Step.repeat
        }.next(StepTag.STEP_4) { step ->
            val setting = step.data as WxStepLogSetting
            
            // 查找"步数排行榜"相关的按钮或文本
            val rankNodes = Assists.findByText("步数排行榜")
            
            // 筛选有效的排行榜节点
            val validRankNodes = rankNodes.filter { node ->
                // 确保文本完全匹配并且可以点击
                node.text?.toString() == "步数排行榜" && 
                node.findFirstParentClickable() != null
            }
            
            if (validRankNodes.isNotEmpty()) {
                OverManager.log("已进入微信运动页面，点击【步数排行榜】")
                validRankNodes.first().findFirstParentClickable()?.click()
                return@next Step.get(StepTag.STEP_5, data = setting)
            }

            if (Assists.getPackageName() == Constants.wxPkgName.value) {
                OverManager.log("没有查找到【步数排行榜】，但是当前已处于微信 APP 中，返回")
                Assists.back()
            }

            if (step.repeatCount == 5) {
                OverManager.log("已重复 5 次依旧没有找到【步数排行榜】，返回第一步", isForceShow = true)
                return@next Step.get(StepTag.STEP_1, data = setting)
            }

            OverManager.log("没有找到【步数排行榜】，重复查找")

            return@next Step.repeat
        }.next(StepTag.STEP_5) { step ->
            val setting = step.data as WxStepLogSetting
            val nodes = Assists.getAllNodes()
            for (node in nodes) {
                if (node?.text?.contains("正在加载") == true) {
                    OverManager.log("正在加载中，等待……")
                    delay(Constants.runStepIntervalTime.intValue.toLong())
                    return@next Step.get(StepTag.STEP_5, data = setting)
                }
            }

            var listView = getListView()

            if (listView == null) {
                OverManager.log("没有找到列表，返回第一步", isForceShow = true)
                return@next Step.get(StepTag.STEP_1, data = setting)
            }

            val idModel: StepListIdModel?
            var listChildren = listView.getChildren()
            if (listChildren.size >= 2) {
                idModel = getBaseIds(listChildren)
                OverManager.log("基准id数据为 $idModel")
                if (idModel == null) {
                    OverManager.log("没有找到可用基准数据，返回第一步", isForceShow = true)
                    return@next Step.get(StepTag.STEP_1, data = setting)
                }
            }
            else {
                OverManager.log("当前运动数据列表数量不符合需求，需要 2，当前为 ${listChildren.size}，返回第一步", isForceShow = true)
                return@next Step.get(StepTag.STEP_1, data = setting)
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
                                return@next Step.get(StepTag.STEP_4, data = setting)
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
                    return@next Step.get(StepTag.STEP_4, data = setting)
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
            return@next Step.get(StepTag.STEP_4, data = setting)
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
        val listView = Assists.findByTags("android.widget.ListView").firstOrNull()

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

        // 1. 查找"步数"文本节点作为关键指示器
        val findKeyNode = nodes.find { it.text == "步数" }
        if (findKeyNode == null) {
            OverManager.log("没有找到步数关键数据，忽略本次记录")
            return false
        }

        // 2. 查找相关数字数据
        // 先查找所有TextView类型的节点
        val allTextViews = nodes.filter { 
            it.className == "android.widget.TextView" && 
            !it.text.isNullOrBlank() &&
            it.text.toString().toIntOrNull() != null  // 只保留文本为数字的节点
        }
        
        // 3. 根据相对位置查找与"步数"关联的数据
        // 获取步数节点位置
        val keyNodeBound = findKeyNode.getBoundsInScreen()
        
        // 找到"点赞"文本节点
        val likeNode = nodes.find { it.text == "点赞" }
        if (likeNode == null) {
            OverManager.log("没有找到点赞关键数据，忽略本次记录")
            return false
        }
        
        // 获取步数数据节点 - 在与"步数"文本同一行且在其右侧的节点
        val stepCountNode = allTextViews.firstOrNull { textNode ->
            val nodeBound = textNode.getBoundsInScreen()
            // 横向靠近，纵向在同一高度区域
            val isHorizontallyNear = nodeBound.left > keyNodeBound.right
            val isVerticallyAligned = abs(nodeBound.centerY() - keyNodeBound.centerY()) < (keyNodeBound.height() * 1.5)
            
            isHorizontallyNear && isVerticallyAligned
        }
        
        // 获取点赞数据节点 - 在与"点赞"文本同一行且在其右侧的节点
        val likeNodeBound = likeNode.getBoundsInScreen()
        val likeCountNode = allTextViews.firstOrNull { textNode ->
            val nodeBound = textNode.getBoundsInScreen()
            // 横向靠近，纵向在同一高度区域
            val isHorizontallyNear = nodeBound.left > likeNodeBound.right
            val isVerticallyAligned = abs(nodeBound.centerY() - likeNodeBound.centerY()) < (likeNodeBound.height() * 1.5)
            
            isHorizontallyNear && isVerticallyAligned
        }

        if (stepCountNode == null || likeCountNode == null) {
            OverManager.log("查找的数据不完整，忽略本次记录")
            Assists.back()
            return false
        }

        OverManager.log("查找到数据，步数：${stepCountNode.text}, 点赞: ${likeCountNode.text}")
        // save data
        DbUtil.saveData(
            stepNum = stepCountNode.text.toString().toIntOrNull(),
            likeNum = likeCountNode.text.toString().toIntOrNull(),
            userName = nameNode.text.toString(),
            userOrder = userOrder,
            logUserMode = LogModel.Single
        )

        return true
    }

}