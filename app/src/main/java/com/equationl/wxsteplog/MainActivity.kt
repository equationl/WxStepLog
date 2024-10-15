package com.equationl.wxsteplog

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.equationl.wxsteplog.model.WxStepLogSetting
import com.equationl.wxsteplog.step.OverManager
import com.equationl.wxsteplog.ui.theme.WxStepLogTheme
import com.equationl.wxsteplog.util.log.Logger
import com.ven.assists.Assists
import com.ven.assists.AssistsService
import com.ven.assists.AssistsServiceListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), AssistsServiceListener {
    private val isAccessibilityServiceEnabled = mutableStateOf(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WxStepLogTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        HomePage(isAccessibilityServiceEnabled)
                    }
                }
            }
        }
        Assists.serviceListeners.add(this)
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            Logger.instance.clearLogFile()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        super.onAccessibilityEvent(event)
        if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {

        }
    }

    override fun onServiceConnected(service: AssistsService) {
        onBackApp()
    }

    override fun onUnbind() {
        // OverManager.clear()
        isAccessibilityServiceEnabled.value = Assists.isAccessibilityServiceEnabled()
    }

    override fun onDestroy() {
        super.onDestroy()
        Assists.serviceListeners.remove(this)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }

    private fun onBackApp() {
        Assists.coroutine.launch {
            while (Assists.getPackageName() != packageName) {
                Assists.back()
                delay(500)
            }
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