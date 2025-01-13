package com.equationl.wxsteplog.ui.view.home.screen

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.equationl.wxsteplog.R
import com.equationl.wxsteplog.SettingGuideActivity
import com.equationl.wxsteplog.constants.Constants
import com.equationl.wxsteplog.constants.Route
import com.equationl.wxsteplog.ui.LocalNavController
import com.equationl.wxsteplog.util.Utils
import com.equationl.wxsteplog.util.datastore.DataKey
import com.equationl.wxsteplog.util.datastore.DataStoreUtils
import com.ven.assists.Assists

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val isAccessibilityServiceEnabled = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Utils.changeScreenOrientation(context, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)

        Constants.wxPkgName.value = DataStoreUtils.getSyncData(DataKey.WX_PKG_NAME, Constants.wxPkgName.value)
        Constants.wxLauncherPkg.value = DataStoreUtils.getSyncData(DataKey.WX_LAUNCHER_PKG_NAME, Constants.wxLauncherPkg.value)
        Constants.runStepIntervalTime.intValue = DataStoreUtils.getSyncData(DataKey.RUN_STEP_INTERVAL_TIME, Constants.runStepIntervalTime.intValue)
        Constants.showDetailLog.value = DataStoreUtils.getSyncData(DataKey.SHOW_DETAIL_LOG, Constants.showDetailLog.value)
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
    LifecycleResumeEffect(Unit) {
        isAccessibilityServiceEnabled.value = Assists.isAccessibilityServiceEnabled()

        onPauseOrDispose  {

        }
    }

    if (isAccessibilityServiceEnabled.value) {
        HomeContent()
    }
    else {
        OpenAccessibilityServiceContent()
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
                    navController.navigate(Route.GLOBAL_SETTING)
                }
            ) {
                Icon(Icons.Outlined.Settings, contentDescription = "setting")
            }
        }
    )
}

@Composable
private fun OpenAccessibilityServiceContent() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("本程序基于无障碍服务实现自动操作及其记录数据，如需使用请先打开无障碍服务", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                Assists.openAccessibilitySetting()
                context.startActivity(Intent(context, SettingGuideActivity::class.java))
            }
        ) {
            Text("打开无障碍服务")
        }
    }
}

@Composable
private fun HomeContent() {
    val navController = LocalNavController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("提示：请选择一个功能后开始运行", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))
        val selectedOption = remember { mutableIntStateOf(0) }
        Column(Modifier.selectableGroup()) {
            Constants.functionList.forEachIndexed { index, title ->
                Row(
                    Modifier.fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = (index == selectedOption.intValue),
                            onClick = { selectedOption.intValue = index },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (index == selectedOption.intValue),
                        onClick = null // null recommended for accessibility with screenreaders
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
        Card {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(getHelpText(index = selectedOption.intValue), style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(32.dp))
        OutlinedButton(
            onClick = {
                when (selectedOption.intValue) {
                    0 -> {
                        Route.SINGLE_LOG
                    }
                    1 -> {
                        Route.CONTINUOUS_LOG
                    }
                    else -> {
                        Route.SINGLE_LOG
                    }
                }.let {
                    navController.navigate(it)
                }
            }
        ) {
            Text("开始运行")
        }
    }

}

private fun getHelpText(index: Int): String {
    return when (index) {
        0 -> {
            "读取 “微信运动” 消息列表中的历史运动数据，一次性读取，读取完毕后自动停止。"
        }
        1 -> {
            "后台连续记录当前运动排行中的实时数据，开启后会一直运行，直至手动停止。"
        }
        else -> {
            ""
        }
    }
}