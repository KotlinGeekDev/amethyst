/**
 * Copyright (c) 2024 Vitor Pamplona
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.vitorpamplona.amethyst.ui.screen.loggedIn.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberEndAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.DefaultAlpha
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.composed.plus
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.vitorpamplona.amethyst.ui.note.OneGiga
import com.vitorpamplona.amethyst.ui.note.OneKilo
import com.vitorpamplona.amethyst.ui.note.OneMega
import com.vitorpamplona.amethyst.ui.note.TenKilo
import com.vitorpamplona.amethyst.ui.note.UserReactionsRow
import com.vitorpamplona.amethyst.ui.note.showAmount
import com.vitorpamplona.amethyst.ui.note.showCount
import com.vitorpamplona.amethyst.ui.theme.BitcoinOrange
import com.vitorpamplona.amethyst.ui.theme.RoyalBlue
import com.vitorpamplona.amethyst.ui.theme.chartStyle
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt

@Composable
fun SummaryBar(state: NotificationSummaryState) {
    var showChart by remember { mutableStateOf(false) }

    UserReactionsRow(state) { showChart = !showChart }

    AnimatedVisibility(
        visible = showChart,
        enter = slideInVertically() + expandVertically(),
        exit = slideOutVertically() + shrinkVertically(),
    ) {
        val lineChartCount =
            lineChart(
                lines =
                    listOf(RoyalBlue, Color.Green, Color.Red).map { lineChartColor ->
                        LineChart.LineSpec(
                            lineColor = lineChartColor.toArgb(),
                            lineBackgroundShader =
                                DynamicShaders.fromBrush(
                                    Brush.verticalGradient(
                                        listOf(
                                            lineChartColor.copy(DefaultAlpha.LINE_BACKGROUND_SHADER_START),
                                            lineChartColor.copy(DefaultAlpha.LINE_BACKGROUND_SHADER_END),
                                        ),
                                    ),
                                ),
                        )
                    },
                targetVerticalAxisPosition = AxisPosition.Vertical.Start,
            )

        val lineChartZaps =
            lineChart(
                lines =
                    listOf(BitcoinOrange).map { lineChartColor ->
                        LineChart.LineSpec(
                            lineColor = lineChartColor.toArgb(),
                            lineBackgroundShader =
                                DynamicShaders.fromBrush(
                                    Brush.verticalGradient(
                                        listOf(
                                            lineChartColor.copy(DefaultAlpha.LINE_BACKGROUND_SHADER_START),
                                            lineChartColor.copy(DefaultAlpha.LINE_BACKGROUND_SHADER_END),
                                        ),
                                    ),
                                ),
                        )
                    },
                targetVerticalAxisPosition = AxisPosition.Vertical.End,
            )

        Row(
            modifier =
                Modifier
                    .padding(vertical = 0.dp, horizontal = 20.dp)
                    .clickable(onClick = { showChart = !showChart }),
        ) {
            ProvideChartStyle(
                chartStyle = MaterialTheme.colorScheme.chartStyle,
            ) {
                ObserveAndShowChart(state, lineChartCount, lineChartZaps)
            }
        }
    }
}

@Composable
private fun ObserveAndShowChart(
    state: NotificationSummaryState,
    lineChartCount: LineChart,
    lineChartZaps: LineChart,
) {
    val axisModel = state.axisLabels.collectAsStateWithLifecycle()
    val chartModel by state.chartModel.collectAsStateWithLifecycle()

    chartModel?.let {
        Chart(
            chart = remember(lineChartCount, lineChartZaps) { lineChartCount.plus(lineChartZaps) },
            model = it,
            startAxis =
                rememberStartAxis(
                    valueFormatter = CountAxisValueFormatter(),
                ),
            endAxis =
                rememberEndAxis(
                    label = axisLabelComponent(color = BitcoinOrange),
                    valueFormatter = AmountAxisValueFormatter(state.shouldShowDecimalsInAxis),
                ),
            bottomAxis =
                rememberBottomAxis(
                    valueFormatter = LabelValueFormatter(axisModel),
                ),
        )
    }
}

@Stable
class LabelValueFormatter(
    val axisLabels: State<List<String>>,
) : AxisValueFormatter<AxisPosition.Horizontal.Bottom> {
    override fun formatValue(
        value: Float,
        chartValues: ChartValues,
    ): String = axisLabels.value[value.roundToInt()]
}

@Stable
class CountAxisValueFormatter : AxisValueFormatter<AxisPosition.Vertical.Start> {
    override fun formatValue(
        value: Float,
        chartValues: ChartValues,
    ): String = showCount(value.roundToInt())
}

@Stable
class AmountAxisValueFormatter(
    val showDecimals: Boolean,
) : AxisValueFormatter<AxisPosition.Vertical.End> {
    override fun formatValue(
        value: Float,
        chartValues: ChartValues,
    ): String =
        if (showDecimals) {
            showAmount(value.toBigDecimal())
        } else {
            showAmountAxis(value.toBigDecimal())
        }
}

var dfG: DecimalFormat = DecimalFormat("#G")
var dfM: DecimalFormat = DecimalFormat("#M")
var dfK: DecimalFormat = DecimalFormat("#k")
var dfN: DecimalFormat = DecimalFormat("#")

fun showAmountAxis(amount: BigDecimal?): String {
    if (amount == null) return ""
    if (amount.abs() < BigDecimal(0.01)) return ""

    return when {
        amount >= OneGiga -> dfG.format(amount.div(OneGiga).setScale(0, RoundingMode.HALF_UP))
        amount >= OneMega -> dfM.format(amount.div(OneMega).setScale(0, RoundingMode.HALF_UP))
        amount >= TenKilo -> dfK.format(amount.div(OneKilo).setScale(0, RoundingMode.HALF_UP))
        else -> dfN.format(amount)
    }
}
