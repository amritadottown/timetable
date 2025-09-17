package town.amrita.timetable.ui

import android.appwidget.AppWidgetManager
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import retrofit2.await
import town.amrita.timetable.activity.LocalWidgetId
import town.amrita.timetable.models.DEFAULT_CONFIG
import town.amrita.timetable.models.Timetable
import town.amrita.timetable.models.TimetableSpec
import town.amrita.timetable.models.validateSchedule
import town.amrita.timetable.models.widgetConfig
import town.amrita.timetable.registry.RegistryService
import town.amrita.timetable.registry.RegistryYears
import town.amrita.timetable.ui.components.DropdownPicker
import town.amrita.timetable.ui.components.LocalSnackbarState
import town.amrita.timetable.ui.components.TimetablePreview
import town.amrita.timetable.ui.components.TimetableScaffold
import town.amrita.timetable.utils.getDisplayName
import town.amrita.timetable.utils.getFileContent
import town.amrita.timetable.utils.updateTimetableFromRegistry
import town.amrita.timetable.utils.updateTimetableFromUri
import town.amrita.timetable.widget.Sizes
import town.amrita.timetable.widget.TimetableAppWidget
import town.amrita.timetable.widget.TimetableWidgetReceiver

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun TimetablePickerScreen(
  viewModel: RegistryScreenViewModel = viewModel()
) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val state by viewModel.state.collectAsStateWithLifecycle()
  val currentWidgetConfig by context.widgetConfig.data.collectAsStateWithLifecycle(DEFAULT_CONFIG)

  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      Log.d("Timetable", uri?.toString() ?: "no uri")
      if (uri != null) {
        val fileName = context.getDisplayName(uri)
        viewModel.localUriChanged(uri, fileName)
      }
    }

  val selectedTab =
    listOf(TimetablePickerSource.Registry, TimetablePickerSource.Local).indexOf(state.source)

  var timetableSelected by remember { mutableStateOf(false) }
  var timetable by remember { mutableStateOf<Timetable?>(null) }
  val validationResult =
    remember(timetable) { timetable?.let { validateSchedule(it) } ?: emptyList() }

  val widgetId = LocalWidgetId.current
  val isWidgetConfiguration = widgetId != AppWidgetManager.INVALID_APPWIDGET_ID
  val hasExistingTimetable = currentWidgetConfig.file != null

  var showUseCurrentDialog by remember { mutableStateOf(false) }

  LaunchedEffect(isWidgetConfiguration, hasExistingTimetable) {
    if (isWidgetConfiguration && hasExistingTimetable) {
      showUseCurrentDialog = true
    }
  }

  val currentTimetableSpec = currentWidgetConfig.file?.let { fileName ->
    try {
      if (currentWidgetConfig.isLocal) {
        fileName.removeSuffix(".json")
      } else {
        val spec = TimetableSpec.fromString(fileName.removeSuffix(".json"))
        "${spec.year}, ${spec.section}, semester ${spec.semester}"
      }
    } catch (e: Exception) {
      fileName.removeSuffix(".json")
    }
  }

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
          try {
            timetable = RegistryService.instance.getTimetable(newSpec).await()
          } catch (e: Exception) {
            Log.d("Timetable", "Failed to fetch timetable: $e")
            viewModel.setRegistryTimetableError("Failed to load timetable: ${e.message}")
          }
        } else {
          timetableSelected = false
        }
      }

      TimetablePickerSource.Local -> {
        timetableSelected = false
        state.localPickerState.fileUri?.let {
          try {
            timetable = context.getFileContent(it)
            timetableSelected = true
          } catch (e: Exception) {
            Log.d("Timetable", "Failed to read local file: $e")
            timetableSelected = true
            viewModel.setLocalTimetableError("Failed to read file: ${e.message}")
          }
        }
      }
    }
  }

  TimetableScaffold(
    title = "Select Timetable"
  ) {
    val snackbarHostState = LocalSnackbarState.current

    if (showUseCurrentDialog) {
      AlertDialog(
        onDismissRequest = { showUseCurrentDialog = false },
        title = { Text("Use Current Timetable?") },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("You have a timetable already configured:")
            currentTimetableSpec?.let { spec ->
              Text(
                text = spec,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
              )
            }
            Text(
              text = "Would you like to continue using it?",
            )
          }
        },
        confirmButton = {
          TextButton(
            onClick = {
              scope.launch {
                try {
                  showUseCurrentDialog = false
                  if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    val activity = context as ComponentActivity
                    activity.finish()
                  }
                } catch (e: Exception) {
                  Log.d("Timetable", "Error using existing timetable: $e")
                  snackbarHostState.showSnackbar(
                    message = "Error using existing timetable. Please try again.",
                    withDismissAction = true
                  )
                }
              }
            }
          ) {
            Text("Use Current")
          }
        },
        dismissButton = {
          TextButton(
            onClick = { showUseCurrentDialog = false }
          ) {
            Text("Choose Different")
          }
        }
      )
    }

    Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(24.dp)) {

      SecondaryTabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent) {
        Tab(
          selected = state.source == TimetablePickerSource.Registry,
          onClick = { viewModel.sourceChanged(TimetablePickerSource.Registry) },
          text = { Text("Online") }
        )
        Tab(
          selected = state.source == TimetablePickerSource.Local,
          onClick = { viewModel.sourceChanged(TimetablePickerSource.Local) },
          text = { Text("Local") }
        )
      }

      when (state.source) {
        TimetablePickerSource.Registry -> {
          with(state.registryState) {
            if (ready) {
              if (indexError != null) {
                Column(
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Text("⚠️ Error: $indexError")
                  Button(
                    onClick = { viewModel.retryIndexLoad() }
                  ) {
                    Text("Retry")
                  }
                }
              } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  DropdownPicker(
                    options = years.toList(),
                    selected = currentYear,
                    label = "Start Year",
                    onSelectionChanged = { viewModel.yearChanged(it) })
                  DropdownPicker(
                    options = sections.toList(),
                    selected = currentSection,
                    label = "Section",
                    onSelectionChanged = { viewModel.sectionChanged(it) })
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
                viewModel.localUriChanged(null, null)
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
        val currentTimetableError = when (state.source) {
          TimetablePickerSource.Registry -> state.registryState.timetableError
          TimetablePickerSource.Local -> state.localPickerState.timetableError
        }
        
        if (currentTimetableError != null) {
          Column(
            Modifier.weight(1f).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text("⚠️ Error: $currentTimetableError")
            Button(
              onClick = { viewModel.retryTimetableLoad() }
            ) {
              Text("Retry")
            }
          }
        } else if (!validationResult.isEmpty()) {
          Column(Modifier.weight(1f).fillMaxSize()) {
            Text(text = "⚠️ Errors found", fontWeight = FontWeight.Medium)
            validationResult.map {
              Text(it)
            }
          }
        } else {
          TimetablePreview(
            Modifier
              .weight(1f)
              .fillMaxSize(), timetable = timetable
          )
        }
      } else {
        Spacer(
          Modifier
            .weight(1f)
            .fillMaxSize()
        )
      }

      Button(
        modifier = Modifier.fillMaxWidth(),
        enabled = timetableSelected && validationResult.isEmpty() && 
                 state.registryState.timetableError == null && 
                 state.localPickerState.timetableError == null,
        onClick = {
          scope.launch {
            try {
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

              if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val activity = context as ComponentActivity
                activity.finish()
              } else {
                val appWidgetManager = GlanceAppWidgetManager(context)
                if (appWidgetManager.getGlanceIds(TimetableAppWidget::class.java).isEmpty()) {
                  if (!appWidgetManager.requestPinGlanceAppWidget(
                      TimetableWidgetReceiver::class.java,
                      TimetableAppWidget(), Sizes.BEEG
                    )
                  ) {
                    snackbarHostState.showSnackbar(
                      message = "Timetable updated. Place the Timetable widget on your home screen to see it.",
                      withDismissAction = true
                    )
                  }
                } else {
                  snackbarHostState.showSnackbar(
                    message = "Timetable updated",
                    withDismissAction = true
                  )
                }
              }
            } catch (e: Exception) {
              Log.d("Timetable", "Error using timetable: $e")
              snackbarHostState.showSnackbar(
                message = "Error updating timetable. Please try again.",
                withDismissAction = true
              )
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
  val currentSemester: String? = null,
  val indexError: String? = null,
  val timetableError: String? = null
)

data class LocalPickerState(
  val fileUri: Uri? = null,
  val fileName: String? = null,
  val timetableError: String? = null
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
              ready = true,
              indexError = null
            )
          )
        }
      } catch (e: Exception) {
        Log.d("Timetable", e.toString())
        _state.update { s ->
          s.copy(
            registryState = s.registryState.copy(
              ready = true,
              indexError = "Failed to load timetable index: ${e.message}"
            )
          )
        }
      }
    }
  }

  fun sourceChanged(newValue: TimetablePickerSource) {
    _state.update { it.copy(source = newValue) }
  }

  fun yearChanged(newValue: String?) {
    _state.update { it.copy(registryState = fixRegistryState(it.registryState.copy(currentYear = newValue, timetableError = null))) }
  }

  fun semesterChanged(newValue: String?) {
    _state.update { it.copy(registryState = fixRegistryState(it.registryState.copy(currentSemester = newValue, timetableError = null))) }
  }

  fun sectionChanged(newValue: String?) {
    _state.update { it.copy(registryState = fixRegistryState(it.registryState.copy(currentSection = newValue, timetableError = null))) }
  }

  fun fixRegistryState(s: RegistryPickerState): RegistryPickerState {
    val newSections = registryYears[s.currentYear] ?: emptyMap()
    val newSection = when (s.currentSection) {
      in newSections -> s.currentSection
      else -> null
    }

    val newSemesters = newSections[newSection]?.toSet() ?: setOf()
    val newSemester = when {
      s.currentSemester in newSemesters -> s.currentSemester
      !newSemesters.isEmpty() -> newSemesters.last()
      else -> null
    }

    return s.copy(
      sections = newSections.keys,
      currentSection = newSection,
      semesters = newSemesters,
      currentSemester = newSemester,
    )
  }

  fun localUriChanged(newUri: Uri?, newName: String?) {
    _state.update {
      it.copy(
        localPickerState = it.localPickerState.copy(
          fileUri = newUri,
          fileName = newName,
          timetableError = null
        )
      )
    }
  }

  fun setRegistryTimetableError(error: String) {
    _state.update {
      it.copy(
        registryState = it.registryState.copy(timetableError = error)
      )
    }
  }

  fun setLocalTimetableError(error: String) {
    _state.update {
      it.copy(
        localPickerState = it.localPickerState.copy(timetableError = error)
      )
    }
  }

  fun retryIndexLoad() {
    _state.update {
      it.copy(
        registryState = it.registryState.copy(
          ready = false,
          indexError = null
        )
      )
    }
    viewModelScope.launch {
      try {
        registryYears = RegistryService.instance.getRegistry().await().timetables
        _state.update { e ->
          e.copy(
            registryState = e.registryState.copy(
              years = registryYears.keys,
              ready = true,
              indexError = null
            )
          )
        }
      } catch (e: Exception) {
        Log.d("Timetable", e.toString())
        _state.update { s ->
          s.copy(
            registryState = s.registryState.copy(
              ready = true,
              indexError = "Failed to load timetable index: ${e.message}"
            )
          )
        }
      }
    }
  }

  fun retryTimetableLoad() {
    when (_state.value.source) {
      TimetablePickerSource.Registry -> {
        _state.update {
          it.copy(
            registryState = it.registryState.copy(timetableError = null)
          )
        }
      }
      TimetablePickerSource.Local -> {
        _state.update {
          it.copy(
            localPickerState = it.localPickerState.copy(timetableError = null)
          )
        }
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