package com.equationl.wxsteplog.ui.view.home.screen

import android.content.Intent
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.equationl.wxsteplog.R
import com.equationl.wxsteplog.SettingGuideActivity
import com.equationl.wxsteplog.constants.Route
import com.equationl.wxsteplog.model.WxStepLogSetting
import com.equationl.wxsteplog.step.OverManager
import com.equationl.wxsteplog.ui.LocalNavController
import com.equationl.wxsteplog.util.Utils
import com.ven.assists.Assists

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
    val userName = remember { mutableStateOf("") }

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
            HomeSettingContent(userName)
        }

        TextButton(
            onClick = {
                if (Assists.isAccessibilityServiceEnabled()) {
                    if (userName.value.isBlank()) {
                        Toast.makeText(context, "请输入要查找的微信用户名！", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        OverManager.show(
                            WxStepLogSetting(
                                userName = userName.value
                            )
                        )
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
private fun HomeSettingContent(userName: MutableState<String>) {
    OutlinedTextField(
        value = userName.value,
        onValueChange = {
            userName.value = it
        },
        singleLine = true,
        label = {
            Text(text = "请输入要记录的微信用户名或备注名")
        },
    )
}