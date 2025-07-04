package me.heftymouse.timetable.ui

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import me.heftymouse.timetable.registry.Registry
import me.heftymouse.timetable.registry.RegistryCourses
import me.heftymouse.timetable.registry.RegistryService
import me.heftymouse.timetable.ui.components.TimetableScaffold
import me.heftymouse.timetable.utils.updateTimetableFromUri
import retrofit2.await

@Composable
fun RegistryScreen(goBack: () -> Unit) {
  val scope = rememberCoroutineScope()
  var data: RegistryCourses? by remember { mutableStateOf(null) }

  var courses: Set<String> by remember { mutableStateOf(emptySet<String>()) }
  var years: Set<String> by remember { mutableStateOf(emptySet<String>()) }
  var semesters: Set<String> by remember { mutableStateOf(emptySet<String>()) }

  var currentCourse: String? by remember { mutableStateOf(null) }
  var currentYear: String? by remember { mutableStateOf(null) }
  var currentSemester: String? by remember { mutableStateOf(null) }

  val context = LocalContext.current

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
    LaunchedEffect(true) {
      try {
        data = RegistryService.instance.getRegistry().await().timetables
        courses = data!!.keys
      } catch (e: Exception) {
        Log.d("Timetable", e.toString())
      }
    }
    Column {
      Button({
        launcher.launch(arrayOf("application/json"))
      }) {
        Text("Pick File")
      }
      Column(Modifier.fillMaxWidth()) {
        Box {
          DropdownPicker(
            options = courses.toList(),
            selected = currentCourse,
            label = "Course",
            onSelectionChanged = {
              currentCourse = it
              if(data == null) return@DropdownPicker
              if(data?.containsKey(currentCourse))

              val tt = data?.timetables!!
              currentCourse = it
              if(tt.containsKey(currentCourse!!) == true) {
                years = tt[currentCourse]!!.keys
                currentYear = if (tt.containsKey(currentYear) == true) currentYear else null
                semesters = tt[currentCourse]!!.getOrDefault(currentYear, mapOf()).keys
                if (tt[currentCourse]!!.getOrDefault(currentYear, mapOf()).containsKey(currentSemester) != true) {
                  currentSemester = null
                }
              }

            })
        }
        Box {
          DropdownPicker(
            options = years.toList(),
            selected = currentYear,
            label = "Year",
            onSelectionChanged = {
              years = data?.timetables[currentCourse]?.keys!!
              Log.d("Timetable", years.toString())
              currentYear = if (data?.timetables[currentCourse]?.containsKey(currentYear) == true) currentYear else null
              currentSemester = if (data?.timetables[currentCourse]?.getOrDefault(currentYear, null)?.containsKey(currentSemester) == true) currentSemester else null
            })
        }
        Box {
          DropdownPicker(
            options = semesters.toList(),
            selected = currentSemester,
            label = "Class",
            onSelectionChanged = {
              currentCourse = it
              years = data?.timetables[currentCourse]?.keys!!
              Log.d("Timetable", years.toString())
              currentYear = if (data?.timetables[currentCourse]?.containsKey(currentYear) == true) currentYear else null
              currentSemester = if (data?.timetables[currentCourse]?.getOrDefault(currentYear, null)?.containsKey(currentSemester) == true) currentSemester else null
            })
        }
      }
      LazyColumn {
        items(items = years.toList()) { e ->
          Text(e)
        }
      }
    }
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
  val textFieldState = rememberTextFieldState(selected ?: "Select")

  ExposedDropdownMenuBox(
    modifier = modifier,
    expanded = expanded,
    onExpandedChange = { expanded = it }) {
    OutlinedTextField(
      modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
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
    RegistryScreen { }
  }
}