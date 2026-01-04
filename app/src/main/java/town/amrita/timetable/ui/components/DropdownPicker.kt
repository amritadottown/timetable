package town.amrita.timetable.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import town.amrita.timetable.utils.longName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownPicker(
  modifier: Modifier = Modifier,
  options: List<String>,
  displayOptions: List<String> = options,
  selected: String?,
  label: String,
  onSelectionChanged: (String?) -> Unit,
  allowNull: Boolean = true
) {
  var expanded by remember { mutableStateOf(false) }
  val textFieldState = TextFieldState(selected ?: "Select")

  Column(modifier) {
    Text(label, fontWeight = FontWeight.Medium)

    Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      options.zip(displayOptions).map { (option, displayOption) ->
        FilterChip(modifier = Modifier.height(32.dp), selected = selected == option,
          onClick = {
            if (allowNull && selected == option) onSelectionChanged(null)
            else onSelectionChanged(option)
          },
          label = {
          Text(displayOption)
          }
        )
      }
    }
  }

//  Box(modifier = modifier) {
//    ExposedDropdownMenuBox(
//      expanded = expanded,
//      onExpandedChange = { expanded = it }) {
//      OutlinedTextField(
//        modifier = Modifier
//          .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
//          .fillMaxWidth(),
//        state = textFieldState,
//        label = @Composable { Text(label) },
//        readOnly = true,
//        lineLimits = TextFieldLineLimits.SingleLine,
//        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
//      )
//      ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
//        if(allowNull) {
//          DropdownMenuItem(
//            text = { Text("Select", style = MaterialTheme.typography.bodyLarge) },
//            onClick = {
//              textFieldState.setTextAndPlaceCursorAtEnd("Select")
//              expanded = false
//              onSelectionChanged(null)
//            },
//            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
//          )
//        }
//        options.zip(displayOptions).map{ option ->
//          DropdownMenuItem(
//            text = { Text(option.second, style = MaterialTheme.typography.bodyLarge) },
//            onClick = {
//              textFieldState.setTextAndPlaceCursorAtEnd(option.second)
//              expanded = false
//              onSelectionChanged(option.first)
//            },
//            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
//          )
//        }
//      }
//    }
//  }
}
