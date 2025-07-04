package com.equationl.wxsteplog

import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.equationl.wxsteplog.ui.HomeNavHost
import com.equationl.wxsteplog.ui.theme.WxStepLogTheme
import com.equationl.wxsteplog.util.log.Logger
import com.ven.assists.AssistsCore
import com.ven.assists.service.AssistsService
import com.ven.assists.service.AssistsServiceListener
import com.ven.assists.utils.CoroutineWrapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity(), AssistsServiceListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WxStepLogTheme {
                HomeNavHost()
            }
        }
        AssistsService.listeners.add(this)
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
        // isAccessibilityServiceEnabled.value = Assists.isAccessibilityServiceEnabled()
    }

    override fun onDestroy() {
        super.onDestroy()
        AssistsService.listeners.remove(this)
    }

//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            moveTaskToBack(true)
//            return true
//        }
//        return super.onKeyDown(keyCode, event)
//    }

//    @Deprecated("Deprecated in Java")
//    override fun onBackPressed() {
//        super.onBackPressed()
//        moveTaskToBack(true)
//    }

    private fun onBackApp() {
        CoroutineWrapper.launch {
            while (AssistsCore.getPackageName() != packageName) {
                AssistsCore.back()
                delay(500)
            }
        }
    }
}