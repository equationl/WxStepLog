package com.equationl.wxsteplog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import com.ven.assists.Assists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingGuideActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Assists.coroutine.launch {
            delay(500)
            withContext(Dispatchers.Main) {
                setContent {
                    SettingGuidePage {
                        finish()
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingGuidePage(
    onClickFinish: () -> Unit
) {
    val config = LocalConfiguration.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        // TODO 启动无障碍引导页面
//        Card(
//            modifier = Modifier.size(width = (config.screenWidthDp / 3 * 2).dp, height = (config.screenHeightDp / 2).dp),
//        ) {
            Text("TODOTODOTODO")
            TextButton(onClick = onClickFinish) {
                Text("关闭")
            }
        //}
    }
}
