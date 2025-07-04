package com.equationl.wxsteplog.ui.view.setting.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.equationl.common.datastore.DataKey
import com.equationl.common.datastore.DataStoreUtils
import com.equationl.common.json.fromJson
import com.equationl.common.json.toJson
import com.equationl.wxsteplog.constants.Constants
import com.equationl.wxsteplog.constants.Route
import com.equationl.wxsteplog.ui.LocalNavController
import kotlinx.coroutines.launch

@Composable
fun SettingScreen() {
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar()
        }
    ) { innerPadding ->

        LaunchedEffect(Unit) {
            scope.launch {
                Constants.wxPkgName.value = DataStoreUtils.getSyncData(DataKey.WX_PKG_NAME, Constants.wxPkgName.value)
                Constants.wxLauncherPkg.value = DataStoreUtils.getSyncData(DataKey.WX_LAUNCHER_PKG_NAME, Constants.wxLauncherPkg.value)
                Constants.csvDelimiter.value = DataStoreUtils.getSyncData(DataKey.CSV_DELIMITER, Constants.csvDelimiter.value)
                Constants.wxViewLimit.value = DataStoreUtils.getSyncData(DataKey.WX_VIEW_LIMIT, Constants.wxViewLimit.value.toJson()).fromJson() ?: Constants.wxViewLimit.value
                Constants.stepOrderLimit.value = DataStoreUtils.getSyncData(DataKey.STEP_ORDER_VIEW_LIMIT, Constants.stepOrderLimit.value.toJson()).fromJson() ?: Constants.stepOrderLimit.value
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SettingContent()
        }
    }
}

@Composable
private fun SettingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = Constants.showDetailLog.value,
                onCheckedChange = {
                    Constants.showDetailLog.value = it
                }
            )
            Text("显示详细日志（勾选后会严重影响读取速度）")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("如果不知道以下选项是什么意思请勿修改", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error))

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = Constants.runStepIntervalTime.intValue.toString(),
            onValueChange = {
                if (it.isDigitsOnly() || it.isEmpty()) {
                    Constants.runStepIntervalTime.intValue = it.toIntOrNull() ?: Constants.runStepIntervalTime.intValue
                }
            },
            singleLine = true,
            label = {
                Text(text = "运行步骤间隔时间(ms)")
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = Constants.wxPkgName.value,
            onValueChange = {
                Constants.wxPkgName.value = it
            },
            singleLine = true,
            label = {
                Text(text = "微信包名")
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = Constants.wxLauncherPkg.value,
            onValueChange = {
                Constants.wxLauncherPkg.value = it
            },
            singleLine = true,
            label = {
                Text(text = "微信启动类")
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = Constants.wxViewLimit.value.top.toInt().toString(),
            onValueChange = {
                Constants.wxViewLimit.value = Constants.wxViewLimit.value.copy(top = it.toFloatOrNull() ?: Constants.wxViewLimit.value.top)
            },
            singleLine = true,
            label = {
                Text(text = "“微信” View 顶部坐标（高度1920为基准）")
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = Constants.wxViewLimit.value.right.toInt().toString(),
            onValueChange = {
                Constants.wxViewLimit.value = Constants.wxViewLimit.value.copy(right = it.toFloatOrNull() ?: Constants.wxViewLimit.value.right)
            },
            singleLine = true,
            label = {
                Text(text = "“微信” View 右部坐标（宽度1080为基准）")
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = Constants.stepOrderLimit.value.top.toInt().toString(),
            onValueChange = {
                Constants.stepOrderLimit.value  = Constants.stepOrderLimit.value.copy(top = it.toFloatOrNull() ?: Constants.stepOrderLimit.value.top)
            },
            singleLine = true,
            label = {
                Text(text = "“步数排行榜” View 顶部坐标（高度1920为基准）")
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = Constants.csvDelimiter.value,
            onValueChange = {
                if (it.length > 1) {
                    return@OutlinedTextField
                }
                Constants.csvDelimiter.value = it
            },
            singleLine = true,
            label = {
                Text(text = "CSV 分隔符号")
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // AI设置入口
        val navController = LocalNavController.current
        Button(
            onClick = {
                navController.navigate(Route.AI_SETTINGS)
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "AI设置",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("AI分析服务设置")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    TopAppBar(
        title = {
            Text(text = "设置")
        },
        navigationIcon = {
            IconButton(onClick = {
                navController.popBackStack()
            }) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "back")
            }
        },
        actions = {
            IconButton(onClick = {
                DataStoreUtils.putSyncData(DataKey.WX_PKG_NAME, Constants.wxPkgName.value)
                DataStoreUtils.putSyncData(DataKey.WX_LAUNCHER_PKG_NAME, Constants.wxLauncherPkg.value)
                DataStoreUtils.putSyncData(DataKey.RUN_STEP_INTERVAL_TIME, Constants.runStepIntervalTime.intValue)
                DataStoreUtils.putSyncData(DataKey.SHOW_DETAIL_LOG, Constants.showDetailLog.value)
                DataStoreUtils.putSyncData(DataKey.CSV_DELIMITER, Constants.csvDelimiter.value)
                DataStoreUtils.putSyncData(DataKey.WX_VIEW_LIMIT, Constants.wxViewLimit.value.toJson())
                DataStoreUtils.putSyncData(DataKey.STEP_ORDER_VIEW_LIMIT, Constants.stepOrderLimit.value.toJson())
                Toast.makeText(context, "已保存！", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }) {
                Icon(Icons.Outlined.Save, "save")
            }
        }
    )
}