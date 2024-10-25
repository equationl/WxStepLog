package com.equationl.wxsteplog.step

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.equationl.wxsteplog.db.DbUtil
import com.equationl.wxsteplog.model.WxStepLogSetting
import com.equationl.wxsteplog.util.Utils
import com.ven.assists.Assists
import com.ven.assists.Assists.click
import com.ven.assists.Assists.findFirstParentClickable
import com.ven.assists.Assists.getBoundsInScreen
import com.ven.assists.stepper.Step
import com.ven.assists.stepper.StepCollector
import com.ven.assists.stepper.StepImpl
import kotlinx.coroutines.delay
import kotlin.math.abs

class LogWxStep : StepImpl() {
    companion object {
        private const val TARGET_PACKAGE_NAME = "com.tencent.mm"
        private const val TAG = "LogWxStep"
        private const val SIMILARITY_THRESHOLD = 50
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

//            Assists.findByText("微信运动").forEach {
//                it.log(TAG)
//                // val screen = it.getBoundsInScreen()
//                // if (screen.top < Assists.getY(1920, 500)) {
//                    OverManager.log("已进入微信主页，点击【微信运动】")
//                    it.findFirstParentClickable()?.click()
//                    return@next Step.get(StepTag.STEP_4, data = step.data)
//                //}
//            }

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

                if (node?.text?.contains(setting.userNameList.first()) == true) {
                    OverManager.log("已进入微信主页，点击【${setting.userNameList.first()}】")
                    node.findFirstParentClickable()?.click()
                    return@next Step.get(StepTag.STEP_6, data = setting)
                }
            }

            if (Assists.getPackageName() == TARGET_PACKAGE_NAME) {
                OverManager.log("没有查找到【${setting.userNameList.first()}】，但是当前已处于微信 APP 中，返回")
                Assists.back()
            }

            if (step.repeatCount == 5) {
                OverManager.log("已重复 5 次依旧没有找到【${setting.userNameList.first()}】，返回第一步")
                return@next Step.get(StepTag.STEP_1, data = setting)
            }

            OverManager.log("没有找到【${setting.userNameList.first()}】，重复查找")

            return@next Step.repeat
        }.next(StepTag.STEP_6) { step ->
            val setting = step.data as WxStepLogSetting
            var findKeyNode:  AccessibilityNodeInfo? = null
            val nodes = Assists.getAllNodes()
            for (node in nodes) {
                if (node?.text?.contains("正在加载") == true) {
                    OverManager.log("正在加载中，等待……")
                    delay(1000)
                    return@next Step.get(StepTag.STEP_6, data = setting)
                }

                if (node?.text == "步数") {
                    findKeyNode = node
                    break
                }
            }

            if (findKeyNode == null) {
                OverManager.log("没有找到步数关键数据，返回上一步")
                Assists.back()
                return@next Step.get(StepTag.STEP_5, data = setting)
            }

            val dataList = mutableListOf<AccessibilityNodeInfo>()
            val keyNodeBound = findKeyNode.getBoundsInScreen()
            for (node in nodes) {
                val nodeBound = node.getBoundsInScreen()
                if (abs(nodeBound.top - keyNodeBound.top) < SIMILARITY_THRESHOLD) {
                    dataList.add(node)
                }
            }

            Log.i(TAG, "onImpl: 原始数据集合: ${dataList.map { it.text }}")

            dataList.removeIf { it.className !=  "android.widget.TextView" || it.text.toString().toIntOrNull() == null}
            Log.i(TAG, "onImpl: 筛选后的数据集: ${dataList.map { it.text }}")
            dataList.sortBy { it.getBoundsInScreen().left }
            Log.i(TAG, "onImpl: 排序后的数据集: ${dataList.map { it.text }}")

            if (dataList.size != 2) {
                OverManager.log("查找的数据不完整，返回上一步")
                Assists.back()
                return@next Step.get(StepTag.STEP_5, data = setting)
            }

            OverManager.log("查找到数据，步数：${dataList[0].text}, 点赞: ${dataList[1].text}")
            // save data
            DbUtil.saveData(
                stepNum = dataList[0].text.toString().toIntOrNull(),
                likeNum = dataList[1].text.toString().toIntOrNull(),
                userName = setting.userNameList.first(),
                userOrder = null,
                logUserMode = setting.logUserMode
            )
            OverManager.log("数据记录完成，返回上一页")
            Assists.back()
            val delay = Utils.getIntervalTime(setting)
            OverManager.log("间隔 $delay ms 后继续")
            delay(delay)
            OverManager.log("间隔时间到，继续记录")
            return@next Step.get(StepTag.STEP_5, data = setting)
        }
    }

}