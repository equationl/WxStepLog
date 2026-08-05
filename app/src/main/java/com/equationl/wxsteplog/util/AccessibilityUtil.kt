package com.equationl.wxsteplog.util

import android.util.Log
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import com.equationl.wxsteplog.model.StepListIdModel
import com.ven.assists.AssistsCore
import com.ven.assists.AssistsCore.containsText
import com.ven.assists.AssistsCore.des
import com.ven.assists.AssistsCore.findById
import com.ven.assists.AssistsCore.getBoundsInScreen
import com.ven.assists.AssistsCore.getNodes
import java.util.Collections
import java.util.IdentityHashMap

object AccessibilityUtil {
    /**
     * Android 12L 及以下的 AccessibilityNodeInfo 持有系统连接资源，需要主动回收。
     * Android 13 起 recycle() 是 no-op，因此只在旧系统执行。
     */
    fun Iterable<AccessibilityNodeInfo?>.recycleCompat(exclude: AccessibilityNodeInfo? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return

        val recycled = Collections.newSetFromMap(IdentityHashMap<AccessibilityNodeInfo, Boolean>())
        for (node in this) {
            if (node == null || node === exclude || !recycled.add(node)) continue
            @Suppress("DEPRECATION")
            runCatching { node.recycle() }
        }
    }

    fun AccessibilityNodeInfo.findFirstTextById(id: String): String? {
        val nodes = findById(id)
        return try {
            nodes.firstOrNull()?.text?.toString()
        } finally {
            nodes.recycleCompat()
        }
    }

    fun AccessibilityNodeInfo.findFirstNodeById(id: String): AccessibilityNodeInfo? {
        val nodes = findById(id)
        val first = nodes.firstOrNull()
        nodes.recycleCompat(exclude = first)
        return first
    }

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
            val itemNodes = baseItem.getNodes()
            try {
                val textNode = mutableListOf<AccessibilityNodeInfo>()
                for (node in itemNodes) {
                    Log.i("el", "getBaseIds: node = ${node.text},${node.className},${node.des()}")
                    if (!node.text.isNullOrBlank()) {
                        textNode.add(node)
                    }
                    // 新版本的点赞节点发生了变化，不是直接 TextView
                    val des = node.des()
                    if (des.contains(",")) {
                        val lineList = des.split(",")
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
            } finally {
                itemNodes.recycleCompat(exclude = baseItem)
            }
        }

        LogWrapper.log("遍历完毕当前数据后依旧没有找到基准数据", isForceShow = true)
        return null
    }

    /**
     * 从运动排行列表中解析出 item 的 点赞数量
     * */
    fun AccessibilityNodeInfo.getLikeTextFromOrder(idModel: StepListIdModel): String? {
        val nodes = this.findById(idModel.itemLikeId)
        try {
            if (idModel.itemLikeIsText) return nodes.firstOrNull()?.text?.toString()

            val des = nodes.firstOrNull()?.des() ?: return null
            if (des.contains(",")) {
                val lineList = des.split(",")
                if (lineList.size < 2) return null
                for (item in lineList) {
                    if (item.toIntOrNull() != null) return item
                }

                return null
            }
            else {
                return des.toIntOrNull()?.toString()
            }
        } finally {
            nodes.recycleCompat()
        }
    }

    /**
    * 查找第一个列表节点，优先查找 ListView，如果不存在则查找 RecyclerView
    * */
    fun getListView(): AccessibilityNodeInfo? {
        // AssistsCore.findByTags() internally walks the complete tree but discards the
        // non-matching node list without recycling it. Keep ownership here instead.
        val nodes = AssistsCore.getAllNodes()
        val listView = nodes.firstOrNull { it.className == "android.widget.ListView" }
            ?: nodes.firstOrNull { it.className == "androidx.recyclerview.widget.RecyclerView" }
        nodes.recycleCompat(exclude = listView)
        return listView
    }

    fun  AccessibilityNodeInfo?.includeByMathText(text: String): Boolean {
        val nodes = this?.getNodes() ?: return false
        return try {
            nodes.any { it.text == text }
        } finally {
            nodes.recycleCompat(exclude = this)
        }
    }

    fun  AccessibilityNodeInfo?.includeByContainsText(text: String): Boolean {
        val nodes = this?.getNodes() ?: return false
        return try {
            nodes.any { it.containsText(text) }
        } finally {
            nodes.recycleCompat(exclude = this)
        }
    }
}
