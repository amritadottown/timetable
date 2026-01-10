package town.amrita.timetable.ui.picker

import android.appwidget.AppWidgetManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import retrofit2.await
import town.amrita.timetable.R
import town.amrita.timetable.activity.LocalWidgetId
import town.amrita.timetable.models.DEFAULT_CONFIG
import town.amrita.timetable.models.Timetable
import town.amrita.timetable.models.validate
import town.amrita.timetable.registry.TimetableSpec
import town.amrita.timetable.models.widgetConfig
import town.amrita.timetable.registry.RegistryService
import town.amrita.timetable.registry.RegistryYears
import town.amrita.timetable.ui.LocalGlobalActions
import town.amrita.timetable.ui.TimetableTheme
import town.amrita.timetable.ui.components.DropdownPicker
import town.amrita.timetable.ui.components.LocalSnackbarState
import town.amrita.timetable.ui.components.TimetablePreview
import town.amrita.timetable.ui.components.TimetableScaffold
import town.amrita.timetable.ui.components.TooltipContainer
import town.amrita.timetable.utils.TODAY
import town.amrita.timetable.utils.updateTimetableFromRegistry

@OptIn(ExperimentalSerializationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimetablePickerScreen(
  viewModel: RegistryScreenViewModel = viewModel(),
  goToLocalPicker: () -> Unit = {},
  goToConfig: (Timetable, TimetableSpec) -> Unit = { _, _ -> }
) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val state = viewModel.state.collectAsStateWithLifecycle().value
  val currentWidgetConfig by context.widgetConfig.data.collectAsStateWithLifecycle(DEFAULT_CONFIG)

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
    } catch (_: Exception) {
      fileName.removeSuffix(".json")
    }
  }

  TimetableScaffold(
    title = "Select Timetable",
    actions = {
      TooltipContainer(tooltipContent = "Use Local File") {
        IconButton(onClick = goToLocalPicker) {
          Icon(
            painter = painterResource(R.drawable.file_open_24px),
            contentDescription = "Use Local File"
          )
        }
      }
      (LocalGlobalActions.current)()
    }
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
              showUseCurrentDialog = false
              if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val activity = context as ComponentActivity
                activity.finish()
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

    Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      when (state) {
        TimetablePickerScreenState.IndexLoading ->
          Column(
            Modifier
              .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            CircularWavyProgressIndicator(Modifier.padding(bottom = 152.dp))
          }

        is TimetablePickerScreenState.IndexError ->
          Column(
            Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text("⚠️ Error: ${state.message}")
            Button(
              onClick = viewModel::retryIndexLoad
            ) {
              Text("Retry")
            }
          }

        is TimetablePickerScreenState.Ready ->
          with(state) {
            Column(
              Modifier.padding(horizontal = 24.dp),
              verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              DropdownPicker(
                options = years.toList().sorted(),
                selected = currentYear,
                label = "Start Year",
                onSelectionChanged = viewModel::yearChanged
              )
              if(sections.isEmpty())
                return@Column
              DropdownPicker(
                options = sections.toList().sortedBy { x -> x.split("-").last() },
                selected = currentSection,
                label = "Section",
                onSelectionChanged = viewModel::sectionChanged
              )
              if(semesters.isEmpty())
                return@Column
              DropdownPicker(
                options = semesters.toList().sorted(),
                selected = currentSemester,
                label = "Semester",
                onSelectionChanged = viewModel::semesterChanged
              )
            }
          }
      }

      if (state is TimetablePickerScreenState.Ready) {
        with(state.timetable) {
          var selectedDay by remember { mutableStateOf(TODAY) }

          when (this) {
            TimetableState.NotSelected ->
              Spacer(
                Modifier
                  .weight(1f)
                  .fillMaxSize()
              )

            TimetableState.Loading ->
              Box(
                Modifier
                  .weight(1f)
                  .fillMaxSize()
              ) {
                CircularWavyProgressIndicator(Modifier.align(Alignment.Center))
              }

            is TimetableState.FetchError ->
              Column(
                Modifier
                  .padding(24.dp)
                  .weight(1f)
                  .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text("⚠️ Error: $message")
                Button(
                  onClick = viewModel::retryTimetableLoad
                ) {
                  Text("Retry")
                }
              }

            is TimetableState.ValidationError ->
              Column(
                Modifier
                  .padding(24.dp)
                  .weight(1f)
                  .fillMaxSize()
              ) {
                Text(text = "⚠️ Errors found", fontWeight = FontWeight.Medium)
                errors.map {
                  Text(it)
                }
              }

            is TimetableState.Selected ->
              TimetablePreview(
                Modifier
                  .weight(1f)
                  .fillMaxSize(),
                timetable = timetable,
                day = selectedDay,
                dayChanged = { newDay -> selectedDay = newDay }
              )
          }
        }
      }

      val needsConfig = (state as? TimetablePickerScreenState.Ready)
        ?.timetable
        ?.let { it as? TimetableState.Selected }
        ?.timetable
        ?.config
        ?.isNotEmpty() == true

      Box(Modifier.padding(horizontal = 24.dp)) {
        if (needsConfig) {
          Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = state.timetable is TimetableState.Selected,
            onClick = {
              (state.timetable as? TimetableState.Selected)?.let {
                goToConfig(it.timetable, it.spec)
              }
            }
          ) {
            Text("Continue")
          }
        } else {
          UseTimetableButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = (state as? TimetablePickerScreenState.Ready)?.timetable is TimetableState.Selected,
            onApplyTimetable = {
              ((state as? TimetablePickerScreenState.Ready)
                ?.timetable as? TimetableState.Selected)
                ?.spec
                ?.let { context.updateTimetableFromRegistry(it, emptyMap(), useCurrentConfig = false) }
            }
          )
        }
      }
    }
  }
}

