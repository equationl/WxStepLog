package com.equationl.wxsteplog.ui.view.home.screen

import android.content.Intent
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.equationl.wxsteplog.R
import com.equationl.wxsteplog.SettingGuideActivity
import com.equationl.wxsteplog.constants.Route
import com.equationl.wxsteplog.model.LogSettingMode
import com.equationl.wxsteplog.model.WxStepLogSetting
import com.equationl.wxsteplog.model.toLogUserMode
import com.equationl.wxsteplog.step.OverManager
import com.equationl.wxsteplog.ui.LocalNavController
import com.equationl.wxsteplog.ui.view.home.widget.HomeSettingContent
import com.equationl.wxsteplog.util.Utils
import com.equationl.wxsteplog.util.datastore.DataKey
import com.equationl.wxsteplog.util.datastore.DataStoreUtils
import com.equationl.wxsteplog.util.fromJsonList
import com.equationl.wxsteplog.util.toJson
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
    var startState by remember { mutableIntStateOf(1) }
    val logUserModel = remember { mutableStateOf(LogSettingMode.Multiple) }
    val userNameList = remember { mutableStateListOf<String>() }
    val intervalTime = remember { mutableStateOf("60000") }
    val isRandomInterval = remember { mutableStateOf(false) }
    val randomIntervalValue = remember { mutableStateOf("30000") }

    LaunchedEffect(startState) {
        scope.launch {
            val userNameListString = DataStoreUtils.getSyncData(DataKey.LOG_MULTIPLE_USER_NAME, "")
            userNameList.clear()
            userNameList.addAll(userNameListString.fromJsonList(String::class.java))
            intervalTime.value = DataStoreUtils.getSyncData(DataKey.LOG_INTERVAL_TIME, "60000")
            isRandomInterval.value = DataStoreUtils.getSyncData(DataKey.LOG_IS_INTERVAL_TIME_RANDOM_RANGE, false)
            randomIntervalValue.value = DataStoreUtils.getSyncData(DataKey.LOG_INTERVAL_TIME_RANDOM_RANGE, "30000")
            logUserModel.value = DataStoreUtils.getSyncData(DataKey.LOG_USER_MODE, LogSettingMode.Multiple.name).toLogUserMode() ?: LogSettingMode.Multiple
        }
    }

    LifecycleResumeEffect(Unit) {
        isAccessibilityServiceEnabled.value = Assists.isAccessibilityServiceEnabled()

        onPauseOrDispose  {

        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isAccessibilityServiceEnabled.value) {
            HomeSettingContent(logUserModel, userNameList, intervalTime, isRandomInterval, randomIntervalValue)
        }

        OutlinedButton (
            onClick = {
                if (Assists.isAccessibilityServiceEnabled()) {
                    if (logUserModel.value == LogSettingMode.Multiple && userNameList.none { it.isNotBlank() }) {
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

                    scope.launch {
                        DataStoreUtils.putSyncData(DataKey.LOG_USER_MODE, logUserModel.value.name)
                        DataStoreUtils.putSyncData(DataKey.LOG_MULTIPLE_USER_NAME, userNameList.filter { it.isNotBlank() }.toList().toJson())
                        DataStoreUtils.putSyncData(DataKey.LOG_INTERVAL_TIME, intervalTime.value)
                        DataStoreUtils.putSyncData(DataKey.LOG_IS_INTERVAL_TIME_RANDOM_RANGE, isRandomInterval.value)
                        DataStoreUtils.putSyncData(DataKey.LOG_INTERVAL_TIME_RANDOM_RANGE, randomIntervalValue.value)

                        withContext(Dispatchers.Main) {
                            OverManager.show(
                                WxStepLogSetting(
                                    userNameList = userNameList.toList(),
                                    logUserMode = logUserModel.value,
                                    intervalTime = intervalTime.value.toLong(),
                                    isRandomInterval = isRandomInterval.value,
                                    randomIntervalValue = randomIntervalValue.value.toLong()
                                )
                            )
                        }

                        startState++
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