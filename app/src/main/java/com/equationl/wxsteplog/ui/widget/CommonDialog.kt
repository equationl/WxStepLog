package com.equationl.wxsteplog.ui.widget

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import com.equationl.wxsteplog.ui.view.statistics.state.StatisticsShowRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeRangePickerDialog(
    initValue: StatisticsShowRange,
    onFilterDate: (range: StatisticsShowRange) -> Unit,
    onDismissRequest: () -> Unit
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initValue.start,
        initialSelectedEndDateMillis = initValue.end,
    )

    state.setSelection(initValue.start, initValue.end)

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(text = "取消")
            }

            TextButton(
                onClick = {
                    state.setSelection(0L, System.currentTimeMillis())
                }
            ) {
                Text(text = "全选")
            }

            TextButton(
                onClick = {
                    onDismissRequest()
                    val startMillis = state.selectedStartDateMillis ?: 0L
                    var endMillis = state.selectedEndDateMillis
                    if (endMillis == null) {
                        endMillis = startMillis + 1
                    }
                    else {
                        // 不包含截止日期
                        //endMillis -= 1
                    }


                    // FIXME 返回的数据被 +8 了，看一下是不是时区设置错了
                    Log.i("el", "DateTimeRangePickerDialog: start = $startMillis, end = $endMillis")
                    onFilterDate(StatisticsShowRange(startMillis, endMillis))
                }
            ) {
                Text(text = "确定")
            }
        }
    ) {
        DateRangePicker(
            state = state,
            headline = null,
            title = null
        )
    }
}


@Composable
fun CommonConfirmDialog(
    title: String,
    content: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "取消")
            }
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = content)
        }
    )
}