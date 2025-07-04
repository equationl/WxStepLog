package com.equationl.wxsteplog.ui.view.singleLog.screen

import android.content.pm.ActivityInfo
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.equationl.wxsteplog.constants.Route
import com.equationl.wxsteplog.overlays.MainOverlay
import com.equationl.wxsteplog.ui.LocalNavController
import com.equationl.wxsteplog.util.Utils

@Composable
fun SingleLogScreen() {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Utils.changeScreenOrientation(context, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SingleLogTopBar()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SingleLogContent()
        }
    }
}

@Composable
private fun SingleLogContent() {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedButton(
            onClick = {
                MainOverlay.showSingleLog()
            }
        ) {
            Text("开始读取")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleLogTopBar() {
    val navController = LocalNavController.current

    TopAppBar(
        title = {
            Text(text = "单次记录")
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
                    navController.navigate(Route.HISTORY_STATISTIC)
                }
            ) {
                Icon(Icons.Outlined.Analytics, contentDescription = "menu")
            }
        }
    )
}