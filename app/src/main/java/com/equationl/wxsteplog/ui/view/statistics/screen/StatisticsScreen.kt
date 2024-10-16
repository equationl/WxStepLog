package com.equationl.wxsteplog.ui.view.statistics.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.BackupTable
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.InsertChartOutlined
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equationl.wxsteplog.ui.LocalNavController
import com.equationl.wxsteplog.ui.view.statistics.state.StatisticsShowRange
import com.equationl.wxsteplog.ui.view.statistics.state.StatisticsShowType
import com.equationl.wxsteplog.ui.view.statistics.state.StatisticsState
import com.equationl.wxsteplog.ui.view.statistics.viewmodel.StatisticsViewModel
import com.equationl.wxsteplog.ui.widget.DateTimeRangePickerDialog
import com.equationl.wxsteplog.ui.widget.LoadingContent
import com.equationl.wxsteplog.util.DateTimeUtil.formatDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        scope.launch(Dispatchers.IO) {
            viewModel.exportData(result, context)
        }
    }

    val isListScroll by remember{
        derivedStateOf {
            state.listState.firstVisibleItemIndex > 0
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val collapsedFraction by remember {
        derivedStateOf {
            scrollBehavior.state.collapsedFraction
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                showType = state.showType,
                iniDateRangeValue = state.showRange,
                onFilterDateRange = viewModel::onFilterShowRange,
                scrollBehavior = scrollBehavior,
                collapsedFraction = collapsedFraction,
                onChangeShowType = {
                    viewModel.onChangeShowType(context)
                },
                onExport = {
                    val intent = viewModel.createNewDocumentIntent()
                    exportLauncher.launch(intent)
                },
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = state.showType == StatisticsShowType.List && isListScroll,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            state.listState.animateScrollToItem(0)
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Outlined.ArrowUpward, contentDescription = "Back to top")
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                LoadingContent()
            }
            else {
                HomeContent(state)
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    showType: StatisticsShowType,
    iniDateRangeValue: StatisticsShowRange,
    scrollBehavior: TopAppBarScrollBehavior,
    collapsedFraction: Float,
    onFilterDateRange: (value: StatisticsShowRange) -> Unit,
    onChangeShowType: () -> Unit,
    onExport: () -> Unit,
) {
    val navController = LocalNavController.current
    var isShowDatePickedDialog by remember { mutableStateOf(false) }
    var isExpandMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    MediumTopAppBar(
        title = {
            if (collapsedFraction >= 0.5f) {
                Text(text = "统计")
            }
            else {
                Text(text = "统计(${iniDateRangeValue.start.formatDateTime("yyyyMMdd")}-${iniDateRangeValue.end.formatDateTime("yyyyMMdd")})")
            }
        },
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "back"
                )
            }
        },
        actions = {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip {
                        Text(
                            text = "From ${iniDateRangeValue.start.formatDateTime("yyyy-MM-dd")} To ${iniDateRangeValue.end.formatDateTime("yyyy-MM-dd")}"
                        )
                    }
                },
                state = rememberTooltipState(isPersistent = true)
            ) {
                IconButton(
                    onClick = {
                        isShowDatePickedDialog = true
                    }
                ) {
                    Icon(Icons.Outlined.DateRange, contentDescription = "date filter")
                }
            }
            IconButton(onClick = onChangeShowType) {
                Icon(
                    if (showType == StatisticsShowType.List) Icons.Outlined.InsertChartOutlined else Icons.AutoMirrored.Outlined.List,
                    contentDescription = "show type"
                )
            }
            IconButton(
                onClick = {
                    isExpandMenu = true
                }
            ) {
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = "more function"
                )
            }

            TopBarMoreFunction(
                expanded = isExpandMenu,
                onDismissRequest = { isExpandMenu = !isExpandMenu },
                onClickExport = {
                    isExpandMenu = false
                    onExport()
                },
            )
        }
    )

    if (isShowDatePickedDialog) {
        DateTimeRangePickerDialog(
            initValue = iniDateRangeValue,
            onFilterDate = onFilterDateRange,
            onDismissRequest = {
                isShowDatePickedDialog = false
            }
        )
    }
}

@Composable
private fun TopBarMoreFunction(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onClickExport: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
        DropdownMenuItem(
            text = {
                Text(text = "Export Data To .csv")
            },
            onClick = onClickExport,
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.BackupTable, contentDescription = "export")
            }
        )
    }

}

@Composable
private fun HomeContent(
    state: StatisticsState,
) {
    when (state.showType) {
        StatisticsShowType.List -> ListContent(state)
        StatisticsShowType.Chart -> ChartContent(state)
    }
}

@Composable
private fun ListContent(
    state: StatisticsState,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("TODO……")
    }

}

@Composable
private fun ChartContent(
    state: StatisticsState,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("TODO……")
    }
}