package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import kotlin.math.atan2

data class CategorySlice(
  val category: CategoryEntity,
  val amount: Double,
  val percentage: Float,
  val color: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDonutChart(
  slices: List<CategorySlice>,
  totalAmount: Double,
  currencySymbol: String,
  modifier: Modifier = Modifier
) {
  var selectedSliceIndex by remember { mutableStateOf<Int?>(null) }
  val animationProgress = remember { Animatable(0f) }

  LaunchedEffect(slices) {
    animationProgress.snapTo(0f)
    animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing))
  }

  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(24.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Category Distribution",
          style = MaterialTheme.styleOfChartTitle,
          color = MaterialTheme.colorScheme.onSurface
        )
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Text(
            text = "${slices.size} Categories",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (slices.isEmpty() || totalAmount <= 0.0) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "No expenses logged for this month yet",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        // Doughnut Canvas
        Box(
          modifier = Modifier.size(210.dp),
          contentAlignment = Alignment.Center
        ) {
          Canvas(
            modifier = Modifier
              .fillMaxSize()
              .pointerInput(slices) {
                detectTapGestures { offset ->
                  val center = Offset(size.width / 2f, size.height / 2f)
                  val dx = offset.x - center.x
                  val dy = offset.y - center.y
                  var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                  if (angle < 0) angle += 360f

                  var currentAngle = 0f
                  var matched: Int? = null
                  for (i in slices.indices) {
                    val sweep = slices[i].percentage * 360f
                    if (angle >= currentAngle && angle <= currentAngle + sweep) {
                      matched = i
                      break
                    }
                    currentAngle += sweep
                  }
                  selectedSliceIndex = if (selectedSliceIndex == matched) null else matched
                }
              }
          ) {
            val strokeWidth = 38.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            var startAngle = -90f
            slices.forEachIndexed { index, slice ->
              val sweep = slice.percentage * 360f * animationProgress.value
              val isSelected = selectedSliceIndex == index
              val effectiveStroke = if (isSelected) strokeWidth * 1.25f else strokeWidth

              drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = effectiveStroke, cap = StrokeCap.Round)
              )
              startAngle += sweep
            }
          }

          // Center Text
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val displaySlice = selectedSliceIndex?.let { slices.getOrNull(it) }
            if (displaySlice != null) {
              Text(
                text = displaySlice.category.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
              )
              Text(
                text = "$currencySymbol${String.format("%.2f", displaySlice.amount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${(displaySlice.percentage * 100).toInt()}% of expenses",
                style = MaterialTheme.typography.labelSmall,
                color = displaySlice.color,
                fontWeight = FontWeight.Bold
              )
            } else {
              Text(
                text = "Total Expenses",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "$currencySymbol${String.format("%,.0f", totalAmount)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Tap slice for details",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.outline
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Legend Chips
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          slices.forEachIndexed { index, slice ->
            val isSelected = selectedSliceIndex == index
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) slice.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, slice.color) else null,
              modifier = Modifier.clickable {
                selectedSliceIndex = if (selectedSliceIndex == index) null else index
              }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .background(slice.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "${slice.category.name} (${(slice.percentage * 100).toInt()}%)",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }
    }
  }
}

data class MonthlyTrendItem(
  val monthLabel: String,
  val income: Double,
  val expense: Double
)

enum class ChartStyle {
  BAR_COLUMNS,
  SPLINE_AREA
}

