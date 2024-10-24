package com.equationl.wxsteplog.step

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import com.equationl.wxsteplog.model.WxStepLogSetting
import com.ven.assists.Assists
import com.ven.assists.Assists.click
import com.ven.assists.Assists.findFirstParentClickable
import com.ven.assists.Assists.getBoundsInScreen
import com.ven.assists.stepper.Step
import com.ven.assists.stepper.StepCollector
import com.ven.assists.stepper.StepImpl
import kotlinx.coroutines.delay

class LogMultipleWxStep : StepImpl() {
    companion object {
        private const val TARGET_PACKAGE_NAME = "com.tencent.mm"
        private const val TAG = "LogMultipleWxStep"
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

            // TODO 在这里滚动并查找、记录数据



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
        }
    }

}