package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TouchAdjustmentScroller(
  currentAmount: Double,
  onAmountChanged: (Double) -> Unit,
  modifier: Modifier = Modifier,
  accentColor: Color = MaterialTheme.colorScheme.primary
) {
  var dragAccumulator by remember { mutableFloatStateOf(0f) }
  var isDragging by remember { mutableStateOf(false) }

  val trackColor by animateColorAsState(
    targetValue = if (isDragging) accentColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
    label = "trackColor"
  )

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Quick Step Buttons Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      StepChip(label = "-100", onClick = { onAmountChanged(maxOf(0.0, currentAmount - 100)) })
      StepChip(label = "-10", onClick = { onAmountChanged(maxOf(0.0, currentAmount - 10)) })
      StepChip(label = "-1", onClick = { onAmountChanged(maxOf(0.0, currentAmount - 1)) })

      // Vertical Touch Scroll Wheel / Drag Handle
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = trackColor,
        tonalElevation = 2.dp,
        modifier = Modifier
          .width(72.dp)
          .height(52.dp)
          .draggable(
            orientation = Orientation.Vertical,
            state = rememberDraggableState { delta ->
              isDragging = true
              // Invert delta: dragging up increases amount, dragging down decreases
              dragAccumulator -= delta
              val stepThreshold = 8f // pixels per $1
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
          .testTag("amount_touch_scroller")
      ) {
        Column(
          modifier = Modifier.padding(4.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = "Scroll up to increase",
            tint = accentColor,
            modifier = Modifier.size(16.dp)
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.UnfoldMore,
              contentDescription = "Touch scroller drag handle",
              tint = accentColor,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
              text = "Touch",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              fontSize = 9.sp,
              color = accentColor
            )
          }
          Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Scroll down to decrease",
            tint = accentColor,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      StepChip(label = "+1", onClick = { onAmountChanged(currentAmount + 1) })
      StepChip(label = "+10", onClick = { onAmountChanged(currentAmount + 10) })
      StepChip(label = "+100", onClick = { onAmountChanged(currentAmount + 100) })
    }

    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "Drag touch cylinder up/down or tap steps for micro-adjustment",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontSize = 11.sp
    )
  }
}

@Composable
private fun StepChip(
  label: String,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .testTag("step_chip_$label"),
    onClick = onClick
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
