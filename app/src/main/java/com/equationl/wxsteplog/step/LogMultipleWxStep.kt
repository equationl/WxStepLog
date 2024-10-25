package com.equationl.wxsteplog.step

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.view.accessibility.AccessibilityNodeInfo
import com.equationl.wxsteplog.db.DbUtil
import com.equationl.wxsteplog.model.LogUserMode
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

class LogMultipleWxStep : StepImpl() {
    companion object {
        private const val TARGET_PACKAGE_NAME = "com.tencent.mm"
        private const val TAG = "LogMultipleWxStep"
    }

    override fun onImpl(collector: StepCollector) {
        collector.next(StepTag.STEP_1) { step ->
            val setting = step.data as WxStepLogSetting
            OverManager.log("当前参数：$setting")
            OverManager.log("启动微信")
            Intent().apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                component = ComponentName(TARGET_PACKAGE_NAME, "com.tencent.mm.ui.LauncherUI")
                try {
                    Assists.service?.startActivity(this)
                } catch (e: ActivityNotFoundException) {
                    OverManager.log("无法启动【微信】，你安装微信了吗？")
                    return@next Step.none
                }
            }
            return@next Step.get(StepTag.STEP_2, data = setting)
        }.next(StepTag.STEP_2) { step ->
            val setting = step.data as WxStepLogSetting
            Assists.findByText("微信").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.right < Assists.getX(1080, 270) && screen.top > Assists.getY(1920, 1850)) {
                    OverManager.log("已打开微信主页，点击【微信】")
                    it.findFirstParentClickable()?.click()
                    return@next Step.get(StepTag.STEP_3, data = setting)
                }
            }

            if (Assists.getPackageName() == TARGET_PACKAGE_NAME) {
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
            val setting = step.data as WxStepLogSetting
            val nodes = Assists.getAllNodes()
            for (node in nodes) {
                // FIXME 需要确保唯一性
                if (node?.text?.contains("微信运动") == true) {
                    OverManager.log("已进入微信主页，点击【微信运动】")
                    node.findFirstParentClickable()?.click()
                    return@next Step.get(StepTag.STEP_4, data = setting)
                }
            }

            if (Assists.getPackageName() == TARGET_PACKAGE_NAME) {
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
            val setting = step.data as WxStepLogSetting
            Assists.findByText("步数排行榜").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.top > Assists.getY(1920, 1800)) {
                    OverManager.log("已进入微信主页，点击【步数排行榜】")
                    it.findFirstParentClickable()?.click()
                    return@next Step.get(StepTag.STEP_5, data = setting)
                }
            }

            if (Assists.getPackageName() == TARGET_PACKAGE_NAME) {
                OverManager.log("没有查找到【步数排行榜】，但是当前已处于微信 APP 中，返回")
                Assists.back()
            }

            if (step.repeatCount == 5) {
                OverManager.log("已重复 5 次依旧没有找到【步数排行榜】，返回第一步")
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
                    delay(1000)
                    return@next Step.get(StepTag.STEP_5, data = setting)
                }
            }

            var listView = getListView()

            if (listView == null) {
                OverManager.log("没有找到列表，返回第一步")
                return@next Step.get(StepTag.STEP_1, data = setting)
            }

            val idModel: StepListIdModel?
            var listChildren = listView.getChildren()
            if (listChildren.size >= 2) {
                idModel = getBaseIds(listChildren)
                OverManager.log("基准id数据为 $idModel")
                if (idModel == null) {
                    OverManager.log("没有找到可用基准数据，返回第一步")
                    return@next Step.get(StepTag.STEP_1, data = setting)
                }
            }
            else {
                OverManager.log("当前运动数据列表数量不符合需求，需要 2，当前为 ${listChildren.size}，返回第一步")
                return@next Step.get(StepTag.STEP_1, data = setting)
            }

            val alreadyLogNameList = mutableListOf<String>()

            while (true) {
                for (item in listChildren) {
                    if (item?.viewIdResourceName == idModel.itemParentId) {
                        val orderText = item.findById(idModel.itemOrderId).firstOrNull()?.text
                        val nameText = item.findById(idModel.itemNameId).firstOrNull()?.text
                        val stepText = item.findById(idModel.itemStepId).firstOrNull()?.text
                        val likeText = item.findById(idModel.itemLikeId).firstOrNull()?.text

                        OverManager.log("查找到数据，排名：$orderText, 名称：$nameText, 步数：$stepText, 点赞: $likeText")

                        if (!orderText.isNullOrBlank() && !nameText.isNullOrBlank() && !stepText.isNullOrBlank() && !likeText.isNullOrBlank()) {
                            if (setting.logUserMode == LogUserMode.Multiple) {
                                if (!setting.userNameList.contains(nameText.toString())) {
                                    OverManager.log("当前用户 $nameText 不在需要记录的列表中，忽略本次记录")
                                    continue
                                }
                            }
                            if (alreadyLogNameList.contains(nameText.toString())) {
                                OverManager.log("当前用户 $nameText 已记录过，忽略本次记录")
                                continue
                            }

                            // save data
                            DbUtil.saveData(
                                stepNum = stepText.toString().toIntOrNull(),
                                likeNum = likeText.toString().toIntOrNull(),
                                userName = nameText.toString(),
                                userOrder = orderText.toString().toIntOrNull(),
                                logUserMode = setting.logUserMode,
                            )
                            alreadyLogNameList.add(nameText.toString())
                        }
                        else {
                            OverManager.log("数据不完整，忽略本次记录")
                        }
                    }
                    else {
                        OverManager.log("未查找到列表项数据，忽略本次记录")
                    }
                }

                val endFlag = listView.findByText("邀请朋友").isNotEmpty()
                if (endFlag) {
                    OverManager.log("已到达最后一页，返回")
                    Assists.back()
                    val delay = Utils.getIntervalTime(setting)
                    OverManager.log("间隔 $delay ms 后继续")
                    delay(delay)
                    OverManager.log("间隔时间到，继续记录")
                    return@next Step.get(StepTag.STEP_4, data = setting)
                }

                OverManager.log("本页已记录完成，滚动到下一页")
                listView?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                delay(1000)

                // 这里需要重新拿一下 listview 对象，不然不知道为什么 listView.getChildren() 返回的不是完整数据
                listView = getListView()
                if (listView == null) {
                    OverManager.log("没有查找到数据，忽略本页")
                    continue
                }
                listChildren = listView.getChildren()
            }

            OverManager.log("运行异常，重复查找")
            return@next Step.repeat
        }
    }

    private fun getBaseIds(list: List<AccessibilityNodeInfo?>): StepListIdModel? {
        // 基准 item 用于确定 view 的 id
        val baseItem = list[1]!!
        val textNode = mutableListOf<AccessibilityNodeInfo>()
        for (node in baseItem.getNodes()) {
            if (!node.text.isNullOrBlank()) {
                textNode.add(node)
            }
        }

        if (textNode.size != 4) {
            OverManager.log("基准数据查找失败，需要数量 4， 当前为 ${textNode.size}")
            return null
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

    private fun getListView(): AccessibilityNodeInfo? {
        val listView = Assists.findByTags("android.widget.ListView").firstOrNull()
        // fixme 这个需要确定一下先
//        if (listView == null) {
//            listView = Assists.findByTags("androidx.recyclerview.widget.RecyclerView")
//        }
        return listView
    }

}