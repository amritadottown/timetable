package me.heftymouse.timetable.ui

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.heftymouse.timetable.registry.RegistryCourses
import me.heftymouse.timetable.registry.RegistryService
import me.heftymouse.timetable.ui.components.TimetableScaffold
import me.heftymouse.timetable.utils.updateTimetableFromUri
import retrofit2.await

@Composable
fun RegistryScreen(goBack: () -> Unit, viewModel: RegistryScreenViewModel = viewModel()) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val state by viewModel.state.collectAsStateWithLifecycle()

  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      Log.d("Timetable", uri?.toString() ?: "no uri")
      if (uri != null) {
        scope.launch {
          context.updateTimetableFromUri(uri)
        }
      }
    }

  TimetableScaffold(title = "Select Timetable") {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
      Button({
        launcher.launch(arrayOf("application/json"))
      }) {
        Text("Pick File")
      }
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box {
          DropdownPicker(
            options = state.courses.toList(),
            selected = state.currentCourse,
            label = "Course",
            onSelectionChanged = { viewModel.courseChanged(it) })
        }
        Box {
          DropdownPicker(
            options = state.years.toList(),
            selected = state.currentYear,
            label = "Year",
            onSelectionChanged = { viewModel.yearChanged(it) })
        }
        Box {
          DropdownPicker(
            options = state.semesters.toList(),
            selected = state.currentSemester,
            label = "Section",
            onSelectionChanged = { viewModel.sectionChanged(it) })
        }
      }
      LazyColumn {
        items(items = state.sections.toList()) { e ->
          Text(e)
        }
      }
    }
  }
}

data class RegistryScreenState(
  val courses: Set<String> = setOf(),
  val currentCourse: String? = null,
  val years: Set<String> = setOf(),
  val currentYear: String? = null,
  val semesters: Set<String> = setOf(),
  val currentSemester: String? = null,
  val sections: Set<String> = setOf()
)

class RegistryScreenViewModel : ViewModel() {
  private val _state = MutableStateFlow(RegistryScreenState())
  val state = _state.asStateFlow()

  private lateinit var tt: RegistryCourses

  init {
    viewModelScope.launch {
      try {
        tt = RegistryService.instance.getRegistry().await().timetables
        _state.update { e -> e.copy(courses = tt.keys) }
      } catch (e: Exception) {
        Log.d("Timetable", e.toString())
      }
    }
  }

  fun courseChanged(newValue: String?) {
    _state.update { fixState(it.copy(currentCourse = newValue)) }
  }

  fun yearChanged(newValue: String?) {
    _state.update { fixState(it.copy(currentYear = newValue)) }
  }

  fun sectionChanged(newValue: String?) {
    _state.update { fixState(it.copy(currentSemester = newValue)) }
  }

  fun fixState(s: RegistryScreenState): RegistryScreenState {
    val newYears = tt[s.currentCourse] ?: emptyMap()
    val newYear =
      if (newYears.containsKey(s.currentYear))
        s.currentYear
      else
        null

    val newSemesters = newYears[newYear] ?: emptyMap()
    val newSemester =
      if (newSemesters.containsKey(s.currentSemester))
        s.currentSemester
      else
        null

    val newSections = newSemesters[newSemester]?.toSet() ?: setOf()

    return s.copy(
      years = newYears.keys,
      semesters = newSemesters.keys,
      currentYear = newYear,
      currentSemester = newSemester,
      sections = newSections
    )
  }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownPicker(
  modifier: Modifier = Modifier,
  options: List<String>,
  selected: String?,
  label: String,
  onSelectionChanged: (String?) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  val textFieldState = TextFieldState(selected ?: "Select")

  ExposedDropdownMenuBox(
    modifier = modifier,
    expanded = expanded,
    onExpandedChange = { expanded = it }) {
    OutlinedTextField(
      modifier = Modifier
        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        .fillMaxWidth(),
      state = textFieldState,
      label = @Composable { Text(label) },
      readOnly = true,
      lineLimits = TextFieldLineLimits.SingleLine,
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
    )
    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      DropdownMenuItem(
        text = { Text("Select", style = MaterialTheme.typography.bodyLarge) },
        onClick = {
          textFieldState.setTextAndPlaceCursorAtEnd("Select")
          expanded = false
          onSelectionChanged(null)
        },
        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
      )
      options.forEach { option ->
        DropdownMenuItem(
          text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
          onClick = {
            textFieldState.setTextAndPlaceCursorAtEnd(option)
            expanded = false
            onSelectionChanged(option)
          },
          contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
        )
      }
    }
  }
}

@Composable
@Preview
fun TimetablePickerScreenPreview() {
  TimetableTheme {
    RegistryScreen(goBack = { })
  }
}