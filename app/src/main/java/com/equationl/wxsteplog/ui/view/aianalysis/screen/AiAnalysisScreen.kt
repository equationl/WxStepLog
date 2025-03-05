package com.equationl.wxsteplog.ui.view.aianalysis.screen

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.equationl.wxsteplog.aiapi.AnalysisStatus
import com.equationl.wxsteplog.aiapi.DataSourceType
import com.equationl.wxsteplog.constants.Route
import com.equationl.wxsteplog.ui.LocalNavController
import com.equationl.wxsteplog.ui.view.aianalysis.viewmodel.AiAnalysisViewModel
import com.equationl.wxsteplog.ui.widget.MarkdownText
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AiAnalysisScreen(
    viewModel: AiAnalysisViewModel = hiltViewModel()
) {
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
            AiAnalysisContent(viewModel)
        }
    }
}

@Composable
private fun AiAnalysisContent(
    viewModel: AiAnalysisViewModel
) {
    val analysisState by viewModel.analysisState.collectAsState()
    val scrollState = rememberScrollState()
    val resultScrollState = rememberScrollState()
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 数据选择部分
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("1. 选择数据来源", style = MaterialTheme.typography.titleMedium)
                
                // 数据源类型选择
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("数据类型:", Modifier.padding(bottom = 4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .wrapContentWidth()
                                .selectable(
                                    selected = viewModel.dataSourceType == DataSourceType.HISTORY,
                                    onClick = { viewModel.switchDataSourceType(DataSourceType.HISTORY) },
                                    role = Role.RadioButton
                                )
                        ) {
                            RadioButton(
                                selected = viewModel.dataSourceType == DataSourceType.HISTORY,
                                onClick = null
                            )
                            Text("历史数据", Modifier.padding(start = 8.dp, end = 16.dp))
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .wrapContentWidth()
                                .selectable(
                                    selected = viewModel.dataSourceType == DataSourceType.REALTIME,
                                    onClick = { viewModel.switchDataSourceType(DataSourceType.REALTIME) },
                                    role = Role.RadioButton
                                )
                        ) {
                            RadioButton(
                                selected = viewModel.dataSourceType == DataSourceType.REALTIME,
                                onClick = null
                            )
                            Text("实时数据", Modifier.padding(start = 8.dp))
                        }
                    }
                }
                
                // 根据选中的数据源类型显示不同的数据选择UI
                if (viewModel.dataSourceType == DataSourceType.HISTORY) {
                    // 历史数据选择UI
                    HistoryDataSelectionContent(viewModel)
                } else {
                    // 实时数据选择UI
                    RealTimeDataSelectionContent(viewModel)
                }
            }
        }
        
        // 模型选择部分
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("2. 选择AI模型", style = MaterialTheme.typography.titleMedium)
                
                // 模型选择器
                ModelSelector(viewModel)
                
                // 额外提示文本
                OutlinedTextField(
                    value = viewModel.userPrompt,
                    onValueChange = { viewModel.onPromptChanged(it) },
                    label = { Text("额外的提示") },
                    placeholder = { Text("可以给AI一些额外的分析提示...") },
                    modifier = Modifier.fillMaxWidth(),
                )
                
                // 是否自动滚动到底部选项
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = viewModel.autoScrollToBottom,
                        onCheckedChange = { viewModel.onAutoScrollToBottomChanged(it) }
                    )
                    Text(
                        "分析时自动滚动到底部", 
                        Modifier
                            .clickable { viewModel.onAutoScrollToBottomChanged(!viewModel.autoScrollToBottom) }
                            .padding(start = 8.dp)
                    )
                }
            }
        }
        
        // 分析结果展示
        analysisState?.let { result ->
            LaunchedEffect(result.content, result.status, result.thinkingContent) {
                if (viewModel.autoScrollToBottom) {
                    // 延迟一小段时间，确保布局已经更新
                    delay(100)

                    val resultScrollMax = resultScrollState.maxValue
                    val mainScrollMax = scrollState.maxValue

                    scrollState.animateScrollTo(mainScrollMax)
                    resultScrollState.animateScrollTo(resultScrollMax)
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("分析结果", style = MaterialTheme.typography.titleMedium)
                    
                    // 分析状态
                    when (result.status) {
                        AnalysisStatus.PROCESSING -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Text("分析中...")
                            }
                        }
                        AnalysisStatus.COMPLETED -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("分析完成", color = Color.Green)
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = {
                                    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clipData = ClipData.newPlainText("analysisResult", "${result.thinkingContent?:""}\n\n${result.content}")
                                    clipboardManager.setPrimaryClip(clipData)
                                    Toast.makeText(context, "复制成功", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text("复制")
                                }
                            }
                        }
                        AnalysisStatus.ERROR -> {
                            Text(
                                "分析出错: ${result.errorMessage ?: "未知错误"}",
                                color = Color.Red
                            )
                        }

                        AnalysisStatus.THINKING -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Text("思考中...")
                            }
                        }
                    }

                    // 分析内容
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 400.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(resultScrollState)
                        ) {
                            result.thinkingContent ?.let {
                                MarkdownText(
                                    markdownContent = it,
                                    modifier = Modifier.padding(6.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f)).padding(6.dp)
                                )
//                                Text(
//                                    text = it,
//                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
//                                    modifier = Modifier.padding(12.dp)
//                                )
                            }

                            MarkdownText(
                                markdownContent = result.content,
                                modifier = Modifier.padding(12.dp)
                            )

