package com.equationl.wxsteplog.ui.view.setting.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.equationl.common.constKey.CommonKey
import com.equationl.wxsteplog.aiapi.ModelBean
import com.equationl.wxsteplog.ui.LocalNavController
import com.equationl.wxsteplog.ui.view.setting.viewmodel.AiSettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun AiSettingsScreen(
    viewModel: AiSettingsViewModel = hiltViewModel()
) {
    val scaffoldState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val navController = LocalNavController.current
    
    // 收集ViewModel中的状态
    val uiState by viewModel.uiState.collectAsState()
    
    // 启动效应，在进入屏幕时获取当前配置
    LaunchedEffect(Unit) {
        viewModel.loadCurrentSettings()
    }
    
    Scaffold(
        // scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = "AI分析设置",
                onBack = {
                    navController.popBackStack()
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    snackbarData = data
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 付费服务开关
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "AI服务设置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 付费服务可用状态显示
                    Text(
                        text = if (uiState.isPaidServiceAvailable) "专业AI服务已安装" else "未检测到专业AI服务",
                        color = if (uiState.isPaidServiceAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 付费服务开关（仅当可用时显示）
                    if (uiState.isPaidServiceAvailable) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "启用专业AI分析服务",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Switch(
                                checked = uiState.isPaidServiceEnabled,
                                onCheckedChange = {
                                    viewModel.togglePaidService(it)
                                    coroutineScope.launch {
                                        scaffoldState.showSnackbar(
                                            message = if (it) "已启用专业AI分析服务" else "已切换至演示AI分析服务"
                                        )
                                    }
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = if (uiState.isPaidServiceEnabled) "当前使用：专业AI分析服务" else "当前使用：演示AI分析服务",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    } else {
                        // 如果付费服务不可用，显示获取方式
                        Text(
                            text = "获取专业AI分析服务:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "• 联系作者购买专业版\n• 返回此页开启 AI 分析服务",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // AI模型配置卡片（无论是否启用付费服务，都显示这部分）
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "AI模型配置",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 模型选择
                    DropdownField(
                        label = "选择AI模型",
                        options = uiState.availableModels,
                        selectedOption = uiState.selectedModel,
                        onOptionSelected = { viewModel.selectModel(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // API密钥输入（对于需要配置的模型）
                    if (uiState.selectedModel?.modelName != "本地模型" && uiState.selectedModel != null) {
                        var apiKey by remember(uiState.selectedModel) { mutableStateOf((uiState.modelConfigs[uiState.selectedModel?.modelName]?.get(CommonKey.API_KEY) as String?) ?: "") }
                        
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = { Text("${uiState.selectedModel?.modeShowName ?: ""} API密钥") },
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            modifier = Modifier.align(Alignment.End),
                            onClick = {
                                coroutineScope.launch {
                                    if (apiKey.isBlank()) {
                                        scaffoldState.showSnackbar("请输入 API 密钥")
                                        return@launch
                                    }

                                    viewModel.saveModelConfig(uiState.selectedModel!!, mapOf(CommonKey.API_KEY to apiKey))
                                    scaffoldState.showSnackbar("已保存 ${uiState.selectedModel!!.modeShowName} 的配置")
                                }
                            }
                        ) {
                            Text("保存配置")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 关于AI分析的说明
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "关于AI分析",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (uiState.isPaidServiceAvailable) {
                            """
                            • 演示模式: 仅用于演示功能操作逻辑和界面，未接入任何 AI 服务
                            • 专业模式: 提供高级AI分析，支持多种AI模型和更深入的数据洞察
                            
                            专业模式特点:
                            • 更精确的数据分析
                            • 支持更多AI模型选择
                            • 可以自定义分析提示
                            • 提供更详细的数据报告
                            """.trimIndent()
                        } else {
                            """
                            • 演示模式: 仅用于演示功能操作逻辑和界面，未接入任何 AI 服务
                            • 专业模式: 提供高级AI分析，支持多种AI模型和更深入的数据洞察
                            
                            • 当前版本提供演示模式 AI 分析功能
                            • 演示模式使用模拟数据和预设分析逻辑
                            • 如需接入真实 AI 分析功能，请安装专业版
                            """.trimIndent()
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    title: String,
    onBack: () -> Unit
) {
    androidx.compose.material3.TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回"
                )
            }
        }
    )
}

@Composable
fun DropdownField(
    label: String,
    options: List<ModelBean>,
    selectedOption: ModelBean?,
    onOptionSelected: (ModelBean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    text = { Text(text = option.modeShowName) },
                )
            }
        }
        
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { expanded = true }
        ) {
            Text(text = selectedOption?.modeShowName ?: "选择模型")
        }
    }
}
