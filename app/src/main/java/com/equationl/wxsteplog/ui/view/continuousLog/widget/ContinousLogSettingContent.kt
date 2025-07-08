package com.equationl.wxsteplog.ui.view.continuousLog.widget

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.equationl.wxsteplog.constants.Constants
import com.equationl.wxsteplog.model.LogSettingMode
import com.equationl.wxsteplog.ui.widget.ChooseUserNameDialog
import com.equationl.wxsteplog.ui.widget.TimePickerDialog
import com.equationl.wxsteplog.util.DateTimeUtil
import com.vanpra.composematerialdialogs.rememberMaterialDialogState

/**
 *
 * @param restTime 停止运行时间，Pair(停止时间，恢复运行时间)，单位都是 min
 * */
@Composable
fun ContinuousLogSettingContent(
    logUserModel: MutableState<LogSettingMode>,
    userNameList: SnapshotStateList<String>,
    intervalTime: MutableState<String>,
    isRandomInterval: MutableState<Boolean>,
    randomIntervalValue: MutableState<String>,
    showDaraFilterUserName: MutableState<String>,
    isAllModelSpecialUser: MutableState<Boolean>,
    isAutoRunning: MutableState<Boolean>,
    isAutoReset: MutableState<Boolean>,
    restTime: MutableState<Pair<Int, Int?>>,
) {
    var context = LocalContext.current
    var isShowMoreSetting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                isAutoRunning.value,
                onCheckedChange = {
                    isAutoRunning.value = it
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Text("自动运行")

            Spacer(Modifier.width(8.dp))

            IconButton(onClick = {
                Toast.makeText(
                    context,
                    "不勾选该选项则需要手动打开 微信-微信运动 后再点击悬浮窗中的 “开始记录” ",
                    Toast.LENGTH_LONG
                ).show()
            }) {
                Icon(Icons.Default.Info, contentDescription = "info")
            }
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            SingleChoiceSegmentedButtonRow {
                LogSettingMode.entries.forEachIndexed { index, statisticsShowScale ->
                    SegmentedButton(selected = statisticsShowScale == logUserModel.value, onClick = { logUserModel.value = statisticsShowScale }, shape = SegmentedButtonDefaults.itemShape(index = index, count = LogSettingMode.entries.size)) {
                        Text(text = statisticsShowScale.showName)
                    }
                }
            }
        }

        if (logUserModel.value == LogSettingMode.Multiple) {
            Spacer(modifier = Modifier.height(8.dp))
            UserInputContent(userNameList)
        }

        if (logUserModel.value == LogSettingMode.All) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(isAllModelSpecialUser.value, onCheckedChange = { isAllModelSpecialUser.value = it})
                Spacer(modifier = Modifier.width(8.dp))
                Text("特别关注用户")
            }
            AnimatedVisibility(
                visible = isAllModelSpecialUser.value
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    UserInputContent(userNameList)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(isShowMoreSetting, onCheckedChange = { isShowMoreSetting = it})
            Spacer(modifier = Modifier.width(8.dp))
            Text("更多设置")
        }

        AnimatedVisibility(isShowMoreSetting) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = intervalTime.value,
                    onValueChange = {
                        if (it.isDigitsOnly() || it.isEmpty()) {
                            intervalTime.value = it
                        }
                    },
                    singleLine = true,
                    label = {
                        Text(text = "记录间隔时间(ms)")
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(isRandomInterval.value, onCheckedChange = { isRandomInterval.value = it})
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("是否随机修改间隔时间")
                }
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedVisibility(isRandomInterval.value) {
                    OutlinedTextField(
                        value = randomIntervalValue.value,
                        onValueChange = {
                            if (it.isDigitsOnly() || it.isEmpty()) {
                                randomIntervalValue.value = it
                            }
                        },
                        singleLine = true,
                        label = {
                            Text(text = "随机修改间隔时间最大值(ms)")
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = showDaraFilterUserName.value,
                    onValueChange = {
                        showDaraFilterUserName.value = it
                        Constants.showDataFilterUserName = it
                    },
                    singleLine = true,
                    label = {
                        Text(text = "查看数据时筛选用户名")
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                )


                Spacer(modifier = Modifier.height(8.dp))

                // 设置定时开启/停止运行
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        isAutoReset.value,
                        onCheckedChange = {
                            isAutoReset.value = it
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Text("定时停止")

                    Spacer(Modifier.width(8.dp))

                    IconButton(onClick = {
                        Toast.makeText(
                            context,
                            "勾选后可以每天定时停止和重启",
                            Toast.LENGTH_LONG
                        ).show()
                    }) {
                        Icon(Icons.Default.Info, contentDescription = "info")
                    }
                }

                if (isAutoReset.value) {
                    Spacer(modifier = Modifier.height(8.dp))

                    TimeRangeSelector(restTime)
                }
            }
        }
    }
}