//                            Text(
//                                text = result.content,
//                                modifier = Modifier
//                                    .padding(12.dp)
//                            )
                        }
                    }
                }
            }
        }
        
        // 开始/取消按钮
        Button(
            onClick = {
                if (viewModel.isAnalyzing) {
                    viewModel.cancelAnalysis()
                } else {
                    // 开始分析
                    viewModel.startAnalysis(context)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(if (viewModel.isAnalyzing) "取消分析" else "开始分析")
        }
    }
}

/**
 * 历史数据选择UI组件
 */
@Composable
private fun HistoryDataSelectionContent(viewModel: AiAnalysisViewModel) {
    // 历史数据选择UI
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 历史记录选择
        HistoryLogSelector(viewModel)
        
        // 用户选择（历史数据）
        HistoryUserSelector(viewModel)
        
        // 日期范围选择
        DateRangeSelector(viewModel)
    }
}

/**
 * 实时数据选择UI组件
 */
@Composable
private fun RealTimeDataSelectionContent(viewModel: AiAnalysisViewModel) {
    val context = LocalContext.current
    // 实时数据选择UI
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 用户选择（实时数据）
        RealTimeUserSelector(viewModel)
        
        // 日期范围选择
        DateRangeSelector(viewModel)
        
        // 剔除重复数据选项
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = viewModel.removeDuplicates,
                onCheckedChange = { viewModel.onRemoveDuplicatesChanged(it) }
            )
            Text(
                "剔除重复数据", 
                Modifier
                    .clickable { viewModel.onRemoveDuplicatesChanged(!viewModel.removeDuplicates) }
                    .padding(start = 8.dp)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = {
                Toast.makeText(
                    context,
                    "这会移除同一用户未改变的数据，可以减少数据量且不会影响分析结果",
                    Toast.LENGTH_LONG
                ).show()
            }) {
                Icon(Icons.Default.Info, contentDescription = "信息")
            }
        }
    }
}

/**
 * 日期范围选择组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeSelector(viewModel: AiAnalysisViewModel) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 开始日期 - 改为垂直布局以适应小屏幕
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text("开始日期:", Modifier.padding(bottom = 4.dp))
            OutlinedTextField(
                value = viewModel.formatDate(viewModel.startDate),
                onValueChange = { },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showStartDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // 结束日期 - 改为垂直布局以适应小屏幕
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text("结束日期:", Modifier.padding(bottom = 4.dp))
            OutlinedTextField(
                value = viewModel.formatDate(viewModel.endDate),
                onValueChange = { },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showEndDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    
    // 日期选择器对话框
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = viewModel.startDate.time
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { timeMillis ->
                        viewModel.onStartDateChanged(Date(timeMillis))
                    }
                    showStartDatePicker = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = viewModel.endDate.time
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { timeMillis ->
                        viewModel.onEndDateChanged(Date(timeMillis))
                    }
                    showEndDatePicker = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelSelector(viewModel: AiAnalysisViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                readOnly = true,
                value = viewModel.selectedModel?.modeShowName ?: "请选择AI模型",
                onValueChange = {},
                label = { Text("AI模型") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                viewModel.supportedModels.forEach { model ->
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (viewModel.isModelConfigured(model)) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "已配置",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(model.modeShowName)
                                if (!viewModel.isModelConfigured(model)) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "(未配置)",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        },
                        onClick = {
                            viewModel.onModelSelected(model)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        
        // 配置按钮
        IconButton(onClick = {
            // 在实际应用中，这里应该导航到一个模型配置页面
            if (viewModel.selectedModel == null) {
                Toast.makeText(context, "请先选择一个AI模型", Toast.LENGTH_SHORT).show()
            } else if (!viewModel.isModelConfigured(viewModel.selectedModel!!)) {
                // 简单的弹窗输入API Key
                showApiKeyDialog(context, false, viewModel)
            } else {
                // 简单的弹窗输入API Key
                showApiKeyDialog(context, true, viewModel)
            }
        }) {
            Icon(Icons.Filled.Settings, contentDescription = "配置模型")
        }
    }
}

/**
 * 显示简单的API Key配置对话框
 */
