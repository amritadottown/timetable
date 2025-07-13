package me.heftymouse.timetable.ui

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import kotlinx.serialization.ExperimentalSerializationApi
import me.heftymouse.timetable.models.Timetable
import me.heftymouse.timetable.models.TimetableSpec
import me.heftymouse.timetable.registry.RegistryService
import me.heftymouse.timetable.registry.RegistryYears
import me.heftymouse.timetable.ui.components.TimetablePreview
import me.heftymouse.timetable.ui.components.TimetableScaffold
import me.heftymouse.timetable.utils.getDisplayName
import me.heftymouse.timetable.utils.getFileContent
import me.heftymouse.timetable.utils.updateTimetableFromRegistry
import me.heftymouse.timetable.utils.updateTimetableFromUri
import retrofit2.await

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun TimetablePickerScreen(
  viewModel: RegistryScreenViewModel = viewModel(),
  globalActions: @Composable (RowScope.() -> Unit) = {}
) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val state by viewModel.state.collectAsStateWithLifecycle()

  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      Log.d("Timetable", uri?.toString() ?: "no uri")
      if (uri != null) {
        val fileName = context.getDisplayName(uri)
        viewModel.localUriChanged(uri, fileName)
      }
    }

  var selectedTab by remember { mutableIntStateOf(0) }

  var timetableSelected by remember { mutableStateOf(false) }
  var timetable by remember { mutableStateOf<Timetable?>(null) }

  LaunchedEffect(state.source, state.registryState, state.localPickerState) {
    when (state.source) {
      TimetablePickerSource.Registry -> {
        val registryState = state.registryState
        if (registryState.currentYear != null && registryState.currentSection != null && registryState.currentSemester != null) {
          timetable = null
          timetableSelected = true
          val newSpec = TimetableSpec(
            registryState.currentYear,
            registryState.currentSection,
            registryState.currentSemester
          )
          timetable = RegistryService.instance.getTimetable(newSpec).await()
        } else {
          timetableSelected = false
        }
      }

      TimetablePickerSource.Local -> {
        timetableSelected = false
        state.localPickerState.fileUri?.let {
          timetable = context.getFileContent(it)
          timetableSelected = true
        }
      }
    }
  }

  TimetableScaffold(
    title = "Select Timetable",
    actions = globalActions
  ) {
    Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(24.dp)) {

      SecondaryTabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent) {
        Tab(
          selected = state.source == TimetablePickerSource.Registry,
          onClick = { selectedTab = 0; viewModel.sourceChanged(TimetablePickerSource.Registry) },
          text = { Text("Online") }
        )
        Tab(
          selected = state.source == TimetablePickerSource.Local,
          onClick = { selectedTab = 1; viewModel.sourceChanged(TimetablePickerSource.Local) },
          text = { Text("Local") }
        )
      }

      when (state.source) {
        TimetablePickerSource.Registry -> {
          with(state.registryState) {
            if (ready) {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box {
                  DropdownPicker(
                    options = years.toList(),
                    selected = currentYear,
                    label = "Start Year",
                    onSelectionChanged = { viewModel.yearChanged(it) })
                }
                Box {
                  DropdownPicker(
                    options = sections.toList(),
                    selected = currentSection,
                    label = "Section",
                    onSelectionChanged = { viewModel.sectionChanged(it) })
                }
                Box {
                  DropdownPicker(
                    options = semesters.toList(),
                    selected = currentSemester,
                    label = "Semester",
                    onSelectionChanged = { viewModel.semesterChanged(it) })
                }
              }
            } else {
              Column(
                Modifier
                  .fillMaxSize()
                  .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                CircularProgressIndicator()
              }
            }
          }
        }

        TimetablePickerSource.Local -> {
          with(state.localPickerState) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              FilledTonalButton(onClick = {
                launcher.launch(arrayOf("application/json"))
              }) {
                Text("Pick File")
              }

              if (fileName != null) {
                Text(fileName)
              }
            }
          }
        }
      }

      if (timetableSelected) {
        TimetablePreview(
          Modifier
            .weight(1f)
            .fillMaxSize(), timetable = timetable
        )
      } else {
        Spacer(Modifier
          .weight(1f)
          .fillMaxSize())
      }

      Button(
        modifier = Modifier.fillMaxWidth(),
        enabled = timetableSelected,
        onClick = {
          scope.launch {
            when (state.source) {
              TimetablePickerSource.Registry -> {
                val registryState = state.registryState
                if (registryState.currentYear != null && registryState.currentSection != null && registryState.currentSemester != null) {
                  val newSpec = TimetableSpec(
                    registryState.currentYear,
                    registryState.currentSection,
                    registryState.currentSemester
                  )
                  context.updateTimetableFromRegistry(newSpec)
                }
              }

              TimetablePickerSource.Local -> {
                state.localPickerState.fileUri?.let { context.updateTimetableFromUri(it) }
              }
            }
          }
        }) {
        Text("Use Timetable")
      }
    }
  }
}

