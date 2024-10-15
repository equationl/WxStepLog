package com.equationl.wxsteplog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.equationl.wxsteplog.db.DbUtil
import com.equationl.wxsteplog.model.WxStepLogSetting
import com.equationl.wxsteplog.step.OverManager
import com.equationl.wxsteplog.ui.theme.WxStepLogTheme
import com.equationl.wxsteplog.util.DateTimeUtil.formatDateTime
import com.equationl.wxsteplog.util.log.Logger
import com.ven.assists.Assists
import com.ven.assists.AssistsService
import com.ven.assists.AssistsServiceListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val scope = rememberCoroutineScope()
    val userName = remember { mutableStateOf("") }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        scope.launch(Dispatchers.IO) {
            exportData(result, context)
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

        TextButton(
            onClick = {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/comma-separated-values"
                    putExtra(Intent.EXTRA_TITLE, "wxStepLog_${System.currentTimeMillis().formatDateTime("yyyy_MM_dd_HH_mm_ss")}.csv")
                }
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                exportLauncher.launch(intent)
            }
        ) {
            Text("导出数据为 csv")
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

private suspend fun exportData(result: ActivityResult, context: Context) {
    val data = result.data
    val uri = data?.data
    uri?.let {
        context.contentResolver.openOutputStream(it)?.let {outputStream ->
            val dataList = DbUtil.db.manHoursDB().queryAllData()
            for (row in dataList) {
                outputStream.write("${row.id},${row.userName},${row.stepNum},${row.likeNum},${row.logTimeString},${row.logTime}\n".toByteArray())
            }
            outputStream.flush()
            outputStream.close()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "导出完成！", Toast.LENGTH_SHORT).show()
            }
        }
    }
}