package com.equationl.wxsteplog.ui.view.continuousLog.screen

import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.equationl.common.datastore.DataKey
import com.equationl.common.datastore.DataStoreUtils
import com.equationl.common.json.fromJsonList
import com.equationl.common.json.toJson
import com.equationl.wxsteplog.constants.Constants
import com.equationl.wxsteplog.constants.Route
import com.equationl.wxsteplog.model.LogSettingMode
import com.equationl.wxsteplog.model.WxStepLogSetting
import com.equationl.wxsteplog.model.toLogUserMode
import com.equationl.wxsteplog.overlays.MainOverlay
import com.equationl.wxsteplog.ui.LocalNavController
import com.equationl.wxsteplog.ui.view.continuousLog.widget.ContinuousLogSettingContent
import com.equationl.wxsteplog.util.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ven.assists.AssistsCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ContinuousLogScreen() {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Utils.changeScreenOrientation(context, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ContinuousLogTopBar()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ContinuousLogPage()
        }
    }
}

@Composable
private fun ContinuousLogPage() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var startState by remember { mutableIntStateOf(1) }
    val logUserModel = remember { mutableStateOf(LogSettingMode.Multiple) }
    val userNameList = remember { mutableStateListOf<String>() }
    val intervalTime = remember { mutableStateOf("60000") }
    val isRandomInterval = remember { mutableStateOf(false) }
    val isAllModelSpecialUser = remember { mutableStateOf(false) }
    val randomIntervalValue = remember { mutableStateOf("30000") }
    val showDaraFilterUserName = remember { mutableStateOf("") }
    val isAutoRunning = remember { mutableStateOf(true) }
    val isAutoReset = remember { mutableStateOf(false) }
    val restTime = remember { mutableStateOf(Pair<Int, Int?>(0, null)) }

    LaunchedEffect(startState) {
        scope.launch {
            val userNameListString = DataStoreUtils.getSyncData(DataKey.LOG_MULTIPLE_USER_NAME, "")
            userNameList.clear()
            userNameList.addAll(userNameListString.fromJsonList(String::class.java))
            intervalTime.value = DataStoreUtils.getSyncData(DataKey.LOG_INTERVAL_TIME, "60000")
            isRandomInterval.value = DataStoreUtils.getSyncData(DataKey.LOG_IS_INTERVAL_TIME_RANDOM_RANGE, false)
            isAllModelSpecialUser.value = DataStoreUtils.getSyncData(DataKey.LOG_IS_ALL_WITH_SPECIAL_USER, false)
            randomIntervalValue.value = DataStoreUtils.getSyncData(DataKey.LOG_INTERVAL_TIME_RANDOM_RANGE, "30000")
            showDaraFilterUserName.value = DataStoreUtils.getSyncData(DataKey.SHOW_DATA_FILTER_USER, "")
            logUserModel.value = DataStoreUtils.getSyncData(DataKey.LOG_USER_MODE, LogSettingMode.Multiple.name).toLogUserMode() ?: LogSettingMode.Multiple
            isAutoRunning.value = DataStoreUtils.getSyncData(DataKey.IS_AUTO_RUNNING, true)
            isAutoReset.value = DataStoreUtils.getSyncData(DataKey.IS_AUTO_RESET, false)

            val type = object : TypeToken<Pair<Int, Int?>>() {}.type
            restTime.value  = Gson().fromJson<Pair<Int, Int>>(DataStoreUtils.getSyncData(DataKey.REST_TIME, ""), type) ?: Pair(0, null)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ContinuousLogSettingContent(logUserModel, userNameList, intervalTime, isRandomInterval, randomIntervalValue, showDaraFilterUserName, isAllModelSpecialUser, isAutoRunning, isAutoReset, restTime)

        OutlinedButton (
            onClick = {
                if (AssistsCore.isAccessibilityServiceEnabled()) {
                    if (
                        (logUserModel.value == LogSettingMode.Multiple || (logUserModel.value == LogSettingMode.All && isAllModelSpecialUser.value)) &&
                        userNameList.none { it.isNotBlank() }
                    ) {
                        Toast.makeText(context, "请至少添加一个要查找的用户名！", Toast.LENGTH_SHORT).show()
                        return@OutlinedButton
                    }
                    if (intervalTime.value.isBlank() || intervalTime.value.toLongOrNull() == null) {
                        Toast.makeText(context, "请输入间隔时间", Toast.LENGTH_SHORT).show()
                        return@OutlinedButton
                    }
                    if (isRandomInterval.value && (randomIntervalValue.value.isBlank()) || randomIntervalValue.value.toLongOrNull() == null) {
                        Toast.makeText(context, "请输入随机间隔时间", Toast.LENGTH_SHORT).show()
                        return@OutlinedButton
                    }
                    if (isAutoReset.value && restTime.value.first == restTime.value.second) {
                        Toast.makeText(context, "停止时间和开始时间不能相同", Toast.LENGTH_SHORT).show()
                        return@OutlinedButton
                    }

                    scope.launch {
                        DataStoreUtils.putSyncData(DataKey.LOG_USER_MODE, logUserModel.value.name)
                        DataStoreUtils.putSyncData(DataKey.LOG_MULTIPLE_USER_NAME, userNameList.filter { it.isNotBlank() }.toList().toJson())
                        DataStoreUtils.putSyncData(DataKey.LOG_INTERVAL_TIME, intervalTime.value)
                        DataStoreUtils.putSyncData(DataKey.LOG_IS_INTERVAL_TIME_RANDOM_RANGE, isRandomInterval.value)
                        DataStoreUtils.putSyncData(DataKey.LOG_INTERVAL_TIME_RANDOM_RANGE, randomIntervalValue.value)
                        DataStoreUtils.putSyncData(DataKey.LOG_IS_ALL_WITH_SPECIAL_USER, isAllModelSpecialUser.value)
                        DataStoreUtils.putSyncData(DataKey.SHOW_DATA_FILTER_USER, showDaraFilterUserName.value)
                        DataStoreUtils.putSyncData(DataKey.WX_PKG_NAME, Constants.wxPkgName.value)
                        DataStoreUtils.putSyncData(DataKey.WX_LAUNCHER_PKG_NAME, Constants.wxLauncherPkg.value)
                        DataStoreUtils.putSyncData(DataKey.IS_AUTO_RUNNING, isAutoRunning.value)
                        DataStoreUtils.putSyncData(DataKey.IS_AUTO_RESET, isAutoReset.value)
                        DataStoreUtils.putSyncData(DataKey.REST_TIME, restTime.value.toJson())


                        Constants.showDataFilterUserName = showDaraFilterUserName.value

                        withContext(Dispatchers.Main) {
                            MainOverlay.showStartLog(
                                WxStepLogSetting(
                                    userNameList = userNameList.toList(),
                                    logUserMode = logUserModel.value,
                                    intervalTime = intervalTime.value.toLong(),
                                    isRandomInterval = isRandomInterval.value,
                                    randomIntervalValue = randomIntervalValue.value.toLong(),
                                    isAllModelSpecialUser = isAllModelSpecialUser.value,
                                    isAutoRunning = isAutoRunning.value,
                                    isAutoReset = isAutoReset.value,
                                    restTime = restTime.value
                                )
                            )
                        }

                        startState++
                    }
                } else {
                    AssistsCore.openAccessibilitySetting()
                    // fixme 如果显示提示，设置页面会被覆盖
                    // context.startActivity(Intent(context, SettingGuideActivity::class.java))
                }
            }
        ) {
            Text("开始记录")
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContinuousLogTopBar() {
    val navController = LocalNavController.current

    TopAppBar(
        title = {
            Text(text = "持续记录")
        },
        navigationIcon = {
            IconButton(onClick = {
                navController.popBackStack()
            }) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "back")
            }
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