data class TimetablePickerScreenState(
  val source: TimetablePickerSource = TimetablePickerSource.Registry,
  val registryState: RegistryPickerState = RegistryPickerState(),
  val localPickerState: LocalPickerState = LocalPickerState(),
)

enum class TimetablePickerSource {
  Registry,
  Local
}

data class RegistryPickerState(
  val ready: Boolean = false,
  val years: Set<String> = setOf(),
  val currentYear: String? = null,
  val sections: Set<String> = setOf(),
  val currentSection: String? = null,
  val semesters: Set<String> = setOf(),
  val currentSemester: String? = null
)

data class LocalPickerState(
  val fileUri: Uri? = null,
  val fileName: String? = null
)


class RegistryScreenViewModel : ViewModel() {
  private val _state = MutableStateFlow(TimetablePickerScreenState())
  val state = _state.asStateFlow()

  private lateinit var registryYears: RegistryYears

  init {
    viewModelScope.launch {
      try {
        registryYears = RegistryService.instance.getRegistry().await().timetables
        _state.update { e ->
          e.copy(
            registryState = e.registryState.copy(
              years = registryYears.keys,
              ready = true
            )
          )
        }
      } catch (e: Exception) {
        Log.d("Timetable", e.toString())
      }
    }
  }

  fun sourceChanged(newValue: TimetablePickerSource) {
    _state.update { it.copy(source = newValue) }
  }

  fun yearChanged(newValue: String?) {
    _state.update { it.copy(registryState = fixRegistryState(it.registryState.copy(currentYear = newValue))) }
  }

  fun semesterChanged(newValue: String?) {
    _state.update { it.copy(registryState = fixRegistryState(it.registryState.copy(currentSemester = newValue))) }
  }

  fun sectionChanged(newValue: String?) {
    _state.update { it.copy(registryState = fixRegistryState(it.registryState.copy(currentSection = newValue))) }
  }

  fun fixRegistryState(s: RegistryPickerState): RegistryPickerState {
    val newSections = registryYears[s.currentYear] ?: emptyMap()
    val newSection =
      if (newSections.containsKey(s.currentSection))
        s.currentSection
      else
        null

    val newSemesters = newSections[newSection]?.toSet() ?: setOf()
    val newSemester =
      if (newSemesters.contains(s.currentSemester))
        s.currentSemester
      else
        null

    return s.copy(
      sections = newSections.keys,
      currentSection = newSection,
      semesters = newSemesters,
      currentSemester = newSemester,
    )
  }

  fun localUriChanged(newUri: Uri, newName: String) {
    _state.update {
      it.copy(
        localPickerState = it.localPickerState.copy(
          fileUri = newUri,
          fileName = newName
        )
      )
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
    TimetablePickerScreen()
  }
}