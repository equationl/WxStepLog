package com.equationl.wxsteplog.ui.view.home.screen

import android.content.Intent
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.equationl.wxsteplog.R
import com.equationl.wxsteplog.SettingGuideActivity
import com.equationl.wxsteplog.constants.Route
import com.equationl.wxsteplog.model.WxStepLogSetting
import com.equationl.wxsteplog.step.LogWxStep
import com.equationl.wxsteplog.step.OverManager
import com.equationl.wxsteplog.ui.LocalNavController
import com.equationl.wxsteplog.util.Utils
import com.equationl.wxsteplog.util.datastore.DataKey
import com.equationl.wxsteplog.util.datastore.DataStoreUtils
import com.ven.assists.Assists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val isAccessibilityServiceEnabled = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Utils.changeScreenOrientation(context, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HomePage(isAccessibilityServiceEnabled)
        }
    }
}

@Composable
private fun HomePage(isAccessibilityServiceEnabled: MutableState<Boolean>) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userName = remember { mutableStateOf("") }
    val intervalTime = remember { mutableStateOf("60000") }
    val isRandomInterval = remember { mutableStateOf(false) }
    val randomIntervalValue = remember { mutableStateOf("30000") }

    LaunchedEffect(Unit) {
        scope.launch {
            userName.value = DataStoreUtils.getSyncData(DataKey.LOG_USER_NAME, "")
            intervalTime.value = DataStoreUtils.getSyncData(DataKey.LOG_INTERVAL_TIME, "60000")
            isRandomInterval.value = DataStoreUtils.getSyncData(DataKey.LOG_IS_INTERVAL_TIME_RANDOM_RANGE, false)
            randomIntervalValue.value = DataStoreUtils.getSyncData(DataKey.LOG_INTERVAL_TIME_RANDOM_RANGE, "30000")
        }
    }

    LifecycleResumeEffect(Unit) {
        isAccessibilityServiceEnabled.value = Assists.isAccessibilityServiceEnabled()

        onPauseOrDispose  {

        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isAccessibilityServiceEnabled.value) {
            HomeSettingContent(userName,intervalTime, isRandomInterval, randomIntervalValue)
        }

        TextButton(
            onClick = {
                if (Assists.isAccessibilityServiceEnabled()) {
                    if (userName.value.isBlank()) {
                        Toast.makeText(context, "请输入要查找的微信用户名！", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    if (intervalTime.value.isBlank() || intervalTime.value.toLongOrNull() == null) {
                        Toast.makeText(context, "请输入间隔时间", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    if (isRandomInterval.value && (randomIntervalValue.value.isBlank()) || randomIntervalValue.value.toLongOrNull() == null) {
                        Toast.makeText(context, "请输入随机间隔时间", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }

                    scope.launch {
                        DataStoreUtils.putSyncData(DataKey.LOG_USER_NAME, userName.value)
                        DataStoreUtils.putSyncData(DataKey.LOG_INTERVAL_TIME, intervalTime.value)
                        DataStoreUtils.putSyncData(DataKey.LOG_IS_INTERVAL_TIME_RANDOM_RANGE, isRandomInterval.value)
                        DataStoreUtils.putSyncData(DataKey.LOG_INTERVAL_TIME_RANDOM_RANGE, randomIntervalValue.value)

                        LogWxStep.LOG_INTERVAL_TIME = intervalTime.value.toLong()
                        LogWxStep.LOG_INTERVAL_TIME_RANDOM_RANGE = randomIntervalValue.value.toLong()

                        withContext(Dispatchers.Main) {
                            OverManager.show(
                                WxStepLogSetting(
                                    userName = userName.value
                                )
                            )
                        }
                    }
                } else {
                    Assists.openAccessibilitySetting()
                    context.startActivity(Intent(context, SettingGuideActivity::class.java))
                }
            }
        ) {
            Text(if (isAccessibilityServiceEnabled.value) "开始记录" else "打开无障碍服务")
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar() {
    val navController = LocalNavController.current

    TopAppBar(
        title = {
            Text(text = stringResource(R.string.app_name))
        },
        actions = {
            IconButton(
                onClick = {
                    navController.navigate(Route.STATISTIC)
                }
            ) {
                Icon(Icons.Outlined.Analytics, contentDescription = "menu")
            }
        }
    )
}

@Composable
private fun HomeSettingContent(
    userName: MutableState<String>,
    intervalTime: MutableState<String>,
    isRandomInterval: MutableState<Boolean>,
    randomIntervalValue: MutableState<String>
) {
    val context = LocalContext.current
    var isShowMoreSetting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = userName.value,
            onValueChange = {
                userName.value = it
            },
            singleLine = true,
            label = {
                Text(text = "请输入要记录的微信用户名或备注名")
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        Toast.makeText(context, "查找当前微信用户名功能开发中，敬请期待", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(imageVector = Icons.Outlined.Search, contentDescription = "search")
                }
            }
        )

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
            }
        }
    }
}