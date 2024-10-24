package com.equationl.wxsteplog.util

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser

fun Any.toJson(): String {
    return Gson().toJson(this)
}

inline fun <reified T> String.fromJson(): T? {
    return try {
        Gson().fromJson(this, T::class.java)
    } catch (e: Exception) {
        Log.w("el, JsonUtil", "fromJson: 转换json失败", e)
        null
    }
}

fun <T> String.fromJsonList(cls: Class<T>?): ArrayList<T> {
    val mList = ArrayList<T>()

    if (this.isBlank()) {
        return mList
    }

    val array = JsonParser.parseString(this).asJsonArray
    if (array != null && array.size() > 0) {
        for (elem in array) {
            mList.add(Gson().fromJson(elem, cls))
        }
    }
    return mList
}