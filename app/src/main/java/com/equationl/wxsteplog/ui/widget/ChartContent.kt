package com.equationl.wxsteplog.ui.widget

import android.text.Layout
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.equationl.wxsteplog.ui.view.statistics.state.StatisticsChartData
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.cartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.fixed
import com.patrykandpatrick.vico.compose.common.component.rememberLayeredComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shadow
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.data.rememberExtraLambda
import com.patrykandpatrick.vico.compose.common.dimensions
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.rememberVerticalLegend
import com.patrykandpatrick.vico.compose.common.shape.markerCorneredShape
import com.patrykandpatrick.vico.compose.common.shape.rounded
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.LayeredComponent
import com.patrykandpatrick.vico.core.common.Legend
import com.patrykandpatrick.vico.core.common.LegendItem
import com.patrykandpatrick.vico.core.common.component.Shadow
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.Corner
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LineSeriesChart(
    dataList: List<StatisticsChartData>
) {
    Log.i("el", "LineSeriesChart: $dataList")
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            //while (isActive) {
                modelProducer.runTransaction {
                    lineSeries {
                        for (data in dataList) {
                            series(data.x, data.y)
                        }
                    }
                }
                //delay(Defaults.TRANSACTION_INTERVAL_MS)
            //}
        }
    }


    CartesianChartHost(
        chart =
        rememberCartesianChart(
            // 线条样式
            rememberLineCartesianLayer(
                    LineCartesianLayer.LineProvider.series(
                        dataList.map { data ->
                            LineCartesianLayer.rememberLine(
                                fill = remember { LineCartesianLayer.LineFill.single(fill(data.color)) },
                                areaFill = null,
                            )
                        }
                    )
            ),
            // Y轴
            startAxis = VerticalAxis.rememberStart(
                label = rememberAxisLabelComponent(
                    color = Color.Black,
                    margins = dimensions(4.dp),
                    padding = dimensions(8.dp, 2.dp),
                    background = rememberShapeComponent(Color(0xfffab94d), CorneredShape.rounded(4.dp)),
                ),
                horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
            ),
            // X 轴
            bottomAxis = HorizontalAxis.rememberBottom(
                //TODO 这里需要改成只显示关键坐标，不需要连续显示
                // 好像不支持这样做，那只能缩短间隔，以半小时为间隔来显示了， 而不是现在的以分钟为间隔
                itemPlacer = HorizontalAxis.ItemPlacer.segmented(),
                valueFormatter =  { _, x, _ ->
                    // TODO 格式化成日期
                    x.toString()
                    //xLabel[x.toInt()] ?: x.toString()
                },
                labelRotationDegrees = 90f,
            ),
            marker = rememberMarker(),
            layerPadding = cartesianLayerPadding(scalableStartPadding = 16.dp, scalableEndPadding = 16.dp),
            legend = rememberLegend(dataList),
        ),
        modelProducer = modelProducer,
        zoomState = rememberVicoZoomState(zoomEnabled = true),
    )
}

// 图例
@Composable
private fun rememberLegend(dataList: List<StatisticsChartData>): Legend<CartesianMeasuringContext, CartesianDrawingContext> {
    val labelComponent = rememberTextComponent(vicoTheme.textColor)
    return rememberVerticalLegend(
        items =
        rememberExtraLambda {
            dataList.forEach {
                add(
                    LegendItem(
                        icon = shapeComponent(it.color, CorneredShape.Pill),
                        labelComponent = labelComponent,
                        label = it.label,
                    )
                )
            }
        },
        iconSize = 8.dp,
        iconPadding = 8.dp,
        spacing = 4.dp,
        padding = dimensions(top = 8.dp),
    )
}

@Composable
internal fun rememberMarker(
    labelPosition: DefaultCartesianMarker.LabelPosition = DefaultCartesianMarker.LabelPosition.Top,
    showIndicator: Boolean = true,
): CartesianMarker {
    val labelBackgroundShape = markerCorneredShape(Corner.FullyRounded)
    val labelBackground =
        rememberShapeComponent(
            color = MaterialTheme.colorScheme.surfaceBright,
            shape = labelBackgroundShape,
            shadow =
            shadow(radius = LABEL_BACKGROUND_SHADOW_RADIUS_DP.dp, dy = LABEL_BACKGROUND_SHADOW_DY_DP.dp),
        )
    val label =
        rememberTextComponent(
            color = MaterialTheme.colorScheme.onSurface,
            textAlignment = Layout.Alignment.ALIGN_CENTER,
            padding = dimensions(8.dp, 4.dp),
            background = labelBackground,
            minWidth = TextComponent.MinWidth.fixed(40.dp),
        )
    val indicatorFrontComponent =
        rememberShapeComponent(MaterialTheme.colorScheme.surface, CorneredShape.Pill)
    val indicatorCenterComponent = rememberShapeComponent(shape = CorneredShape.Pill)
    val indicatorRearComponent = rememberShapeComponent(shape = CorneredShape.Pill)
    val indicator =
        rememberLayeredComponent(
            rear = indicatorRearComponent,
            front =
            rememberLayeredComponent(
                rear = indicatorCenterComponent,
                front = indicatorFrontComponent,
                padding = dimensions(5.dp),
            ),
            padding = dimensions(10.dp),
        )
    val guideline = rememberAxisGuidelineComponent()
    return remember(label, labelPosition, indicator, showIndicator, guideline) {
        object :
            DefaultCartesianMarker(
                label = label,
                labelPosition = labelPosition,
                indicator =
                if (showIndicator) {
                    { color ->
                        LayeredComponent(
                            rear = ShapeComponent(Color(color).copy(alpha = 0.15f).value.toInt(), CorneredShape.Pill),
                            front =
                            LayeredComponent(
                                rear =
                                ShapeComponent(
                                    color = color,
                                    shape = CorneredShape.Pill,
                                    shadow = Shadow(radiusDp = 12f, color = color),
                                ),
                                front = indicatorFrontComponent,
                                padding = dimensions(5.dp),
                            ),
                            padding = dimensions(10.dp),
                        )
                    }
                } else {
                    null
                },
                indicatorSizeDp = 36f,
                guideline = guideline,
            ) {
            override fun updateInsets(
                context: CartesianMeasuringContext,
                horizontalDimensions: HorizontalDimensions,
                model: CartesianChartModel,
                insets: Insets,
            ) {
                with(context) {
                    val baseShadowInsetDp =
                        CLIPPING_FREE_SHADOW_RADIUS_MULTIPLIER * LABEL_BACKGROUND_SHADOW_RADIUS_DP
                    var topInset = (baseShadowInsetDp - LABEL_BACKGROUND_SHADOW_DY_DP).pixels
                    var bottomInset = (baseShadowInsetDp + LABEL_BACKGROUND_SHADOW_DY_DP).pixels
                    when (labelPosition) {
                        LabelPosition.Top,
                        LabelPosition.AbovePoint -> topInset += label.getHeight(context) + tickSizeDp.pixels
                        LabelPosition.Bottom -> bottomInset += label.getHeight(context) + tickSizeDp.pixels
                        LabelPosition.AroundPoint -> {}
                    }
                    insets.ensureValuesAtLeast(top = topInset, bottom = bottomInset)
                }
            }
        }
    }
}

private const val LABEL_BACKGROUND_SHADOW_RADIUS_DP = 4f
private const val LABEL_BACKGROUND_SHADOW_DY_DP = 2f
private const val CLIPPING_FREE_SHADOW_RADIUS_MULTIPLIER = 1.4f