@Composable
fun MonthlyComparisonBarChart(
  trends: List<MonthlyTrendItem>,
  currencySymbol: String,
  modifier: Modifier = Modifier
) {
  var selectedChartStyle by remember { mutableStateOf(ChartStyle.BAR_COLUMNS) }
  var touchedItemIndex by remember { mutableStateOf<Int?>(null) }
  val animProgress = remember { Animatable(0f) }

  LaunchedEffect(trends, selectedChartStyle) {
    animProgress.snapTo(0f)
    animProgress.animateTo(1f, animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing))
  }

  val maxVal = remember(trends) {
    val highest = trends.maxOfOrNull { maxOf(it.income, it.expense) } ?: 1000.0
    if (highest <= 0.0) 1000.0 else highest * 1.15
  }

  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(24.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      // Header and Style Switcher
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Historical Cash Flow",
            style = MaterialTheme.styleOfChartTitle,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "6-Month Income vs. Expense Trend",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Toggle between Bars and Curve Area
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          FilterChip(
            selected = selectedChartStyle == ChartStyle.BAR_COLUMNS,
            onClick = { selectedChartStyle = ChartStyle.BAR_COLUMNS; touchedItemIndex = null },
            label = { Text("Bars", fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(14.dp)) },
            shape = RoundedCornerShape(10.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
          )
          FilterChip(
            selected = selectedChartStyle == ChartStyle.SPLINE_AREA,
            onClick = { selectedChartStyle = ChartStyle.SPLINE_AREA; touchedItemIndex = null },
            label = { Text("Curve", fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, modifier = Modifier.size(14.dp)) },
            shape = RoundedCornerShape(10.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Active Tooltip Banner if a month column is tapped
      AnimatedVisibility(
        visible = touchedItemIndex != null,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        val touched = touchedItemIndex?.let { trends.getOrNull(it) }
        if (touched != null) {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 12.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${touched.monthLabel} Summary:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                  text = "In: +$currencySymbol${String.format("%.0f", touched.income)}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF0F9D58)
                )
                Text(
                  text = "Out: -$currencySymbol${String.format("%.0f", touched.expense)}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFEA4335)
                )
                val net = touched.income - touched.expense
                Text(
                  text = "Net: ${if (net >= 0) "+" else ""}$currencySymbol${String.format("%.0f", net)}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (net >= 0) Color(0xFF0F9D58) else Color(0xFFEA4335)
                )
              }
            }
          }
        }
      }

      if (trends.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No historical trends to visualize yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        // Chart Canvas
        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .pointerInput(trends) {
              detectTapGestures { offset ->
                val groupWidth = size.width / trends.size
                val index = (offset.x / groupWidth).toInt().coerceIn(0, trends.size - 1)
                touchedItemIndex = if (touchedItemIndex == index) null else index
              }
            }
        ) {
          val width = size.width
          val height = size.height
          val bottomY = height - 26.dp.toPx()
          val chartHeight = bottomY - 14.dp.toPx()

          val groupCount = trends.size
          val groupWidth = width / groupCount

          // 1. Grid Lines
          val gridLines = 3
          for (g in 0..gridLines) {
            val y = 14.dp.toPx() + (chartHeight * g / gridLines)
            drawLine(
              color = Color.LightGray.copy(alpha = 0.25f),
              start = Offset(0f, y),
              end = Offset(width, y),
              strokeWidth = 1.dp.toPx()
            )
          }

          // 2. Render Based on Mode
          when (selectedChartStyle) {
            ChartStyle.BAR_COLUMNS -> {
              val barWidth = 13.dp.toPx()
              trends.forEachIndexed { i, item ->
                val centerX = i * groupWidth + (groupWidth / 2f)
                val isSelected = touchedItemIndex == i

                // Highlight background column when touched
                if (isSelected) {
                  drawRoundRect(
                    color = Color.LightGray.copy(alpha = 0.15f),
                    topLeft = Offset(i * groupWidth, 0f),
                    size = Size(groupWidth, bottomY),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx())
                  )
                }

                // Income bar (green)
                val incBarHeight = ((item.income / maxVal) * chartHeight * animProgress.value).toFloat()
                val incX = centerX - barWidth - 2.5.dp.toPx()
                val incY = bottomY - incBarHeight

                drawRoundRect(
                  brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF81C995), Color(0xFF0F9D58))
                  ),
                  topLeft = Offset(incX, incY),
                  size = Size(barWidth, incBarHeight),
                  cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                // Expense bar (red)
                val expBarHeight = ((item.expense / maxVal) * chartHeight * animProgress.value).toFloat()
                val expX = centerX + 2.5.dp.toPx()
                val expY = bottomY - expBarHeight

                drawRoundRect(
                  brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF28B82), Color(0xFFEA4335))
                  ),
                  topLeft = Offset(expX, expY),
                  size = Size(barWidth, expBarHeight),
                  cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
              }
            }

            ChartStyle.SPLINE_AREA -> {
              // Smooth Spline Curves for Income and Expense
              val incomePoints = trends.mapIndexed { i, item ->
                val x = i * groupWidth + (groupWidth / 2f)
                val y = bottomY - ((item.income / maxVal) * chartHeight * animProgress.value).toFloat()
                Offset(x, y)
              }

              val expensePoints = trends.mapIndexed { i, item ->
                val x = i * groupWidth + (groupWidth / 2f)
                val y = bottomY - ((item.expense / maxVal) * chartHeight * animProgress.value).toFloat()
                Offset(x, y)
              }

              // Draw Income Area Gradient & Line
              val incPath = buildSplinePath(incomePoints)
              val incAreaPath = Path().apply {
                addPath(incPath)
                lineTo(incomePoints.last().x, bottomY)
                lineTo(incomePoints.first().x, bottomY)
                close()
              }

              drawPath(
                path = incAreaPath,
                brush = Brush.verticalGradient(
                  colors = listOf(Color(0xFF0F9D58).copy(alpha = 0.25f), Color(0xFF0F9D58).copy(alpha = 0.02f)),
                  startY = 0f,
                  endY = bottomY
                )
              )

              drawPath(
                path = incPath,
                color = Color(0xFF0F9D58),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
              )

              // Draw Expense Area Gradient & Line
              val expPath = buildSplinePath(expensePoints)
              val expAreaPath = Path().apply {
                addPath(expPath)
                lineTo(expensePoints.last().x, bottomY)
                lineTo(expensePoints.first().x, bottomY)
                close()
              }

              drawPath(
                path = expAreaPath,
                brush = Brush.verticalGradient(
                  colors = listOf(Color(0xFFEA4335).copy(alpha = 0.20f), Color(0xFFEA4335).copy(alpha = 0.02f)),
                  startY = 0f,
                  endY = bottomY
                )
              )

              drawPath(
                path = expPath,
                color = Color(0xFFEA4335),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
              )

              // Draw Data Point Dots
              incomePoints.forEach { pt ->
                drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                drawCircle(color = Color(0xFF0F9D58), radius = 3.5.dp.toPx(), center = pt)
              }

              expensePoints.forEach { pt ->
                drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                drawCircle(color = Color(0xFFEA4335), radius = 3.5.dp.toPx(), center = pt)
              }
            }
          }
        }

        // X-Axis Month Labels Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          trends.forEachIndexed { i, it ->
            val isSelected = touchedItemIndex == i
            Text(
              text = it.monthLabel,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

/**
 * Builds a smooth cubic Bézier spline through points.
 */
private fun buildSplinePath(points: List<Offset>): Path {
  val path = Path()
  if (points.isEmpty()) return path
  path.moveTo(points.first().x, points.first().y)

  for (i in 0 until points.size - 1) {
    val current = points[i]
    val next = points[i + 1]
    val controlX = (current.x + next.x) / 2f
    path.cubicTo(
      controlX, current.y,
      controlX, next.y,
      next.x, next.y
    )
  }
  return path
}

private val MaterialTheme.styleOfChartTitle
  @Composable get() = MaterialTheme.typography.titleMedium.copy(
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 0.15.sp
  )
