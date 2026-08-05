package com.equationl.wxsteplog.util

import android.os.SystemClock

/** Lightweight key diagnostics retained even when detailed logs are disabled. */
class ContinuousReadMetrics {
    private var startTime = 0L
    private var pageCount = 0
    private var dbWriteTime = 0L
    private var detailWaitTime = 0L
    private var listLookupTime = 0L

    fun beginIfNeeded() {
        if (startTime == 0L) startTime = SystemClock.elapsedRealtime()
    }

    fun addDbWriteTime(time: Long) { dbWriteTime += time }
    fun addDetailWaitTime(time: Long) { detailWaitTime += time }
    fun addListLookupTime(time: Long) { listLookupTime += time }
    fun onPageRead() { pageCount++ }

    fun finish(reason: String) {
        if (startTime == 0L) return
        val runtime = Runtime.getRuntime()
        val usedMemoryMb = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        LogWrapper.log(
            "本轮读取性能：reason=$reason, total=${SystemClock.elapsedRealtime() - startTime}ms, " +
                "pages=$pageCount, detail=${detailWaitTime}ms, db=${dbWriteTime}ms, " +
                "listLookup=${listLookupTime}ms, heap=${usedMemoryMb}MB",
            isForceShow = true,
        )
        reset()
    }

    private fun reset() {
        startTime = 0L
        pageCount = 0
        dbWriteTime = 0L
        detailWaitTime = 0L
        listLookupTime = 0L
    }
}
