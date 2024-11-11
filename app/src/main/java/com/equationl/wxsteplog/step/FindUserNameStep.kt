package com.equationl.wxsteplog.step

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.view.accessibility.AccessibilityNodeInfo
import com.equationl.wxsteplog.constants.Constants
import com.ven.assists.Assists
import com.ven.assists.Assists.click
import com.ven.assists.Assists.findFirstParentClickable
import com.ven.assists.Assists.getBoundsInScreen
import com.ven.assists.Assists.getChildren
import com.ven.assists.Assists.getNodes
import com.ven.assists.stepper.Step
import com.ven.assists.stepper.StepCollector
import com.ven.assists.stepper.StepImpl
import kotlinx.coroutines.delay

class FindUserNameStep : StepImpl() {
    companion object {
        private const val TARGET_PACKAGE_NAME = "com.tencent.mm"
        private const val TAG = "FindUserNameStep"

        private val nameSet = mutableMapOf<String, MutableSet<String>>()
    }

    override fun onImpl(collector: StepCollector) {
        collector.next(StepTag.STEP_1) { step ->
            OverManager.log("启动微信")
            nameSet.clear()
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
            return@next Step.get(StepTag.STEP_2)
        }.next(StepTag.STEP_2) { step ->
            Assists.findByText("通讯录").forEach {
                var screen = it.getBoundsInScreen()
                if (screen.left > Assists.getX(1080, 340) && screen.top > Assists.getX(1920, 1850)) {
                    OverManager.log("已打开微信主页，点击【通讯录】")
                    it.findFirstParentClickable()?.click()

                    delay(1000)

                    OverManager.log("再次点击【通讯录】确保回到顶部")
                    it.findFirstParentClickable()?.click()
                    return@next Step.get(StepTag.STEP_3)
                }
            }

            if (Assists.getPackageName() == TARGET_PACKAGE_NAME) {
                OverManager.log("没有查找到【通讯录】，但是当前已处于微信 APP 中，返回")
                Assists.back()
            }

            if (step.repeatCount == 5) {
                OverManager.log("已重复 5 次依旧没有找到【通讯录】，结束运行")
                return@next Step.get(StepTag.STEP_4)
            }

            OverManager.log("没有找到【通讯录】，重复查找")

            return@next Step.repeat
        }.next(StepTag.STEP_3) { step ->
            var listView = getListView()

            if (listView == null) {
                OverManager.log("没有找到列表，结束运行")
                return@next Step.get(StepTag.STEP_4)
            }

            var listNodes = listView.getNodes()
            while (true) {
                for (item in listNodes) {
                    if (item.className == "android.widget.TextView" && !item?.text.isNullOrBlank()) {
                        OverManager.log("找到一个好友：${item.text}")
                        val id = item.viewIdResourceName
                        if (nameSet[id] == null) {
                            nameSet[id] = mutableSetOf()
                        }
                        nameSet[id]?.add(item.text.toString())
                    }
                }

                var endFlag = false
                var num: Int? = null
                val lastItem = listView?.getChildren()?.lastOrNull()
                for (item in lastItem?.getNodes() ?: listOf()) {
                    if (item.className == "android.widget.TextView" && item?.text?.contains("个朋友") == true) {
                        endFlag = true
                        num = item.text.toString().replace("个朋友", "").toIntOrNull()
                        break
                    }
                }

                if (endFlag) {
                    OverManager.log("已到达最后一页，总计好友 $num 个，结束查找")
                    return@next Step.get(StepTag.STEP_4, data = num)
                }

                OverManager.log("本页已记录完成，滚动到下一页")
                listView?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                delay(50)

                // 这里需要重新拿一下 listview 对象，不然不知道为什么 listView.getChildren() 返回的不是完整数据
                listView = getListView()
                if (listView == null) {
                    OverManager.log("没有查找到数据，忽略本页")
                    continue
                }
                listNodes = listView.getNodes()
            }

            OverManager.log("运行异常，结束运行")
            return@next Step.get(StepTag.STEP_4)
        }.next(StepTag.STEP_4) { step ->
            // 仅保留 id 最多的数据
            val maxNameList = nameSet.maxByOrNull { it.value.size }
            val newList = maxNameList?.value
            if (newList != null) {
                OverManager.log("实际找到 ${newList.size} 个好友")
                newList.addAll(Constants.allUserNameList)
                Constants.allUserNameList.clear()
                Constants.allUserNameList.addAll(newList)
            }
            else {
                OverManager.log("没有找到合法数据！")
            }

            OverManager.log("结束运行，返回 APP")
            Assists.tasks()
            delay(50)
            Assists.tasks()

            delay(500)

            OverManager.hideOverlay()

            return@next Step.none
        }
    }

    private fun getListView(): AccessibilityNodeInfo? {
        Assists.findByTags("android.widget.ListView").forEach {
            val screen = it.getBoundsInScreen()
            if (screen.left >= 0 && screen.left < Assists.getX(1080, 1080) &&
                screen.right >= Assists.getX(1080, 1080)
            ) {
                return it
            }
        }

        Assists.findByTags("androidx.recyclerview.widget.RecyclerView").forEach {
            val screen = it.getBoundsInScreen()
            if (screen.left >= 0 && screen.left < Assists.getX(1080, 1080) &&
                screen.right >= Assists.getX(1080, 1080)
            ) {
                return it
            }
        }

        return null
    }

}