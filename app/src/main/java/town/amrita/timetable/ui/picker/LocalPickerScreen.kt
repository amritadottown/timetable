package town.amrita.timetable.ui.picker

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import town.amrita.timetable.models.Timetable
import town.amrita.timetable.models.validate
import town.amrita.timetable.models.validateSubjectReference
import town.amrita.timetable.ui.components.TimetablePreview
import town.amrita.timetable.ui.components.TimetableScaffold
import town.amrita.timetable.utils.getDisplayName
import town.amrita.timetable.utils.getFileContent
import town.amrita.timetable.utils.updateTimetableFromUri

@Composable
fun LocalPickerScreen(
  viewModel: LocalPickerScreenViewModel = viewModel(),
  goToConfig: (Timetable, Uri) -> Unit = { _, _ -> }
) {
  val state = viewModel.state.collectAsStateWithLifecycle().value
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  val readFromUri = { uri: Uri ->
    scope.launch {
      val filename = context.getDisplayName(uri)
      try {
        val tt = withContext(Dispatchers.IO) { context.getFileContent(uri) }
        viewModel.fileSelected(uri, filename, tt)
      } catch (e: Exception) {
        viewModel.fileReadError(uri, filename, e.message.toString())
      }
    }
  }

  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      Log.d("Timetable", uri?.toString() ?: "no uri")
      uri?.let(readFromUri)
    }

  TimetableScaffold(title = "Select File") {
    Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        FilledTonalButton(onClick = {
          launcher.launch(arrayOf("application/json"))
        }) {
          Text("Pick File")
        }

        (state as? LocalPickerScreenState.Selected)?.filename?.let {
          Text(it)
        }
      }

      when (state) {
        LocalPickerScreenState.NotSelected -> Spacer(Modifier
          .weight(1f)
          .fillMaxSize())

        is LocalPickerScreenState.Selected ->
          with(state.timetable) {
            when (this) {
              is LocalTimetableState.ReadError ->
                Column(
                  Modifier
                    .weight(1f)
                    .fillMaxSize(),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Text("⚠️ Error: $message")
                  Button(
                    onClick = { readFromUri(state.uri) }
                  ) {
                    Text("Retry")
                  }
                }

              is LocalTimetableState.ValidationError ->
                Column(
                  Modifier
                    .weight(1f)
                    .fillMaxSize()
                ) {
                  Text(text = "⚠️ Errors found", fontWeight = FontWeight.Medium)
                  errors.map {
                    Text(it)
                  }
                }

              is LocalTimetableState.Selected ->
                TimetablePreview(
                  Modifier
                    .weight(1f)
                    .fillMaxSize(), 
                  timetable = timetable
                )
            }
          }
      }

      val needsConfig = (state as? LocalPickerScreenState.Selected)
        ?.timetable
        ?.let { it as? LocalTimetableState.Selected }
        ?.timetable
        ?.config
        ?.isNotEmpty() == true

      if (needsConfig) {
        Button(
          modifier = Modifier.fillMaxWidth(),
          enabled = state.timetable is LocalTimetableState.Selected,
          onClick = {
            val timetableState = state.timetable as? LocalTimetableState.Selected

            if (timetableState != null) {
              goToConfig(timetableState.timetable, state.uri)
            }
          }
        ) {
          Text("Continue")
        }
      } else {
        UseTimetableButton(
          modifier = Modifier.fillMaxWidth(),
          enabled = (state as? LocalPickerScreenState.Selected)?.timetable is LocalTimetableState.Selected,
          onApplyTimetable = {
            val selectedState = state as? LocalPickerScreenState.Selected

            if (selectedState != null) {
              context.updateTimetableFromUri(selectedState.uri)
            }
          }
        )
      }
    }
  }
}

sealed class LocalPickerScreenState {
  object NotSelected : LocalPickerScreenState()
  data class Selected(val uri: Uri, val filename: String, val timetable: LocalTimetableState) :
    LocalPickerScreenState()
}

sealed class LocalTimetableState {
  data class ReadError(val message: String) : LocalTimetableState()
  data class ValidationError(val errors: List<String>) : LocalTimetableState()
  data class Selected(val timetable: Timetable) : LocalTimetableState()
}

class LocalPickerScreenViewModel : ViewModel() {
  private val _state =
    MutableStateFlow<LocalPickerScreenState>(LocalPickerScreenState.NotSelected)
  val state = _state.asStateFlow()

  fun fileSelected(uri: Uri, filename: String, timetable: Timetable) {
    val errors = timetable.validate()
    _state.value =
      LocalPickerScreenState.Selected(
        uri, filename,
        if (errors.isEmpty())
          LocalTimetableState.Selected(timetable)
        else
          LocalTimetableState.ValidationError(errors)
      )
  }

  fun fileReadError(uri: Uri, filename: String, message: String) {
    _state.value =
      LocalPickerScreenState.Selected(uri, filename, LocalTimetableState.ReadError(message))
  }
}