private fun showApiKeyDialog(context: Context, isEdit: Boolean, viewModel: AiAnalysisViewModel) {
    val editText = EditText(context).apply {
        hint = "请输入API Key"
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        setPadding(50, 30, 50, 30)
    }
    
    AlertDialog.Builder(context)
        .setTitle(if (isEdit) "修改 ${viewModel.selectedModel?.modeShowName ?: ""}" else "配置 ${viewModel.selectedModel?.modeShowName ?: ""}")
        .setView(editText)
        .setPositiveButton("保存") { _, _ ->
            val apiKey = editText.text.toString().trim()
            if (apiKey.isNotEmpty()) {
                viewModel.saveModelConfig(viewModel.selectedModel!!, apiKey)
                Toast.makeText(context, "配置已保存", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "API Key不能为空", Toast.LENGTH_SHORT).show()
            }
        }
        .setNegativeButton("取消", null)
        .show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar() {
    val navController = LocalNavController.current
    
    TopAppBar(
        title = {
            Text(text = "AI 步数分析")
        },
        navigationIcon = {
            IconButton(onClick = {
                navController.popBackStack()
            }) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
            }
        },
        actions = {
            IconButton(onClick = {
                navController.navigate(Route.AI_SETTINGS)
            }) {
                Icon(Icons.Default.Settings, contentDescription = "AI服务设置")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryLogSelector(viewModel: AiAnalysisViewModel) {
    
    // 历史记录选择
    if (viewModel.logHistoryList.isEmpty()) {
        Text("没有可用的历史数据", Modifier.fillMaxWidth())
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("历史记录:", Modifier.padding(bottom = 4.dp))
            
            var historyExpanded by remember { mutableStateOf(false) }
            val selectedLogTime = viewModel.selectedHistoryLogTime
            val historyText = viewModel.logHistoryList.find { it.logStartTime == selectedLogTime }?.let {
                val startTimeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(it.startTime))
                val endTimeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(it.endTime))

                "$startTimeStr ~ $endTimeStr"
            } ?: "选择历史记录"
            
            ExposedDropdownMenuBox(
                expanded = historyExpanded,
                onExpandedChange = { historyExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = historyText,
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = historyExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                )
                
                ExposedDropdownMenu(
                    expanded = historyExpanded,
                    onDismissRequest = { historyExpanded = false }
                ) {
                    viewModel.logHistoryList.forEach { historyItem ->
                        val startTimeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(historyItem.startTime))
                        val endTimeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(historyItem.endTime))

                        val itemText = "$startTimeStr ~ $endTimeStr"
                        
                        DropdownMenuItem(
                            text = { Text(itemText) },
                            onClick = {
                                viewModel.onHistoryLogSelected(historyItem.logStartTime)
                                historyExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryUserSelector(viewModel: AiAnalysisViewModel) {
    // 用户选择（历史数据）
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("用户:", Modifier.padding(bottom = 4.dp))
        
        var userExpanded by remember { mutableStateOf(false) }
        val selectedUser = viewModel.historyUserList[viewModel.selectedHistoryUserIndex]
        
        ExposedDropdownMenuBox(
            expanded = userExpanded,
            onExpandedChange = { userExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedUser,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = userExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
            )
            
            ExposedDropdownMenu(
                expanded = userExpanded,
                onDismissRequest = { userExpanded = false }
            ) {
                viewModel.historyUserList.forEachIndexed { index, userName ->
                    DropdownMenuItem(
                        text = { Text(userName) },
                        onClick = {
                            viewModel.onHistoryUserSelected(index)
                            userExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RealTimeUserSelector(viewModel: AiAnalysisViewModel) {
    // 用户选择（实时数据）
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("用户:", Modifier.padding(bottom = 4.dp))
        
        var userExpanded by remember { mutableStateOf(false) }
        val selectedUser = viewModel.realtimeUserList[viewModel.selectedRealtimeUserIndex]
        
        ExposedDropdownMenuBox(
            expanded = userExpanded,
            onExpandedChange = { userExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedUser,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = userExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
            )
            
            ExposedDropdownMenu(
                expanded = userExpanded,
                onDismissRequest = { userExpanded = false }
            ) {
                viewModel.realtimeUserList.forEachIndexed {index,userName ->
                    DropdownMenuItem(
                        text = { Text(userName) },
                        onClick = {
                            viewModel.onRealtimeUserSelected(index)
                            userExpanded = false
                        }
                    )
                }
            }
        }
    }
}
