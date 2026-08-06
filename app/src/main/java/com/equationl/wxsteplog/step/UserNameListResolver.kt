package com.equationl.wxsteplog.step

/**
 * Resolves the contact-name group from all text nodes exposed by WeChat's contact list.
 *
 * WeChat reuses one view id for the contact names and other ids for section headers,
 * shortcuts and the friend-count footer. Selecting the largest group alone is not safe:
 * a small contact list can contain more alphabetical section headers than contacts.
 */
internal object UserNameListResolver {
    private val shortcutTexts = setOf(
        "新的朋友",
        "群聊",
        "标签",
        "公众号",
        "企业微信联系人",
    )
    private val sectionHeaderRegex = Regex("^[A-Z#]$")
    private val friendCountRegex = Regex("^\\d+个朋友$")

    fun resolve(textGroups: Collection<Set<String>>): List<String> {
        return textGroups
            .asSequence()
            // Alphabetical section headers share one id. Drop that whole group instead
            // of removing individual one-letter names from a genuine contact group.
            .filterNot { group ->
                group.isNotEmpty() && group.all { text -> sectionHeaderRegex.matches(text.trim()) }
            }
            .map { group ->
                group.filterNot { text -> isStructuralText(text.trim()) }
            }
            .filter { it.isNotEmpty() }
            .maxByOrNull { it.size }
            .orEmpty()
    }

    private fun isStructuralText(text: String): Boolean {
        return text.isBlank() || text in shortcutTexts || friendCountRegex.matches(text)
    }
}
