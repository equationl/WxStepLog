package com.equationl.wxsteplog.ui.view.aianalysis.screen

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.equationl.wxsteplog.ai.AnalysisStatus
import com.equationl.wxsteplog.model.StepHistoryLogStartTimeDbModel
import com.equationl.wxsteplog.ui.LocalNavController
import com.equationl.wxsteplog.ui.view.aianalysis.viewmodel.AiAnalysisViewModel
import java.util.Calendar
import java.util.Date

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
    val context = LocalContext.current
    val analysisState by viewModel.analysisState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
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
                Text("1. 选择数据", style = MaterialTheme.typography.titleMedium)
                
                // 历史记录选择
                HistoryLogSelector(viewModel)
                
                // 用户选择
                UserSelector(viewModel)
                
                // 日期范围选择
                DateRangeSelector(viewModel)
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
                
                // 模型选择
                ModelSelector(viewModel)
                
                // 提示输入
                OutlinedTextField(
                    value = viewModel.userPrompt,
                    onValueChange = { viewModel.onPromptChanged(it) },
                    label = { Text("额外分析提示（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("例如：分析我的步数与健康的关系") }
                )
            }
        }
        
        // 分析按钮
        Button(
            onClick = { 
                if (!viewModel.isModelConfigured(viewModel.selectedModel)) {
                    Toast.makeText(context, "请先配置选中的AI模型", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                viewModel.startAnalysis() 
            },
            enabled = !viewModel.isAnalyzing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("开始分析")
        }
        
        // 分析结果展示
        analysisState?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "分析结果 (${result.modelName})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        if (result.status == AnalysisStatus.PROCESSING) {
                            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 错误信息
                    result.errorMessage?.let { error ->
                        Text(
                            text = "错误: $error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    // 分析内容
                    if (result.content.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = result.content,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    // 取消按钮
                    if (result.status == AnalysisStatus.PROCESSING) {
                        OutlinedButton(
                            onClick = { viewModel.cancelAnalysis() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("取消分析")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryLogSelector(viewModel: AiAnalysisViewModel) {
    var expanded by remember { mutableStateOf(false) }
    
    // 数据列表为空时加载数据
    LaunchedEffect(Unit) {
        if (viewModel.logHistoryList.isEmpty()) {
            viewModel.loadLogHistoryList()
        }
    }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = if (viewModel.selectedHistoryLogTime > 0) {
                val item = viewModel.logHistoryList.find { it.logStartTime == viewModel.selectedHistoryLogTime }
                formatHistoryLogItem(item)
            } else {
                "请选择历史记录"
            },
            onValueChange = {},
            label = { Text("数据源") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            viewModel.logHistoryList.forEach { item ->
                DropdownMenuItem(
                    text = { Text(formatHistoryLogItem(item)) },
                    onClick = {
                        viewModel.onHistoryLogSelected(item.logStartTime)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
private fun formatHistoryLogItem(item: StepHistoryLogStartTimeDbModel?): String {
    return item?.let {
        val startDate = item.startTime?.let { time ->
            android.text.format.DateFormat.format("yyyy-MM-dd", Date(time))
        } ?: "未知"
        val endDate = item.endTime?.let { time ->
            android.text.format.DateFormat.format("yyyy-MM-dd", Date(time))
        } ?: "未知"
        "记录组: $startDate 至 $endDate (${item.count}条)"
    } ?: "未选择"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserSelector(viewModel: AiAnalysisViewModel) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = viewModel.selectedUser.takeIf { it.isNotEmpty() } ?: "请选择用户",
            onValueChange = {},
            label = { Text("用户") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            viewModel.userList.forEach { user ->
                DropdownMenuItem(
                    text = { Text(user) },
                    onClick = {
                        viewModel.onUserSelected(user)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
private fun DateRangeSelector(viewModel: AiAnalysisViewModel) {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 开始日期选择器
        OutlinedTextField(
            value = viewModel.formatDate(viewModel.startDate),
            onValueChange = {},
            readOnly = true,
            label = { Text("开始日期") },
            trailingIcon = {
                IconButton(onClick = {
                    showDatePicker(context, viewModel.startDate) { date ->
                        viewModel.onStartDateChanged(date)
                    }
                }) {
                    Icon(Icons.Outlined.DateRange, contentDescription = "选择开始日期")
                }
            },
            modifier = Modifier.weight(1f)
        )
        
        // 结束日期选择器
        OutlinedTextField(
            value = viewModel.formatDate(viewModel.endDate),
            onValueChange = {},
            readOnly = true,
            label = { Text("结束日期") },
            trailingIcon = {
                IconButton(onClick = {
                    showDatePicker(context, viewModel.endDate) { date ->
                        viewModel.onEndDateChanged(date)
                    }
                }) {
                    Icon(Icons.Outlined.DateRange, contentDescription = "选择结束日期")
                }
            },
            modifier = Modifier.weight(1f)
        )
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
                value = viewModel.selectedModel.takeIf { it.isNotEmpty() } ?: "请选择AI模型",
                onValueChange = {},
                label = { Text("AI模型") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
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
                                Text(model)
                                if (!viewModel.isModelConfigured(model)) {
                                    Text(
                                        " (未配置)",
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
            // 这里简化处理，直接弹出Toast提示
            if (viewModel.selectedModel.isEmpty()) {
                Toast.makeText(context, "请先选择一个AI模型", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "请在设置中配置AI模型", Toast.LENGTH_SHORT).show()
            }
        }) {
            Icon(Icons.Filled.Settings, contentDescription = "配置模型")
        }
    }
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
        }
    )
}

private fun showDatePicker(
    context: android.content.Context,
    initialDate: Date,
    onDateSelected: (Date) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        time = initialDate
    }
    
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                onDateSelected(time)
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
