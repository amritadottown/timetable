package town.amrita.timetable.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipContainer(modifier: Modifier = Modifier, tooltipContent: String, content: @Composable () -> Unit) {
  TooltipBox(
    modifier = modifier,
    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(positioning = TooltipAnchorPosition.Above),
    tooltip = {
      PlainTooltip { Text(tooltipContent) }
    },
    state = rememberTooltipState()
  ) {
    content()
  }
}