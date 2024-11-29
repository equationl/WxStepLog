package com.equationl.wxsteplog.ui.widget

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.equationl.wxsteplog.step.OverManager
import com.equationl.wxsteplog.ui.view.statistics.state.StatisticsShowRange
import com.equationl.wxsteplog.util.DateTimeUtil
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState

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
                    state.setSelection(0L, System.currentTimeMillis() + DateTimeUtil.DAY_MILL_SECOND_TIME)
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
fun LoadingDialog(
    content: String,
    onDismissRequest: () -> Unit
) {

    Dialog(
        onDismissRequest = onDismissRequest,
        DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Column(
            modifier = Modifier
                .sizeIn(minWidth = 200.dp, minHeight = 200.dp)
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            if (content.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(content)
            }
        }
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

@Composable
fun ExportConfirmDialog(
    onConfirmWithFilter: () -> Unit,
    onConfirmAll: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "取消")
            }
            TextButton(onClick = onConfirmAll) {
                Text(text = "全部")
            }
            TextButton(onClick = onConfirmWithFilter) {
                Text(text = "筛选")
            }
        },
        title = {
            Text(text = "导出数据")
        },
        text = {
            Text(text = "请选择导出数据类型：“全部” 表示导出当前已记录的全部数据；“筛选” 表示导出当前筛选条件下的数据")
        }
    )
}

@Composable
fun ChooseUserNameDialog(
    showState: MaterialDialogState,
    initUserNameList: List<String>,
    allUserNameList: List<String>,
    onConfirm: (List<String>) -> Unit,
) {
    val chooseUserNameList = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        chooseUserNameList.addAll(initUserNameList)
    }

    MaterialDialog(
        dialogState = showState,
        buttons = {
            positiveButton("确定") {
                onConfirm(chooseUserNameList.toList())
            }
            negativeButton("取消") {
                showState.hide()
            }
        },
        backgroundColor = MaterialTheme.colorScheme.background,
        autoDismiss = false
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .heightIn(min = 180.dp)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("请选择需要记录的用户名", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    OverManager.showFindUser()
                }) {
                    Text(text = "刷新数据")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (allUserNameList.isEmpty()) {
                Text(text = "暂无数据，请点击上方“刷新数据”按钮")
            }
            LazyColumn {
                items(
                    allUserNameList.size,
                ) {
                    val userName = allUserNameList[it]
                    val isChecked = chooseUserNameList.contains(userName)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Checkbox(isChecked, onCheckedChange = { isChecked ->
                            if (isChecked) {
                                chooseUserNameList.add(userName)
                            }
                            else {
                                chooseUserNameList.remove(userName)
                            }
                        })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = userName)
                    }
                }
            }
        }
    }
}