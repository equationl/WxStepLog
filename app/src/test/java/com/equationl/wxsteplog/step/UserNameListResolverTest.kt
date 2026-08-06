package com.equationl.wxsteplog.step

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserNameListResolverTest {
    @Test
    fun `selects contacts instead of a larger alphabetical-index group`() {
        val groups = listOf(
            setOf("新的朋友", "群聊", "标签"),
            setOf("F", "J", "S", "W", "X"),
            setOf("联系人甲", "联系人乙", "联系人丙", "联系人丁", "联系人戊"),
            setOf("3个朋友"),
        )

        val result = UserNameListResolver.resolve(groups)

        assertEquals(
            listOf("联系人甲", "联系人乙", "联系人丙", "联系人丁", "联系人戊"),
            result,
        )
    }

    @Test
    fun `keeps a single-letter name when it belongs to a contact group`() {
        val groups = listOf(
            setOf("A", "B", "C"),
            setOf("A", "小明"),
            setOf("2个朋友"),
        )

        assertEquals(listOf("A", "小明"), UserNameListResolver.resolve(groups))
    }

    @Test
    fun `returns empty when only structural text exists`() {
        val groups = listOf(
            setOf("新的朋友", "群聊", "标签", "公众号"),
            setOf("A", "B", "#"),
            setOf("0个朋友"),
        )

        assertTrue(UserNameListResolver.resolve(groups).isEmpty())
    }
}
