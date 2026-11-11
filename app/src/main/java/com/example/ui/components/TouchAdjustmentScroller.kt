package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun TouchAdjustmentScroller(
  currentAmount: Double,
  onAmountChanged: (Double) -> Unit,
  modifier: Modifier = Modifier,
  accentColor: Color = MaterialTheme.colorScheme.primary
) {
  var dragAccumulator by remember { mutableFloatStateOf(0f) }
  var isDragging by remember { mutableStateOf(false) }

  val pillBackground = MaterialTheme.colorScheme.surfaceVariant
  val infoCardBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
  val emphasisColor = MaterialTheme.colorScheme.onSurface

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Stepper Controls & Touch Cylinder Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 2.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Negative steps
      StepPill(label = "-100", onClick = { onAmountChanged(maxOf(0.0, currentAmount - 100)) })
      StepPill(label = "-10", onClick = { onAmountChanged(maxOf(0.0, currentAmount - 10)) })
      StepPill(label = "-1", onClick = { onAmountChanged(maxOf(0.0, currentAmount - 1)) })

      // Center Neon Touch Cylinder
      Box(
        modifier = Modifier
          .width(82.dp)
          .height(58.dp)
          .clip(RoundedCornerShape(24.dp))
          .background(
            if (isDragging) accentColor.copy(alpha = 0.28f) else accentColor.copy(alpha = 0.16f)
          )
          .border(
            width = 1.6.dp,
            color = accentColor,
            shape = RoundedCornerShape(24.dp)
          )
          .draggable(
            orientation = Orientation.Vertical,
            state = rememberDraggableState { delta ->
              isDragging = true
              // Invert delta: dragging up increases amount, dragging down decreases
              dragAccumulator -= delta
              val stepThreshold = 8f // 8px per $1 step
              if (abs(dragAccumulator) >= stepThreshold) {
                val steps = (dragAccumulator / stepThreshold).toInt()
                val newAmount = maxOf(0.0, currentAmount + steps)
                onAmountChanged(newAmount)
                dragAccumulator -= (steps * stepThreshold)
              }
            },
            onDragStopped = {
              isDragging = false
              dragAccumulator = 0f
            }
          )
          .clickable {
            // Tap adds $1
            onAmountChanged(currentAmount + 1.0)
          }
          .testTag("amount_touch_scroller"),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = "Step up",
            tint = accentColor,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = "Touch",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accentColor
          )
          Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Step down",
            tint = accentColor,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      // Positive steps
      StepPill(label = "+1", onClick = { onAmountChanged(currentAmount + 1) })

      // Vertical Stepper Pill (+ 10 -)
      Box(
        modifier = Modifier
          .width(42.dp)
          .height(58.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(pillBackground)
          .testTag("step_vertical_10"),
        contentAlignment = Alignment.Center
      ) {
        Column(
          modifier = Modifier.padding(vertical = 2.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .clickable { onAmountChanged(currentAmount + 10) },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "+10",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(14.dp)
            )
          }
          Text(
            text = "10",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Box(
            modifier = Modifier
              .size(24.dp)
              .clickable { onAmountChanged(maxOf(0.0, currentAmount - 10)) },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Remove,
              contentDescription = "-10",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Info Hint Banner Matching Image 2
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = infoCardBackground,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Info,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = buildAnnotatedString {
            append("Drag the cylinder up/down or tap steps for ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = emphasisColor)) {
              append("micro-adjustment")
            }
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
          modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(MaterialTheme.colorScheme.outline)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
          imageVector = Icons.Default.TouchApp,
          contentDescription = "Swipe up or down gesture",
          tint = accentColor,
          modifier = Modifier.size(22.dp)
        )
      }
    }
  }
}

@Composable
private fun StepPill(
  label: String,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .clickable(onClick = onClick)
      .padding(horizontal = 11.dp, vertical = 14.dp)
      .testTag("step_chip_$label"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