sealed class TimetablePickerScreenState {
  object IndexLoading : TimetablePickerScreenState()
  data class IndexError(val message: String) : TimetablePickerScreenState()
  data class Ready(
    val years: Set<String> = setOf(),
    val currentYear: String? = null,
    val sections: Set<String> = setOf(),
    val currentSection: String? = null,
    val semesters: Set<String> = setOf(),
    val currentSemester: String? = null,

    val timetable: TimetableState = TimetableState.NotSelected
  ) : TimetablePickerScreenState()
}

sealed class TimetableState {
  object NotSelected : TimetableState()
  object Loading : TimetableState()
  data class FetchError(val message: String) : TimetableState()
  data class ValidationError(val errors: List<String>) : TimetableState()
  data class Selected(val spec: TimetableSpec, val timetable: Timetable) : TimetableState()
}

class RegistryScreenViewModel : ViewModel() {
  private val _state =
    MutableStateFlow<TimetablePickerScreenState>(TimetablePickerScreenState.IndexLoading)
  val state = _state.asStateFlow()

  private lateinit var registryYears: RegistryYears

  init {
    viewModelScope.launch {
      _state.value = TimetablePickerScreenState.IndexLoading
      try {
        registryYears = RegistryService.instance.getRegistry().await().timetables
        _state.value = TimetablePickerScreenState.Ready(years = registryYears.keys)
      } catch (e: Exception) {
        Log.d("Timetable", e.toString())
        _state.value = TimetablePickerScreenState.IndexError(e.message.toString())
      }
    }
  }

  fun loadIndex() {
    viewModelScope.launch {
      _state.value = TimetablePickerScreenState.IndexLoading
      try {
        registryYears = RegistryService.instance.getRegistry().await().timetables
        _state.value = TimetablePickerScreenState.Ready(years = registryYears.keys)
      } catch (e: Exception) {
        Log.d("Timetable", e.toString())
        _state.value = TimetablePickerScreenState.IndexError(e.message.toString())
      }
    }
  }

  fun updateIfReady(transform: (TimetablePickerScreenState.Ready) -> TimetablePickerScreenState) {
    _state.update {
      if (it is TimetablePickerScreenState.Ready) transform(it) else it
    }
  }

  fun yearChanged(newValue: String?) {
    updateIfReady { fixRegistryState(it.copy(currentYear = newValue), setCurrentSemester = true) }
    tryLoadTimetable()
  }

  fun sectionChanged(newValue: String?) {
    updateIfReady { fixRegistryState(it.copy(currentSection = newValue), setCurrentSemester = true) }
    tryLoadTimetable()
  }

  fun semesterChanged(newValue: String?) {
    updateIfReady { fixRegistryState(it.copy(currentSemester = newValue)) }
    tryLoadTimetable()
  }

  fun fixRegistryState(s: TimetablePickerScreenState.Ready, setCurrentSemester: Boolean = false): TimetablePickerScreenState.Ready {
    val newSections = registryYears[s.currentYear] ?: emptyMap()
    val newSection = when (s.currentSection) {
      in newSections -> s.currentSection
      else -> null
    }

    val newSemesters = newSections[newSection]?.toSet() ?: setOf()
    val newSemester = when {
      s.currentSemester in newSemesters -> s.currentSemester
      setCurrentSemester && !newSemesters.isEmpty() -> newSemesters.last()
      else -> null
    }

    return s.copy(
      sections = newSections.keys,
      currentSection = newSection,
      semesters = newSemesters,
      currentSemester = newSemester,
    )
  }

  fun tryLoadTimetable() {
    viewModelScope.launch {
      val s = _state.value
      if (s !is TimetablePickerScreenState.Ready)
        return@launch

      if (registryYears[s.currentYear]?.get(s.currentSection)
          ?.contains(s.currentSemester) != true
      ) {
        updateIfReady {
          it.copy(timetable = TimetableState.NotSelected)
        }
        return@launch
      }

      val spec = TimetableSpec(
        s.currentYear.toString(), s.currentSection.toString(),
        s.currentSemester.toString()
      )

      if ((s.timetable as? TimetableState.Selected)?.spec == spec)
        return@launch

      updateIfReady { it.copy(timetable = TimetableState.Loading) }
      try {
        val tt = RegistryService.instance.getTimetable(spec).await()
        val errors = tt.validate()

        updateIfReady {
          it.copy(
            timetable =
              if (errors.isEmpty()) TimetableState.Selected(spec, tt)
              else TimetableState.ValidationError(errors)
          )
        }

      } catch (e: Exception) {
        Log.d("Timetable", "Failed to fetch timetable: $e")
        updateIfReady { it.copy(timetable = TimetableState.FetchError(e.message.toString())) }
      }
    }
  }

  fun retryIndexLoad() {
    loadIndex()
  }

  fun retryTimetableLoad() {
    tryLoadTimetable()
  }
}

@Composable
@Preview
fun TimetablePickerScreenPreview() {
  TimetableTheme {
    TimetablePickerScreen()
  }
}