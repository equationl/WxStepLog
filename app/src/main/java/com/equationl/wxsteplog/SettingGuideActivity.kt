package com.equationl.wxsteplog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
        modifier = Modifier.fillMaxSize().clickable { onClickFinish() },
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().height((config.screenHeightDp / 2).dp).padding(32.dp)
            ) {
                Box {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("请关闭本提示后在当前页面中允许 ${stringResource(R.string.app_name)} 的服务")
                    }

                    TextButton(
                        onClick = onClickFinish,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text("关闭")
                    }
                }
            }
        }
    }
}