@Composable
private fun UserInputContent(
    userNameList: SnapshotStateList<String>,
) {
    val dialogState = rememberMaterialDialogState()

    userNameList.forEachIndexed { index, name ->
        OutlinedTextField(
            value = name,
            onValueChange = {
                userNameList[index] = it
            },
            singleLine = true,
            label = {
                Text(text = "请输入要记录的微信用户名或备注名")
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        userNameList.removeAt(index)
                    }
                ) {
                    Icon(imageVector = Icons.Outlined.Clear, contentDescription = "Clear")
                }
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton (
            onClick = {
                userNameList.add("")
            }
        ) {
            Text("添加用户名")
        }

        TextButton (
            onClick = {
                dialogState.show()
            }
        ) {
            Text("查找用户名")
        }
    }

    ChooseUserNameDialog(
        showState = dialogState,
        initUserNameList = userNameList.toList(),
        allUserNameList = Constants.allUserNameList,
        onConfirm = {
            userNameList.clear()
            userNameList.addAll(it)
            dialogState.hide()
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeRangeSelector(restTime: MutableState<Pair<Int, Int?>>) {
    var showStopTimePicker by remember { mutableStateOf(false) }
    var showResumeTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 停止时间
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("停止时间:", Modifier.padding(bottom = 4.dp))
            OutlinedTextField(
                value = DateTimeUtil.min2HourMinString(restTime.value.first),
                onValueChange = { },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showStopTimePicker = true }) {
                        Icon(Icons.Default.AccessTime, contentDescription = "选择时间")
                    }
                },
            )
        }

        // 启动时间
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("启动时间（留空则表示不重启）:", Modifier.padding(bottom = 4.dp))
            OutlinedTextField(
                value = if (restTime.value.second == null) "" else DateTimeUtil.min2HourMinString(restTime.value.second!!),
                onValueChange = { },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showResumeTimePicker = true }) {
                        Icon(Icons.Default.AccessTime, contentDescription = "选择时间")
                    }
                },
            )
        }
    }

    if (showStopTimePicker) {
        val stopTimePickerState = rememberTimePickerState(
            initialHour = DateTimeUtil.min2HourMin(restTime.value.first).first,
            initialMinute = DateTimeUtil.min2HourMin(restTime.value.first).second,
            is24Hour = true
        )

        TimePickerDialog(
            stopTimePickerState,
            onConfirm = {
                restTime.value = restTime.value.copy(first = DateTimeUtil.hourMin2Min(stopTimePickerState.hour, stopTimePickerState.minute))
                showStopTimePicker = false
            },
            onDismiss = {
                showStopTimePicker = false
            }
        )
    }

    if (showResumeTimePicker) {
        val resumeTimePickerState = rememberTimePickerState(
            initialHour = DateTimeUtil.min2HourMin(restTime.value.second ?: 0).first,
            initialMinute = DateTimeUtil.min2HourMin(restTime.value.second ?: 0).second,
            is24Hour = true
        )

        TimePickerDialog(
            resumeTimePickerState,
            onConfirm = {
                restTime.value = restTime.value.copy(second = DateTimeUtil.hourMin2Min(resumeTimePickerState.hour, resumeTimePickerState.minute))
                showResumeTimePicker = false
            },
            onDismiss = {
                showResumeTimePicker = false
            },
            onClear = {
                restTime.value = restTime.value.copy(second = null)
                showResumeTimePicker = false
            }
        )
    }
}