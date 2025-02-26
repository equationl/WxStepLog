package com.equationl.wxsteplog.util

import android.util.Log
import com.equationl.wxsteplog.constants.Constants

object CsvUtil {
    fun encodeCsvLineString(vararg data: Any?): String {
        val delimiter = Constants.csvDelimiter.value
        Log.i("el", "encodeCsvLineString: delimiter = $delimiter")
        val result = StringBuilder()

        data.forEachIndexed { index, item ->
            item?.toString()?.let { str ->
                // 处理特殊字符转义
                val needsQuotes = str.any { it == delimiter[0] || it == '\n' || it == '\r' || it == '"' }

                if (needsQuotes) {
                    result.append("\"")
                    result.append(str.replace("\"", "\"\""))  // 转义双引号
                    result.append("\"")
                } else {
                    result.append(str)
                }
            } ?: run {
                // 保持空字段不写入 "null"
                result.append("")
            }

            if (index != data.lastIndex) {
                result.append(delimiter)
            }
        }

        result.append("\n")
        return result.toString()
    }

    fun encodeCsvLineByte(vararg data: Any?): ByteArray {
        return encodeCsvLineString(*data).toByteArray(Charsets.UTF_8)
    }

    /**
     * 符合 RFC 4180 的 CSV 行解析
     * 特性：
     * 1. 处理带引号的字段 (例: "value,with,commas")
     * 2. 处理转义双引号 (例: "value with ""quotes""")
     * 3. 支持自定义分隔符
     */
    fun decodeCsvLine(line: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false
        var lastCharIsQuote = false

        for (c in line) {
            when {
                c == '"' -> {
                    if (inQuotes && lastCharIsQuote) {
                        // 转义双引号：连续两个引号
                        currentField.append('"')
                        lastCharIsQuote = false
                    } else {
                        lastCharIsQuote = !lastCharIsQuote
                        inQuotes = !inQuotes
                    }
                }
                c == delimiter && !inQuotes -> {
                    // 结束当前字段
                    result.add(currentField.toString())
                    currentField.clear()
                }
                else -> {
                    currentField.append(c)
                    lastCharIsQuote = false
                }
            }
        }

        // 添加最后一个字段
        result.add(currentField.toString())

        // 验证引号闭合
        if (inQuotes) {
            throw IllegalArgumentException("Unclosed quotes in CSV line")
        }

        return result
    }
}