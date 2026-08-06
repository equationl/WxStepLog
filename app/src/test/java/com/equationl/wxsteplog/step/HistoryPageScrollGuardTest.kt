package com.equationl.wxsteplog.step

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryPageScrollGuardTest {
    @Test
    fun `reaches end when accepted scroll leaves viewport unchanged`() {
        val page = listOf(
            HistoryCardSnapshot("日期甲", top = 100, bottom = 300),
            HistoryCardSnapshot("日期乙", top = 320, bottom = 520),
        )

        assertTrue(HistoryPageScrollGuard.hasReachedEnd(page, page.toList()))
    }

    @Test
    fun `continues when a new card appears after scroll`() {
        val before = listOf(
            HistoryCardSnapshot("日期甲", top = 100, bottom = 300),
            HistoryCardSnapshot("日期乙", top = 320, bottom = 520),
        )
        val after = listOf(
            HistoryCardSnapshot("日期丙", top = 100, bottom = 300),
            HistoryCardSnapshot("日期甲", top = 320, bottom = 520),
        )

        assertFalse(HistoryPageScrollGuard.hasReachedEnd(before, after))
    }

    @Test
    fun `continues when cards move but visible dates are unchanged`() {
        val before = listOf(
            HistoryCardSnapshot("日期甲", top = 100, bottom = 300),
            HistoryCardSnapshot("日期乙", top = 320, bottom = 520),
        )
        val after = listOf(
            HistoryCardSnapshot("日期甲", top = 20, bottom = 220),
            HistoryCardSnapshot("日期乙", top = 240, bottom = 440),
        )

        assertFalse(HistoryPageScrollGuard.hasReachedEnd(before, after))
    }

    @Test
    fun `does not infer end from two empty snapshots`() {
        assertFalse(HistoryPageScrollGuard.hasReachedEnd(emptyList(), emptyList()))
    }
}
