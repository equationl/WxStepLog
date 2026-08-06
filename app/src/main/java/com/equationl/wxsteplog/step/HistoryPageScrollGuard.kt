package com.equationl.wxsteplog.step

internal data class HistoryCardSnapshot(
    val headerText: String,
    val top: Int,
    val bottom: Int,
)

/**
 * Some WeChat versions report a successful backward-scroll action even when the history
 * list is already at its oldest page. Treat an unchanged, non-empty viewport as the end.
 */
internal object HistoryPageScrollGuard {
    fun hasReachedEnd(
        beforeScroll: List<HistoryCardSnapshot>,
        afterScroll: List<HistoryCardSnapshot>,
    ): Boolean {
        return beforeScroll.isNotEmpty() && beforeScroll == afterScroll
    }
}
