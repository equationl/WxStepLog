package com.equationl.wxsteplog.util

import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.equationl.wxsteplog.model.StepListIdModel
import com.ven.assists.AssistsCore
import com.ven.assists.AssistsCore.containsText
import com.ven.assists.AssistsCore.des
import com.ven.assists.AssistsCore.findById
import com.ven.assists.AssistsCore.getBoundsInScreen
import com.ven.assists.AssistsCore.getNodes

object AccessibilityUtil {
    /**
     * 获取运动排名列表各项目的 ID
     * */
    fun getSportOrderListBaseId(list: List<AccessibilityNodeInfo?>): StepListIdModel? {
        var isLikeText = true
        // 基准 item 用于确定 view 的 id
        for (baseItem in list) {
            if (baseItem == null) {
                LogWrapper.log("baseItem is null")
                continue
            }
            val textNode = mutableListOf<AccessibilityNodeInfo>()
            for (node in baseItem.getNodes()) {
                Log.i("el", "getBaseIds: node = ${node.text},${node.className},${node.des()}")
                if (!node.text.isNullOrBlank()) {
                    textNode.add(node)
                }
                // 新版本的点赞节点发生了变化，不是直接 TextView
                var des = node.des()
                if (des.contains(",")) {
                    var lineList = des.split(",")
                    Log.i("el", "getSportOrderListBaseId: lineList = $lineList")
                    if (lineList.size >= 2 && (lineList[0].contains("赞") || lineList[0].lowercase().contains("like")) && lineList[1].toIntOrNull() != null) {
                        isLikeText = false
                        textNode.add(node)
                    }
                }
            }

            if (textNode.size != 4) {
                LogWrapper.log("基准数据查找失败，需要数量 4， 当前为 ${textNode.size}")
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
                itemLikeIsText = isLikeText
            )
        }

        LogWrapper.log("遍历完毕当前数据后依旧没有找到基准数据", isForceShow = true)
        return null
    }

    /**
     * 从运动排行列表中解析出 item 的 点赞数量
     * */
    fun AccessibilityNodeInfo.getLikeTextFromOrder(idModel: StepListIdModel): String? {
        if (idModel.itemLikeIsText) return this.findById(idModel.itemLikeId).firstOrNull()?.text?.toString()
        else {
            var des = this.findById(idModel.itemLikeId).firstOrNull()?.des() ?: return null
            if (des.contains(",")) {
                var lineList = des.split(",")
                if (lineList.size < 2) return null
                for (item in lineList) {
                    if (item.toIntOrNull() != null) return item
                }

                return null
            }
            else {
                return des.toIntOrNull()?.toString()
            }
        }
    }

    /**
    * 查找第一个列表节点，优先查找 ListView，如果不存在则查找 RecyclerView
    * */
    fun getListView(): AccessibilityNodeInfo? {
        var listView = AssistsCore.findByTags("android.widget.ListView").firstOrNull()
        if (listView == null) {
            listView = AssistsCore.findByTags("androidx.recyclerview.widget.RecyclerView").firstOrNull()
        }

        return listView
    }

    fun  AccessibilityNodeInfo?.includeByMathText(text: String): Boolean {
        var nodes = this?.getNodes() ?: return false
        return nodes.find { it?.text ==  text } != null
    }

    fun  AccessibilityNodeInfo?.includeByContainsText(text: String): Boolean {
        var nodes = this?.getNodes() ?: return false
        return nodes.find { it?.containsText(text) == true } != null
    }
}